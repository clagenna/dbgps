package sm.clagenna.dbgps.javafx;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.clagenna.dbgps.sql.GestDBSqlServer;
import sm.clagenna.dbgps.sql.GestDbSqlite;
import sm.clagenna.stdcla.enums.EServerId;
import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoFormatter;
import sm.clagenna.stdcla.geo.GeoGpxParser;
import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.geo.GeoScanJpg;
import sm.clagenna.stdcla.geo.fromgoog.GeoConvGpx;
import sm.clagenna.stdcla.geo.fromgoog.JacksonParseRecurse;
import sm.clagenna.stdcla.sys.ex.GeoFileException;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.SecPwd;

@Data
@EqualsAndHashCode(callSuper = false)
public class DataModelGpsInfo {
  private static final Logger s_log = LogManager.getLogger(DataModelGpsInfo.class);

  private static final String CSZ_PROP_SRCDIR = "source.dir";
  private static final String CSZ_PROP_SRCTIP = "source.tipo";

  private static DataModelGpsInfo s_inst;

  public enum ThreadWork {
    ParseSource, //
    LeggiDB, //
    salvaDB, //
    SaveToGPX, //
    RinominaFotoFile
  }

  private Path         srcDir;
  private boolean      invalidSrc;
  private EGeoSrcCoord tipoSource;
  private boolean      showGMS;
  private ThreadWork   tipoThread;

  private EServerId tipoDB;
  private Path      dbName;
  private String    dbHost;
  private Integer   dbService;
  private String    dbUser;
  private String    dbPaswd;

  private static final String CSZ_PROP_GPXFILE = "gpx.file";
  private boolean             invalidGPX;
  private Path                destGPXfile;
  private boolean             expLanciaBaseCamp;

  public FiltroGeoCoord filtro;

  private GeoList       geoList;
  private boolean       dateTimeUnique;
  private LocalDateTime updDtGeo;
  private Double        updLongitude;
  private Double        updLatitude;
  private EGeoSrcCoord  updTipoSource;

  public DataModelGpsInfo() {
    s_inst = this;
    clearFiltro();
  }

  public static DataModelGpsInfo getInstance() {
    return s_inst;
  }

  public void clearFiltro() {
    if (filtro == null)
      filtro = new FiltroGeoCoord();
    filtro.clear();
    s_log.debug("Pulito il filtro ricerca");
  }

  public GeoCoord getMingeo() {
    if (geoList == null)
      return new GeoCoord();
    return geoList.getMingeo();
  }

  public GeoCoord getMaxgeo() {
    if (geoList == null)
      return new GeoCoord();
    return geoList.getMaxgeo();
  }

  public GeoList getGeoList() {
    if ( !filtro.isActive() || geoList == null)
      return geoList;
    List<GeoCoord> li = geoList.stream().filter(geo -> filtro.isGood(geo)).toList();
    GeoList nLi = new GeoList();
    nLi.setBUniqueTs(isDateTimeUnique());
    nLi.addAll(li);
    return nLi;
  }

  public void setTipoSource(EGeoSrcCoord pv) {
    tipoSource = pv;
    String szKeyProp = String.format("%s.%s", CSZ_PROP_SRCDIR, tipoSource.toString());
    String sz = AppProperties.getInstance().getProperty(szKeyProp);
    if (sz != null)
      setSrcDir(Paths.get(sz));
  }

  public void setSrcDir(Path pth) {
    if (pth == null) {
      srcDir = null;
      tipoSource = null;
      return;
    }
    if (Files.exists(pth)) {
      srcDir = pth;
      boolean bIsDir = Files.isDirectory(pth, LinkOption.NOFOLLOW_LINKS);
      String sz = pth.toString();
      int n = sz.lastIndexOf(".");
      String szExt = null;
      if (n > 0)
        szExt = sz.substring(n + 1).toLowerCase();
      if (szExt != null) {
        switch (szExt) {
          case "json":
            tipoSource = EGeoSrcCoord.google;
            break;
          case "gpx":
            tipoSource = EGeoSrcCoord.track;
            break;
          default:
            if (bIsDir)
              tipoSource = EGeoSrcCoord.foto;
            break;
        }
      }
      if (tipoSource != null) {
        setInvalidSrc(false);
        String szKeyProp = String.format("%s.%s", CSZ_PROP_SRCDIR, tipoSource.toString());
        AppProperties.getInstance().setProperty(szKeyProp, pth.toString());
      }
    }
  }

  public void setDestDB(Path pth) {
    dbName = null;
    // tipoDB = null;
    if (pth == null)
      return;
    //    if (Files.exists(pth)) {
    dbName = pth;
    String sz = pth.toString();
    int n = sz.lastIndexOf(".");
    String szExt = null;
    if (n > 0)
      szExt = sz.substring(n + 1).toLowerCase();
    if (szExt != null) {
      switch (szExt) {
        case "sqlite":
          tipoDB = EServerId.SQLite;
          break;
        case "sqlite3":
          tipoDB = EServerId.SQLite3;
          break;
        default:
          break;
      }
    }
    //  }
  }

  public void readProperties(AppProperties p_props) {
    String sz = p_props.getProperty(CSZ_PROP_SRCDIR);
    if (sz != null && sz.length() > 2) {
      srcDir = Paths.get(sz);
      sz = p_props.getProperty(CSZ_PROP_SRCTIP);
      if (sz != null)
        tipoSource = EGeoSrcCoord.valueOf(sz);
    }

    tipoDB = null;
    sz = p_props.getProperty(AppProperties.CSZ_PROP_DB_Type);
    if (sz != null)
      tipoDB = EServerId.valueOf(sz);
    readPropByTpDB();
    sz = p_props.getProperty(CSZ_PROP_GPXFILE);
    if (sz != null)
      destGPXfile = Paths.get(sz);
  }

  private void readPropByTpDB() {
    AppProperties props = AppProperties.getInstance();
    String sz;
    sz = props.getProperty(getPropDB(tipoDB, AppProperties.CSZ_PROP_DB_Host));
    if (sz != null)
      dbHost = sz;

    dbService = null;
    sz = props.getProperty(getPropDB(tipoDB, AppProperties.CSZ_PROP_DB_service));
    if (sz != null)
      dbService = Integer.parseInt(sz);

    sz = props.getProperty(getPropDB(tipoDB, AppProperties.CSZ_PROP_DB_name));
    if (sz != null)
      dbName = Paths.get(sz);

    dbUser = null;
    sz = props.getProperty(getPropDB(tipoDB, AppProperties.CSZ_PROP_DB_user));
    if (sz != null)
      dbUser = sz;

    dbPaswd = null;
    sz = props.getProperty(getPropDB(tipoDB, AppProperties.CSZ_PROP_DB_passwd));
    if (sz != null) {
      SecPwd sec = new SecPwd();
      sz = sec.decrypt(sz);
      dbPaswd = sz;
    }
  }

  public void setTipoDB(EServerId p_Id) {
    if (tipoDB == p_Id)
      return;
    tipoDB = p_Id;
    readPropByTpDB();
  }

  public void saveProperties(AppProperties p_props) {
    if (srcDir != null) {
      p_props.setProperty(CSZ_PROP_SRCDIR, srcDir.toAbsolutePath().toString());
      if (tipoSource != null)
        p_props.setProperty(CSZ_PROP_SRCTIP, tipoSource.toString());
    }

    if (tipoDB != null)
      p_props.setProperty(AppProperties.CSZ_PROP_DB_Type, tipoDB.toString());
    if (dbName != null) {
      p_props.setProperty(getPropDB(tipoDB, AppProperties.CSZ_PROP_DB_name), getDbName().toString());
    }
    if (dbHost != null) {
      p_props.setProperty(getPropDB(tipoDB, AppProperties.CSZ_PROP_DB_Host), getDbHost());
    }
    if (dbService != null) {
      p_props.setProperty(getPropDB(tipoDB, AppProperties.CSZ_PROP_DB_service), getDbService());
    }
    if (dbUser != null) {
      p_props.setProperty(getPropDB(tipoDB, AppProperties.CSZ_PROP_DB_user), getDbUser());
    }
    if (dbPaswd != null) {
      SecPwd sec = new SecPwd();
      String sz = sec.encrypt(dbPaswd);
      p_props.setProperty(getPropDB(tipoDB, AppProperties.CSZ_PROP_DB_passwd), sz);
    }

    if (destGPXfile != null)
      p_props.setProperty(CSZ_PROP_GPXFILE, destGPXfile.toAbsolutePath().toString());
  }

  public String getPropDB(EServerId p_id, String p_prop) {
    String szRet = p_prop;
    if (p_prop.equals(AppProperties.CSZ_PROP_DB_Type))
      return szRet;
    if (p_id != null) {
      String szTp = p_id.toString();
      String[] arr = szRet.split("\\.");
      szRet = String.format("%s.%s.%s", arr[0], szTp, arr[1]);
    }
    return szRet;
  }

  public GeoList initData() {
    if (geoList != null) {
      geoList.clear();
    }
    geoList = new GeoList();
    geoList.setBUniqueTs(isDateTimeUnique());
    return geoList;
  }

  public void leggiDB() {
    switch (tipoDB) {
      case HSqlDB:
        break;
      case SQLite:
      case SQLite3:
        leggiDBSQLite();
        break;
      case SqlServer:
        leggiDBSqlServer();
        break;
      default:
        break;

    }
  }

  private void leggiDBSQLite() {
    try (GestDbSqlite gdb = new GestDbSqlite()) {
      gdb.setDbFileName(dbName);
      gdb.createOrOpenDatabase();
      GeoList li = gdb.readAll();
      if (geoList != null)
        geoList.addAll(li);
      else
        geoList = li;
      s_log.info("Presenti {} rec in Model", geoList.size());
    } catch (Exception e) {
      s_log.error("Errore open SQLite DB, err={}", e.getMessage());
    }
  }

  private void leggiDBSqlServer() {
    try (GestDBSqlServer gdb = new GestDBSqlServer()) {
      gdb.setDbName(dbName);
      gdb.setDbHost(dbHost);
      gdb.setService(dbService);
      gdb.setUser(dbUser);
      gdb.setPasswd(dbPaswd);
      gdb.OpenDatabase();
      GeoList li = gdb.readAll();
      if (geoList != null)
        geoList.addAll(li);
      else
        geoList = li;
      s_log.info("Presenti {} rec in Model", geoList.size());
    } catch (Exception e) {
      s_log.error("Errore open SQLite DB, err={}", e.getMessage());
    }
  }

  public void salvaDB() {
    switch (tipoDB) {
      case HSqlDB:
        break;
      case SQLite:
      case SQLite3:
        salvaDBSQLite();
        break;
      case SqlServer:
        salvaDBSqlServer();
        break;
      default:
        break;

    }

  }

  private void salvaDBSQLite() {
    s_log.info("Salva DB SQLite su {}", dbName.toString());
    try (GestDbSqlite gdb = new GestDbSqlite()) {
      gdb.setDbFileName(dbName);
      gdb.backupDB();
      gdb.setOverWrite(true);
      gdb.createOrOpenDatabase();
      gdb.saveDB(geoList);
    } catch (IOException e) {
      s_log.error("Errore salva SQLite DB, err={}", e.getMessage(), e);
    }
  }

  private void salvaDBSqlServer() {
    s_log.info("Salva DB SqlServer su {}", dbName.toString());
    try (GestDBSqlServer gdb = new GestDBSqlServer()) {
      gdb.setDbName(dbName.getFileName());
      gdb.setDbHost(dbHost);
      gdb.setService(dbService);
      gdb.setUser(dbUser);
      gdb.setPasswd(dbPaswd);
      gdb.setOverWrite(true);
      gdb.OpenDatabase();
      gdb.saveDB(geoList);
    } catch (IOException e) {
      s_log.error("Errore salva SQLite DB, err={}", e.getMessage(), e);
    }
  }

  public static boolean confirmationDialog(Alert.AlertType alertType, String statement) {
    Alert alert = new Alert(alertType, statement);
    alert.setTitle("Salva DB");
    alert.getButtonTypes().addAll(ButtonType.CANCEL);
    Optional<ButtonType> choose = alert.showAndWait();
    return choose.get() == ButtonType.OK;
  }

  public void parseSource() {
    if (geoList == null)
      initData();

    switch (tipoSource) {
      case foto:
        parseExifFotos();
        break;
      case google:
        parseJsonTracks();
        break;
      case track:
        parseGpxFile();
        break;
      default:
        s_log.warn("Non riesco ad interpretare tipo sorg:{}", tipoSource);
        break;
    }
  }

  private void parseJsonTracks() {
    GeoFormatter.setShowLink(true);
    JacksonParseRecurse geoParse = new JacksonParseRecurse();
    s_log.info("Starting parse of {}", srcDir);
    GeoList li = geoParse.parseGeo(srcDir);
    if (geoList == null || geoList.size() == 0)
      geoList = li;
    else
      geoList.addAll(li);
    addAllExisting();
  }

  private void addAllExisting() {
    s_log.debug("Sort by timeStamp of {}", srcDir.getFileName().toString());
    geoList.setBUniqueTs(isDateTimeUnique());
    geoList.sortByTStamp();
    s_log.debug("filter nearest of {}", srcDir.getFileName().toString());
    GeoList liNn = geoList.filterNearest();
    geoList.clear();
    geoList = null;
    geoList = liNn;
    geoList.setBUniqueTs(isDateTimeUnique());
    s_log.debug("Sorted and filtered of {}", srcDir.getFileName().toString());
  }

  private void parseExifFotos() {
    GeoScanJpg scj = new GeoScanJpg(geoList);
    try {
      scj.scanDir(srcDir);
      addAllExisting();
    } catch (GeoFileException e) {
      s_log.error("Errore scan JPG Fotos in {}, errr={}", srcDir.toString(), e.getMessage());
    }
  }

  private void parseGpxFile() {
    GeoGpxParser gpxp = new GeoGpxParser();
    try {
      GeoList li = gpxp.parseGpx(srcDir);
      if (li != null && li.size() > 0) {
        if (geoList != null) {
          geoList.addAll(li);
          addAllExisting();
        } else
          geoList = li;
      }
    } catch (Exception e) {
      s_log.error("Errore scan GPX file {}, errr={}", srcDir.toString(), e.getMessage());
    }
  }

  public void saveToGPX() {
    if (destGPXfile == null) {
      s_log.error("Non ha specificato nessun file di destinazione GPX");
      return;
    }
    if (geoList == null || geoList.size() == 0) {
      s_log.warn("Non ci sono dati da Salvare");
      return;
    }
    GeoList li = getGeoList();
    int nRec = li.size();
    s_log.info("Salvo {} recs su file GPX {}", nRec, destGPXfile.toString());
    GeoConvGpx togpx = new GeoConvGpx();

    togpx.setDestGpxFile(destGPXfile);
    togpx.setListGeo(li);
    togpx.setOverwrite(true);
    togpx.saveToGpx();
    s_log.info("Salvato GPX to {}", togpx.getDestGpxFile().toString());
    try {
      if (expLanciaBaseCamp) {
        s_log.info("Lancio programma associato a {}", togpx.getDestGpxFile().toString());
        Desktop.getDesktop().open(togpx.getDestGpxFile().toFile());
      }
    } catch (IOException e) {
      s_log.error("Lancio \"{}\", err={}", togpx.getDestGpxFile().toString(), e.getMessage());
    }
  }

  public void rinominaFotoFiles() {
    GeoList li = getGeoList();
    if (li == null || li.size() == 0) {
      s_log.warn("nessuna foto da rinominare (insieme vuoto)!");
      return;
    }
    long conta = li //
        .stream() //
        .filter( //
            geo -> geo.hasFotoFile()) //
        .count();
    if (conta == 0) {
      s_log.warn("nessuna foto da rinominare!");
      return;
    }
    li //
        .stream() //
        .filter( //
            geo -> geo.hasFotoFile())
        .forEach(geo -> renameFotoFile(geo));

  }

  public void saveFotoFile(GeoCoord p_updGeo) {
    s_log.debug("Cambio nome/coordinate alla foto \"{}\"", p_updGeo.getFotoFile().toString());
    GeoScanJpg scj = new GeoScanJpg(geoList);
    scj.cambiaGpsCoordinate(p_updGeo);
  }

  public Object renameFotoFile(GeoCoord p_geo) {
    s_log.debug("Cambio il nome a \"{}\" in base a suo Time Stamp", p_geo.getFotoFile().toString());
    GeoScanJpg scj = new GeoScanJpg(geoList);
    scj.cambiaNome(p_geo);
    return null;
  }

  public Object setDbStrService(String p_text) {
    if (p_text == null || p_text.length() == 0)
      return null;
    try {
      setDbService(Integer.valueOf(p_text));
    } catch (Exception e) {
      s_log.error("Errore valore del DB service=\"{}\", err={}", p_text, e.getMessage());
    }
    return null;
  }

  public String execute() {
    s_log.debug("Start di " + tipoThread);
    switch (tipoThread) {
      case LeggiDB:
        leggiDB();
        break;
      case salvaDB:
        salvaDB();
        break;
      case ParseSource:
        parseSource();
        break;
      case RinominaFotoFile:
        rinominaFotoFiles();
        break;
      case SaveToGPX:
        saveToGPX();
        break;
      default:
        break;

    }
    s_log.debug("Fine di di " + tipoThread);
    return String.format("Fine %s ...", tipoThread.toString());
  }

}

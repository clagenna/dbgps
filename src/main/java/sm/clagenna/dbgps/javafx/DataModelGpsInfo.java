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
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sm.clagenna.dbgps.sql.GestDBSqlServer;
import sm.clagenna.dbgps.sql.GestDbSqlite;
import sm.clagenna.stdcla.geo.EExifPriority;
import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoFormatter;
import sm.clagenna.stdcla.geo.GeoGpxParser;
import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.geo.GeoScanJpg;
import sm.clagenna.stdcla.geo.fromgoog.GeoConvGpx;
import sm.clagenna.stdcla.geo.fromgoog.JacksonParseRecurse;
import sm.clagenna.stdcla.sql.EServerId;
import sm.clagenna.stdcla.sys.ex.GeoFileException;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ParseData;
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

  private Path          srcDir;
  private boolean       invalidSrc;
  private EGeoSrcCoord  tipoSource;
  private EExifPriority priorityInfo;
  private boolean       showGMS;
  private ThreadWork    tipoThread;

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
  private boolean       addSimilFoto;
  private boolean       recurseDirs;
  private boolean       dateTimeUnique;
  private LocalDateTime updDtGeo;
  private Double        updLongitude;
  private Double        updLatitude;
  private EGeoSrcCoord  updTipoSource;

  // TODO Menu context "copia coord"
  // TODO Menu context "copia path completo"

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

  /**
   * Aggiorno l'elemento della lista:
   * <ol>
   * <li>Verifico la presenza del {@link GeoCoord} nella lista presentata
   * (<code>p_li</code>) in base al criterio di uguaglianza
   * {@link GeoCoord#equalSolo(Object)}</li>
   * <li>se presente, imposto i valori del elemento nella lista copiandoci sopra
   * <code>p_geo con </code> con la chiamata a
   * {@link GeoCoord#assign(GeoCoord)}</li>
   * </ol>
   *
   * @param p_li
   *          la lista da aggiornare
   * @param p_geo
   *          l'elemento con cui aggiornare
   * @return l'elemento aggiornato
   */
  public GeoCoord assignGeoInList(List<GeoCoord> p_li, GeoCoord p_geo) {
    GeoCoord geo = null;
    if (null == p_li || null == p_geo) {
      s_log.error("List or Geo is *NULL*");
      return geo;
    }
    List<GeoCoord> l_li = p_li //
        .stream() //
        .filter(s -> s.equalSolo(p_geo)) //
        .collect(Collectors.toList());
    if (null == l_li || l_li.size() == 0) {
      s_log.warn("In Lista *NON* trovo GeoGPS con data = {}", ParseData.s_fmtTs.format(p_geo.getTstamp()));
      return geo;
    }
    if (l_li.size() != 1) {
      s_log.warn("In Lista trovo + di 1 GeoGPS con data = {}", ParseData.s_fmtTs.format(p_geo.getTstamp()));
      // return geo;
    }
    for (GeoCoord geo2 : l_li) {
      geo2.assign(p_geo);
      if (null == geo)
        geo = geo2;
    }
    return geo;
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
    if (null == geoList || geoList.size() == 0)
      s_log.info("non ho letto nulla !");
    else
      s_log.info("Ho {} recs nel registro", geoList.size());
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
    scj.setAddSimilFoto(isAddSimilFoto());
    scj.setExifPrio(priorityInfo);
    scj.setRecurseDirs(recurseDirs);
    try {
      scj.scanDir(srcDir);
      addAllExisting();
    } catch (GeoFileException e) {
      s_log.error("Errore scan JPG Fotos in {}, err={}", srcDir.toString(), e.getMessage());
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
    s_log.warn("Rinomina di tutti i files con seq. priorita: {}", priorityInfo.desc());
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
            geo -> geo.hasFotoFile()) //
        .forEach(geo -> renameFotoFile(geo));

  }

  /**
   * Cambia fisicamente il file delle foto con:
   * <ol>
   * <li>Con il timeStamp aggiorno la Data Acq nelle info Exif</li>
   * <li>Con il timeStamp cambio il nome file</li>
   * <li>aggiorno le coordinate (se presenti) nelle info Exif</li>
   * </ol>
   *
   * @param p_updGeo
   *          il {@link GeoCoord} con le modifiche
   * @return True se ci sono state modifiche
   */
  public boolean saveFotoFile(GeoCoord p_updGeo) {
    s_log.debug("Cambio Ts,nome,coordinate alla foto \"{}\"", p_updGeo.getFotoFile().toString());
    GeoScanJpg scj = new GeoScanJpg(geoList);
    scj.setAddSimilFoto(isAddSimilFoto());
    scj.setExifPrio(priorityInfo);
    boolean bChngTs = scj.cambiaTStamp(p_updGeo);
    scj.cambiaGpsCoordinate(p_updGeo);
    int ii = geoList.indexOf(p_updGeo);
    if (ii >= 0)
      geoList.get(ii).update(p_updGeo);
    if (bChngTs)
      renameFotoFile(p_updGeo);
    return bChngTs;
  }

  public Object renameFotoFile(GeoCoord p_geo) {
    s_log.debug("Cambio il nome a \"{}\" in base a suo Time Stamp", p_geo.getFotoFile().toString());
    GeoScanJpg scj = new GeoScanJpg(geoList);
    scj.setAddSimilFoto(isAddSimilFoto());
    scj.setExifPrio(priorityInfo);
    scj.renameFile(p_geo);
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
    s_log.debug("Start Thread separato per " + tipoThread);
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
    s_log.debug("Fine del thread separato per " + tipoThread);
    return String.format("Fine %s ...", tipoThread.toString());
  }

}

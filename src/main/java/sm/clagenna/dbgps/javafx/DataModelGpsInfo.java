package sm.clagenna.dbgps.javafx;

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
import sm.clagenna.dbgps.cmdline.GestDbSqlite;
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

@Data
public class DataModelGpsInfo {
  private static final Logger s_log = LogManager.getLogger(DataModelGpsInfo.class);

  private static final String CSZ_PROP_SRCDIR = "source.dir";
  private static final String CSZ_PROP_SRCTIP = "source.tipo";

  private static DataModelGpsInfo s_inst;

  private Path         srcDir;
  private boolean      invalidSrc;
  private EGeoSrcCoord tipoSource;
  private boolean      showGMS;

  private static final String CSZ_PROP_DBFILE = "db.dir";
  private static final String CSZ_PROP_DBTIP  = "db.tipo";

  private Path      destDB;
  private EServerId tipoDB;

  private static final String CSZ_PROP_GPXFILE = "gpx.file";
  private boolean             invalidGPX;
  private Path                destGPXfile;

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
    destDB = null;
    // tipoDB = null;
    if (pth == null)
      return;
    //    if (Files.exists(pth)) {
    destDB = pth;
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
    sz = p_props.getProperty(CSZ_PROP_DBFILE);
    if (sz != null) {
      destDB = Paths.get(sz);
      sz = p_props.getProperty(CSZ_PROP_DBTIP);
      if (sz != null)
        tipoDB = EServerId.valueOf(sz);
    }
    sz = p_props.getProperty(CSZ_PROP_GPXFILE);
    if (sz != null)
      destGPXfile = Paths.get(sz);
  }

  public void saveProperties(AppProperties p_props) {
    if (srcDir != null) {
      p_props.setProperty(CSZ_PROP_SRCDIR, srcDir.toAbsolutePath().toString());
      if (tipoSource != null)
        p_props.setProperty(CSZ_PROP_SRCTIP, tipoSource.toString());
    }
    if (destDB != null) {
      p_props.setProperty(CSZ_PROP_DBFILE, destDB.toAbsolutePath().toString());
      if (tipoDB != null)
        p_props.setProperty(CSZ_PROP_DBTIP, tipoDB.toString());
    }
    if (destGPXfile != null)
      p_props.setProperty(CSZ_PROP_GPXFILE, destGPXfile.toAbsolutePath().toString());
  }

  public void initData() {
    if (geoList != null) {
      geoList.clear();
    }
    geoList = new GeoList();
    geoList.setBUniqueTs(isDateTimeUnique());
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
        break;
      default:
        break;

    }
  }

  private void leggiDBSQLite() {
    try (GestDbSqlite gdb = new GestDbSqlite()) {
      gdb.setDbFileName(destDB);
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

  public void salvaDB() {
    switch (tipoDB) {
      case HSqlDB:
        break;
      case SQLite:
      case SQLite3:
        salvaDBSQLite();
        break;
      case SqlServer:
        break;
      default:
        break;

    }

  }

  private void salvaDBSQLite() {
    if (Files.exists(destDB)) {
      if ( !DataModelGpsInfo.confirmationDialog(AlertType.WARNING, "Sicuro di sovrascrivere il file : " + destDB.toString())) {
        s_log.warn("Salva DB SQLite Annullata !");
        return;
      }
    }
    s_log.info("Salva DB SQLite su {}", destDB.toString());
    try (GestDbSqlite gdb = new GestDbSqlite()) {
      gdb.setDbFileName(destDB);
      gdb.backupDB();
      gdb.setOverWrite(true);
      gdb.createOrOpenDatabase();
      gdb.saveDB(geoList);
    } catch (IOException e) {
      e.printStackTrace();
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
    int nRec = geoList.size();
    s_log.info("Salvo {} recs su file GPX {}", nRec, destGPXfile.toString());
    GeoConvGpx togpx = new GeoConvGpx();

    togpx.setDestGpxFile(destGPXfile);
    togpx.setListGeo(geoList);
    togpx.setOverwrite(true);
    togpx.saveToGpx();
    s_log.info("Salvato GPX to {}", togpx.getDestGpxFile().toString());
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
}

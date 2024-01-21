package sm.clagenna.dbgps.javafx;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import sm.clagenna.stdcla.enums.EServerId;
import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoFormatter;
import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.geo.fromgoog.GeoConvGpx;
import sm.clagenna.stdcla.geo.fromgoog.JacksonParseRecurse;
import sm.clagenna.stdcla.utils.AppProperties;

@Data
public class DataModelGpsInfo {
  private static final Logger s_log = LogManager.getLogger(DataModelGpsInfo.class);

  private static final String CSZ_PROP_SRCDIR = "source.dir";
  private static final String CSZ_PROP_SRCTIP = "source.tipo";

  private static DataModelGpsInfo s_inst;

  private Path         srcDir;
  private EGeoSrcCoord tipoSource;
  private boolean      showGMS;

  private static final String CSZ_PROP_DBFILE = "db.dir";
  private static final String CSZ_PROP_DBTIP  = "db.tipo";

  private Path      destDB;
  private EServerId tipoDB;

  private static final String CSZ_PROP_GPXFILE = "gpx.file";
  private Path                destGPXfile;

  private LocalDateTime fltrDtIniz;
  private LocalDateTime fltrDtFine;
  private EGeoSrcCoord  fltrTipoSource;
  private Double        fltrLonMin;
  private Double        fltrLonMax;
  private Double        fltrLatMin;
  private Double        fltrLatMax;

  private GeoList       geoList;
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
    fltrDtIniz = null;
    fltrDtFine = null;
    fltrTipoSource = null;
    fltrLonMin = null;
    fltrLonMax = null;
    fltrLatMin = null;
    fltrLatMax = null;
    s_log.debug("Pulito il filtro ricerca");
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
    }
  }

  public void setDestDB(Path pth) {
    destDB = null;
    tipoDB = null;
    if (pth == null)
      return;
    if (Files.exists(pth)) {
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
    }
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

  public void parseSource() {
    switch (tipoSource) {
      case foto:
        parseExifFotos();
        break;
      case google:
        parseJsonTracks();
        break;
      case track:
        break;
      default:
        parseGpxFile();
        break;

    }
  }

  private void parseJsonTracks() {
    GeoFormatter.setShowLink(true);
    JacksonParseRecurse geoParse = new JacksonParseRecurse();
    s_log.info("Starting parse of {}", srcDir);
    geoList = geoParse.parseGeo(srcDir);
    s_log.info("Sort by timeStamp of {}", srcDir.getFileName().toString());
    geoList.sortByTStamp();
    s_log.info("filter nearest of {}", srcDir.getFileName().toString());
    GeoList liNn = geoList.filterNearest();
    geoList.clear();
    geoList = null;
    geoList = liNn;
  }

  private void parseExifFotos() {
    System.out.println("DataModelGpsInfo.parseExifFotos()");
  }

  private void parseGpxFile() {
    System.out.println("DataModelGpsInfo.parseGpxFile()");
  }

  public void saveToGPX() {
    if (destGPXfile == null) {
      s_log.error("Non ha specificato nessun file di destinazione GPX");
      return;
    }
    if ( geoList == null || geoList.size() == 0) {
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
}

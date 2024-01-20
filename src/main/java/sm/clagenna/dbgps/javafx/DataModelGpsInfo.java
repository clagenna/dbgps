package sm.clagenna.dbgps.javafx;

import java.nio.file.Path;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import sm.clagenna.stdcla.enums.EServerId;
import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.utils.AppProperties;

@Data
public class DataModelGpsInfo {
  private static final Logger s_log = LogManager.getLogger(DataModelGpsInfo.class);

  private Path         srcDir;
  private EGeoSrcCoord tipoSource;

  private Path      destDB;
  private EServerId tipoDB;

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
    clearFiltro();
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

  public void readProperties(AppProperties p_props) {
    // 
  }

  public void saveProperties(AppProperties p_props) {
    // 
  }
}

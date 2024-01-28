package sm.clagenna.dbgps.javafx;

import java.time.LocalDateTime;

import lombok.Data;
import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoCoord;

@Data
public class FiltroGeoCoord {
  public enum Confr {
    OP_LT("<") //
    , OP_LE("<=") //
    , OP_EQ("=") //
    , OP_GE(">=") //
    , OP_GT(">");

    private Confr(String pop) {
      op = pop;
    }

    private String op;

    public String op() {
      return this.op;
    }
  }

  private EGeoSrcCoord  tipoSrc;
  private Boolean       filePresent;
  private LocalDateTime dtMin;
  private Confr         opDtMin;
  private LocalDateTime dtMax;
  private Confr         opDtMax;
  private Double        lonMin;
  private Confr         opLonMin;
  private Double        lonMax;
  private Confr         opLonMax;
  private Double        latMin;
  private Confr         opLatMin;
  private Double        latMax;
  private Confr         opLatMax;
  private boolean       active;
  private String        err;

  public FiltroGeoCoord() {
    active = false;
  }

  public void clear() {
    tipoSrc = null;
    dtMin = null;
    opDtMin = null;
    dtMax = null;
    opDtMax = null;
    lonMin = null;
    opLonMin = null;
    lonMax = null;
    opLonMax = null;
    latMin = null;
    opLatMin = null;
    latMax = null;
    opLatMax = null;
    filePresent = null;
    active = false;
  }

  public boolean isGood(GeoCoord coo) {
    boolean bRet = coo != null;
    if ( !bRet)
      return bRet;
    EGeoSrcCoord tip = coo.getSrcGeo();
    if (tipoSrc != null) {
      if (tipoSrc != tip)
        return false;
    }
    if (dtMin != null) {
      LocalDateTime dt = coo.getTstamp();
      Confr op = opDtMin == null ? Confr.OP_GE : opDtMin;
      if ( !confronta(dtMin, dt, op))
        return false;
    }
    if (dtMax != null) {
      LocalDateTime dt = coo.getTstamp();
      Confr op = opDtMax == null ? Confr.OP_LE : opDtMax;
      if ( !confronta(dtMax, dt, op))
        return false;
    }
    if (lonMin != null) {
      Confr op = opLonMin == null ? Confr.OP_GE : opLonMin;
      if ( !confronta(lonMin, coo.getLongitude(), op))
        return false;
    }
    if (lonMax != null) {
      Confr op = opLonMax == null ? Confr.OP_LE : opLonMax;
      if ( !confronta(lonMax, coo.getLongitude(), op))
        return false;
    }
    if (latMin != null) {
      Confr op = opLatMin == null ? Confr.OP_GE : opLatMin;
      if ( !confronta(latMin, coo.getLatitude(), op))
        return false;
    }
    if (latMax != null) {
      Confr op = opLatMax == null ? Confr.OP_LE : opLatMax;
      if ( !confronta(latMax, coo.getLatitude(), op))
        return false;
    }
    if (filePresent != null) {
      bRet = ! (filePresent ^ coo.getFotoFile() != null);
    }
    return bRet;
  }

  private boolean confronta(Double p_ref, double p_val, Confr p_op) {
    boolean bRet = false;
    switch (p_op) {
      case OP_LT:
        bRet = p_val < p_ref;
        break;
      case OP_LE:
        bRet = p_val <= p_ref;
        break;
      case OP_EQ:
        bRet = p_ref == p_val;
        break;
      case OP_GE:
        bRet = p_val >= p_ref;
        break;
      case OP_GT:
        bRet = p_val > p_ref;
        break;
    }
    return bRet;
  }

  private boolean confronta(LocalDateTime p_dtRef, LocalDateTime p_dt, Confr p_op) {
    boolean bRet = false;
    switch (p_op) {
      case OP_LT:
        bRet = p_dt.isBefore(p_dtRef);
        break;
      case OP_LE:
        bRet = p_dt.equals(p_dtRef);
        if ( !bRet)
          bRet = p_dt.isBefore(p_dtRef);
        break;
      case OP_EQ:
        bRet = p_dt.equals(p_dtRef);
        break;
      case OP_GE:
        bRet = p_dt.equals(p_dtRef);
        if ( !bRet)
          bRet = p_dt.isAfter(p_dtRef);
        break;
      case OP_GT:
        bRet = p_dt.isAfter(p_dtRef);
        break;
      default:
        break;
    }
    return bRet;
  }

  public boolean checkValues() {
    err = null;
    if (dtMin != null && dtMax != null) {
      if (dtMin.isAfter(dtMax))
        err = "Le date non sono congrue";
    }
    if (lonMin != null && lonMax != null) {
      if (lonMin > lonMax) {
        err = "Le longitudini non sono congrue";
      }
    }
    if (latMin != null && latMax != null) {
      if (latMin > latMax) {
        err = "Le latitudini non sono congrue";
      }
    }
    return err == null;
  }

}

package prova.dbgps;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.utils.ParseData;

public class JacksonParse {
  private static final String CSZ_JSON_TAKEOUT = "F:\\java\\photon2\\imaging\\dati\\trackGoogle\\Records-2023_BIS.json";

  public static final String FLD_source            = "source";
  public static final String FLD_osLevel           = "osLevel";
  public static final String FLD_accuracy          = "accuracy";
  public static final String FLD_altitude          = "altitude";
  public static final String FLD_velocity          = "velocity";
  public static final String FLD_deviceTag         = "deviceTag";
  public static final String FLD_timestamp         = "timestamp";
  public static final String FLD_formFactor        = "formFactor";
  public static final String FLD_latitudeE7        = "latitudeE7";
  public static final String FLD_longitudeE7       = "longitudeE7";
  public static final String FLD_platformType      = "platformType";
  public static final String FLD_batteryCharging   = "batteryCharging";
  public static final String FLD_deviceTimestamp   = "deviceTimestamp";
  public static final String FLD_serverTimestamp   = "serverTimestamp";
  public static final String FLD_deviceDesignation = "deviceDesignation";
  public static final String FLD_verticalAccuracy  = "verticalAccuracy";

  // private static ParseData s_tmParse = new ParseData();

  private GeoList liGeo;

  public JacksonParse() {
    //
  }

  @Test
  public void doDtheJob() {
    parseGeo(CSZ_JSON_TAKEOUT);
    liGeo.sortByTStamp();
    List<GeoCoord> liNew = filtraList();
    System.out.printf("Orig size=%d\n", liGeo.size());
    System.out.printf("New  size=%d\tDiff=%d\n", liNew.size(), liGeo.size() - liNew.size());
  }

  private List<GeoCoord> filtraList() {
    GeoCoord prec = null;
    List<GeoCoord> liNew = new LinkedList<>();

    for (GeoCoord geo : liGeo) {
      if (prec == null) {
        prec = geo;
        liNew.add(geo);
        continue;
      }
      double dist = prec.distance(geo);
      long secs = ChronoUnit.SECONDS.between(prec.getTstamp(), geo.getTstamp());
      if (dist > 2 || secs > 10) {
        liNew.add(geo);
        String sz = String.format("%.8f, %.8f diff=%d, dist=%.2fm", //
            geo.getLatitude(), //
            geo.getLongitude(), //
            secs, dist);
        System.out.println(sz);
      }
      prec = geo;
    }
    return liNew;
  }

  public void parseGeo(String p_jsonfile) {
    final int goodLiv = 2;
    @SuppressWarnings("unused")
    int arrLiv = 0;
    int nestLiv = 0;
    @SuppressWarnings("unused")
    int riga = 0;
    String fldNam = null;
    Object fldVal = null;
    liGeo = new GeoList();
    JsonFactory jsfact = new JsonFactory();
    GeoCoord geo = null;
    try (JsonParser jsp = jsfact.createParser(new FileInputStream(p_jsonfile))) {
      do {
        JsonToken tok = jsp.nextToken();
        System.out.println(tok.toString());
        riga = jsp.getCurrentLocation().getLineNr();
        switch (tok) {
          case START_ARRAY:
            arrLiv++;
            // System.out.printf("%d)-- liv=%s:%s --(%s)--\n", riga, nestLiv, arrLiv, fldNam);
            break;
          case END_ARRAY:
            // System.out.printf("%d)^^^ liv=%s:%s ^^(%s)^^\n", riga, nestLiv, arrLiv, fldNam);
            arrLiv--;
            break;
          case START_OBJECT:
            nestLiv++;
            if (nestLiv == goodLiv) {
              geo = new GeoCoord();
              geo.setSrcGeo(EGeoSrcCoord.google);
              // System.out.printf("%d)--- liv=%s:%s --(%s)--\n", riga, nestLiv, arrLiv, fldNam);
            }
            break;
          case END_OBJECT:
            if (nestLiv == goodLiv) {
              liGeo.add(geo);
              geo = null;
              // System.out.printf("%d)^^^ liv=%s:%s ^^(%s)^^\n", riga, nestLiv, arrLiv, fldNam);
            }
            nestLiv--;
            break;

          case FIELD_NAME:
            fldNam = jsp.getText();
            fldVal = null;
            if (nestLiv == goodLiv) {
              JsonToken tok2 = jsp.nextToken();
              switch (tok2) {
                case VALUE_STRING:
                  fldVal = jsp.getText();
                  break;
                case VALUE_NUMBER_FLOAT:
                  fldVal = jsp.getDoubleValue();
                  break;
                case VALUE_NUMBER_INT:
                  fldVal = jsp.getIntValue();
                  break;
                case VALUE_FALSE:
                  fldVal = Boolean.FALSE;
                  break;
                case VALUE_TRUE:
                  fldVal = Boolean.TRUE;
                  break;
                default:
                  break;
              }
              if (fldVal != null) {
                // System.out.printf("%d) %16s = %s\n", riga, fldNam, fldVal);
                geo = assignGeo(geo, fldNam, fldVal);
              }
            }
            break;
          default:
            break;
        }
      } while (nestLiv > 0);
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private GeoCoord assignGeo(GeoCoord p_geo, String p_fldNam, Object p_fldVal) {
    if (p_fldVal == null)
      return p_geo;
    double dbl;
    LocalDateTime dt;
    switch (p_fldNam) {
      case FLD_latitudeE7:
        dbl = Double.parseDouble(p_fldVal.toString()) / 10_000_000F;
        p_geo.setLatitude(dbl);
        break;
      case FLD_longitudeE7:
        dbl = Double.parseDouble(p_fldVal.toString()) / 10_000_000F;
        p_geo.setLongitude(dbl);
        break;
      case FLD_altitude:
        dbl = Double.parseDouble(p_fldVal.toString());
        p_geo.setAltitude(dbl);
        break;
      case FLD_timestamp:
        // privilegio il "deviceTimestamp"
        dt = ParseData.parseData(p_fldVal.toString());
        if (p_geo.getTstamp() == null)
          p_geo.setTstamp(dt);
        break;
      case FLD_deviceTimestamp:
        dt = ParseData.parseData(p_fldVal.toString());
        p_geo.setTstamp(dt);
        break;
      default:
        break;
    }
    return p_geo;
  }

  public void parse(String p_jsonfile) {
    JsonFactory jsfact = new JsonFactory();
    try (JsonParser jsp = jsfact.createParser(new FileInputStream(p_jsonfile))) {
      while (jsp.nextToken() != JsonToken.END_OBJECT) {
        JsonToken tok = jsp.getCurrentToken();

        switch (tok) {
          case START_ARRAY:
            System.out.println("Starting array");
            break;
          case END_ARRAY:
            System.out.println("Ending array");
            break;
          case START_OBJECT:
            System.out.println("Starting object");
            break;
          case END_OBJECT:
            System.out.println("Ending object");
            break;
          case FIELD_NAME:
            System.out.println("Key: " + jsp.getText());
            break;
          case VALUE_STRING:
            System.out.println("String value: " + jsp.getText());
            break;
          case VALUE_NUMBER_INT:
            System.out.println("Number value: " + jsp.getIntValue());
            break;
          case VALUE_NUMBER_FLOAT:
            System.out.println("Float value: " + jsp.getDoubleValue());
            break;
          case VALUE_TRUE:
            System.out.println("Boolean value: true");
            break;
          case VALUE_FALSE:
            System.out.println("Boolean value: false");
            break;
          case VALUE_NULL:
            System.out.println("Null value");
            break;
          default:
            System.out.println("Unknown value:" + tok);
            break;
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}

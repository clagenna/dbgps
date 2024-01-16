package sm.clagenna.dbgps.cmdline;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoJsonParser;
import sm.clagenna.stdcla.sql.DBConnSQLite;

public class MainProvaNoStd {
  private static final Logger s_log            = LogManager.getLogger(MainProvaNoStd.class);
  private static final String CSZ_JSON_TAKEOUT = "F:\\java\\photon2\\imaging\\dati\\trackGoogle\\Records-2023_BIS.json";
  private static final String SQL_TBL_Gps      =                                                                        //
      "CREATE TABLE IF NOT EXISTS gpspos (\n"                                                                           //
          + " timestamp integer NOT NULL,\n"                                                                            //
          + " longitude real NOT NULL,\n"                                                                               //
          + " latitude  real NOT NULL,\n"                                                                               //
          + " altitude  real NOT NULL,\n"                                                                               //
          + " source text\n" + ");";

  private static final String     SQL_Ins_GpsPos = "INSERT INTO gpspos ("   //
      + " timestamp, longitude, latitude, altitude, source) "               //
      + " VALUES ( ?,?,?,?,? )";
  private static final ZoneOffset zId            = ZoneOffset.UTC;

  private DBConnSQLite      m_db;
  private CallableStatement m_stmt;

  public static void main(String[] args) {
    MainProvaNoStd app = new MainProvaNoStd();
    app.doTheJob("GpsInfo.sqlite3");
  }

  private void doTheJob(String p_dbName) {
    m_db = new DBConnSQLite(p_dbName);
    Connection conn = m_db.doConn();
    if (conn == null)
      return;
    try {
      Statement stmt = conn.createStatement();
      stmt.execute(SQL_TBL_Gps);
    } catch (SQLException e) {
      s_log.error("Errore crea Table, err={}", e.getMessage(), e);
    }
    parseGoogleTakeout();
    try {
      m_db.close();
    } catch (IOException e) {
      s_log.error("Errore close DB, err={}", e.getMessage(), e);
    }
  }

  private void parseGoogleTakeout() {
    GeoJsonParser par = new GeoJsonParser();
    List<GeoCoord> li = par.parse(CSZ_JSON_TAKEOUT);
    Connection conn = m_db.getConn();
    try {
      m_stmt = conn.prepareCall(SQL_Ins_GpsPos);
    } catch (SQLException e) {
      s_log.error("Errore prepare Stmt, err={}", e.getMessage(), e);
      return;
    }
    GeoCoord prec = null;
    try {
      for (GeoCoord geo : li) {
        if (prec != null) {
          long secs = Math.abs(ChronoUnit.SECONDS.between(geo.getTstamp(), prec.getTstamp()));
          if ((secs < 2) || (prec.distance(geo) < 0.1))
            continue;
        }
        addGeoToDb(geo);
        prec = geo;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private void addGeoToDb(GeoCoord p_geo) throws SQLException {
    int k = 1;
    long unixTime = p_geo.getTstamp().toEpochSecond(zId);
    m_stmt.setLong(k++, unixTime);
    m_stmt.setDouble(k++, p_geo.getLongitude());
    m_stmt.setDouble(k++, p_geo.getLatitude());
    m_stmt.setDouble(k++, p_geo.getAltitude());
    m_stmt.setString(k++, p_geo.getSrcGeo().toString());

    m_stmt.execute();
  }

  @SuppressWarnings("unused")
  private void doTheJob2(String p_string) {
    GestDbSqlite db = new GestDbSqlite();
    Connection conn = db.createOrOpenDatabase(p_string);
    db.creaTabella(SQL_TBL_Gps);
  }

}

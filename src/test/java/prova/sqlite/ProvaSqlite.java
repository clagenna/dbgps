package prova.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;

import org.junit.Test;

import sm.clagenna.stdcla.enums.EServerId;
import sm.clagenna.stdcla.sql.DBConn;
import sm.clagenna.stdcla.sql.DBConnFactory;

public class ProvaSqlite {

  public ProvaSqlite() {
    // TODO Auto-generated constructor stub
  }

  @Test
  public void provalo() {
    DBConnFactory fact = new DBConnFactory();
    DBConn dbconn = fact.get(EServerId.SQLite3.toString());
    dbconn.setDbname("F:\\java\\photon2\\dbgps\\data\\2023_JULY.sqlite3");
    // String szUrl = dbconn.getURL();
    Connection conn = dbconn.doConn();
    String szSQL = "SELECT " //
        + "  DATETIME(timestamp, 'unixepoch') AS timestamp" //
        + ", longitude" //
        + ", latitude" //
        + ", source" //
        + "  FROM gpspos" //
        + " ORDER BY timestamp";
    DateFormat fmt = DateFormat.getDateTimeInstance();
    try (PreparedStatement stmt = conn.prepareStatement(szSQL)) {
      ResultSet res = stmt.executeQuery();
      int k=1;
      @SuppressWarnings("unused")
      String sz = res.getString(k);
      Date dt = res.getTimestamp(k++);
      double lon = res.getDouble(k++);
      double lat = res.getDouble(k++);
      System.out.printf("%s,%.8f,%.8f\n", fmt.format(dt),lon, lat );
    } catch (SQLException e) {
      //
      e.printStackTrace();
    }
  }
}

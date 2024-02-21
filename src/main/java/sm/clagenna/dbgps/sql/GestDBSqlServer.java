package sm.clagenna.dbgps.sql;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.ZoneId;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import sm.clagenna.stdcla.enums.EServerId;
import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.sql.DBConnFactory;
import sm.clagenna.stdcla.sql.DBConnSQL;
import sm.clagenna.stdcla.sys.TimerMeter;

@Data
public class GestDBSqlServer implements Closeable {
  private static final Logger s_log = LogManager.getLogger(GestDBSqlServer.class);

  private static final String SQL_Sel_Gps = //
      " SELECT     gp.timestamp" //
          + "     ,gp.latitude" //
          + "     ,gp.longitude" //
          + "     ,gp.altitude" //
          + "     ,gp.source" //
          + "     ,gf.filename" //
          + "   FROM dbo.GPSInfo AS gp" //
          + "     LEFT OUTER JOIN dbo.GpsFile AS gf" //
          + "     ON gf.id = gp.idFile"; //

  /**
   * <pre>
   *  &#64;timestamp  datetime ,
      &#64;longitude  float ,
      &#64;latitude   float ,
      &#64;altitude   int ,
      &#64;source     varchar(64) ,
      @filenam        varchar(128)
   * </pre>
   */
  private static final String QRY_ADDGPSINFO =       //
      "{? = call dbo.addGpsInfo(?, ?, ?, ?, ?, ? )}";
  private PreparedStatement   m_stmt_CallSql;

  private boolean    overWrite;
  private String     dbName;
  private String     dbHost;
  private Integer    service;
  private String     user;
  private String     passwd;
  private Connection conn;

  public GestDBSqlServer() {
    //
  }

  public void setDbName(Path p_destDB) {
    dbName = p_destDB.toString();
  }

  public void OpenDatabase() {
    DBConnFactory fa = new DBConnFactory();
    DBConnSQL dbConn = (DBConnSQL) fa.get(EServerId.SqlServer.toString());
    dbConn.setDbname(dbName);
    dbConn.setHost(dbHost);
    dbConn.setService(service);
    dbConn.setUser(user);
    dbConn.setPasswd(passwd);
    conn = dbConn.doConn();
  }

  public void saveDB(GeoList p_geoList) {
    if (conn == null) {
      s_log.error("DB not opened!");
      return;
    }
    if (p_geoList == null || p_geoList.size() == 0) {
      s_log.error("Nothing to save!");
      return;
    }
    int rowNo = 0;
    int qta = 0;
    TimerMeter tim = new TimerMeter("Insert GeoCoord");
    tim.start();
    try (CallableStatement stmt = conn.prepareCall(QRY_ADDGPSINFO);) {
      for (GeoCoord coo : p_geoList) {
        rowNo++;
        int k = 1;
        stmt.registerOutParameter(k++, java.sql.Types.INTEGER);
        stmt.setTimestamp(k++, Timestamp.valueOf(coo.getTstamp()));
        stmt.setDouble(k++, coo.getLongitude());
        stmt.setDouble(k++, coo.getLatitude());
        stmt.setInt(k++, 0);
        stmt.setString(k++, coo.getSrcGeo().toString());
        if (coo.getFotoFile() == null)
          stmt.setNull(k++, Types.VARCHAR);
        else
          stmt.setString(k++, coo.getFotoFile().toString());
        stmt.execute();
        int i = stmt.getInt(1);
        qta += i;
      }
      s_log.debug("GestDBSqlServer saveDB() = {}", tim.stop());
      s_log.info("Added {} records to DB", qta);
    } catch (SQLException e) {
      s_log.error("Errore Ins SQL, row={}, err={}", rowNo, e.getMessage());
    }
  }

  @Override
  public void close() throws IOException {

  }

  public GeoList readAll() {
    GeoList li = new GeoList();
    if (conn == null) {
      s_log.error("DB not opened!");
      return li;
    }

    s_log.debug("Leggo records Geo dal DB {}", dbName);
    try (Statement stmt = conn.createStatement()) {
      try (ResultSet rs = stmt.executeQuery(SQL_Sel_Gps)) {
        while (rs.next()) {
          int k = 1;
          Date dt = rs.getTimestamp(k++);
          double lat = rs.getDouble(k++);
          double lon = rs.getDouble(k++);
          int alt = rs.getInt(k++);
          String src = rs.getString(k++);
          String foto = rs.getString(k++);
          Path pth = foto == null || foto.length() < 2 ? null : Paths.get(foto);

          GeoCoord geo = new GeoCoord();
          geo.setTstamp(dt.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime());
          geo.setLatitude(lat);
          geo.setLongitude(lon);
          geo.setAltitude(alt);
          geo.setSrcGeo(EGeoSrcCoord.valueOf(src));
          geo.setFotoFile(pth);
          li.add(geo);
        }
      }
      s_log.info("Letti {} rec da DB {}", li.size(), dbName);
    } catch (SQLException e) {
      s_log.error("Lettura DB SQL Server, err = {}", e.getMessage(), e);
    }
    return li;
  }

}

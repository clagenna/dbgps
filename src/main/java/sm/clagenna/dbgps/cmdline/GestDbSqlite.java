package sm.clagenna.dbgps.cmdline;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoList;

@Data
public class GestDbSqlite implements Closeable {
  private static final Logger s_log = LogManager.getLogger(GestDbSqlite.class);

  private static final String SQL_TBL_Gps = //
      "CREATE TABLE IF NOT EXISTS gpspos (\n" //
          + " timestamp integer NOT NULL,\n" //
          + " longitude real NOT NULL,\n" //
          + " latitude  real NOT NULL,\n" //
          + " altitude  real NOT NULL,\n" //
          + " source text\n" + ");";

  private static final String SQL_Ins_GpsPos = "INSERT INTO gpspos (" //
      + " timestamp, longitude, latitude, altitude, source) " //
      + " VALUES ( ?,?,?,?,? )";

  private Connection conn = null;
  private boolean    overWrite;
  private Path       dbFileName;

  public GestDbSqlite() {
    //
  }

  public Connection createOrOpenDatabase(String fileName) {
    setDbFileName(Paths.get(fileName));
    return createOrOpenDatabase();
  }

  public Connection createOrOpenDatabase() {
    String url = "jdbc:sqlite:" + dbFileName;
    try {
      s_log.info("Opening SQLite3 DB: {}", dbFileName.toString());
      conn = DriverManager.getConnection(url);
      if (conn != null && s_log.isDebugEnabled()) {
        DatabaseMetaData meta = conn.getMetaData();
        s_log.debug("Utilizzo il driver: {}", meta.getDriverName());
      }
      s_log.info("Opened DB: {}", dbFileName.toString());
      creaTabella(SQL_TBL_Gps);
    } catch (SQLException e) {
      s_log.error("Errore conn DataBase {}, err={}", dbFileName, e.getMessage(), e);
    }
    return conn;
  }

  public void creaTabella(String p_query) {
    if (conn == null)
      throw new UnsupportedOperationException("Non sei connesso !");
    try {
      Statement stmt = conn.createStatement();
      stmt.execute(p_query);
    } catch (SQLException e) {
      s_log.error("Errore crea Table, err={}", e.getMessage(), e);
    }
  }

  public int addAll(GeoList p_li) {
    if (conn == null) {
      s_log.error("DB not opened!");
      return -1;
    }
    int qta = 0;
    s_log.debug("Adding {} records to DB", p_li.size());
    try (PreparedStatement stmt = conn.prepareStatement(SQL_Ins_GpsPos)) {
      for (GeoCoord geo : p_li) {
        int col = 1;
        stmt.clearParameters();
        long epoch = geo.getTstamp().toEpochSecond(ZoneOffset.UTC);
        stmt.setLong(col++, epoch);
        stmt.setDouble(col++, geo.getLongitude());
        stmt.setDouble(col++, geo.getLatitude());
        stmt.setDouble(col++, geo.getAltitude());
        stmt.setString(col++, geo.getSrcGeo().toString());
        // stmt.executeUpdate();
        stmt.addBatch();
        qta++;
      }
      conn.setAutoCommit(false);
      stmt.executeBatch();
      conn.setAutoCommit(true);
      s_log.debug("Added {} records to DB", qta);
    } catch (SQLException e) {
      s_log.error("Errore Ins SQL, row={}, err={}", qta, e.getMessage(), e);
    }
    return qta;
  }

  public void backupDB() throws IOException {
    if ( !Files.exists(dbFileName, LinkOption.NOFOLLOW_LINKS))
      return;
    String szPath = dbFileName.toAbsolutePath().toString();
    s_log.warn("creo il backup di {}", szPath);
    int n = szPath.lastIndexOf(".");
    String sz1 = szPath.substring(0, n);
    String szExt = szPath.substring(n + 1);
    LocalDateTime now = LocalDateTime.now();
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    String szNow = fmt.format(now);
    String szBak = String.format("%s_%s_%s.bak", sz1, szExt, szNow);
    try {
      s_log.warn("Creo il BAK file {}", szBak);
      CopyOption[] options = new CopyOption[] { //
          StandardCopyOption.REPLACE_EXISTING //
          /* , StandardCopyOption.COPY_ATTRIBUTES */ };
      Files.move(dbFileName, Paths.get(szBak), options);
    } catch (IOException e) {
      s_log.error("Errore backup su {}", szBak, e);
      throw e;
    }
  }

  @Override
  public void close() {
    try {
      if (conn != null) {
        conn.close();
      }
      conn = null;
    } catch (SQLException e) {
      s_log.error("Errore close di {}", getDbFileName().toString(), e);
    }
  }

  public int getCount(String p_string) {
    int qta = -1;
    String qry = String.format("select count(*) as qta from %s", p_string);
    try (Statement stmt = conn.createStatement()) {
      ResultSet rs = stmt.executeQuery(qry);
      if (rs.next())
        qta = rs.getInt("qta");
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return qta;
  }

}

package sm.clagenna.dbgps.sql;

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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoCoordFoto;
import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.sql.SqlTypes;
import sm.clagenna.stdcla.sys.TimerMeter;

@Data
public class GestDbSqlite implements Closeable {
  private static final Logger s_log = LogManager.getLogger(GestDbSqlite.class);

  private static final String SQL_Test_Table = //
      "SELECT COUNT(*) as qta FROM sqlite_master WHERE type='table' AND name='%s';";

  private static final String SQL_TBL_Fotofile = //
      "CREATE TABLE IF NOT EXISTS fotofiles ( " //
          + "    id       INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT," //
          + "    fotofile TEXT" //
          + ");";

  private static final String SQL_TBL_Gps            =                        //
      "CREATE TABLE IF NOT EXISTS gpspos ("                                   //
          + " id        integer NOT NULL PRIMARY KEY AUTOINCREMENT,"          //
          + " timestamp integer NOT NULL,"                                    //
          + " longitude real NOT NULL,"                                       //
          + " latitude  real NOT NULL,"                                       //
          + " altitude  real NOT NULL,"                                       //
          + " source    text,"                                                //
          + " idfile    INTEGER REFERENCES fotofiles (id) "                   //
          + ");";
  private static final String SQL_Indx_Timestamp_Gps =                        //
      "CREATE INDEX IF NOT EXISTS IX_Gpspos_timestamp ON gpspos (timestamp);";
  private static final String SQL_Indx_longitude_Gps =                        //
      "CREATE INDEX IF NOT EXISTS IX_Gpspos_longitude ON gpspos (longitude);";

  private static final String SQL_Ins_GpsPos = "INSERT INTO gpspos (" //
      + " timestamp, longitude, latitude, altitude, source, idfile) " //
      + " VALUES ( ?,?,?,?,?,? )";

  private static final String  SQL_Sel_Gps          =              //
      "SELECT DATETIME(gp.timestamp, 'unixepoch') AS timestamp,"   //
          + "           gp.latitude,"                              //
          + "           gp.longitude,"                             //
          + "           gp.altitude,"                              //
          + "           gp.source,"                                //
          + "           ft.fotofile"                               //
          + "      FROM gpspos AS gp"                              //
          + "           LEFT OUTER JOIN"                           //
          + "           fotofiles AS ft ON gp.idfile = ft.id";
  private static final String  SQL_Sel_Id_Fotofiles =              //
      "SELECT seq FROM sqlite_sequence WHERE name='fotofiles'";
  private static final String  SQL_Ins_Fotofiles    =              //
      "INSERT INTO fotofiles (id, fotofile) VALUES (?, ?);";
  private static final String  SQL_Sel_Fotofiles    =              //
      "SELECT id,fotofile FROM fotofiles;";
  private PreparedStatement    stmtInsFoto;
  private Connection           conn                 = null;
  private boolean              overWrite;
  private Path                 dbFileName;
  int                          nLastIdFile;
  private Map<String, Integer> mapFiles;

  public GestDbSqlite() {
    nLastIdFile = 0;
  }

  public boolean existTable(String tbName) {
    boolean bRet = false;
    int qta = 0;
    String qry = String.format(SQL_Test_Table, tbName);
    try (Statement stmt = conn.createStatement()) {
      try (ResultSet rs = stmt.executeQuery(qry)) {
        if (rs.next())
          qta = rs.getInt("qta");
        bRet = qta > 0;
      }
    } catch (SQLException e) {
      s_log.error("Errore check table {}, err={}", tbName, e.getMessage(), e);
    }
    return bRet;
  }

  public Connection createOrOpenDatabase(String fileName) {
    setDbFileName(Paths.get(fileName));
    return createOrOpenDatabase();
  }

  public Connection createOrOpenDatabase() {
    String url = "jdbc:sqlite:" + dbFileName;
    try {
      Class.forName("org.sqlite.JDBC");
      s_log.info("Opening SQLite3 DB: {}", dbFileName.toString());
      conn = DriverManager.getConnection(url);
      if (conn != null && s_log.isDebugEnabled()) {
        DatabaseMetaData meta = conn.getMetaData();
        s_log.debug("Utilizzo il driver: {}", meta.getDriverName());
      }
      s_log.info("Opened DB: {}", dbFileName.toString());
      executeQuery(SQL_TBL_Fotofile);
      executeQuery(SQL_TBL_Gps);
      executeQuery(SQL_Indx_Timestamp_Gps);
      executeQuery(SQL_Indx_longitude_Gps);
    } catch (Exception e) {
      s_log.error("Errore conn DataBase {}, err={}", dbFileName, e.getMessage(), e);
    }
    return conn;
  }

  public void executeQuery(String p_query) {
    if (conn == null)
      throw new UnsupportedOperationException("Non sei connesso !");
    try {
      Statement stmt = conn.createStatement();
      stmt.execute(p_query);
    } catch (SQLException e) {
      s_log.error("Errore crea Table, err={}", e.getMessage(), e);
    }
  }

  public int saveDB(GeoList p_li) {
    if (conn == null) {
      s_log.error("DB not opened!");
      return -1;
    }
    leggiLastIdFile();
    int qta = 0;
    TimerMeter tim = new TimerMeter("Insert GeoCoord");
    tim.start();
    int nIdFile = -1;
    s_log.debug("Adding {} records to DB", p_li.size());
    try (PreparedStatement stmtFoto = conn.prepareStatement(SQL_Ins_Fotofiles)) {
      try (PreparedStatement stmt = conn.prepareStatement(SQL_Ins_GpsPos)) {
        for (GeoCoord geo : p_li) {
          //          conn.setAutoCommit(false);
          if (geo.getFotoFile() != null)
            nIdFile = insertFotoFile(stmtFoto, geo.getFotoFile());

          int col = 1;
          stmt.clearParameters();
          long epoch = geo.getTstamp().toEpochSecond(GeoCoordFoto.s_zoneOffSet);
          stmt.setLong(col++, epoch);
          stmt.setDouble(col++, geo.getLongitude()); 
          stmt.setDouble(col++, geo.getLatitude());
          stmt.setDouble(col++, geo.getAltitude());
          stmt.setString(col++, geo.getSrcGeo().toString());

          if (geo.getFotoFile() == null)
            stmt.setNull(col++, SqlTypes.INTEGER.code());
          else
            stmt.setInt(col++, nIdFile);

          //          stmt.executeUpdate();
          stmt.addBatch();
          //          conn.commit();
          qta++;
          // System.out.printf("GestDbSqlite.saveDB(%d)\n", qta);
          if (qta % 127 == 0) {
            System.out.println(tim.stop() + " n=" + qta);
            tim.start();
            conn.setAutoCommit(false);
            stmtFoto.executeBatch();
            stmt.executeBatch();
            conn.setAutoCommit(true);
          }
        }
        conn.setAutoCommit(false);
        stmtFoto.executeBatch();
        stmt.executeBatch();
        conn.setAutoCommit(true);
        s_log.info("Added {} records to DB", qta);
      }
    } catch (SQLException e) {
      try {
        conn.rollback();
      } catch (SQLException e1) {
        // e1.printStackTrace();
      }
      s_log.error("Errore Ins SQL, row={}, err={}", qta, e.getMessage(), e);
    }
    return qta;
  }

  private void leggiLastIdFile() {
    nLastIdFile = -1;
    mapFiles = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    try (Statement stmt = conn.createStatement()) {
      ResultSet rs = stmt.executeQuery(SQL_Sel_Fotofiles);
      while (rs.next()) {
        int col = 1;
        Integer nId = Integer.valueOf(rs.getInt(col++));
        String szFotof = rs.getString(col++);
        mapFiles.put(szFotof, nId);
        nLastIdFile = nLastIdFile < nId ? nId : nLastIdFile;
      }
    } catch (SQLException e) {
      s_log.error("Error sel last id insert in FotoFiles, err={}", e.getMessage());
    }

  }

  private int insertFotoFile(PreparedStatement p_stmtFoto, Path pth) throws SQLException {
    Integer nId = -1;
    if (pth == null)
      return nId;
    nId = mapFiles.get(pth.toString());
    if (nId != null)
      return nId;

    nId = ++nLastIdFile;
    mapFiles.put(pth.toString(), nId);
    p_stmtFoto.clearParameters();
    int nCol = 1;
    p_stmtFoto.setInt(nCol++, nId);
    p_stmtFoto.setString(nCol++, pth.toString());
    // p_stmtFoto.executeUpdate();
    p_stmtFoto.addBatch();
    return nId;
  }

  public GeoList readAll() {
    GeoList li = new GeoList();
    if (conn == null) {
      s_log.error("DB not opened!");
      return li;
    }

    s_log.debug("Leggo records Geo dal DB {}", dbFileName.toString());
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
      s_log.info("Letti {} rec da DB {}", li.size(), dbFileName.toString());
    } catch (SQLException e) {
      s_log.error("Lettura DB Sqlite, err = {}", e.getMessage(), e);
    }
    return li;
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

  public int getLastIdFoto() {
    nLastIdFile = 0;
    try (Statement stmt = conn.createStatement()) {
      ResultSet rs = stmt.executeQuery(SQL_Sel_Id_Fotofiles);
      if (rs.next())
        nLastIdFile = rs.getInt("seq");
    } catch (SQLException e) {
      s_log.error("Error sel last id insert in FotoFiles, err={}", e.getMessage());
    }
    // System.out.printf("GestDbSqlite.getLastIdFoto(%d)\n", nLastId);
    return nLastIdFile;
  }

}

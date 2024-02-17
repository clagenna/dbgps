package sm.clagenna.dbgps.sql;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.PreparedStatement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Data;
import sm.clagenna.stdcla.enums.EServerId;
import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.sql.DBConnFactory;
import sm.clagenna.stdcla.sql.DBConnSQL;

@Data
public class GestDBSqlServer implements Closeable {
  private static final Logger s_log = LogManager.getLogger(GestDBSqlServer.class);

  private static final String QRY_ADDGPSINFO =           //
      "{call dbo.addGpsInfo(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
  private PreparedStatement   m_stmt_CallSql;

  private boolean overWrite;
  private String dbName;

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
    dbConn.doConn();

  }

  public void saveDB(GeoList p_geoList) {
    // 

  }

  @Override
  public void close() throws IOException {

  }

}

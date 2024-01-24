package prova.sqlite;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import sm.clagenna.dbgps.cmdline.GestDbSqlite;
import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.geo.GeoScanJpg;
import sm.clagenna.stdcla.sys.ex.GeoFileException;

public class ScanPhotosGps {
  private static final String CSZ_START_DIR = "F:\\My Foto\\2023\\2023-07-06 Normandia Bretagna";

  private GeoList mylist;

  public ScanPhotosGps() {
    //
  }

  @Test
  public void doTheJob() throws GeoFileException {
    Path pthStart = Paths.get(CSZ_START_DIR);
    mylist = new GeoList();
    GeoScanJpg scj = new GeoScanJpg(mylist);
    scj.scanDir(pthStart);
    // Butta in un DataBase
    String szDbFile = "data/2023_JULY.sqlite3";

    try (GestDbSqlite db = new GestDbSqlite()) {
      db.setOverWrite(false);
      db.setDbFileName(Paths.get(szDbFile));
      db.createOrOpenDatabase();
      int qta1 = db.getCount("gpspos");
      db.saveDB(mylist);
      int qta2 = db.getCount("gpspos");
      System.out.printf("Count(*) prima=%d, dopo=%d\n", qta1, qta2);
    }
  }

}

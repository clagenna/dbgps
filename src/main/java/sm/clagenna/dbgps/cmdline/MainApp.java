package sm.clagenna.dbgps.cmdline;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.geo.fromgoog.GeoConvGpx;
import sm.clagenna.stdcla.geo.fromgoog.JacksonParseRecurse;

public class MainApp {
  private static final Logger s_log = LogManager.getLogger(MainApp.class);
  private GeoList             m_listGeo;

  public MainApp() {
    //
  }

  public static void main(String[] args) {
    RigaComando cmd = new RigaComando();
    if ( !cmd.parseOption(args))
      return;
    MainApp app = new MainApp();
    try {
      app.doTheJob(cmd);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void doTheJob(RigaComando p_cmd) throws IOException {
    //    GeoJacksonParser geoParse = new GeoJacksonParser();
    //    GeoList li = geoParse.parseJson(p_cmd.getSourceFile().toString());
    JacksonParseRecurse geoParse = new JacksonParseRecurse();
    String szFiGoog = p_cmd.getSourceFile().toString();
    s_log.info("Starting parse of {}", szFiGoog);
    m_listGeo = geoParse.parseGeo(szFiGoog);
    s_log.info("Sort by timeStamp of {}", szFiGoog);
    m_listGeo.sortByTStamp();
    s_log.info("filter nearest of {}", szFiGoog);
    GeoList liNn = m_listGeo.filterNearest();
    m_listGeo.clear();
    m_listGeo = null;
    m_listGeo = liNn;
    if (p_cmd.isGpxNeeded())
      saveToGPX(p_cmd);
    if ( !p_cmd.isDBNeeded())
      return;
    GestDbSqlite db = new GestDbSqlite();
    db.setOverWrite(p_cmd.isOverwrite());
    db.setDbFileName(p_cmd.getDestDB());
    db.backupDB();
    db.createOrOpenDatabase();
    int qtaIns = db.addAll(m_listGeo);
    if (qtaIns < 1)
      System.err.println("Errore in insert");
  }

  private void saveToGPX(RigaComando p_cmd) {
    GeoConvGpx togpx = new GeoConvGpx();
    String destGpx = p_cmd.getDestGPX().toString();

    togpx.setDestGpxFile(Paths.get(destGpx));
    togpx.setListGeo(m_listGeo);
    togpx.setOverwrite(p_cmd.isOverwrite());
    togpx.saveToGpx();
    s_log.info("Saved GPX to {}", togpx.getDestGpxFile().toString());
  }
}

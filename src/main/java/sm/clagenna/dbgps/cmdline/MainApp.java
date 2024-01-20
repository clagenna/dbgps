package sm.clagenna.dbgps.cmdline;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoFormatter;
import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.geo.fromgoog.GeoConvGpx;
import sm.clagenna.stdcla.geo.fromgoog.JacksonParseRecurse;

public class MainApp {
  private static final Logger       s_log    = LogManager.getLogger(MainApp.class);
  private static final GeoFormatter s_geofmt = new GeoFormatter();
  private GeoList                   m_listGeo;

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
    GeoFormatter.setShowLink(true);
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
    trovaAlcuneFoto();
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
    db.close();
  }

  private void trovaAlcuneFoto() {
    final String[] arr = { //
          "2023-07-07 19:31:21"
        , "2023-07-07 19:46:40" //
        , "2023-07-13 12:34:54" //
        , "2023-07-15 15:47:09" //
    };
    for (String sz : arr) {
      LocalDateTime dt = s_geofmt.parseTStamp(sz);
      GeoCoord geo = m_listGeo.findNearest(dt);
      System.out.printf("%s --> %s\n", GeoFormatter.s_fmtmY4MD_hms.format(dt), geo.toString());
    }
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

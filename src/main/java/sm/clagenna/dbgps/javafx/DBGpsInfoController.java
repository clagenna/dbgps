package sm.clagenna.dbgps.javafx;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import sm.clagenna.dbgps.sys.FotoViewerProducer;
import sm.clagenna.dbgps.sys.ThreadExec;
import sm.clagenna.dbgps.sys.Versione;
import sm.clagenna.stdcla.geo.EExifPriority;
import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoFormatter;
import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.sql.EServerId;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ILog4jReader;
import sm.clagenna.stdcla.utils.Log4jRow;
import sm.clagenna.stdcla.utils.MioAppender;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class DBGpsInfoController implements Initializable, ILog4jReader {
  private static final Logger s_log             = LogManager.getLogger(DBGpsInfoController.class);
  private static final String lNK_MAPS          = "https://www.google.com/maps?z=15&t=h&q=%.8f,%.8f";
  public static final String  CSZ_FXMLNAME      = "RegGpsInfo.fxml";
  private static final String CSZ_PROP_COL      = "tbview.col.%s";
  private static final String COL01_DATETIME    = "tstamp";
  private static final String COL02_LATITUDE    = "latitude";
  private static final String COL03_LONGITUDE   = "longitude";
  private static final String COL04_SOURCE      = "srcGeo";
  private static final String COL05_ALTITUDE    = "altitude";
  private static final String COL05_FOTOFILE    = "fotoFile";
  private static final String IMAGE_EDITING_ICO = "photogr.png";

  private static final String CSZ_LOG_LEVEL   = "logLevel";
  private static final String CSZ_INDTABPANE  = "indxtabpane";
  private static final String CSZ_SPLITPOSTAB = "splitpostab";
  private static final String CSZ_SPLITPOS    = "splitpos";

  @FXML
  private TabPane   tabPane;
  @FXML
  private SplitPane spltPaneTab;

  @FXML
  private TextField               txFileSorg;
  @FXML
  private Button                  btCercaFileSrc;
  @FXML
  private CheckBox                ckAddSimilFoto;
  @FXML
  private CheckBox                ckRecurseDir;
  @FXML
  private ComboBox<EGeoSrcCoord>  cbTipoFileSrc;
  @FXML
  private ComboBox<EExifPriority> cbPriorityInfo;
  @FXML
  private Button                  btApriFileSrc;
  @FXML
  private Button                  btRicaricaFileSrc;

  @FXML
  private CheckBox            ckShowGMS;
  @FXML
  private TextField           txDBFile;
  @FXML
  private TextField           txDBHost;
  @FXML
  private TextField           txDBService;
  @FXML
  private TextField           txDBUser;
  @FXML
  private PasswordField       txDBPswd;
  @FXML
  private Button              btCercaFileDB;
  @FXML
  private ComboBox<EServerId> cbTipoDb;
  @FXML
  private CheckBox            ckDatetimeUnique;

  @FXML
  private Button btApriDbFile;
  @FXML
  private Button btSalvaDb;

  @FXML
  private TextField txGPXFile;
  @FXML
  private Button    btCercaGPXFile;
  @FXML
  private Button    btSaveToGPX;
  @FXML
  private CheckBox  ckExpLanciaBaseC;

  @FXML
  private TextField              txFltrDtMin;
  @FXML
  private TextField              txFltrDtMax;
  @FXML
  private ComboBox<EGeoSrcCoord> cbFltrTipoSrc;
  @FXML
  private TextField              txFltrLatMin;
  @FXML
  private TextField              txFltrLatMax;
  @FXML
  private TextField              txFltrLonMin;
  @FXML
  private TextField              txFltrLonMax;
  @FXML
  private Label                  lbFltrLatMin;
  @FXML
  private Label                  lbFltrLatMax;
  @FXML
  private Label                  lbFltrLonMin;
  @FXML
  private Label                  lbFltrLonMax;
  @FXML
  private CheckBox               ckFilePresent;
  @FXML
  private Button                 btFltrFiltra;
  @FXML
  private Button                 btFltrClear;

  @FXML
  private SplitPane           spltPane;
  @FXML
  private TableView<GeoCoord> tblvRecDB;
  private int                 m_nColsInfoType;

  private MenuItem mnuCtxVaiCoord;
  private MenuItem mnuCtxDtMin;
  private MenuItem mnuCtxDtMax;
  private MenuItem mnuCtxLonMin;
  private MenuItem mnuCtxLonMax;
  private MenuItem mnuCtxLatMin;
  private MenuItem mnuCtxLatMax;
  private MenuItem mnuCtxDt5Minute;
  private MenuItem mnuCtxDt10Minute;
  private MenuItem mnuCtxCopyCoord;
  private MenuItem mnuCtxCopyPath;
  private MenuItem mnuCtxGessLoc;

  @FXML
  private TextField                     txUpdDatetime;
  @FXML
  private TextField                     txUpdFromWEB;
  @FXML
  private Button                        btUpdParseWEB;
  @FXML
  private Button                        btUpdParseWEBModSave;
  @FXML
  private Label                         lbUpdLongitude;
  @FXML
  private Label                         lbUpdLatitude;
  @FXML
  private TextField                     txUpdLongitude;
  @FXML
  private TextField                     txUpdLatitude;
  @FXML
  private ComboBox<EGeoSrcCoord>        cbUpdTipoSrc;
  @FXML
  private TextField                     txUpdFotoFile;
  @FXML
  private Button                        btUpdModif;
  @FXML
  private Button                        btUpdInsert;
  @FXML
  private Button                        btUpdDelete;
  @FXML
  private Button                        btUpdSaveFoto;
  @FXML
  private Button                        btUpdClear;
  @FXML
  private Button                        btUpdClear1;
  @FXML
  private Button                        btUpdClear2;
  @FXML
  private Button                        btUpdRenameAllFoto;
  @FXML
  private TableView<Log4jRow>           tblvLogs;
  @FXML
  private TableColumn<Log4jRow, String> colTime;
  @FXML
  private TableColumn<Log4jRow, String> colLev;
  @FXML
  private TableColumn<Log4jRow, String> colMsg;
  @FXML
  private Button                        btClearMsg;

  @FXML
  private ComboBox<Level> cbLevelMin;
  @FXML
  private Label           lblLogs;

  private FotoViewerProducer fotoViewProd;
  private Level              levelMin;
  private List<Log4jRow>     m_liMsgs;
  private DataModelGpsInfo   m_model;
  private GeoFormatter       m_updGeoFmt;
  /** il clone del GeoCoord da trattare */
  private GeoCoord           m_updGeo;
  /** GeoCoord della {@link GeoList} originale (quindi con stesso hash) */
  private GeoCoord           m_updGeoOrig;

  public DBGpsInfoController() {
    //
  }

  @Override
  public void addLog(String[] p_arr) {
    // [0] - class emitting
    // [1] - timestamp
    // [2] - Log Level
    // [3] - message
    // System.out.println("addLog=" + String.join("\t", p_arr));
    Log4jRow riga = null;
    try {
      riga = new Log4jRow(p_arr);
    } catch (Exception e) {
      e.printStackTrace();
    }
    if (riga != null)
      addRigaLog(riga);
  }

  private void addRigaLog(Log4jRow rig) {
    if (m_liMsgs == null)
      m_liMsgs = new ArrayList<>();
    m_liMsgs.add(rig);
    // System.out.printf("DBGpsInfoController.addRigaLog(min=%s)\n", levelMin.toString());
    Platform.runLater(() -> {
      if (rig.getLevel().intLevel() <= levelMin.intLevel())
        tblvLogs.getItems().add(rig);
    });
    // if ( rig.getLevel().isInRange( Level.FATAL, levelMin )) // isLessSpecificThan(levelMin))
    //    if (rig.getLevel().intLevel() <= levelMin.intLevel()) {
    //??      ObservableList<Log4jRow> itms = tblvLogs.getItems();
    //      itms.add(rig);
    //      if (itms.size() > 4) {
    //
    //        Platform.runLater(new Runnable() {
    //          @Override
    //          public void run() {
    //            tblvLogs.scrollTo(itms.size() - 1);
    //          }
    //        });
    //      }
    //    }
  }

  private void updButtonsGest() {
    if (m_updGeo == null) {
      btUpdParseWEBModSave.setDisable(true);
      btUpdModif.setDisable(true);
      btUpdInsert.setDisable(true);
      btUpdDelete.setDisable(true);
      btUpdSaveFoto.setDisable(true);
      lbUpdLatitude.setStyle("");
      lbUpdLongitude.setStyle("");
      return;
    }
    boolean bv = m_updGeo.isChanged(m_updGeoOrig);
    boolean bg = m_updGeo.isGuessed();
    if (bg) {
      lbUpdLatitude.setStyle(MioTableCellRenderCoord.CSZ_GUESSED_CSS);
      lbUpdLongitude.setStyle(MioTableCellRenderCoord.CSZ_GUESSED_CSS);
    } else {
      lbUpdLatitude.setStyle("");
      lbUpdLongitude.setStyle("");
    }
    String sz = txUpdFromWEB.getText();
    btUpdParseWEBModSave.setDisable(null == sz || sz.length() < 5);
    btUpdModif.setDisable(bv);
    btUpdInsert.setDisable(bv);
    btUpdDelete.setDisable( !m_updGeo.isComplete());
    bv = m_updGeo.isComplete() && m_updGeo.hasFotoFile();
    btUpdSaveFoto.setDisable( !bv);
  }

  private void updClearUpd() {
    m_updGeo = null;
    updButtonsGest();
    // txUpdDatetime.setText(null);
    txUpdLatitude.setText(null);
    txUpdLongitude.setText(null);
    txUpdFromWEB.setText(null);
    cbUpdTipoSrc.getSelectionModel().clearSelection();
    txUpdFotoFile.setText(null);
  }

  private void updAddModificaDati(GeoCoord p_geo, boolean bNewData) {
    if (p_geo != null) {
      LocalDateTime dtTs = p_geo.getTstampNew();
      if (null == dtTs)
        dtTs = p_geo.getTstamp();
      if (null != dtTs)
        txUpdDatetime.setText(GeoFormatter.s_fmtmY4MD_hms.format(dtTs));
      else
        s_log.error("Il file {} non ha DateTime", p_geo.getFotoFile());
      double dbl = p_geo.getLongitude();
      if (dbl != 0)
        txUpdLongitude.setText(MioTableCellRenderCoord.s_fmt.format(dbl));
      else
        txUpdLongitude.setText(null);
      dbl = p_geo.getLatitude();
      if (dbl != 0)
        txUpdLatitude.setText(MioTableCellRenderCoord.s_fmt.format(dbl));
      else
        txUpdLatitude.setText(null);
      cbUpdTipoSrc.getSelectionModel().select(p_geo.getSrcGeo());
      Path fo = p_geo.getFotoFile();
      btUpdSaveFoto.setDisable(fo == null || !p_geo.hasLonLat());
      if (fo != null)
        txUpdFotoFile.setText(fo.toString());
      else
        txUpdFotoFile.setText(null);
      txUpdFotoFile.setEditable(false);
    } else {
      txUpdDatetime.setText(null);
      txUpdLongitude.setText(null);
      txUpdLatitude.setText(null);
      cbUpdTipoSrc.getSelectionModel().select(null);
    }
    if (bNewData) {
      try {
        m_updGeo = (GeoCoord) p_geo.clone();
        m_updGeoOrig = p_geo;
        // testUpdOrig(p_pnew);
      } catch (CloneNotSupportedException e) {
        //
      }
    }
  }

  private void testUpdOrig(GeoCoord p_o) {
    if (p_o == null)
      return;
    System.out.printf("geo=%d,%d\t%s\n", m_updGeo.hashCode() % 1021, System.identityHashCode(m_updGeo) % 1021,
        m_updGeo.toStringSimple());
    System.out.printf("rif=%d,%d\t%s\n", p_o.hashCode() % 1021, System.identityHashCode(p_o) % 1021, p_o.toStringSimple());
    StringBuilder sb = new StringBuilder();
    for (GeoCoord g : m_model.getGeoList()) {
      sb //
          .append(String.format("\t%5d,%d)", g.hashCode() % 1021, System.identityHashCode(g) % 1021)) //
          .append(g.toStringSimple()) //
          .append("\n");
    }
    System.out.println(sb.toString());
    GeoList li = m_model.getGeoList();
    List<GeoCoord> pro = li.stream().filter(s -> s.equals(p_o)).toList();
    if (pro != null && pro.size() > 0) {
      boolean bMatch = false;
      for (GeoCoord g : pro) {
        bMatch = g.hashCode() == p_o.hashCode();
        if ( !bMatch)
          System.out.printf("%d != %d\n", g.hashCode(), p_o.hashCode());
      }
    } else
      System.out.printf("%d Not Present !!\n", p_o.hashCode());
  }

  @FXML
  public void btApriDBClick(ActionEvent event) {
    Button[] enaDis = { btApriDbFile, btSalvaDb, btUpdClear2 };
    lanciaMainAppBackGroundWork(DataModelGpsInfo.ThreadWork.LeggiDB, enaDis);
  }
  //
  //  @FXML
  //  public void btApriDBClickThread(ActionEvent event) {
  //    Button[] enaDis = { btApriDbFile, btSalvaDb, btUpdClear2 };
  //    lanciaMainAppBackGroundWork(DataModelGpsInfo.ThreadWork.LeggiDB, enaDis);
  //  }

  @FXML
  public void btSalvaDBClick(ActionEvent event) {
    if (m_model == null)
      return;
    switch (m_model.getTipoDB()) {
      case SQLite:
      case SQLite3:
        if (Files.exists(m_model.getDbName())) {
          if ( !DataModelGpsInfo.confirmationDialog(AlertType.WARNING,
              "Sicuro di sovrascrivere il file : " + m_model.getDbName().toString())) {
            s_log.warn("Salva DB SQLite Annullata !");
            return;
          }
        }
        break;
      default:
        break;
    }
    //    m_model.salvaDB();
    Button[] enaDis = { btApriDbFile, btSalvaDb, btUpdClear2 };
    lanciaMainAppBackGroundWork(DataModelGpsInfo.ThreadWork.salvaDB, enaDis);
  }

  @FXML
  public void btApriSourceClick(ActionEvent event) {
    m_model.parseSource();
    Platform.runLater(() -> caricaLaGrigliaGeo());
  }

  /**
   * rileggo (in modalità Background work) tutte le foto dal disco
   *
   * @param event
   */
  @FXML
  public void btApriSourceClickThread(ActionEvent event) {
    Button[] enaDis = { btApriFileSrc, btRicaricaFileSrc, btUpdClear1 };
    lanciaMainAppBackGroundWork(DataModelGpsInfo.ThreadWork.ParseSource, enaDis);
  }

  /**
   * Rigenero/rinfresco il Data Model rileggendo tutte le foto
   *
   * @param event
   */
  @FXML
  public void btRicaricaSrcClickThread(ActionEvent event) {
    m_model.initData();
    updClearUpd();
    btApriSourceClickThread((ActionEvent) null);
  }

  private void lanciaMainAppBackGroundWork(DataModelGpsInfo.ThreadWork tpw, Button[] enaDis) {
    s_log.debug("Lancio {} in background con un thread", tpw.toString());
    ExecutorService backGrService = MainAppGpsInfo.getInst().getBackGrService();
    Stage stage = MainAppGpsInfo.getInst().getPrimaryStage();
    setCursorOnStage(stage, Cursor.WAIT);
    try {
      m_model.setTipoThread(tpw);
      ThreadExec thex = new ThreadExec(m_model);
      s_log.debug("esegui {} ... new Task!", tpw.toString());
      // =========== Start Event ================
      thex.setOnRunning(ev -> {
        for (Button bt : enaDis)
          bt.setDisable(true);
        s_log.debug("... setOnRunning()");
      });
      // =========== Finish Event ================
      thex.setOnSucceeded(ev -> {
        s_log.debug("...setOnSucceeded()");
        for (Button bt : enaDis)
          bt.setDisable(false);
        setCursorOnStage(stage, Cursor.DEFAULT);
        for (Button bt : enaDis)
          bt.setDisable(false);
        caricaLaGrigliaGeo();
      });
      s_log.debug("exec service {} start", tpw.toString());
      backGrService.execute(thex);
      s_log.debug("exec service {} exit", tpw.toString());
      // ========================================
    } catch (Exception e) {
      lblLogs.textProperty().unbind();
      s_log.error("Errore esecuzione {}", tpw.toString(), e);
    }

  }

  private void setCursorOnStage(Stage stage, Cursor cur) {
    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        // System.out.println("Cursor " + cur);
        stage.getScene().setCursor(cur);
      }
    });
  }

  @FXML
  public void btCercaDBClick(ActionEvent event) {
    Stage stage = MainAppGpsInfo.getInst().getPrimaryStage();
    FileChooser fil = new FileChooser();
    fil.setTitle("Cerca il DataBase dei dati");
    Path pth = m_model.getDbName();
    Path pthDir = null;
    if (pth != null && Files.exists(pth)) {
      if (Files.isDirectory(pth))
        pthDir = pth;
      else
        pthDir = pth.getParent();
    }
    if (pthDir != null)
      fil.setInitialDirectory(pthDir.toFile());

    File fileScelto = fil.showOpenDialog(stage);
    if (fileScelto != null) {
      pth = fileScelto.toPath();
      if (Files.isRegularFile(pth)) {
        m_model.setDestDB(fileScelto.toPath());
        txDBFile.setText(fileScelto.getAbsolutePath());
        cbTipoDb.getSelectionModel().select(m_model.getTipoDB());
        cbTipoDBSrcSel(null);
        s_log.info("Hai scelto src dir {}", fileScelto.getAbsolutePath());
      } else {
        msgBox("Per un Data Base Devi scegliere un File", AlertType.WARNING);
      }
    } else {
      s_log.debug("Non hai scelto nulla !!");
    }
  }

  @FXML
  public void btCercaGPXClick(ActionEvent event) {
    Stage stage = MainAppGpsInfo.getInst().getPrimaryStage();
    FileChooser fil = new FileChooser();
    fil.setTitle("Cerca/indica il file GPX di destinazione dei dati");
    Path pth = m_model.getDestGPXfile();
    Path pthDir = null;
    if (pth != null && Files.exists(pth)) {
      if (Files.isDirectory(pth))
        pthDir = pth;
      else
        pthDir = pth.getParent();
    }
    if (pthDir != null)
      fil.setInitialDirectory(pthDir.toFile());

    File fileScelto = fil.showSaveDialog(stage);
    if (fileScelto != null) {
      pth = fileScelto.toPath();
      if ( !pth.toString().toLowerCase().endsWith(".gpx")) {
        msgBox("Per salvare i GPX devi scegliere un File con suffisso .GPX", AlertType.WARNING);
        return;
      }
      m_model.setDestGPXfile(pth);
      txGPXFile.setText(fileScelto.getAbsolutePath());
      s_log.info("Hai scelto il file GPX {}", fileScelto.getAbsolutePath());
      m_model.setInvalidGPX(false);
    } else {
      s_log.debug("Non hai scelto nulla !!");
    }
  }

  @FXML
  public void btCercaSourceClick(ActionEvent event) {
    Stage stage = MainAppGpsInfo.getInst().getPrimaryStage();
    EGeoSrcCoord tp = m_model.getTipoSource();
    Path pth = m_model.getSrcDir();
    File fileScelto = null;
    if (tp == EGeoSrcCoord.foto) {
      DirectoryChooser dir = new DirectoryChooser();
      dir.setTitle("Cerca Direttorio di Foto");
      Path pthDir = pth;
      if (pthDir != null) {
        if (Files.isRegularFile(pthDir))
          pthDir = pthDir.getParent();
        if (Files.exists(pthDir))
          dir.setInitialDirectory(pthDir.toFile());
      }
      fileScelto = dir.showDialog(stage);
    } else {
      FileChooser fil = new FileChooser();
      fil.setTitle("Cerca il File di Track");
      if (pth != null) {
        if (Files.exists(pth))
          fil.setInitialDirectory(pth.getParent().toFile());
      }
      fileScelto = fil.showOpenDialog(stage);
    }
    // imposto la dir precedente (se c'e')
    // AppProperties props = AppProperties.getInstance();
    if (fileScelto != null) {
      m_model.setSrcDir(fileScelto.toPath());
      txFileSorg.setText(fileScelto.getAbsolutePath());
      cbFltrTipoSrc.getSelectionModel().select(m_model.getTipoSource());
      s_log.info("Hai scelto src dir {}", fileScelto.getAbsolutePath());
    } else {
      s_log.debug("Non hai scelto nulla !!");
    }
  }

  @FXML
  public void cbFltrTipoSrcClick() {
    btFltrFiltraClick((ActionEvent) null);
  }

  @FXML
  void btClearMsgClick(ActionEvent event) {
    // System.out.println("ReadFattHTMLController.btClearMsgClick()");
    tblvLogs.getItems().clear();
    if (m_liMsgs != null)
      m_liMsgs.clear();
    m_liMsgs = null;
  }

  @FXML
  public void btFltrClearClick(ActionEvent event) {
    m_model.getFiltro().clear();
    txFltrDtMin.setText(null);
    txFltrDtMax.setText(null);
    cbFltrTipoSrc.getSelectionModel().clearSelection();
    txFltrLatMin.setText(null);
    txFltrLatMax.setText(null);
    txFltrLonMin.setText(null);
    txFltrLonMax.setText(null);
    ckFilePresent.setIndeterminate(true);

    Platform.runLater(() -> caricaLaGrigliaGeo());
  }

  @FXML
  public void btFltrFiltraClick(ActionEvent event) {
    FiltroGeoCoord filtro = m_model.getFiltro();
    if ( !filtro.checkValues()) {
      String szErr = filtro.getErr();
      msgBox(szErr, AlertType.WARNING);
      return;
    }
    filtro.setActive(true);
    //    System.out.printf("Presente Indeterm=%s\n\tsel=%s\n", //
    //        ckFilePresent.isIndeterminate(), //
    //        ckFilePresent.isSelected());
    if (ckFilePresent.isIndeterminate())
      filtro.setFilePresent(null);

    Platform.runLater(() -> caricaLaGrigliaGeo());
  }

  @FXML
  public void btSaveToGPXClick(ActionEvent event) {
    // m_model.saveToGPX();
    Button[] enaDis = { btSaveToGPX, btUpdRenameAllFoto };
    lanciaMainAppBackGroundWork(DataModelGpsInfo.ThreadWork.SaveToGPX, enaDis);
  }

  @FXML
  public void btUpdParseWEBClick(ActionEvent event) {
    // System.out.printf("btUpdParseWEB(\"%s\")\n", txUpdFromWEB.getText());
    if (m_updGeo == null)
      m_updGeo = new GeoCoord();
    if (m_updGeoFmt == null)
      m_updGeoFmt = new GeoFormatter();
    String szDt = txUpdDatetime.getText();
    if (szDt != null && szDt.length() > 2) {
      m_updGeoFmt.parseTStamp(m_updGeo, szDt);
      m_updGeoFmt.setWebTime(m_updGeo.getTstamp());
    }
    String szWeb = txUpdFromWEB.getText();
    m_updGeo = m_updGeoFmt.parseWeb(m_updGeo, szWeb);
    updAddModificaDati(m_updGeo, false);
    updButtonsGest();
  }

  @FXML
  public void btUpdParseWEBModSaveClick(ActionEvent event) {
    if (m_updGeo == null)
      return;

    Stage stage = MainAppGpsInfo.getInst().getPrimaryStage();
    s_log.info("Richiesta : Parse WEB + Modifica Riga + Save Foto su {}", m_updGeo.getFotoFile().toString());
    setCursorOnStage(stage, Cursor.WAIT);

    Platform.runLater(new Runnable() {
      @Override
      public void run() {
        btUpdParseWEBClick(event);
        if (btUpdModifClick(event))
          btUpdSaveFotoClick(event);
      }
    });
    setCursorOnStage(stage, Cursor.DEFAULT);
  }

  @FXML
  public void btUpdInsertClick(ActionEvent event) {
    if (null == m_updGeo)
      return;
    GeoList li = m_model.getGeoList();
    if (null == li)
      li = m_model.initData();
    if (li.contains(m_updGeo)) {
      s_log.warn("Coordinata {} gia' presente", m_updGeo.toString());
      String sz = MioAppender.getInst().lastMsg();
      if (sz != null)
        msgBox(sz, AlertType.WARNING);
    } else {
      li.add(m_updGeo);
      updClearUpd();
    }
    Platform.runLater(() -> caricaLaGrigliaGeo());
  }

  /**
   * In modalita' <b>modifica</b> della riga<br/>
   * Se ho una modifica in corso ( {@link #m_updGeo} oppure
   * {@link #m_updGeoOrig} ) verifico che:
   * <ol>
   * <li>nel <b>data model</b> sia presente la lista dati (vedi
   * {@link DataModelGpsInfo#getGeoList()})</li>
   * <li>Se presente, allora aggiorno sia a) nel model, sia b) la TableView</li>
   * </ol>
   * sia che sia presente il record nella lista del data model
   *
   * @param event
   * @return
   */
  @FXML
  public boolean btUpdModifClick(ActionEvent event) {
    boolean bRet = false;
    if (null == m_updGeo || null == m_updGeoOrig)
      return bRet;
    GeoList li = m_model.getGeoList();
    if (null == li)
      li = m_model.initData();
    if (li.size() == 0)
      return bRet;
    //    testUpdOrig(m_updGeoOrig);
    bRet = true;
    m_model.assignGeoInList(li, m_updGeo);
    //    GeoCoord it = null;
    //    int indx = li.indexOf(m_updGeoOrig);
    //    if (indx >= 0) {
    //      it = li.get(indx);
    //      it.assign(m_updGeo);
    //    } else
    //      System.out.println("btUpdModifClick() Geo not Found !");
    // ??????????????
    //        ObservableList<GeoCoord> li2 = tblvRecDB.getItems();
    //        m_model.assignGeoInList(li2, m_updGeo);
    // ???????????????
    //    it = null;
    //    indx = li2.indexOf(m_updGeoOrig);
    //    if (indx >= 0) {
    //      it = li2.get(indx);
    //      it.assign(m_updGeo);
    //    } else
    //      System.out.println("btUpdModifClick() Geo not Found in tableView !");
    //
    //    testUpdOrig(m_updGeoOrig);
    Platform.runLater(() -> caricaLaGrigliaGeo());
    updButtonsGest();
    return bRet;
  }

  @FXML
  public void btUpdCopiaCoord(ActionEvent event) {
    if (null != m_updGeo && m_updGeo.hasLonLat()) {
      String sz = String.format(Locale.US, "%.10f,%.10f", m_updGeo.getLatitude(), m_updGeo.getLongitude());
      final Clipboard clipboard = Clipboard.getSystemClipboard();
      final ClipboardContent content = new ClipboardContent();
      content.putString(sz);
      // content.putHtml("<b>Some</b> text");
      clipboard.setContent(content);
      s_log.info("Copiato nella ClipBoard: {}", sz);
    }
  }

  @FXML
  public void btUpdCopiaPathFoto(ActionEvent event) {
    if (null != m_updGeo && m_updGeo.hasFotoFile()) {
      String sz = m_updGeo.getFotoFile().toString();
      final Clipboard clipboard = Clipboard.getSystemClipboard();
      final ClipboardContent content = new ClipboardContent();
      content.putString(sz);
      // content.putHtml("<b>Some</b> text");
      clipboard.setContent(content);
      s_log.info("Copiato nella ClipBoard: {}", sz);
    }
  }

  @FXML
  public void btUpdDeleteClick(ActionEvent event) {
    // FIXME Testare bene la btUpdDeleteClick
    testUpdOrig(m_updGeoOrig);
    GeoList li = m_model.getGeoList();
    int indx = li.indexOf(m_updGeoOrig);
    if (indx >= 0) {
      @SuppressWarnings("unused") GeoCoord it = li.get(indx);
      System.out.println("boh!");
    }
    Platform.runLater(() -> caricaLaGrigliaGeo());
  }

  @FXML
  public void btUpdClearClick(ActionEvent event) {
    if (msgBox("Sicuro di cancellare i dati della Griglia ?", AlertType.CONFIRMATION)) {
      m_model.initData();
      Platform.runLater(() -> caricaLaGrigliaGeo());
      updClearUpd();
    }
  }

  /**
   * Salvo fisicamente le info della Tab di modifica sul file foto
   *
   * @param event
   */
  @FXML
  private void btUpdSaveFotoClick(ActionEvent event) {
    if (null == m_updGeo || //
        null == m_updGeo.getFotoFile()
    /* || !m_updGeo.isGuessed() */
    /* || !m_updGeo.hasLonLat() */)
      return;
    boolean bRicar = m_model.saveFotoFile(m_updGeo);
    if (bRicar) {
      // rigenero il Data Model dopo le modifiche
      btRicaricaSrcClickThread(null);
    } else {
      // rinfresco solo la griglia
      Platform.runLater(() -> caricaLaGrigliaGeo());
    }
    updButtonsGest();
  }

  @FXML
  private void btUpdRenameAllFotoClick(ActionEvent event) {
    mnuFRinominaFotoClick(null);
  }

  /**
   * Rigenero la griglia della TableView con i dati presenti nel model ( vedi
   * {@link DataModelGpsInfo#getGeoList()} ).<br/>
   * Inoltre aggiorno anche la distanza dal punto precendente ( vedi
   * {@link GeoCoord#altitudeAsDistance(GeoCoord)} )<br/>
   * Aggiorno anche il Tab dei filtri con i valori Min/Max
   *
   */
  private void caricaLaGrigliaGeo() {
    GeoList liData = m_model.getGeoList();
    ObservableList<GeoCoord> itms = tblvRecDB.getItems();

    // --- 1) cancello la griglia precedente
    itms.clear();

    if (liData == null) {
      lblLogs.setText("Letti 0 recs");
      return;
    }

    // 2) carico la griglia un GeoCoord alla volta
    GeoCoord prec = null;
    for (GeoCoord geo : liData) {
      if (prec != null)
        geo.altitudeAsDistance(prec);
      itms.add(geo);
      // lo faccio in un botto solo dopo
      //      if (null != m_updGeo && geo.equalSolo(m_updGeo))
      //        tblvRecDB.getSelectionModel().select(geo);
      if (geo.hasLonLat())
        prec = geo;
    }

    // 3) nel tab filtri aggiorno le info Min/Max
    GeoCoord minGeo = m_model.getMingeo();
    GeoCoord maxGeo = m_model.getMaxgeo();
    lbFltrLonMin.setText(String.format(Locale.US, "%.10f", minGeo.getLongitude()));
    lbFltrLonMax.setText(String.format(Locale.US, "%.10f", maxGeo.getLongitude()));
    lbFltrLatMin.setText(String.format(Locale.US, "%.10f", minGeo.getLatitude()));
    lbFltrLatMax.setText(String.format(Locale.US, "%.10f", maxGeo.getLatitude()));

    if (null != m_updGeo) {
      GeoCoord itm = tblvRecDB.getSelectionModel().getSelectedItem();
      if (null == itm) {
        ObservableList<GeoCoord> lli = tblvRecDB.getItems();
        List<GeoCoord> litrov = lli.stream().filter(s -> s.equalSolo(m_updGeoOrig)).toList();
        if (null != litrov && litrov.size() > 0)
          itm = litrov.get(0);
      }
      if (null != itm)
        itm.assign(m_updGeo);
      // tblvRecDB.getSelectionModel().select(m_updGeo);
    }
    // tod: Togliere dopo i test update della form
    //    else
    //      System.out.println("Riga updGeo *vuota*");
    //    GeoCoord xx = tblvRecDB.getSelectionModel().getSelectedItem();
    //    if (null == xx)
    //      System.out.println("*nessuna* riga selezionata !");
    //    else
    //      System.out.println("sel=" + xx.toStringSimple());
    // ---------------------------------------------

    lblLogs.setText(String.format("Letti %d recs", liData.size()));
  }

  @FXML
  private void cbLevelMinSel(ActionEvent event) {
    levelMin = cbLevelMin.getSelectionModel().getSelectedItem();
    // System.out.println("ReadFattHTMLController.cbLevelMinSel():" + levelMin.name());
    if (m_liMsgs == null || m_liMsgs.size() == 0)
      return;
    Platform.runLater(() -> {
      // List<Log4jRow> li = m_liMsgs.stream().filter(s -> s.getLevel().isInRange(Level.FATAL, levelMin )).toList(); // !s.getLevel().isLessSpecificThan(levelMin)).toList();
      tblvLogs.getItems().clear();
      List<Log4jRow> li = m_liMsgs //
          .stream() //
          .filter(s -> s.getLevel().intLevel() <= levelMin.intLevel()) //
          .toList();
      tblvLogs.getItems().addAll(li);
    });
  }

  @FXML
  private void cbTipoDBSrcSel(ActionEvent event) {
    EServerId tp = cbTipoDb.getSelectionModel().getSelectedItem();
    if (tp == m_model.getTipoDB())
      return;
    m_model.setTipoDB(tp);

    switch (tp) {
      case SqlServer:
        btCercaFileDB.setVisible(false);
        txDBHost.setDisable(false);
        txDBService.setDisable(false);
        txDBUser.setDisable(false);
        txDBPswd.setDisable(false);
        break;
      default:
        btCercaFileDB.setVisible(true);
        txDBHost.setDisable(true);
        txDBService.setDisable(true);
        txDBUser.setDisable(true);
        txDBPswd.setDisable(true);
        break;
    }
    txDBHost.setText(m_model.getDbHost());
    txDBFile.setText(m_model.getDbName().toString());
    txDBService.setText(String.valueOf(m_model.getDbService()));
    txDBUser.setText(m_model.getDbUser());
    txDBPswd.setText(m_model.getDbPaswd());
  }

  @FXML
  private void cbTipoFileSrcSel(ActionEvent event) {
    EGeoSrcCoord tp = cbTipoFileSrc.getSelectionModel().getSelectedItem();
    m_model.setTipoSource(tp);
    Path pth = null;
    String sz = txFileSorg.getText();
    if (sz != null)
      pth = Paths.get(sz);
    if (Utils.isChanged(m_model.getSrcDir(), pth)) {
      txFileSorg.setText(m_model.getSrcDir().toString());
    }
  }

  @FXML
  private void cbPriorityInfoSel(ActionEvent event) {
    EExifPriority ep = cbPriorityInfo.getSelectionModel().getSelectedItem();
    m_model.setPriorityInfo(ep);
  }

  private Object ckDatetimeUniqueClick(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    m_model.setDateTimeUnique(ckDatetimeUnique.isSelected());
    // System.out.printf("ckDatetimeUnique Click(%s)\n", m_model.isDateTimeUnique());
    tblvRecDB.refresh();
    return null;
  }

  @FXML
  private void mnuEShowGms(ActionEvent event) {
    Object obj = event.getSource();
    if (obj instanceof CheckMenuItem mnu) {
      boolean bv = mnu.isSelected();
      ckShowGMS.setSelected(bv);
      m_model.setShowGMS(bv);
      tblvRecDB.refresh();
    }
  }

  @FXML
  private void mnuEExtendsColsInfo(ActionEvent event) {
    Object obj = event.getSource();
    if (obj instanceof CheckMenuItem mnu) {
      boolean bv = mnu.isSelected();
      if (bv)
        creaColsTabViewDue();
      else
        creaColsTabViewUno();
      tblvRecDB.refresh();
    }
  }

  private void creaContextMenu() {
    ContextMenu cntxMenu = new ContextMenu();

    mnuCtxVaiCoord = new MenuItem("Vai Coord. (Ctrl-Click)");
    mnuCtxVaiCoord.setOnAction((ActionEvent ev) -> {
      mnuVaiCoord(ev);
    });
    cntxMenu.getItems().add(mnuCtxVaiCoord);
    // ---------------------------------------------
    cntxMenu.getItems().add(new SeparatorMenuItem());
    // ---------------------------------------------
    Menu mnuFiltri = new Menu("Imposta Filtro");
    cntxMenu.getItems().add(mnuFiltri);

    mnuCtxDtMin = new MenuItem("Filtro Dt.Min.");
    mnuCtxDtMin.setOnAction((ActionEvent ev) -> {
      mnuSetFltrDtMin(ev);
    });
    // cntxMenu.getItems().add(mnuCtxDtMin);
    mnuFiltri.getItems().add(mnuCtxDtMin);

    mnuCtxDtMax = new MenuItem("Filtro Dt.Max.");
    mnuCtxDtMax.setOnAction((ActionEvent ev) -> {
      mnuSetFltrDtMax(ev);
    });
    // cntxMenu.getItems().add(mnuCtxDtMax);
    mnuFiltri.getItems().add(mnuCtxDtMax);

    mnuCtxLonMin = new MenuItem("Filtro Lon. Min");
    mnuCtxLonMin.setOnAction((ActionEvent ev) -> {
      mnuSetFltrLonMin(ev);
    });
    // cntxMenu.getItems().add(mnuCtxLonMin);
    mnuFiltri.getItems().add(mnuCtxLonMin);

    mnuCtxLonMax = new MenuItem("Filtro Lon. Max");
    mnuCtxLonMax.setOnAction((ActionEvent ev) -> {
      mnuSetFltrLonMax(ev);
    });
    // cntxMenu.getItems().add(mnuCtxLonMax);
    mnuFiltri.getItems().add(mnuCtxLonMax);

    mnuCtxLatMin = new MenuItem("Filtro Lat. Min");
    mnuCtxLatMin.setOnAction((ActionEvent ev) -> {
      mnuSetFltrLatMin(ev);
    });
    // cntxMenu.getItems().add(mnuCtxLatMin);
    mnuFiltri.getItems().add(mnuCtxLatMin);

    mnuCtxLatMax = new MenuItem("Filtro Lat. Max");
    mnuCtxLatMax.setOnAction((ActionEvent ev) -> {
      mnuSetFltrLatMax(ev);
    });
    // cntxMenu.getItems().add(mnuCtxLatMax);
    mnuFiltri.getItems().add(mnuCtxLatMax);

    mnuFiltri.getItems().add(new SeparatorMenuItem());

    mnuCtxDt5Minute = new MenuItem("Filtro +- 5 Minutes");
    mnuCtxDt5Minute.setOnAction((ActionEvent ev) -> {
      mnuSetCtxDtMinute(ev, 5);
    });
    // cntxMenu.getItems().add(mnuCtxDt5Minute);
    mnuFiltri.getItems().add(mnuCtxDt5Minute);

    mnuCtxDt10Minute = new MenuItem("Filtro +- 10 Minutes");
    mnuCtxDt10Minute.setOnAction((ActionEvent ev) -> {
      mnuSetCtxDtMinute(ev, 10);
    });
    // cntxMenu.getItems().add(mnuCtxDt10Minute);
    mnuFiltri.getItems().add(mnuCtxDt10Minute);

    cntxMenu.getItems().add(new SeparatorMenuItem());

    mnuCtxCopyCoord = new MenuItem("Copia Coord.");
    mnuCtxCopyCoord.setOnAction((ActionEvent ev) -> {
      btUpdCopiaCoord(ev);
    });
    cntxMenu.getItems().add(mnuCtxCopyCoord);

    mnuCtxCopyPath = new MenuItem("Copia Path foto.");
    mnuCtxCopyPath.setOnAction((ActionEvent ev) -> {
      btUpdCopiaPathFoto(ev);
    });
    cntxMenu.getItems().add(mnuCtxCopyPath);

    mnuCtxGessLoc = new MenuItem("Indovina pos.");
    mnuCtxGessLoc.setOnAction((ActionEvent ev) -> {
      mnuGuessLocation(ev);
    });
    cntxMenu.getItems().add(mnuCtxGessLoc);

    tblvRecDB.setContextMenu(cntxMenu);
  }

  public void exitApplication(WindowEvent e) {
    if (m_model == null)
      return;
    AppProperties props = AppProperties.getInstance();
    m_model.saveProperties(props);
    saveColumnWidth(props);

    int iTab = tabPane.getSelectionModel().getSelectedIndex();
    props.setProperty(CSZ_INDTABPANE, iTab);

    double[] pos = spltPane.getDividerPositions();
    props.setDoubleProperty(CSZ_SPLITPOS, pos[0]);
    pos = spltPaneTab.getDividerPositions();
    props.setDoubleProperty(CSZ_SPLITPOSTAB, pos[0]);

    Level liv = cbLevelMin.getSelectionModel().getSelectedItem();
    props.setProperty(CSZ_LOG_LEVEL, liv.toString());
    Platform.exit();
  }

  private void impostaIco(Stage p_mainstage) {
    InputStream stre = getClass().getResourceAsStream(IMAGE_EDITING_ICO);
    if (stre == null)
      stre = getClass().getClassLoader().getResourceAsStream(IMAGE_EDITING_ICO);
    if (stre != null) {
      Image ico = new Image(stre);
      p_mainstage.getIcons().add(ico);
    }
  }

  @Override
  public void initialize(URL p_location, ResourceBundle p_resources) {
    AppProperties props = AppProperties.getInstance();
    Stage mainstage = MainAppGpsInfo.getInst().getPrimaryStage();
    s_log.debug("Start Application {}", getClass().getSimpleName());
    m_updGeoFmt = new GeoFormatter();
    fotoViewProd = new FotoViewerProducer();
    m_model = new DataModelGpsInfo();
    m_model.readProperties(props);
    cbFltrTipoSrc.getItems().add((EGeoSrcCoord) null);
    cbFltrTipoSrc.getItems().addAll(EGeoSrcCoord.values());
    cbFltrTipoSrc.valueProperty().addListener(new ChangeListener<EGeoSrcCoord>() {

      @Override
      public void changed(ObservableValue<? extends EGeoSrcCoord> p_observable, EGeoSrcCoord p_oldValue, EGeoSrcCoord p_newValue) {
        // System.out.printf("Combo Old=%s\tNew=%s\n", p_oldValue, p_newValue);
        FiltroGeoCoord filtro = m_model.getFiltro();
        filtro.setTipoSrc(p_newValue);
      }

    });

    cbTipoFileSrc.getItems().addAll(EGeoSrcCoord.values());
    cbPriorityInfo.getItems().addAll(EExifPriority.values());
    cbPriorityInfo.getSelectionModel().select(EExifPriority.ExifFileDir);
    m_model.setPriorityInfo(EExifPriority.ExifFileDir);

    cbUpdTipoSrc.getItems().add(null);
    cbUpdTipoSrc.getItems().addAll(EGeoSrcCoord.values());

    Path pth = m_model.getSrcDir();
    if (pth != null) {
      txFileSorg.setText(pth.toString());
      cbTipoFileSrc.getSelectionModel().select(m_model.getTipoSource());
    }
    ckAddSimilFoto.setSelected(false);
    ckAddSimilFoto.selectedProperty().addListener(new ChangeListener<Boolean>() {

      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        m_model.setAddSimilFoto(ckAddSimilFoto.isSelected());
        tblvRecDB.refresh();
      }
    });

    ckRecurseDir.setSelected(false);
    ckRecurseDir.selectedProperty().addListener(new ChangeListener<Boolean>() {

      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        m_model.setRecurseDirs(ckRecurseDir.isSelected());
        tblvRecDB.refresh();
      }
    });

    txFileSorg.focusedProperty().addListener((obs, oldv, newv) -> txFileSorgLostFocus(obs, oldv, newv));
    ckShowGMS.setSelected(false);
    ckShowGMS.selectedProperty().addListener(new ChangeListener<Boolean>() {

      @Override
      public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        m_model.setShowGMS(ckShowGMS.isSelected());
        tblvRecDB.refresh();
      }
    });

    cbTipoDb.getItems().addAll(EServerId.values());
    cbTipoDb.getSelectionModel().select(m_model.getTipoDB());
    cbTipoDBSrcSel(null);

    pth = m_model.getDbName();
    if (pth != null) {
      txDBFile.setText(pth.toString());
    }
    txDBFile.focusedProperty().addListener((obs, oldv, newv) -> txFileDBLostFocus(obs, oldv, newv));

    txDBHost.focusedProperty().addListener((obs, oldv, newv) -> m_model.setDbHost(txDBHost.getText()));
    txDBHost.setText(m_model.getDbHost());

    txDBService.textProperty().addListener(new ChangeListener<String>() {

      @Override
      public void changed(ObservableValue<? extends String> p_observable, String p_oldValue, String p_newValue) {
        if ( !p_newValue.matches("\\d*"))
          txDBService.setText(p_newValue.replaceAll("[^\\d]", ""));
      }
    });

    txDBService.focusedProperty().addListener((obs, oldv, newv) -> m_model.setDbStrService(txDBService.getText()));
    txDBService.setText(String.valueOf(m_model.getDbService()));

    txDBUser.focusedProperty().addListener((obs, oldv, newv) -> m_model.setDbUser(txDBUser.getText()));
    txDBUser.setText(m_model.getDbUser());

    txDBPswd.focusedProperty().addListener((obs, oldv, newv) -> m_model.setDbPaswd(txDBPswd.getText()));
    txDBPswd.setText(m_model.getDbPaswd());

    ckDatetimeUnique.setSelected(false);
    ckDatetimeUnique.selectedProperty().addListener((obs, oldv, newv) -> ckDatetimeUniqueClick(obs, oldv, newv));

    txGPXFile.focusedProperty().addListener((obs, oldv, newv) -> txGPXFileLostFocus(obs, oldv, newv));
    pth = m_model.getDestGPXfile();
    if (pth != null)
      txGPXFile.setText(pth.toString());
    ckExpLanciaBaseC.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent p_ev) {
        m_model.setExpLanciaBaseCamp(ckExpLanciaBaseC.isSelected());
      }
    });

    txFltrDtMin.focusedProperty().addListener((obs, oldv, newv) -> txFltrDtMinLostFocus(obs, oldv, newv));
    txFltrDtMax.focusedProperty().addListener((obs, oldv, newv) -> txFltrDtMaxLostFocus(obs, oldv, newv));

    txFltrLonMin.focusedProperty().addListener((obs, oldv, newv) -> txFltrLonMinLostFocus(obs, oldv, newv));
    txFltrLonMax.focusedProperty().addListener((obs, oldv, newv) -> txFltrLonMaxLostFocus(obs, oldv, newv));
    txFltrLatMin.focusedProperty().addListener((obs, oldv, newv) -> txFltrLatMinLostFocus(obs, oldv, newv));
    txFltrLatMax.focusedProperty().addListener((obs, oldv, newv) -> txFltrLatMaxLostFocus(obs, oldv, newv));
    ckFilePresent.setIndeterminate(true);
    // ***********    questa *NON SENTE* il passaggio da indeterm. a checked !!  *******************
    //    ckFilePresent.selectedProperty().addListener(new ChangeListener<Boolean>() {
    //
    //      @Override
    //      public void changed(ObservableValue<? extends Boolean> p_observable, Boolean p_oldValue, Boolean p_newValue) {
    //        FiltroGeoCoord filtro = m_model.getFiltro();
    //        filtro.setFilePresent(ckFilePresent.isSelected());
    //        System.out.printf("Presente Indeterm=%s\n\tsel=%s\n\tfiltr=%s\n", //
    //            ckFilePresent.isIndeterminate(), //
    //            ckFilePresent.isSelected(), //
    //            filtro.getFilePresent() //
    //        );
    //      }
    //    });

    ckFilePresent.setOnAction(new EventHandler<ActionEvent>() {

      @Override
      public void handle(ActionEvent p_ev) {
        FiltroGeoCoord filtro = m_model.getFiltro();
        if (ckFilePresent.isIndeterminate())
          filtro.setFilePresent(null);
        else
          filtro.setFilePresent(ckFilePresent.isSelected());
        //        System.out.printf("CkEvt = indet=%s\n\tsel=%s\n", //
        //            ckFilePresent.isIndeterminate(), //
        //            ckFilePresent.isSelected() //
        //        );
        p_ev.consume();
        btFltrFiltraClick((ActionEvent) null);
      }
    });

    int iTabs = props.getIntProperty(CSZ_INDTABPANE);
    if (iTabs >= 0)
      tabPane.getSelectionModel().select(iTabs);

    initializeTable(props);
    creaUpdControls();
    creaContextMenu();
    mainstage.setOnCloseRequest(e -> exitApplication(e));
    mainstage.setTitle(Versione.getVersionEx());
    leggiProperties(mainstage);
    preparaLogPanel(props);
    // preparaUpdPanel(props, mainstage);
    impostaIco(mainstage);
  }

  private void creaUpdControls() {
    txUpdDatetime.focusedProperty().addListener((obs, oldv, newv) -> txUpdDatetimeLostFocus(obs, oldv, newv));
    cbFltrTipoSrc.valueProperty().addListener((obs, oldv, newv) -> cbUpdTipoSrcClick(obs, oldv, newv));

    txUpdLatitude.focusedProperty().addListener((obs, oldv, newv) -> txUpdLatitudeLostFocus(obs, oldv, newv));
    txUpdLongitude.focusedProperty().addListener((obs, oldv, newv) -> txUpdLongitudeLostFocus(obs, oldv, newv));
    txUpdFotoFile.focusedProperty().addListener((obs, oldv, newv) -> txUpdFotoFileLostFocus(obs, oldv, newv));
    txUpdFromWEB.focusedProperty().addListener((obs, oldv, newv) -> txUpdtxUpdFromWEBLostFocus(obs, oldv, newv));
    updButtonsGest();
  }

  private void initializeTable(AppProperties p_props) {
    // attuale.setText("Attuale");
    tblvRecDB.setRowFactory(tv -> {
      TableRow<GeoCoord> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (row.isEmpty())
          return;
        if (event.getClickCount() == 1 && event.isControlDown()) {
          // System.out.println("Click + Ctrl !");
          GeoCoord fil = tblvRecDB.getSelectionModel().getSelectedItem();
          if (fil != null && fil.hasLonLat())
            mnuVaiCoord((ActionEvent) null);
          return;
        }
        if (event.getClickCount() == 2) {
          // System.out.println("Doppio Click");
          GeoCoord geo = tblvRecDB.getSelectionModel().getSelectedItem();
          Path pthFoto = geo.getFotoFile();
          if (geo != null && pthFoto != null) {
            // System.out.println("***** show foto " + pthFoto.toString());
            // imagePopupWindowShow(row);
            fotoViewProd.creaFotoViewer(row);
          }
        }

      });
      return row;
    });
    tblvRecDB.getSelectionModel().selectedItemProperty().addListener((obs, pold, pnew) -> {
      if (pnew != null) {
        try {
          GeoCoord clo = (GeoCoord) pnew.clone();
          updAddModificaDati(clo, true);
          TableRow<GeoCoord> row = new TableRow<GeoCoord>();
          GeoCoord geo = tblvRecDB.getSelectionModel().getSelectedItem();
          row.setItem(geo);
          fotoViewProd.newEvent("rowsel", row);
        } catch (CloneNotSupportedException e) {
          e.printStackTrace();
        }
        updContextMenu(pnew);
        updButtonsGest();
      }
    });

    creaColsTabViewUno();

    for (TableColumn<GeoCoord, ?> c : tblvRecDB.getColumns()) {
      readColumnWidth(p_props, c);
    }
    // aggiungo lo scroll sull'ultima riga dei logs
    tblvLogs.getItems()
        .addListener((ListChangeListener<Log4jRow>) s -> Platform.runLater(() -> tblvLogs.scrollTo(s.getList().size() - 1)));
  }

  private void creaColsTabViewUno() {
    if (m_nColsInfoType == 1)
      return;
    m_nColsInfoType = 1;
    int ncols = tblvRecDB.getColumns().size();
    for (int k = ncols - 1; k >= 0; k--)
      tblvRecDB.getColumns().remove(k);

    TableColumn<GeoCoord, LocalDateTime> colDatetime = new TableColumn<>("Date Time");
    colDatetime.setCellValueFactory(new PropertyValueFactory<GeoCoord, LocalDateTime>(COL01_DATETIME));
    tblvRecDB.getColumns().add(colDatetime);

    TableColumn<GeoCoord, Double> colLatitude = new TableColumn<>("Latitudine");
    colLatitude.setCellValueFactory(new PropertyValueFactory<GeoCoord, Double>(COL02_LATITUDE));
    tblvRecDB.getColumns().add(colLatitude);

    TableColumn<GeoCoord, Double> colLongitude = new TableColumn<>("Longitudine");
    colLongitude.setCellValueFactory(new PropertyValueFactory<GeoCoord, Double>(COL03_LONGITUDE));
    tblvRecDB.getColumns().add(colLongitude);

    TableColumn<GeoCoord, Double> colAltitude = new TableColumn<>("Distanza");
    colAltitude.setCellValueFactory(new PropertyValueFactory<GeoCoord, Double>(COL05_ALTITUDE));
    tblvRecDB.getColumns().add(colAltitude);

    TableColumn<GeoCoord, EGeoSrcCoord> colSource = new TableColumn<>("Sorgente");
    colSource.setCellValueFactory(new PropertyValueFactory<GeoCoord, EGeoSrcCoord>(COL04_SOURCE));
    tblvRecDB.getColumns().add(colSource);

    TableColumn<GeoCoord, String> colFotofile = new TableColumn<>("Foto File");
    colFotofile.setCellValueFactory(new PropertyValueFactory<GeoCoord, String>(COL05_FOTOFILE));
    tblvRecDB.getColumns().add(colFotofile);

    colDatetime.setCellFactory(column -> {
      return new MioTableCellRenderDate<GeoCoord, LocalDateTime>(COL01_DATETIME);
    });
    colLatitude.setCellFactory(column -> {
      return new MioTableCellRenderCoord<GeoCoord, Double>(COL02_LATITUDE);
    });
    colLongitude.setCellFactory(column -> {
      return new MioTableCellRenderCoord<GeoCoord, Double>(COL03_LONGITUDE);
    });
    colAltitude.setCellFactory(column -> {
      return new MioTableCellRenderDist<GeoCoord, Double>(COL05_ALTITUDE);
    });
  }

  private void creaColsTabViewDue() {
    if (m_nColsInfoType == 2)
      return;
    if (m_nColsInfoType != 1)
      creaColsTabViewUno();
    m_nColsInfoType = 2;
    //  e' gia la prima colonna inserita da "creaColsTabViewUno()"
    //    TableColumn<GeoCoord, LocalDateTime> colAssunta = new TableColumn<>("Assunta");
    //    colAssunta.setCellValueFactory(new PropertyValueFactory<GeoCoord, LocalDateTime>("dtAssunta"));
    //    tblvRecDB.getColumns().add(colAssunta);
    //    colAssunta.setCellFactory(column -> {
    //      return new MioTableCellRenderDate<GeoCoord, LocalDateTime>("dtAssunta");
    //    });

    TableColumn<GeoCoord, LocalDateTime> colNomeDir = new TableColumn<>("NomeDir");
    colNomeDir.setCellValueFactory(new PropertyValueFactory<GeoCoord, LocalDateTime>("dtNomeDir"));
    tblvRecDB.getColumns().add(colNomeDir);
    colNomeDir.setCellFactory(column -> {
      return new MioTableCellRenderDate<GeoCoord, LocalDateTime>("dtNomeDir");
    });

    TableColumn<GeoCoord, LocalDateTime> colNomeFile = new TableColumn<>("NomeFile");
    colNomeFile.setCellValueFactory(new PropertyValueFactory<GeoCoord, LocalDateTime>("dtNomeFile"));
    tblvRecDB.getColumns().add(colNomeFile);
    colNomeFile.setCellFactory(column -> {
      return new MioTableCellRenderDate<GeoCoord, LocalDateTime>("dtNomeFile");
    });

    TableColumn<GeoCoord, LocalDateTime> coldtCreazione = new TableColumn<>("dtCreazione");
    coldtCreazione.setCellValueFactory(new PropertyValueFactory<GeoCoord, LocalDateTime>("dtCreazione"));
    tblvRecDB.getColumns().add(coldtCreazione);
    coldtCreazione.setCellFactory(column -> {
      return new MioTableCellRenderDate<GeoCoord, LocalDateTime>("dtCreazione");
    });

    TableColumn<GeoCoord, LocalDateTime> colUltModif = new TableColumn<>("UltModif");
    colUltModif.setCellValueFactory(new PropertyValueFactory<GeoCoord, LocalDateTime>("dtUltModif"));
    tblvRecDB.getColumns().add(colUltModif);
    colUltModif.setCellFactory(column -> {
      return new MioTableCellRenderDate<GeoCoord, LocalDateTime>("dtUltModif");
    });

    TableColumn<GeoCoord, LocalDateTime> colAcquisizione = new TableColumn<>("Acquisizione");
    colAcquisizione.setCellValueFactory(new PropertyValueFactory<GeoCoord, LocalDateTime>("dtAcquisizione"));
    tblvRecDB.getColumns().add(colAcquisizione);
    colAcquisizione.setCellFactory(column -> {
      return new MioTableCellRenderDate<GeoCoord, LocalDateTime>("dtAcquisizione");
    });

  }

  private void updContextMenu(GeoCoord pnew) {
    boolean bvDis = !pnew.hasLonLat();
    mnuCtxVaiCoord.setDisable(bvDis);
    mnuCtxLatMin.setDisable(bvDis);
    mnuCtxLatMax.setDisable(bvDis);
    mnuCtxLonMin.setDisable(bvDis);
    mnuCtxLonMax.setDisable(bvDis);
    mnuCtxGessLoc.setDisable( !bvDis);
  }

  private void leggiProperties(Stage mainstage) {
    AppProperties props = AppProperties.getInstance();
    int posX = props.getIntProperty(AppProperties.CSZ_PROP_POSFRAME_X);
    int posY = props.getIntProperty(AppProperties.CSZ_PROP_POSFRAME_Y);
    int dimX = props.getIntProperty(AppProperties.CSZ_PROP_DIMFRAME_X);
    int dimY = props.getIntProperty(AppProperties.CSZ_PROP_DIMFRAME_Y);

    double minx = 0, maxx = 0, miny = 0, maxy = 0;

    for (Screen scr : Screen.getScreens()) {
      Rectangle2D schermo = scr.getBounds();
      System.out.println(schermo);
      minx = schermo.getMinX() < minx ? schermo.getMinX() : minx;
      maxx = schermo.getMaxX() >= maxx ? schermo.getMaxX() : maxx;
      miny = schermo.getMinY() < miny ? schermo.getMinY() : miny;
      maxy = schermo.getMaxY() >= maxy ? schermo.getMaxY() : maxy;
    }
    //    System.out.printf("X %.2f - %.2f\n", minx, maxx);
    //    System.out.printf("Y %.2f - %.2f\n", miny, maxy);

    if (dimX * dimY != 0) {
      mainstage.setWidth(dimX);
      mainstage.setHeight(dimY);
    }
    if (posX * posY != 0) {
      posX = (int) (posX < minx ? minx : posX);
      posY = (int) (posY < minx ? minx : posY);
      mainstage.setX(posX);
      mainstage.setY(posY);
    }

    ChangeListener<Boolean> list = new ChangeListener<Boolean>() {

      @Override
      public void changed(ObservableValue<? extends Boolean> p_observable, Boolean p_oldValue, Boolean p_newValue) {
        if (p_newValue) {
          setSplitPos();
          p_observable.removeListener(this);
        }
      }
    };
    // se setto solo splitPos() (Senza passare attraverso lo showingProperty)
    // lo stage subisce un resize quando la window finisce di costruire
    mainstage.showingProperty().addListener(list);
  }

  @FXML
  public void mnuFEsci(ActionEvent e) {
    exitApplication(null);
  }

  @FXML
  public void mnuFExportGPXClick(ActionEvent e) {
    m_model.setInvalidGPX(true);
    btCercaGPXClick(null);
    if ( !m_model.isInvalidGPX())
      btSaveToGPXClick(null);
  }

  @FXML
  public void mnuFReadFotoClick(ActionEvent e) {
    m_model.setTipoSource(EGeoSrcCoord.foto);
    btCercaSourceClick((ActionEvent) null);
    btApriSourceClick((ActionEvent) null);
    //    btCercaSourceClick(null);
    //    if ( !m_model.isInvalidSrc())
    //      btApriSourceClick(null);
  }

  @FXML
  public void mnuFReadGoogleClick(ActionEvent e) {
    m_model.setTipoSource(EGeoSrcCoord.google);
    m_model.setInvalidSrc(true);
    btCercaSourceClick(null);
    if ( !m_model.isInvalidSrc())
      btApriSourceClick(null);
  }

  @FXML
  public void mnuFReadTrackClick(ActionEvent e) {
    m_model.setTipoSource(EGeoSrcCoord.track);
    m_model.setInvalidSrc(true);
    btCercaSourceClick(null);
    if ( !m_model.isInvalidSrc())
      btApriSourceClick(null);
  }

  private void mnuGuessLocation(ActionEvent p_ev) {
    GeoCoord fil = tblvRecDB.getSelectionModel().getSelectedItem();
    guessPosition(fil);
    tblvRecDB.refresh();
    updAddModificaDati(fil, true);
  }

  private void mnuSetCtxDtMinute(ActionEvent ev, int p_minutes) {
    GeoCoord geo = tblvRecDB.getSelectionModel().getSelectedItem();
    FiltroGeoCoord filtro = m_model.getFiltro();

    LocalDateTime dt = geo.getTstamp().minusMinutes(p_minutes);
    txFltrDtMin.setText(ParseData.s_fmtDtExif.format(dt));
    filtro.setDtMin(dt);

    dt = geo.getTstamp().plusMinutes(p_minutes);
    txFltrDtMax.setText(ParseData.s_fmtDtExif.format(dt));
    filtro.setDtMax(dt);

    btFltrFiltraClick((ActionEvent) null);
  }

  public void mnuFSalvaInterpolaClick(ActionEvent e) {
    GeoList li = m_model.getGeoList();
    li //
        .stream() //
        .filter( //
            geo -> null != geo.getFotoFile() && //
                geo.hasLonLat() && //
                geo.isGuessed()) //
        .forEach(geo -> m_model.saveFotoFile(geo));
    tblvRecDB.refresh();
  }

  @FXML
  public void mnuFRinominaFotoClick(ActionEvent e) {
    //    GeoList li = m_model.getGeoList();
    //    li //
    //        .stream() //
    //        .filter( //
    //            geo -> geo.hasFotoFile())
    //        .forEach(geo -> m_model.renameFotoFile(geo));
    Button[] enaDis = { btSaveToGPX, btUpdRenameAllFoto };
    lanciaMainAppBackGroundWork(DataModelGpsInfo.ThreadWork.RinominaFotoFile, enaDis);
    // btRicaricaSrcClickThread((ActionEvent) null);
  }

  public void mnuEInterpolaClick(ActionEvent e) {
    GeoList li = m_model.getGeoList();
    li //
        .stream() //
        .filter(geo -> geo.getSrcGeo() == EGeoSrcCoord.foto && !geo.hasLonLat()) //
        .forEach(geo -> guessPosition(geo));
    tblvRecDB.refresh();
  }

  private void guessPosition(GeoCoord fil) {
    if (null == fil.getFotoFile())
      return;
    s_log.debug("Guess position for {} foto", fil.getFotoFile().toString());
    GeoCoord guess = m_model.getGeoList().findNearest(fil.getTstamp());
    if (guess == null) {
      s_log.warn("Non trovo posizione per la foto {}", fil.getFotoFile().toString());
      return;
    }
    double lat = guess.getLatitude();
    double lon = guess.getLongitude();
    if (lat * lon != 0) {
      String lnk = String.format(Locale.US, lNK_MAPS, lat, lon);
      final ClipboardContent cont = new ClipboardContent();
      cont.putString(lnk);
      Clipboard.getSystemClipboard().setContent(cont);
      s_log.debug("Guess pos: {}", lnk);
      fil.setGuessed(true);
      fil.setLatitude(lat);
      fil.setLongitude(lon);
    }
  }

  private void mnuSetFltrDtMax(ActionEvent p_ev) {
    FiltroGeoCoord filtro = m_model.getFiltro();
    GeoCoord coo = tblvRecDB.getSelectionModel().getSelectedItem();
    LocalDateTime dt = coo.getTstamp();
    txFltrDtMax.setText(ParseData.s_fmtDtExif.format(dt));
    filtro.setDtMax(dt);
  }

  private void mnuSetFltrDtMin(ActionEvent p_ev) {
    GeoCoord coo = tblvRecDB.getSelectionModel().getSelectedItem();
    LocalDateTime dt = coo.getTstamp();
    txFltrDtMin.setText(ParseData.s_fmtDtExif.format(dt));
    FiltroGeoCoord filtro = m_model.getFiltro();
    filtro.setDtMin(dt);
  }

  private void mnuSetFltrLatMax(ActionEvent p_ev) {
    FiltroGeoCoord filtro = m_model.getFiltro();
    GeoCoord coo = tblvRecDB.getSelectionModel().getSelectedItem();
    double dbl = coo.getLatitude();
    txFltrLatMax.setText(String.format(Locale.US, "%.10f", dbl));
    filtro.setLatMax(dbl);
  }

  private void mnuSetFltrLatMin(ActionEvent p_ev) {
    FiltroGeoCoord filtro = m_model.getFiltro();
    GeoCoord coo = tblvRecDB.getSelectionModel().getSelectedItem();
    double dbl = coo.getLatitude();
    txFltrLatMin.setText(String.format(Locale.US, "%.10f", dbl));
    filtro.setLatMin(dbl);
  }

  private void mnuSetFltrLonMax(ActionEvent p_ev) {
    FiltroGeoCoord filtro = m_model.getFiltro();
    GeoCoord coo = tblvRecDB.getSelectionModel().getSelectedItem();
    double dbl = coo.getLongitude();
    txFltrLonMax.setText(String.format(Locale.US, "%.10f", dbl));
    filtro.setLonMax(dbl);
  }

  private void mnuSetFltrLonMin(ActionEvent p_ev) {
    FiltroGeoCoord filtro = m_model.getFiltro();
    GeoCoord coo = tblvRecDB.getSelectionModel().getSelectedItem();
    double dbl = coo.getLongitude();
    txFltrLonMin.setText(String.format(Locale.US, "%.10f", dbl));
    filtro.setLonMin(dbl);
  }

  private void mnuVaiCoord(ActionEvent p_ev) {
    // System.out.println("MainApp2FxmlController.mnuVaiCoord():" + p_ev);
    GeoCoord fil = tblvRecDB.getSelectionModel().getSelectedItem();

    if (fil != null) {
      double lat = fil.getLatitude();
      double lon = fil.getLongitude();
      if (lat * lon != 0) {
        String lnk = String.format(Locale.US, lNK_MAPS, lat, lon);
        final ClipboardContent cont = new ClipboardContent();
        cont.putString(lnk);
        Clipboard.getSystemClipboard().setContent(cont);
        MainAppGpsInfo ma = MainAppGpsInfo.getInst();
        ma.showLink(lnk);
        s_log.info("ClipBoard Copied: {}", lnk);
      }
    } else {
      System.out.println("No file selected!");
    }
  }

  @FXML
  public void mnuhAbout() {
    String szMsg = "Versione dell'applicazione\n" + Versione.getVersionEx();
    msgBox(szMsg, AlertType.INFORMATION, IMAGE_EDITING_ICO);
  }

  @SuppressWarnings("unused")
  private void msgBox(String p_txt) {
    msgBox(p_txt, AlertType.INFORMATION);
  }

  private boolean msgBox(String p_txt, AlertType tipo) {
    return msgBox(p_txt, tipo, (String) null);
  }

  private boolean msgBox(String p_txt, AlertType tipo, String p_ico) {
    boolean bRet = true;
    Alert alt = new Alert(tipo);
    Scene sce = MainAppGpsInfo.getInst().getPrimaryStage().getScene();
    if (null != p_ico) {
      URL resico = getClass().getResource(p_ico);
      if (null == resico)
        resico = getClass().getClassLoader().getResource(IMAGE_EDITING_ICO);
      if (null != resico) {
        ImageView ico = new ImageView(resico.toString());
        alt.setGraphic(ico);
      }
    }

    Window wnd = null;
    if (sce != null)
      wnd = sce.getWindow();
    if (wnd != null) {
      alt.initOwner(wnd);
      alt.setTitle(tipo.toString());
      alt.setHeaderText(tipo.toString());
      alt.setContentText(p_txt);
      Optional<ButtonType> result = alt.showAndWait();
      if (tipo == AlertType.CONFIRMATION) {
        bRet = result.get() == ButtonType.OK;
      }
    } else
      s_log.error("Windows==null; msg={}", p_txt);
    return bRet;
  }

  private void preparaLogPanel(AppProperties props) {
    MioAppender.setLogReader(this);
    // -------- combo level -------
    cbLevelMin.getItems().addAll(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL);
    levelMin = Level.INFO;
    if (props != null) {
      String sz = props.getProperty(CSZ_LOG_LEVEL);
      if (sz != null)
        levelMin = Level.toLevel(sz);
    }
    cbLevelMin.getSelectionModel().select(levelMin);

    // -------- table view log info -------------------
    tblvLogs.setPlaceholder(new Label("Nessun messaggio da mostrare" + ""));
    tblvLogs.setFixedCellSize(21.0);
    tblvLogs.setRowFactory(row -> new TableRow<Log4jRow>() {
      @Override
      public void updateItem(Log4jRow item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
          setStyle("");
          return;
        }
        String cssSty = "-fx-background-color: ";
        Level tip = item.getLevel();
        StandardLevel lev = tip.getStandardLevel();
        switch (lev) {
          case TRACE:
            cssSty += "beige";
            break;
          case DEBUG:
            cssSty += "silver";
            break;
          case INFO:
            cssSty = "";
            break;
          case WARN:
            cssSty += "coral";
            break;
          case ERROR:
            cssSty += "hotpink";
            break;
          case FATAL:
            cssSty += "deeppink";
            break;
          default:
            cssSty = "";
            break;
        }
        setStyle(cssSty);
      }
    });

    colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
    colLev.setCellValueFactory(new PropertyValueFactory<>("level"));
    colMsg.setCellValueFactory(new PropertyValueFactory<>("message"));
    // leggo le properties colonne dei log
    for (TableColumn<Log4jRow, ?> c : tblvLogs.getColumns()) {
      readColumnWidth(props, c);
    }
  }

  //  private void preparaUpdPanel(AppProperties p_props, Stage p_mainstage) {
  //    // cosa devo fare qui ?
  //  }

  private void readColumnWidth(AppProperties p_prop, TableColumn<?, ?> p_col) {
    String id = p_col.getText().replace(' ', '_');
    String sz = String.format(CSZ_PROP_COL, id);
    double w = p_prop.getDoubleProperty(sz);
    if (w > 0) {
      p_col.setPrefWidth(w);
      // System.out.printf("DBGpsInfoController.readColumnWidth(%.2f)\n", w);
    }
  }

  private void saveColumnWidth(AppProperties p_prop) {
    for (TableColumn<GeoCoord, ?> c : tblvRecDB.getColumns()) {
      // System.out.printf("MainApp2FxmlController.saveColumnWidth(\"%s\")\n", c.getId());
      saveColumnWidth(p_prop, c);
    }
    for (TableColumn<Log4jRow, ?> c : tblvLogs.getColumns()) {
      // System.out.printf("MainApp2FxmlController.saveColumnWidth(\"%s\")\n", c.getId());
      saveColumnWidth(p_prop, c);
    }
  }

  private void saveColumnWidth(AppProperties p_prop, TableColumn<?, ?> p_col) {
    String id = p_col.getText().replace(' ', '_');
    String sz = String.format(CSZ_PROP_COL, id);
    p_prop.setDoubleProperty(sz, p_col.getWidth());
  }

  private void setSplitPos() {
    AppProperties props = AppProperties.getInstance();
    Double dblPos = props.getDoubleProperty(CSZ_SPLITPOS);
    if (dblPos != null) {
      spltPane.setDividerPositions(dblPos);
    }
    dblPos = props.getDoubleProperty(CSZ_SPLITPOSTAB);
    if (dblPos != null) {
      spltPaneTab.setDividerPositions(dblPos);
    }
  }

  private Object txFileDBLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    if ( !p_newv) {
      String szOld = m_model.getDbName() != null ? m_model.getDbName().toString() : null;
      String szFi = txDBFile.getText();
      if (Utils.isChanged(szFi, szOld)) {
        s_log.info("Utilizzo {} come file per salva DB", szFi);
        m_model.setDestDB(Paths.get(szFi));
      }
    }
    return null;
  }

  private Object txFileSorgLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    if ( !p_newv) {
      String szOld = m_model.getSrcDir() != null ? m_model.getSrcDir().toString() : null;
      String szFi = txFileSorg.getText();
      if (Utils.isChanged(szFi, szOld)) {
        s_log.info("Utilizzo {} come sorgente", szFi);
        m_model.setSrcDir(Paths.get(szFi));
      }
    }
    return null;
  }

  private Object txFltrDtMaxLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    // System.out.printf("DBGpsInfoController.txDtMaxLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
    GeoFormatter fmt = new GeoFormatter();
    FiltroGeoCoord filtro = m_model.getFiltro();
    if ( !p_newv) {
      LocalDateTime dtOld = filtro.getDtMax() != null ? filtro.getDtMax() : null;
      String szDt = txFltrDtMax.getText();
      LocalDateTime dtVal = szDt != null && szDt.length() > 10 ? fmt.parseTStamp(szDt) : null;
      if (Utils.isChanged(dtVal, dtOld)) {
        filtro.setDtMax(dtVal);
      }
    }
    return null;
  }

  private Object txFltrDtMinLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    // System.out.printf("DBGpsInfoController.txDtMinLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
    GeoFormatter fmt = new GeoFormatter();
    FiltroGeoCoord filtro = m_model.getFiltro();
    if ( !p_newv) {
      LocalDateTime dtOld = filtro.getDtMin() != null ? filtro.getDtMin() : null;
      String szDt = txFltrDtMin.getText();
      LocalDateTime dtVal = szDt != null && szDt.length() > 10 ? fmt.parseTStamp(szDt) : null;
      if (Utils.isChanged(dtVal, dtOld)) {
        filtro.setDtMin(dtVal);
      }
    }
    return null;
  }

  private Object txFltrLatMaxLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    // System.out.printf("DBGpsInfoController.txLatMaxLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
    // GeoFormatter fmt = new GeoFormatter();
    FiltroGeoCoord filtro = m_model.getFiltro();
    if ( !p_newv) {
      Double dtOld = filtro.getLatMax() != null ? filtro.getLatMax() : null;
      Double dtVal = null;
      String sz = txFltrLatMax.getText();
      if (sz != null && sz.length() > 0)
        dtVal = Double.parseDouble(txFltrLatMax.getText());
      if (Utils.isChanged(dtVal, dtOld)) {
        filtro.setLatMax(dtVal);
      }
    }
    return null;
  }

  private Object txFltrLatMinLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    // System.out.printf("DBGpsInfoController.txLatMinLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
    FiltroGeoCoord filtro = m_model.getFiltro();
    if ( !p_newv) {
      Double dtOld = filtro.getLatMin() != null ? filtro.getLatMin() : null;
      Double dtVal = null;
      String sz = txFltrLatMin.getText();
      if (sz != null && sz.length() > 0)
        dtVal = Double.parseDouble(sz);
      if (Utils.isChanged(dtVal, dtOld)) {
        filtro.setLatMin(dtVal);
      }
    }
    return null;
  }

  private Object txFltrLonMaxLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    // System.out.printf("DBGpsInfoController.txLonMaxLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
    FiltroGeoCoord filtro = m_model.getFiltro();
    if ( !p_newv) {
      Double dtOld = filtro.getLonMax() != null ? filtro.getLonMax() : null;
      Double dtVal = null;
      String sz = txFltrLonMax.getText();
      if (sz != null && sz.length() > 0)
        dtVal = Double.parseDouble(sz);
      if (Utils.isChanged(dtVal, dtOld)) {
        filtro.setLonMax(dtVal);
      }
    }
    return null;
  }

  private Object txFltrLonMinLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    // System.out.printf("DBGpsInfoController.txLonMinLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
    FiltroGeoCoord filtro = m_model.getFiltro();
    if ( !p_newv) {
      Double dtOld = filtro.getLonMin() != null ? filtro.getLonMin() : null;
      Double dtVal = null;
      String sz = txFltrLonMin.getText();
      if (sz != null && sz.length() > 0)
        dtVal = Double.parseDouble(sz);
      if (Utils.isChanged(dtVal, dtOld)) {
        filtro.setLonMin(dtVal);
      }
    }
    return null;
  }

  private Object txUpdDatetimeLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    if (m_updGeo == null)
      m_updGeo = new GeoCoord();
    try {
      if ( !p_newv) {
        String szDt = txUpdDatetime.getText();
        m_updGeo.setTstampNew(m_updGeoFmt.parseTStamp(szDt));
        updAddModificaDati(m_updGeo, false);
        updButtonsGest();
      }
    } catch (Exception e) {
      // e.printStackTrace();
      s_log.error("Upd TStamp error {}", e.getMessage(), e);
    }
    return null;
  }

  private Object cbUpdTipoSrcClick(ObservableValue<? extends EGeoSrcCoord> p_obs, EGeoSrcCoord p_oldv, EGeoSrcCoord p_newv) {
    if (m_updGeo == null)
      m_updGeo = new GeoCoord();
    try {
      m_updGeo.setSrcGeo(cbUpdTipoSrc.getSelectionModel().getSelectedItem());
      updAddModificaDati(m_updGeo, false);
      updButtonsGest();
    } catch (Exception e) {
      //
    }
    return null;
  }

  private Object txUpdtxUpdFromWEBLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    if (m_updGeo == null)
      m_updGeo = new GeoCoord();
    try {
      if ( !p_newv) {
        // m_updGeoFmt.parseTStamp(m_updGeo, txUpdDatetime.getText());
        btUpdParseWEBClick(null);
        updAddModificaDati(m_updGeo, false);
        updButtonsGest();
      }
    } catch (Exception e) {
      //
    }
    return null;
  }

  private Object txUpdLongitudeLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    if (m_updGeo == null)
      m_updGeo = new GeoCoord();
    try {
      if ( !p_newv) {
        m_updGeoFmt.parseLongitude(m_updGeo, txUpdLongitude.getText());
        updAddModificaDati(m_updGeo, false);
        updButtonsGest();
      }
    } catch (Exception e) {
      //
    }
    return null;
  }

  private Object txUpdLatitudeLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    if (m_updGeo == null)
      m_updGeo = new GeoCoord();
    try {
      if ( !p_newv) {
        m_updGeoFmt.parseLatitude(m_updGeo, txUpdLatitude.getText());
        updAddModificaDati(m_updGeo, false);
        updButtonsGest();
      }
    } catch (Exception e) {
      //
    }
    return null;
  }

  private Object txUpdFotoFileLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    if (m_updGeo == null)
      m_updGeo = new GeoCoord();
    try {
      if ( !p_newv)
        m_updGeo.setFotoFile(Paths.get(txUpdFotoFile.getText()));
    } catch (Exception e) {
      //
    }
    updButtonsGest();
    return null;
  }

  private Object txGPXFileLostFocus(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    // System.out.printf("DBGpsInfoController.txGPXFileLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
    if ( !p_newv) {
      String szOld = m_model.getDestGPXfile() != null ? m_model.getDestGPXfile().toString() : null;
      String szFi = txGPXFile.getText();
      if (Utils.isChanged(szFi, szOld)) {
        s_log.info("Utilizzo {} come file per salva GPX", szFi);
        m_model.setDestGPXfile(Paths.get(szFi));
      }
    }
    return null;
  }

  //  public void imagePopupWindowShow(TableRow<GeoCoord> row) {
  //    fotoViewProd.creaFotoViewer(row);
  //  }

}

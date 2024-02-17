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

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import sm.clagenna.dbgps.sys.Versione;
import sm.clagenna.stdcla.enums.EServerId;
import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoFormatter;
import sm.clagenna.stdcla.geo.GeoList;
import sm.clagenna.stdcla.javafx.ImageViewResizer;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ILog4jReader;
import sm.clagenna.stdcla.utils.Log4jRow;
import sm.clagenna.stdcla.utils.MioAppender;
import sm.clagenna.stdcla.utils.ParseData;
import sm.clagenna.stdcla.utils.Utils;

public class RegJpsInfoController implements Initializable, ILog4jReader {
  private static final Logger s_log             = LogManager.getLogger(RegJpsInfoController.class);
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
  private static final String CSZ_SPLITPOSTAB = "splitpostab";
  private static final String CSZ_SPLITPOS    = "splitpos";

  @FXML
  private SplitPane spltPaneTab;

  @FXML
  private TextField              txFileSorg;
  @FXML
  private Button                 btCercaFileSrc;
  @FXML
  private ComboBox<EGeoSrcCoord> cbTipoFileSrc;
  @FXML
  private Button                 btApriFileSrc;

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
  private TextField           txDBPswd;
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
  private SplitPane                            spltPane;
  @FXML
  private TableView<GeoCoord>                  tblvRecDB;
  @FXML
  private TableColumn<GeoCoord, LocalDateTime> colDatetime;
  @FXML
  private TableColumn<GeoCoord, Double>        colLatitude;
  @FXML
  private TableColumn<GeoCoord, Double>        colLongitude;
  @FXML
  private TableColumn<GeoCoord, Double>        colAlitude;
  @FXML
  private TableColumn<GeoCoord, EGeoSrcCoord>  colSource;
  @FXML
  private TableColumn<GeoCoord, String>        colFotofile;

  private MenuItem mnuCtxVaiCoord;
  private MenuItem mnuCtxDtMin;
  private MenuItem mnuCtxDtMax;
  private MenuItem mnuCtxLonMin;
  private MenuItem mnuCtxLonMax;
  private MenuItem mnuCtxLatMin;
  private MenuItem mnuCtxLatMax;
  private MenuItem mnuCtxGessLoc;

  @FXML
  private TextField              txUpdDatetime;
  @FXML
  private TextField              txUpdFromWEB;
  @FXML
  private Button                 btUpdParseWEB;
  @FXML
  private Label                  lbUpdLongitude;
  @FXML
  private Label                  lbUpdLatitude;
  @FXML
  private TextField              txUpdLongitude;
  @FXML
  private TextField              txUpdLatitude;
  @FXML
  private ComboBox<EGeoSrcCoord> cbUpdTipoSrc;
  @FXML
  private TextField              txUpdFotoFile;
  @FXML
  private Button                 btUpdModif;
  @FXML
  private Button                 btUpdInsert;
  @FXML
  private Button                 btUpdDelete;
  @FXML
  private Button                 btUpdSaveFoto;
  @FXML
  private Button                 btUpdClear;
  @FXML
  private Button                 btUpdRenameAllFoto;

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
  private ComboBox<Level>  cbLevelMin;
  @FXML
  private Label            lblLogs;
  private Level            levelMin;
  private List<Log4jRow>   m_liMsgs;
  private DataModelGpsInfo m_model;
  private GeoFormatter     m_updGeoFmt;
  /** il clone del GeoCoord da trattare */
  private GeoCoord         m_updGeo;
  /** GeoCoord della {@link GeoList} originale (quindi con stesso hash) */
  private GeoCoord         m_updGeoOrig;

  public RegJpsInfoController() {
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
    // if ( rig.getLevel().isInRange( Level.FATAL, levelMin )) // isLessSpecificThan(levelMin))
    if (rig.getLevel().intLevel() <= levelMin.intLevel()) {
      ObservableList<Log4jRow> itms = tblvLogs.getItems();
      itms.add(rig);
      if (itms.size() > 4) {

        Platform.runLater(new Runnable() {
          @Override
          public void run() {
            tblvLogs.scrollTo(itms.size() - 1);
          }
        });
      }
    }
  }

  private void updButtonsGest() {
    if (m_updGeo == null) {
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

  private void updAddModificaDati(GeoCoord p_pnew, boolean bNewData) {
    if (p_pnew != null) {
      txUpdDatetime.setText(GeoFormatter.s_fmtmY4MD_hms.format(p_pnew.getTstamp()));
      double dbl = p_pnew.getLongitude();
      if (dbl != 0)
        txUpdLongitude.setText(MioTableCellRenderCoord.s_fmt.format(dbl));
      else
        txUpdLongitude.setText(null);
      dbl = p_pnew.getLatitude();
      if (dbl != 0)
        txUpdLatitude.setText(MioTableCellRenderCoord.s_fmt.format(dbl));
      else
        txUpdLatitude.setText(null);
      cbUpdTipoSrc.getSelectionModel().select(p_pnew.getSrcGeo());
      Path fo = p_pnew.getFotoFile();
      btUpdSaveFoto.setDisable(fo == null || !p_pnew.hasLonLat());
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
        m_updGeo = (GeoCoord) p_pnew.clone();
        m_updGeoOrig = p_pnew;
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
    m_model.leggiDB();
    caricaLaGrigliaGeo();
  }

  @FXML
  public void btApriSourceClick(ActionEvent event) {
    m_model.parseSource();
    caricaLaGrigliaGeo();
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

    caricaLaGrigliaGeo();
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
    caricaLaGrigliaGeo();
  }

  @FXML
  public void btSalvaDBClick(ActionEvent event) {
    if (m_model == null)
      return;
    m_model.salvaDB();
    caricaLaGrigliaGeo();
  }

  @FXML
  public void btSaveToGPXClick(ActionEvent event) {
    m_model.saveToGPX();
  }

  @FXML
  public void btUpdParseWEB(ActionEvent event) {
    System.out.printf("btUpdParseWEB(\"%s\")\n", txUpdFromWEB.getText());
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
    caricaLaGrigliaGeo();
  }

  @FXML
  public void btUpdModifClick(ActionEvent event) {
    if (null == m_updGeo)
      return;
    GeoList li = m_model.getGeoList();
    if (null == li)
      li = m_model.initData();
    if (li.size() == 0)
      return;
    //    testUpdOrig(m_updGeoOrig);
    if (null != m_updGeoOrig) {
      GeoCoord it = null;
      int indx = li.indexOf(m_updGeoOrig);
      if (indx >= 0) {
        it = li.get(indx);
        it.assign(m_updGeo);
        updClearUpd();
      }
    }
    //    testUpdOrig(m_updGeoOrig);
    caricaLaGrigliaGeo();
  }

  @FXML
  public void btUpdDeleteClick(ActionEvent event) {
    testUpdOrig(m_updGeoOrig);
    GeoList li = m_model.getGeoList();
    int indx = li.indexOf(m_updGeoOrig);
    if (indx >= 0) {
      @SuppressWarnings("unused")
      GeoCoord it = li.get(indx);
      System.out.println("boh!");
    }
    caricaLaGrigliaGeo();
  }

  @FXML
  public void btUpdClearClick(ActionEvent event) {
    if (msgBox("Sicuro di cancellare i dati della Griglia ?", AlertType.CONFIRMATION)) {
      m_model.initData();
      caricaLaGrigliaGeo();
      updClearUpd();
    }
  }

  @FXML
  private void btUpdSaveFotoClick(ActionEvent event) {
    if (m_updGeo == null || //
        m_updGeo.getFotoFile() == null || //
        !m_updGeo.isGuessed() || //
        !m_updGeo.hasLonLat())
      return;
    m_model.saveFotoFile(m_updGeo);
    tblvRecDB.refresh();
  }

  @FXML
  private void btUpdRenameAllFotoClick(ActionEvent event) {
    System.out.println("RegJpsInfoController.btUpdRenameAllFotoClick()");
  }

  private void caricaLaGrigliaGeo() {
    GeoList li = m_model.getGeoList();
    ObservableList<GeoCoord> itms = tblvRecDB.getItems();
    itms.clear();
    //    System.out.printf("Presente Indeterm=%s\n\tsel=%s\n", //
    //        ckFilePresent.isIndeterminate(), //
    //        ckFilePresent.isSelected());
    if (li == null) {
      lblLogs.setText("Letti 0 recs");
      return;
    }
    GeoCoord prec = null;
    for (GeoCoord geo : li) {
      if (prec != null)
        geo.altitudeAsDistance(prec);
      itms.add(geo);
      if (geo.hasLonLat())
        prec = geo;
    }
    GeoCoord minGeo = m_model.getMingeo();
    GeoCoord maxGeo = m_model.getMaxgeo();
    lbFltrLonMin.setText(String.format(Locale.US, "%.10f", minGeo.getLongitude()));
    lbFltrLonMax.setText(String.format(Locale.US, "%.10f", maxGeo.getLongitude()));
    lbFltrLatMin.setText(String.format(Locale.US, "%.10f", minGeo.getLatitude()));
    lbFltrLatMax.setText(String.format(Locale.US, "%.10f", maxGeo.getLatitude()));

    lblLogs.setText(String.format("Letti %d recs", li.size()));
  }

  @FXML
  private void cbLevelMinSel(ActionEvent event) {
    levelMin = cbLevelMin.getSelectionModel().getSelectedItem();
    // System.out.println("ReadFattHTMLController.cbLevelMinSel():" + levelMin.name());
    tblvLogs.getItems().clear();
    if (m_liMsgs == null || m_liMsgs.size() == 0)
      return;
    // List<Log4jRow> li = m_liMsgs.stream().filter(s -> s.getLevel().isInRange(Level.FATAL, levelMin )).toList(); // !s.getLevel().isLessSpecificThan(levelMin)).toList();
    List<Log4jRow> li = m_liMsgs.stream().filter(s -> s.getLevel().intLevel() <= levelMin.intLevel()).toList();
    tblvLogs.getItems().addAll(li);
  }

  @FXML
  private void cbTipoDBSrcSel(ActionEvent event) {
    EServerId tp = cbTipoDb.getSelectionModel().getSelectedItem();
    m_model.setTipoDB(tp);
    switch (tp) {
      case SqlServer:
        txDBHost.setDisable(false);
        txDBService.setDisable(false);
        txDBUser.setDisable(false);
        txDBPswd.setDisable(false);
        break;
      default:
        txDBHost.setDisable(true);
        txDBService.setDisable(true);
        txDBUser.setDisable(true);
        txDBPswd.setDisable(true);
        break;
    }
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

  private Object ckDatetimeUniqueClick(ObservableValue<? extends Boolean> p_obs, Boolean p_oldv, Boolean p_newv) {
    m_model.setDateTimeUnique(ckDatetimeUnique.isSelected());
    // System.out.printf("ckDatetimeUnique Click(%s)\n", m_model.isDateTimeUnique());
    tblvRecDB.refresh();
    return null;
  }

  private void creaContextMenu() {
    ContextMenu cntxMenu = new ContextMenu();
    mnuCtxVaiCoord = new MenuItem("Vai Coord.");
    mnuCtxVaiCoord.setOnAction((ActionEvent ev) -> {
      mnuVaiCoord(ev);
    });
    cntxMenu.getItems().add(mnuCtxVaiCoord);
    cntxMenu.getItems().add(new SeparatorMenuItem());
    mnuCtxDtMin = new MenuItem("Filtro Dt.Min.");
    mnuCtxDtMin.setOnAction((ActionEvent ev) -> {
      mnuSetFltrDtMin(ev);
    });
    cntxMenu.getItems().add(mnuCtxDtMin);

    mnuCtxDtMax = new MenuItem("Filtro Dt.Max.");
    mnuCtxDtMax.setOnAction((ActionEvent ev) -> {
      mnuSetFltrDtMax(ev);
    });
    cntxMenu.getItems().add(mnuCtxDtMax);

    mnuCtxLonMin = new MenuItem("Filtro Lon. Min");
    mnuCtxLonMin.setOnAction((ActionEvent ev) -> {
      mnuSetFltrLonMin(ev);
    });
    cntxMenu.getItems().add(mnuCtxLonMin);

    mnuCtxLonMax = new MenuItem("Filtro Lon. Max");
    mnuCtxLonMax.setOnAction((ActionEvent ev) -> {
      mnuSetFltrLonMax(ev);
    });
    cntxMenu.getItems().add(mnuCtxLonMax);

    mnuCtxLatMin = new MenuItem("Filtro Lat. Min");
    mnuCtxLatMin.setOnAction((ActionEvent ev) -> {
      mnuSetFltrLatMin(ev);
    });
    cntxMenu.getItems().add(mnuCtxLatMin);

    mnuCtxLatMax = new MenuItem("Filtro Lat. Max");
    mnuCtxLatMax.setOnAction((ActionEvent ev) -> {
      mnuSetFltrLatMax(ev);
    });
    cntxMenu.getItems().add(mnuCtxLatMax);

    cntxMenu.getItems().add(new SeparatorMenuItem());

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
    cbUpdTipoSrc.getItems().add(null);
    cbUpdTipoSrc.getItems().addAll(EGeoSrcCoord.values());

    Path pth = m_model.getSrcDir();
    if (pth != null) {
      txFileSorg.setText(pth.toString());
      cbTipoFileSrc.getSelectionModel().select(m_model.getTipoSource());
    }
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
    pth = m_model.getDbName();
    if (pth != null) {
      txDBFile.setText(pth.toString());
      cbTipoDb.getSelectionModel().select(m_model.getTipoDB());
      cbTipoDBSrcSel(null);
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

    initializeTable(props);
    creaUpdControls();
    creaContextMenu();
    mainstage.setOnCloseRequest(e -> exitApplication(e));
    mainstage.setTitle(Versione.getVersionEx());
    leggiProperties(mainstage);
    preparaLogPanel(props);
    preparaUpdPanel(props, mainstage);
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
            imagePopupWindowShow(row);
          }
        }

      });
      return row;
    });

    tblvRecDB.getSelectionModel().selectedItemProperty().addListener((obs, pold, pnew) -> {
      if (pnew != null) {
        try {
          updAddModificaDati((GeoCoord) pnew.clone(), true);
        } catch (CloneNotSupportedException e) {
          e.printStackTrace();
        }
        updButtonsGest();
        boolean bvDis = !pnew.hasLonLat();
        mnuCtxVaiCoord.setDisable(bvDis);
        mnuCtxLatMin.setDisable(bvDis);
        mnuCtxLatMax.setDisable(bvDis);
        mnuCtxLonMin.setDisable(bvDis);
        mnuCtxLonMax.setDisable(bvDis);
        mnuCtxGessLoc.setDisable( !bvDis);
      }
    });

    colDatetime.setCellValueFactory(new PropertyValueFactory<GeoCoord, LocalDateTime>(COL01_DATETIME));
    colLatitude.setCellValueFactory(new PropertyValueFactory<GeoCoord, Double>(COL02_LATITUDE));
    colLongitude.setCellValueFactory(new PropertyValueFactory<GeoCoord, Double>(COL03_LONGITUDE));
    colAlitude.setCellValueFactory(new PropertyValueFactory<GeoCoord, Double>(COL05_ALTITUDE));
    colSource.setCellValueFactory(new PropertyValueFactory<GeoCoord, EGeoSrcCoord>(COL04_SOURCE));
    colFotofile.setCellValueFactory(new PropertyValueFactory<GeoCoord, String>(COL05_FOTOFILE));
    colDatetime.setCellFactory(column -> {
      return new MioTableCellRenderDate<GeoCoord, LocalDateTime>(COL01_DATETIME);
    });
    colLatitude.setCellFactory(column -> {
      return new MioTableCellRenderCoord<GeoCoord, Double>(COL02_LATITUDE);
    });
    colLongitude.setCellFactory(column -> {
      return new MioTableCellRenderCoord<GeoCoord, Double>(COL03_LONGITUDE);
    });
    colAlitude.setCellFactory(column -> {
      return new MioTableCellRenderDist<GeoCoord, Double>(COL05_ALTITUDE);
    });

    for (TableColumn<GeoCoord, ?> c : tblvRecDB.getColumns()) {
      readColumnWidth(p_props, c);
    }

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

    if (dimX * dimY > 0) {
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
    m_model.setInvalidSrc(true);
    btCercaSourceClick(null);
    if ( !m_model.isInvalidSrc())
      btApriSourceClick(null);
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
    // System.out.println("MainApp2FxmlController.mnuVaiCoord():" + p_ev);
    GeoCoord fil = tblvRecDB.getSelectionModel().getSelectedItem();
    guessPosition(fil);
    tblvRecDB.refresh();
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
    GeoList li = m_model.getGeoList();
    li //
        .stream() //
        .filter( //
            geo -> geo.hasFotoFile())
        .forEach(geo -> m_model.renameFotoFile(geo));
    tblvRecDB.refresh();
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
    FiltroGeoCoord filtro = m_model.getFiltro();
    GeoCoord coo = tblvRecDB.getSelectionModel().getSelectedItem();
    LocalDateTime dt = coo.getTstamp();
    txFltrDtMin.setText(ParseData.s_fmtDtExif.format(dt));
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

  @SuppressWarnings("unused")
  private void msgBox(String p_txt) {
    msgBox(p_txt, AlertType.INFORMATION);
  }

  private boolean msgBox(String p_txt, AlertType tipo) {
    boolean bRet = true;
    Alert alt = new Alert(tipo);
    Scene sce = MainAppGpsInfo.getInst().getPrimaryStage().getScene();
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
        // System.out.println("stile=" + cssSty);
        setStyle(cssSty);
      }
    });

    // colTime.setMaxWidth(60.);
    colTime.setCellValueFactory(new PropertyValueFactory<>("time"));
    // colLev.setMaxWidth(48.0);
    colLev.setCellValueFactory(new PropertyValueFactory<>("level"));
    colMsg.setCellValueFactory(new PropertyValueFactory<>("message"));
    cbLevelMin.getItems().addAll(Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL);
    cbLevelMin.getSelectionModel().select(levelMin);
    // leggo le properties colonne dei log
    for (TableColumn<Log4jRow, ?> c : tblvLogs.getColumns()) {
      readColumnWidth(props, c);
    }
    // -------- combo level -------
    levelMin = Level.INFO;
    if (props != null) {
      String sz = props.getProperty(CSZ_LOG_LEVEL);
      if (sz != null)
        levelMin = Level.toLevel(sz);
    }
    cbLevelMin.getSelectionModel().select(levelMin);
  }

  private void preparaUpdPanel(AppProperties p_props, Stage p_mainstage) {

  }

  private void readColumnWidth(AppProperties p_prop, TableColumn<?, ?> p_col) {
    String sz = String.format(CSZ_PROP_COL, p_col.getId());
    double w = p_prop.getDoubleProperty(sz);
    if (w > 0)
      p_col.setPrefWidth(w);
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
    String sz = String.format(CSZ_PROP_COL, p_col.getId());
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
    // System.out.printf("RegJpsInfoController.txDtMaxLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
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
    // System.out.printf("RegJpsInfoController.txDtMinLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
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
    // System.out.printf("RegJpsInfoController.txLatMaxLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
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
    // System.out.printf("RegJpsInfoController.txLatMinLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
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
    // System.out.printf("RegJpsInfoController.txLonMaxLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
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
    // System.out.printf("RegJpsInfoController.txLonMinLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
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
        m_updGeo.setTstamp(m_updGeoFmt.parseTStamp(txUpdDatetime.getText()));
        updAddModificaDati(m_updGeo, false);
        updButtonsGest();
      }
    } catch (Exception e) {
      //
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
        btUpdParseWEB(null);
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
    // System.out.printf("RegJpsInfoController.txGPXFileLostFocus(oldv=%s, newv=%s)\n", p_oldv, p_newv);
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

  public void imagePopupWindowShow(TableRow<GeoCoord> row) {
    GeoCoord geo = row.getItem();
    Stage stage = new Stage();
    Stage primaryStage = MainAppGpsInfo.getInst().getPrimaryStage();
    //    stage.setWidth(800);
    //    stage.setHeight(600);
    //    File imageFile = rowData.getPath().toFile();
    //    Image image = new Image(imageFile.toURI().toString());
    //    ImageView imageView = new ImageView(image);
    //    ImageViewResizer imgResiz = new ImageViewResizer(imageView);

    ImageViewResizer imgResiz = caricaImg(geo);
    Image img = imgResiz.getImageView().getImage();
    double dblWi = img.getWidth();
    double dblHe = img.getHeight();
    final double STAGE_WIDTH = 1000.;
    double prop = dblWi / dblHe;
    int stageWith = (int) STAGE_WIDTH;
    int stageHeight = (int) (STAGE_WIDTH / prop);

    stage.setWidth(stageWith);
    stage.setHeight(stageHeight);

    //    StackPane root = new StackPane();
    //    root.getChildren().addAll(imgResiz);

    VBox vbox = new VBox();
    vbox.getChildren().addAll(imgResiz);
    // VBox.setVgrow(root, Priority.ALWAYS);
    Scene scene = new Scene(vbox);
    scene.setOnKeyReleased(new EventHandler<KeyEvent>() {

      @Override
      public void handle(KeyEvent event) {
        switch (event.getCode()) {
          case RIGHT:
          case LEFT:
            doRightLeft(row, event.getCode(), scene);
            break;
          default:
            break;
        }

      }
    });

    stage.setScene(scene);
    stage.setTitle(geo.getFotoFile().toString());
    stage.initModality(Modality.NONE);
    stage.initOwner(primaryStage);

    stage.show();
  }

  private ImageViewResizer caricaImg(GeoCoord p_fi) {
    File imageFile = p_fi.getFotoFile().toFile();
    Image image = new Image(imageFile.toURI().toString());
    ImageView imageView = new ImageView(image);
    ImageViewResizer imgResiz = new ImageViewResizer(imageView);
    return imgResiz;
  }

  protected void doRightLeft(TableRow<GeoCoord> row, KeyCode code, Scene scene) {
    boolean bOk = false;
    int qta = tblvRecDB.getItems().size();
    int indx = tblvRecDB.getSelectionModel().getSelectedIndex();
    GeoCoord geo = tblvRecDB.getSelectionModel().getSelectedItem();
    boolean bLoop = true;
    switch (code) {
      case LEFT:
        while (bLoop) {
          tblvRecDB.getFocusModel().focusPrevious();
          indx--;
          if (indx < 0) {
            bLoop = false;
            break;
          }
          tblvRecDB.getSelectionModel().select(indx);
          geo = tblvRecDB.getSelectionModel().getSelectedItem();
          if (geo != null && //
              geo.getSrcGeo() == EGeoSrcCoord.foto && //
              geo.getFotoFile() != null) {
            bLoop = false;
            bOk = true;
          }

        }
        break;
      case RIGHT:
        while (bLoop) {
          tblvRecDB.getFocusModel().focusNext();
          indx++;
          if (indx >= qta) {
            bLoop = false;
            break;
          }
          tblvRecDB.getSelectionModel().select(indx);
          geo = tblvRecDB.getSelectionModel().getSelectedItem();
          if (geo != null && //
              geo.getSrcGeo() == EGeoSrcCoord.foto && //
              geo.getFotoFile() != null) {
            bLoop = false;
            bOk = true;
          }
        }
        break;
      default:
        break;
    }
    if ( !bOk)
      return;
    tblvRecDB.getSelectionModel().select(indx);
    GeoCoord fi = tblvRecDB.getSelectionModel().getSelectedItem();
    // System.out.printf("MainApp2FxmlController.doRightLeft(%s)\n", fi.getAttuale());
    Platform.runLater(() -> {
      tblvRecDB.requestFocus();
      int i = tblvRecDB.getSelectionModel().getSelectedIndex();
      tblvRecDB.getSelectionModel().select(i);
      i = i > 1 ? i - 2 : i;
      tblvRecDB.scrollTo(i);
    });

    ImageViewResizer imgResiz = caricaImg(fi);

    VBox vbox = new VBox();
    vbox.getChildren().addAll(imgResiz);
    scene.setRoot(vbox);
  }

}

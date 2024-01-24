package sm.clagenna.dbgps.javafx;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import sm.clagenna.dbgps.sys.Versione;
import sm.clagenna.stdcla.enums.EServerId;
import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ILog4jReader;
import sm.clagenna.stdcla.utils.Log4jRow;
import sm.clagenna.stdcla.utils.MioAppender;
import sm.clagenna.stdcla.utils.Utils;

public class RegJpsInfoController implements Initializable, ILog4jReader {
  private static final Logger s_log           = LogManager.getLogger(RegJpsInfoController.class);
  public static final String  CSZ_FXMLNAME    = "RegGpsInfo.fxml";
  private static final String CSZ_PROP_COL    = "tbview.col.%s";
  private static final String COL01_DATETIME  = "tstamp";
  private static final String COL02_LATITUDE  = "latitude";
  private static final String COL03_LONGITUDE = "longitude";
  private static final String COL04_SOURCE    = "srcGeo";
  private static final String COL05_FOTOFILE  = "fotoFile";

  @FXML
  private TextField              txFileSorg;
  @FXML
  private Button                 btCercaFileSrc;
  @FXML
  private ComboBox<EGeoSrcCoord> cbTipoFileSrc;
  @FXML
  private Button                 btApriFileStc;
  @FXML
  private CheckBox               ckShowGMS;

  @FXML
  private TextField           txFileDB;
  @FXML
  private Button              btCercaFileDB;
  @FXML
  private ComboBox<EServerId> cbTipoDb;
  @FXML
  private Button              btApriDbFile;
  @FXML
  private Button              btSalvaDb;

  @FXML
  private TextField txGPXFile;
  @FXML
  private Button    btCercaGPXFile;
  @FXML
  private Button    btSaveToGPX;

  @FXML
  private TextField              txFltrDtIniz;
  @FXML
  private TextField              txFltrDtFine;
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
  private TableColumn<GeoCoord, EGeoSrcCoord>  colSource;
  @FXML
  private TableColumn<GeoCoord, String>        colFotofile;

  @FXML
  private TextField              txUpdDatetime;
  @FXML
  private TextField              txUpdLongitude;
  @FXML
  private TextField              txUpdLatitude;
  @FXML
  private ComboBox<EGeoSrcCoord> cbUpdTipoSrc;
  @FXML
  private Button                 btUpdModif;
  @FXML
  private Button                 btUpdInsert;
  @FXML
  private Button                 btUpdDelete;

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
  private ComboBox<Level>               cbLevelMin;
  @FXML
  private static final String           CSZ_LOG_LEVEL = "logLevel";
  private static final String           CSZ_SPLITPOS  = "splitpos";
  @SuppressWarnings("unused")
  private Label                         lblLogs;
  private Level                         levelMin;
  private List<Log4jRow>                m_liMsgs;
  private DataModelGpsInfo              m_model;

  public RegJpsInfoController() {
    //
  }

  @Override
  public void initialize(URL p_location, ResourceBundle p_resources) {
    AppProperties props = AppProperties.getInstance();
    Stage mainstage = MainAppGpsInfo.getInst().getPrimaryStage();
    s_log.debug("Start Application {}", getClass().getSimpleName());
    m_model = new DataModelGpsInfo();
    m_model.readProperties(props);
    cbFltrTipoSrc.getItems().addAll(EGeoSrcCoord.values());
    cbTipoFileSrc.getItems().addAll(EGeoSrcCoord.values());
    cbUpdTipoSrc.getItems().addAll(EGeoSrcCoord.values());
    cbTipoDb.getItems().addAll(EServerId.values());

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

    pth = m_model.getDestDB();
    if (pth != null) {
      txFileDB.setText(pth.toString());
      cbTipoDb.getSelectionModel().select(m_model.getTipoDB());
    }

    txGPXFile.focusedProperty().addListener((obs, oldv, newv) -> txGPXFileLostFocus(obs, oldv, newv));

    initializeTable();
    mainstage.setOnCloseRequest(e -> exitApplication(e));
    mainstage.setTitle(Versione.getVersionEx());
    leggiProperties(mainstage);
    preparaLogPanel(props);

  }

  public void exitApplication(WindowEvent e) {
    if (m_model == null)
      return;
    AppProperties props = AppProperties.getInstance();
    m_model.saveProperties(props);
    saveColumnWidth(props);
    double[] pos = spltPane.getDividerPositions();
    props.setDoubleProperty(CSZ_SPLITPOS, pos[0]);
    Level liv = cbLevelMin.getSelectionModel().getSelectedItem();
    props.setProperty(CSZ_LOG_LEVEL, liv.toString());
    Platform.exit();
  }

  private void initializeTable() {
    // attuale.setText("Attuale");
    tblvRecDB.setRowFactory(tv -> {
      TableRow<GeoCoord> row = new TableRow<>();
      row.setOnMouseClicked(event -> {
        if (row.isEmpty())
          return;
        if (event.getClickCount() == 1 && event.isControlDown()) {
          // System.out.println("Click + Ctrl !");
          mnuVaiCoord((ActionEvent) null);
        }

      });
      return row;
    });

    colDatetime.setCellValueFactory(new PropertyValueFactory<GeoCoord, LocalDateTime>(COL01_DATETIME));
    colLatitude.setCellValueFactory(new PropertyValueFactory<GeoCoord, Double>(COL02_LATITUDE));
    colLongitude.setCellValueFactory(new PropertyValueFactory<GeoCoord, Double>(COL03_LONGITUDE));
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

  private void setSplitPos() {
    AppProperties props = AppProperties.getInstance();
    Double dblPos = props.getDoubleProperty(CSZ_SPLITPOS);
    if (dblPos != null) {
      spltPane.setDividerPositions(dblPos);
    }
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

  private void readColumnWidth(AppProperties p_prop, TableColumn<?, ?> p_col) {
    String sz = String.format(CSZ_PROP_COL, p_col.getId());
    double w = p_prop.getDoubleProperty(sz);
    if (w > 0)
      p_col.setPrefWidth(w);
  }

  private void saveColumnWidth(AppProperties p_prop, TableColumn<?, ?> p_col) {
    String sz = String.format(CSZ_PROP_COL, p_col.getId());
    p_prop.setDoubleProperty(sz, p_col.getWidth());
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
      cbFltrTipoSrc.getSelectionModel().select(m_model.getFltrTipoSource());
      s_log.info("Hai scelto src dir {}", fileScelto.getAbsolutePath());
    } else {
      s_log.debug("Non hai scelto nulla !!");
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

  @FXML
  public void btApriSourceClick(ActionEvent event) {
    m_model.parseSource();
    caricaLaGrigliaGeo();
  }

  private void caricaLaGrigliaGeo() {
    List<GeoCoord> li = m_model.getGeoList();
    ObservableList<GeoCoord> itms = tblvRecDB.getItems();
    itms.clear();
    if (li == null)
      return;
    for (GeoCoord geo : li) {
      itms.add(geo);
    }

    ContextMenu cntxMenu = new ContextMenu();
    MenuItem itm = new MenuItem("Vai Coord.");
    itm.setOnAction((ActionEvent ev) -> {
      mnuVaiCoord(ev);
    });
    cntxMenu.getItems().add(itm);
    tblvRecDB.setContextMenu(cntxMenu);
  }

  private void mnuVaiCoord(ActionEvent p_ev) {
    // System.out.println("MainApp2FxmlController.mnuVaiCoord():" + p_ev);
    GeoCoord fil = tblvRecDB.getSelectionModel().getSelectedItem();
    final String LNK_MAPS = "https://www.google.com/maps?z=15&t=h&q=%.8f,%.8f";
    if (fil != null) {
      double lat = fil.getLatitude();
      double lon = fil.getLongitude();
      if (lat * lon != 0) {
        String lnk = String.format(Locale.US, LNK_MAPS, lat, lon);
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
  public void btCercaDBClick(ActionEvent event) {
    Stage stage = MainAppGpsInfo.getInst().getPrimaryStage();
    FileChooser fil = new FileChooser();
    fil.setTitle("Cerca il DataBase dei dati");
    Path pth = m_model.getDestDB();
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
        txFileDB.setText(fileScelto.getAbsolutePath());
        cbTipoDb.getSelectionModel().select(m_model.getTipoDB());
        s_log.info("Hai scelto src dir {}", fileScelto.getAbsolutePath());
      } else {
        msgBox("Per un Data Base Devi scegliere un File", AlertType.WARNING);
      }
    } else {
      s_log.debug("Non hai scelto nulla !!");
    }
  }

  @FXML
  public void btApriDBClick(ActionEvent event) {
    m_model.leggiDB();
    caricaLaGrigliaGeo();
  }

  @FXML
  public void btSalvaDBClick(ActionEvent event) {
    m_model.salvaDB();
    caricaLaGrigliaGeo();
  }

  @FXML
  private void cbTipoDBSrcSel(ActionEvent event) {
    EServerId tp = cbTipoDb.getSelectionModel().getSelectedItem();
    m_model.setTipoDB(tp);
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

    File fileScelto = fil.showOpenDialog(stage);
    if (fileScelto != null) {
      pth = fileScelto.toPath();
      if (Files.isRegularFile(pth)) {
        m_model.setDestGPXfile(pth);
        txGPXFile.setText(fileScelto.getAbsolutePath());
        s_log.info("Hai scelto il file GPX {}", fileScelto.getAbsolutePath());
      } else {
        msgBox("Per salvare i GPX devi scegliere un File", AlertType.WARNING);
      }
    } else {
      s_log.debug("Non hai scelto nulla !!");
    }
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

  @FXML
  public void btSaveToGPXClick(ActionEvent event) {
    m_model.saveToGPX();
  }

  @SuppressWarnings("unused")
  private void msgBox(String p_txt) {
    msgBox(p_txt, AlertType.INFORMATION);
  }

  private void msgBox(String p_txt, AlertType tipo) {
    Alert alt = new Alert(tipo);
    Scene sce = MainAppGpsInfo.getInst().getPrimaryStage().getScene();
    Window wnd = null;
    if (sce != null)
      wnd = sce.getWindow();
    if (wnd != null) {
      alt.initOwner(wnd);
      alt.setTitle(tipo.toString());
      alt.setContentText(p_txt);
      alt.showAndWait();
    } else
      s_log.error("Windows==null; msg={}", p_txt);
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

  @FXML
  void btClearMsgClick(ActionEvent event) {
    // System.out.println("ReadFattHTMLController.btClearMsgClick()");
    tblvLogs.getItems().clear();
    if (m_liMsgs != null)
      m_liMsgs.clear();
    m_liMsgs = null;
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

}

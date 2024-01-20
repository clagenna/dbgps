package sm.clagenna.dbgps.javafx;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.StandardLevel;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.WindowEvent;
import sm.clagenna.stdcla.enums.EServerId;
import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.utils.AppProperties;
import sm.clagenna.stdcla.utils.ILog4jReader;
import sm.clagenna.stdcla.utils.Log4jRow;
import sm.clagenna.stdcla.utils.MioAppender;

public class RegJpsInfoController implements Initializable, ILog4jReader {
  private static final Logger s_log        = LogManager.getLogger(RegJpsInfoController.class);
  public static final String  CSZ_FXMLNAME = "RegGpsInfo.fxml";
  private static final String CSZ_PROP_COL = "tbview.col.%s";

  @FXML
  private TextField              txFileSorg;
  @FXML
  private Button                 btCercaFileSrc;
  @FXML
  private ComboBox<EGeoSrcCoord> cbTipoFileSrc;
  @FXML
  private Button                 btApriFileStc;

  @FXML
  private TextField           txFileDB;
  @FXML
  private Button              btCercaFileDB;
  @FXML
  private ComboBox<EServerId> cbTipoDb;
  @FXML
  private Button              btApriDbFile;

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
    s_log.debug("Start Application {}", getClass().getSimpleName());
    m_model = new DataModelGpsInfo();
    m_model.readProperties(props);
    preparaLogPanel(props);
    cbFltrTipoSrc.getItems().addAll(EGeoSrcCoord.values());
    cbTipoFileSrc.getItems().addAll(EGeoSrcCoord.values());
    cbUpdTipoSrc.getItems().addAll(EGeoSrcCoord.values());
    cbTipoDb.getItems().addAll(EServerId.values());

  }

  public void exitApplication(WindowEvent e) {
    if (m_model == null)
      return;
    AppProperties props = AppProperties.getInstance();
    m_model.saveProperties(props);
    saveColumnWidth(props);
    double[] pos = spltPane.getDividerPositions();
    // String szPos = String.format("%.4f", pos[0]).replace(",", ".");
    props.setDoubleProperty(CSZ_SPLITPOS, pos[0]);
    Platform.exit();
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
    if (props != null) {
      String sz = props.getProperty(CSZ_LOG_LEVEL);
      if (sz != null)
        levelMin = Level.toLevel(sz);
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

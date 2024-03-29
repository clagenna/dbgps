package sm.clagenna.dbgps.javafx;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import sm.clagenna.dbgps.sys.FotoViewerProducer;
import sm.clagenna.stdcla.geo.EGeoSrcCoord;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.javafx.ImageViewResizer;

public class GpsFotoViewer extends Stage //
    implements PropertyChangeListener {

  private static final Logger s_log = LogManager.getLogger(GpsFotoViewer.class);

  private FotoViewerProducer  prod;
  private Stage               primaryStage;
  private Scene               m_scene;
  private TableView<GeoCoord> tblvRecDB;

  public GpsFotoViewer(FotoViewerProducer p_prod) {
    prod = p_prod;
    primaryStage = MainAppGpsInfo.getInst().getPrimaryStage();
    s_log.debug("Istanzio un GpsFotoViewer()");
  }

  public void showImage(TableRow<GeoCoord> row) {
    GeoCoord p_geo = row.getItem();
    ImageViewResizer imgResiz = caricaImgDaFile(p_geo);
    Image img = imgResiz.getImageView().getImage();
    double dblWi = img.getWidth();
    double dblHe = img.getHeight();
    final double STAGE_WIDTH = 1000.;
    double prop = dblWi / dblHe;
    int stageWith = (int) STAGE_WIDTH;
    int stageHeight = (int) (STAGE_WIDTH / prop);

    setWidth(stageWith);
    setHeight(stageHeight);

    VBox vbox = new VBox();
    vbox.getChildren().addAll(imgResiz);
    tblvRecDB = row.getTableView();
    m_scene = new Scene(vbox);
    m_scene.setOnKeyReleased(new EventHandler<KeyEvent>() {

      @Override
      public void handle(KeyEvent event) {
        // System.out.printf("DBGpsInfoController.imagePopupWindowShow(%s)\n", event.toString());
        switch (event.getCode()) {
          case RIGHT:
          case LEFT:
          case UP:
          case DOWN:
            doRightLeft(row, event.getCode(), m_scene);
            break;
          case ESCAPE:
            windowClosing(null);
            close();
            break;
          default:
            break;
        }

      }
    });
    // m_scene.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
    this.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::windowClosing);
    InputStream icost = getClass().getResourceAsStream("/fotografia.png");
    getIcons().add(new Image(icost));
    setTitle(p_geo.getFotoFile().toString());
    initModality(Modality.NONE);
    initOwner(primaryStage);
    setScene(m_scene);
    
    show();
  }

  private ImageViewResizer caricaImgDaFile(GeoCoord p_fi) {
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
      case UP:
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
      case DOWN:
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

    Platform.runLater(() -> {
      tblvRecDB.requestFocus();
      int i = tblvRecDB.getSelectionModel().getSelectedIndex();
      tblvRecDB.getSelectionModel().select(i);
      i = i > 1 ? i - 2 : i;
      tblvRecDB.scrollTo(i);
    });

    ImageViewResizer imgResiz = caricaImgDaFile(fi);

    VBox vbox = new VBox();
    vbox.getChildren().addAll(imgResiz);
    scene.setRoot(vbox);
  }

  @Override
  public void propertyChange(PropertyChangeEvent p_evt) {
    // System.out.println("GpsFotoViewer.propertyChange():"+p_evt.toString());
    @SuppressWarnings("unchecked")
    TableRow<GeoCoord> row = (TableRow<GeoCoord>) p_evt.getNewValue();
    GeoCoord geo = row.getItem();
    ImageViewResizer imgResiz = caricaImgDaFile(geo);
    VBox vbox = new VBox();
    vbox.getChildren().addAll(imgResiz);
    m_scene.setRoot(vbox);
  }

  private WindowEvent windowClosing(WindowEvent t1) {
    // System.out.println("GpsFotoViewer.windowClosing()");
    prod.closeWindow(this);
    return t1;
  }
}

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

  private static final Logger s_log          = LogManager.getLogger(GpsFotoViewer.class);
  private static final String IMAGE_FOTO_ICO = "fotografia2.png";
  private static final double STAGE_WIDTH    = 1000.;

  @SuppressWarnings("unused")
  private FotoViewerProducer  prod;
  private Stage               primaryStage;
  private Scene               m_scene;
  private TableView<GeoCoord> tblvRecDB;

  private double           m_wiZoom;
  private double           m_wi;
  private double           m_he;
  private File             m_imageFile;
  private Image            m_image;
  private ImageView        m_imageView;
  private ImageViewResizer m_imgResiz;
  private VBox             m_vbox;

  public GpsFotoViewer(FotoViewerProducer p_prod) {
    prod = p_prod;
    primaryStage = MainAppGpsInfo.getInst().getPrimaryStage();
    m_scene = null;
    s_log.debug("Istanzio un GpsFotoViewer()");
  }

  public void showImage(TableRow<GeoCoord> row) {
    GeoCoord p_geo = row.getItem();
    tblvRecDB = row.getTableView();

    mostraImmagine(p_geo);

    impostaIco();
    setTitle(p_geo.getFotoFile().toString());
    initModality(Modality.NONE);
    initOwner(primaryStage);
    setScene(m_scene);

    show();
  }

  private void mostraImmagine(GeoCoord p_geo) {
    ImageViewResizer imgResiz = caricaImgDaFile(p_geo);
    double prop = m_wi / m_he;
    int stageWith = (int) STAGE_WIDTH;
    int stageHeight = (int) (STAGE_WIDTH / prop);

    setWidth(stageWith);
    setHeight(stageHeight);

    VBox vbox = new VBox();
    vbox.getChildren().addAll(imgResiz);
    if (null == m_scene)
      m_scene = new Scene(vbox);
    else
      m_scene.setRoot(vbox);

    m_scene.setOnKeyReleased(new EventHandler<KeyEvent>() {

      @Override
      public void handle(KeyEvent event) {
        // System.out.printf("DBGpsInfoController.imagePopupWindowShow(%s)\n", event.toString());
        switch (event.getCode()) {
          case RIGHT:
          case LEFT:
          case UP:
          case DOWN:
            doRightLeft(event.getCode());
            break;
          case ESCAPE:
            windowClosing(null);
            close();
            break;
          default:
            // System.out.println("GpsFotoViewer.mostraImmagine:" + event.toString());
            String cc = event.getText();
            if ( !event.isShiftDown() || cc == null || cc.length() == 0)
              break;
            switch (cc) {
              case "+":
              case "-":
                doZoom(cc, event);
                break;
              default:
                break;
            }
            break;
        }

      }
    });
    // m_scene.getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::closeWindowEvent);
    this.addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, this::windowClosing);
  }

  protected void doZoom(String p_cc, KeyEvent p_event) {
    // FIXME questo zoom non funziona
    System.out.printf("GpsFotoViewer.doZoom(\"%s\")\n", p_cc);
    switch (p_cc) {
      case "+":
        if (m_wiZoom < 4_000)
          m_wiZoom += 50;
        break;
      case "-":
        if (m_wiZoom > 100)
          m_wiZoom -= 50;
        break;
      default:
        return;
    }
    m_imageView.setImage(m_image);
    m_imageView.setFitWidth(m_wiZoom);
    m_imgResiz = new ImageViewResizer(m_imageView);
    m_vbox = new VBox();
    m_vbox.getChildren().addAll(m_imgResiz);
    m_scene.setRoot(m_vbox);
  }

  private ImageViewResizer caricaImgDaFile(GeoCoord p_fi) {
    if ( !p_fi.hasFotoFile())
      return null;
    m_imageFile = p_fi.getFotoFile().toFile();
    m_image = new Image(m_imageFile.toURI().toString());
    m_wi = m_image.getWidth();
    m_he = m_image.getHeight();
    m_wiZoom = m_wi;

    m_imageView = new ImageView(m_image);
    m_imageView.setPreserveRatio(true);
    m_imageView.setSmooth(true);
    m_imageView.setCache(true);

    ImageViewResizer imgResiz = new ImageViewResizer(m_imageView);
    return imgResiz;
  }

  private void impostaIco() {
    InputStream stre = getClass().getResourceAsStream(IMAGE_FOTO_ICO);
    if (stre == null)
      stre = getClass().getClassLoader().getResourceAsStream(IMAGE_FOTO_ICO);
    if (stre != null) {
      Image ico = new Image(stre);
      getIcons().add(ico);
    } else
      s_log.debug("Non trovo l'ICO {}", IMAGE_FOTO_ICO);
  }

  protected void doRightLeft(KeyCode code) {
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

    m_imgResiz = caricaImgDaFile(fi);

    m_vbox = new VBox();
    m_vbox.getChildren().addAll(m_imgResiz);
    setTitle(fi.getFotoFile().toString());
    m_scene.setRoot(m_vbox);
  }

  @Override
  public void propertyChange(PropertyChangeEvent p_evt) {
    // System.out.println("GpsFotoViewer.propertyChange():" + p_evt.toString());
    @SuppressWarnings("unchecked")
    TableRow<GeoCoord> row = (TableRow<GeoCoord>) p_evt.getNewValue();
    GeoCoord geo = row.getItem();
    if ( !geo.hasFotoFile())
      return;
    ImageViewResizer imgResiz = caricaImgDaFile(geo);
    VBox vbox = new VBox();
    vbox.getChildren().addAll(imgResiz);
    m_scene.setRoot(vbox);
    double posY = getY();
    if (posY < 0)
      setY(6.);
    double prop = m_wi / m_he;
    int lwi = (int) STAGE_WIDTH;
    int lhe = (int) (STAGE_WIDTH / prop);
    // System.out.printf("GpsFotoViewer.doRightLeft(%.2f - %d, %d)\n", posY, lwi, lhe);
    setWidth(lwi);
    setHeight(lhe);
  }

  private WindowEvent windowClosing(WindowEvent t1) {
    // System.out.println("GpsFotoViewer.windowClosing()");
    // ??  prod.closeWindow(this);
    return t1;
  }
}

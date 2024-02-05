package sm.clagenna.dbgps.javafx;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sm.clagenna.stdcla.utils.AppProperties;

public class MainAppGpsInfo extends Application {

  private static final Logger s_log = LogManager.getLogger(MainAppGpsInfo.class);

  private static final String   CSZ_MAIN_APP_CSS = "/sm/clagenna/dbgps/javafx/styleMainApp.css";
  private static final String   CSZ_PROPERTIES   = "GpsInfo.properties";
  private static MainAppGpsInfo inst;
  private Stage                 primaryStage;

  public MainAppGpsInfo() {
    //
  }

  @Override
  public void start(Stage pStage) throws Exception {
    inst = this;
    String version = System.getProperty("java.version");
    s_log.debug("Start di {} with Java Version {}", getClass().getSimpleName(), version);
    this.primaryStage = pStage;

    AppProperties prop = new AppProperties();
    prop.leggiPropertyFile(new File(CSZ_PROPERTIES), false, false);
    URL url = getClass().getResource(RegJpsInfoController.CSZ_FXMLNAME);
    if (url == null) {
      System.err.printf("non trovo getClass().getResource(%s)\n", RegJpsInfoController.CSZ_FXMLNAME);
      url = getClass().getClassLoader().getResource(RegJpsInfoController.CSZ_FXMLNAME);
      //      url = getClass().getClassLoader().getResource(l_fxml);
      if (url == null) {
        System.err.printf("non trovo getClass().getClassLoader().getResource(%s)\n", RegJpsInfoController.CSZ_FXMLNAME);
      } else {
        System.out.printf("Trovato getClass().getClassLoader().getResource(%s)\n", RegJpsInfoController.CSZ_FXMLNAME);
      }
    } else {
      System.out.printf("Trovato getClass().getResource(%s)\n", RegJpsInfoController.CSZ_FXMLNAME);
    }
    Parent radice = null;
    try {
      radice = FXMLLoader.load(url);
    } catch (IOException e) {
      e.printStackTrace();
    }
    Scene scene = new Scene(radice, 900, 440);
    url = getClass().getResource(CSZ_MAIN_APP_CSS);
    if (url == null)
      url = getClass().getClassLoader().getResource(CSZ_MAIN_APP_CSS);
    scene.getStylesheets().add(url.toExternalForm());

    pStage.setScene(scene);
    pStage.show();
  }

  @Override
  public void stop() throws Exception {
    s_log.debug("MainApp stop");
    AppProperties prop = AppProperties.getInstance();

    prop.setProperty(AppProperties.CSZ_PROP_DIMFRAME_X, String.format("%.0f", primaryStage.getWidth()));
    prop.setProperty(AppProperties.CSZ_PROP_DIMFRAME_Y, String.format("%.0f", primaryStage.getHeight()));
    prop.setProperty(AppProperties.CSZ_PROP_POSFRAME_X, String.format("%.0f", primaryStage.getX()));
    prop.setProperty(AppProperties.CSZ_PROP_POSFRAME_Y, String.format("%.0f", primaryStage.getY()));
    
    prop.salvaSuProperties();

    super.stop();
  }

  public static void main(String[] args) {
    Application.launch(args);
  }

  public static MainAppGpsInfo getInst() {
    return inst;
  }

  public static void setInst(MainAppGpsInfo inst) {
    MainAppGpsInfo.inst = inst;
  }

  public Stage getPrimaryStage() {
    return primaryStage;
  }

  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  public void showLink(String p_lnk) {
    getHostServices().showDocument(p_lnk);

  }

}

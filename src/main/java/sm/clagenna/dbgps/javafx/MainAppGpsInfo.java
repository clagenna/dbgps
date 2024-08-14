package sm.clagenna.dbgps.javafx;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
  private ExecutorService       backGrService;

  public MainAppGpsInfo() {
    //
  }

  @Override
  public void start(Stage pStage) throws Exception {
    // questo perche il propertyHelper protesta...
    System.setProperty("javafx.sg.warn", "true");
    inst = this;
    String version = System.getProperty("java.version");
    s_log.debug("Start di {} with Java Version {}", getClass().getSimpleName(), version);
    this.primaryStage = pStage;

    AppProperties prop = new AppProperties();
    prop.leggiPropertyFile(new File(CSZ_PROPERTIES), false, false);
    URL url = getClass().getResource(DBGpsInfoController.CSZ_FXMLNAME);
    if (url == null) {
      System.err.printf("non trovo getClass().getResource(%s)\n", DBGpsInfoController.CSZ_FXMLNAME);
      url = getClass().getClassLoader().getResource(DBGpsInfoController.CSZ_FXMLNAME);
      //      url = getClass().getClassLoader().getResource(l_fxml);
      if (url == null) {
        System.err.printf("non trovo getClass().getClassLoader().getResource(%s)\n", DBGpsInfoController.CSZ_FXMLNAME);
        throw new FileNotFoundException(DBGpsInfoController.CSZ_FXMLNAME);
      } else {
        s_log.debug("Trovato getClass().getClassLoader().getResource({})", DBGpsInfoController.CSZ_FXMLNAME);
      }
    } else {
      s_log.debug("Trovato getClass().getResource({})", DBGpsInfoController.CSZ_FXMLNAME);
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
    if ( backGrService != null ) {
      backGrService.shutdown();
      backGrService.awaitTermination(5, TimeUnit.SECONDS);
      while ( !backGrService.isTerminated()) {
        //
      }
      backGrService = null;
      s_log.debug("Executor service SHUTDOWN");
    }
    AppProperties prop = AppProperties.getInstance();
    if (primaryStage.getWidth() > 0) {
      prop.setProperty(AppProperties.CSZ_PROP_DIMFRAME_X, String.format("%.0f", primaryStage.getWidth()));
      prop.setProperty(AppProperties.CSZ_PROP_DIMFRAME_Y, String.format("%.0f", primaryStage.getHeight()));
      prop.setProperty(AppProperties.CSZ_PROP_POSFRAME_X, String.format("%.0f", primaryStage.getX()));
      prop.setProperty(AppProperties.CSZ_PROP_POSFRAME_Y, String.format("%.0f", primaryStage.getY()));
    }
    prop.salvaSuProperties();

    super.stop();
  }
  
  public ExecutorService getBackGrService() {
    if ( backGrService == null)
      backGrService= Executors.newFixedThreadPool(1);
    return backGrService;
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

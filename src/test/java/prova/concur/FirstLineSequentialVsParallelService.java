package prova.concur;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javafx.application.Application;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class FirstLineSequentialVsParallelService extends Application {
  private static final String[] URLs = { //
      "http://www.google.com", //
      "http://www.yahoo.com", //
      "http://www.microsoft.com", //
      "http://www.oracle.com" };

  private ExecutorService sequentialFirstLineExecutor;
  private ExecutorService parallelFirstLineExecutor;

  @Override
  public void init() throws Exception {
    sequentialFirstLineExecutor = Executors.newFixedThreadPool(1, //
        new FirstLineThreadFactory("sequential"));

    parallelFirstLineExecutor = Executors.newFixedThreadPool(URLs.length, new FirstLineThreadFactory("parallel"));
  }

  @Override
  public void stop() throws Exception {
    parallelFirstLineExecutor.shutdown();
    parallelFirstLineExecutor.awaitTermination(3, TimeUnit.SECONDS);

    sequentialFirstLineExecutor.shutdown();
    sequentialFirstLineExecutor.awaitTermination(3, TimeUnit.SECONDS);
  }

  public static void main(String[] args) {
    Application.launch(args);
  }

  @Override
  public void start(Stage stage) throws Exception {
    final VBox messages = new VBox();
    messages.setStyle("-fx-background-color: cornsilk; -fx-padding: 10;");

    messages.getChildren().addAll( //
        new Label("Parallel Execution"), // 
        new Label("------------------"), //
        new Label());
    fetchFirstLines(messages, parallelFirstLineExecutor);

    messages.getChildren().addAll( //
        new Label("Sequential Execution"), // 
        new Label("--------------------"), //
        new Label());
    fetchFirstLines(messages, sequentialFirstLineExecutor);

    messages.setStyle("-fx-font-family: monospace");

    stage.setScene(new Scene(messages, 600, 800));
    stage.show();
  }

  private void fetchFirstLines(final VBox monitoredLabels, ExecutorService executorService) {
    for (final String url : URLs) {
      final FirstLineService service = new FirstLineService();
      service.setExecutor(executorService);
      service.setUrl(url);

      final ProgressMonitoredLabel monitoredLabel = new ProgressMonitoredLabel(url);
      monitoredLabels.getChildren().add(monitoredLabel);
      monitoredLabel.progress.progressProperty().bind(service.progressProperty());

      service.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
        @Override
        public void handle(WorkerStateEvent t) {
          monitoredLabel.addStrings(service.getMessage(), service.getValue());
        }
      });
      service.start();
    }
  }
}

package prova.concur;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ProgressMonitoredLabel extends HBox {
  final ProgressBar progress;
  final VBox        labels;

  public ProgressMonitoredLabel(String initialString) {
    super(20);

    progress = new ProgressBar();
    labels = new VBox();
    labels.getChildren().addAll(new Label(initialString), new Label());

    progress.setPrefWidth(100);
    progress.setMinWidth(Region.USE_PREF_SIZE);
    HBox.setHgrow(labels, Priority.ALWAYS);
    setMinHeight(80);

    getChildren().addAll(progress, labels);
  }

  public void addStrings(String... strings) {
    for (String string : strings) {
      labels.getChildren().add(labels.getChildren().size() - 1, new Label(string));
    }
  }
}

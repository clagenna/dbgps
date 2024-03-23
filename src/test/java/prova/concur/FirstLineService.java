package prova.concur;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public class FirstLineService extends Service<String> {
  private StringProperty url = new SimpleStringProperty(this, "url");

  public final void setUrl(String value) {
    url.set(value);
  }

  public final String getUrl() {
    return url.get();
  }

  public final StringProperty urlProperty() {
    return url;
  }

  @Override
  protected Task<String> createTask() {
    
    final String _url = getUrl();
    
    return new Task<String>() {
      @Override
      protected String call() throws Exception {
        updateMessage("Called on thread: " + Thread.currentThread().getName());
        URL u = new URI(_url).toURL();
        BufferedReader in = new BufferedReader(new InputStreamReader(u.openStream()));
        String result = in.readLine();
        in.close();

        // pause just so that it really takes some time to run the task
        // so that parallel execution behaviour can be observed.
        for (int i = 0; i < 100; i++) {
          updateProgress(i, 100);
          Thread.sleep(50);
        }

        return result;
      }
    };
  }
}

package prova.concur;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class FirstLineThreadFactory implements ThreadFactory {
  static final AtomicInteger poolNumber = new AtomicInteger(1);
  private final String       type;

  public FirstLineThreadFactory(String type) {
    this.type = type;
  }

  @Override
  public Thread newThread(Runnable runnable) {
    Thread thread = new Thread(runnable, "LineService-" + poolNumber.getAndIncrement() + "-thread-" + type);
    thread.setDaemon(true);

    return thread;
  }
}

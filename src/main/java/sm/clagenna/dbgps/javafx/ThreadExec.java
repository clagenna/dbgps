package sm.clagenna.dbgps.javafx;

import javafx.concurrent.Task;
import sm.clagenna.stdcla.utils.MioAppender;

/**
 * Classe che implementa il {@link Task} da consegnare al executor service (vedi
 * {@link MainAppGpsInfo#getBackGrService()} ) per essere eseguito in background.
 */
public class ThreadExec extends Task<String> {
  private DataModelGpsInfo m_mod;

  public ThreadExec(DataModelGpsInfo p_mod) {
    m_mod = p_mod;
  }

  @Override
  protected String call() throws Exception {
    m_mod.execute();
    return MioAppender.getInst().lastMsg();
  }

}

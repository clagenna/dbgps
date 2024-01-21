package sm.clagenna.dbgps.javafx;

import java.time.LocalDateTime;

import javafx.scene.control.TableCell;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoFormatter;

public class MioTableCellRenderDate<T, F> extends TableCell<GeoCoord, LocalDateTime> {

  @SuppressWarnings("unused")
  private String m_colName;

  public MioTableCellRenderDate(String p_colNam) {
    m_colName = p_colNam;
  }

  @Override
  protected void updateItem(LocalDateTime item, boolean empty) {
    super.updateItem(item, empty);
    if (item == null || empty) {
      setText(null);
      setStyle("");
      return;
    }
    if (getTableRow() == null)
      return;
    GeoCoord geo = getTableRow().getItem();
    // Format date.
    LocalDateTime dt = geo.getTstamp();
    String sz = GeoFormatter.s_fmtmY4MD_hms.format(dt);
    setText(sz);
    setStyle( "-fx-alignment: CENTER-RIGHT;");
  }

}

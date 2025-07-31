package sm.clagenna.dbgps.javafx;

import java.time.LocalDateTime;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoCoordFoto;
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
    TableRow<GeoCoord> row = getTableRow();
    GeoCoord geo = (GeoCoord) row.getItem();
    // Paint colo = null;
    LocalDateTime dtAss = null;
    if (geo instanceof GeoCoordFoto gcf)
      dtAss = gcf.getDtAssunta();
    StringBuilder sty = new StringBuilder();
    String sz = GeoFormatter.s_fmtmY4MD_hms.format(item);
    sty.append("-fx-alignment: CENTER-RIGHT;");
    if (null != dtAss) {
      int v = dtAss.compareTo(item);
      v = v < 0 ? -1 : v;
      v = v > 0 ? 1 : v;
      if (null != item && item.isBefore(LocalDateTime.MAX)) {
        switch (v) {
          case -1:
            // setTextFill(Color.BLACK);
            sty.append("-fx-background-color: darkturquoise; -fx-font-weight: bolder;");
            break;
          case 0:
            // setTextFill(Color.BLACK);
            // setStyle("");
            break;
          case 1:
            sty.append("-fx-background-color: lightcoral;");
            // setTextFill(Color.BLACK);
            break;
        }
      }
    }
    setText(sz);
    setStyle(sty.toString());
  }

}

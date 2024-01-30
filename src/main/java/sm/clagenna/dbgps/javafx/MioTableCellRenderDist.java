package sm.clagenna.dbgps.javafx;

import javafx.scene.control.TableCell;
import sm.clagenna.stdcla.geo.GeoCoord;

public class MioTableCellRenderDist<T, F> extends TableCell<GeoCoord, Double> {

  public MioTableCellRenderDist(String p_sz) {
    //
  }

  @Override
  protected void updateItem(Double p_item, boolean p_empty) {
    super.updateItem(p_item, p_empty);
    setText(null);
    setStyle("-fx-alignment: CENTER-RIGHT;");

    if (p_item == null || p_empty || getTableRow() == null || p_item == 0)
      return;
    if (getTableRow() == null)
      return;
    GeoCoord geo = getTableRow().getItem();
    double dbl = geo.getAltitude();
    if (dbl == 0)
      return;
    String sz = "";
    sz = String.format("%d", (int) dbl);
    setText(sz);
  }

}

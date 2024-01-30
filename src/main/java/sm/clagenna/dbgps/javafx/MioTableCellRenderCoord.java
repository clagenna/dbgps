package sm.clagenna.dbgps.javafx;

import javafx.scene.control.TableCell;
import sm.clagenna.stdcla.geo.GMS;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoFormatter;

public class MioTableCellRenderCoord<T, F> extends TableCell<GeoCoord, Double> {

  private String m_colName;

  public MioTableCellRenderCoord(String p_sz) {
    m_colName = p_sz;
  }

  @Override
  protected void updateItem(Double p_item, boolean p_empty) {
    super.updateItem(p_item, p_empty);
    setText(null);
    String sty = "-fx-alignment: CENTER-RIGHT;";
    setStyle( sty);
    DataModelGpsInfo model = DataModelGpsInfo.getInstance();
    boolean showGMS = model.isShowGMS();
    if (p_item == null || p_empty || getTableRow() == null || p_item == 0)
      return;
    if (getTableRow() == null)
      return;
    GeoCoord geo = getTableRow().getItem();
    if ( geo.isGuessed())
      sty += " -fx-text-fill: #EA4335; -fx-font-weight: bold;";
    double dbl = geo.getLatitude();
    int nLatLon = GeoFormatter.LATITUDE;
    if (m_colName.startsWith("lon")) {
      dbl = geo.getLongitude();
      nLatLon = GeoFormatter.LONGITUDINE;
    }
    String sz = "";
    if (showGMS) {
      GMS gms = GeoFormatter.convertWGS84(dbl, nLatLon);
      sz = gms.toString();
    } else {
      sz = String.format("%.10f", dbl);
    }
    setText(sz);
    setStyle(sty);
  }

}

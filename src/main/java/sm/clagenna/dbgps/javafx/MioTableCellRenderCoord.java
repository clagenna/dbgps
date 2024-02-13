package sm.clagenna.dbgps.javafx;

import java.text.DecimalFormat;

import javafx.scene.control.TableCell;
import sm.clagenna.stdcla.geo.GMS;
import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoFormatter;

public class MioTableCellRenderCoord<T, F> extends TableCell<GeoCoord, Double> {
  public static final String COLOR_GUESS     = "#EA4335";
  public static String       CSZ_GUESSED_CSS = String.format(" -fx-text-fill: %s; -fx-font-weight: bold;", COLOR_GUESS);
  public static final DecimalFormat s_fmt = new DecimalFormat("0.##########"); 
  private String             m_colName;

  public MioTableCellRenderCoord(String p_sz) {
    m_colName = p_sz;
  }

  @Override
  protected void updateItem(Double p_item, boolean p_empty) {
    super.updateItem(p_item, p_empty);
    setText(null);
    String sty = "-fx-alignment: CENTER-RIGHT;";
    setStyle(sty);
    DataModelGpsInfo model = DataModelGpsInfo.getInstance();
    boolean showGMS = model.isShowGMS();
    if (p_item == null || p_empty || getTableRow() == null || p_item == 0)
      return;
    if (getTableRow() == null)
      return;
    GeoCoord geo = getTableRow().getItem();
    if (geo.isGuessed())
      sty += CSZ_GUESSED_CSS;
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
      // sz = String.format("%.6f", dbl);
      sz = s_fmt.format(dbl);
    }
    setText(sz);
    setStyle(sty);
  }

}

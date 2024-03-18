package prova.listen;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import sm.clagenna.stdcla.geo.GeoCoord;

public class MouseMoveGeo implements PropertyChangeListener {
  private String nam;

  public MouseMoveGeo(String p_nam) {
    nam = p_nam;
  }

  @Override
  public void propertyChange(PropertyChangeEvent p_evt) {
    GeoCoord geo = (GeoCoord) p_evt.getNewValue();
    System.out.printf("Sono \"%s\" ricevo %s\n", nam, geo.toStringSimple());
  }

}

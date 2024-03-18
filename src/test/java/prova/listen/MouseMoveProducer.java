package prova.listen;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import sm.clagenna.stdcla.geo.GeoCoord;

public class MouseMoveProducer {
  private GeoCoord              geo;
  private PropertyChangeSupport support;

  public MouseMoveProducer() {
    support = new PropertyChangeSupport(this);
  }

  public void addPropertyChangeListener(PropertyChangeListener pcl) {
    support.addPropertyChangeListener(pcl);
  }

  public void removePropertyChangeListener(PropertyChangeListener pcl) {
    support.removePropertyChangeListener(pcl);
  }

  public void setGeo(GeoCoord p_geo) {
    support.firePropertyChange("newgeo", this.geo, p_geo);
    geo = p_geo;
  }
}

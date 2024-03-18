package prova.listen;

import java.time.LocalDateTime;

import org.junit.Test;

import sm.clagenna.stdcla.geo.GeoCoord;

public class ProvaListen {

  @Test
  public void provalo() {
    MouseMoveProducer prod = new MouseMoveProducer();
    MouseMoveGeo el1 = new MouseMoveGeo("Claudio");
    MouseMoveGeo el2 = new MouseMoveGeo("Eugy");

    prod.addPropertyChangeListener(el1);
    prod.addPropertyChangeListener(el2);

    GeoCoord geo = new GeoCoord(LocalDateTime.now(), 1.2, 2.1);
    prod.setGeo(geo);
    geo = new GeoCoord(LocalDateTime.now(), 7, 5, 9.3);
    prod.setGeo(geo);
    
    prod.removePropertyChangeListener(el1);
    
    geo = new GeoCoord(LocalDateTime.now(), 7, 5, 9.3);
    prod.setGeo(geo);

  }
}

package prova.dbgps;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;

import org.junit.Test;

import sm.clagenna.stdcla.geo.GeoCoord;
import sm.clagenna.stdcla.geo.GeoFormatter;

public class ProvaSublist {
  private static final int N_SecsBefore = 60 * 60 * 6;
  private static final int N_SecsAfter  = 60 * 60 * 7;

  private List<GeoCoord> li;
  private GeoCoord       mingeo;
  private GeoCoord       maxgeo;
  private LocalDateTime  m_trova;

  public ProvaSublist() {
    //
  }

  @Test
  public void provalo() {
    riempi();
    System.out.printf("%32s\t%d\n", "Qta elems", li.size());
    System.out.printf("%32s\t%s\n", "Min Geo", GeoFormatter.s_fmt2Y4MD_hms.format(mingeo.getTstamp()));
    System.out.printf("%32s\t%s\n", "Max Geo", GeoFormatter.s_fmt2Y4MD_hms.format(maxgeo.getTstamp()));
    System.out.printf("%32s\t%s\n", "Momento ricerca", GeoFormatter.s_fmt2Y4MD_hms.format(m_trova));
    //    System.out.printf("%32s %d", "Qta elems", li.size());
    //    System.out.printf("%32s %d", "Qta elems", li.size());
    GeoCoord nearest = findNearest(m_trova);
    System.out.printf("%32s\t%s\n", "il piu vicino:", GeoFormatter.s_fmt2Y4MD_hms.format(nearest.getTstamp()));

  }

  public GeoCoord findNearest(LocalDateTime trova) {
    GeoCoord retGeo = null;
    // ausilio per min distanza in tempo
    long minDistSecs = 99_999_999L;
    GeoCoord geoTrova = new GeoCoord();
    geoTrova.setTstamp(trova);
    // allargo il campo (in secondi) per beccare i geoCoord piu vicini
    boolean looppa = true;
    // i geoCoord piu vicini
    List<GeoCoord> filtered = null;
    // ampiezza dello spazio temporale da filtrare
    int delta = 5;
    int tries = 0;
    do {
      LocalDateTime loMom = trova.minusSeconds(delta);
      LocalDateTime hiMom = trova.plusSeconds(delta);
      filtered = li //
          .stream() //
          .filter( //
              s -> ( //
              s.getTstamp().isAfter(loMom) //
                  && s.getTstamp().isBefore(hiMom) //
              ) //
          ) //
          .toList();
      looppa = filtered == null || filtered.size() == 0;
      delta += 5;
      tries++;
    } while (looppa && tries < 10);
    if (filtered == null || filtered.size() == 0) {
      System.err.println("Non trovo punti vicini a:" + GeoFormatter.s_fmt2Y4MD_hms.format(trova));
      return retGeo;
    }
    System.out.println("------ List filtrati -------");
    filtered //
        .stream() //
        .forEach(s -> System.out.println(GeoFormatter.s_fmt2Y4MD_hms.format(s.getTstamp())));
    System.out.println("--------------------------------");
    for (GeoCoord geo : filtered) {
      if (retGeo == null) {
        retGeo = geo;
        minDistSecs = Math.abs(geo.distInSecs(geoTrova));
        // System.out.printf("ProvaSublist.findNearest(%s)\n", minDistSecs);
        continue;
      }
      long lDist = Math.abs(geo.distInSecs(geoTrova));
      if (lDist < minDistSecs) {
        retGeo = geo;
        minDistSecs = lDist;
        // System.out.printf("ProvaSublist.findNearest(%s)\n", minDistSecs);
      }
    }
    return retGeo;
  }

  private void riempi() {
    li = new ArrayList<>();
    LocalDateTime oggi = LocalDateTime.now();
    LocalDateTime prima = oggi.minusSeconds(N_SecsBefore);
    LocalDateTime dopo = oggi.plusSeconds(N_SecsAfter);
    int diffsec = (int) ChronoUnit.SECONDS.between(prima, dopo);
    Random rndGen = new Random();
    int rnd = new Random().nextInt(diffsec);
    m_trova = prima.plusSeconds(rnd);
    LocalDateTime dt = prima;
    while (dt.isBefore(dopo)) {
      GeoCoord geo = new GeoCoord();
      geo.setTstamp(dt);
      li.add(geo);
      dt = dt.plusSeconds(rndGen.nextInt(1, 20));
    }
    mingeo = li //
        .stream() //
        .min(Comparator.comparing(GeoCoord::getTstamp)) //
        .orElseThrow(NoSuchElementException::new);
    maxgeo = li //
        .stream() //
        .max(Comparator.comparing(GeoCoord::getTstamp)) //
        .orElseThrow(NoSuchElementException::new);
  }
}

package prova.dbgps;

import java.time.LocalDateTime;

import org.junit.Test;

import sm.clagenna.stdcla.geo.GeoCoord;

public class ProvaGeocoord {

  @Test
  public void doTheJob() {
    /*
     * SELECT dbo.distLatLon( 43.961895, 12.467523, 44.031294, 12.622114) / 1000
     * as Dist SELECT dbo.distLatLon( 43.962335, 12.466502, 44.031147,
     * 12.621909) / 1000 as Dist SELECT dbo.distLatLon( 43.962335, 12.466502,
     * 43.768440, 13.135647) / 1000 as Dist
     */
    GeoCoord coA = new GeoCoord(LocalDateTime.now(), 43.961895, 12.467523);
    GeoCoord coB = new GeoCoord(LocalDateTime.now(), 44.031294, 12.622114);
    System.out.printf("punto A %s\n", coA.toString());
    System.out.printf("punto B %s\n", coB.toString());
    System.out.printf("Dist A to B %.3f m\n", coA.distance(coB));
    System.out.println();

    //      double[] coord = { 43.961895, 12.467523, 44.031294, 12.622114 };
    //      double hav1 = app.calcDistance(coord[0], coord[1], coord[2], coord[3]);
    //      double vin1 = app.calcDistanceVincenty(coord[0], coord[1], coord[2], coord[3]);
    //      System.out.printf("dist1 = %.3f\tvin1 = %.3f\n", hav1 / 1000, vin1 / 1000);
    //
    //      coord = new double[] { 43.962335, 12.466502, 44.031147, 12.621909 };
    //      double hav2 = app.calcDistance(coord[0], coord[1], coord[2], coord[3]);
    //      double vin2 = app.calcDistanceVincenty(coord[0], coord[1], coord[2], coord[3]);
    //      System.out.printf("dist2 = %.3f\tvin2 = %.3f\n", hav2 / 1000, vin2 / 1000);
    //
    //      coord = new double[] { 43.962335, 12.466502, 43.768440, 13.135647 };
    //      double hav3 = app.calcDistance(coord[0], coord[1], coord[2], coord[3]);
    //      double vin3 = app.calcDistanceVincenty(coord[0], coord[1], coord[2], coord[3]);
    //      System.out.printf("dist3 = %.3f\tvin3 = %.3f\n", hav3 / 1000, vin3 / 1000);
    // google map = 57,91
  }

}

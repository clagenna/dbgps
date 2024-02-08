package prova.paste;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class ProvaPaste {

  private static final String[] ARR = { //
      "https://tools.wmflabs.org/geohack/geohack.php?language=it&pagename=Navarra&params=42.818_N_-1.644_E_type:adm1st_scale:5000000&title=Navarra", //
      "https://www.openstreetmap.org/#map=10/42.8246/-1.6438", //
      "https://www.openstreetmap.org/#map=10/42.8246/-1.6438", //
      "https://www.openstreetmap.org/#map=15/42.538/1.7214", //
      "http://maps.google.com/maps?ll=42.818,-1.644&spn=5,5&t=m&q=42.818,-1.644", //
      "41.97376532210692, 2.8034697647126765", //
      "https://www.google.com/maps/place/17491+Peralada,+Provincia+di+Girona,+Spagna/@42.309013,3.0001924,16z/data=!3m1!4b1!4m12!1m5!3m4!2zNDLCsDQ5JzA0LjgiTiAxwrAzOCczOC40Ilc!8m2!3d42.818!4d-1.644!3m5!1s0x12ba8ed0c2ae038d:0x4c157bc397b1e7db!8m2!3d42.3083523!4d3.0094404!16zL20vMGMxZ2gy?entry=ttu", //
      "-34.13770444498097, 18.43361943813669 Fish Hoeck Beach", //
      "https://www.google.com/maps/place/Fortaleza+-+Zone+1,+Fortaleza+-+Cear%C3%A1,+Brasile/@-3.7899622,-38.6013372,12z/data=!3m1!4b1!4m6!3m5!1s0x7c74f21f6c15c2f:0xfd6d6706cb7927aa!8m2!3d-3.7327177!4d-38.5269947!16s%2Fg%2F11bc6l927w?entry=ttu", //
      "40.7241935707728, -73.99692611790844", //
      "https://www.openstreetmap.org/search?whereami=1&query=-16.7204%2C-43.8162#map=9/-16.7204/-43.8162", //
      "https://www.openstreetmap.org/#map=9/-16.7204/-43.8162"
  };

  private Pattern apat[] = { //
     // Pattern.compile(".*(-*\\d+\\.\\d+)/(-*\\d+\\.\\d+).*"), //
      Pattern.compile("[^0-9\\-\\+]*([+\\-]?\\d+\\.\\d+)[^0-9\\-\\+]*([+\\-]?\\d+\\.\\d+).*"), //
  };

  public ProvaPaste() {
    //
  }

  @Test
  public void provalo() {
    boolean bFound = false;
    for (String sz : ARR) {
      bFound = false;
      int k = 0;
      for (Pattern pat : apat) {
        k++;
        Matcher mtch = pat.matcher(sz);
        if (mtch.find()) {
          // System.out.printf("%s\t%s\n", mtch.group(1), mtch.group(2));
          System.out.printf("%d)%-18s %-18s\t%s\n", k, mtch.group(1), mtch.group(2), sz);
          bFound = true;
          break;
        }
      }
      if ( !bFound)
        System.out.printf("---:%s\n", sz);
    }
  }

}

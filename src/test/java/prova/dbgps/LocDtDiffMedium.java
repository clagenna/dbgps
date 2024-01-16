package prova.dbgps;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

public class LocDtDiffMedium {

  public LocDtDiffMedium() {
    //
  }

  @Test
  public void provalo() {
    LocalDateTime dt1 = LocalDateTime.parse("2023-07-14T10:37:34");
    LocalDateTime dt2 = LocalDateTime.parse("2023-07-14T11:48:02");

    System.out.printf("Diff fra %s and %s\n", dt1, dt2);
    long dif = dt1.until(dt2, ChronoUnit.SECONDS);
    System.out.printf("  ==> %s secs\n", dif);

    LocalDateTime dtm = dt1.plusSeconds(dif / 2);
    System.out.printf("Ora media = %s\n", dtm);

  }

}

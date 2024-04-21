package prova.concur;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.Test;

public class ProvaWalk {

  @Test
  public void provalo() {
    int depth = 99;
    Path pth = Paths.get("F:\\My Foto\\2024\\2024-02-19 Peio");
    try (Stream<Path> stre = Files.walk(pth, depth)) {
      stre //
          .filter(Files::isRegularFile) //
          .forEach(s -> gestisci(s));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void gestisci(Path pth) {
    String sz = pth.getFileName().toString().toLowerCase();
    if (sz.endsWith(".jpg") || sz.endsWith(".jpeg"))
      System.out.println(pth.toString());
    else if (sz.endsWith(".heic"))
      System.out.println("HEIC ->" + pth.toString());
    else
      System.out.printf("ProvaWalk.gestisci: Scarto il file %s\n", pth.getFileName());
  }
}

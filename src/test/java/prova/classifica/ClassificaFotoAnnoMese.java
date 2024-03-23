package prova.classifica;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ClassificaFotoAnnoMese {

  Pattern pat = Pattern.compile(".*([0-9]{4})([0-9]{2})([0-9]{2})_.*");

  public ClassificaFotoAnnoMese() {
    //
  }

  public static void main(String[] args) {
    ClassificaFotoAnnoMese app = new ClassificaFotoAnnoMese();
    final String startDir = "F:\\My Foto\\2024\\Camera";
    app.scanDir(startDir);
  }

  private void scanDir(String p_startDir) {
    try (Stream<Path> stre = Files.walk(Paths.get(p_startDir))) {
      stre //
          .filter(s -> !Files.isDirectory(s)) //
          .forEach(s -> trattaFile(s));
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private Object trattaFile(Path p_s) {
    if (null == p_s)
      return null;
    Matcher mt = pat.matcher(p_s.toString());
    if ( !mt.matches()) {
      System.out.println("Scarto file:" + p_s.getFileName().toString());
      return null;
    }
    String szAnno = mt.group(1);
    String szMese = mt.group(2);
    String szDirDest = String.format("%s_%s", szAnno, szMese);
    sposta(p_s, szDirDest);
    System.out.printf("ClassificaFotoAnnoMese.trattaFile(%s) -> %s\n", p_s.getFileName().toString(), szDirDest);
    return null;
  }

  private void sposta(Path p_s, String p_szDirDest) {
    Path dest = Paths.get(p_s.getParent().toString(), p_szDirDest);
    try {
      if (Files.notExists(dest, LinkOption.NOFOLLOW_LINKS))
        Files.createDirectory(dest);
      Path fiDest = Paths.get(dest.toString(), p_s.getFileName().toString());
      Files.move(p_s, fiDest);
    } catch (IOException e) {
      e.printStackTrace();
    }

  }
}

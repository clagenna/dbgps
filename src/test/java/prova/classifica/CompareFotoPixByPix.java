package prova.classifica;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class CompareFotoPixByPix {
  // Main driver method
  public static void main(String[] args) {
    //    if (args.length < 2) {
    //      System.out.println("Usage: foto1 foto2");
    //      return;
    //    }
    List<String> liFoto = new ArrayList<>();
    double perc = 0;
    liFoto.add("F:\\temp\\MiaShare\\20240303_100500.jpg");
    liFoto.add("F:\\temp\\MiaShare\\20240303_100500_Copy.jpg");
    liFoto.add("F:\\temp\\MiaShare\\20240303_100502.jpg");
    liFoto.add("F:\\temp\\MiaShare\\20240303_100621.jpg");

    CompareFotoPixByPix app = new CompareFotoPixByPix();
    //    double perc = app.compareFoto(args[0], args[1]);
    for (int i = 1; i < liFoto.size(); i++) {
      perc = app.compareFoto(liFoto.get(0), liFoto.get(i));
      System.out.printf("Difference Percentage %.3f fra %s e %s\n", perc, liFoto.get(0), liFoto.get(i));
    }
  }

  private double compareFoto(String p_f1, String p_f2) {
    double diff = 1000.;
    BufferedImage imgA = null;
    BufferedImage imgB = null;

    try {
      File fileA = new File(p_f1);
      File fileB = new File(p_f2);

      imgA = ImageIO.read(fileA);
      imgB = ImageIO.read(fileB);
    } catch (IOException e) {
      System.out.println(e);
      return diff;
    }

    int width1 = imgA.getWidth();
    int width2 = imgB.getWidth();
    int height1 = imgA.getHeight();
    int height2 = imgB.getHeight();

    // Checking whether the images are of same size or
    // not
    if (width1 != width2 || height1 != height2) {
      System.out.println("Error: Images dimensions" + " mismatch");
      return diff;
    }
    long lDiff = 0;
    for (int y = 0; y < height1; y++) {
      for (int x = 0; x < width1; x++) {

        int rgbA = imgA.getRGB(x, y);
        int rgbB = imgB.getRGB(x, y);
        int redA = rgbA >> 16 & 0xff;
        int greenA = rgbA >> 8 & 0xff;
        int blueA = rgbA & 0xff;
        int redB = rgbB >> 16 & 0xff;
        int greenB = rgbB >> 8 & 0xff;
        int blueB = rgbB & 0xff;

        lDiff += Math.abs(redA - redB);
        lDiff += Math.abs(greenA - greenB);
        lDiff += Math.abs(blueA - blueB);
      }
    }
    // So total number of pixels = width * height * 3
    double total_pixels = width1 * height1 * 3;
    double avg_different_pixels = lDiff / total_pixels;
    // There are 255 values of pixels in total
    diff = avg_different_pixels / 255f * 100f;

    System.out.printf("Difference Percentage %.3f\n", diff);
    return diff;
  }
}

package prova.classifica;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.MSER;
import org.opencv.imgcodecs.Imgcodecs;

import nu.pattern.OpenCV;

public class OpenCVSimilarPhoto {

  static {
    // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    OpenCV.loadLocally();
  }

  public static void main(String[] args) {
    // Load images
    //    Mat img1 = Imgcodecs.imread("F:\\My Foto\\2024\\2024-03-01 Marocco\\f20240304_093427.jpg");
    //    Mat img2 = Imgcodecs.imread("F:\\My Foto\\2024\\2024-03-01 Marocco\\f20240304_093438.jpg");
    String sz1 = "F:\\java\\photon2\\dbgps\\data\\fotos\\f20240304_093427_PICC.jpg";
    String sz2 = "F:\\java\\photon2\\dbgps\\data\\fotos\\f20240304_093438_PICC.jpg";
    String sz3 = "F:\\java\\photon2\\dbgps\\data\\fotos\\f20240304_093447_PICC.jpg";
    
//    OpenCVSimilarPhoto.doTheJob(sz1, sz2);
//    OpenCVSimilarPhoto.doTheJob(sz1, sz3);
//    OpenCVSimilarPhoto.doTheJob(sz1, sz1);
    
    OpenCVSimilarPhoto.secondMethod(sz1, sz2);
    OpenCVSimilarPhoto.secondMethod(sz1, sz3);
    OpenCVSimilarPhoto.secondMethod(sz1, sz1);
  }
  
  public static void secondMethod(String szImg1, String szImg2) {
    Mat img1 = Imgcodecs.imread(szImg1);
    Mat img2 = Imgcodecs.imread(szImg2);
    Mat diff = new Mat();
    Core.absdiff(img1,img2,diff);
    Scalar sumDiff = Core.sumElems(diff);
    double totalDiff = sumDiff.val[0] + sumDiff.val[1] + sumDiff.val[2];
    System.out.printf("Differenza = %.2f\n", totalDiff);
  }

  public static void doTheJob(String szImg1, String szImg2) {
    Mat img1 = Imgcodecs.imread(szImg1);
    Mat img2 = Imgcodecs.imread(szImg2);
    // FeatureDetector detector = AgastFeatureDetector.create(AgastFeatureDetector.AGAST_5_8);
    MSER detector = MSER.create();

    // Detect keypoints and compute descriptors
    MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
    Mat descriptors1 = new Mat();
    // detector.detectAndCompute(img1, new Mat(), keypoints1, descriptors1);
    detector.detect(img1, keypoints1, descriptors1);

    MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
    Mat descriptors2 = new Mat();
    detector.detect(img2, keypoints2, descriptors2);

    // Create descriptor matcher
    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

    // Match descriptors
    MatOfDMatch matches = new MatOfDMatch();
    matcher.match(descriptors1, descriptors2, matches);

    // Compute similarity score
    double score = matches.rows();
    System.out.printf("Differenza =%.2f\n", score);
  }
}

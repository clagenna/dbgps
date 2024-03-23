package prova.classifica;

import nu.pattern.OpenCV;

public class ProvaOpenCVFace {

  public static void main(String[] args) {
    // Load the native OpenCV library
    // OpenCV.loadShared(); not for Java>12.0
    OpenCV.loadLocally();
    // System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    new OpenCVFaceDetection().run(args);
  }
}

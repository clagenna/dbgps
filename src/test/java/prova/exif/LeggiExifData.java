package prova.exif;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryType;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii;
import org.junit.Test;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import sm.clagenna.stdcla.utils.ParseData;

public class LeggiExifData {

  private static final String CSZ_DIRFOTO = "\\\\nascasa\\photo\\2024\\2024-03-01 Marocco\\02";
  private List<Path>          liPath      = new ArrayList<>();

  // vedi: https://exiftool.org/TagNames/EXIF.html

  private static int                TAG_OFFSET_TIME            = 0x9010;
  private static int                TAG_OFFSET_TIME_ORIG       = 0x9011;
  private static int                TAG_OFFSET_TIME_DIGIT      = 0x9012;
  private static int                TAG_TIME_ZONE_OFfSET       = 0x882a;
  private static final TagInfoAscii TIFF_TAG_OFFSET_TIME       = new TagInfoAscii("OffsetTime", TAG_OFFSET_TIME, 20,
      TiffDirectoryType.TIFF_DIRECTORY_ROOT);
  private static final TagInfoAscii TIFF_TAG_OFFSET_TIME_ORIG  = new TagInfoAscii("OffsetTimeOrig", TAG_OFFSET_TIME_ORIG, 20,
      TiffDirectoryType.TIFF_DIRECTORY_ROOT);
  private static final TagInfoAscii TIFF_TAG_OFFSET_TIME_DIGIT = new TagInfoAscii("OffsetTimeOrig", TAG_OFFSET_TIME_DIGIT, 20,
      TiffDirectoryType.TIFF_DIRECTORY_ROOT);
  private static final TagInfoAscii TIFF_TAG_TIMEZONE_OFFSET   = new TagInfoAscii("OffsetTimeOrig", TAG_TIME_ZONE_OFfSET, 20,
      TiffDirectoryType.TIFF_DIRECTORY_ROOT);

  SimpleDateFormat s_sfmt = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");

  @Test
  public void doTheJob() throws ImageReadException {
    liPath.add(Paths.get(CSZ_DIRFOTO, "f20240302_085448.jpg"));
    //    try {
    //      Files.list(Paths.get(CSZ_DIRFOTO)) //
    //          .forEach(s -> leggiMetadata(s.toFile()));
    for (Path pth : liPath)
      leggiMetadata2(pth);
    //    } catch (IOException e) {
    //      e.printStackTrace();
    //    }
  }

  @SuppressWarnings("unused")
  private void leggiMetadata2(Path p_fi) throws ImageReadException {
    ImageMetadata imgMetadt = null;
    JpegImageMetadata jpegMetadata = null;
    try {
      imgMetadt = Imaging.getMetadata(p_fi.toFile());
      if ( ! (imgMetadt instanceof JpegImageMetadata)) {
        System.out.println("non ho metadata !!!");
        return;
      }
      jpegMetadata = (JpegImageMetadata) imgMetadt;
    } catch (ImageReadException | IOException e) {
      e.printStackTrace();
      return;
    }
    final TiffField dateTimeField = jpegMetadata.findEXIFValueWithExactMatch(TiffTagConstants.TIFF_TAG_DATE_TIME);
    ParseData prsDt = new ParseData();
    LocalDateTime locDt = null;
    OffsetDateTime ofsDt = null;
    if (dateTimeField != null) {
      System.out.println("Date Time=" + dateTimeField.toString());
      locDt = prsDt.parseData(dateTimeField.getStringValue());
    } else
      System.out.println("Date Time **NULL**");

    TiffField oofs = jpegMetadata.findEXIFValue(TIFF_TAG_OFFSET_TIME);
    if (oofs != null) {
      System.out.println("Offset Time=" + oofs.toString());
      String szOfs = oofs.getStringValue();
      ofsDt = prsDt.parseOffsetDateTime(dateTimeField.getStringValue(), szOfs);
      System.out.println("Date Time Offset =" + ofsDt.toString());
    } else
      System.out.println("Offset Time **NULL**");

    oofs = jpegMetadata.findEXIFValue(TIFF_TAG_OFFSET_TIME_DIGIT);
    if (oofs != null)
      System.out.println("Offset Time Digit=" + oofs.toString());
    else
      System.out.println("Offset Time Digit **NULL**");

    oofs = jpegMetadata.findEXIFValue(TIFF_TAG_OFFSET_TIME_ORIG);
    if (oofs != null)
      System.out.println("Offset Time Orig=" + oofs.toString());
    else
      System.out.println("Offset Time Orig **NULL**");

    oofs = jpegMetadata.findEXIFValue(TIFF_TAG_TIMEZONE_OFFSET);
    if (oofs != null)
      System.out.println("Offset Time Zone Offset=" + oofs.toString());
    else
      System.out.println("Offset Time Zone Offset **NULL**");
  }

  @SuppressWarnings("unused")
  private void leggiMetadata(Path p_fi) {
    boolean bDeb = true;
    if (bDeb)
      System.out.println("\n" + p_fi.toString());
    Metadata metadata = null;
    try {
      metadata = ImageMetadataReader.readMetadata(p_fi.toFile());
    } catch (ImageProcessingException | IOException e) {
      e.printStackTrace();
      return;
    }

    //    String szDtTime = null;
    String szTimeZone = null;
    String szLatLonRef = null;
    String szDtTime = null;
    String szLat = null;
    String szLon = null;
    String szAlt = null;

    for (Directory directory : metadata.getDirectories()) {
      for (Tag tag : directory.getTags()) {
        System.out.format("%s/%s = %s\n", directory.getName(), tag.getTagName(), tag.getDescription());
        String szTag = String.format("%s/%s", directory.getName(), tag.getTagName());
        String szVal = tag.getDescription();
        switch (szTag) {
          //  Exif SubIFD/Time Zone = +02:00
          //  Exif SubIFD/Time Zone Original = +02:00
          case "Exif SubIFD/Time Zone":
            szTimeZone = szVal;
            break;

          case "Exif SubIFD/Date/Time Original":
          case "Exif IFD0/Date/Time":
            szDtTime = szVal;
            break;

          // GPS/GPS Latitude Ref = N
          // GPS/GPS Latitude = 49Â° 26' 23,88
          // GPS/GPS Longitude Ref = E
          // GPS/GPS Longitude = 1Â° 5' 57,27
          // GPS/GPS Altitude Ref = Sea level
          // GPS/GPS Altitude = 100 metres
          case "GPS/GPS Latitude Ref":
            szLatLonRef = szVal;
            break;
          case "GPS/GPS Latitude":
            szLat = szLatLonRef + " " + szVal;
            break;
          case "GPS/GPS Longitude Ref":
            szLatLonRef = szVal;
            break;
          case "GPS/GPS Longitude":
            szLon = szLatLonRef + " " + szVal;
            break;
          case "GPS/GPS Altitude Ref":
            break;
          case "GPS/GPS Altitude":
            szAlt = szVal;
            break;
        }
      }
      if (directory.hasErrors()) {
        for (String error : directory.getErrors()) {
          System.err.format("ERROR: %s", error);
        }
      }
    }
  }

}

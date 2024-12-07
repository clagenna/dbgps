package prova.exif;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffField;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.TiffDirectoryType;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.taginfos.TagInfoAscii;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;

import sm.clagenna.stdcla.utils.ParseData;

public class CambiaZoneOffset {
  private static int    TAG_OFFSET_TIME  = 0x9010;
  private static String TAG_DEFAULT_ZONE = "+01:00";

  private static final TagInfoAscii TIFF_TAG_OFFSET_TIME = new TagInfoAscii(//
      "OffsetTime", //
      TAG_OFFSET_TIME, //
      -1, //
      TiffDirectoryType.TIFF_DIRECTORY_ROOT);

  ParseData             prsDt;
  private LocalDateTime m_lastDateTime;
  // private ZoneOffset    m_lastZone;

  public CambiaZoneOffset() {
    //
  }

  public static void main(String[] args) {
    if (null == args || 0 == args.length) {
      CambiaZoneOffset.usage();
      return;
    }
    Path pth = Paths.get(args[0]);
    if ( !Files.exists(pth, LinkOption.NOFOLLOW_LINKS)) {
      CambiaZoneOffset.usage();
      return;
    }
    CambiaZoneOffset app = new CambiaZoneOffset();
    try {
      app.doTheJob(pth);
    } catch (ImageReadException e) {
      e.printStackTrace();
    }
  }

  private static void usage() {
    System.out.println("Devi specificare un direttorio esistente da analizzare");
  }

  public void doTheJob(Path p_pth) throws ImageReadException {
    prsDt = new ParseData();
    try {
      Files.list(p_pth) //
          .filter(s -> s.toString().endsWith(".jpg")) //
          .forEach(s -> studiaFile(s));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Object studiaFile(Path pth) {
    System.out.printf("\n%s\n", pth.toString());
    String szOfset = leggiMetadata(pth);
    if ( !szOfset.contains("+01:00"))
      cambiaOffset(pth);
    return null;
  }

  private String leggiMetadata(Path p_pth) {
    String szRet = null;
    ImageMetadata imgMetadt = null;
    JpegImageMetadata jpegMetadata = null;
    try {
      imgMetadt = Imaging.getMetadata(p_pth.toFile());
      if ( ! (imgMetadt instanceof JpegImageMetadata)) {
        System.out.println("non ho metadata !!!");
        return szRet;
      }
      jpegMetadata = (JpegImageMetadata) imgMetadt;
    } catch (ImageReadException | IOException e) {
      e.printStackTrace();
      return szRet;
    }
    final TiffField dateTimeField = jpegMetadata.findEXIFValueWithExactMatch(TiffTagConstants.TIFF_TAG_DATE_TIME);
    m_lastDateTime = null;
    try {
      if (dateTimeField == null) {
        System.out.println("Date Time **NULL**");
        return szRet;
      }
      System.out.println("Date Time=" + dateTimeField.toString());
      szRet = dateTimeField.getStringValue();
      m_lastDateTime = ParseData.parseData(szRet);
    } catch (ImageReadException e) {
      e.printStackTrace();
      return szRet;
    }
    OffsetDateTime dtOfs = null;
    try {
      TiffField oofs = jpegMetadata.findEXIFValue(TIFF_TAG_OFFSET_TIME);
      if (oofs != null) {
        String szOfsz = oofs.getStringValue();
        // m_lastZone = ZoneOffset.of(szOfsz);
        System.out.println("Offset Time=" + oofs.toString());
        dtOfs = prsDt.parseOffsetDateTime(dateTimeField.getStringValue(), szOfsz);
        szRet += dtOfs.toString();
      } else
        System.out.println("Offset Time **NULL**");
    } catch (ImageReadException e) {
      e.printStackTrace();
      return szRet;
    }
    return szRet;
  }

  private void cambiaOffset(Path p_pth) {
    Path pthCopy = backupFotoFile(p_pth);
    boolean bOk = false;
    if (null == pthCopy) {
      System.err.println("errore copia file: " + p_pth.toString());
      return;
    }
    File jpegFrom = pthCopy.toFile();
    File jpegDst = p_pth.toFile();

    try (FileOutputStream fos = new FileOutputStream(jpegDst); OutputStream os = new BufferedOutputStream(fos);) {
      TiffOutputSet outputSet = null;
      final ImageMetadata metadata = Imaging.getMetadata(jpegFrom);
      final JpegImageMetadata jpegMetadata = (JpegImageMetadata) metadata;
      TiffImageMetadata exif = null;
      if (null != jpegMetadata) {
        exif = jpegMetadata.getExif();
        if (null != exif) {
          outputSet = exif.getOutputSet();
        }
      }
      if (null == outputSet)
        outputSet = new TiffOutputSet();

      // Scrittura del nuovo ZoneOffset
      final TiffOutputDirectory exifDirectory = outputSet.getOrCreateExifDirectory();
      exifDirectory.removeField(TIFF_TAG_OFFSET_TIME);
      exifDirectory.add(TIFF_TAG_OFFSET_TIME, TAG_DEFAULT_ZONE);

      exifDirectory.removeField(TiffTagConstants.TIFF_TAG_DATE_TIME);
      // per correggere le foto (EOS r6) che hanno +02:00 in Marocco
      // tolgo -1 a quelle foto che hanno +02:00 per riportarle a +01:00
      String sz = ParseData.s_fmtDtExif.format(m_lastDateTime.minusHours(1));
      exifDirectory.add(TiffTagConstants.TIFF_TAG_DATE_TIME, sz);

      new ExifRewriter().updateExifMetadataLossless(jpegFrom, os, outputSet);
      System.out.printf("*********   CAMBIATO %s\n", p_pth.toString());
      bOk = true;
    } catch (ImageReadException e) {
      e.printStackTrace();
    } catch (ImageWriteException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    try {
      if (bOk) {
        Files.delete(pthCopy);
        if (null != m_lastDateTime) {
          cambiaAttrFile(p_pth, m_lastDateTime);
        }
      } else
        Files.move(pthCopy, p_pth, StandardCopyOption.REPLACE_EXISTING);
    } catch (Exception e) {
      System.out.printf("Errore di cancellazione di %s\n", pthCopy.toString());
    }

  }

  private void cambiaAttrFile(Path pthNew, FileTime timeFi) throws IOException {
    Files.setAttribute(pthNew, "creationTime", timeFi);
    Files.setAttribute(pthNew, "lastAccessTime", timeFi);
    Files.setAttribute(pthNew, "lastModifiedTime", timeFi);
  }

  private void cambiaAttrFile(Path p_pth, LocalDateTime p_dt) throws IOException {
    FileTime timeFi = FileTime.from(p_dt.toInstant(ZoneOffset.UTC));
    cambiaAttrFile(p_pth, timeFi);
  }

  private Path backupFotoFile(Path pth) {
    String szExt = ".jpg";
    int n = pth.toString().lastIndexOf(".");
    if (n > 0)
      szExt = pth.toString().substring(n + 1);
    Path pthCopy = Paths.get(pth.getParent().toString(), UUID.randomUUID().toString() + "." + szExt);
    try {
      Files.copy(pth, pthCopy, StandardCopyOption.COPY_ATTRIBUTES);
    } catch (IOException e) {
      pthCopy = null;
      System.err.printf("Errore %s backup file per %s\n", e.getMessage(), pth.toString());
    }
    return pthCopy;
  }

}

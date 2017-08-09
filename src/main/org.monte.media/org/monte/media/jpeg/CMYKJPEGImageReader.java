/* @(#)CMYJKJPEGImageReader.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.media.jpeg;

import org.monte.media.jfif.JFIFInputStream;
import java.awt.color.ICC_Profile;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.monte.media.image.CMYKImages;
import static org.monte.media.image.CMYKImages.createImageFromInvertedCMYK;
import static org.monte.media.image.CMYKImages.createImageFromInvertedYCCK;
import static org.monte.media.image.CMYKImages.createImageFromRGB;
import static org.monte.media.image.CMYKImages.createImageFromYCC;
import static org.monte.media.image.CMYKImages.createImageFromYCCK;
import org.monte.media.io.ByteArrayImageInputStream;
import org.monte.media.io.IOStreams;
import org.monte.media.io.ImageInputStreamAdapter;

/**
 * Reads a JPEG image with colors in the CMYK color space.
 * <p>
 * For optimal performance with CMYK images, please run the JVM with the following
 * VM option:
 * <pre>
 * -Dsun.java2d.cmm=sun.java2d.cmm.kcms.KcmsServiceProvider
 * </pre>
 *
 * @author Werner Randelshofer
 * @version $Id: CMYKJPEGImageReader.java 364 2016-11-09 19:54:25Z werner $
 */
public class CMYKJPEGImageReader extends ImageReader {

  private boolean ignoreIccProfile = false;

  /**
   * This profile is used when ignoreIccProfile is true, or when no profile is provided by the image data.
   */
  private ICC_Profile defaultIccProfile = null;
  
  /**
   * In JPEG files, YCCK and CMYK values are typically stored as inverted
   * values.
   */
  private boolean isInvertColors = true;
  /**
   * When we read the header, we read the whole image.
   */
  private BufferedImage image;

  /**
   * This value is set to true, when we returned the image.
   */
  private boolean didReturnImage;

  public CMYKJPEGImageReader() {
    this(new CMYKJPEGImageReaderSpi());
  }

  public CMYKJPEGImageReader(ImageReaderSpi originatingProvider) {
    super(originatingProvider);
  }

  @Override
  public int getNumImages(boolean allowSearch) throws IOException {
    return 1;
  }

  @Override
  public int getWidth(int imageIndex) throws IOException {
    readHeader();
    return image.getWidth();
  }

  @Override
  public int getHeight(int imageIndex) throws IOException {
    readHeader();
    return image.getHeight();
  }

  @Override
  public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
    readHeader();
    LinkedList<ImageTypeSpecifier> l = new LinkedList<ImageTypeSpecifier>();
    l.add(new ImageTypeSpecifier(CMYKImages.RGB, CMYKImages.RGB.createCompatibleSampleModel(image.getWidth(), image.getHeight())));
    return l.iterator();
  }

  @Override
  public IIOMetadata getStreamMetadata() throws IOException {
    return null;
  }

  @Override
  public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
    return null;
  }

  @Override
  public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
    if (imageIndex > 0) {
      throw new IndexOutOfBoundsException();
    }
    readHeader();
    didReturnImage = true;
    return image;
  }

  /**
   * Reads the PGM header. Does nothing if the header has already been loaded.
   */
  private void readHeader() throws IOException {
    if (image == null) {

      ImageInputStream iis = null;
      Object in = getInput();
      /* No need for JMF support in CMYKJPEGImageReader.
             if (in instanceof Buffer) {
             in = ((Buffer) in).getData();
             }*/

      if (in instanceof byte[]) {
        iis = new ByteArrayImageInputStream((byte[]) in);
      } else if (in instanceof ImageInputStream) {
        iis = (ImageInputStream) in;
      } else if (in instanceof InputStream) {
        iis = new MemoryCacheImageInputStream((InputStream) in);
      } else {
        throw new IOException("Can't handle input of type " + in);
      }
      didReturnImage = false;
      image = read(iis, isInvertColors, ignoreIccProfile, defaultIccProfile);
    }
  }

  /**
   * @return the YCCKInversed property.
   */
  public boolean isInvertColors() {
    return isInvertColors;
  }

  /**
   * @param newValue the new value
   */
  public void setInvertColors(boolean newValue) {
    this.isInvertColors = newValue;
  }

  public boolean isIgnoreIccProfile() {
    return ignoreIccProfile;
  }

  public void setIgnoreIccProfile(boolean newValue) {
    this.ignoreIccProfile = newValue;
  }
  
  public ICC_Profile getDefaultIccProfile() {
    return defaultIccProfile;
  }

  public void setDefaultIccProfile(ICC_Profile newValue) {
    this.defaultIccProfile = newValue;
  }
  
  public static BufferedImage read(ImageInputStream in, boolean inverseYCCKColors, boolean ignoreProfile,
          ICC_Profile defaultProfile) throws IOException {
    // Seek to start of input stream
    in.seek(0);

    // Extract metadata from the JFIF stream.
    // --------------------------------------
    // In particular, we are interested into the following fields:
    int samplePrecision = 0;
    int numberOfLines = 0;
    int numberOfSamplesPerLine = 0;
    int numberOfComponentsInFrame = 0;
    int app14AdobeColorTransform = 0;
    ByteArrayOutputStream app2ICCProfile = new ByteArrayOutputStream();
    // Browse for marker segments, and extract data from those
    // which are of interest.
    JFIFInputStream fifi = new JFIFInputStream(new ImageInputStreamAdapter(in));
    for (JFIFInputStream.Segment seg = fifi.getNextSegment(); seg != null; seg = fifi.getNextSegment()) {
      if (0xffc0 <= seg.marker && seg.marker <= 0xffc3
              || 0xffc5 <= seg.marker && seg.marker <= 0xffc7
              || 0xffc9 <= seg.marker && seg.marker <= 0xffcb
              || 0xffcd <= seg.marker && seg.marker <= 0xffcf) {
        // SOF0 - SOF15: Start of Frame Header marker segment
        DataInputStream dis = new DataInputStream(fifi);
        samplePrecision = dis.readUnsignedByte();
        numberOfLines = dis.readUnsignedShort();
        numberOfSamplesPerLine = dis.readUnsignedShort();
        numberOfComponentsInFrame = dis.readUnsignedByte();
        // ...the rest of SOF header is not important to us.
        // In fact, by encountering a SOF header, we have reached
        // the end of the metadata section we are interested in.
        // Thus we can abort here.
        break;

      } else if (seg.marker == 0xffe2) {
        // APP2: Application-specific marker segment
        if (seg.length >= 26) {
          DataInputStream dis = new DataInputStream(fifi);
          // Check for 12-bytes containing the null-terminated string: "ICC_PROFILE".
          if (dis.readLong() == 0x4943435f50524f46L && dis.readInt() == 0x494c4500) {
            // Skip 2 bytes
            dis.skipBytes(2);

            // Read Adobe ICC_PROFILE int buffer. The profile is split up over
            // multiple APP2 marker segments.
            IOStreams.copy(dis,app2ICCProfile);
          }
        }
      } else if (seg.marker == 0xffee) {
        // APP14: Application-specific marker segment
        if (seg.length == 12) {
          DataInputStream dis = new DataInputStream(fifi);
          // Check for 6-bytes containing the null-terminated string: "Adobe".
          if (dis.readInt() == 0x41646f62L && dis.readUnsignedShort() == 0x6500) {
            int version = dis.readUnsignedByte();
            int app14Flags0 = dis.readUnsignedShort();
            int app14Flags1 = dis.readUnsignedShort();
            app14AdobeColorTransform = dis.readUnsignedByte();
          }
        }
      }
    }
    //fifi.close();
      // Try to instantiate an ICC_Profile from the app2ICCProfile
      ICC_Profile profile = defaultProfile;
      if (!ignoreProfile && app2ICCProfile.size() > 0) {
        try {
          profile = ICC_Profile.getInstance(new ByteArrayInputStream(app2ICCProfile.toByteArray()));
        } catch (Throwable ex) {
          // icc profile is corrupt
          ex.printStackTrace();
        }
      }


    // Read the image data
    BufferedImage img = null;
    if (numberOfComponentsInFrame != 4) {
      // Read image with YCC color encoding.
      in.seek(0);
      img = readImageFromYCC(new ImageInputStreamAdapter(in), null);
    } else if (numberOfComponentsInFrame == 4) {

      switch (app14AdobeColorTransform) {
        case 0:
        default:
          // Read image with RGBW color encoding.
          in.seek(0);

          if (inverseYCCKColors) {
            img = readImageFromInvertedCMYK(new ImageInputStreamAdapter(in), profile);
          } else {
            img = readImageFromCMYK(new ImageInputStreamAdapter(in), profile);
          }
          break;
        case 1:
          throw new IOException("YCbCr not supported");
        case 2:
          // Read image with inverted YCCK color encoding.
          // FIXME - How do we determine from the JFIF file whether
          // YCCK colors are inverted?

          in.seek(0);
          if (inverseYCCKColors) {
            img = readImageFromInvertedYCCK(new ImageInputStreamAdapter(in), profile);
          } else {
            img = readImageFromYCCK(new ImageInputStreamAdapter(in), profile);
          }
          break;
      }
    }
    return img;
  }

  public static ImageReader createNativeJPEGReader() {
    for (ImageReader r : (Iterable<ImageReader>) () -> ImageIO.getImageReadersByFormatName("jpeg")) {
      if ("com.sun.imageio.plugins.jpeg.JPEGImageReader".equals(r.getClass().getName())) {
        return r;
      }
    }
    throw new InternalError("could not find native JPEG Reader");
  }

  /**
   * Reads a CMYK JPEG image from the provided InputStream, converting the
   * colors to RGB using the provided CMYK ICC_Profile. The image data must be
   * in the CMYK color space.
   * <p>
   * Use this method, if you have already determined that the input stream
   * contains a CMYK JPEG image.
   *
   * @param in An InputStream, preferably an ImageInputStream, in the JPEG File
   * Interchange Format (JFIF).
   * @param cmykProfile An ICC_Profile for conversion from the CMYK color space
   * to the RGB color space. If this parameter is null, a default profile is
   * used.
   * @return a BufferedImage containing the decoded image
   * @throws java.io.IOException
   */
  public static BufferedImage readImageFromCMYK(InputStream in, ICC_Profile cmykProfile) throws IOException {
    ImageInputStream inputStream = null;
    ImageReader reader = createNativeJPEGReader();
    try {
      inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
      reader.setInput(inputStream);
      Raster raster = reader.readRaster(0, null);
      BufferedImage image = CMYKImages.createImageFromCMYK(raster, cmykProfile);
      return image;
    } finally {
      reader.dispose();
    }
  }

  /**
   * Reads a RGBA JPEG image from the provided InputStream, converting the
   * colors to RGBA using the provided RGBA ICC_Profile. The image data must be
   * in the RGBA color space.
   * <p>
   * Use this method, if you have already determined that the input stream
   * contains a RGBA JPEG image.
   *
   * @param in An InputStream, preferably an ImageInputStream, in the JPEG File
   * Interchange Format (JFIF).
   * @param rgbaProfile An ICC_Profile for conversion from the RGBA color space
   * to the RGBA color space. If this parameter is null, a default profile is
   * used.
   * @return a BufferedImage containing the decoded image.
   * @throws java.io.IOException
   */
  public static BufferedImage readImageFromInvertedCMYK(InputStream in, ICC_Profile rgbaProfile) throws IOException {
    ImageInputStream inputStream = null;
    ImageReader reader = createNativeJPEGReader();
    try {
      inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
      reader.setInput(inputStream);
      Raster raster = reader.readRaster(0, null);
      BufferedImage image = createImageFromInvertedCMYK(raster, rgbaProfile);
      return image;
    } finally {
      reader.dispose();
    }
  }

  public static BufferedImage readImageFromRGB(InputStream in, ICC_Profile rgbaProfile) throws IOException {
    ImageInputStream inputStream = null;
    ImageReader reader = createNativeJPEGReader();
    try {
      inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
      reader.setInput(inputStream);
      Raster raster = reader.readRaster(0, null);
      BufferedImage image = createImageFromRGB(raster, rgbaProfile);
      return image;
    } finally {
      reader.dispose();
    }
  }

  public static BufferedImage readImageFromYCC(InputStream in, ICC_Profile yccProfile) throws IOException {
    ImageInputStream inputStream = null;
    ImageReader reader = createNativeJPEGReader();
    try {
      inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
      reader.setInput(inputStream);
      Raster raster = reader.readRaster(0, null);
      BufferedImage image = createImageFromYCC(raster, yccProfile);
      return image;
    } finally {
      reader.dispose();
    }
  }

  /**
   * Reads a YCCK JPEG image from the provided InputStream, converting the
   * colors to RGB using the provided CMYK ICC_Profile. The image data must be
   * in the YCCK color space.
   * <p>
   * Use this method, if you have already determined that the input stream
   * contains a YCCK JPEG image.
   *
   * @param in An InputStream, preferably an ImageInputStream, in the JPEG File
   * Interchange Format (JFIF).
   * @param cmykProfile An ICC_Profile for conversion from the CMYK color space
   * to the RGB color space. If this parameter is null, a default profile is
   * used.
   * @return a BufferedImage containing the decoded image.
   * @throws java.io.IOException
   */
  public static BufferedImage readImageFromYCCK(InputStream in, ICC_Profile cmykProfile) throws IOException {
    ImageInputStream inputStream = null;
    ImageReader reader = createNativeJPEGReader();
    try {
      inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
      reader.setInput(inputStream);
      Raster raster = reader.readRaster(0, null);
      BufferedImage image = createImageFromYCCK(raster, cmykProfile);
      return image;
    } finally {
      reader.dispose();
    }
  }

  /**
   * Reads an inverted-YCCK JPEG image from the provided InputStream, converting
   * the colors to RGB using the provided CMYK ICC_Profile. The image data must
   * be in the inverted-YCCK color space.
   * <p>
   * Use this method, if you have already determined that the input stream
   * contains an inverted-YCCK JPEG image.
   *
   * @param in An InputStream, preferably an ImageInputStream, in the JPEG File
   * Interchange Format (JFIF).
   * @param cmykProfile An ICC_Profile for conversion from the CMYK color space
   * to the RGB color space. If this parameter is null, a default profile is
   * used.
   * @return a BufferedImage containing the decoded image.
   * @throws java.io.IOException
   */
  public static BufferedImage readImageFromInvertedYCCK(InputStream in, ICC_Profile cmykProfile) throws IOException {
    ImageInputStream inputStream = null;
    ImageReader reader = createNativeJPEGReader();
    try {
      inputStream = (in instanceof ImageInputStream) ? (ImageInputStream) in : ImageIO.createImageInputStream(in);
      reader.setInput(inputStream);
      Raster raster = reader.readRaster(0, null);
      BufferedImage image = createImageFromInvertedYCCK(raster, cmykProfile);
      return image;
    } finally {
      reader.dispose();
    }
  }

  /**
   * Disposes of resources held internally by the reader.
   */
  @Override
  public void dispose() {
    try {
      if (image != null && !didReturnImage) {
        image.flush();
      }
    } catch (Throwable ex) {
      // consume the exception
      ex.printStackTrace();
    } finally {
      image = null;
    }
  }
  
}

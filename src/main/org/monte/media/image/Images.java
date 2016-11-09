/* @(#)Images.java
 * Copyright © 2005-2008 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.image;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.*;
import java.net.*;
import java.util.Hashtable;
import org.monte.media.util.stream.RangeStream;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import javax.swing.*;
import org.monte.media.color.ColorSpaces;

/**
 * Image processing methods for buffered images.
 *
 * @author Werner Randelshofer, Karl von Randow
 * @version 2.2 2008-04-19 Create graphiteFilter lazily.
 * <br>2.1 2007-07-25 Added method toBufferedImage(RenderedImage).
 * <br>2.0 2006-12-24 by Karl von Randow: On the fly conversion from Aqua Blue
 * to Aqua Graphite appearance added.
 * <br>1.0.2 2005-09-12 Brought my work-around for Java 1.4.1 back to live.
 * <br>1.0.1 2005-05-21 Accidentaly used bitmask transparency instead of
 * translucent transparency.
 * <br>1.0 13 March 2005 Created.
 */
public class Images {

  private static final DirectColorModel RGB = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
  private static final DirectColorModel ARGB = new DirectColorModel(32, 0xff0000, 0xff00, 0xff, 0xff000000);

  /**
   * Prevent instance creation.
   */
  private Images() {
  }

  /*
     private static GraphiteFilter graphiteFilter;
    
     private static GraphiteFilter getGraphiteFilter() {
     if (graphiteFilter == null) {
     graphiteFilter = new GraphiteFilter();
     }
     return graphiteFilter;
     }*/
  public static Image createImage(Class<?> baseClass, String location) {
    URL resource = baseClass.getResource(location);
    if (resource == null) {
      System.err.println("Warning: Images.createImage no resource found for " + baseClass + " " + location);
      return null;
    }
    return createImage(resource);
  }

  public static Image createImage(URL resource) {
    Image image = Toolkit.getDefaultToolkit().createImage(resource);
    /*
         if (Preferences.getString("AppleAquaColorVariant").equals("6")) {
         if (canGraphite(resource)) {
         image = toGraphite(image);
         }
         }*/
    return image;
  }

  /*
     private static Properties canGraphite;
    
     private static boolean canGraphite(URL resource) {
     if (canGraphite == null) {
     synchronized (Images.class) {
     if (canGraphite == null) {
     Properties p = new Properties();
     try {
     p.load(Images.class.getResourceAsStream("graphiteable.properties"));
     } catch (IOException e) {
     System.err.println("Failed to load graphiteable.properties: " + e);
     }
     canGraphite = p;
     }
     }
     }
     String file = resource.getFile();
     int i = file.lastIndexOf(File.separatorChar);
     if (i != -1) {
     file = file.substring(i + 1);
     }
     return canGraphite.containsKey(file);
     }
    
     /**
     * This method returns a buffered image with the contents of an image.
     *
     * Code derived from the Java Developers Almanac 1.4
     * http://javaalmanac.com/egs/java.awt.image/Image2Buf.html?l=rel
     * /
     private static Image toGraphite(Image image) {
     return Toolkit.getDefaultToolkit().
     createImage(new FilteredImageSource(image.getSource(), getGraphiteFilter()));
     }*/
  /**
   * Based on a code example from:
   * http://tams-www.informatik.uni-hamburg.de/applets/hades/webdemos/00-intro/02-imageprocessing/saturation.html
   * author karlvr
   *
   * /
   * public static class GraphiteFilter extends RGBImageFilter { private final
   * static float saturationAdjust = 0.179f; private static float hueAdjust =
   * 0.0052f; private static float brightnessAdjust = 0.09f;
   *
   *
   * private float[] hsb = new float[3];
   *
   * public int filterRGB(int x, int y, int rgb) { int alpha = rgb & 0xff000000;
   * int red = (rgb >> 16) & 0xff; int green = (rgb >> 8) & 0xff; int blue = rgb
   * & 0xff; /* float RW = (1f - saturationAdjust) * 0.3086f; // or 0.299 for
   * YIV values float RG = (1f - saturationAdjust) * 0.6084f; // or 0.587 for
   * YIV values float RB = (1f - saturationAdjust) * 0.0820f; // or 0.114 for
   * YIV values / float RW = (1f - saturationAdjust) * 0.333f; // or 0.299 for
   * YIV values float RG = (1f - saturationAdjust) * 0.333f; // or 0.587 for YIV
   * values float RB = (1f - saturationAdjust) * 0.333f; // or 0.114 for YIV
   * values
   *
   * float a = RW + saturationAdjust; float b = RW; float c = RW; float d = RG;
   * float e = RG + saturationAdjust; float f = RG; float g = RB; float h = RB;
   * float i = RB + saturationAdjust;
   *
   * int outputRed = (int) (a*red + d*green + g*blue); int outputGreen = (int)
   * (b*red + e*green + h*blue); int outputBlue = (int) (c*red + f*green +
   * i*blue); return alpha | (outputRed << 16) | (outputGreen << 8) |
   * (outputBlue); } }
   */
  /**
   * Converts the image to a buffered image into the sRGB color space. Preserves
   * the alpha channel of the input image.
   * @param img img
   * @return img
   */
  public static BufferedImage toRGBImage(Image img) {
    BufferedImage src = toBufferedImage(img);
    if (src.getColorModel().getColorSpace().getType() == RGB.getColorSpace().getType()) {
      return src;
    }
    ColorModel dst = hasAlpha(src) ? ARGB : RGB;
    return toImageWithColorModel_usingColorConvertOp(img, dst);
  }

  /**
   * Converts the image into a buffered image with an RGB color model. This
   * method returns the same image, if no conversion is needed.
   * <p>
   * This method should be run with "KCMS" (Kodak Color Management System). The "Little CMS" which
   * is the default in JVMs is 4 times slower.
   * <p>
   * Start the VM with the following options:
   * <pre>
   * -Dsun.java2d.cmm=sun.java2d.cmm.kcms.KcmsServiceProvider
   * </pre>
   *
   * @param img an image
   * @param cm the destination color model
   * @return the converted image, may be the same as the source image
   */
  public static BufferedImage toImageWithColorModel_usingColorConvertOp(Image img, ColorModel cm) {
    BufferedImage src = toBufferedImage(img);
    if (src.getColorModel().equals(cm)) {
      return src;
    }
    int w = src.getWidth();
    int h = src.getHeight();

    ColorConvertOp op = new ColorConvertOp(src.getColorModel().getColorSpace(), cm.getColorSpace(), null);
    BufferedImage dest = new BufferedImage(cm, cm.createCompatibleWritableRaster(w, h), cm.isAlphaPremultiplied(), new Hashtable<>());

    // split the image into bands and convert each band in parallel
    //op.filter(src.getRaster(), dest.getRaster());
    RangeStream.range(0, h).parallel().forEach((lo,hi)->{
      Raster src1 = src.getRaster().createChild(0, lo, w, hi - lo, 0, 0, null);
      WritableRaster dest1 = (WritableRaster) dest.getRaster().createChild(0, lo, w, hi - lo, 0, 0, null);
      op.filter(src1, dest1);
    });

    return dest;
  }

  /**
   * Converts the image into a buffered image with an RGB color model. This
   * method returns the same image, if no conversion is needed.
   *
   * @param img an image
   * @param cm cm destination color model
   * @return the converted image, may be the same as the source image
   */
  public static BufferedImage toImageWithColorModel_usingDrawImage(Image img, ColorModel cm) {
    BufferedImage src = toBufferedImage(img);
    BufferedImage dest = new BufferedImage(cm, cm.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), cm.isAlphaPremultiplied(), new Hashtable<>());

    Graphics graphics = dest.getGraphics();
    try {
      graphics.drawImage(src, 0, 0, null);
    } finally {
      graphics.dispose();
    }
    return dest;
  }

  /**
   * Converts the image to a buffered image. Returns the same image, if the
   * image is already a buffered image.
   */
  public static BufferedImage toBufferedImage(RenderedImage rImg) {
    BufferedImage image;
    if (rImg instanceof BufferedImage) {
      image = (BufferedImage) rImg;
    } else {
      Raster r = rImg.getData();
      WritableRaster wr = WritableRaster.createWritableRaster(
              r.getSampleModel(), null);
      rImg.copyData(wr);
      image = new BufferedImage(
              rImg.getColorModel(),
              wr,
              rImg.getColorModel().isAlphaPremultiplied(),
              null
      );
    }
    return image;
  }

  /**
   * Clone the image.
   */
  public static BufferedImage cloneImage(RenderedImage rImg) {
    BufferedImage image;

    Raster r = rImg.getData();
    WritableRaster wr = WritableRaster.createWritableRaster(
            r.getSampleModel(), null);
    rImg.copyData(wr);
    image = new BufferedImage(
            rImg.getColorModel(),
            wr,
            rImg.getColorModel().isAlphaPremultiplied(),
            null
    );

    return image;
  }

  public static BufferedImage toBufferedImage(Image image) {
    if (image instanceof BufferedImage) {
      return (BufferedImage) image;
    }

    // This code ensures that all the pixels in the image are loaded
    image = new ImageIcon(image).getImage();

    // Create a buffered image with a format that's compatible with the screen
    BufferedImage bimage = null;

    if (System.getProperty("java.version").startsWith("1.4.1_")) {
      // Workaround for Java 1.4.1 on Mac OS X.
      // For this JVM, we always create an ARGB image to prevent a class
      // cast exception in
      // sun.awt.image.BufImgSurfaceData.createData(BufImgSurfaceData.java:434)
      // when we attempt to draw the buffered image.
      bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB);
    } else {
      // Determine if the image has transparent pixels; for this method's
      // implementation, see e661 Determining If an Image Has Transparent Pixels
      boolean hasAlpha;
      try {
        hasAlpha = hasAlpha(image);
      } catch (IllegalAccessError e) {
        // If we can't determine this, we assume that we have an alpha,
        // in order not to loose data.
        hasAlpha = true;
      }

      GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
      try {
        // Determine the type of transparency of the new buffered image
        int transparency = Transparency.OPAQUE;
        if (hasAlpha) {
          transparency = Transparency.TRANSLUCENT;
        }

        // Create the buffered image
        GraphicsDevice gs = ge.getDefaultScreenDevice();
        GraphicsConfiguration gc = gs.getDefaultConfiguration();
        bimage = gc.createCompatibleImage(
                image.getWidth(null), image.getHeight(null), transparency);
      } catch (Exception e) {
        //} catch (HeadlessException e) {
        // The system does not have a screen
      }

      if (bimage == null) {
        // Create a buffered image using the default color model
        int type = BufferedImage.TYPE_INT_RGB;
        if (hasAlpha) {
          type = BufferedImage.TYPE_INT_ARGB;
        }
        bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
      }
    }

    // Copy image to buffered image
    Graphics g = bimage.createGraphics();

    // Paint the image onto the buffered image
    g.drawImage(image, 0, 0, null);
    g.dispose();

    return bimage;

    // My own implementation:
    /*
         if (image instanceof BufferedImage) {
         return (BufferedImage) image;
         } else {
         BufferedImage bufImg;
         Frame f = new Frame();
         f.pack();
         MediaTracker t = new MediaTracker(f);
         t.addImage(image, 0);
         try { t.waitForAll(); } catch (InterruptedException e) {}
         
         // Workaround for Java 1.4.1 on Mac OS X.
         if (System.getProperty("java.version").startsWith("1.4.1_")) {
         bufImg = new BufferedImage(image.getWidth(f), image.getHeight(f), BufferedImage.TYPE_INT_ARGB);
         } else {
         bufImg = GraphicsEnvironment
         .getLocalGraphicsEnvironment()
         .getDefaultScreenDevice()
         .getDefaultConfiguration()
         .createCompatibleImage(image.getWidth(null), image.getHeight(null), Transparency.TRANSLUCENT);
         }
         Graphics2D imgGraphics = bufImg.createGraphics();
         imgGraphics.drawImage(image, 0, 0, f);
         imgGraphics.dispose();
         f.dispose();
         return bufImg;
         }*/
  }

  /**
   * Converts an AWT image to Java FX.
   * <p>
   * This method performs better than SwingFXUtils on Java SE 8 if the
   * underlying Raster has a DataBufferInt.
   *
   * @param bimg A buffered image, must not be null.
   * @param wimg A writable image, can be null.
   * @return a JavaFX writable image
   */
  public static WritableImage toFXImage(BufferedImage bimg, WritableImage wimg) {
    final int w = bimg.getWidth();
    final int h = bimg.getHeight();

    if (wimg == null || wimg.getWidth() != w || wimg.getHeight() != h) {
      wimg = new WritableImage(bimg.getWidth(), bimg.getHeight());
    }

    // perform fast conversion if possible
    fast:
    if ((bimg.getSampleModel() instanceof SinglePixelPackedSampleModel)
            && bimg.getRaster().getDataBuffer() instanceof DataBufferInt) {
      int[] p = ((DataBufferInt) bimg.getRaster().getDataBuffer()).getData();
      if (p.length != w * h) {
        break fast; // can't do it the fast way, because we do not know if there is an offset and/or a scanline stride
      }

      switch (bimg.getType()) {
        case BufferedImage.TYPE_INT_RGB:
          // set Alpha value to 0xff on all pixels (make pixels opaque)
          int[] o = new int[p.length];
          for (int i = 0; i < o.length; i++) {
            o[i] = p[i] | 0xff000000;
          }
          wimg.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(),
                  o, 0, bimg.getWidth());
          return wimg;
        case BufferedImage.TYPE_INT_ARGB:
          wimg.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbInstance(),
                  p, 0, bimg.getWidth());
          return wimg;
        case BufferedImage.TYPE_INT_ARGB_PRE:
          wimg.getPixelWriter().setPixels(0, 0, w, h, PixelFormat.getIntArgbPreInstance(),
                  p, 0, bimg.getWidth());
          return wimg;
      }
    }

    // do it the slow way
    return SwingFXUtils.toFXImage(bimg, wimg);
  }

  /**
   * This method returns true if the specified image has transparent pixels
   *
   * Code taken from the Java Developers Almanac 1.4
   * http://javaalmanac.com/egs/java.awt.image/HasAlpha.html
   */
  public static boolean hasAlpha(Image image) {
    // If buffered image, the color model is readily available
    if (image instanceof BufferedImage) {
      BufferedImage bimage = (BufferedImage) image;
      return bimage.getColorModel().hasAlpha();
    }

    // Use a pixel grabber to retrieve the image's color model;
    // grabbing a single pixel is usually sufficient
    PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
    try {
      pg.grabPixels();
    } catch (InterruptedException e) {
    }

    // Get the image's color model
    ColorModel cm = pg.getColorModel();
    return cm.hasAlpha();
  }

  /**
   * Splits an image into count subimages.
   */
  public static BufferedImage[] split(Image image, int count, boolean isHorizontal) {
    BufferedImage src = Images.toBufferedImage(image);
    if (count == 1) {
      return new BufferedImage[]{src};
    }

    BufferedImage[] parts = new BufferedImage[count];
    for (int i = 0; i < count; i++) {
      if (isHorizontal) {
        parts[i] = src.getSubimage(
                src.getWidth() / count * i, 0,
                src.getWidth() / count, src.getHeight()
        );
      } else {
        parts[i] = src.getSubimage(
                0, src.getHeight() / count * i,
                src.getWidth(), src.getHeight() / count
        );
      }
    }
    return parts;
  }

  /**
   * Converts the image into a format that can be handled easier.
   */
  public static BufferedImage toIntImage(BufferedImage img) {
    if (img.getRaster().getDataBuffer() instanceof DataBufferInt) {
      return img;
    } else {
      BufferedImage intImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
      Graphics2D g = intImg.createGraphics();
      g.drawImage(img, 0, 0, null);
      g.dispose();
      return intImg;
    }
  }

  /**
   * Converts an image into an array of integer pixels. If the image has an
   * integer data buffer, the internal pixel array is returned.
   */
  public static int[] toPixels(BufferedImage img) {
    return ((DataBufferInt) toIntImage(img).getRaster().getDataBuffer()).getData();
  }

  /**
   * Converts an array of integer pixels into an image. The array is referenced
   * by the image.
   */
  public static BufferedImage toImage(int[] pixels, int width, int height) {
    return new BufferedImage(DirectColorModel.getRGBdefault(),//
            Raster.createWritableRaster(new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, width, height,
                    new int[]{0xff00000, 0xff00, 0xff}),//
                    new DataBufferInt(pixels, width * height), new Point(0, 0)),
            false, null
    );
  }
}

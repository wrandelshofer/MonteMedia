/*
 * @(#)ColorManagementImageReader.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.io;

import org.monte.media.color.ParametricLinearRgbColorSpace;
import org.monte.media.color.ParametricNonLinearRgbColorSpace;
import org.monte.media.color.icc.ICC_ProfileReader;
import org.monte.media.color.tonecurve.GammaToneMapper;
import org.monte.media.color.tonecurve.ToneMapper;
import org.monte.media.math.Point2D;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.stream.ImageInputStream;
import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/// Reads an image with color management.
public class ColorManagedImageReader implements AutoCloseable {
    private ImageReader imageReader;
    private ImageInputStream imageInputStream;

    public ColorManagedImageReader() {
    }

    public void setInput(File file) {
        try {
            imageInputStream = ImageIO.createImageInputStream(file);
            Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(imageInputStream);
            if (imageReaders.hasNext()) {
                imageReader = imageReaders.next();
                imageReader.setInput(imageInputStream, false, false);
            } else {
                imageReader = null;
            }
        } catch (IOException e) {
            // bail
        }
    }

    public void setInput(ImageInputStream iis) {
        Iterator<ImageReader> imageReaders = ImageIO.getImageReaders(iis);
        if (imageReaders.hasNext()) {
            imageReader = imageReaders.next();
            imageReader.setInput(iis, false, false);
        } else {
            imageReader = null;
        }
    }

    public void setInput(ImageReader imageReader) {
        this.imageReader = imageReader;
    }

    public int getNumImages(boolean allowSearch) throws IOException {
        return imageReader.getNumImages(true);
    }

    /**
     * Reads the image with the color space as specified in the file.
     *
     * @param imageIndex the image index
     * @return the image with the color space as specified in the file.
     * @throws IOException
     */
    public BufferedImage read(int imageIndex) throws IOException {
        BufferedImage image = imageReader.read(imageIndex);
        IIOMetadata imageMetadata = imageReader.getImageMetadata(imageIndex);
        return applyColorManagement(image, imageMetadata);
    }


    private BufferedImage applyColorManagement(BufferedImage image, IIOMetadata imageMetadata) {
        var profile = new ICC_ProfileReader(imageMetadata).getProfile();
        ColorModel cm = image.getColorModel();
        ColorSpace cs = null;
        if (profile != null) {
            cs = new ICC_ColorSpace(profile);

        } else if (imageMetadata.getNativeMetadataFormatName().equals("javax_imageio_png_1.0")) {
            // Derive a color profile from the metadata
            IIOMetadataNode root = (IIOMetadataNode) imageMetadata.getAsTree("javax_imageio_png_1.0");
            NodeList gAMA = root.getElementsByTagName("gAMA");
            ToneMapper toneMapper = null;
            if (gAMA.getLength() > 0) {
                IIOMetadataNode chrm = (IIOMetadataNode) gAMA.item(0);
                float gamma = Float.parseFloat(chrm.getAttribute("value")) / 100000.0f;
                toneMapper = new GammaToneMapper(1 / gamma);
            }
            NodeList chrmNodes = root.getElementsByTagName("cHRM");
            ParametricLinearRgbColorSpace linearColorSpace = null;
            ParametricLinearRgbColorSpace linx = null;
            if (chrmNodes.getLength() > 0) {
                IIOMetadataNode chrm = (IIOMetadataNode) chrmNodes.item(0);
                float whiteX = Float.parseFloat(chrm.getAttribute("whitePointX")) / 100000.0f;
                float whiteY = Float.parseFloat(chrm.getAttribute("whitePointY")) / 100000.0f;
                float redX = Float.parseFloat(chrm.getAttribute("redX")) / 100000.0f;
                float redY = Float.parseFloat(chrm.getAttribute("redY")) / 100000.0f;
                float greenX = Float.parseFloat(chrm.getAttribute("greenX")) / 100000.0f;
                float greenY = Float.parseFloat(chrm.getAttribute("greenY")) / 100000.0f;
                float blueX = Float.parseFloat(chrm.getAttribute("blueX")) / 100000.0f;
                float blueY = Float.parseFloat(chrm.getAttribute("blueY")) / 100000.0f;
                linearColorSpace = new ParametricLinearRgbColorSpace("Custom RGB",
                        new Point2D(redX, redY),
                        new Point2D(greenX, greenY),
                        new Point2D(blueX, blueY),
                        new Point2D(whiteX, whiteY), -1);
            }
            if (linearColorSpace != null) {
                if (toneMapper != null) {
                    cs = new ParametricNonLinearRgbColorSpace("Custom RGB", linearColorSpace, toneMapper, -1);
                } else {
                    cs = linearColorSpace;
                    ;
                }
            }
        }

        if (cs != null && cm instanceof DirectColorModel dcm) {
            ColorModel colorModel = new DirectColorModel(cs,
                    dcm.getPixelSize(),
                    dcm.getRedMask(), dcm.getGreenMask(), dcm.getBlueMask(), dcm.getAlphaMask(),
                    dcm.isAlphaPremultiplied(), dcm.getTransferType());
            return new BufferedImage(
                    colorModel,
                    image.getRaster(),
                    image.isAlphaPremultiplied(),
                    null
            );
        } else if (cs != null && cm instanceof ComponentColorModel dcm) {
            ColorModel colorModel = new ComponentColorModel(cs,
                    dcm.hasAlpha(),
                    dcm.isAlphaPremultiplied(), dcm.getTransparency(), dcm.getTransferType());
            return new BufferedImage(
                    colorModel,
                    image.getRaster(),
                    image.isAlphaPremultiplied(),
                    null
            );
        }

        return image;
    }


    @Override
    public void close() {
        if (imageReader != null) {
            imageReader.dispose();
            imageReader = null;
        }
        if (imageInputStream != null) {
            try {
                imageInputStream.close();
            } catch (IOException e) {
                //bail
            }
            imageInputStream = null;
        }
    }

    public static BufferedImage read(File file) throws IOException {
        try (ImageInputStream iis = ImageIO.createImageInputStream(file);
             var reader = new ColorManagedImageReader()) {
            reader.setInput(iis);
            return reader.read(0);
        }
    }
}

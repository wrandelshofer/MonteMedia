/*
 * @(#)ColorManagementImageReader.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.io;

import org.monte.media.color.icc.ICC_ProfileReader;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
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


    public ColorManagedImageReader() {
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

    public BufferedImage read(int imageIndex) throws IOException {
        BufferedImage image = imageReader.read(imageIndex);
        IIOMetadata imageMetadata = imageReader.getImageMetadata(imageIndex);
        return applyColorManagement(image, imageMetadata);
    }

    private BufferedImage applyColorManagement(BufferedImage image, IIOMetadata imageMetadata) {
        var profile = new ICC_ProfileReader(imageMetadata).getProfile();
        if (profile != null && image.getColorModel() instanceof DirectColorModel dcm) {
            ColorModel colorModel = new DirectColorModel(new ICC_ColorSpace(profile),
                    dcm.getPixelSize(),
                    dcm.getRedMask(), dcm.getGreenMask(), dcm.getBlueMask(), dcm.getAlphaMask(),
                    dcm.isAlphaPremultiplied(), dcm.getTransferType());

            return new BufferedImage(
                    colorModel,
                    image.getRaster(),
                    image.isAlphaPremultiplied(),
                    null
            );
        } else if (profile != null && image.getColorModel() instanceof ComponentColorModel dcm) {
            ColorModel colorModel = new ComponentColorModel(new ICC_ColorSpace(profile),
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

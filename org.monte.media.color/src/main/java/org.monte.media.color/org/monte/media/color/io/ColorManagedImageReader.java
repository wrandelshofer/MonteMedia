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
import java.util.Arrays;
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
            IO.println(Arrays.toString(profile.getData()));
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

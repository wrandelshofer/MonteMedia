/*
 * @(#)ILBMImageWriter.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.ilbm;

import org.monte.media.amigabitmap.AmigaBitmapImage;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ILBMImageWriter extends ImageWriter {
    protected ILBMImageWriter(ImageWriterSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {
        return null;
    }

    @Override
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {
        return null;
    }

    @Override
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {
        var input = image.getRenderedImage();
        var sampleModel = input.getSampleModel();
        var colorModel = input.getColorModel();

        AmigaBitmapImage bmp = new AmigaBitmapImage(input.getWidth(), input.getHeight(), colorModel.getPixelSize(), colorModel);
        bmp.convertFromChunky((BufferedImage) input);
        Integer camg = null;
        if (streamMetadata instanceof ILBMMetadata m) {
            camg = m.getCamg();
        }

        ILBMEncoder codec = new ILBMEncoder();
        switch (output) {
            case File f -> {
                codec.write(f, bmp, camg);
            }
            case ImageOutputStream f -> {
                codec.write(f, bmp, camg);
            }
            default -> throw new IOException("can not write to output=" + output);
        }

    }
}

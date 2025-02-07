/*
 * @(#)ILBMWriterSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.ilbm;


import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.Locale;

public class ILBMImageWriterSpi extends ImageWriterSpi {
    private static String[] readerSpiNames =
            {ILBMImageWriter.class.getName()};
    private static String[] formatNames = {"ilbm", "ILBM"};
    private static String[] extensions = {"ilbm"};
    private static String[] mimeType = {"image/ilbm"};

    public ILBMImageWriterSpi() {
        super("Monte Media", "1.0", formatNames, extensions, mimeType,
                ILBMImageWriter.class.getName(),
                new Class<?>[]{ImageOutputStream.class},
                readerSpiNames,
                false,
                null, null, null, null,
                true,
                ILBMMetadata.nativeMetadataFormatName,
                "com.sun.imageio.plugins.bmp.BMPMetadataFormat",
                null, null);
    }

    @Override
    public boolean canEncodeImage(ImageTypeSpecifier type) {
        if (type.getColorModel() instanceof IndexColorModel icm
                && !icm.hasAlpha()
                && type.getNumBands() == 1
                && icm.getPixelSize() > 0 && icm.getPixelSize() <= 8) {
            return true;
        }
        return false;
    }

    @Override
    public ImageWriter createWriterInstance(Object extension) throws IOException {
        return new ILBMImageWriter(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "ILBM Interleaved Bitmap";
    }
}

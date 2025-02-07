/*
 * @(#)ILBMWriterSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.ilbm;

import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.Locale;

public class ILBMImageWriterSpi extends ImageWriterSpi {
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

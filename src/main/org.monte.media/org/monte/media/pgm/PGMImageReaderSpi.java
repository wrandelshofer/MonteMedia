/* @(#)PGMImageReaderSpi.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.pgm;

import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * ImageIO service provider interface for images in the Netpbm grayscale image
 * format (PGM).
 * <p>
 * See: <a href="http://netpbm.sourceforge.net/doc/pgm.html">PGM Format Specification</a>.
 *
 * @author Werner Randelshofer
 * @version 1.0 2009-12-14 Created.
 */
public class PGMImageReaderSpi extends ImageReaderSpi {

    public PGMImageReaderSpi() {
        super("Werner Randelshofer",//vendor name
                "1.0",//version
                new String[]{"PGM"},//names
                new String[]{"pgm"},//suffixes,
                new String[]{"image/pgm"},// MIMETypes,
                "org.monte.media.pgm.PGMImageReader",// readerClassName,
                new Class<?>[]{ImageInputStream.class},// inputTypes,
                null,// writerSpiNames,
                false,// supportsStandardStreamMetadataFormat,
                null,// nativeStreamMetadataFormatName,
                null,// nativeStreamMetadataFormatClassName,
                null,// extraStreamMetadataFormatNames,
                null,// extraStreamMetadataFormatClassNames,
                false,// supportsStandardImageMetadataFormat,
                null,// nativeImageMetadataFormatName,
                null,// nativeImageMetadataFormatClassName,
                null,// extraImageMetadataFormatNames,
                null// extraImageMetadataFormatClassNames
                );
    }

    @Override
    public boolean canDecodeInput(Object source) throws IOException {
        if (source instanceof ImageInputStream) {
            ImageInputStream in = (ImageInputStream) source;
            in.mark();

            // Check if file starts with "P5"
            if (in.readShort() != 0x5035) {
                in.reset();
                return false;
            }
            // Check if the next byte is a whitespace (blank, TAB, CR or LF)
            int b;
            if ((b = in.readUnsignedByte()) != 0x20 && b != 0x09 && b != 0x0d && b != 0x0a) {
                in.reset();
                return false;
            }
            in.reset();
            return true;
        }
        return false;
    }

    @Override
    public ImageReader createReaderInstance(Object extension) throws IOException {
        return new PGMImageReader(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "PGM Image Reader";
    }
}

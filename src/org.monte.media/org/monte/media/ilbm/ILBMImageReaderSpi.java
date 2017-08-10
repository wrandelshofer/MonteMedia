/* @(#)ILBMImageReaderSpi.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.ilbm;

import org.monte.media.iff.IFFParser;
import java.io.IOException;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * ImageIO service provider interface for images in the Amiga IFF Interleaved
 * Bitmap image format (ILBM).
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class ILBMImageReaderSpi extends ImageReaderSpi {
    protected final static int FORM_ID = IFFParser.stringToID("FORM");
    protected final static int CAT_ID = IFFParser.stringToID("CAT ");
    protected final static int LIST_ID = IFFParser.stringToID("LIST");
    protected final static int ILBM_ID = IFFParser.stringToID("ILBM");
    protected final static int ANIM_ID = IFFParser.stringToID("ANIM");

    public ILBMImageReaderSpi() {
        super("Werner Randelshofer",//vendor name
                "1.0",//version
                new String[]{"ILBM"},//names
                new String[]{"ilbm","lbm",""},//suffixes,
                new String[]{"image/ilbm"},// MIMETypes,
                "org.monte.media.ilbm.ILBMImageReader",// readerClassName,
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

            // Check if file starts with "FORM", "CAT " or "LIST"
            int fileID = in.readInt();
            if (fileID != FORM_ID && fileID!=CAT_ID&&fileID!=LIST_ID) {
                in.reset();
                return false;
            }
            // Check if file content is "ILBM" or "ANIM"
            int contentSize = in.readInt();
            int contentID = in.readInt();
            if (contentID != ILBM_ID && contentID!=ANIM_ID) {
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
        return new ILBMImageReader(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "ILBM Interleaved Bitmap";
    }
}

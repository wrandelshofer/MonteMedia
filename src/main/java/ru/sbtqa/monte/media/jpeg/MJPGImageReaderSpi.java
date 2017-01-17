/* @(#)MJPGImageReaderSpi.java
 * Copyright © 2010-2011 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.jpeg;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

/**
 * ImageIO service provider interface for images in the Motion JPEG (MJPG)
 * format.
 * 
 * The reader described by this class can read Motion JPEG files with omitted
 * Huffmann table.
 * 
 * For more information see: Microsoft Windows Bitmap Format. Multimedia
 * Technical Note: JPEG DIB Format. (c) 1993 Microsoft Corporation. All rights
 * reserved.
 * <a href="http://www.fileformat.info/format/bmp/spec/b7c72ebab8064da48ae5ed0c053c67a4/BMPDIB.TXT">BMPDIB.txt</a>
 *
 *
 * @author Werner Randelshofer
 * @version $Id: MJPGImageReaderSpi.java 364 2016-11-09 19:54:25Z werner $
 */
public class MJPGImageReaderSpi extends ImageReaderSpi {

    public MJPGImageReaderSpi() {
        super("Werner Randelshofer",//vendor name
              "1.0",//version
              new String[]{"MJPG"},//names
              new String[]{"mjpg"},//suffixes,
              new String[]{"image/mjpg"},// MIMETypes,
              "ru.sbtqa.monte.media.jmf.renderer.video.MJPGImageReader",// readerClassName,
              new Class<?>[]{ImageInputStream.class, InputStream.class, byte[].class/*,javax.media.Buffer.class*/},// inputTypes,
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

            // Check if file starts with a JFIF SOI magic (0xffd8=-40)
            if (in.readShort() != -40) {
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
        return new MJPGImageReader(this);
    }

    @Override
    public String getDescription(Locale locale) {
        return "MJPG Image Reader";
    }
}

/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

import org.monte.media.jcodec.mp4.MP4WriterSpi;

/**
 * Provides a movie writer that uses the JCodec library.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.media.jcodec {
    requires jcodec;
    requires java.desktop;

    requires org.monte.media;

    provides org.monte.media.av.MovieWriterSpi with MP4WriterSpi;
    provides org.monte.media.av.CodecSpi with org.monte.media.jcodec.codec.PictureCodecSpi, org.monte.media.jcodec.codec.H264CodecSpi;
}

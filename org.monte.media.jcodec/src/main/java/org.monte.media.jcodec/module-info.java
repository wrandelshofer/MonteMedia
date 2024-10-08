/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

import org.monte.media.jcodec.h264.JCodecH264CodecSpi;
import org.monte.media.jcodec.h264.JCodecPictureCodecSpi;
import org.monte.media.jcodec.mp4.JCodecMP4WriterSpi;

/**
 * Provides a movie writer that uses the JCodec library.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.media.jcodec {
    requires jcodec;
    requires java.desktop;

    exports org.monte.media.jcodec.h264;
    exports org.monte.media.jcodec.mp4;

    requires org.monte.media;

    provides org.monte.media.av.MovieWriterSpi with JCodecMP4WriterSpi;
    provides org.monte.media.av.CodecSpi with JCodecPictureCodecSpi, JCodecH264CodecSpi;
}

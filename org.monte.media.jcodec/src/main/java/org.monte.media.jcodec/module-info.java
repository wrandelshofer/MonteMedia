/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Provides a movie writer that uses the JCodec library as a third-party artifact.
 * <p>
 * Since the org.monte.media already contains an inlined copy of selected parts of the JCodec library,
 * you will typically not need this module.
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

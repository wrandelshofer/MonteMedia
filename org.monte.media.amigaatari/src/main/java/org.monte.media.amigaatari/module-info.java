/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * A library for processing Amiga and Atari still images, video, audio and meta-data.
 * <p>
 * Supports the following container formats:
 * <table><caption>Supported container formats.</caption>
 * <tr><td>IFF</td><td>Electronic Arts Interchange File Format</td><td>Read,
 * Write</td></tr>
 * </table>
 * <p>
 * Supports the following video encodings:
 * <table><caption>Supported video encodings.</caption>
 * <tr><td>Op5</td><td>ANIM</td><td>Amiga Animation</td><td>Decode,
 * Encode</td></tr>
 * <tr><td>Op7 Short/Long</td><td>ANIM</td><td>Amiga
 * Animation</td><td>Decode</td></tr>
 * <tr><td>Op8 Short/Long</td><td>ANIM</td><td>Amiga
 * Animation</td><td>Decode</td></tr>
 * <tr><td>SEQ</td><td>SEQ</td><td>Atari Cyber Paint
 * Sequence</td><td>Decode</td></tr>
 * </table>
 * <p>
 * Supports the following audio encodings:
 * <table><caption>Supported audio encodings.</caption>
 * <tr><td>8SVX</td><td>8SVX, ANIM</td><td>Pulse Code
 * Modulation</td><td>Decode</td></tr>
 * </table>
 * <p>
 * Provides the following image encodings to javax.imageio:
 * <table><caption>Supported image encodings.</caption>
 * <tr><td>ILBM</td><td>IFF</td><td>Amiga Interleaved Bitmap</td><td>Decode,
 * Encode</td></tr>
 * <tr><td>PBM</td><td>IFF</td><td>Amiga Packed Bitmap</td><td>Decode,
 * Encode</td></tr>
 * <tr><td>PGM</td><td>PGM</td><td>Netpbm grayscale
 * image</td><td>Decode</td></tr>
 * </table>
 *
 * @author Werner Randelshofer
 */
module org.monte.media.amigaatari {
    requires java.desktop;
    requires java.prefs;
    requires org.monte.media;
    requires org.monte.media.swing;

    exports org.monte.media.anim;
    exports org.monte.media.amigabitmap;
    exports org.monte.media.amigabitmap.codec.video;
    exports org.monte.media.eightsvx;
    exports org.monte.media.iff;
    exports org.monte.media.ilbm;
    exports org.monte.media.pbm;
    exports org.monte.media.seq;

    provides org.monte.media.av.CodecSpi with
            org.monte.media.amigabitmap.codec.video.AmigaBitmapCodecSpi;

    provides org.monte.media.av.MovieWriterSpi with
            org.monte.media.anim.ANIMWriterSpi;

    provides javax.imageio.spi.ImageReaderSpi with
            org.monte.media.pbm.PBMImageReaderSpi,
            org.monte.media.ilbm.ILBMImageReaderSpi;
}

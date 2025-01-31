/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * A library for processing still images, video, audio and meta-data.
 * <p>
 * Supports the following container formats:
 * <table><caption>Supported container formats.</caption>
 * <tr><td>RIFF</td><td>Microsoft Resource Interchange File
 * Format</td><td>Read</td></tr>
 * <tr><td>AVI</td><td>Microsoft Audio Video Interchange</td><td>Read,
 * Write</td></tr>
 * <tr><td>MOV</td><td>Apple QuickTime</td><td>Write</td></tr>
 * <tr><td>JFIF</td><td>JPEG File Interchange Format</td><td>Read,
 * Write</td></tr>
 * <tr><td>MP3</td><td>MP3 Elementary Stream</td><td>Read</td></tr>
 * <tr><td>MPO</td><td>MultiPicture Object Format</td><td>Read</td></tr>
 * <tr><td>TIFF</td><td>Tagged Image File Format</td><td>Read, Write</td></tr>
 * </table>
 * <p>
 * Supports the following video encodings:
 * <table><caption>Supported video encodings.</caption>
 * <tr><td>PNG</td><td>AVI, MOV</td><td>Portable Networks
 * Graphics</td><td>Decode, Encode</td></tr>
 * <tr><td>DIB</td><td>AVI</td><td>Microsoft Device Independent
 * Bitmap</td><td>Encode</td></tr>
 * <tr><td>Run Length</td><td>AVI</td><td>Run Length
 * Encoding</td><td>Encode</td></tr>
 * <tr><td>ZMBV</td><td>AVI</td><td>DosBox Capture
 * Codec</td><td>Decode</td></tr>
 * <tr><td>TSCC</td><td>AVI, MOV</td><td>TechSmith Screen Capture
 * Codec</td><td>Decode, Encode</td></tr>
 * <tr><td>MJPG</td><td>AVI, MOV</td><td>Motion JPEG</td><td>Decode,
 * Encode</td></tr>
 * <tr><td>Animation</td><td>MOV</td><td>QuickTime Animation</td><td>Encode</td></tr>
 * <tr><td>Raw</td><td>MOV</td><td>QuickTime RAW</td><td>Encode</td></tr>
 * </table>
 * <p>
 * Supports the following audio encodings:
 * <table><caption>Supported audio encodings.</caption>
 * <tr><td>PCM</td><td>AVI, MOV</td><td>Pulse Code Modulation</td><td>Decode,
 * Encode</td></tr>
 * </table>
 * <p>
 * Provides the following image encodings to javax.imageio:
 * <table><caption>Supported image encodings.</caption>
 * <tr><td>PGM</td><td>PGM</td><td>Netpbm grayscale
 * image</td><td>Decode</td></tr>
 * <tr><td>CMYK</td><td>JPEG</td><td>JPEG CMYK Image</td><td>Decode</td></tr>
 * <tr><td>MPO</td><td>MPO</td><td>MultiPicture Object Format</td><td>Decode</td></tr>
 * </table>
 * <p>
 * Supports the following meta-data encodings:
 * <table><caption>Supported meta-data encodings.</caption>
 * <tr><td>EXIF</td><td>AVI, JPEG, MPO</td><td>Exchangeable Image File
 * Format</td><td>Decode</td></tr>
 * </table>
 *
 * @author Werner Randelshofer
 */
module org.monte.media {
    requires java.desktop;
    requires java.prefs;

    exports org.monte.media.beans;
    exports org.monte.media.av;
    exports org.monte.media.av.codec.audio;
    exports org.monte.media.av.codec.time;
    exports org.monte.media.av.codec.video;
    exports org.monte.media.avi;
    exports org.monte.media.avi.codec.audio;
    exports org.monte.media.avi.codec.video;
    exports org.monte.media.color;
    exports org.monte.media.exception;
    exports org.monte.media.exif;
    exports org.monte.media.image;
    exports org.monte.media.imgseq;
    exports org.monte.media.interpolator;
    exports org.monte.media.io;
    exports org.monte.media.jfif;
    exports org.monte.media.jpeg;
    exports org.monte.media.math;
    exports org.monte.media.mjpg;
    exports org.monte.media.mp3;
    exports org.monte.media.mpo;
    exports org.monte.media.pgm;
    exports org.monte.media.quicktime;
    exports org.monte.media.quicktime.codec.audio;
    exports org.monte.media.mp4.codec.video;
    exports org.monte.media.riff;
    exports org.monte.media.tree;
    exports org.monte.media.tiff;
    exports org.monte.media.util;
    exports org.monte.media.util.stream;
    exports org.monte.media.zipmovie;
    exports org.monte.media.mp4;
    exports org.monte.media.av.codec.text;
    exports org.monte.media.qtff;
    exports org.monte.media.qtff.atom;

    uses org.monte.media.av.CodecSpi;
    uses org.monte.media.av.MovieWriterSpi;
    uses org.monte.media.av.MovieReaderSpi;

    provides org.monte.media.av.CodecSpi with
            org.monte.media.av.codec.video.JPEGCodecSpi,
            org.monte.media.av.codec.video.PNGCodecSpi,
            org.monte.media.av.codec.video.TechSmithCodecSpi,
            org.monte.media.quicktime.codec.audio.QuickTimePCMAudioCodecSpi,
            org.monte.media.quicktime.codec.text.AppleClosedCaptionCodecSpi,
            org.monte.media.quicktime.codec.video.AnimationCodecSpi,
            org.monte.media.quicktime.codec.video.RawCodecSpi,
            org.monte.media.avi.codec.audio.AVIPCMAudioCodecSpi,
            org.monte.media.avi.codec.video.DIBCodecSpi,
            org.monte.media.avi.codec.video.RunLengthCodecSpi,
            org.monte.media.avi.codec.video.ZMBVCodecSpi,
            H264CodecSpi;

    provides org.monte.media.av.MovieWriterSpi with
            org.monte.media.quicktime.QuickTimeWriterSpi,
            org.monte.media.mp4.MP4WriterSpi,
            org.monte.media.avi.AVIWriterSpi,
            org.monte.media.zipmovie.ZipMovieWriterSpi;

    provides org.monte.media.av.MovieReaderSpi with
            org.monte.media.avi.AVIReaderSpi,
            org.monte.media.quicktime.QuickTimeReaderSpi;

    provides javax.imageio.spi.ImageReaderSpi with
            org.monte.media.pgm.PGMImageReaderSpi,
            org.monte.media.jpeg.CMYKJPEGImageReaderSpi,
            org.monte.media.mpo.MPOImageReaderSpi;
}

/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

/**
 * Contains classes for processing audio and video.
 */
module org.monte.media.av {
    requires java.desktop;
    requires transitive org.monte.media.base;
    
    exports org.monte.media.av;
    exports org.monte.media.av.codec.audio;
    exports org.monte.media.av.codec.time;
    exports org.monte.media.av.codec.video;
    exports org.monte.media.concurrent;
    exports org.monte.media.mp3;
    exports org.monte.media.player;
    
    uses org.monte.media.av.CodecSpi;
    provides org.monte.media.av.CodecSpi with
            org.monte.media.av.codec.video.JPEGCodecSpi,
            org.monte.media.av.codec.video.PNGCodecSpi,
            org.monte.media.av.codec.video.TechSmithCodecSpi
            ;
    uses org.monte.media.av.MovieWriterSpi;
    uses org.monte.media.av.MovieReaderSpi;
}

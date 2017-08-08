/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.quicktime {
    requires java.desktop;
    
    requires transitive org.monte.media.av;
    
    exports org.monte.media.quicktime;
    provides org.monte.media.av.CodecSpi with
            org.monte.media.quicktime.codec.audio.QuickTimePCMAudioCodecSpi,
            org.monte.media.quicktime.codec.video.AnimationCodecSpi,
            org.monte.media.quicktime.codec.video.RawCodecSpi
            ;
    provides org.monte.media.av.MovieWriterSpi with org.monte.media.quicktime.QuickTimeWriterSpi;
    provides org.monte.media.av.MovieReaderSpi with org.monte.media.quicktime.QuickTimeReaderSpi;
}

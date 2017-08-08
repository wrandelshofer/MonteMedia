/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.avi {
    requires java.desktop;
    
    requires transitive org.monte.media.av;
    requires transitive org.monte.media.riff;
    
    exports org.monte.media.avi;
    
    provides org.monte.media.av.CodecSpi with
            org.monte.media.avi.codec.audio.AVIPCMAudioCodecSpi,
            org.monte.media.avi.codec.video.DIBCodecSpi,
            org.monte.media.avi.codec.video.RunLengthCodecSpi,
            org.monte.media.avi.codec.video.ZMBVCodecSpi
            ;
    provides org.monte.media.av.MovieWriterSpi with org.monte.media.avi.AVIWriterSpi;
    provides org.monte.media.av.MovieReaderSpi with org.monte.media.avi.AVIReaderSpi;
    
}

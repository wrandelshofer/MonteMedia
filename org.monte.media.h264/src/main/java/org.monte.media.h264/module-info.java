/*
 * @(#)module-info.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

import org.monte.media.h264.codec.video.H264EncoderSpi;

/// A library for encoding videos into the H.264 format.
///
/// @author Werner Randelshofer
module org.monte.media.h264 {
    requires java.desktop;
    requires java.prefs;
    requires org.monte.media;
    requires org.monte.media.swing;

    exports org.monte.media.h264.codec.video;

    provides org.monte.media.av.CodecSpi with H264EncoderSpi;
}

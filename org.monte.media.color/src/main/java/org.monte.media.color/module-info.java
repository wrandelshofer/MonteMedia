/*
 * @(#)module-info.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

/// A library for processing colors.
///
/// @author Werner Randelshofer
module org.monte.media.color {
    requires java.desktop;
    requires java.prefs;
    requires org.monte.media;

    exports org.monte.media.color;
    exports org.monte.media.color.kmeans;
    exports org.monte.media.color.kmeans.algo;
    exports org.monte.media.color.dither;
    exports org.monte.media.color.octree;
    exports org.monte.media.color.quant;
}

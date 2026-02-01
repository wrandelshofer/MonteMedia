/*
 * @(#)module-info.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */


/// Provides readers and writers for the mp3 file format.
///
/// @author Werner Randelshofer
@SuppressWarnings("module")
module org.monte.media.mp3 {
    requires java.desktop;
    requires java.prefs;
    requires org.monte.media;

    exports org.monte.media.mp3;
}

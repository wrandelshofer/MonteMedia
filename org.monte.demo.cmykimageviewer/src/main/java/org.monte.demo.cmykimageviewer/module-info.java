/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/// A program that demonstrates how to read CMYK JPEG images with the Monte Media library.
///
/// @author Werner Randelshofer
module org.monte.demo.cmykimageviewer {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;

    requires org.monte.media;
    requires org.monte.media.color;
    requires org.monte.media.javafx;

    exports org.monte.demo.cmykimageviewer;
}

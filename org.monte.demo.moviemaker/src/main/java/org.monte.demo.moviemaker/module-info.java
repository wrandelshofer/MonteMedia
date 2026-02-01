/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/// A program that demonstrates how to generate movies with the Monte Media library.
///
/// @author Werner Randelshofer
module org.monte.demo.moviemaker {
    requires java.desktop;
    requires java.prefs;

    requires org.monte.media;
    requires org.monte.media.swing;
    requires org.monte.media.mp3;
    requires org.monte.media.quicktime;

    exports org.monte.demo.moviemaker;
}

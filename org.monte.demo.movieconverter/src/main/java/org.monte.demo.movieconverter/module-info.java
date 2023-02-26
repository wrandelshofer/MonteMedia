/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * A program that demonstrates how to convert movies with the Monte Media library.
 *
 * @author Werner Randelshofer
 */
module org.monte.demo.movieconverter {
    requires java.desktop;

    requires org.monte.media;
    requires org.monte.media.swing;

    exports org.monte.demo.movieconverter;
}

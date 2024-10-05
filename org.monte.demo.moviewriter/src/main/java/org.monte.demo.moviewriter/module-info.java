/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * A program that demonstrates how to write AVI files with the Monte Media library.
 *
 * @author Werner Randelshofer
 */
module org.monte.demo.moviewriter {
    requires java.desktop;

    requires org.monte.media;
    requires org.monte.media.jcodec;

    exports org.monte.demo.moviewriter;
}

/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * A collection of programs that demonstrate how to perform file I/O with the Monte Media library.
 *
 * @author Werner Randelshofer
 */
module org.monte.demo.io {
    requires java.desktop;

    requires org.monte.media;

    exports org.monte.demo.io;
}

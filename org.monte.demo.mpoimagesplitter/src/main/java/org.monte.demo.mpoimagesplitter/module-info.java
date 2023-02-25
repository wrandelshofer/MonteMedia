/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * A program that demonstrates how to split a multi-image MPO JPEG file up into single image files with the Monte Media library.
 *
 * @author Werner Randelshofer
 */
module org.monte.demo.mpoimagesplitter {
    requires java.desktop;

    requires org.monte.media;
    requires org.monte.media.swing;

    exports org.monte.demo.mpoimagesplitter;
}

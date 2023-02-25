/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * A program that demonstrates how to record audio with the Monte Media library.
 *
 * @author Werner Randelshofer
 */
module org.monte.demo.audiorecorder {
    requires java.desktop;

    requires org.monte.media;

    exports org.monte.demo.audiorecorder;
}

/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * A program that demonstrates how to implement a screen recorder with the Monte Media library.
 * <p>
 * Please note that the org.monte.media.screenrecorder module provides a
 * fully working screen recorder application.
 * The demo provided in this module contains additional, experimental options.
 *
 * @author Werner Randelshofer
 */
module org.monte.demo.screenrecorder {
    requires java.desktop;
    requires java.prefs;

    requires org.monte.media;
    requires org.monte.media.swing;
    requires org.monte.media.screenrecorder;

    exports org.monte.demo.screenrecorder;
}

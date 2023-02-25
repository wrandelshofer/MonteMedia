/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * A screen recorder in pure Java.
 *
 * @author Werner Randelshofer
 */
module org.monte.media.screenrecorder {
    requires java.desktop;
    requires java.prefs;

    requires org.monte.media;
    requires org.monte.media.swing;

    exports org.monte.media.screenrecorder;
    opens org.monte.media.screenrecorder.images;
}

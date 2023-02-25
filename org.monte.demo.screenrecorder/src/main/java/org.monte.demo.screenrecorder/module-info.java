/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Screen Recorder demo.
 * <p>
 * Please note that the org.monte.media.screenrecorder module provides a
 * fully working screen recorder application.
 * The demo provided in this module contains additional, experimental options.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.demo.screenrecorder {
    requires java.desktop;
    requires java.prefs;

    requires org.monte.media;
    requires org.monte.media.swing;
    requires org.monte.media.screenrecorder;

    exports org.monte.demo.screenrecorder;
}

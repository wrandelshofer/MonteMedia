/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Provides Swing classes for use with the Monte Media library.
 *
 * @author Werner Randelshofer
 */
module org.monte.media.swing {
    requires java.desktop;
    requires java.prefs;
    requires org.monte.media;

    exports org.monte.media.player;
    exports org.monte.media.swing;
    exports org.monte.media.swing.border;
    exports org.monte.media.swing.datatransfer;
    exports org.monte.media.swing.movie;
    exports org.monte.media.swing.plaf;
    exports org.monte.media.swing.player;

    opens org.monte.media.player;
    opens org.monte.media.swing.images;
    opens org.monte.media.swing.player.images;
}

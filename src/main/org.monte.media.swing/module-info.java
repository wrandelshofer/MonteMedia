/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.swing {
    requires java.desktop;
    
    requires transitive org.monte.media.av;
    requires transitive org.monte.media.img;

    exports org.monte.media.swing;
    exports org.monte.media.swing.border;
    exports org.monte.media.swing.datatransfer;
    exports org.monte.media.swing.movie;
    exports org.monte.media.swing.player;
}

/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.core {
    requires java.desktop;

    exports org.monte.media.beans;
    exports org.monte.media.exception;
    exports org.monte.media.color;
    exports org.monte.media.concurrent;
    exports org.monte.media.gui;
    exports org.monte.media.gui.border;
    exports org.monte.media.gui.datatransfer;
    exports org.monte.media.gui.plaf;
    exports org.monte.media.gui.tree;
    exports org.monte.media.io;
    exports org.monte.media.image;
    exports org.monte.media.math;
    exports org.monte.media.util;
}

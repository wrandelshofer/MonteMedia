/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

/**
 * Contains classes for processing image content.
 */
module org.monte.media.base {
    requires java.desktop;
    
    exports org.monte.media.beans;
    exports org.monte.media.color;
    exports org.monte.media.exception;
    exports org.monte.media.image;
    exports org.monte.media.io;
    exports org.monte.media.math;
    exports org.monte.media.tree;
    exports org.monte.media.util;
    exports org.monte.media.util.stream;
}

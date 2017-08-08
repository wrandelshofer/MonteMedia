/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

/**
 * Contains classes for processing image content.
 */
module org.monte.media.img {
    requires java.desktop;

    requires transitive org.monte.media.base;
    requires transitive org.monte.media.riff;

    exports org.monte.media.jpeg;
    exports org.monte.media.mpo;
    exports org.monte.media.tiff;
    exports org.monte.media.exif;
    exports org.monte.media.jfif;
}

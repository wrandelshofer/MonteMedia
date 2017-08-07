/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.exif {
    requires java.desktop;

    requires transitive org.monte.media.core;
    requires transitive org.monte.media.jfif;
    requires transitive org.monte.media.riff;
    requires transitive org.monte.media.tiff;
    
    exports org.monte.media.exif;
}

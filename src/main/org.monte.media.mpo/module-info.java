/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.mpo {
    requires java.desktop;

    requires org.monte.media.exif;
    requires org.monte.media.jpeg;

    exports org.monte.media.mpo;
}

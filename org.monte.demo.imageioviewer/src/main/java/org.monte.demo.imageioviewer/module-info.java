/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Image IO viewer demo.
 *
 * @author Werner Randelshofer
 */
module org.monte.demo.imageioviewer {
    requires java.desktop;

    requires org.monte.media;
    requires org.monte.media.swing;

    exports org.monte.demo.imageioviewer;
}

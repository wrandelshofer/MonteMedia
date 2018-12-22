/* @(#)module-info.java
 * Copyright © 2018 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Provides utility classes for JavaFX.
 * 
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.media.javafx {
    requires java.desktop;
    requires org.monte.media;
    requires javafx.swing;

    exports org.monte.media.javafx;
}

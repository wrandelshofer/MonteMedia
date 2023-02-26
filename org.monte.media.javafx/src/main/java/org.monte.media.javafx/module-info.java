/* @(#)module-info.java
 * Copyright © 2018 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Provides JavaFX classes for use with the Monte Media library.
 *
 * @author Werner Randelshofer
 */
module org.monte.media.javafx {
    requires java.desktop;
    requires org.monte.media;
    requires javafx.swing;

    exports org.monte.media.javafx;
}

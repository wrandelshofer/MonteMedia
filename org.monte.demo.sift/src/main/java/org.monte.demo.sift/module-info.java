/* @(#)module-info.java
 * Copyright © 2021 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * A program that demonstrates how to analyze Amiga IFF files with the Monte Media library.
 * <p>
 * Inspired by sift.c for Amiga.
 *
 * @author Werner Randelshofer
 */
module org.monte.demo.sift {
    requires java.desktop;
    requires java.prefs;

    requires org.monte.media;
    requires org.monte.media.amigaatari;

    exports org.monte.demo.sift;
}

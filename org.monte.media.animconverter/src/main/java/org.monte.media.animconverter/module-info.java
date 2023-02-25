/* @(#)module-info.java
 * Copyright © 2021 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Converter for ANIM Amiga Cell Animations.
 *
 * @author Werner Randelshofer
 */
module org.monte.media.animconverter {
    requires java.desktop;

    requires org.monte.media;
    requires org.monte.media.amigaatari;

    exports org.monte.media.animconverter;
}

/* @(#)module-info.java
 * Copyright © 2021 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * A converter for ANIM Amiga Cell Animation files.
 *
 * @author Werner Randelshofer
 */
module org.monte.media.animconverter {
    requires java.desktop;

    requires org.monte.media;
    requires org.monte.media.amigaatari;

    exports org.monte.media.animconverter;
}

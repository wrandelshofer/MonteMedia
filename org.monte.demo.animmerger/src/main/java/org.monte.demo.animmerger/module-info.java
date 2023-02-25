/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Merging of ANIM files demo.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.demo.animmerger {
    requires java.desktop;

    requires org.monte.media;
    requires org.monte.media.amigaatari;
    requires org.monte.media.swing;

    exports org.monte.demo.animmerger;
}

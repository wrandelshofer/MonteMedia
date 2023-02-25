/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Movie converter demo.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.demo.movieconverter {
    requires java.desktop;

    requires org.monte.media;
    requires org.monte.media.swing;

    exports org.monte.demo.movieconverter;
}

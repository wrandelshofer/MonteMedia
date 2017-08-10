/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * CMYK image viewer demo.
 * 
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.demo.cmykimageviewer {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;
    
    requires org.monte.media;
    
    exports org.monte.demo.cmykimageviewer;
}

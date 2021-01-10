/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Image IO viewer demo.
 * 
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.demo.imageioviewer {
    requires java.desktop;
    
    requires org.monte.media;
    
    exports org.monte.demo.imageioviewer;
}

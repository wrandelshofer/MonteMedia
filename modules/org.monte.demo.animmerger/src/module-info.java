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
    
    exports org.monte.demo.animmerger;
}

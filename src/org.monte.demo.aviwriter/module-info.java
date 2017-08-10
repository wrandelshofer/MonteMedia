/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * AVIWriter demo.
 * 
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.demo.aviwriter {
    requires java.desktop;
    
    requires org.monte.media;
    
    exports org.monte.demo.aviwriter;
}

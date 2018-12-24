/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * JMF TSCC demo.
 * 
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.demo.jmftsccdemo {
    requires jmf;
    requires java.desktop;
    
    requires org.monte.media;

    exports org.monte.jmftsccdemo;
}

/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.demo.iodemo {
    requires java.desktop;
    
    requires org.monte.media.av;
    requires org.monte.media.img;
    
    exports org.monte.iodemo;
}

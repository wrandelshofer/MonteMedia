/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.cmykdemo {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;
    
    requires org.monte.media.jpeg;
    requires org.monte.media.misc;
    
    exports org.monte.cmykdemo;
}

/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */

module org.monte.demo.cmykdemo {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;
    
    requires org.monte.media;
    
    exports org.monte.cmykdemo;
}

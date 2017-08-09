/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */

module org.monte.demo.cmykimageviewer {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;
    
    requires org.monte.media;
    
    exports org.monte.demo.cmykimageviewer;
}

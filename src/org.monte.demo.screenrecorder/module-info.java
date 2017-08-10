/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

module org.monte.demo.screenrecorder {
    requires java.desktop;
    requires java.prefs;
    
    requires org.monte.media;
    
    exports org.monte.demo.screenrecorder;
}

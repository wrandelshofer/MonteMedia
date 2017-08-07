/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.screenrecorder {
    requires java.desktop;
    requires java.prefs;
    
    requires org.monte.media.avimovie;
    requires org.monte.media.quicktimemovie;
    requires org.monte.media.misc;
    
    exports org.monte.screenrecorder;
}

/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Screen Recorder demo.
 * <p>
 * Please note that the org.monte.media module includes a screen recorder application. 
 * The demo provided in this module may contain additional, experimental options.
 * 
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.demo.screenrecorder {
    requires java.desktop;
    requires java.prefs;
    
    requires org.monte.media;
    
    exports org.monte.demo.screenrecorder;
}

/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.moviemaker {
    requires java.desktop;
    requires java.prefs;
    
    requires org.monte.media.quicktime;    
    requires org.monte.media.mpthree;    
    requires org.monte.media.movie;    
    
    exports org.monte.moviemaker;    
}

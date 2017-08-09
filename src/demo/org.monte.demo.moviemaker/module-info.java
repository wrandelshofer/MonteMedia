/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.demo.moviemaker {
    requires java.desktop;
    requires java.prefs;
    
    requires org.monte.media;    
    
    exports org.monte.moviemaker;    
}

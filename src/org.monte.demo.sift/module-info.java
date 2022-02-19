/* @(#)module-info.java
 * Copyright © 2021 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Takes an IFF file and tells you what's in it.
 * <p>
 * Inspired by sift.c for Amiga.
 * 
 * @author Werner Randelshofer
 */
module org.monte.demo.sift {
    requires java.desktop;
    requires java.prefs;
    
    requires org.monte.media;
    
    exports org.monte.demo.sift;
}

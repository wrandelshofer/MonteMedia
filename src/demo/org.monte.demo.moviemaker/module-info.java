/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */

module org.monte.demo.moviemaker {
    requires java.desktop;
    requires java.prefs;
    
    requires org.monte.media;    
    
    exports org.monte.demo.moviemaker;    
}

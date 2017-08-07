/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.avimovie {
    requires java.desktop;
    
    requires transitive org.monte.media.avi;
    requires transitive org.monte.media.movie;

    exports org.monte.media.avimovie;
}

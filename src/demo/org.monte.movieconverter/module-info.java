/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.movieconverter {
    requires java.desktop;
    
    requires org.monte.media.movie;

    exports org.monte.movieconverter;
}

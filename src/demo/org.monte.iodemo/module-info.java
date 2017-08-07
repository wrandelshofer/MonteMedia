/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.iodemo {
    requires java.desktop;
    
    requires org.monte.media.player;
    requires org.monte.media.movie;
    requires org.monte.media.misc;
    
    exports org.monte.iodemo;
}

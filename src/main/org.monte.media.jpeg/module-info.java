/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.jpeg {
    requires java.desktop;

    requires transitive org.monte.media.core;
    requires transitive org.monte.media.jfif;
    
    exports org.monte.media.jpeg;
}

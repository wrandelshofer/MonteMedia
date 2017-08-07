/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.tiff {
    requires java.desktop;
    
    requires transitive org.monte.media.core;
    
    exports org.monte.media.tiff;
}

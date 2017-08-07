/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.codec {
    requires java.desktop;
    
    requires transitive org.monte.media.core;
    
    exports org.monte.media.audio;
    exports org.monte.media.codec;
    exports org.monte.media.video;
    exports org.monte.media.converter;
    exports org.monte.media.interpolator;
}

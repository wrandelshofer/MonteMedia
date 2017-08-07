/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.player {
    requires java.desktop;
    
    requires transitive org.monte.media.core;
    requires transitive org.monte.media.codec;
    
    exports org.monte.media.player;
    exports org.monte.media.player.gui;
}

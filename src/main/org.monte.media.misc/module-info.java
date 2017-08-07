/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.misc {
    requires java.desktop;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.swing;
    
    requires transitive org.monte.media.core;
    requires transitive org.monte.media.codec;
    requires transitive org.monte.media.movie;
    
    exports org.monte.media.binary;
    exports org.monte.media.fx;
    exports org.monte.media.imgseq;
}

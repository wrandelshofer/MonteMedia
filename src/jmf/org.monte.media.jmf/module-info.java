/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */

module org.monte.media.jmf {
    requires jmf;
    requires java.desktop;
    
    requires org.monte.media;
    
    exports org.monte.media.jmf.codec;
    exports org.monte.media.jmf.codec.video;
}

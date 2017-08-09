/* @(#)module-info.java
 * Copyright © 2007 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

module org.monte.media.jmf {
    requires org.monte.media;
    
    exports org.monte.media.jmf.codec;
    exports org.monte.media.jmf.codec.video;
    exports org.monte.media.jmf.jpeg;
}

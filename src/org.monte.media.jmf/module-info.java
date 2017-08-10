/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Provides codecs to the Java Media Framework (JMF).
 * <p>
 * Please note that you must register the codecs with the JMF Registry application to make them
 * available in JMF.
 * 
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.media.jmf {
    requires jmf;
    requires java.desktop;
    
    requires org.monte.media;
    
    exports org.monte.media.jmf.codec;
    exports org.monte.media.jmf.codec.video;
}

/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Audio recorder demo.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
module org.monte.demo.audiorecorder {
    requires java.desktop;

    requires org.monte.media;

    exports org.monte.demo.audiorecorder;
}

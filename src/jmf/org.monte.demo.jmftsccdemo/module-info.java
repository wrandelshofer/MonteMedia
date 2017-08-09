/* @(#)module-info.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */

module org.monte.demo.jmftsccdemo {
    requires jmf;
    requires java.desktop;
    
    requires org.monte.media;

    exports org.monte.jmftsccdemo;
}

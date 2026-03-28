/* @(#)module-info.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */


/// A program that demonstrates how to play movies with the Monte Media library.
///
/// @author Werner Randelshofer
module org.monte.demo.javafx.colorquantizer {
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.fxml;
    requires java.desktop;
    requires org.monte.media;
    requires org.monte.media.amigaatari;
    requires org.monte.media.color;
    requires javafx.controls;
    requires javafx.swing;

    exports org.monte.demo.javafx.cubescene to javafx.graphics;
    exports org.monte.demo.javafx.colorquantizer to javafx.graphics;
    opens org.monte.demo.javafx.colorquantizer to javafx.fxml;
    exports org.monte.demo.javafx.colorquantizer.model to javafx.graphics;
    opens org.monte.demo.javafx.colorquantizer.model to javafx.fxml;
    exports org.monte.demo.javafx.colorquantizer.codec to javafx.graphics;
    opens org.monte.demo.javafx.colorquantizer.codec to javafx.fxml;
    exports org.monte.demo.javafx.colorquantizer.scene3d to javafx.graphics;
    opens org.monte.demo.javafx.colorquantizer.scene3d to javafx.fxml;

}

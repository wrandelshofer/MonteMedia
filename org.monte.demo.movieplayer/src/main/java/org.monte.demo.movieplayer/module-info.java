/* @(#)module-info.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * A program that demonstrates how to play movies with the Monte Media library.
 *
 * @author Werner Randelshofer
 */
module org.monte.demo.movieplayer {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;

    exports org.monte.demo.movieplayer;
    opens org.monte.demo.movieplayer to javafx.fxml;
    exports org.monte.demo.movieplayer.fxplayer;
    opens org.monte.demo.movieplayer.fxplayer to javafx.fxml;
}

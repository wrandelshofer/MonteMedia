/* @(#)module-info.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

import org.monte.demo.javafx.movieplayer.monteplayer.WritableImageCodecSpi;

/**
 * A program that demonstrates how to play movies with the Monte Media library.
 *
 * @author Werner Randelshofer
 */
module org.monte.demo.javafx.movieplayer {
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires javafx.swing;
    requires java.desktop;
    requires org.monte.media;

    exports org.monte.demo.javafx.movieplayer;
    opens org.monte.demo.javafx.movieplayer to javafx.fxml;
    exports org.monte.demo.javafx.movieplayer.fxplayer;
    opens org.monte.demo.javafx.movieplayer.fxplayer to javafx.fxml;
    exports org.monte.demo.javafx.movieplayer.monteplayer;
    opens org.monte.demo.javafx.movieplayer.monteplayer to javafx.fxml;
    exports org.monte.demo.javafx.movieplayer.model;
    opens org.monte.demo.javafx.movieplayer.model to javafx.fxml;

    provides org.monte.media.av.CodecSpi with WritableImageCodecSpi;
}

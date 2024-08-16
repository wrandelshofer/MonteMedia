/* @(#)module-info.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

import org.monte.demo.movieplayer.monteplayer.WritableImageCodecSpi;

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
    requires javafx.swing;
    requires java.desktop;
    requires org.monte.media;

    exports org.monte.demo.movieplayer;
    opens org.monte.demo.movieplayer to javafx.fxml;
    exports org.monte.demo.movieplayer.fxplayer;
    opens org.monte.demo.movieplayer.fxplayer to javafx.fxml;
    exports org.monte.demo.movieplayer.monteplayer;
    opens org.monte.demo.movieplayer.monteplayer to javafx.fxml;
    exports org.monte.demo.movieplayer.model;
    opens org.monte.demo.movieplayer.model to javafx.fxml;

    provides org.monte.media.av.CodecSpi with WritableImageCodecSpi;
}

/*
 * @(#)Main.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        MainWindowController.startStage(stage);
    }
}

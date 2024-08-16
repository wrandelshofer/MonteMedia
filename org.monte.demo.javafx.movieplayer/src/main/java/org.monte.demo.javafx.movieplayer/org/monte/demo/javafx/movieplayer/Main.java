/*
 * @(#)Main.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class Main extends Application {
    public static void main(String... args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
        ResourceBundle labels = ResourceBundle.getBundle("org.monte.demo.javafx.movieplayer.Labels");
        loader.setResources(labels);
        Parent root = loader.load();
        MainWindowController controller = loader.getController();


        stage.titleProperty().bind(controller.fileProperty().map(f ->
                labels.getString("application.name") + (f == null ? "" : ": " + f.getName())
        ));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("controls.css").toString());
        stage.setScene(scene);
        stage.setWidth(400);
        stage.setHeight(300);
        stage.show();
    }
}

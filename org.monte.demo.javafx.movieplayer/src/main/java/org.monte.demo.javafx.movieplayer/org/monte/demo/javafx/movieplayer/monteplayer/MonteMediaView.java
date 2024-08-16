/**
 * Sample Skeleton for 'MonteMediaView.fxml' Controller Class
 */

package org.monte.demo.javafx.movieplayer.monteplayer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MonteMediaView {
    private final ObjectProperty<MonteMedia> media = new SimpleObjectProperty<>();
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="imageView"
    private ImageView imageView; // Value injected by FXMLLoader

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert imageView != null : "fx:id=\"imageView\" was not injected: check your FXML file 'MonteMediaView.fxml'.";

        media.addListener((o, oldv, newv) -> {
            if (oldv != null) {
                imageView.imageProperty().unbind();
            }
            if (newv != null) {
                imageView.imageProperty().bind(newv.videoImageProperty());
                imageView.fitWidthProperty().bind(newv.widthProperty());
                imageView.fitHeightProperty().bind(newv.heightProperty());
            }
        });
    }

    public MonteMedia getMedia() {
        return media.get();
    }

    public void setMedia(MonteMedia newValue) {
        media.set(newValue);
    }

    public ObjectProperty<MonteMedia> mediaProperty() {
        return media;
    }

    public ImageView getRoot() {
        return imageView;
    }

    public static MonteMediaView newVideoView() {
        FXMLLoader loader = new FXMLLoader(MonteMediaView.class.getResource("MonteMediaView.fxml"));
        ResourceBundle labels = ResourceBundle.getBundle("org.monte.demo.javafx.movieplayer.Labels");
        loader.setResources(labels);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MonteMediaView controller = loader.getController();
        return controller;
    }
}

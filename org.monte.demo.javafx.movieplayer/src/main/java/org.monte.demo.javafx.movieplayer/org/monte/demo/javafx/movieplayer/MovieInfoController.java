/*
 * @(#)MovieInfoController.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer;


import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.monte.demo.javafx.movieplayer.model.MediaInterface;
import org.monte.demo.javafx.movieplayer.model.TrackInterface;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

public class MovieInfoController {
    record InfoItem(StringProperty keyProperty, StringProperty valueProperty) {
        public InfoItem(String k, String v) {
            this(new SimpleStringProperty(k), new SimpleStringProperty(v));
        }
    }

    private final InvalidationListener invalidationListener = new InvalidationListener() {
        @Override
        public void invalidated(Observable observable) {
            var m = media.get();
            if (m != null) updateTable(m);
        }
    };

    private final ObjectProperty<MediaInterface> media = new SimpleObjectProperty<>();
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="tableColumn1"
    private TableColumn<InfoItem, String> keyColumn; // Value injected by FXMLLoader

    @FXML // fx:id="valueColumn"
    private TableColumn<InfoItem, String> valueColumn; // Value injected by FXMLLoader

    @FXML // fx:id="tableView"
    private TableView<InfoItem> tableView; // Value injected by FXMLLoader

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert keyColumn != null : "fx:id=\"keyColumn\" was not injected: check your FXML file 'MovieInfoPane.fxml'.";
        assert valueColumn != null : "fx:id=\"valueColumn\" was not injected: check your FXML file 'MovieInfoPane.fxml'.";
        assert tableView != null : "fx:id=\"tableView\" was not injected: check your FXML file 'MovieInfoPane.fxml'.";

        keyColumn.setCellValueFactory(f -> f.getValue().keyProperty());
        valueColumn.setCellValueFactory(f -> f.getValue().valueProperty());

        media.addListener((o, oldMedia, newMedia) -> {
            if (oldMedia != null) {
                oldMedia.getTracks().removeListener(invalidationListener);
                oldMedia.getMetadata().removeListener(invalidationListener);
            }
            if (newMedia != null) {
                newMedia.getTracks().addListener(invalidationListener);
                newMedia.getMetadata().addListener(invalidationListener);
            }
            invalidationListener.invalidated(media);
        });
    }

    public Parent getRoot() {
        return tableView;
    }

    private void updateTable(MediaInterface media) {
        ObservableList<InfoItem> items = tableView.getItems();
        items.clear();
        items.add(new InfoItem("Player ", media.getClass().getSimpleName()));
        for (TrackInterface track : media.getTracks()) {
            items.add(new InfoItem("Track #" + track.getTrackID(), track.getName()));
            track.getMetadata().entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                    .forEach(entry -> items.add(new InfoItem("  " + entry.getKey(), getStringForValue(entry))));
        }

        media.getMetadata().entrySet().stream().sorted(Comparator.comparing(Map.Entry::getKey))
                .forEach(entry -> items.add(new InfoItem(entry.getKey(), getStringForValue(entry))));

        for (InfoItem item : items) {
            System.out.println(item);
        }
    }

    private static String getStringForValue(Map.Entry<String, Object> entry) {
        String string = Objects.toString(entry.getValue());
        if ("encoding".equals(entry.getKey()) && entry.getValue() instanceof String s) {
            boolean isPrintable = true;
            for (char c : string.toCharArray()) {
                if (Character.isISOControl(c)) {
                    isPrintable = false;
                    break;
                }
            }
            if (!isPrintable) {
                var buf = new StringBuffer();
                buf.append("0x");
                for (char ch : s.toCharArray()) {
                    if (ch < 16) buf.append('0');
                    buf.append(Integer.toHexString(ch));
                }
                string = buf.toString();
            }
        }
        return string;
    }

    public ObjectProperty<MediaInterface> mediaProperty() {
        return media;
    }

    public static MovieInfoController createMovieInfoController() {
        try {
            FXMLLoader loader = new FXMLLoader(MovieInfoController.class.getResource("MovieInfoPane.fxml"));
            ResourceBundle labels = ResourceBundle.getBundle("org.monte.demo.javafx.movieplayer.Labels");
            //loader.setRoot(new GridPane());
            loader.setResources(labels);
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

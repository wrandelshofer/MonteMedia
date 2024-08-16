/*
 * @(#)FXMedia.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.fxplayer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.media.Media;
import javafx.util.Duration;
import org.monte.demo.movieplayer.model.MediaInterface;
import org.monte.demo.movieplayer.model.TrackInterface;

/**
 * Adapter for JavaFX {@link Media}.
 */
public class FXMedia implements MediaInterface {
    private final Media media;

    public FXMedia(Media media) {
        this.media = media;
    }

    @Override
    public ReadOnlyObjectProperty<Duration> durationProperty() {
        return media.durationProperty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ReadOnlyObjectProperty<Throwable> errorProperty() {
        return (ReadOnlyObjectProperty<Throwable>) (ReadOnlyObjectProperty<?>) media.errorProperty();
    }

    @Override
    public ReadOnlyIntegerProperty heightProperty() {
        return media.heightProperty();
    }

    @Override
    public ReadOnlyIntegerProperty widthProperty() {
        return media.widthProperty();
    }

    @Override
    public ObjectProperty<Runnable> onErrorProperty() {
        return media.onErrorProperty();
    }

    @Override
    public ObservableMap<String, Duration> getMarkers() {
        return media.getMarkers();
    }

    @Override
    public ObservableMap<String, Object> getMetadata() {
        return media.getMetadata();
    }

    @Override
    public String getSource() {
        return media.getSource();
    }

    @Override
    public ObservableList<TrackInterface> getTracks() {
        ObservableList<TrackInterface> result = FXCollections.observableArrayList();
        media.getTracks().forEach(t -> result.add(new FXTrack(t)));
        return result;
    }


}

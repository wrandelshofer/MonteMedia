/*
 * @(#)AbstractMedia.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.util.Duration;

import java.util.LinkedHashMap;

public abstract class AbstractMedia implements MediaInterface {
    protected final ReadOnlyObjectWrapper<Duration> duration = new ReadOnlyObjectWrapper<>();
    protected final ReadOnlyObjectWrapper<Throwable> error = new ReadOnlyObjectWrapper<>();
    protected final ReadOnlyIntegerWrapper height = new ReadOnlyIntegerWrapper();
    protected final ReadOnlyIntegerWrapper width = new ReadOnlyIntegerWrapper();
    protected final ObjectProperty<Runnable> onError = new SimpleObjectProperty<>();
    protected final ObservableMap<String, Duration> markers = FXCollections.observableMap(new LinkedHashMap<>());
    protected final ObservableMap<String, Object> metadata = FXCollections.observableMap(new LinkedHashMap<>());
    protected final ObservableList<TrackInterface> tracks = FXCollections.observableArrayList();
    protected final String source;

    protected AbstractMedia(String source) {
        this.source = source;
    }

    @Override
    public ReadOnlyObjectProperty<Duration> durationProperty() {
        return duration.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyObjectProperty<Throwable> errorProperty() {
        return error.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyIntegerProperty heightProperty() {
        return height.getReadOnlyProperty();
    }

    @Override
    public ReadOnlyIntegerProperty widthProperty() {
        return width.getReadOnlyProperty();
    }

    @Override
    public ObjectProperty<Runnable> onErrorProperty() {
        return onError;
    }

    @Override
    public ObservableMap<String, Duration> getMarkers() {
        return markers;
    }

    @Override
    public ObservableMap<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public ObservableList<TrackInterface> getTracks() {
        return tracks;
    }
}

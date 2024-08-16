/*
 * @(#)MediaInterface.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.util.Duration;

/**
 * Interface for {@link javafx.scene.media.Media}.
 */
public interface MediaInterface {
    ReadOnlyObjectProperty<Duration> durationProperty();

    ReadOnlyObjectProperty<Throwable> errorProperty();

    ReadOnlyIntegerProperty heightProperty();

    ReadOnlyIntegerProperty widthProperty();

    ObjectProperty<Runnable> onErrorProperty();

    default Duration getDuration() {
        return durationProperty().get();
    }

    default Throwable getError() {
        return errorProperty().get();
    }

    default int getHeight() {
        return heightProperty().get();
    }

    default int getWidth() {
        return widthProperty().get();
    }

    ObservableMap<String, Duration> getMarkers();

    ObservableMap<String, Object> getMetadata();

    default Runnable getOnError() {
        return onErrorProperty().get();
    }

    default void setOnError(Runnable r) {
        onErrorProperty().set(r);
    }

    String getSource();

    ObservableList<TrackInterface> getTracks();

}

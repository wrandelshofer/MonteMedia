/*
 * @(#)FXMedia.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.fxplayer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.media.AudioTrack;
import javafx.scene.media.Media;
import javafx.scene.media.SubtitleTrack;
import javafx.scene.media.Track;
import javafx.scene.media.VideoTrack;
import javafx.util.Duration;
import org.monte.demo.javafx.movieplayer.model.MediaInterface;
import org.monte.demo.javafx.movieplayer.model.TrackInterface;

import java.util.Objects;

/**
 * Adapter for JavaFX {@link Media}.
 */
public class FXMedia implements MediaInterface {
    private final ObservableList<TrackInterface> tracks = FXCollections.observableArrayList();

    private final Media media;

    public FXMedia(Media media) {
        this.media = media;
        for (Track track : media.getTracks()) {
            if (Objects.requireNonNull(track) instanceof VideoTrack) {
                VideoTrack t = (VideoTrack) Objects.requireNonNull(track);
                tracks.add(new FXVideoTrack(t));
            } else if (track instanceof AudioTrack) {
                AudioTrack t = (AudioTrack) track;
                tracks.add(new FXAudioTrack(t));
            } else if (track instanceof SubtitleTrack) {
                SubtitleTrack t = (SubtitleTrack) track;
                tracks.add(new FXSubtitleTrack(t));
            } else {
                tracks.add(new FXTrack(track));
            }
        }

        media.getTracks().addListener(new ListChangeListener<Track>() {
            @Override
            public void onChanged(Change<? extends Track> c) {
                while (c.next()) {
                    if (c.wasRemoved()) {
                        tracks.subList(c.getFrom(), c.getTo()).clear();
                        ;
                    }
                    if (c.wasAdded()) {
                        int i = c.getFrom();
                        for (Track track : c.getAddedSubList()) {
                            if (Objects.requireNonNull(track) instanceof VideoTrack) {
                                VideoTrack t = (VideoTrack) Objects.requireNonNull(track);
                                tracks.add(i, new FXVideoTrack(t));
                            } else if (track instanceof AudioTrack) {
                                AudioTrack t = (AudioTrack) track;
                                tracks.add(i, new FXAudioTrack(t));
                            } else if (track instanceof SubtitleTrack) {
                                SubtitleTrack t = (SubtitleTrack) track;
                                tracks.add(i, new FXSubtitleTrack(t));
                            } else {
                                tracks.add(i, new FXTrack(track));
                            }
                            i++;
                        }
                    }
                }
            }
        });
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
        return tracks;
    }


}

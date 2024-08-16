/**
 * Sample Skeleton for 'PlayerControls.fxml' Controller Class
 */

package org.monte.demo.javafx.movieplayer;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.monte.demo.javafx.movieplayer.model.MediaPlayerInterface;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

public class PlayerControlsController {
    private final ObjectProperty<MediaPlayerInterface> player = new SimpleObjectProperty<>();

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="currentTimeLabel"
    private Label currentTimeLabel; // Value injected by FXMLLoader

    @FXML // fx:id="playButton"
    private ToggleButton playButton; // Value injected by FXMLLoader

    @FXML // fx:id="root"
    private HBox root; // Value injected by FXMLLoader

    @FXML // fx:id="timeSlider"
    private Slider timeSlider; // Value injected by FXMLLoader

    @FXML // fx:id="totalDurationLabel"
    private Label totalDurationLabel; // Value injected by FXMLLoader
    private ChangeListener<MediaPlayer.Status> statusChangeListener = (o, old, newv) -> playButton.setSelected(newv == MediaPlayer.Status.PLAYING);

    @FXML
    void seekLast(ActionEvent event) {
        MediaPlayerInterface p = getPlayer();
        if (p != null) {
            p.seek(p.getTotalDuration());
        }
    }

    @FXML
    void seekFirst(ActionEvent event) {
        MediaPlayerInterface p = getPlayer();
        if (p != null) {
            p.seek(Duration.ZERO);
        }
    }

    @FXML
    void seekNext(ActionEvent event) {
        MediaPlayerInterface p = getPlayer();
        if (p != null) {
            p.seek(p.getCurrentTime().add(Duration.seconds(1.0 / 10)));
        }
    }

    @FXML
    void seekPrevious(ActionEvent event) {
        MediaPlayerInterface p = getPlayer();
        if (p != null) {
            p.seek(p.getCurrentTime().subtract(Duration.seconds(1.0 / 10)));
        }
    }

    @FXML
    void togglePlay(ActionEvent event) {
        MediaPlayerInterface p = getPlayer();
        if (p != null) {
            switch (p.getStatus()) {
                case null -> {
                    // do nothing
                }
                default -> {
                    p.play();
                }
                case PLAYING -> {
                    p.pause();
                }
            }
        }
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert currentTimeLabel != null : "fx:id=\"currentTimeLabel\" was not injected: check your FXML file 'PlayerControls.fxml'.";
        assert playButton != null : "fx:id=\"playButton\" was not injected: check your FXML file 'PlayerControls.fxml'.";
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'PlayerControls.fxml'.";
        assert timeSlider != null : "fx:id=\"timeSlider\" was not injected: check your FXML file 'PlayerControls.fxml'.";
        assert totalDurationLabel != null : "fx:id=\"totalDurationLabel\" was not injected: check your FXML file 'PlayerControls.fxml'.";

        player.addListener(this::playerChanged);
        timeSlider.valueProperty().addListener(this::timeSliderChanged);
    }

    private void timeSliderChanged(Observable observable, Number oldValue, Number newValue) {
        MediaPlayerInterface p = getPlayer();
        if (p != null && newValue != null && (timeSlider.isPressed())) {
            p.seek(new Duration(newValue.doubleValue()));
        }
    }

    private final ChangeListener<Duration> currentTimeHandler = this::currenTimeChanged;

    private void playerChanged(Observable observable, MediaPlayerInterface oldValue, MediaPlayerInterface newValue) {
        if (oldValue != null) {
            playButton.textProperty().unbind();
            timeSlider.maxProperty().unbind();
            oldValue.currentTimeProperty().removeListener(currentTimeHandler);
            currentTimeLabel.textProperty().unbind();
            totalDurationLabel.textProperty().unbind();
            newValue.statusProperty().removeListener(statusChangeListener);
        }
        if (newValue != null) {
            timeSlider.maxProperty().bind(newValue.totalDurationProperty().map(duration -> duration == null ? 0.0 : duration.toMillis()));
            totalDurationLabel.textProperty().bind(newValue.totalDurationProperty().map(this::toTotalDurationString));
            currentTimeLabel.textProperty().bind(Bindings.createStringBinding(
                    () -> this.toCurrentTimeString(newValue.getCurrentTime(), newValue.getTotalDuration()),
                    newValue.totalDurationProperty(),
                    newValue.currentTimeProperty()
            ));
            newValue.currentTimeProperty().addListener(currentTimeHandler);
            newValue.statusProperty().addListener(statusChangeListener);
        }
    }

    private final static NumberFormat fmt2Digits = NumberFormat.getNumberInstance(Locale.ENGLISH);
    private final static NumberFormat fmt3Digits = NumberFormat.getNumberInstance(Locale.ENGLISH);

    static {
        fmt2Digits.setMinimumIntegerDigits(2);
        fmt2Digits.setMaximumFractionDigits(0);
        fmt2Digits.setGroupingUsed(false);
        fmt3Digits.setMinimumIntegerDigits(3);
        fmt3Digits.setMaximumFractionDigits(0);
        fmt3Digits.setGroupingUsed(false);
    }

    private String toTotalDurationString(Duration duration) {
        if (duration == null || duration.isUnknown()) {
            return resources.getString("duration.unknown");
        }
        if (duration.isIndefinite()) {
            return resources.getString("duration.indefinite");
        }
        StringBuilder buf = new StringBuilder();
        double millis = duration.toMillis();
        int seconds = (int) ((millis / 1000.0) % 60);
        int minutes = (int) ((millis / 60_000.0) % 60);
        int hours = (int) ((millis / 3600_000.0));
        if (hours > 0) {
            buf.append(fmt2Digits.format(hours));
            buf.append(':');
        }
        if (hours > 0 || minutes > 0) {
            buf.append(fmt2Digits.format(minutes));
            buf.append(':');
        }
        buf.append(fmt2Digits.format(seconds));
        int fraction = (int) millis % 1000;
        buf.append('.');
        buf.append(fmt3Digits.format(fraction));

        return buf.toString();
    }

    private String toCurrentTimeString(Duration duration, Duration totalDuration) {
        if (duration == null || duration.isUnknown()) {
            return resources.getString("duration.unknown");
        }
        if (duration.isIndefinite()) {
            return resources.getString("duration.indefinite");
        }
        if (totalDuration == null || totalDuration.isUnknown() || totalDuration.isIndefinite()) {
            totalDuration = Duration.hours(99);
        }
        StringBuilder buf = new StringBuilder();
        double millis = duration.toMillis();
        int seconds = (int) ((millis / 1000.0) % 60);
        int minutes = (int) ((millis / 60_000.0) % 60);
        int hours = (int) ((millis / 3600_000.0));
        double totalMillis = totalDuration.toMillis();
        int totalMinutes = (int) ((totalMillis / 60_000.0) % 60);
        int totalHours = (int) ((totalMillis / 3600_000.0));
        if (totalHours > 0) {
            buf.append(fmt2Digits.format(hours));
            buf.append(':');
        }
        if (totalHours > 0 || totalMinutes > 0) {
            buf.append(fmt2Digits.format(minutes));
            buf.append(':');
        }
        buf.append(fmt2Digits.format(seconds));
        int fraction = (int) millis % 1000;
        buf.append('.');
        buf.append(fmt3Digits.format(fraction));
        return buf.toString();
    }


    private void currenTimeChanged(Observable observable, Duration oldValue, Duration newValue) {
        if (!timeSlider.isPressed() && newValue != null) {
            timeSlider.setValue(newValue.toMillis());
        }
    }

    public HBox getRoot() {
        return root;
    }

    public MediaPlayerInterface getPlayer() {
        return player.get();
    }

    public Property<MediaPlayerInterface> playerProperty() {
        return player;
    }

    public void setPlayer(MediaPlayerInterface p) {
        player.set(p);
    }
}

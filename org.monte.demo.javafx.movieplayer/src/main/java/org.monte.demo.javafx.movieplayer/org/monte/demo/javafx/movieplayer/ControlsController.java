/**
 * Sample Skeleton for 'Controls.fxml' Controller Class
 */

package org.monte.demo.javafx.movieplayer;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.monte.demo.javafx.movieplayer.model.MediaPlayerInterface;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class ControlsController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="backwardButton"
    private Button backwardButton; // Value injected by FXMLLoader

    @FXML // fx:id="controllerPane"
    private GridPane controllerPane; // Value injected by FXMLLoader

    @FXML // fx:id="durationLabel"
    private Label durationLabel; // Value injected by FXMLLoader

    @FXML // fx:id="forwardButton"
    private Button forwardButton; // Value injected by FXMLLoader

    @FXML // fx:id="muteButton"
    private ToggleButton muteButton; // Value injected by FXMLLoader

    @FXML // fx:id="playButton"
    private ToggleButton playButton; // Value injected by FXMLLoader

    @FXML // fx:id="rootPane"
    private AnchorPane rootPane; // Value injected by FXMLLoader

    @FXML // fx:id="timeLabel"
    private Label timeLabel; // Value injected by FXMLLoader

    @FXML // fx:id="timeSlider"
    private Slider timeSlider; // Value injected by FXMLLoader

    @FXML // fx:id="volumeSlider"
    private Slider volumeSlider; // Value injected by FXMLLoader
    private final ObjectProperty<MediaPlayerInterface> player = new SimpleObjectProperty<>();


    @FXML
    void goBackward(ActionEvent event) {
        MediaPlayerInterface p = getPlayer();
        if (p == null) return;

        p.seek(p.getCurrentTime().subtract(
                p.getStatus() == MediaPlayer.Status.PLAYING ? Duration.minutes(1) : Duration.seconds(1)));
    }

    @FXML
    void goForward(ActionEvent event) {
        MediaPlayerInterface p = getPlayer();
        if (p == null) return;
        p.seek(p.getCurrentTime().add(p.getStatus() == MediaPlayer.Status.PLAYING ? Duration.minutes(1) : Duration.seconds(1)));
    }

    public MediaPlayerInterface getPlayer() {
        return player.get();
    }

    public ObjectProperty<MediaPlayerInterface> playerProperty() {
        return player;
    }

    public void setPlayer(MediaPlayerInterface p) {
        this.player.set(p);
    }

    @FXML
    void togglePlayPause(ActionEvent event) {
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
        assert backwardButton != null : "fx:id=\"backwardButton\" was not injected: check your FXML file 'Controls.fxml'.";
        assert controllerPane != null : "fx:id=\"controllerPane\" was not injected: check your FXML file 'Controls.fxml'.";
        assert durationLabel != null : "fx:id=\"durationLabel\" was not injected: check your FXML file 'Controls.fxml'.";
        assert forwardButton != null : "fx:id=\"forwardButton\" was not injected: check your FXML file 'Controls.fxml'.";
        assert muteButton != null : "fx:id=\"muteButton\" was not injected: check your FXML file 'Controls.fxml'.";
        assert playButton != null : "fx:id=\"playButton\" was not injected: check your FXML file 'Controls.fxml'.";
        assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'Controls.fxml'.";
        assert timeLabel != null : "fx:id=\"timeLabel\" was not injected: check your FXML file 'Controls.fxml'.";
        assert timeSlider != null : "fx:id=\"timeSlider\" was not injected: check your FXML file 'Controls.fxml'.";
        assert volumeSlider != null : "fx:id=\"volumeSlider\" was not injected: check your FXML file 'Controls.fxml'.";

        ControllerPaneMouseDraggedHandler dh = new ControllerPaneMouseDraggedHandler(this);

        ControllerPaneVisibleHandler vh = new ControllerPaneVisibleHandler(this);
        player.addListener(this::playerChanged);
        volumeSlider.disableProperty().bind(muteButton.selectedProperty());


        timeSlider.valueProperty().addListener(this::timeSliderChanged);
    }

    private void timeSliderChanged(Observable observable, Number oldValue, Number newValue) {
        MediaPlayerInterface p = getPlayer();
        if (p != null && newValue != null && (timeSlider.isPressed())) {
            p.seek(new Duration(newValue.doubleValue()));
        }
    }

    private final ChangeListener<Duration> currentTimeHandler = this::currentTimeChanged;
    private ChangeListener<MediaPlayer.Status> statusChangeListener = (o, old, newv) -> playButton.setSelected(newv == MediaPlayer.Status.PLAYING);

    private void playerChanged(Observable observable, MediaPlayerInterface oldValue, MediaPlayerInterface newValue) {
        if (oldValue != null) {
            playButton.textProperty().unbind();
            timeSlider.maxProperty().unbind();
            oldValue.currentTimeProperty().removeListener(currentTimeHandler);
            timeLabel.textProperty().unbind();
            durationLabel.textProperty().unbind();
            newValue.statusProperty().removeListener(statusChangeListener);
            volumeSlider.valueProperty().unbindBidirectional(oldValue.volumeProperty());
            muteButton.setDisable(true);
            muteButton.selectedProperty().unbindBidirectional(oldValue.muteProperty());
        }
        if (newValue != null) {
            timeSlider.maxProperty().bind(newValue.totalDurationProperty().map(duration -> duration == null ? 0.0 : duration.toMillis()));
            durationLabel.textProperty().bind(newValue.totalDurationProperty().map(this::toTotalDurationString));
            timeLabel.textProperty().bind(Bindings.createStringBinding(
                    () -> this.toCurrentTimeString(newValue.getCurrentTime(), newValue.getTotalDuration()),
                    newValue.totalDurationProperty(),
                    newValue.currentTimeProperty()
            ));
            newValue.currentTimeProperty().addListener(currentTimeHandler);
            newValue.statusProperty().addListener(statusChangeListener);
            volumeSlider.valueProperty().bindBidirectional(newValue.volumeProperty());
            muteButton.selectedProperty().bindBidirectional(newValue.muteProperty());
            muteButton.setDisable(false);
        }
    }

    private final static NumberFormat fmt2Digits = NumberFormat.getNumberInstance(Locale.ENGLISH);
    private final static NumberFormat fmt3Digits = NumberFormat.getNumberInstance(Locale.ENGLISH);

    private void currentTimeChanged(Observable observable, Duration oldValue, Duration newValue) {
        if (!timeSlider.isPressed() && newValue != null) {
            timeSlider.setValue(newValue.toMillis());
        }
    }

    static {
        fmt2Digits.setMinimumIntegerDigits(2);
        fmt2Digits.setMaximumFractionDigits(0);
        fmt2Digits.setGroupingUsed(false);
        fmt3Digits.setMinimumIntegerDigits(3);
        fmt3Digits.setMaximumFractionDigits(0);
        fmt3Digits.setGroupingUsed(false);
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

    private class ControllerPaneVisibleHandler {
        private Timer timer = new Timer();

        private class ShowHideTask extends TimerTask {
            private final boolean show;
            private volatile boolean cancelled;

            private ShowHideTask(boolean show) {
                this.show = show;
            }

            @Override
            public boolean cancel() {
                cancelled = true;
                return super.cancel();
            }

            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (!cancelled) {
                        controllerPane.setVisible(show);
                    }
                });
            }
        }

        private ShowHideTask currentTask;

        public ControllerPaneVisibleHandler(ControlsController controlsController) {
            rootPane.setOnMouseEntered(this::rootPaneEntered);
            rootPane.setOnMouseExited(this::rootPaneExited);
            controllerPane.setOnMouseEntered(this::controllerPaneEntered);
            rootPane.setOnMouseMoved(this::rootPaneMouseMoved);
            controllerPane.setOnMouseMoved(this::controllerPaneMouseMoved);
        }

        private void controllerPaneMouseMoved(MouseEvent mouseEvent) {
            mouseEvent.consume();
        }

        private void rootPaneMouseMoved(MouseEvent mouseEvent) {
            mouseEvent.consume();
            controllerPane.setVisible(true);
            schedule(false);
        }

        private void cancelScheduled() {
            if (currentTask != null) {
                currentTask.cancel();
                currentTask = null;
            }
        }

        private void schedule(boolean show) {
            cancelScheduled();
            timer.schedule(currentTask = new ShowHideTask(show), 2000);
        }

        private void controllerPaneEntered(MouseEvent mouseEvent) {
            mouseEvent.consume();
            cancelScheduled();
        }

        private void rootPaneExited(MouseEvent mouseEvent) {
            mouseEvent.consume();
            schedule(false);
        }

        private void rootPaneEntered(MouseEvent mouseEvent) {
            mouseEvent.consume();
            cancelScheduled();
            controllerPane.setVisible(true);
        }
    }

    private class ControllerPaneMouseDraggedHandler {

        private double prevMouseX, prevMouseY;

        public ControllerPaneMouseDraggedHandler(ControlsController controlsController) {
            controllerPane.setOnMouseDragged(this::mouseDragged);
            controllerPane.setOnMousePressed(this::mousePressed);

        }

        private void mousePressed(MouseEvent event) {
            event.consume();
            prevMouseX = event.getSceneX();
            prevMouseY = event.getSceneY();
        }

        private void mouseDragged(MouseEvent event) {
            event.consume();
            double sceneX = event.getSceneX();
            double dx = sceneX - prevMouseX;
            double sceneY = event.getSceneY();
            double dy = sceneY - prevMouseY;
            Parent parent = controllerPane.getParent();
            double width = controllerPane.getWidth();
            double height = controllerPane.getHeight();
            double parentWidth, parentHeight;
            if (parent instanceof Pane p) {
                parentWidth = Math.max(p.getWidth(), width);
                parentHeight = Math.max(p.getHeight(), height);
            } else {
                parentHeight = height;
                parentWidth = width;
            }
            controllerPane.setLayoutX(Math.clamp(controllerPane.getLayoutX() + dx, 0, parentWidth - width));
            controllerPane.setLayoutY(Math.clamp(controllerPane.getLayoutY() + dy, 0, parentHeight - height));
            prevMouseX = sceneX;
            prevMouseY = sceneY;
        }
    }

    public Node getRoot() {
        return rootPane;
    }

}

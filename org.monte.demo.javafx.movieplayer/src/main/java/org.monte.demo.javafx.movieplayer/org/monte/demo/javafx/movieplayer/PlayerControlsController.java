/**
 * Sample Skeleton for 'PlayerControls.fxml' Controller Class
 */

package org.monte.demo.javafx.movieplayer;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.monte.demo.javafx.movieplayer.model.AudioTrackInterface;
import org.monte.demo.javafx.movieplayer.model.MediaPlayerInterface;
import org.monte.demo.javafx.movieplayer.model.TrackInterface;

import java.net.URL;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;

public class PlayerControlsController extends AnchorPane {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="backwardButton"
    private Button backwardButton; // Value injected by FXMLLoader

    @FXML // fx:id="controllerPane"
    private GridPane controllerPane; // Value injected by FXMLLoader


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

    private final ObjectProperty<MediaPlayerInterface> player = new SimpleObjectProperty<>();


    @FXML
    void seekBackward(ActionEvent event) {
        MediaPlayerInterface p = getPlayer();
        if (p == null) return;
        Duration seekTime;
        Duration currentTime = p.getCurrentTime();
        if (p.getStatus() == MediaPlayer.Status.PLAYING) {
            seekTime = currentTime.subtract(Duration.minutes(1));
        } else {
            seekTime = p.getFrameBefore(currentTime);
        }
        p.seek(seekTime);
    }

    @FXML
    void seekForward(ActionEvent event) {
        MediaPlayerInterface p = getPlayer();
        if (p == null) return;
        Duration seekTime;
        Duration currentTime = p.getCurrentTime();
        if (p.getStatus() == MediaPlayer.Status.PLAYING) {
            seekTime = currentTime.add(Duration.minutes(1));
        } else {
            seekTime = p.getFrameAfter(currentTime);
        }
        p.seek(seekTime);
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
        assert backwardButton != null : "fx:id=\"backwardButton\" was not injected: check your FXML file 'PlayerControls.fxml'.";
        assert controllerPane != null : "fx:id=\"controllerPane\" was not injected: check your FXML file 'PlayerControls.fxml'.";
        assert forwardButton != null : "fx:id=\"forwardButton\" was not injected: check your FXML file 'PlayerControls.fxml'.";
        assert muteButton != null : "fx:id=\"muteButton\" was not injected: check your FXML file 'PlayerControls.fxml'.";
        assert playButton != null : "fx:id=\"playButton\" was not injected: check your FXML file 'PlayerControls.fxml'.";
        assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'PlayerControls.fxml'.";
        assert timeLabel != null : "fx:id=\"timeLabel\" was not injected: check your FXML file 'PlayerControls.fxml'.";
        assert timeSlider != null : "fx:id=\"timeSlider\" was not injected: check your FXML file 'PlayerControls.fxml'.";

        ControllerPaneMouseDraggedHandler dh = new ControllerPaneMouseDraggedHandler(this);

        ControllerPaneVisibleHandler vh = new ControllerPaneVisibleHandler(this);
        player.addListener(this::playerChanged);

        muteButton.visibleProperty().bind(hasAudio);

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
    private final ReadOnlyBooleanWrapper hasAudio = new ReadOnlyBooleanWrapper();
    private final ListChangeListener<TrackInterface> trackHandler = new ListChangeListener<TrackInterface>() {
        @Override
        public void onChanged(Change<? extends TrackInterface> c) {
            updateHasAudio();
        }
    };

    private void updateHasAudio() {
        boolean hasAudio = false;
        MediaPlayerInterface player = getPlayer();
        if (player != null) {
            for (var e : player.getMedia().getTracks()) {
                if (e instanceof AudioTrackInterface) {
                    hasAudio = true;
                    break;
                }
            }
            PlayerControlsController.this.hasAudio.set(hasAudio);
        }
    }

    private void playerChanged(Observable observable, MediaPlayerInterface oldValue, MediaPlayerInterface newValue) {
        if (oldValue != null) {
            playButton.textProperty().unbind();
            timeSlider.maxProperty().unbind();
            oldValue.currentTimeProperty().removeListener(currentTimeHandler);
            timeLabel.textProperty().unbind();
            newValue.statusProperty().removeListener(statusChangeListener);
            muteButton.selectedProperty().unbindBidirectional(oldValue.muteProperty());
            oldValue.getMedia().getTracks().removeListener(trackHandler);
        }
        if (newValue != null) {
            timeSlider.maxProperty().bind(newValue.totalDurationProperty().map(duration -> duration == null ? 0.0 : duration.toMillis()));
            timeLabel.textProperty().bind(Bindings.createStringBinding(
                    () -> this.toCurrentTimeString(newValue.getCurrentTime(), newValue.getTotalDuration()),
                    newValue.totalDurationProperty(),
                    newValue.currentTimeProperty()
            ));
            newValue.currentTimeProperty().addListener(currentTimeHandler);
            newValue.statusProperty().addListener(statusChangeListener);
            muteButton.selectedProperty().bindBidirectional(newValue.muteProperty());
            newValue.getMedia().getTracks().addListener(trackHandler);
        }
        updateHasAudio();
    }

    private final static NumberFormat fmt2Digits = NumberFormat.getNumberInstance(Locale.ENGLISH);

    private void currentTimeChanged(Observable observable, Duration oldValue, Duration newValue) {
        if (!timeSlider.isPressed() && newValue != null) {
            timeSlider.setValue(newValue.toMillis());
        }
    }

    static {
        fmt2Digits.setMinimumIntegerDigits(2);
        fmt2Digits.setMaximumFractionDigits(0);
        fmt2Digits.setGroupingUsed(false);
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
        int hundreds = (int) (millis / 10) % 100;
        buf.append('.');
        buf.append(fmt2Digits.format(hundreds));
        return buf.toString();
    }


    private class ControllerPaneVisibleHandler {
        private Timer timer;

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
                        if (show) {
                            show();
                        } else {
                            hide();
                        }
                    }
                });
            }

            private void hide() {
                rootPane.getChildren().remove(controllerPane);
            }
        }

        private ShowHideTask currentTask;

        public ControllerPaneVisibleHandler(PlayerControlsController controlsController) {
            rootPane.setOnMouseEntered(this::rootPaneEntered);
            rootPane.setOnMouseExited(this::rootPaneExited);
            controllerPane.setOnMouseEntered(this::controllerPaneEntered);
            rootPane.setOnMouseMoved(this::rootPaneMouseMoved);
            controllerPane.setOnMouseMoved(this::controllerPaneMouseMoved);

            rootPane.sceneProperty().addListener((o, oldv, newv) -> {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
            });

        }

        private void controllerPaneMouseMoved(MouseEvent mouseEvent) {
            mouseEvent.consume();
        }

        private void rootPaneMouseMoved(MouseEvent mouseEvent) {
            mouseEvent.consume();
            show();
            schedule(false, 2000);
        }

        private void show() {
            var b = controllerPane.getLayoutBounds();
            double rw = rootPane.getWidth();
            double rh = rootPane.getHeight();
            if (b.getMinX() < 0 || b.getMinY() < 0 || b.getMaxX() > rh || b.getMaxY() > rw) {
                controllerPane.relocate((rw - b.getWidth()) * 0.5, (rh - b.getHeight()) * 0.75);
            }
            ObservableList<Node> children = rootPane.getChildren();
            if (!children.contains(controllerPane)) {
                children.add(controllerPane);
            }
        }

        private void cancelScheduled() {
            if (currentTask != null) {
                currentTask.cancel();
                currentTask = null;
            }
        }

        private void schedule(boolean show, int millis) {
            cancelScheduled();
            getOrCreateTimer().schedule(currentTask = new ShowHideTask(show), millis);
        }

        private Timer getOrCreateTimer() {
            return timer == null ? timer = new Timer(PlayerControlsController.this + "-timer") : timer;
        }

        private void controllerPaneEntered(MouseEvent mouseEvent) {
            mouseEvent.consume();
            cancelScheduled();
        }

        private void rootPaneExited(MouseEvent mouseEvent) {
            mouseEvent.consume();
            schedule(false, 100);
        }

        private void rootPaneEntered(MouseEvent mouseEvent) {
            mouseEvent.consume();
            cancelScheduled();
            show();
        }
    }

    private class ControllerPaneMouseDraggedHandler {

        private double prevMouseX, prevMouseY;

        public ControllerPaneMouseDraggedHandler(PlayerControlsController controlsController) {
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
            double width = controllerPane.getWidth();
            double height = controllerPane.getHeight();
            double parentWidth = rootPane.getWidth();
            double parentHeight = rootPane.getHeight();

            int minimumAmountVisible = 10;
            double newX = Math.clamp(controllerPane.getLayoutX() + dx, 0 - width + minimumAmountVisible, parentWidth - minimumAmountVisible);
            double newY = Math.clamp(controllerPane.getLayoutY() + dy, 0 - height + minimumAmountVisible, parentHeight - minimumAmountVisible);

            AnchorPane.setLeftAnchor(controllerPane, null);
            AnchorPane.setRightAnchor(controllerPane, null);
            AnchorPane.setTopAnchor(controllerPane, null);
            AnchorPane.setBottomAnchor(controllerPane, null);
            AnchorPane.setLeftAnchor(controllerPane, newX);
            //AnchorPane.setRightAnchor(controllerPane, rootPane.getWidth() - width - newX);
            AnchorPane.setBottomAnchor(controllerPane, rootPane.getHeight() - height - newY);
            prevMouseX = sceneX;
            prevMouseY = sceneY;
        }
    }

    public Node getRoot() {
        return rootPane;
    }

}

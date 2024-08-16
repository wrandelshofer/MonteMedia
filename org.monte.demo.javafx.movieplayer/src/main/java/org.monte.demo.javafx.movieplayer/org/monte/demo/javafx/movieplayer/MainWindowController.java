/*
 * @(#)MainWindowController.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.monte.demo.javafx.movieplayer.fxplayer.FXMedia;
import org.monte.demo.javafx.movieplayer.fxplayer.FXMediaPlayer;
import org.monte.demo.javafx.movieplayer.model.MediaInterface;
import org.monte.demo.javafx.movieplayer.model.MediaPlayerInterface;
import org.monte.demo.javafx.movieplayer.monteplayer.MonteMedia;
import org.monte.demo.javafx.movieplayer.monteplayer.MonteMediaPlayer;
import org.monte.demo.javafx.movieplayer.monteplayer.MonteMediaView;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindowController {
    enum Mode {
        JavaFX, MonteMedia
    }

    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(Mode.MonteMedia);
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML
    private StackPane stackPane; // Value injected by FXMLLoader


    @FXML // fx:id="rootPane"
    private BorderPane rootPane; // Value injected by FXMLLoader
    private FileChooser fileChooser;

    @FXML
    void about(ActionEvent event) {
        new Alert(Alert.AlertType.NONE,
                resources.getString("application.name") + "\n\n" + resources.getString("application.copyright"),
                ButtonType.CLOSE
        ).show();
    }


    @FXML // fx:id="statusBar"
    private HBox statusBar; // Value injected by FXMLLoader

    @FXML
    void close(ActionEvent event) {
        getStage().close();
    }

    @FXML
    void open(ActionEvent event) {
        File file = getFileChooser().showOpenDialog(getStage());
        if (file != null) {
            setFile(file);
        }
    }

    private Stage getStage() {
        Scene scene = rootPane.getScene();
        return scene == null ? null : (Stage) scene.getWindow();
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert stackPane != null : "fx:id=\"stackPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert statusBar != null : "fx:id=\"statusBar\" was not injected: check your FXML file 'MainWindow.fxml'.";

        new DropFileHandler(stackPane, this::setFile);

        fileProperty().addListener(o -> this.createMoviePlayer());
    }


    private void sizeStageToScene() {
        Stage stage = getStage();
        if (stage != null) {
            stage.sizeToScene();
        }
    }

    private final ObjectProperty<File> file = new SimpleObjectProperty<>();
    private final ObjectProperty<MediaPlayerInterface> player = new SimpleObjectProperty<>();

    private FileChooser getFileChooser() {
        if (fileChooser == null) {
            fileChooser = new FileChooser();
        }
        return fileChooser;
    }

    public void setFile(File file) {
        this.file.set(file);
    }

    public File getFile() {
        return file.get();
    }

    public ObjectProperty<File> fileProperty() {
        return file;
    }

    private void createMoviePlayer() {
        createMoviePlayer(0);
    }

    private void createMoviePlayer(int retries) {
        stackPane.getChildren().clear();
        var oldPlayer = player.get();
        if (oldPlayer != null) {
            oldPlayer.dispose();
            player.set(null);
        }
        MediaPlayerInterface player = switch (mode.get()) {
            case JavaFX -> createFXMoviePlayer();

            case MonteMedia -> createMonteMediaPlayer();

        };

        if (player == null || player.getError() != null) {
            retryCreateMoviePlayer(retries);
        } else {
            player.setOnError(() -> retryCreateMoviePlayer(retries));
        }
    }

    private void retryCreateMoviePlayer(int retries) {
        Mode[] values = Mode.values();
        if (retries < values.length) {
            mode.set(values[(mode.get().ordinal() + 1) % values.length]);
            createMoviePlayer(retries + 1);
        } else {
            ObservableList<Node> c = stackPane.getChildren();
            c.clear();
            c.add(new Label(resources.getString("error.creatingPlayer")));
        }
    }


    private MediaPlayerInterface createMonteMediaPlayer() {
        File mediaFile = file.get();
        if (mediaFile == null) {
            return null;
        }
        MonteMedia movie = new MonteMedia(mediaFile);
        MonteMediaPlayer player = new MonteMediaPlayer(movie);
        MonteMediaView monteMediaView = MonteMediaView.newVideoView();
        monteMediaView.setMedia(movie);
        ControlsController playerController = createPlayerController();
        playerController.setPlayer(player);
        showPlayer(monteMediaView.getRoot(), player, movie, playerController);
        return player;
    }


    private MediaPlayerInterface createFXMoviePlayer() {
        File mediaFile = file.get();
        if (mediaFile == null) {
            return null;
        }
        Media media;
        MediaPlayer mediaPlayer;
        MediaView mediaView;
        try {
            String string = mediaFile.toURI().toString();
            media = new Media(string);
            mediaPlayer = new MediaPlayer(media);

            if (mediaPlayer.getError() != null) {
                // Handle synchronous error creating Player.
                mediaPlayer.dispose();
                return null;
            }
            ControlsController playerController = createPlayerController();
            FXMediaPlayer p = new FXMediaPlayer(mediaPlayer);
            player.set(p);
            playerController.setPlayer(p);
            mediaPlayer.setAutoPlay(true);
            mediaView = new MediaView(mediaPlayer);

            showPlayer(mediaView, p, new FXMedia(media), playerController);
            //mediaView.setOnError(t -> leftStatusLabel.setText(resources.getString("error") + t.toString()));

            return p;
        } catch (Exception mediaException) {
            // Handle exception in Media constructor.
        }
        return null;
    }


    private void showPlayer(Node mediaView, MediaPlayerInterface mediaPlayer, MediaInterface media, ControlsController playerController) {
        mediaPlayer.setOnReady(() -> {
            sizeStageToScene();
        });

        stackPane.getChildren().addAll(mediaView, playerController.getRoot());
    }

    private ControlsController createPlayerController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Controls.fxml"));
            ResourceBundle labels = ResourceBundle.getBundle("org.monte.demo.javafx.movieplayer.Labels");
            loader.setResources(labels);
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

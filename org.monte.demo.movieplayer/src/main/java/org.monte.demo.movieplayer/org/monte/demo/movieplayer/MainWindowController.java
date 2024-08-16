/*
 * @(#)MainWindowController.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.monte.demo.movieplayer.fxplayer.FXMedia;
import org.monte.demo.movieplayer.fxplayer.FXMediaPlayer;
import org.monte.demo.movieplayer.model.MediaInterface;
import org.monte.demo.movieplayer.model.MediaPlayerInterface;
import org.monte.demo.movieplayer.monteplayer.MonteMedia;
import org.monte.demo.movieplayer.monteplayer.MonteMediaPlayer;
import org.monte.demo.movieplayer.monteplayer.MonteMediaView;

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

    @FXML // fx:id="centerPane"
    private VBox centerPane; // Value injected by FXMLLoader

    @FXML // fx:id="leftStatusLabel"
    private Label leftStatusLabel; // Value injected by FXMLLoader


    @FXML // fx:id="rightStatusLabel"
    private Label rightStatusLabel; // Value injected by FXMLLoader

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

    @FXML // fx:id="showStatusBarCheckMenuItem"
    private CheckMenuItem showStatusBarCheckMenuItem; // Value injected by FXMLLoader

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
        assert centerPane != null : "fx:id=\"centerPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert leftStatusLabel != null : "fx:id=\"leftStatusLabel\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert rightStatusLabel != null : "fx:id=\"rightStatusLabel\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert showStatusBarCheckMenuItem != null : "fx:id=\"showStatusBarCheckMenuItem\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert statusBar != null : "fx:id=\"statusBar\" was not injected: check your FXML file 'MainWindow.fxml'.";

        new DropFileHandler(centerPane, this::setFile);

        fileProperty().addListener(o -> this.createMoviePlayer());
        showStatusBarCheckMenuItem.selectedProperty().addListener(this::statusBarVisibilityChanged);
        showStatusBarCheckMenuItem.setSelected(false);
    }

    private void statusBarVisibilityChanged(Object o, Boolean oldv, Boolean newv) {
        rootPane.setBottom(newv ? statusBar : null);
        sizeStageToScene();
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
        centerPane.getChildren().clear();
        leftStatusLabel.setText(null);
        var oldPlayer = player.get();
        if (oldPlayer != null) {
            oldPlayer.dispose();
            player.set(null);
        }
        switch (mode.get()) {
            case JavaFX -> {
                createFXMoviePlayer();
            }
            case MonteMedia -> {
                createMonteMediaPlayer();
            }
        }
    }

    private void createMonteMediaPlayer() {
        File mediaFile = file.get();
        if (mediaFile == null) {
            return;
        }
        MonteMedia movie = new MonteMedia(mediaFile);
        MonteMediaPlayer player = new MonteMediaPlayer(movie);
        MonteMediaView monteMediaView = MonteMediaView.newVideoView();
        monteMediaView.setMedia(movie);
        PlayerControlsController playerController = createPlayerController();
        playerController.setPlayer(player);
        showPlayer(monteMediaView.getRoot(), player, movie, playerController);
        movie.errorProperty().addListener((o, oldv, newv) -> leftStatusLabel.setText(resources.getString("error") + newv.toString()));
    }

    private void createFXMoviePlayer() {
        File mediaFile = file.get();
        if (mediaFile == null) {
            return;
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
                leftStatusLabel.setText(resources.getString("error") + mediaPlayer.getError().getMessage());
                mediaPlayer.dispose();
                return;
            }
            PlayerControlsController playerController = createPlayerController();
            FXMediaPlayer p = new FXMediaPlayer(mediaPlayer);
            player.set(p);
            playerController.setPlayer(p);
            mediaPlayer.setAutoPlay(true);
            mediaView = new MediaView(mediaPlayer);

            showPlayer(mediaView, p, new FXMedia(media), playerController);
            mediaView.setOnError(t -> leftStatusLabel.setText(resources.getString("error") + t.toString()));


        } catch (Exception mediaException) {
            // Handle exception in Media constructor.
            leftStatusLabel.setText(resources.getString("error") + mediaException.getMessage());
        }
    }

    private void showPlayer(Node mediaView, MediaPlayerInterface mediaPlayer, MediaInterface media, PlayerControlsController playerController) {
        ScrollPane scrollPane = new ScrollPane(mediaView);
        mediaPlayer.setOnReady(() -> {
            sizeStageToScene();
        });
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        VBox.setVgrow(playerController.getRoot(), Priority.NEVER);
        VBox view = new VBox();
        VBox.setVgrow(view, Priority.ALWAYS);
        view.getChildren().addAll(scrollPane, playerController.getRoot());
        centerPane.getChildren().add(view);
    }

    private PlayerControlsController createPlayerController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("PlayerControls.fxml"));
            ResourceBundle labels = ResourceBundle.getBundle("org.monte.demo.movieplayer.Labels");
            loader.setResources(labels);
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

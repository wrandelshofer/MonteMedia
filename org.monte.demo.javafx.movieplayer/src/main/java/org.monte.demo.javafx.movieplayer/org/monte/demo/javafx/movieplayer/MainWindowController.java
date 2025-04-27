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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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

    @FXML // fx:id="rootPane"
    private BorderPane rootPane; // Value injected by FXMLLoader
    @FXML // fx:id="rootPane"
    private VBox vBox; // Value injected by FXMLLoader
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
        MediaPlayerInterface p = player.get();
        if (p != null) {
            p.dispose();
            player.set(null);
            vBox.getChildren().clear();
        }
        getStage().close();
    }

    @FXML
    void open(ActionEvent event) {
        File file = getFileChooser().showOpenDialog(getStage());
        if (file != null) {
            setFile(file);
        }
    }

    public static void startStage(Stage stage) {
        FXMLLoader loader = new FXMLLoader(MainWindowController.class.getResource("MainWindow.fxml"));
        ResourceBundle labels = ResourceBundle.getBundle("org.monte.demo.javafx.movieplayer.Labels");
        loader.setResources(labels);
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
//        throw new RuntimeException(e);
        }
        MainWindowController controller = loader.getController();


        controller.fileProperty().addListener((o, oldv, f) ->
                stage.setTitle((f == null ? labels.getString("application.name") : f.getName()))
        );
        stage.setOnHidden(event -> controller.close(null));


        stage.setTitle(labels.getString("application.name"));
        Scene scene = new Scene(root);
        scene.getStylesheets().add(MainWindowController.class.getResource("controls.css").toString());
        stage.setScene(scene);
        stage.setWidth(400);
        stage.setHeight(300);
        stage.show();
    }

    @FXML
    void newWindow(ActionEvent event) {
        MainWindowController.startStage(new Stage());
    }

    @FXML
    void togglePlayPause(ActionEvent event) {
        MediaPlayerInterface player = getPlayer();
        if (player != null) {
            if (player.getStatus() == MediaPlayer.Status.PLAYING) {
                player.pause();
            } else {
                player.play();
            }
        }
    }

    private Stage getStage() {
        Scene scene = rootPane.getScene();
        return scene == null ? null : (Stage) scene.getWindow();
    }

    @FXML
    void zoomIn(ActionEvent event) {
        zoomTo((Math.round(getZoomPower() + 1)));
    }

    @FXML
    void zoomOut(ActionEvent event) {
        zoomTo((Math.round(getZoomPower() - 1)));
    }

    @FXML
    void zoomToActualSize(ActionEvent event) {
        zoomTo(0);
    }

    void zoomTo(double power) {
        MediaInterface media = getPlayer() instanceof MediaPlayerInterface ? getPlayer().getMedia() : null;
        if (media == null) {
            return;
        }
        double factor = Math.pow(2, power);
        if (mediaPane != null) {
            mediaPane.setPrefWidth(media.getWidth() * factor);
            mediaPane.setPrefHeight(media.getHeight() * factor);
        }
        getStage().sizeToScene();
    }

    private MediaPlayerInterface getPlayer() {
        return player.get();
    }

    private double getZoomPower() {
        MediaInterface media = getPlayer() instanceof MediaPlayerInterface ? getPlayer().getMedia() : null;
        if (media == null) {
            return 1;
        }
        double factor = vBox.getWidth() / media.getWidth();
        double power = Math.log(factor) / Math.log(2);
        return power;
    }

    @FXML
    void zoomToFit(ActionEvent event) {
        zoomTo(getZoomPower());
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert vBox != null : "fx:id=\"stackPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'MainWindow.fxml'.";
        assert statusBar != null : "fx:id=\"statusBar\" was not injected: check your FXML file 'MainWindow.fxml'.";

        new DropFileHandler(vBox, this::setFile);

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
        vBox.getChildren().clear();
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
            retryCreateMoviePlayer(retries, player == null ? null : player.getError());
        } else {
            this.player.set(player);
            player.setOnError(() -> retryCreateMoviePlayer(retries, player.getError()));
        }
    }

    private void retryCreateMoviePlayer(int retries, Throwable error) {
        Mode[] values = Mode.values();
        if (retries < values.length) {
            mode.set(values[(mode.get().ordinal() + 1) % values.length]);
            createMoviePlayer(retries + 1);
        } else {
            ObservableList<Node> c = vBox.getChildren();
            c.clear();
            c.add(new Label(resources.getString("error.creatingPlayer") + "\n" + error));
            error.printStackTrace();
        }
    }


    private MediaPlayerInterface createMonteMediaPlayer() {
        File mediaFile = file.get();
        if (mediaFile == null) {
            return null;
        }
        MonteMedia movie = new MonteMedia(mediaFile);
        MonteMediaPlayer player = new MonteMediaPlayer(movie);
        MonteMediaView monteMediaView = MonteMediaView.newMonteMediaView();
        monteMediaView.setMedia(movie);
        PlayerControlsController playerController = PlayerControlsController.createPlayerController();
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
            PlayerControlsController playerController = PlayerControlsController.createPlayerController();
            FXMediaPlayer p = new FXMediaPlayer(mediaPlayer);
            player.set(p);
            playerController.setPlayer(p);
            mediaPlayer.setAutoPlay(true);
            mediaView = new MediaView(mediaPlayer);


            mediaView.fitWidthProperty().bind(vBox.widthProperty());
            mediaView.fitHeightProperty().bind(vBox.heightProperty());
            mediaView.setManaged(false);

            mediaView.setPreserveRatio(false);


            showPlayer(mediaView, p, new FXMedia(media), playerController);

            return p;
        } catch (Exception mediaException) {
            // Handle exception in Media constructor.
        }
        return null;
    }

    private Pane mediaPane;

    private void showPlayer(Node mediaView, MediaPlayerInterface mediaPlayer, MediaInterface media, PlayerControlsController playerController) {
        mediaPane = mediaView instanceof Pane p ? p : new BorderPane(mediaView);

        mediaPlayer.setOnReady(() -> {
            zoomTo(getZoomPower());
        });

        VBox.setVgrow(mediaPane, Priority.ALWAYS);
        VBox.setVgrow(playerController.getRoot(), Priority.NEVER);
        vBox.getChildren().setAll(mediaPane, playerController.getRoot());
    }


}

/**
 * Sample Skeleton for 'MonteMediaView.fxml' Controller Class
 */

package org.monte.demo.javafx.movieplayer.monteplayer;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.transform.Affine;
import javafx.scene.transform.MatrixType;
import javafx.scene.transform.Scale;
import org.monte.demo.javafx.movieplayer.model.TrackInterface;
import org.monte.media.av.Format;
import org.monte.media.av.codec.video.AffineTransform;
import org.monte.media.av.codec.video.VideoFormatKeys;

import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MonteMediaView {
    private final ObjectProperty<MonteMedia> media = new SimpleObjectProperty<>();
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="rootPane"
    private Pane rootPane; // Value injected by FXMLLoader
    @FXML // fx:id="group"
    private Group group; // Value injected by FXMLLoader

    private ObjectBinding<Scale> scaleGroupBinding;

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert rootPane != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'MonteMediaView.fxml'.";
        assert group != null : "fx:id=\"rootPane\" was not injected: check your FXML file 'MonteMediaView.fxml'.";

        group.getTransforms().add(new Scale(1, 1, 0, 0));

        media.addListener((o, oldv, newv) -> {
            if (oldv != null) {
                ObservableList<TrackInterface> tracks = oldv.getTracks();
                tracks.removeListener(trackHandler);
                group.getChildren().clear();
                if (scaleGroupBinding != null) {
                    scaleGroupBinding.dispose();
                    scaleGroupBinding = null;
                }
            }
            if (newv != null) {
                ObservableList<TrackInterface> tracks = newv.getTracks();
                tracks.addListener(trackHandler);
                for (TrackInterface tr : tracks) {
                    addTrack(tr);
                }
                scaleGroupBinding = Bindings.createObjectBinding(() -> {
                            return new Scale(rootPane.getWidth() / newv.getWidth(), rootPane.getHeight() / newv.getHeight(), 0, 0);
                        }, rootPane.widthProperty(), rootPane.heightProperty(), newv.widthProperty(), newv.heightProperty()

                );
                scaleGroupBinding.addListener((oo, oldvv, newvv) -> {
                    group.getTransforms().set(0, newvv);
                });
            }
        });
    }

    private void addTrack(TrackInterface tr) {
        if (tr instanceof MonteVideoTrack) {
            MonteVideoTrack vt = (MonteVideoTrack) tr;
            ImageView imageView = new ImageView();
            imageView.imageProperty().bind(vt.videoImageProperty());
            Format format = vt.getFormat();
            if (format != null) {
                AffineTransform transform = format.get(VideoFormatKeys.TransformKey, AffineTransform.IDENTITY);
                if (!transform.isIdentity()) {
                    Affine affine = new Affine(transform.getFlatMatrix(), MatrixType.MT_2D_2x3, 0);
                    imageView.getTransforms().add(affine);
                }
            }
            group.getChildren().add(imageView);
            trackMap.put(tr, imageView);
        }
    }

    private final ListChangeListener<TrackInterface> trackHandler = new ListChangeListener<TrackInterface>() {
        @Override
        public void onChanged(Change<? extends TrackInterface> c) {
            while (c.next()) {
                for (TrackInterface remitem : c.getRemoved()) {
                    removeTrack(remitem);
                }
                for (TrackInterface additem : c.getAddedSubList()) {
                    addTrack(additem);
                }
            }
        }
    };
    private Map<TrackInterface, Node> trackMap = new LinkedHashMap<>();

    private void removeTrack(TrackInterface remitem) {
        Node remove = trackMap.remove(remitem);
        group.getChildren().remove(remove);
        if (remove instanceof ImageView) {
            ImageView imageView = (ImageView) remove;
            imageView.imageProperty().unbind();
        }
    }

    public MonteMedia getMedia() {
        return media.get();
    }

    public void setMedia(MonteMedia newValue) {
        media.set(newValue);
    }

    public ObjectProperty<MonteMedia> mediaProperty() {
        return media;
    }

    public Pane getRoot() {
        return rootPane;
    }

    public static MonteMediaView newMonteMediaView() {
        FXMLLoader loader = new FXMLLoader(MonteMediaView.class.getResource("MonteMediaView.fxml"));
        ResourceBundle labels = ResourceBundle.getBundle("org.monte.demo.javafx.movieplayer.Labels");
        loader.setResources(labels);
        try {
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MonteMediaView controller = loader.getController();
        return controller;
    }
}

/*
 * @(#)MainInspectorController.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

/// Sample Skeleton for 'Untitled' Controller Class
package org.monte.demo.javafx.colorquantizer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.monte.demo.javafx.colorquantizer.model.ColorQuantizerMainModel;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

public class MainInspectorController {

    private final ObjectProperty<ColorQuantizerMainModel> model = new SimpleObjectProperty<>(new ColorQuantizerMainModel());
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="fileField"
    private TextField fileField; // Value injected by FXMLLoader
    @FXML // fx:id="root"
    private VBox root; // Value injected by FXMLLoader
    @FXML // fx:id="tabPane"
    private TabPane tabPane; // Value injected by FXMLLoader
    private FileChooser openFileChooser;
    private FileChooser saveFileChooser;

    public static MainInspectorController newInstance() {
        try {
            FXMLLoader loader = new FXMLLoader(MainInspectorController.class.getResource("MainInspector.fxml"));
            // ResourceBundle labels = ResourceBundle.getBundle("org.monte.demo.javafx.movieplayer.Labels");
            //  loader.setRoot(new GridPane());
            // loader.setResources(labels);
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ColorQuantizerMainModel getModel() {
        return model.get();
    }

    public Parent getRoot() {
        return root;
    }

    private SizeInspectorController resizeInspector;
    private BatchInspectorController batchInspector;
    private ColorInspectorController colorInspector;

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert fileField != null : "fx:id=\"fileField\" was not injected: check your FXML file 'MainInspector.fxml'.";
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'MainInspector.fxml'.";
        assert tabPane != null : "fx:id=\"tabPane\" was not injected: check your FXML file 'MainInspector.fxml'.";

        resizeInspector = SizeInspectorController.newInstance();
        batchInspector = BatchInspectorController.newInstance();
        colorInspector = ColorInspectorController.newInstance();
        tabPane.getTabs().add(new Tab("Size", resizeInspector.getRoot()));
        tabPane.getTabs().add(new Tab("Color", colorInspector.getRoot()));
        tabPane.getTabs().add(new Tab("Batch", batchInspector.getRoot()));
        resizeInspector.modelProperty().bind(model);
        colorInspector.modelProperty().bind(model);
        batchInspector.modelProperty().bind(model);
        model.addListener(this::modelChanged);
        modelChanged(model, null, model.get());
        new DragSupport(fileField, Clipboard::hasFiles, this::onFilesDropped);


    }


    private Boolean onFilesDropped(Dragboard dragboard) {
        var files = dragboard.getFiles();
        if (files.isEmpty()) return false;
        var file = files.getFirst();
        if (file.isFile()) {
            getModel().setReferenceFile(file.toPath());
            return true;
        }
        return false;
    }

    private void modelChanged(ObservableValue<? extends ColorQuantizerMainModel> o, ColorQuantizerMainModel oldv, ColorQuantizerMainModel newv) {
        if (oldv != null) {
            fileField.textProperty().unbind();
        }
        if (newv != null) {
            fileField.textProperty().bind(newv.referenceFileProperty().map(p -> p == null ? null : p.getFileName().toString()));
        }
    }

    public ObjectProperty<ColorQuantizerMainModel> modelProperty() {
        return model;
    }

    @FXML
    void onLoad(ActionEvent event) {
        reuseOpenFileChooser();
        File newFile = openFileChooser.showOpenDialog(root.getScene().getWindow());
        if (newFile != null) getModel().setReferenceFile(newFile.toPath());

    }

    @FXML
    void onSaveAs(ActionEvent event) {
        reuseSaveFileChooser();
        File newFile = saveFileChooser.showSaveDialog(root.getScene().getWindow());
        var filter = saveFileChooser.getSelectedExtensionFilter();
        String formatName = filter == null ? "PNG" : filter.getDescription();
        if (newFile != null && filter != null && !filter.getExtensions().isEmpty()) {
            int p = newFile.getName().lastIndexOf('.');
            String ext = p < 0 ? "" : newFile.getName().substring(p + 1);
            if (!filter.getExtensions().contains(ext)) {
                newFile = new File(newFile.toString() + "." + filter.getExtensions().getFirst());
            }
        }
        if (newFile != null) {
            boolean success = getModel().saveFileAs(newFile.toPath(), formatName);
            if (!success) {
                var alert = new Alert(Alert.AlertType.ERROR, "Can not save file " + newFile.getName() + " as " + formatName + ".");
                alert.initOwner(getRoot().getScene().getWindow());
                alert.showAndWait();
            }
        }

    }

    private void reuseOpenFileChooser() {
        if (openFileChooser == null) {
            openFileChooser = new FileChooser();
        }
    }

    private void reuseSaveFileChooser() {
        if (saveFileChooser == null) {
            saveFileChooser = new FileChooser();
            var list = StreamSupport.stream(
                            Spliterators.spliteratorUnknownSize(
                                    IIORegistry.getDefaultInstance().getServiceProviders(ImageWriterSpi.class, o -> true, true), 0), false)
                    .sorted(Comparator.comparing((t) -> t.getFormatNames()[0].toLowerCase(Locale.ROOT))).toList();
            ;
            for (ImageWriterSpi spi : list) {
                Optional<String> formatName = Arrays.stream(spi.getFormatNames()).sorted().findFirst();
                if (formatName.isEmpty()) continue;
                saveFileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(formatName.get(), spi.getFileSuffixes()));
            }
        }
    }

}

/*
 * @(#)BatchInspectorController.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Sample Skeleton for 'BatchInspector.fxml' Controller Class
 */

package org.monte.demo.javafx.colorquantizer;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.Clipboard;
import javafx.scene.input.Dragboard;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.monte.demo.javafx.colorquantizer.model.ColorQuantizerMainModel;

import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Spliterators;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class BatchInspectorController {

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="inputFilesListView"
    private ListView<Path> inputFilesListView; // Value injected by FXMLLoader

    @FXML // fx:id="onSelectOutputDirectory"
    private Button onSelectOutputDirectory; // Value injected by FXMLLoader

    @FXML // fx:id="outputDirectoryField"
    private TextField outputDirectoryField; // Value injected by FXMLLoader
    @FXML // fx:id="progressIndicator"
    private ProgressIndicator progressIndicator; // Value injected by FXMLLoader

    @FXML // fx:id="progressLabel"
    private Label progressLabel; // Value injected by FXMLLoader

    @FXML // fx:id="outputFormatBox"
    private ComboBox<String> outputFormatBox; // Value injected by FXMLLoader

    @FXML // fx:id="root"
    private GridPane root; // Value injected by FXMLLoader

    @FXML // fx:id="startBatchButton"
    private Button startBatchButton; // Value injected by FXMLLoader

    @FXML // fx:id="stopBatchButton"
    private Button stopBatchButton; // Value injected by FXMLLoader

    @FXML
    void onAdd(ActionEvent event) {
        reuseOpenFileChooser();
        List<File> newFiles = openFileChooser.showOpenMultipleDialog(root.getScene().getWindow());
        if (newFiles != null) addInputFiles(newFiles);
    }

    private void reuseOpenFileChooser() {
        if (openFileChooser == null) {
            openFileChooser = new FileChooser();
        }
    }

    private void reuseOutputDirChooser() {
        if (outputDirChooser == null) {
            outputDirChooser = new DirectoryChooser();
        }
    }

    @FXML
    void onClear(ActionEvent event) {
        model.get().getBatchInputFiles().clear();
    }

    @FXML
    void onRemove(ActionEvent event) {
        model.get().getBatchInputFiles().removeAll(inputFilesListView.getSelectionModel().getSelectedItems());
    }

    @FXML
    void onSelectOutputDirectory(ActionEvent event) {
        reuseOutputDirChooser();
        File newDir = outputDirChooser.showDialog(root.getScene().getWindow());
        model.get().setBatchOutputDirectory(newDir == null ? null : newDir.toPath());
    }

    public Node getRoot() {
        return root;
    }

    private FileChooser openFileChooser;
    private DirectoryChooser outputDirChooser;

    private Task<Void> batchTask;

    @FXML
    void onStartBatch(ActionEvent event) {

        progressIndicator.setDisable(false);
        progressLabel.setDisable(false);
        stopBatchButton.setDisable(false);
        startBatchButton.setDisable(true);

        batchTask = model.get().createBatchTask();

        progressIndicator.progressProperty().bind(batchTask.progressProperty());
        progressLabel.textProperty().bind(batchTask.messageProperty());
        batchTask.stateProperty().addListener(this::onBatchStateChange);
        ForkJoinPool.commonPool().submit(batchTask);
    }

    private void onBatchStateChange(Observable observable, Worker.State oldv, Worker.State newv) {
        System.out.println("batchStateChanged " + oldv + "->" + newv);
        switch (newv) {
            case SUCCEEDED, CANCELLED, FAILED -> onBatchTerminated();
        }
    }


    void onBatchTerminated() {
        progressIndicator.progressProperty().unbind();
        progressLabel.textProperty().unbind();
        if (progressIndicator.isIndeterminate()) progressIndicator.setProgress(0);
        if (batchTask != null) {
            var e = batchTask.getException();
            if (e != null && e.getMessage() != null) {
                progressLabel.setText(e.getMessage());
            }
        }
        onStopBatch(null);
    }

    @FXML
    void onStopBatch(ActionEvent event) {
        progressIndicator.setDisable(true);
        progressLabel.setDisable(true);
        stopBatchButton.setDisable(true);
        startBatchButton.setDisable(false);
        if (batchTask != null) {
            batchTask.cancel();
            batchTask = null;
        }
    }

    public ColorQuantizerMainModel getModel() {
        return model.get();
    }

    public ObjectProperty<ColorQuantizerMainModel> modelProperty() {
        return model;
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert inputFilesListView != null : "fx:id=\"inputFilesListView\" was not injected: check your FXML file 'BatchInspector.fxml'.";
        assert onSelectOutputDirectory != null : "fx:id=\"onSelectOutputDirectory\" was not injected: check your FXML file 'BatchInspector.fxml'.";
        assert outputDirectoryField != null : "fx:id=\"outputDirectoryField\" was not injected: check your FXML file 'BatchInspector.fxml'.";
        assert outputFormatBox != null : "fx:id=\"outputFormatBox\" was not injected: check your FXML file 'BatchInspector.fxml'.";
        assert progressIndicator != null : "fx:id=\"progressIndicator\" was not injected: check your FXML file 'BatchInspector.fxml'.";
        assert progressLabel != null : "fx:id=\"progressLabel\" was not injected: check your FXML file 'BatchInspector.fxml'.";
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'BatchInspector.fxml'.";
        assert startBatchButton != null : "fx:id=\"startBatchButton\" was not injected: check your FXML file 'BatchInspector.fxml'.";
        assert stopBatchButton != null : "fx:id=\"stopBatchButton\" was not injected: check your FXML file 'BatchInspector.fxml'.";
        inputFilesListView.setCellFactory(new Callback<ListView<Path>, ListCell<Path>>() {
            @Override
            public ListCell<Path> call(ListView<Path> p) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Path item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setText(null);
                        } else {
                            setText(item.getFileName().toString());
                        }
                    }
                };
            }
        });
        inputFilesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        new DragSupport(inputFilesListView, Clipboard::hasFiles, this::onInputFilesDropped);
        new DragSupport(outputDirectoryField, Clipboard::hasFiles, this::onOutputDirDropped);
        model.addListener(this::modelChanged);
        modelChanged(model, null, model.get());

        var list = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                IIORegistry.getDefaultInstance().getServiceProviders(ImageWriterSpi.class, o -> true, true), 0), false)
                .sorted(Comparator.comparing((t) -> t.getFormatNames()[0].toLowerCase(Locale.ROOT)))
                .map(t -> Arrays.stream(t.getFormatNames()).sorted().findFirst().orElse("---"))
                .toList();
        outputFormatBox.getItems().setAll(list);

    }

    private Boolean onInputFilesDropped(Dragboard dragboard) {
        var files = dragboard.getFiles();
        if (files.isEmpty()) return false;
        addInputFiles(files);
        return true;
    }

    private Boolean onOutputDirDropped(Dragboard dragboard) {
        var files = dragboard.getFiles();
        if (files.isEmpty()) return false;
        var file = files.getFirst();
        if (file.isDirectory()) {
            getModel().setBatchOutputDirectory(file.toPath());
            return true;
        }
        return false;
    }

    private final ObjectProperty<ColorQuantizerMainModel> model = new SimpleObjectProperty<>(new ColorQuantizerMainModel());

    public static BatchInspectorController newInstance() {
        try {
            FXMLLoader loader = new FXMLLoader(BatchInspectorController.class.getResource("BatchInspector.fxml"));
            // ResourceBundle labels = ResourceBundle.getBundle("org.monte.demo.javafx.movieplayer.Labels");
            //  loader.setRoot(new GridPane());
            // loader.setResources(labels);
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void modelChanged(ObservableValue<? extends ColorQuantizerMainModel> o, ColorQuantizerMainModel
            oldv, ColorQuantizerMainModel newv) {
        if (oldv != null) {
            inputFilesListView.itemsProperty().unbindBidirectional(oldv.batchInputFilesProperty());
            outputDirectoryField.textProperty().unbind();
            outputFormatBox.valueProperty().unbindBidirectional(oldv.batchOutputFormatProperty());
        }
        if (newv != null) {
            inputFilesListView.itemsProperty().bindBidirectional(newv.batchInputFilesProperty());
            outputFormatBox.valueProperty().bindBidirectional(newv.batchOutputFormatProperty());
            outputDirectoryField.textProperty().bind(newv.batchOutputDirectoryProperty().map(p -> p == null ? "." : p.getFileName().toString()));
        }
    }


    private void addInputFiles(List<File> files) {
        model.get().submit(new Task<List<Path>>() {

            @Override
            protected List<Path> call() throws Exception {
                List<Path> filteredAndSortedPaths =
                        files.stream().<Path>flatMap(f -> {
                            if (f.isDirectory()) {
                                try (var s = Files.list(f.toPath())) {
                                    return s.toList().stream();
                                } catch (IOException e) {
                                    return Stream.empty();
                                }
                            } else {
                                return Stream.of(f.toPath());
                            }
                        }).filter(f -> {
                            try {
                                return !Files.isHidden(f) && !Files.isDirectory(f);
                            } catch (IOException e) {
                                return false;
                            }
                        }).sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();

                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        model.get().getBatchInputFiles().addAll(filteredAndSortedPaths);
                    }
                });
                return filteredAndSortedPaths;
            }
        });
    }


}

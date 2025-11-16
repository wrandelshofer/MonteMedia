/*
 * @(#)SizeInspectorController.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Sample Skeleton for 'SizeInspector.fxml' Controller Class
 */

package org.monte.demo.javafx.colorquantizer;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.converter.NumberStringConverter;
import org.monte.demo.javafx.colorquantizer.model.ColorQuantizerMainModel;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SizeInspectorController {
    private final ObjectProperty<ColorQuantizerMainModel> model = new SimpleObjectProperty<>(new ColorQuantizerMainModel());

    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;

    @FXML // fx:id="cropBottomField"
    private TextField cropBottomField; // Value injected by FXMLLoader

    @FXML // fx:id="cropCheckBox"
    private CheckBox cropCheckBox; // Value injected by FXMLLoader

    @FXML // fx:id="cropLeftField"
    private TextField cropLeftField; // Value injected by FXMLLoader

    @FXML // fx:id="cropRightField"
    private TextField cropRightField; // Value injected by FXMLLoader

    @FXML // fx:id="cropTopField"
    private TextField cropTopField; // Value injected by FXMLLoader

    @FXML // fx:id="heightLabel"
    private Label heightLabel; // Value injected by FXMLLoader

    @FXML // fx:id="preserveAspectRatioCheckBox"
    private CheckBox preserveAspectRatioCheckBox; // Value injected by FXMLLoader

    @FXML // fx:id="root"
    private GridPane root; // Value injected by FXMLLoader

    @FXML // fx:id="scaleCheckBox"
    private CheckBox scaleCheckBox; // Value injected by FXMLLoader

    @FXML // fx:id="scaleHeightField"
    private TextField scaleHeightField; // Value injected by FXMLLoader

    @FXML // fx:id="scaleWidthField"
    private TextField scaleWidthField; // Value injected by FXMLLoader

    @FXML // fx:id="widthLabel"
    private Label widthLabel; // Value injected by FXMLLoader

    @FXML // fx:id="zoomSpinner"
    private Spinner<Integer> zoomSpinner; // Value injected by FXMLLoader

    public Parent getRoot() {
        return root;
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert cropBottomField != null : "fx:id=\"cropBottomField\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        assert cropCheckBox != null : "fx:id=\"cropCheckBox\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        assert cropLeftField != null : "fx:id=\"cropLeftField\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        assert cropRightField != null : "fx:id=\"cropRightField\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        assert cropTopField != null : "fx:id=\"cropTopField\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        assert heightLabel != null : "fx:id=\"heightLabel\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        assert preserveAspectRatioCheckBox != null : "fx:id=\"preserveAspectRatioCheckBox\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        assert scaleCheckBox != null : "fx:id=\"scaleCheckBox\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        assert scaleHeightField != null : "fx:id=\"scaleHeightField\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        assert scaleWidthField != null : "fx:id=\"scaleWidthField\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        assert widthLabel != null : "fx:id=\"widthLabel\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        assert zoomSpinner != null : "fx:id=\"zoomSpinner\" was not injected: check your FXML file 'SizeInspector.fxml'.";
        zoomSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-5, 5, 0, 1));
        model.addListener(this::modelChanged);
        modelChanged(model, null, model.get());
        cropTopField.disableProperty().bind(cropCheckBox.selectedProperty().not());
        cropBottomField.disableProperty().bind(cropCheckBox.selectedProperty().not());
        cropRightField.disableProperty().bind(cropCheckBox.selectedProperty().not());
        cropLeftField.disableProperty().bind(cropCheckBox.selectedProperty().not());
        scaleHeightField.disableProperty().bind(scaleCheckBox.selectedProperty().not());
        scaleWidthField.disableProperty().bind(scaleCheckBox.selectedProperty().not());
    }

    private void modelChanged(ObservableValue<? extends ColorQuantizerMainModel> o, ColorQuantizerMainModel oldv, ColorQuantizerMainModel newv) {
        if (oldv != null) {
            widthLabel.textProperty().unbind();
            heightLabel.textProperty().unbind();
            scaleCheckBox.selectedProperty().unbindBidirectional(oldv.scaleProperty());
            cropCheckBox.selectedProperty().unbindBidirectional(oldv.cropProperty());
            scaleHeightField.textProperty().unbindBidirectional(oldv.scaledHeightProperty());
            scaleWidthField.textProperty().unbindBidirectional(oldv.scaledWidthProperty());
            preserveAspectRatioCheckBox.selectedProperty().unbindBidirectional(oldv.preserveAspectRatioProperty());
            cropLeftField.textProperty().unbindBidirectional(oldv.cropLeftProperty());
            cropRightField.textProperty().unbindBidirectional(oldv.cropRightProperty());
            cropTopField.textProperty().unbindBidirectional(oldv.cropTopProperty());
            cropBottomField.textProperty().unbindBidirectional(oldv.cropBottomProperty());
            if (keepZoomProperty != null) {
                zoomSpinner.getValueFactory().valueProperty().unbindBidirectional(keepZoomProperty);
                keepZoomProperty = null;
            }
        }
        if (newv != null) {
            widthLabel.textProperty().bind(newv.widthProperty().asString());
            heightLabel.textProperty().bind(newv.heightProperty().asString());
            scaleCheckBox.selectedProperty().bindBidirectional(newv.scaleProperty());
            cropCheckBox.selectedProperty().bindBidirectional(newv.cropProperty());
            preserveAspectRatioCheckBox.selectedProperty().bindBidirectional(newv.preserveAspectRatioProperty());
            scaleHeightField.textProperty().bindBidirectional(newv.scaledHeightProperty(), new NumberStringConverter());
            scaleWidthField.textProperty().bindBidirectional(newv.scaledWidthProperty(), new NumberStringConverter());
            cropLeftField.textProperty().bindBidirectional(newv.cropLeftProperty(), new NumberStringConverter());
            cropRightField.textProperty().bindBidirectional(newv.cropRightProperty(), new NumberStringConverter());
            cropTopField.textProperty().bindBidirectional(newv.cropTopProperty(), new NumberStringConverter());
            cropBottomField.textProperty().bindBidirectional(newv.cropBottomProperty(), new NumberStringConverter());
            keepZoomProperty = newv.zoomProperty().asObject();
            zoomSpinner.getValueFactory().valueProperty().bindBidirectional(keepZoomProperty);
        }
    }

    /**
     * Prevent zoom property from being garbage collected.
     */
    private ObjectProperty<Integer> keepZoomProperty;

    public ObjectProperty<ColorQuantizerMainModel> modelProperty() {
        return model;
    }

    public static SizeInspectorController newInstance() {
        try {
            FXMLLoader loader = new FXMLLoader(SizeInspectorController.class.getResource("SizeInspector.fxml"));
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
}

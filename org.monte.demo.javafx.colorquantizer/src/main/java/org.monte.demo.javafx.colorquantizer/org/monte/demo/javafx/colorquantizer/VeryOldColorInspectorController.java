/*
 * @(#)VeryOldColorInspectorController.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

/**
 * Sample Skeleton for 'VeryOldColorInspector.fxml' Controller Class
 */

package org.monte.demo.javafx.colorquantizer;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.monte.demo.javafx.colorquantizer.model.ColorQuantizerMainModel;
import org.monte.demo.javafx.colorquantizer.model.ModelColorSpace;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

import static org.monte.demo.javafx.colorquantizer.model.ModelColorSpace.OKLAB;

public class VeryOldColorInspectorController {

    private final ObjectProperty<Consumer<Integer>> onComputeKMeans = new SimpleObjectProperty<>(i -> {
    });
    private final ObjectProperty<ColorQuantizerMainModel> model = new SimpleObjectProperty<>(new ColorQuantizerMainModel());
    private final ListProperty<Color> palette = new SimpleListProperty<>(FXCollections.emptyObservableList());
    private final ObjectProperty<ModelColorSpace> colorSpaceProperty = new SimpleObjectProperty<>(ModelColorSpace.SRGB);
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;
    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML // fx:id="colorSpace"
    private ToggleGroup colorSpace; // Value injected by FXMLLoader
    @FXML // fx:id="kMeansButton"
    private Button kMeansButton; // Value injected by FXMLLoader
    @FXML // fx:id="kMeansField"
    private TextField kMeansField; // Value injected by FXMLLoader
    @FXML // fx:id="okLabRadio"
    private RadioButton okLabRadio; // Value injected by FXMLLoader
    @FXML // fx:id="palettePane"
    private FlowPane palettePane; // Value injected by FXMLLoader
    @FXML // fx:id="root"
    private GridPane root; // Value injected by FXMLLoader
    @FXML // fx:id="sRgbRadio"
    private RadioButton sRgbRadio; // Value injected by FXMLLoader

    public static VeryOldColorInspectorController newInstance() {
        try {
            FXMLLoader loader = new FXMLLoader(VeryOldColorInspectorController.class.getResource("VeryOldColorInspector.fxml"));
            // ResourceBundle labels = ResourceBundle.getBundle("org.monte.demo.javafx.movieplayer.Labels");
            //  loader.setRoot(new GridPane());
            // loader.setResources(labels);
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectProperty<ModelColorSpace> colorSpacePropertyProperty() {
        return colorSpaceProperty;
    }

    @FXML
    void computeKMeans(ActionEvent event) {
        onComputeKMeans.get().accept(Integer.parseInt(kMeansField.getText()));
    }

    public ModelColorSpace getColorSpaceProperty() {
        return colorSpaceProperty.get();
    }

    public Consumer<Integer> getOnComputeKMeans() {
        return onComputeKMeans.get();
    }

    public ObservableList<Color> getPalette() {
        return palette.get();
    }

    public GridPane getRoot() {
        return root;
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert colorSpace != null : "fx:id=\"colorSpace\" was not injected: check your FXML file 'VeryOldColorInspector.fxml'.";
        assert kMeansButton != null : "fx:id=\"kMeansButton\" was not injected: check your FXML file 'VeryOldColorInspector.fxml'.";
        assert kMeansField != null : "fx:id=\"kMeansField\" was not injected: check your FXML file 'VeryOldColorInspector.fxml'.";
        assert okLabRadio != null : "fx:id=\"okLabRadio\" was not injected: check your FXML file 'VeryOldColorInspector.fxml'.";
        assert palettePane != null : "fx:id=\"palettePane\" was not injected: check your FXML file 'VeryOldColorInspector.fxml'.";
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'VeryOldColorInspector.fxml'.";
        assert sRgbRadio != null : "fx:id=\"sRgbRadio\" was not injected: check your FXML file 'VeryOldColorInspector.fxml'.";


        colorSpace.selectedToggleProperty().addListener((o, oldv, t) -> colorSpaceProperty.set(t == sRgbRadio ? ModelColorSpace.SRGB : OKLAB));
        colorSpaceProperty.addListener((o, oldv, t) -> {
            switch (t) {
                case SRGB -> sRgbRadio.setSelected(true);
                case OKLAB -> okLabRadio.setSelected(true);
            }
            ;
        });
        palette.addListener(
                new ListChangeListener<Color>() {
                    public void onChanged(Change<? extends Color> c) {
                        ObservableList<Node> children = palettePane.getChildren();
                        while (c.next()) {
                            if (c.wasPermutated()) {
                                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                                    //permutate
                                }
                            } else if (c.wasUpdated()) {
                                //update item
                            } else if (c.wasRemoved()) {
                                children.remove(c.getFrom(), c.getFrom() + c.getRemovedSize());
                            } else if (c.wasAdded()) {
                                ObservableList<? extends Color> list = c.getList();
                                for (int i = c.getFrom(); i < c.getTo(); i++) {
                                    var r = new Region();
                                    r.setBackground(new Background(new BackgroundFill(list.get(i), null, null)));
                                    r.setPrefSize(16, 16);
                                    children.add(i, r);
                                }
                            }
                        }
                    }
                });
    }

    public ObjectProperty<ColorQuantizerMainModel> modelProperty() {
        return model;
    }

    public ObjectProperty<Consumer<Integer>> onComputeKMeansProperty() {
        return onComputeKMeans;
    }

    public ListProperty<Color> paletteProperty() {
        return palette;
    }
}

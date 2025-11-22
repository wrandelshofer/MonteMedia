/*
 * @(#)ColorInspectorController.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

/// Sample Skeleton for 'ColorInspector.fxml' Controller Class
package org.monte.demo.javafx.colorquantizer;

import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;
import javafx.util.converter.NumberStringConverter;
import org.monte.demo.javafx.colorquantizer.model.ColorMode;
import org.monte.demo.javafx.colorquantizer.model.ColorQuantizerMainModel;
import org.monte.demo.javafx.colorquantizer.model.DitheringMethod;
import org.monte.demo.javafx.colorquantizer.model.PaletteMode;
import org.monte.media.color.Rec709ColorSpace;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ColorInspectorController {
    private final ListProperty<Color> palette = new SimpleListProperty<>(FXCollections.observableArrayList());

    public GridPane getRoot() {
        return root;
    }

    public ObjectProperty<ColorQuantizerMainModel> modelProperty() {
        return model;
    }

    private final ObjectProperty<ColorQuantizerMainModel> model = new SimpleObjectProperty<>(new ColorQuantizerMainModel());
    @FXML // ResourceBundle that was given to the FXMLLoader
    private ResourceBundle resources;

    @FXML // URL location of the FXML file that was given to the FXMLLoader
    private URL location;
    @FXML // fx:id="colorSpaceComboBox"
    private ComboBox<ColorSpace> colorSpaceComboBox; // Value injected by FXMLLoader

    @FXML // fx:id="colorModeComboBox"
    private ComboBox<ColorMode> colorModeComboBox; // Value injected by FXMLLoader

    @FXML // fx:id="computePaletteButton"
    private Button computePaletteButton; // Value injected by FXMLLoader

    @FXML // fx:id="ditherIntensityField"
    private TextField ditherIntensityField; // Value injected by FXMLLoader

    @FXML // fx:id="ditherIntensityLabel"
    private Label ditherIntensityLabel; // Value injected by FXMLLoader

    @FXML // fx:id="ditheringMethodComboBox"
    private ComboBox<DitheringMethod> ditheringMethodComboBox; // Value injected by FXMLLoader

    @FXML // fx:id="inputImageColorSpaceLabel"
    private Label inputImageColorSpaceLabel; // Value injected by FXMLLoader

    @FXML // fx:id="inputImageColorsLabel"
    private Label inputImageColorsLabel; // Value injected by FXMLLoader

    @FXML // fx:id="loadPaletteButton"
    private Button loadPaletteButton; // Value injected by FXMLLoader

    @FXML // fx:id="lockPalette0CheckBox"
    private CheckBox lockPalette0CheckBox; // Value injected by FXMLLoader

    @FXML // fx:id="paletteIndex0ColorField"
    private TextField palette0ColorField; // Value injected by FXMLLoader

    @FXML // fx:id="paletteSizeField"
    private TextField paletteSizeField; // Value injected by FXMLLoader

    @FXML // fx:id="paletteSizeLabel"
    private Label paletteSizeLabel; // Value injected by FXMLLoader

    @FXML // fx:id="paletteLabel"
    private Label paletteLabel; // Value injected by FXMLLoader

    @FXML // fx:id="paletteModeComboBox"
    private ComboBox<PaletteMode> paletteModeComboBox; // Value injected by FXMLLoader

    @FXML // fx:id="palettePane"
    private FlowPane palettePane; // Value injected by FXMLLoader


    @FXML // fx:id="root"
    private GridPane root; // Value injected by FXMLLoader

    @FXML
    void onComputePalette(ActionEvent event) {

    }

    @FXML
    void onLoadPalette(ActionEvent event) {

    }

    private final ChangeListener<? super IndexColorModel> colorModelListener = this::colorModelChanged;

    private void colorModelChanged(Object o, IndexColorModel oldv, IndexColorModel newv) {
        if (newv == null) {
            palette.clear();
        } else {
            try {
                int[] rgbs = new int[newv.getMapSize()];
                List<Color> newList = new ArrayList<>(rgbs.length);
                newv.getRGBs(rgbs);
                for (int i = 0; i < rgbs.length; i++) {
                    int rgb = rgbs[i];
                    newList.add(Color.rgb((rgb >> 16) & 0xff, (rgb >> 8) & 0xff, rgb & 0xff));
                }
                palette.setAll(newList);
            } catch (Throwable t) {
                System.err.println(t);
                t.printStackTrace();
            }
        }
    }

    @FXML
        // This method is called by the FXMLLoader when initialization is complete
    void initialize() {
        assert colorModeComboBox != null : "fx:id=\"colorModeComboBox\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert colorSpaceComboBox != null : "fx:id=\"colorSpaceComboBox\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert computePaletteButton != null : "fx:id=\"computePaletteButton\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert ditherIntensityField != null : "fx:id=\"ditherIntensityField\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert ditherIntensityLabel != null : "fx:id=\"ditherIntensityLabel\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert ditheringMethodComboBox != null : "fx:id=\"ditheringMethodComboBox\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert inputImageColorSpaceLabel != null : "fx:id=\"inputImageColorSpaceLabel\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert inputImageColorsLabel != null : "fx:id=\"inputImageColorsLabel\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert loadPaletteButton != null : "fx:id=\"loadPaletteButton\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert lockPalette0CheckBox != null : "fx:id=\"lockPalette0CheckBox\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert palette0ColorField != null : "fx:id=\"palette0ColorField\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert paletteSizeField != null : "fx:id=\"paletteSizeField\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert paletteSizeLabel != null : "fx:id=\"paletteSizeLabel\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert paletteLabel != null : "fx:id=\"paletteLabel\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert paletteModeComboBox != null : "fx:id=\"paletteModeComboBox\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert palettePane != null : "fx:id=\"palettePane\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        assert root != null : "fx:id=\"root\" was not injected: check your FXML file 'ColorInspector.fxml'.";
        colorModeComboBox.itemsProperty().set(FXCollections.observableArrayList(ColorMode.values()));
        paletteModeComboBox.itemsProperty().set(FXCollections.observableArrayList(PaletteMode.values()));
        ditheringMethodComboBox.itemsProperty().set(FXCollections.observableArrayList(DitheringMethod.values()));
        colorModeComboBox.valueProperty().addListener(this::updateEnabledStates);
        paletteModeComboBox.valueProperty().addListener(this::updateEnabledStates);
        model.addListener(this::modelChanged);
        modelChanged(model, null, model.get());
        updateEnabledStates(null, null, null);
        colorSpaceComboBox.itemsProperty().set(FXCollections.observableArrayList(
                null,
                ColorSpace.getInstance(ColorSpace.CS_sRGB),
                Rec709ColorSpace.getInstance()
        ));
        colorSpaceComboBox.setConverter(new StringConverter<ColorSpace>() {
            @Override
            public String toString(ColorSpace cs) {
                return cs == null ? "as specified in image" : cs.isCS_sRGB() ? "sRGB" : cs.toString();
            }

            @Override
            public ColorSpace fromString(String string) {
                return null;
            }
        });

        palette.addListener((ListChangeListener<Color>) c -> {
            ObservableList<Node> children = palettePane.getChildren();
            while (c.next()) {
                /*
                if (c.wasPermutated()) {
                    for (int i = c.getFrom(); i < c.getTo(); ++i) {
                        //permutate
                    }
                } else if (c.wasUpdated()) {
                    //update item
                } else {*/
                if (c.wasRemoved()) {
                    children.remove(c.getFrom(), c.getFrom() + c.getRemovedSize());
                }
                if (c.wasAdded()) {
                    List<? extends Color> added = c.getAddedSubList();
                    int index = c.getFrom();
                    for (Color a : added) {
                        var r = new Region();
                        r.setBackground(new Background(new BackgroundFill(a, null, null)));
                        r.setPrefSize(16, 16);
                        var l = new Label(null, r);
                        l.setTooltip(new Tooltip("#" + a.toString().substring(2, 8)));
                        children.add(index++, l);
                    }
                }
                //}
            }
        });
    }

    private void updateEnabledStates(Observable o, Object oldv, Object newv) {
        var cm = model.get().getColorMode();
        var pm = model.get().getPaletteMode();
        boolean disablePalette = switch (cm) {
            case _24_BIT_INDEXED_COLORS, _12_BIT_INDEXED_COLORS, AMIGA_HAM6, AMIGA_HAM8, AMIGA_HAM8_FLICKERFREE ->
                    false;
            default -> true;
        };
        boolean disableDithering = switch (cm) {
            case _24_BIT_RGB -> true;

            default -> false;
        };
        boolean disableLoadPalette = disablePalette || pm != PaletteMode.LOADED_PALETTE;
        ditheringMethodComboBox.setDisable(disableDithering);
        ditherIntensityField.setDisable(disableDithering);
        paletteModeComboBox.setDisable(disablePalette);
        paletteSizeField.setDisable(disablePalette);
        loadPaletteButton.setDisable(disableLoadPalette);
        computePaletteButton.setDisable(disableLoadPalette);
        palettePane.setVisible(!disablePalette);
        lockPalette0CheckBox.setDisable(disablePalette);
        palette0ColorField.setDisable(disablePalette);
    }

    public static ColorInspectorController newInstance() {
        try {
            FXMLLoader loader = new FXMLLoader(ColorInspectorController.class.getResource("ColorInspector.fxml"));
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
            inputImageColorsLabel.textProperty().unbind();
            inputImageColorSpaceLabel.textProperty().unbind();
            colorModeComboBox.valueProperty().unbindBidirectional(oldv.colorModeProperty());
            paletteModeComboBox.valueProperty().unbindBidirectional(oldv.paletteModeProperty());
            ditheringMethodComboBox.valueProperty().unbindBidirectional(oldv.ditheringMethodProperty());
            ditherIntensityField.textProperty().unbindBidirectional(oldv.ditherIntensityFactorProperty());
            paletteSizeField.textProperty().unbindBidirectional(oldv.paletteSizeProperty());
            oldv.indexColorModelProperty().removeListener((ChangeListener<? super IndexColorModel>) colorModelListener);
            palette0ColorField.textProperty().unbindBidirectional(oldv.palette0ColorProperty());
            lockPalette0CheckBox.selectedProperty().unbindBidirectional(oldv.lockPaletteIndex0Property());
            colorSpaceComboBox.valueProperty().unbindBidirectional(oldv.referenceImageColorSpaceProperty());
        }
        if (newv != null) {
            inputImageColorSpaceLabel.textProperty().bind(newv.rawReferenceImageProperty().map(
                    img -> {
                        if (img == null) return "no image";
                        if (img.getColorModel().getColorSpace() instanceof ICC_ColorSpace iccColorSpace) {
                            return iccColorSpace.getProfile().toString();
                        } else {
                            return img.getColorModel().getColorSpace().toString();
                        }
                    })
            );
            inputImageColorsLabel.textProperty().bind(newv.rawReferenceImageProperty().map(

                    img -> switch (img == null ? null : img.getType()) {
                        case null -> "no image";
                        case BufferedImage.TYPE_INT_RGB -> "24 bit RGB, int";
                        case BufferedImage.TYPE_INT_ARGB -> "32 bit ARGB, int";
                        case BufferedImage.TYPE_INT_ARGB_PRE -> "32 bit ARGB premultiplied, int";
                        case BufferedImage.TYPE_INT_BGR -> "24 bit BGR, int";
                        case BufferedImage.TYPE_3BYTE_BGR -> "24 bit RGB, 3 bytes";
                        case BufferedImage.TYPE_4BYTE_ABGR -> "24 bit ARGB, 4 bytes";
                        case BufferedImage.TYPE_4BYTE_ABGR_PRE -> "24 bit ARGB premultiplied, 4 bytes";
                        case BufferedImage.TYPE_BYTE_GRAY -> "8 bit Gray, byte";
                        case BufferedImage.TYPE_BYTE_BINARY -> "1, 2 or 4-bit indexed Colors, byte packed";
                        case BufferedImage.TYPE_BYTE_INDEXED -> "8 bit indexed Colors, byte";
                        case BufferedImage.TYPE_USHORT_GRAY -> "16-bit Gray, ushort";
                        case BufferedImage.TYPE_USHORT_565_RGB -> "5-6-5 bit RGB, ushort";
                        case BufferedImage.TYPE_USHORT_555_RGB -> "5-5-5 bit RGB, ushort";
                        case BufferedImage.TYPE_CUSTOM -> "custom";
                        default -> "type=" + img.getType();
                    }
            ));
            colorSpaceComboBox.valueProperty().bindBidirectional(newv.referenceImageColorSpaceProperty());
            colorModeComboBox.valueProperty().bindBidirectional(newv.colorModeProperty());
            paletteModeComboBox.valueProperty().bindBidirectional(newv.paletteModeProperty());
            ditheringMethodComboBox.valueProperty().bindBidirectional(newv.ditheringMethodProperty());
            ditherIntensityField.textProperty().bindBidirectional(newv.ditherIntensityFactorProperty(), new NumberStringConverter());
            paletteSizeField.textProperty().bindBidirectional(newv.paletteSizeProperty(), new NumberStringConverter());
            newv.indexColorModelProperty().addListener((ChangeListener<? super IndexColorModel>) colorModelListener);
            colorModelListener.changed(newv.indexColorModelProperty(), null, newv.getIndexColorModel());
            lockPalette0CheckBox.selectedProperty().bindBidirectional(newv.lockPaletteIndex0Property());
            palette0ColorField.textProperty().bindBidirectional(newv.palette0ColorProperty(), new StringConverter<Color>() {
                @Override
                public String toString(Color object) {
                    return object == null ? "" : "#" + object.toString().substring(2, 8);
                }

                @Override
                public Color fromString(String string) {
                    if (string == null || string.isBlank()) return null;
                    try {
                        return Color.web(string);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                }
            });
        }
    }
}

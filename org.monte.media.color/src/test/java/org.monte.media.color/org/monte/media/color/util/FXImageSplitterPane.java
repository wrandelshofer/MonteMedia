/*
 * @(#)FXImageSplitter.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.util;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;


public class FXImageSplitterPane extends VBox {
    private final ObjectProperty<Image> img1 = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> img2 = new SimpleObjectProperty<>();

    public FXImageSplitterPane() {
        init();
    }

    public Image getImg1() {
        return img1.get();
    }

    public Image getImg2() {
        return img2.get();
    }

    public void setImg1(Image img1) {
        this.img1.set(img1);
    }

    public void setImg2(Image img2) {
        this.img2.set(img2);
    }

    public ObjectProperty<Image> img1Property() {
        return img1;
    }

    public ObjectProperty<Image> img2Property() {
        return img2;
    }

    private void init() {
        ImageView img2View = new ImageView();
        ImageView img1View = new ImageView();
        img2View.imageProperty().bind(img2);
        img1View.imageProperty().bind(img1);

        // 2. Setup the Clipping Mask for the top image
        Rectangle clipMask = new Rectangle();
        clipMask.heightProperty().bind(img1.map(Image::getHeight));
        img1View.setClip(clipMask);

        // 3. Create a Slider for interaction
        Slider splitSlider = new Slider(0.0, 1.0, 0.5);

        // 4. Bind the clip width to the slider value
        clipMask.widthProperty().bind(splitSlider.valueProperty().map(v -> (img1.get() == null) ? 160 : img1.get().getWidth() * v.doubleValue()));

        // 5. Layout: Stack the images, then place slider below
        StackPane imageContainer = new StackPane(img2View, img1View);
        getChildren().addAll(imageContainer, splitSlider);
    }
}
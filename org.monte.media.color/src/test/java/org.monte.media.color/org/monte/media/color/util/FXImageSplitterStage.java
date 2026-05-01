/*
 * @(#)FXImageSplitterStage.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.util;


import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FXImageSplitterStage {
    private Stage stage;
    private FXImageSplitterPane panel;
    private final ObjectProperty<Image> img1 = new SimpleObjectProperty<>();
    private final ObjectProperty<Image> img2 = new SimpleObjectProperty<>();

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

    public FXImageSplitterStage() {
    }


    public void show() {
        Platform.runLater(() -> {
            if (stage == null) {
                stage = new Stage();
                panel = new FXImageSplitterPane();
                stage.setScene(new Scene(panel));
            }
            panel.img1Property().bind(img1);
            panel.img2Property().bind(img2);
            stage.sizeToScene();
            stage.show();
        });
    }
}

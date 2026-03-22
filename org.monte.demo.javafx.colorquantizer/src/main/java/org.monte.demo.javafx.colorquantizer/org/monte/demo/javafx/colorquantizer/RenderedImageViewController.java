/*
 * @(#)RenderedImageViewController.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer;

import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import org.monte.media.amigabitmap.AmigaBitmapImageConverter;
import org.monte.media.amigabitmap.AmigaHAMColorModel;
import org.monte.media.image.algo.NearestNeighbourResampleAlgoFloat;

import java.awt.image.BufferedImage;
import java.util.stream.IntStream;

public class RenderedImageViewController {
    private ScrollPane root = new ScrollPane();
    private ImageView imageView = new ImageView();
    private final IntegerProperty zoom = new SimpleIntegerProperty(0);
    private final ObjectProperty<BufferedImage> image = new SimpleObjectProperty<>();

    public RenderedImageViewController() {
        init();
    }

    private void updateImage(Observable observable) {
        BufferedImage newImg = image.get();
        if (newImg != null && newImg.getColorModel() instanceof AmigaHAMColorModel) {
            newImg = AmigaBitmapImageConverter.newInstance().hamToRgb(newImg, null);
        }
        Image fxImg = newImg == null ? null : SwingFXUtils.toFXImage(newImg, null);

        if (zoom.get() == 0 || fxImg == null) {
            imageView.setImage(fxImg);
        } else {
            new NearestNeighbourResampleAlgoFloat();
            var img = imageView.getImage();
            int width = img == null ? 320 : (int) img.getWidth();
            int zoomValue = zoom.get();
            float scaleFactor = (float) Math.pow(2, zoomValue);
            imageView.setImage(resample(fxImg, scaleFactor));
        }
    }

    private Image resample(Image input, float scaleFactor) {
        int srcWidth = (int) input.getWidth();
        int srcHeight = (int) input.getHeight();
        int dstWidth = Math.max((int) (srcWidth * scaleFactor), 1);
        int dstHeight = Math.max((int) (srcHeight * scaleFactor), 1);
        WritableImage output = new WritableImage(dstWidth, dstHeight);
        resample(input.getPixelReader(), srcWidth, srcHeight, 0, srcWidth,
                output.getPixelWriter(), dstWidth, dstHeight, 0, dstWidth);
        return output;
    }

    public void resample(PixelReader srcPixels, int srcWidth, int srcHeight, int srcOffset, int srcScanline, PixelWriter dstPixels, int dstWidth, int dstHeight, int dstOffset, int dstScanline) {
        // scale factors
        float sx = srcWidth / (float) dstWidth;
        float sy = srcHeight / (float) dstHeight;

        // translation
        int tx = (int) (sx * 0.5f);
        int ty = (int) (sy * 0.5f);

        //for (int destY = 0; destY < dstHeight; destY++) {
        IntStream.range(0, dstHeight).forEach(destY -> {
            int srcY = (int) (destY * sy) + ty;
            for (int destX = 0; destX < dstWidth; destX++) {
                int srcX = (int) (destX * sx) + tx;
                dstPixels.setArgb(destX, destY, srcPixels.getArgb(srcX, srcY));
            }
        });
    }

    public int getZoom() {
        return zoom.get();
    }

    public IntegerProperty zoomProperty() {
        return zoom;
    }

    private void init() {
        imageView.setPreserveRatio(true);
        root.setContent(imageView);
        root.setPrefSize(640, 480);
        zoom.addListener(this::updateImage);
        imageView.setSmooth(false);
        image.addListener(this::updateImage);
    }

    public Node getRoot() {
        return root;
    }

    public BufferedImage getImage() {
        return image.get();
    }

    public void setImage(BufferedImage newValue) {
        image.set(newValue);
    }

    public ObjectProperty<BufferedImage> imageProperty() {
        return image;
    }
}

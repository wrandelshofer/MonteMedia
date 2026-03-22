/*
 * @(#)RenderedImageViewController.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.colorquantizer;

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import org.monte.media.amigabitmap.AmigaBitmapImageConverter;
import org.monte.media.amigabitmap.AmigaHAMColorModel;
import org.monte.media.image.algo.NearestNeighbourResampleAlgoFloat;

import java.awt.image.BufferedImage;
import java.nio.IntBuffer;
import java.util.concurrent.ForkJoinPool;

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
        int width = Math.max((int) (srcWidth * scaleFactor), 1);
        int height = Math.max((int) (srcHeight * scaleFactor), 1);
        if ((double) width * height * 4.0 > Integer.MAX_VALUE) {
            return null;
        }

        IntBuffer intBuffer = IntBuffer.allocate(width * height);
        PixelFormat<IntBuffer> pixelFormat = PixelFormat.getIntArgbPreInstance();
        PixelBuffer<IntBuffer> pixelBuffer = new PixelBuffer<>(width, height, intBuffer, pixelFormat);
        Image output = new WritableImage(pixelBuffer);

        var in = input.getPixelReader();
        var out = intBuffer;
        ForkJoinPool.commonPool().submit(() -> resample(in, srcWidth, srcHeight, 0, srcWidth,
                out, pixelBuffer, width, height, 0, width));

        return output;
    }

    public void resample(PixelReader srcPixels, int srcWidth, int srcHeight, int srcOffset, int srcScanline, IntBuffer dstPixels, PixelBuffer<IntBuffer> pixelBuffer, int dstWidth, int dstHeight, int dstOffset, int dstScanline) {
        // scale factors
        float sx = srcWidth / (float) dstWidth;
        float sy = srcHeight / (float) dstHeight;

        // translation
        int tx = (int) (sx * 0.5f);
        int ty = (int) (sy * 0.5f);

        int block = 256;

        var dst = dstPixels.array();
        for (int y = 0; y < dstHeight; y += block) {
            //IntStream.range(0, dstHeight).parallel().forEach(y -> {
            for (int x = 0; x < dstWidth; x += block) {
                int blockx = Math.min(dstWidth, x + block) - x;
                int blocky = Math.min(dstHeight, y + block) - y;
                for (int yy = 0; yy < blocky; yy++) {
                    int srcY = (int) ((y + yy) * sy) + ty;
                    for (int xx = 0; xx < blockx; xx++) {
                        int srcX = (int) ((x + xx) * sx) + tx;
                        dst[(yy + y) * dstWidth + x + xx] = srcPixels.getArgb(srcX, srcY);
                    }
                }
                final int finalX = x;
                final int finalY = y;
                Platform.runLater(() -> pixelBuffer.updateBuffer(pbuf -> new Rectangle2D(finalX, finalY, blockx, blocky)));
            }
        }
        Platform.runLater(() -> pixelBuffer.updateBuffer(pbuf -> null));
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

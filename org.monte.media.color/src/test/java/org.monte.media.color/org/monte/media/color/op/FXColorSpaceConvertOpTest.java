/*
 * @(#)ColorSpaceImageOpTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.op;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.Window;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.monte.media.color.RecBT2020ColorSpace;
import org.monte.media.color.util.FXImageSplitterStage;
import org.monte.media.color.util.TestImageFactory;

import java.awt.image.BufferedImage;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class FXColorSpaceConvertOpTest {
    @Test
    void shouldConvertColorSpaceImage() {
        ColorSpaceConvertOp op = new ColorSpaceConvertOp();
        BufferedImage src = TestImageFactory.createRgbCubeFaces();
        BufferedImage dst = TestImageFactory.createImage(RecBT2020ColorSpace.getInstance(), src.getWidth(), src.getHeight(), 8);
        //BufferedImage dst = TestImageFactory.createImage(ColorSpace.getInstance(ColorSpace.CS_sRGB), src.getWidth(), src.getHeight(), 8);
        op.filter(src, dst);
        var frame = new FXImageSplitterStage();
        frame.setImg1(SwingFXUtils.toFXImage(src, null));
        frame.setImg2(SwingFXUtils.toFXImage(dst, null));
        frame.show();
    }

    @BeforeAll
    public static void beforeAll() {
        Platform.startup(() -> {
        });
    }

    @AfterAll
    public static void afterAll() {
        CompletableFuture<Boolean> f = new CompletableFuture<>();
        Platform.runLater(() -> {
            for (Window w : Window.getWindows()) {
                IO.println(w);
                w.showingProperty().addListener((v, o, n) -> {
                    if (!n) f.complete(true);
                });
            }
        });
        try {
            f.get(300_000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            throw new RuntimeException(e);
        }
        for (Window w : Window.getWindows()) {
            w.hide();
        }
    }
}
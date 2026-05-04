/*
 * @(#)ColorSpaceImageOpTest.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.op;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.monte.media.color.RecBT2020ColorSpace;
import org.monte.media.color.util.ImageSplitterJFrame;
import org.monte.media.color.util.TestImageFactory;

import java.awt.Window;
import java.awt.image.BufferedImage;

@Disabled
public class SwingColorSpaceConvertOpTest {
    @Test
    void shouldConvertColorSpaceImage() {
        ColorSpaceConvertOp op = new ColorSpaceConvertOp();
        BufferedImage src = TestImageFactory.createRgbCubeFaces();
        BufferedImage dst = TestImageFactory.createImage(RecBT2020ColorSpace.getInstance(), src.getWidth(), src.getHeight(), 8);
        //BufferedImage dst = TestImageFactory.createImage(ColorSpace.getInstance(ColorSpace.CS_CIEXYZ), src.getWidth(), src.getHeight(), 8);
        op.filter(src, dst);
        var frame = new ImageSplitterJFrame();
        System.out.println("src:" + src);
        System.out.println("dst:" + dst);
        frame.setImg1(src);
        frame.setImg2(dst);
        frame.show();
    }

    @AfterAll
    public static void afterAll() {
        /*
        try {
            Thread.sleep(300_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        for (Window w : Window.getWindows()) {
            w.dispose();
        }
    }
}
/*
 * @(#)AmigaBufferedImageConverter.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.amigabitmap;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

public interface AmigaBitmapImageConverter {
    /**
     * Converts an {@link AmigaBitmapImage} to {@link BufferedImage}, creating
     * a new object if needed.
     *
     * @param input the input image
     * @return the converted image
     */
    default BufferedImage toBufferedImage(AmigaBitmapImage input) {
        return toBufferedImage(input, null);
    }

    /**
     * Converts an {@link BufferedImage} to {@link AmigaBitmapImage}, creating
     * a new object if needed.
     *
     * @param input the input image
     * @return the converted image
     */
    default AmigaBitmapImage toBitmapImage(BufferedImage input) {
        return toBitmapImage(input, null);
    }

    /**
     * Converts an {@link BufferedImage} to {@link AmigaBitmapImage}, creating
     * a new object if needed.
     *
     * @param input the input image
     * @return the converted image
     */
    default AmigaBitmapImage toBitmapImage(ImageProducer input) {
        return toBitmapImage(input, null);
    }

    /**
     * Converts an {@link AmigaBitmapImage} to {@link BufferedImage}, creating
     * a new object if needed.
     *
     * @param input  the input image
     * @param output an optional buffered image that can be used to store the output data
     * @return the converted image
     */
    BufferedImage toBufferedImage(AmigaBitmapImage input, BufferedImage output);

    /**
     * Converts an {@link BufferedImage} to {@link AmigaBitmapImage}, creating
     * a new object if needed.
     *
     * @param input  the input image
     * @param output an optional bitmap image that can be used to store the output data
     * @return the converted image
     */
    AmigaBitmapImage toBitmapImage(BufferedImage input, AmigaBitmapImage output);

    /**
     * Converts an {@link BufferedImage} to {@link AmigaBitmapImage}, creating
     * a new object if needed.
     *
     * @param input  the input image
     * @param output an optional bitmap image that can be used to store the output data
     * @return the converted image
     */
    default AmigaBitmapImage toBitmapImage(ImageProducer input, AmigaBitmapImage output) {
        class MyImageConsumer implements ImageConsumer {
            int width = 0;
            int height = 0;
            ColorModel colorModel = null;
            byte[] bytePixels;
            int[] intPixels;
            boolean imageComplete;

            public MyImageConsumer() {
            }

            @Override
            public void setDimensions(int width, int height) {
                this.width = width;
                this.height = height;
            }

            @Override
            public void setProperties(Hashtable<?, ?> props) {

            }

            @Override
            public void setColorModel(ColorModel model) {
                colorModel = model;
            }

            @Override
            public void setHints(int hintflags) {

            }

            @Override
            public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) {
                if (bytePixels == null || bytePixels.length != width * height) {
                    bytePixels = new byte[width * height];
                }
                int inputScan = off + y * scansize + x;
                int outputScan = y * width;
                for (int i = 0; i < h; i++) {
                    System.arraycopy(pixels, inputScan, bytePixels, outputScan, w);
                    inputScan += scansize;
                    outputScan += width;
                }
            }

            @Override
            public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
                if (intPixels == null || intPixels.length != width * height) {
                    intPixels = new int[width * height];
                }
                int inputScan = off + y * scansize + x;
                int outputScan = y * width;
                for (int i = 0; i < h; i++) {
                    System.arraycopy(pixels, inputScan, intPixels, outputScan, w);
                    inputScan += scansize;
                    outputScan += width;
                }
            }

            @Override
            public void imageComplete(int status) {
                imageComplete = true;
            }
        }
        MyImageConsumer ic = new MyImageConsumer();
        input.startProduction(ic);
        if (!ic.imageComplete || ic.bytePixels == null && ic.intPixels == null) {
            throw new IllegalArgumentException("can not convert the image");
        }
        BufferedImage bufferedImage = ic.bytePixels != null
                ? new BufferedImage(ic.colorModel,
                WritableRaster.createPackedRaster(new DataBufferByte(ic.bytePixels, ic.bytePixels.length),
                        ic.width, ic.height, ic.width, new int[]{0xff}, new Point(0, 0)),
                false,
                null)
                : new BufferedImage(ic.colorModel,
                WritableRaster.createPackedRaster(new DataBufferInt(ic.intPixels, ic.intPixels.length),
                        ic.width, ic.height, ic.width, new int[]{0xff0000, 0x00ff00, 0x0000ff}, new Point(0, 0)),
                false,
                null);
        return toBitmapImage(bufferedImage, output);
    }

    /**
     * Creates a new instance that is optimized for the current operating system architecture.
     *
     * @return a new instance
     */
    public static AmigaBitmapImageConverter newInstance() {
        return ("x86_64".equals(System.getProperty("os.arch"))) ? new ParallelExtractAmigaBitmapImageConverter() : new LongMultiplyAmigaBitmapImageConverter();
    }


}

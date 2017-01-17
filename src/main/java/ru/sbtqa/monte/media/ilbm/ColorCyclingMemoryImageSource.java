/* @(#)ColorCyclingMemoryImageSource.java
 * Copyright © 2009-2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.ilbm;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageConsumer;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import javax.swing.Timer;

/**
 * ColorCyclingMemoryImageSource.
 *
 * @author Werner Randelshofer
 * @version 1.1 2010-08-03 Added method putProperties. Added support for blended
 * color cycles.
 * <br>1.0.1 2010-11-08 Fixed color cycling rate.
 * <br>1.0 2009-12-17 Created.
 */
public class ColorCyclingMemoryImageSource extends MemoryImageSource {

    private int width;
    private int height;
    private ColorModel model;
    private Object pixels;
    private int pixeloffset;
    private int pixelscan;
    private Hashtable<?, ?> properties;
    private ArrayList<ColorCycle> colorCycles = new ArrayList<ColorCycle>();
    private Timer timer;
    private HashSet<ImageConsumer> consumers = new HashSet<ImageConsumer>();
    /**
     * Whether color cycling is available.
     */
    private boolean isColorCyclingAvailable;
    /**
     * Whether color cycling is started.
     */
    private boolean isStarted;
    /**
     * Whether color cycles are blended.
     */
    private boolean isBlendedColorCycling;
    private volatile ColorModel cycledModel;

    /**
     * Constructs an ImageProducer object which uses an array of bytes to
     * produce data for an Image object.
     *
     * @param w the width of the rectangle of pixels
     * @param h the height of the rectangle of pixels
     * @param cm the specified <code>ColorModel</code>
     * @param pix an array of pixels
     * @param off the offset into the array of where to store the first pixel
     * @param scan the distance from one row of pixels to the next in the array
     * @see java.awt.Component#createImage
     */
    public ColorCyclingMemoryImageSource(int w, int h, ColorModel cm,
          byte[] pix, int off, int scan) {
        super(w, h, cm, pix, off, scan);
        initialize(w, h, cm, (Object) pix, off, scan, new Hashtable<Object, Object>());
    }

    /**
     * Constructs an ImageProducer object which uses an array of bytes to
     * produce data for an Image object.
     *
     * @param w the width of the rectangle of pixels
     * @param h the height of the rectangle of pixels
     * @param cm the specified <code>ColorModel</code>
     * @param pix an array of pixels
     * @param off the offset into the array of where to store the first pixel
     * @param scan the distance from one row of pixels to the next in the array
     * @param props a list of properties that the <code>ImageProducer</code>
     * uses to process an image
     * @see java.awt.Component#createImage
     */
    public ColorCyclingMemoryImageSource(int w, int h, ColorModel cm,
          byte[] pix, int off, int scan,
          Hashtable<?, ?> props) {
        super(w, h, cm, pix, off, scan, props);
        initialize(w, h, cm, (Object) pix, off, scan, props);
    }

    /**
     * Constructs an ImageProducer object which uses an array of integers to
     * produce data for an Image object.
     *
     * @param w the width of the rectangle of pixels
     * @param h the height of the rectangle of pixels
     * @param cm the specified <code>ColorModel</code>
     * @param pix an array of pixels
     * @param off the offset into the array of where to store the first pixel
     * @param scan the distance from one row of pixels to the next in the array
     * @see java.awt.Component#createImage
     */
    public ColorCyclingMemoryImageSource(int w, int h, ColorModel cm,
          int[] pix, int off, int scan) {
        super(w, h, cm, pix, off, scan);
        initialize(w, h, cm, (Object) pix, off, scan, null);
    }

    /**
     * Constructs an ImageProducer object which uses an array of integers to
     * produce data for an Image object.
     *
     * @param w the width of the rectangle of pixels
     * @param h the height of the rectangle of pixels
     * @param cm the specified <code>ColorModel</code>
     * @param pix an array of pixels
     * @param off the offset into the array of where to store the first pixel
     * @param scan the distance from one row of pixels to the next in the array
     * @param props a list of properties that the <code>ImageProducer</code>
     * uses to process an image
     * @see java.awt.Component#createImage
     */
    public ColorCyclingMemoryImageSource(int w, int h, ColorModel cm,
          int[] pix, int off, int scan,
          Hashtable<?, ?> props) {
        super(w, h, cm, pix, off, scan, props);
        initialize(w, h, cm, (Object) pix, off, scan, props);
    }

    private void initialize(int w, int h, ColorModel cm,
          Object pix, int off, int scan, Hashtable<?, ?> props) {
        width = w;
        height = h;
        model = cm;
        pixels = pix;
        pixeloffset = off;
        pixelscan = scan;
        properties = (props == null) ? new Hashtable<Object, Object>() : props;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ColorModel getColorModel() {
        return model;
    }

    public Hashtable<?, ?> getProperties() {
        return properties;
    }

    @Override
    public synchronized void newPixels(byte[] newpix, ColorModel newmodel,
          int offset, int scansize) {
        this.pixels = newpix;
        this.model = newmodel;
        this.pixeloffset = offset;
        this.pixelscan = scansize;
        super.newPixels(newpix, cycledModel == null ? newmodel : cycledModel, offset, scansize);
    }

    /**
     * Changes to a new int array to hold the pixels for this image. If the
     * animation flag has been turned on through the setAnimated() method, then
     * the new pixels will be immediately delivered to any ImageConsumers that
     * are currently interested in the data for this image.
     *
     * @param newpix the new pixel array
     * @param newmodel the specified <code>ColorModel</code>
     * @param offset the offset into the array
     * @param scansize the distance from one row of pixels to the next in the
     * array
     * @see #newPixels(int, int, int, int, boolean)
     * @see #setAnimated
     */
    @Override
    public synchronized void newPixels(int[] newpix, ColorModel newmodel,
          int offset, int scansize) {
        this.pixels = newpix;
        this.model = newmodel;
        this.pixeloffset = offset;
        this.pixelscan = scansize;
        super.newPixels(newpix, cycledModel == null ? newmodel : cycledModel, offset, scansize);
    }

    public void addColorCycle(ColorCycle cc) {
        colorCycles.add(cc);
    }

    @Override
    public void addConsumer(ImageConsumer ic) {
        super.addConsumer(ic);
        consumers.add(ic);
        if (isStarted && !consumers.isEmpty()) {
            startAnimationTimer();
        }
    }

    @Override
    public void removeConsumer(ImageConsumer ic) {
        super.removeConsumer(ic);
        consumers.remove(ic);
        if (isStarted && consumers.isEmpty()) {
            stopAnimationTimer();
        }
    }

    @Override
    public void setAnimated(boolean b) {
        super.setAnimated(b);
        isColorCyclingAvailable = b;

        if (isColorCyclingAvailable && !consumers.isEmpty() && isStarted) {
            startAnimationTimer();
        } else {
            stopAnimationTimer();
        }
    }

    /**
     * Starts or stops color cycling.
     *
     * @param b TODO
     */
    public void setColorCyclingStarted(boolean b) {
        isStarted = b;
        if (isColorCyclingAvailable && !consumers.isEmpty() && isStarted) {
            startAnimationTimer();
        } else {
            stopAnimationTimer();
        }
    }

    /**
     * Returns true if color cycling is on.
     *
     * @return TODO
     */
    public boolean isColorCyclingStarted() {
        return isStarted;
    }

    /**
     * Starts color cycling.
     */
    public void start() {
        setColorCyclingStarted(true);
    }

    /**
     * Stops color cycling.
     */
    public void stop() {
        setColorCyclingStarted(false);
    }

    public boolean isStarted() {
        return isColorCyclingStarted();
    }

    private synchronized void startAnimationTimer() {
        if (timer != null) {
            return;
        }
        if (model instanceof IndexColorModel) {
            final IndexColorModel icm = (IndexColorModel) model;
            final int[] rgbs = new int[icm.getMapSize()];
            icm.getRGBs(rgbs);

            // Calculate the timer delay
            int delay = 1000;
            int i = 0;
            if (isBlendedColorCycling) {
                for (ColorCycle cc : colorCycles) {
                    if (cc.isActive()) {
                        // Note: we divide 1000 by 4
                        // 2 for Nyquist Theorem (double sample rate)
                        // 2 for blending
                        int ccDelay = 1000 / 4 * cc.getTimeScale() / cc.getRate();
                        if (ccDelay < delay) {
                            delay = Math.max(1, ccDelay);
                        }
                    }
                }
                delay = Math.max(delay, 1000 / 60); // throttle at 60 fps
            } else {
                for (ColorCycle cc : colorCycles) {
                    if (cc.isActive()) {
                        // Note: we divide 1000 by 2 (=double sampling rate)
                        // because of Nyquist theorem
                        int ccDelay = 1000 / 2 * cc.getTimeScale() / cc.getRate();
                        if (ccDelay < delay) {
                            delay = Math.max(1, ccDelay);
                        }
                    }
                }
            }

            timer = new Timer(delay, new ActionListener() {

                private int[] previousCycled = new int[rgbs.length];
                private int[] cycled = new int[rgbs.length];
                long startTime = System.currentTimeMillis();

                @Override
                public void actionPerformed(ActionEvent evt) {
                    long now = System.currentTimeMillis();
                    System.arraycopy(rgbs, 0, cycled, 0, rgbs.length);
                    for (ColorCycle cc : colorCycles) {
                        cc.doCycle(cycled, now - startTime);
                    }
                    // We only fire new pixels, if the cycles have changed
                    if (!Arrays.equals(previousCycled, cycled)) {
                        ColorCyclingMemoryImageSource.super.newPixels((byte[]) pixels, //
                              cycledModel = new IndexColorModel(8, cycled.length, cycled, 0, false, -1, DataBuffer.TYPE_BYTE),
                              pixeloffset,
                              pixelscan);
                    }
                    // swap cycled colors
                    int[] tmp = previousCycled;
                    previousCycled = cycled;
                    cycled = tmp;
                }
            });
            timer.setRepeats(true);
            timer.start();
        }
    }

    private synchronized void stopAnimationTimer() {
        if (timer != null) {
            timer.stop();
            timer = null;
            cycledModel = null;
            // Reset colors to their initial state
            ColorCyclingMemoryImageSource.super.newPixels((byte[]) pixels, //
                  model,
                  pixeloffset,
                  pixelscan);
        }

    }

    /**
     * Creates a BufferedImage which shares its pixel data with this memory
     * image source.
     *
     * @return TODO
     */
    public BufferedImage toBufferedImage() {
        DataBuffer buf = (pixels instanceof byte[])
              ?//
              new DataBufferByte((byte[]) pixels, pixelscan * height, pixeloffset)
              ://
              new DataBufferInt((int[]) pixels, pixelscan * height, pixeloffset);
        SampleModel sm = model.createCompatibleSampleModel(width, height);
        WritableRaster raster = Raster.createWritableRaster(sm, buf, new Point());
        return new BufferedImage(model, raster, false, properties);
    }

    public boolean isColorCyclingAvailable() {
        return isColorCyclingAvailable;
    }

    @SuppressWarnings("unchecked")
    public void putProperties(Map<?, ?> props) {
        properties.putAll((Map) props);
    }

    public void setBlendedColorCycling(boolean newValue) {
        isBlendedColorCycling = newValue;
        for (ColorCycle cc : colorCycles) {
            cc.setBlended(newValue);
        }
        if (isStarted()) {
            stop();
            start();
        }
    }

    public boolean isBlendedColorCycling() {
        return isBlendedColorCycling;
    }
}

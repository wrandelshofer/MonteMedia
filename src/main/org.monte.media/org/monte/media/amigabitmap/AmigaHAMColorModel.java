/* @(#)MultiShow.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */
package org.monte.media.amigabitmap;

import java.awt.image.DirectColorModel;

/**
 * ColorModel for HAM compressed images.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.1 2006-12-25 New constructor added.
 * <br>1.0  1999-10-19
 */
public class AmigaHAMColorModel extends DirectColorModel {
    //insert class definition here
    public final static int
            HAM6 = 6,
            HAM8 = 8;
    
    protected int HAMType;
    protected int map_size;
    protected boolean opaque;
    protected int[] rgb;
    
    /**
     * Creates a new HAM Color model using the specified base colors.
     *
     * @param aHAMType Type, must be HAM6 or HAM 8.
     * @param size The size of the color palette.
     * @param r The red colors as 8 bit or as 4 bit values.
     * @param g The green colors as 8 bit or as 4 bit values.
     * @param b The blue colors as 8 bit or as 4 bit values.
     * @param isOCS Set this to true if the colors are 4 bit values.
     */
    public AmigaHAMColorModel(int aHAMType,int size,byte r[],byte g[],byte b[], boolean isOCS) {
        super(24,0x00ff0000,0x0000ff00,0x000000ff);
        if (aHAMType != HAM6 && aHAMType != HAM8) {
            throw new IllegalArgumentException("Unknown HAM Type: " + aHAMType);
        }
        HAMType = aHAMType;
        if (isOCS) {
            byte[] r8 = new byte[size];
            byte[] g8 = new byte[size];
            byte[] b8 = new byte[size];
            for (int i=0; i < size; i++) {
                r8[i] = (byte) (((r[i] & 0xf) << 4) | (r[i] & 0xf));
                g8[i] = (byte) (((g[i] & 0xf) << 4) | (g[i] & 0xf));
                b8[i] = (byte) (((b[i] & 0xf) << 4) | (b[i] & 0xf));
            }
            setRGBs(size,r8,g8,b8,null);
        } else {
            setRGBs(size,r,g,b,null);
        }
    }
    
    /**
     * Creates a new HAM Color model using the specified base colors.
     *
     * @param aHAMType Type, must be HAM6 or HAM 8.
     * @param size The size of the color palette.
     * @param rgb The rgb colors.
     * @param isOCS Set this to true if the colors are 12 bit precision only.
     */
    public AmigaHAMColorModel(int aHAMType,int size,int rgb[], boolean isOCS) {
        super(24,0x00ff0000,0x0000ff00,0x000000ff);
        if (aHAMType != HAM6 && aHAMType != HAM8) {
            throw new IllegalArgumentException("Unknown HAM Type: " + aHAMType);
        }
        
        HAMType = aHAMType;
        if (isOCS) {
            byte[] r = new byte[rgb.length];
            byte[] g = new byte[rgb.length];
            byte[] b = new byte[rgb.length];
            for (int i=0; i < rgb.length; i++) {
                r[i] = (byte) (((rgb[i] & 0xf00) >>> 8) |
                        (rgb[i] & 0xf00) >>> 4);
                g[i] = (byte) (((rgb[i] & 0xf0) >>> 4) |
                        (rgb[i] & 0xf0));
                b[i] = (byte) (((rgb[i] & 0xf) ) |
                        (rgb[i] & 0xf) << 4);
            }
            setRGBs(size,r,g,b,null);
        } else {
            byte[] r = new byte[size];
            byte[] g = new byte[size];
            byte[] b = new byte[size];
            for (int i=0; i < size; i++) {
                r[i] = (byte) ((rgb[i] & 0xff0000) >>> 16);
                g[i] = (byte) ((rgb[i] & 0xff00) >>> 8);
                b[i] = (byte) (rgb[i] & 0xff);
            }
            setRGBs(size,r,g,b,null);
        }
    }
    
    /**
     * Returns the HAM Type of this AmigaHAMColorModel: HAM8 or HAM6.
     */
    public int getHAMType() {
        return HAMType;
    }
    
    /**
     * Returns the number of planes required to represent
 this AmigaHAMColorModel in a Bitmap.
     */
    public int getDepth() {
        return HAMType;
    }
    
    /**
     * Sets the HAM base colors.
     * @param size The size of the color palette.
     * @param r The red colors as 8 bit values.
     * @param g The green colors as 8 bit values.
     * @param b The blue colors as 8 bit values.
     * @param a The alpha channels as 8 bit values.
     */
    protected void setRGBs(int size, byte r[], byte g[], byte b[], byte a[]) {
        if (size > 256) {
            throw new ArrayIndexOutOfBoundsException();
        }
        map_size = size;
        rgb = new int[256];
        int alpha = 0xff;
        opaque = true;
        for (int i = 0; i < size; i++) {
            if (a != null) {
                alpha = (a[i] & 0xff);
                if (alpha != 0xff) {
                    opaque = false;
                }
            }
            rgb[i] = (alpha << 24)
            | ((r[i] & 0xff) << 16)
            | ((g[i] & 0xff) << 8)
            | (b[i] & 0xff);
        }
    }
    
    /**
     * Copies the array of red color components into the given array.  Only
     * the initial entries of the array as specified by getMapSize() are
     * written.
     */
    final public void getReds(byte r[]) {
        for (int i = 0; i < map_size; i++) {
            r[i] = (byte) (rgb[i] >> 16);
        }
    }
    
    /**
     * Copies the array of green color components into the given array.  Only
     * the initial entries of the array as specified by getMapSize() are
     *  written.
     */
    final public void getGreens(byte g[]) {
        for (int i = 0; i < map_size; i++) {
            g[i] = (byte) (rgb[i] >> 8);
        }
    }
    
    /**
     * Copies the array of blue color components into the given array.  Only
     * the initial entries of the array as specified by getMapSize() will
     * be written.
     */
    final public void getBlues(byte b[]) {
        for (int i = 0; i < map_size; i++) {
            b[i] = (byte) rgb[i];
        }
    }
    /**
     * Copies the array of color components into the given array.  Only
     * the initial entries of the array as specified by getMapSize() will
     * be written.
     */
    final public void getRGBs(int rgbs[]) {
        for (int i = 0; i < map_size; i++) {
            rgbs[i] = rgb[i];
        }
    }
    /**
     * Returns the size of the color component arrays in this IndexColorModel.
     */
    final public int getMapSize() {
        return map_size;
    }
}

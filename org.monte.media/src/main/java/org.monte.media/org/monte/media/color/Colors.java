/*
 * @(#)Colors.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;

import java.awt.Transparency;
import java.awt.image.IndexColorModel;

/**
 * {@code Colors}.
 *
 * @author Werner Randelshofer
 */
public class Colors {

    /**
     * Prevent instance creation.
     */
    private Colors() {
    }


    /**
     * The macintosh palette is arranged as follows: there are 256 colours to
     * allocate, an even distribution of colors through the color cube might be
     * desirable but 256 is not the cube of an integer. 6x6x6 is 216 and so the
     * first 216 colors are an equal 6x6x6 sampling of the color cube.
     * This leaves 40 colours to allocate, this has been done by choosing a ramp of
     * 10 shades each for red, green, blue and grey.
     *
     * <p>
     * References:<br>
     * <a href="http://paulbourke.net/texture_colour/colourramp/">http://paulbourke.net/texture_colour/colourramp/</a>
     *
     * @return The Macintosh color palette.
     */
    public static IndexColorModel createMacColors() {
        byte[] r = new byte[256];
        byte[] g = new byte[256];
        byte[] b = new byte[256];

        // Generate color cube with 216 colors
        int index = 0;
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 6; k++) {
                    r[index] = (byte) (255 - 51 * i);
                    g[index] = (byte) (255 - 51 * j);
                    b[index] = (byte) (255 - 51 * k);
                    index++;
                }
            }
        }

        index--; // overwrite last color (black) with color ramp

        // Generate red ramp
        byte[] ramp = {(byte) 238, (byte) 221, (byte) 187, (byte) 170, (byte) 136, (byte) 119, 85, 68, 34, 17};
        for (int i = 0; i < 10; i++) {
            r[index] = ramp[i];
            g[index] = (byte) (0);
            b[index] = (byte) (0);
            index++;
        }
        // Generate green ramp
        for (int j = 0; j < 10; j++) {
            r[index] = (byte) (0);
            g[index] = ramp[j];
            b[index] = (byte) (0);
            index++;
        }
        // Generate blue ramp
        for (int k = 0; k < 10; k++) {
            r[index] = (byte) (0);
            g[index] = (byte) (0);
            b[index] = ramp[k];
            index++;
        }
        // Generate gray ramp
        for (int ijk = 0; ijk < 10; ijk++) {
            r[index] = ramp[ijk];
            g[index] = ramp[ijk];
            b[index] = ramp[ijk];
            index++;
        }
        // last color is black (nothing to do)

        /*
        for (int i=0;i<256;i++) {
        if (i%6==0) System.out.println(); else System.out.print("  ");
        System.out.print(Integer.toHexString(r[i]&0xff)+","+Integer.toHexString(g[i]&0xff)+","+Integer.toHexString(b[i]&0xff));
        }*/

        return new IndexColorModel(8, 256, r, g, b);
    }

    /**
     * Gray colors from dark to bright
     *
     * @param pixelSize should be 2, 4, or 8 (or at least enough to fit the number of colors)
     * @param numColors number of bits 2=4, 4=16, or 8=256
     * @return IndexColorModel
     */
    public static IndexColorModel createGrayColorsDarkToBright(int pixelSize, int numColors) {
        byte[] r = new byte[numColors];

        // Generate gray ramp
        int step = numColors == 1 ? 255 : 255 / (numColors - 1);
        for (int i = 0; i < numColors; i++) {
            r[i] = (byte) (i * step);
        }
        return new IndexColorModel(pixelSize, numColors, r, r, r, Transparency.OPAQUE);
    }

    /**
     * Gray colors from bright to dark
     *
     * @param pixelSize should be 2, 4, or 8 (or at least enough to fit the number of colors)
     * @param numColors number of bits 2=4, 4=16, or 8=256
     * @return IndexColorModel
     */
    public static IndexColorModel createGrayColorsBrightToDark(int pixelSize, int numColors) {
        byte[] r = new byte[numColors];

        // Generate gray ramp
        int step = numColors == 1 ? 255 : 255 / (numColors - 1);
        for (int i = 0; i < numColors; i++) {
            r[numColors - i - 1] = (byte) (i * step);
        }
        return new IndexColorModel(pixelSize, numColors, r, r, r, Transparency.OPAQUE);
    }

}

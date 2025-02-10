package org.monte.media.impl.jcodec.common.model;

/**
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * @author The JCodec project
 */
public final class ColorSpace {
    public static final int MAX_PLANES = 4;

    public int nComp;

    public int[] compPlane;

    public int[] compWidth;

    public int[] compHeight;

    public boolean planar;

    private String _name;

    public int bitsPerPixel;

    private ColorSpace(String name, int nComp, int[] compPlane, int[] compWidth, int[] compHeight, boolean planar) {
        this._name = name;
        this.nComp = nComp;
        this.compPlane = compPlane;
        this.compWidth = compWidth;
        this.compHeight = compHeight;
        this.planar = planar;
        this.bitsPerPixel = calcBitsPerPixel(nComp, compWidth, compHeight);
    }

    @Override
    public String toString() {
        return _name;
    }

    private static int calcBitsPerPixel(int nComp, int[] compWidth, int[] compHeight) {
        int bitsPerPixel = 0;
        for (int i = 0; i < nComp; i++) {
            bitsPerPixel += ((8 >> compWidth[i]) >> compHeight[i]);
        }
        return bitsPerPixel;
    }

    public int getWidthMask() {
        return ~(nComp > 1 ? compWidth[1] : 0);
    }

    public int getHeightMask() {
        return ~(nComp > 1 ? compHeight[1] : 0);
    }

    /**
     * Determines if two colors match. Aside from simply comparing the objects
     * this function also takes into account lables ANY, ANY_INTERLEAVED, ANY
     * PLANAR.
     *
     * @param inputColor
     * @return True if the color is the same or matches the label.
     */
    public boolean matches(ColorSpace inputColor) {
        if (inputColor == this)
            return true;
        if (inputColor == ANY || this == ANY)
            return true;
        if ((inputColor == ANY_INTERLEAVED || this == ANY_INTERLEAVED || inputColor == ANY_PLANAR || this == ANY_PLANAR)
                && inputColor.planar == this.planar)
            return true;
        return false;
    }

    /**
     * Calculates the component size based on the fullt size and color subsampling of the given component index.
     *
     * @param size
     * @return Component size
     */
    public Size compSize(Size size, int comp) {
        if (compWidth[comp] == 0 && compHeight[comp] == 0)
            return size;
        return new Size(size.getWidth() >> compWidth[comp], size.getHeight() >> compHeight[comp]);
    }

    private static final int[] _000 = new int[]{0, 0, 0};
    private static final int[] _011 = new int[]{0, 1, 1};
    private static final int[] _012 = new int[]{0, 1, 2};
    public final static ColorSpace BGR = new ColorSpace("BGR", 3, _000, _000, _000, false);
    public final static ColorSpace RGB = new ColorSpace("RGB", 3, _000, _000, _000, false);
    public final static ColorSpace YUV420 = new ColorSpace("YUV420", 3, _012, _011, _011, true);
    public final static ColorSpace YUV420J = new ColorSpace("YUV420J", 3, _012, _011, _011, true);
    public final static ColorSpace YUV422 = new ColorSpace("YUV422", 3, _012, _011, _000, true);
    public final static ColorSpace YUV422J = new ColorSpace("YUV422J", 3, _012, _011, _000, true);
    public final static ColorSpace YUV444 = new ColorSpace("YUV444", 3, _012, _000, _000, true);
    public final static ColorSpace YUV444J = new ColorSpace("YUV444J", 3, _012, _000, _000, true);
    public final static ColorSpace YUV422_10 = new ColorSpace("YUV422_10", 3, _012, _011, _000, true);
    public final static ColorSpace GREY = new ColorSpace("GREY", 1, new int[]{0}, new int[]{0}, new int[]{0}, true);
    public final static ColorSpace MONO = new ColorSpace("MONO", 1, _000, _000, _000, true);
    public final static ColorSpace YUV444_10 = new ColorSpace("YUV444_10", 3, _012, _000, _000, true);

    /**
     * Any color space, used in the cases where any color space will do.
     */
    public final static ColorSpace ANY = new ColorSpace("ANY", 0, null, null, null, true);

    /**
     * Any planar color space, used in the cases where any planar color space will do.
     */
    public final static ColorSpace ANY_PLANAR = new ColorSpace("ANY_PLANAR", 0, null, null, null, true);

    /**
     * Any interleaved color space, used in the cases where any interleaved color space will do.
     */
    public final static ColorSpace ANY_INTERLEAVED = new ColorSpace("ANY_INTERLEAVED", 0, null, null, null, false);

    /**
     * Same color, used in filters to declare that the color stays unchanged.
     */
    public final static ColorSpace SAME = new ColorSpace("SAME", 0, null, null, null, false);

}

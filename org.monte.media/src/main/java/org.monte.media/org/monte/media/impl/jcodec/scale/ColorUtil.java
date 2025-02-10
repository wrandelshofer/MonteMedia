package org.monte.media.impl.jcodec.scale;

import org.monte.media.impl.jcodec.common.model.ColorSpace;
import org.monte.media.impl.jcodec.common.model.Picture;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.arraycopy;

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
public class ColorUtil {

    private static Map<ColorSpace, Map<ColorSpace, Transform>> map = new HashMap<ColorSpace, Map<ColorSpace, Transform>>();

    static {
        Map<ColorSpace, Transform> rgb = new HashMap<ColorSpace, Transform>();
        rgb.put(ColorSpace.RGB, new Idential());
        rgb.put(ColorSpace.YUV420J, new RgbToYuv420j());
        rgb.put(ColorSpace.YUV420, new RgbToYuv420p());
        rgb.put(ColorSpace.YUV422, new RgbToYuv422p());
        map.put(ColorSpace.RGB, rgb);

        Map<ColorSpace, Transform> yuv420 = new HashMap<ColorSpace, Transform>();
        yuv420.put(ColorSpace.YUV420, new Idential());
        yuv420.put(ColorSpace.YUV422, new Yuv420pToYuv422p());
        yuv420.put(ColorSpace.RGB, new Yuv420pToRgb());
        yuv420.put(ColorSpace.YUV420J, new Idential());
        map.put(ColorSpace.YUV420, yuv420);

        Map<ColorSpace, Transform> yuv422 = new HashMap<ColorSpace, Transform>();
        yuv422.put(ColorSpace.YUV422, new Idential());
        yuv422.put(ColorSpace.YUV420, new Yuv422pToYuv420p());
        yuv422.put(ColorSpace.YUV420J, new Yuv422pToYuv420p());
        yuv422.put(ColorSpace.RGB, new Yuv422pToRgb());
        map.put(ColorSpace.YUV422, yuv422);

        Map<ColorSpace, Transform> yuv444 = new HashMap<ColorSpace, Transform>();
        yuv444.put(ColorSpace.YUV444, new Idential());
        map.put(ColorSpace.YUV444, yuv444);

        Map<ColorSpace, Transform> yuv444j = new HashMap<ColorSpace, Transform>();
        yuv444j.put(ColorSpace.YUV444J, new Idential());
        yuv444j.put(ColorSpace.YUV420J, new Yuv444jToYuv420j());
        map.put(ColorSpace.YUV444J, yuv444j);

        Map<ColorSpace, Transform> yuv420j = new HashMap<ColorSpace, Transform>();
        yuv420j.put(ColorSpace.YUV420J, new Idential());
        yuv420j.put(ColorSpace.YUV422, new Yuv420pToYuv422p());
        yuv420j.put(ColorSpace.RGB, new Yuv420jToRgb());
        yuv420j.put(ColorSpace.YUV420, new Idential());
        map.put(ColorSpace.YUV420J, yuv420j);
    }

    public static Transform getTransform(ColorSpace from, ColorSpace to) {
        Map<ColorSpace, Transform> map2 = map.get(from);

        return map2 == null ? null : map2.get(to);
    }

    public static class Idential implements Transform {
        @Override
        public void transform(Picture src, Picture dst) {
            for (int i = 0; i < Math.min(src.getData().length, dst.getData().length); i++)
                arraycopy(src.getPlaneData(i), 0, dst.getPlaneData(i), 0,
                        Math.min(src.getPlaneData(i).length, dst.getPlaneData(i).length));
            byte[][] srcLowBits = src.getLowBits();
            byte[][] dstLowBits = dst.getLowBits();
            if (srcLowBits != null && dstLowBits != null) {
                for (int i = 0; i < Math.min(src.getData().length, dst.getData().length); i++)
                    arraycopy(srcLowBits[i], 0, dstLowBits[i], 0,
                            Math.min(src.getPlaneData(i).length, dst.getPlaneData(i).length));
            }
        }
    }
}

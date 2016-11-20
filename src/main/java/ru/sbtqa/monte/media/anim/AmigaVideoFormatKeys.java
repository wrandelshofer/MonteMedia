/* @(#)AmigaVideoFormatKeys.java
 * Copyright © 2011 Werner Randelshofer, Switzerland.
 * 
 * You may not use, copy or modify this file, except in compliance onlyWith the
 * license agreement you entered into onlyWith Werner Randelshofer.
 * For details see accompanying license terms.
 */
package ru.sbtqa.monte.media.anim;

import java.util.ArrayList;
import java.util.Iterator;
import ru.sbtqa.monte.media.Format;
import ru.sbtqa.monte.media.FormatKey;
import ru.sbtqa.monte.media.VideoFormatKeys;
import ru.sbtqa.monte.media.math.Rational;

/**
 * {@code AmigaVideoFormatKeys}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class AmigaVideoFormatKeys extends VideoFormatKeys {

    /** The Amiga monitor id. 
     */
    public final static FormatKey<Integer> MonitorIdKey = new FormatKey<Integer>("monitorId", Integer.class);
    /** Anim Op5 . */
    public static final String ENCODING_ANIM_OP5 = "op5";

    enum ColorMode {

        HAM, EHB, NORMAL
    }
    public final static FormatKey<ColorMode> ColorModeKey = new FormatKey<ColorMode>("colorMode", ColorMode.class);

    public static Format fromCAMG(int camg) {
        AmigaDisplayInfo i = AmigaDisplayInfo.getInfo(camg);
        return new Format(
                MediaTypeKey, MediaType.VIDEO,
                EncodingKey, ENCODING_BITMAP_IMAGE,
                WidthKey, i.textOverscanWidth,
                HeightKey, i.textOverscanHeight,
                MonitorIdKey, camg & AmigaDisplayInfo.MONITOR_ID_MASK,
                ColorModeKey, i.isEHB() ? ColorMode.EHB : (i.isHAM() ? ColorMode.HAM : ColorMode.NORMAL),
                InterlaceKey, i.isInterlace(),
                PixelAspectRatioKey, new Rational(i.resolutionX, i.resolutionY),
                FrameRateKey, new Rational(i.fps, 1));

    }

    public static int toCAMG(Format fmt) {
        int camg = 0;

        // determine monitor id
        int monitorId = 0;
        if (fmt.containsKey(MonitorIdKey)) {
            monitorId = fmt.get(MonitorIdKey);
        } else {
            ArrayList<AmigaDisplayInfo> infs = new ArrayList<AmigaDisplayInfo>(AmigaDisplayInfo.getAllInfos().values());
            if (fmt.containsKey(InterlaceKey)) {
                boolean value = fmt.get(InterlaceKey);
                reduceListBoolean(value, new InfGetter<Boolean>() {

                    @Override
                    public Boolean get(AmigaDisplayInfo inf) {
                        return inf.isInterlace();
                    }
                }, infs);
            }
            if (fmt.containsKey(FrameRateKey)) {
                Rational value = fmt.get(FrameRateKey);
                reduceListRational(value, new InfGetter<Rational>() {

                    @Override
                    public Rational get(AmigaDisplayInfo inf) {
                        return new Rational(inf.fps, 1);
                    }
                }, infs);
            }
            if (fmt.containsKey(PixelAspectRatioKey)) {
                Rational value = fmt.get(PixelAspectRatioKey);
                reduceListRational(value, new InfGetter<Rational>() {

                    @Override
                    public Rational get(AmigaDisplayInfo inf) {
                        return new Rational(inf.resolutionX, inf.resolutionY);
                    }
                }, infs);
            }
            ArrayList<AmigaDisplayInfo> bestInfs = new ArrayList<AmigaDisplayInfo>(infs);
            if (fmt.containsKey(WidthKey)) {
                int value = fmt.get(WidthKey);
                reduceListIntegerOnlyTakeIfSmaller(value, new InfGetter<Integer>() {

                    @Override
                    public Integer get(AmigaDisplayInfo inf) {
                        return inf.textOverscanWidth;
                    }
                }, infs);
            }
            if (fmt.containsKey(HeightKey)) {
                Integer value = fmt.get(HeightKey);
                reduceListIntegerOnlyTakeIfSmaller(value, new InfGetter<Integer>() {

                    @Override
                    public Integer get(AmigaDisplayInfo inf) {
                        return inf.textOverscanHeight;
                    }
                }, infs);
            }
            if (infs.isEmpty()) {
                infs = new ArrayList<AmigaDisplayInfo>(bestInfs);
                if (fmt.containsKey(WidthKey)) {
                    Integer value = fmt.get(WidthKey);
                    reduceListIntegerOnlyTakeIfSmaller(value, new InfGetter<Integer>() {

                        @Override
                        public Integer get(AmigaDisplayInfo inf) {
                            return inf.maxOverscanWidth;
                        }
                    }, infs);
                }
                if (fmt.containsKey(HeightKey)) {
                    Integer value = fmt.get(HeightKey);
                    reduceListIntegerOnlyTakeIfSmaller(value, new InfGetter<Integer>() {

                        @Override
                        public Integer get(AmigaDisplayInfo inf) {
                            return inf.maxOverscanHeight;
                        }
                    }, infs);
                }
            }
            if (infs.isEmpty()) {
                infs = new ArrayList<AmigaDisplayInfo>(bestInfs);
                if (fmt.containsKey(WidthKey)) {
                    Integer value = fmt.get(WidthKey);
                    reduceListInteger(value, new InfGetter<Integer>() {

                        @Override
                        public Integer get(AmigaDisplayInfo inf) {
                            return inf.maxOverscanWidth;
                        }
                    }, infs);
                }
                if (fmt.containsKey(HeightKey)) {
                    Integer value = fmt.get(HeightKey);
                    reduceListInteger(value, new InfGetter<Integer>() {

                        @Override
                        public Integer get(AmigaDisplayInfo inf) {
                            return inf.maxOverscanHeight;
                        }
                    }, infs);
                }
            }
        }
        
        int colorMode=0;
        if (fmt.containsKey(ColorModeKey)) {
            switch (fmt.get(ColorModeKey)) {
                case EHB:
                    colorMode=AmigaDisplayInfo.EHB_COLORMODE;
                    break;
                case HAM:
                    colorMode=AmigaDisplayInfo.HAM_COLORMODE;
                    break;
                case NORMAL:
                    break;
            }
        }
        
        camg = monitorId|colorMode;

        return camg;
    }

    private interface InfGetter<T> {

        public T get(AmigaDisplayInfo inf);
    }

    private static void reduceListRational(Rational value, InfGetter<Rational> g, ArrayList<AmigaDisplayInfo> infs) {
        ArrayList<AmigaDisplayInfo> bestInfs = new ArrayList<AmigaDisplayInfo>();
        bestInfs.add(infs.get(0));
        float bestCost = g.get(infs.get(0)).subtract(value).floatValue();
        bestCost *= bestCost;
        for (Iterator<AmigaDisplayInfo> i = infs.iterator(); i.hasNext();) {
            AmigaDisplayInfo inf = i.next();
            Rational iv = g.get(inf);
            if (iv.compareTo(value) != 0) {
                i.remove();
            }
            float icost = iv.subtract(value).floatValue();
            icost *= icost;
            if (icost < bestCost) {
                bestInfs.clear();
                bestCost = icost;
            } else if (icost == bestCost) {
                bestInfs.add(inf);
            }
        }
        if (infs.isEmpty()) {
            infs.addAll(bestInfs);
        }
    }

    private static void reduceListInteger(int value, InfGetter<Integer> g, ArrayList<AmigaDisplayInfo> infs) {
        ArrayList<AmigaDisplayInfo> bestInfs = new ArrayList<AmigaDisplayInfo>();
        bestInfs.add(infs.get(0));
        float bestCost = g.get(infs.get(0)) - value;
        bestCost *= bestCost;
        for (Iterator<AmigaDisplayInfo> i = infs.iterator(); i.hasNext();) {
            AmigaDisplayInfo inf = i.next();
            int iv = g.get(inf);
            if (iv != value) {
                i.remove();
            }
            float icost = iv - value;
            icost *= icost;
            if (icost < bestCost) {
                bestInfs.clear();
                bestCost = icost;
            } else if (icost == bestCost) {
                bestInfs.add(inf);
            }
        }
        if (infs.isEmpty()) {
            infs.addAll(bestInfs);
        }
    }

    private static void reduceListIntegerOnlyTakeIfSmaller(int value, InfGetter<Integer> g, ArrayList<AmigaDisplayInfo> infs) {
        reduceListInteger(value, g, infs);
        for (Iterator<AmigaDisplayInfo> i = infs.iterator(); i.hasNext();) {
            AmigaDisplayInfo inf = i.next();
            int iv = g.get(inf);
            if (value > iv) {
                i.remove();
            }
        }
    }
    private static void reduceListBoolean(boolean value, InfGetter<Boolean> g, ArrayList<AmigaDisplayInfo> infs) {
        for (Iterator<AmigaDisplayInfo> i = infs.iterator(); i.hasNext();) {
            AmigaDisplayInfo inf = i.next();
            boolean iv = g.get(inf);
            if (iv != value) {
                i.remove();
            }
        }
    }
}

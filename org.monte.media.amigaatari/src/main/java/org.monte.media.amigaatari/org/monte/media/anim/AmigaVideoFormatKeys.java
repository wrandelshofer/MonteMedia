/*
 * @(#)AmigaVideoFormatKeys.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.anim;

import org.monte.media.amigabitmap.AmigaDisplayInfo;
import org.monte.media.amigabitmap.AmigaDisplayInfoDatabase;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKey;
import org.monte.media.av.codec.video.VideoFormatKeys;
import org.monte.media.math.Rational;

import java.util.ArrayList;
import java.util.Iterator;

/// `AmigaVideoFormatKeys`.
///
/// @author Werner Randelshofer
public class AmigaVideoFormatKeys extends VideoFormatKeys {

    /// The Amiga "CAMG" monitor id.
    public final static FormatKey<Integer> MonitorIdKey = new FormatKey<>("monitorId", Integer.class);
    /// Anim Op5 .
    public static final String ENCODING_ANIM_OP5 = "op5";

    enum ColorMode {

        HAM, EHB, NORMAL
    }

    public final static FormatKey<ColorMode> ColorModeKey = new FormatKey<>("colorMode", ColorMode.class);

    public static Format fromCAMG(int camg) {
        AmigaDisplayInfo info = AmigaDisplayInfoDatabase.getInfo(camg);
        return new Format(
                MediaTypeKey, MediaType.VIDEO,
                EncodingKey, ENCODING_BITMAP_IMAGE,
                WidthKey, info.textOverscanWidth(),
                HeightKey, info.textOverscanHeight(),
                MonitorIdKey, camg & AmigaDisplayInfoDatabase.MONITOR_ID_MASK,
                ColorModeKey, info.isEHB() ? ColorMode.EHB : (info.isHAM() ? ColorMode.HAM : ColorMode.NORMAL),
                InterlaceKey, info.isInterlace(),
                PixelAspectRatioKey, new Rational(info.resolutionX(), info.resolutionY()),
                FrameRateKey, new Rational(info.fps(), 1));

    }

    public static int toCAMG(Format fmt) {
        int camg = 0;

        // determine monitor id
        int monitorId = 0;
        if (fmt.containsKey(MonitorIdKey)) {
            monitorId = fmt.get(MonitorIdKey);
        } else {
            ArrayList<AmigaDisplayInfo> infs = new ArrayList<>(AmigaDisplayInfoDatabase.getAllInfos().values());
            if (fmt.containsKey(InterlaceKey)) {
                boolean value = fmt.get(InterlaceKey);
                reduceListBoolean(value, AmigaDisplayInfo::isInterlace, infs);
            }
            if (fmt.containsKey(FrameRateKey)) {
                Rational value = fmt.get(FrameRateKey);
                reduceListRational(value, info -> new Rational(info.fps(), 1), infs);
            }
            if (fmt.containsKey(PixelAspectRatioKey)) {
                Rational value = fmt.get(PixelAspectRatioKey);
                reduceListRational(value, info -> new Rational(info.resolutionX(), info.resolutionY()), infs);
            }
            ArrayList<AmigaDisplayInfo> bestInfs = new ArrayList<>(infs);
            if (fmt.containsKey(WidthKey)) {
                int value = fmt.get(WidthKey);
                reduceListIntegerOnlyTakeIfSmaller(value, AmigaDisplayInfo::textOverscanWidth, infs);
            }
            if (fmt.containsKey(HeightKey)) {
                Integer value = fmt.get(HeightKey);
                reduceListIntegerOnlyTakeIfSmaller(value, AmigaDisplayInfo::textOverscanHeight, infs);
            }
            if (infs.isEmpty()) {
                infs = new ArrayList<>(bestInfs);
                if (fmt.containsKey(WidthKey)) {
                    Integer value = fmt.get(WidthKey);
                    reduceListIntegerOnlyTakeIfSmaller(value, AmigaDisplayInfo::maxOverscanWidth, infs);
                }
                if (fmt.containsKey(HeightKey)) {
                    Integer value = fmt.get(HeightKey);
                    reduceListIntegerOnlyTakeIfSmaller(value, AmigaDisplayInfo::maxOverscanHeight, infs);
                }
            }
            if (infs.isEmpty()) {
                infs = new ArrayList<>(bestInfs);
                if (fmt.containsKey(WidthKey)) {
                    Integer value = fmt.get(WidthKey);
                    reduceListInteger(value, AmigaDisplayInfo::maxOverscanWidth, infs);
                }
                if (fmt.containsKey(HeightKey)) {
                    Integer value = fmt.get(HeightKey);
                    reduceListInteger(value, AmigaDisplayInfo::maxOverscanHeight, infs);
                }
            }
        }

        int colorMode = 0;
        if (fmt.containsKey(ColorModeKey)) {
            switch (fmt.get(ColorModeKey)) {
                case EHB:
                    colorMode = AmigaDisplayInfoDatabase.EHB_COLORMODE;
                    break;
                case HAM:
                    colorMode = AmigaDisplayInfoDatabase.HAM_COLORMODE;
                    break;
                case NORMAL:
                    break;
            }
        }

        camg = monitorId | colorMode;

        return camg;
    }

    private interface InfGetter<T> {

        public T get(AmigaDisplayInfo info);
    }

    private static void reduceListRational(Rational value, InfGetter<Rational> g, ArrayList<AmigaDisplayInfo> infs) {
        if (infs.isEmpty()) {
            return;
        }
        ArrayList<AmigaDisplayInfo> bestInfs = new ArrayList<>();
        bestInfs.add(infs.get(0));
        float bestCost = g.get(infs.get(0)).subtract(value).floatValue();
        bestCost *= bestCost;
        for (Iterator<AmigaDisplayInfo> i = infs.iterator(); i.hasNext(); ) {
            AmigaDisplayInfo info = i.next();
            Rational iv = g.get(info);
            if (iv.compareTo(value) != 0) {
                i.remove();
            }
            float icost = iv.subtract(value).floatValue();
            icost *= icost;
            if (icost < bestCost) {
                bestInfs.clear();
                bestCost = icost;
            } else if (icost == bestCost) {
                bestInfs.add(info);
            }
        }
        if (infs.isEmpty()) {
            infs.addAll(bestInfs);
        }
    }

    private static void reduceListInteger(int value, InfGetter<Integer> g, ArrayList<AmigaDisplayInfo> infs) {
        if (infs.isEmpty()) {
            return;
        }
        ArrayList<AmigaDisplayInfo> bestInfs = new ArrayList<>();
        bestInfs.add(infs.get(0));
        float bestCost = g.get(infs.get(0)) - value;
        bestCost *= bestCost;
        for (Iterator<AmigaDisplayInfo> i = infs.iterator(); i.hasNext(); ) {
            AmigaDisplayInfo info = i.next();
            int iv = g.get(info);
            if (iv != value) {
                i.remove();
            }
            float icost = iv - value;
            icost *= icost;
            if (icost < bestCost) {
                bestInfs.clear();
                bestCost = icost;
            } else if (icost == bestCost) {
                bestInfs.add(info);
            }
        }
        if (infs.isEmpty()) {
            infs.addAll(bestInfs);
        }
    }

    private static void reduceListIntegerOnlyTakeIfSmaller(int value, InfGetter<Integer> g, ArrayList<AmigaDisplayInfo> infs) {
        if (infs.isEmpty()) {
            return;
        }
        reduceListInteger(value, g, infs);
        for (Iterator<AmigaDisplayInfo> i = infs.iterator(); i.hasNext(); ) {
            AmigaDisplayInfo info = i.next();
            int iv = g.get(info);
            if (value > iv) {
                i.remove();
            }
        }
    }

    private static void reduceListBoolean(boolean value, InfGetter<Boolean> g, ArrayList<AmigaDisplayInfo> infs) {
        if (infs.isEmpty()) {
            return;
        }
        for (Iterator<AmigaDisplayInfo> i = infs.iterator(); i.hasNext(); ) {
            AmigaDisplayInfo info = i.next();
            boolean iv = g.get(info);
            if (iv != value) {
                i.remove();
            }
        }
    }
}

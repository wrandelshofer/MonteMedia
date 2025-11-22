/*
 * @(#)AmigaDisplayInfo.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.amigabitmap;

import java.awt.Dimension;


/// Amiga display info record.
public record AmigaDisplayInfo(
        int camg,
        String name,
        int textOverscanWidth, int textOverscanHeight,
        int maxOverscanWidth, int maxOverscanHeight,
        int minimalSizeWidth, int minimalSizeHeight,
        int maximalSizeWidth, int maximalSizeHeight,
        int colorRegisterDepth,
        int resolutionX, int resolutionY,
        int pixelSpeed,
        int fps) {

    public AmigaDisplayInfo(int camg, String name, Dimension textOverscan, Dimension maxOverscan, Dimension minimalSize, Dimension maximalSize, int colorRegisterDepth, Dimension resolution, int pixelSpeed, int fps) {
        this(camg, name,
                textOverscan.width,
                textOverscan.height,
                maxOverscan.width,
                maxOverscan.height,
                minimalSize.width,
                minimalSize.height,
                maximalSize.width,
                maximalSize.height,
                colorRegisterDepth,
                resolution.width,
                resolution.height,
                pixelSpeed,
                fps);
    }

    public boolean isOCS() {
        return colorRegisterDepth == 4;
    }

    public boolean isHAM() {
        return (camg & AmigaDisplayInfoDatabase.COLORMODE_MASK) == AmigaDisplayInfoDatabase.HAM_COLORMODE;
    }

    public boolean isEHB() {
        return (camg & AmigaDisplayInfoDatabase.COLORMODE_MASK) == AmigaDisplayInfoDatabase.EHB_COLORMODE;
    }

    public boolean isInterlace() {
        boolean isInterlace;
        switch (camg & AmigaDisplayInfoDatabase.MONITOR_ID_MASK) {
            case AmigaDisplayInfoDatabase.NTSC_MONITOR_ID:
            case AmigaDisplayInfoDatabase.PAL_MONITOR_ID:
                isInterlace = (camg & AmigaDisplayInfoDatabase.PALNTSC_INTERLACE_MASK) == AmigaDisplayInfoDatabase.PALNTSC_INTERLACE_MODE;
                break;
            case AmigaDisplayInfoDatabase.MULTISCAN_MONITOR_ID:
                isInterlace = (camg & AmigaDisplayInfoDatabase.MULTISCAN_INTERLACE_MASK) == AmigaDisplayInfoDatabase.MULTISCAN_INTERLACE_MODE;
                break;
            default:
                isInterlace = false;
                break;
        }
        return isInterlace;
    }

    public boolean isDualPlayfield() {
        return (camg & AmigaDisplayInfoDatabase.DUALPLAYFIELD_MASK) == AmigaDisplayInfoDatabase.DUALPLAYFIELD_MODE;
    }

    /*
    public static void main(String[] args) {
        TreeMap<Integer, AmigaDisplayInfo> tm = new TreeMap<Integer, AmigaDisplayInfo>(getAllInfos());

        for (Map.Entry<Integer, AmigaDisplayInfo> e : tm.entrySet()) {
            AmigaDisplayInfo i = e.getValue();
            int camg = i.camg;
            int colorRegisterDepth = i.colorRegisterDepth;
            String suffix="";
            if ((camg & MONITOR_ID_MASK) == DEFAULT_MONITOR_ID) {
                i = tm.get(camg | NTSC_MONITOR_ID);
                colorRegisterDepth = 4;
                suffix=" OCS";
            }
            System.out.println("{0x" + Integer.toHexString(camg) + ", \"" + i.name +suffix+ "\"// camg, name\n"
                    + ", " + i.textOverscan.width + ", " + i.textOverscan.height + " // text overscan\n"
                    + ", " + i.maxOverscan.width + ", " + i.maxOverscan.height + " // max overscan\n"
                    + ", " + i.minimalSize.width + ", " + i.minimalSize.height + " // minimal size\n"
                    + ", " + i.maximalSize.width + ", " + i.maximalSize.height + " // maximal size\n"
                    + ", " + colorRegisterDepth + " // color register depth\n"
                    + ", " + i.resolution.width + ", " + i.resolution.height + " // resolution\n"
                    + ", " + i.pixelSpeed + " // pixel speed\n"
                    + "},//\n");
        }
    }*/
}

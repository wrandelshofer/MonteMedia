/* @(#)AmigaDisplayInfo.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.amigabitmap;

import java.awt.Dimension;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * {@code AmigaDisplayInfo}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class AmigaDisplayInfo {

    public final int camg;
    public final String name;
    public final int textOverscanWidth,textOverscanHeight;
    public final int maxOverscanWidth,maxOverscanHeight;
    public final int minimalSizeWidth,minimalSizeHeight;
    public final int maximalSizeWidth,maximalSizeHeight;
    public final int colorRegisterDepth;
    /** Ticks per pixel X/Y */
    public final int resolutionX,resolutionY;
    /** Approximation in nanoseconds. */
    public final int pixelSpeed;
    public final int fps;
    private static TreeMap<Integer, AmigaDisplayInfo> infos;

    public AmigaDisplayInfo(int camg, String name, Dimension textOverscan, Dimension maxOverscan, Dimension minimalSize, Dimension maximalSize, int colorRegisterDepth, Dimension resolution, int pixelSpeed, int fps) {
        this.camg = camg;
        this.name = name;
        this.textOverscanWidth = textOverscan.width;
        this.textOverscanHeight = textOverscan.height;
        this.maxOverscanWidth = maxOverscan.width;
        this.maxOverscanHeight = maxOverscan.height;
        this.minimalSizeWidth = minimalSize.width;
        this.minimalSizeHeight = minimalSize.height;
        this.maximalSizeWidth = maximalSize.width;
        this.maximalSizeHeight = maximalSize.height;
        this.colorRegisterDepth = colorRegisterDepth;
        this.resolutionX = resolution.width;
        this.resolutionY = resolution.height;
        this.pixelSpeed = pixelSpeed;
        this.fps = fps;
    }
    
    public boolean isOCS() {
        return colorRegisterDepth==4;
    }
    
    public boolean isHAM() {
        return (camg&COLORMODE_MASK)==HAM_COLORMODE;
    }
    public boolean isEHB() {
        return (camg&COLORMODE_MASK)==EHB_COLORMODE;
    }
    public boolean isInterlace() {
        boolean isInterlace;
        switch (camg & MONITOR_ID_MASK) {
            case NTSC_MONITOR_ID:
            case PAL_MONITOR_ID:
                isInterlace=(camg&PALNTSC_INTERLACE_MASK)==PALNTSC_INTERLACE_MODE;
                break;
            case MULTISCAN_MONITOR_ID:
                isInterlace=(camg&MULTISCAN_INTERLACE_MASK)==MULTISCAN_INTERLACE_MODE;
                break;
            default:
                isInterlace=false;
                break;
        }
        return isInterlace;
    }

    public static Map<Integer, AmigaDisplayInfo> getAllInfos() {
        if (infos == null) {
            infos = new TreeMap<Integer, AmigaDisplayInfo>();
            for (Object[] e : infoTable) {
                int i = 0;
                int camg = (Integer) e[i++];
                String name = (String) e[i++];
                Dimension textOverscan = new Dimension((Integer) e[i++], (Integer) e[i++]);
                Dimension maxOverscan = new Dimension((Integer) e[i++], (Integer) e[i++]);
                Dimension minimalSize = new Dimension((Integer) e[i++], (Integer) e[i++]);
                Dimension maximalSize = new Dimension((Integer) e[i++], (Integer) e[i++]);
                int colorRegisterDepth = (Integer) e[i++];
                Dimension resolution = new Dimension((Integer) e[i++], (Integer) e[i++]);
                int pixelSpeed = (Integer) e[i++];
                Integer fps = monitorToFPSMap.get(camg & MONITOR_ID_MASK).fps;
                if (fps == null) {
                    System.out.println("NO FPS  0x" + Integer.toHexString(camg) + " " + name);
                }
                infos.put(camg, new AmigaDisplayInfo(camg, name, textOverscan, maxOverscan, minimalSize, maximalSize, colorRegisterDepth, resolution, pixelSpeed, fps));
            }
        }
        return Collections.unmodifiableMap(infos);
    }

    public static AmigaDisplayInfo getInfo(int camg) {
        return getAllInfos().get(camg);
    }
    
    /** CAMG monitor ID mask. */
    public final static int MONITOR_ID_MASK = 0xffff1000;
    /** Default ID chooses a system dependent screen mode. We always fall back
     * to NTSC OCS with 60fps.
     * 
     * The default monitor ID triggers OCS mode!
     * OCS stands for "Original Chip Set". The OCS chip set only had 4 bits per color register.
     * All later chip sets hat 8 bits per color register. 
     */
    public final static int DEFAULT_MONITOR_ID = 0x00000000;
    /** NTSC, 60fps, 44:52. */
    public final static int NTSC_MONITOR_ID = 0x00011000;
    /** PAL, 50fps, 44:44. */
    public final static int PAL_MONITOR_ID = 0x00021000;
    /** MULTISCAN (VGA), 58fps, 44:44. */
    public final static int MULTISCAN_MONITOR_ID = 0x00031000;
    /** A2024, 60fps (I don't know the real value). */
    public final static int A2024_MONITOR_ID = 0x00041000;
    /** PROTO, 60fps (I don't know the real value). */
    public final static int PROTO_MONITOR_ID = 0x00051000;
    /** EURO72, 69fps, 44:44. */
    public final static int EURO72_MONITOR_ID = 0x00061000;
    /** EURO36, 73fps, 44:44. */
    public final static int EURO36_MONITOR_ID = 0x00071000;
    /** SUPER72, 71fps, 34:40. */
    public final static int SUPER72_MONITOR_ID = 0x00081000;
    /** DBLNTSC, 58fps, 44:52. */
    public final static int DBLNTSC_MONITOR_ID = 0x00091000;
    /** DBLPAL, 48fps, 44:44. */
    public final static int DBLPAL_MONITOR_ID = 0x000a1000;
    
    public static int[] getMonitorIds() {
        return new int[] {
            DEFAULT_MONITOR_ID,
            NTSC_MONITOR_ID,
            PAL_MONITOR_ID,
            MULTISCAN_MONITOR_ID,
            A2024_MONITOR_ID,
            PROTO_MONITOR_ID,
            EURO72_MONITOR_ID,
            EURO36_MONITOR_ID,
            SUPER72_MONITOR_ID,
            DBLNTSC_MONITOR_ID,
            DBLPAL_MONITOR_ID,
        };
    }
     public static int[] getGoodMonitorIds() {
        return new int[] {
            DEFAULT_MONITOR_ID,
            NTSC_MONITOR_ID,
            PAL_MONITOR_ID,
            MULTISCAN_MONITOR_ID,
            //A2024_MONITOR_ID,
            //PROTO_MONITOR_ID,
            EURO72_MONITOR_ID,
            EURO36_MONITOR_ID,
            SUPER72_MONITOR_ID,
            DBLNTSC_MONITOR_ID,
            DBLPAL_MONITOR_ID,
        };
    }
    public static int getFPS(int camg) {
        MonitorItem mi= monitorToFPSMap.get(camg&MONITOR_ID_MASK);
                return mi==null?0:mi.fps;
    }
    public  static String getMonitorName(int camg) {
        MonitorItem mi= monitorToFPSMap.get(camg&MONITOR_ID_MASK);
                return mi==null?null:mi.name;
    }

public    boolean isDualPlayfield() {
      return( camg&DUALPLAYFIELD_MASK)==DUALPLAYFIELD_MODE;
    }

    private static class MonitorItem {
        String name;
        int fps;

        public MonitorItem(String name, int fps) {
            this.name = name;
            this.fps = fps;
        }
        
    }
    private final static TreeMap<Integer, MonitorItem> monitorToFPSMap;

    static {
        monitorToFPSMap = new TreeMap<Integer, MonitorItem>();
        monitorToFPSMap.put(DEFAULT_MONITOR_ID, new MonitorItem("NTSC OCS",60));
        monitorToFPSMap.put(NTSC_MONITOR_ID, new MonitorItem("NTSC",60));
        monitorToFPSMap.put(PAL_MONITOR_ID, new MonitorItem("PAL",50));
        monitorToFPSMap.put(MULTISCAN_MONITOR_ID,new MonitorItem("MULTISCAN", 58));
        monitorToFPSMap.put(A2024_MONITOR_ID, new MonitorItem("A2024",60));
        monitorToFPSMap.put(PROTO_MONITOR_ID, new MonitorItem("PROTO",60));
        monitorToFPSMap.put(EURO72_MONITOR_ID, new MonitorItem("EURO72",69));
        monitorToFPSMap.put(EURO36_MONITOR_ID, new MonitorItem("EURO36",73));
        monitorToFPSMap.put(SUPER72_MONITOR_ID, new MonitorItem("SUPER72",71));
        monitorToFPSMap.put(DBLNTSC_MONITOR_ID, new MonitorItem("DBLNTSC",58));
        monitorToFPSMap.put(DBLPAL_MONITOR_ID, new MonitorItem("DBLPAL",48));
    }
    /** CAMG display properties. */
    public final static int COLORMODE_MASK = 0x00000880;
    /** CAMG HAM mode. */
    public final static int HAM_COLORMODE = 0x00000800;
    /** CAMG EHB mode. */
    public final static int EHB_COLORMODE = 0x00000080;
    /** CAMG interlace mask. Only valid for PAL and NTSC monitors. */
    public final static int PALNTSC_INTERLACE_MASK = 0x00000004;
    /** CAMG interlace mode. Only valid for PAL and NTSC monitors. */
    public final static int PALNTSC_INTERLACE_MODE = 0x00000004;
    /** CAMG interlace mask. Only valid for MULTISCAN monitors. */
    public final static int MULTISCAN_INTERLACE_MASK = 0x00000001;
    /** CAMG interlace mode. Only valid for MULTISCAN monitors. */
    public final static int MULTISCAN_INTERLACE_MODE = 0x00000001;
    /** CAMG dual playfield mask. */
    public final static int DUALPLAYFIELD_MASK = 0x00000400;
    /** CAMG dual playfield mode. */
    public final static int DUALPLAYFIELD_MODE = 0x00000400;
    /** Well known CAMG formats. */
    public final static int NTSC_320x200_44t52_60fps = 0x11000;
    public final static int NTSC_320x400_44t26_interlaced_60fps = 0x11004;
    public final static int NTSC_640x200_22t52_60fps = 0x19000;
    public final static int NTSC_640x400_22t26_interlaced_60fps = 0x19004;
    public final static int NTSC_1280x200_11t52_60fps = 0x19020;
    public final static int NTSC_1280x400_11t26_interlaced_60fps = 0x19024;
    public final static int PAL_320x256_44t44_50fps = 0x21000;
    public final static int PAL_320x512_44t22_interlaced_50fps = 0x21004;
    public final static int PAL_640x256_22t44_50fps = 0x29000;
    public final static int PAL_640x512_22t22_interlaced_50fps = 0x29004;
    public final static int PAL_1280x256_11t44_50fps = 0x29020;
    public final static int PAL_1280x512_11t22_interlaced_50fps = 0x29024;
    public final static int MULTISCAN_160x480_88t22_58fps = 0x31004;
    public final static int MULTISCAN_160x960_88t11_interlaced_58fps = 0x31005;
    public final static int MULTISCAN_320x480_44t22_58fps = 0x39004;
    public final static int MULTISCAN_320x960_44t11_interlaced_58fps = 0x39005;
    public final static int MULTISCAN_640x480_22t22_58fps = 0x39024;
    public final static int MULTISCAN_640x960_22t11_interlaced_58fps = 0x39025;
    public final static int EURO72_640x400_22t22_69fps = 0x69024;
    public final static int EURO72_640x800_22t11_interlaced_69fps = 0x69025;
    public final static int EURO36_320x200_44t44_73fps = 0x71000;
    public final static int EURO36_320x400_44t22_interlaced_73fps = 0x71004;
    public final static int EURO36_640x200_22t44_73fps = 0x79000;
    public final static int EURO36_640x400_22t22_interlaced_73fps = 0x79004;
    public final static int EURO36_1280x200_11t44_73fps = 0x79020;
    public final static int EURO36_1280x400_11t22_interlaced_73fps = 0x79024;
    public final static int SUPER72_200x300_68t40_71fps = 0x81000;
    public final static int SUPER72_200x600_68t20_interlaced_71fps = 0x81004;
    public final static int SUPER72_400x300_34t40_71fps = 0x89000;
    public final static int SUPER72_400x600_34t20_interlaced_71fps = 0x89004;
    public final static int SUPER72_800x300_17t40_71fps = 0x89020;
    public final static int SUPER72_800x600_17t20_interlaced_71fps = 0x89024;
    public final static int DBLNTSC_320x200_44t52_58fps = 0x91000;
    public final static int DBLNTSC_320x400_44t26_58fps = 0x91004;
    public final static int DBLNTSC_320x800_44t13_interlaced_58fps = 0x91005;
    public final static int DBLNTSC_640x200_22t52_58fps = 0x99000;
    public final static int DBLNTSC_640x400_22t26_58fps = 0x99004;
    public final static int DBLNTSC_640x800_22t13_interlaced_58fps = 0x99005;
    public final static int DBLPAL_320x256_44t44_48fps = 0xa1000;
    public final static int DBLPAL_320x512_44t22_48fps = 0xa1004;
    public final static int DBLPAL_320x1024_44t11_interlaced_48fps = 0xa1005;
    public final static int DBLPAL_640x256_22t44_48fps = 0xa9000;
    public final static int DBLPAL_640x512_22t22_48fps = 0xa9004;
    public final static int DBLPAL_640x1024_22t11_interlaced_48fps = 0xa9005;
    private final static Object[][] infoTable = {

{0x0, "NTSC OCS:LowRes"// camg, name
, 320, 200 // text overscan
, 362, 241 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 44, 52 // resolution
, 140 // pixel speed
},//

{0x4, "NTSC OCS:LowRes Interlace"// camg, name
, 320, 400 // text overscan
, 362, 482 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 44, 26 // resolution
, 140 // pixel speed
},//

{0x80, " OCS"// camg, name
, 320, 200 // text overscan
, 362, 241 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 44, 52 // resolution
, 140 // pixel speed
},//

{0x84, " OCS"// camg, name
, 320, 400 // text overscan
, 362, 482 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 44, 26 // resolution
, 140 // pixel speed
},//

{0x400, " OCS"// camg, name
, 320, 200 // text overscan
, 362, 241 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 44, 52 // resolution
, 140 // pixel speed
},//

{0x404, " OCS"// camg, name
, 320, 400 // text overscan
, 362, 482 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 44, 26 // resolution
, 140 // pixel speed
},//

{0x440, " OCS"// camg, name
, 320, 200 // text overscan
, 362, 241 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 44, 52 // resolution
, 140 // pixel speed
},//

{0x444, " OCS"// camg, name
, 320, 400 // text overscan
, 362, 482 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 44, 26 // resolution
, 140 // pixel speed
},//

{0x800, " OCS"// camg, name
, 320, 200 // text overscan
, 362, 241 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 44, 52 // resolution
, 140 // pixel speed
},//

{0x804, " OCS"// camg, name
, 320, 400 // text overscan
, 362, 482 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 44, 26 // resolution
, 140 // pixel speed
},//

{0x8000, "NTSC OCS:HighRes"// camg, name
, 640, 200 // text overscan
, 724, 241 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 22, 52 // resolution
, 70 // pixel speed
},//

{0x8004, "NTSC OCS:HighRes Interlace"// camg, name
, 640, 400 // text overscan
, 724, 482 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 22, 26 // resolution
, 70 // pixel speed
},//

{0x8020, "NTSC OCS:SuperHighRes"// camg, name
, 1280, 200 // text overscan
, 1448, 241 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 11, 52 // resolution
, 35 // pixel speed
},//

{0x8024, "NTSC OCS:SuperHighRes Interlace"// camg, name
, 1280, 400 // text overscan
, 1448, 482 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 11, 26 // resolution
, 35 // pixel speed
},//

{0x8080, ""// camg, name
, 640, 200 // text overscan
, 724, 241 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 22, 52 // resolution
, 70 // pixel speed
},//

{0x8084, ""// camg, name
, 640, 400 // text overscan
, 724, 482 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 22, 26 // resolution
, 70 // pixel speed
},//

{0x80a0, ""// camg, name
, 1280, 200 // text overscan
, 1448, 241 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 11, 52 // resolution
, 35 // pixel speed
},//

{0x80a4, ""// camg, name
, 1280, 400 // text overscan
, 1448, 482 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 11, 26 // resolution
, 35 // pixel speed
},//

{0x8400, ""// camg, name
, 640, 200 // text overscan
, 724, 241 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 22, 52 // resolution
, 70 // pixel speed
},//

{0x8404, ""// camg, name
, 640, 400 // text overscan
, 724, 482 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 22, 26 // resolution
, 70 // pixel speed
},//

{0x8420, ""// camg, name
, 1280, 200 // text overscan
, 1448, 241 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 11, 52 // resolution
, 35 // pixel speed
},//

{0x8424, ""// camg, name
, 1280, 400 // text overscan
, 1448, 482 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 11, 26 // resolution
, 35 // pixel speed
},//

{0x8440, ""// camg, name
, 640, 200 // text overscan
, 724, 241 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 22, 52 // resolution
, 70 // pixel speed
},//

{0x8444, ""// camg, name
, 640, 400 // text overscan
, 724, 482 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 22, 26 // resolution
, 70 // pixel speed
},//

{0x8460, ""// camg, name
, 1280, 200 // text overscan
, 1448, 241 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 11, 52 // resolution
, 35 // pixel speed
},//

{0x8464, ""// camg, name
, 1280, 400 // text overscan
, 1448, 482 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 11, 26 // resolution
, 35 // pixel speed
},//

{0x8800, ""// camg, name
, 640, 200 // text overscan
, 724, 241 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 22, 52 // resolution
, 70 // pixel speed
},//

{0x8804, ""// camg, name
, 640, 400 // text overscan
, 724, 482 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 22, 26 // resolution
, 70 // pixel speed
},//

{0x8820, ""// camg, name
, 1280, 200 // text overscan
, 1448, 241 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 11, 52 // resolution
, 35 // pixel speed
},//

{0x8824, ""// camg, name
, 1280, 400 // text overscan
, 1448, 482 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 4 // color register depth
, 11, 26 // resolution
, 35 // pixel speed
},//

{0x11000, "NTSC:LowRes"// camg, name
, 320, 200 // text overscan
, 362, 241 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 52 // resolution
, 140 // pixel speed
},//

{0x11004, "NTSC:LowRes Interlace"// camg, name
, 320, 400 // text overscan
, 362, 482 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 26 // resolution
, 140 // pixel speed
},//

{0x11080, ""// camg, name
, 320, 200 // text overscan
, 362, 241 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 52 // resolution
, 140 // pixel speed
},//

{0x11084, ""// camg, name
, 320, 400 // text overscan
, 362, 482 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 26 // resolution
, 140 // pixel speed
},//

{0x11400, ""// camg, name
, 320, 200 // text overscan
, 362, 241 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 52 // resolution
, 140 // pixel speed
},//

{0x11404, ""// camg, name
, 320, 400 // text overscan
, 362, 482 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 26 // resolution
, 140 // pixel speed
},//

{0x11440, ""// camg, name
, 320, 200 // text overscan
, 362, 241 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 52 // resolution
, 140 // pixel speed
},//

{0x11444, ""// camg, name
, 320, 400 // text overscan
, 362, 482 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 26 // resolution
, 140 // pixel speed
},//

{0x11800, ""// camg, name
, 320, 200 // text overscan
, 362, 241 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 52 // resolution
, 140 // pixel speed
},//

{0x11804, ""// camg, name
, 320, 400 // text overscan
, 362, 482 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 26 // resolution
, 140 // pixel speed
},//

{0x19000, "NTSC:HighRes"// camg, name
, 640, 200 // text overscan
, 724, 241 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 52 // resolution
, 70 // pixel speed
},//

{0x19004, "NTSC:HighRes Interlace"// camg, name
, 640, 400 // text overscan
, 724, 482 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 26 // resolution
, 70 // pixel speed
},//

{0x19020, "NTSC:SuperHighRes"// camg, name
, 1280, 200 // text overscan
, 1448, 241 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 52 // resolution
, 35 // pixel speed
},//

{0x19024, "NTSC:SuperHighRes Interlace"// camg, name
, 1280, 400 // text overscan
, 1448, 482 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 26 // resolution
, 35 // pixel speed
},//

{0x19080, ""// camg, name
, 640, 200 // text overscan
, 724, 241 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 52 // resolution
, 70 // pixel speed
},//

{0x19084, ""// camg, name
, 640, 400 // text overscan
, 724, 482 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 26 // resolution
, 70 // pixel speed
},//

{0x190a0, ""// camg, name
, 1280, 200 // text overscan
, 1448, 241 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 52 // resolution
, 35 // pixel speed
},//

{0x190a4, ""// camg, name
, 1280, 400 // text overscan
, 1448, 482 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 26 // resolution
, 35 // pixel speed
},//

{0x19400, ""// camg, name
, 640, 200 // text overscan
, 724, 241 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 52 // resolution
, 70 // pixel speed
},//

{0x19404, ""// camg, name
, 640, 400 // text overscan
, 724, 482 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 26 // resolution
, 70 // pixel speed
},//

{0x19420, ""// camg, name
, 1280, 200 // text overscan
, 1448, 241 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 52 // resolution
, 35 // pixel speed
},//

{0x19424, ""// camg, name
, 1280, 400 // text overscan
, 1448, 482 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 26 // resolution
, 35 // pixel speed
},//

{0x19440, ""// camg, name
, 640, 200 // text overscan
, 724, 241 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 52 // resolution
, 70 // pixel speed
},//

{0x19444, ""// camg, name
, 640, 400 // text overscan
, 724, 482 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 26 // resolution
, 70 // pixel speed
},//

{0x19460, ""// camg, name
, 1280, 200 // text overscan
, 1448, 241 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 52 // resolution
, 35 // pixel speed
},//

{0x19464, ""// camg, name
, 1280, 400 // text overscan
, 1448, 482 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 26 // resolution
, 35 // pixel speed
},//

{0x19800, ""// camg, name
, 640, 200 // text overscan
, 724, 241 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 52 // resolution
, 70 // pixel speed
},//

{0x19804, ""// camg, name
, 640, 400 // text overscan
, 724, 482 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 26 // resolution
, 70 // pixel speed
},//

{0x19820, ""// camg, name
, 1280, 200 // text overscan
, 1448, 241 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 52 // resolution
, 35 // pixel speed
},//

{0x19824, ""// camg, name
, 1280, 400 // text overscan
, 1448, 482 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 26 // resolution
, 35 // pixel speed
},//

{0x21000, "PAL:LowRes"// camg, name
, 320, 256 // text overscan
, 362, 283 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 140 // pixel speed
},//

{0x21004, "PAL:LowRes Interlace"// camg, name
, 320, 512 // text overscan
, 362, 566 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 140 // pixel speed
},//

{0x21080, ""// camg, name
, 320, 256 // text overscan
, 362, 283 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 140 // pixel speed
},//

{0x21084, ""// camg, name
, 320, 512 // text overscan
, 362, 566 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 140 // pixel speed
},//

{0x21400, ""// camg, name
, 320, 256 // text overscan
, 362, 283 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 140 // pixel speed
},//

{0x21404, ""// camg, name
, 320, 512 // text overscan
, 362, 566 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 140 // pixel speed
},//

{0x21440, ""// camg, name
, 320, 256 // text overscan
, 362, 283 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 140 // pixel speed
},//

{0x21444, ""// camg, name
, 320, 512 // text overscan
, 362, 566 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 140 // pixel speed
},//

{0x21800, ""// camg, name
, 320, 256 // text overscan
, 362, 283 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 140 // pixel speed
},//

{0x21804, ""// camg, name
, 320, 512 // text overscan
, 362, 566 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 140 // pixel speed
},//

{0x29000, "PAL:HighRes"// camg, name
, 640, 256 // text overscan
, 724, 283 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 70 // pixel speed
},//

{0x29004, "PAL:HighRes Interlace"// camg, name
, 640, 512 // text overscan
, 724, 566 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 70 // pixel speed
},//

{0x29020, "PAL:SuperHighRes"// camg, name
, 1280, 256 // text overscan
, 1448, 283 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 44 // resolution
, 35 // pixel speed
},//

{0x29024, "PAL:SuperHighRes Interlace"// camg, name
, 1280, 512 // text overscan
, 1448, 566 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 22 // resolution
, 35 // pixel speed
},//

{0x29080, ""// camg, name
, 640, 256 // text overscan
, 724, 283 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 70 // pixel speed
},//

{0x29084, ""// camg, name
, 640, 512 // text overscan
, 724, 566 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 70 // pixel speed
},//

{0x290a0, ""// camg, name
, 1280, 256 // text overscan
, 1448, 283 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 44 // resolution
, 35 // pixel speed
},//

{0x290a4, ""// camg, name
, 1280, 512 // text overscan
, 1448, 566 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 22 // resolution
, 35 // pixel speed
},//

{0x29400, ""// camg, name
, 640, 256 // text overscan
, 724, 283 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 70 // pixel speed
},//

{0x29404, ""// camg, name
, 640, 512 // text overscan
, 724, 566 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 70 // pixel speed
},//

{0x29420, ""// camg, name
, 1280, 256 // text overscan
, 1448, 283 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 44 // resolution
, 35 // pixel speed
},//

{0x29424, ""// camg, name
, 1280, 512 // text overscan
, 1448, 566 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 22 // resolution
, 35 // pixel speed
},//

{0x29440, ""// camg, name
, 640, 256 // text overscan
, 724, 283 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 70 // pixel speed
},//

{0x29444, ""// camg, name
, 640, 512 // text overscan
, 724, 566 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 70 // pixel speed
},//

{0x29460, ""// camg, name
, 1280, 256 // text overscan
, 1448, 283 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 44 // resolution
, 35 // pixel speed
},//

{0x29464, ""// camg, name
, 1280, 512 // text overscan
, 1448, 566 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 22 // resolution
, 35 // pixel speed
},//

{0x29800, ""// camg, name
, 640, 256 // text overscan
, 724, 283 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 70 // pixel speed
},//

{0x29804, ""// camg, name
, 640, 512 // text overscan
, 724, 566 // max overscan
, 32, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 70 // pixel speed
},//

{0x29820, ""// camg, name
, 1280, 256 // text overscan
, 1448, 283 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 44 // resolution
, 35 // pixel speed
},//

{0x29824, ""// camg, name
, 1280, 512 // text overscan
, 1448, 566 // max overscan
, 64, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 22 // resolution
, 35 // pixel speed
},//

{0x31000, "MULTISCAN:ExtraLowRes"// camg, name
, 160, 240 // text overscan
, 164, 240 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 44 // resolution
, 140 // pixel speed
},//

{0x31004, "MULTISCAN:ExtraLowRes"// camg, name
, 160, 480 // text overscan
, 164, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0x31005, "MULTISCAN:ExtraLowRes Interlace"// camg, name
, 160, 960 // text overscan
, 164, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0x31080, ""// camg, name
, 160, 240 // text overscan
, 164, 240 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 44 // resolution
, 140 // pixel speed
},//

{0x31084, ""// camg, name
, 160, 480 // text overscan
, 164, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0x31085, ""// camg, name
, 160, 960 // text overscan
, 164, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0x31404, ""// camg, name
, 160, 480 // text overscan
, 164, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0x31405, ""// camg, name
, 160, 960 // text overscan
, 164, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0x31444, ""// camg, name
, 160, 480 // text overscan
, 164, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0x31445, ""// camg, name
, 160, 960 // text overscan
, 164, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0x31800, ""// camg, name
, 160, 240 // text overscan
, 164, 240 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 44 // resolution
, 140 // pixel speed
},//

{0x31804, ""// camg, name
, 160, 480 // text overscan
, 164, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0x31805, ""// camg, name
, 160, 960 // text overscan
, 164, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0x39000, "MULTISCAN:LowRes"// camg, name
, 320, 240 // text overscan
, 328, 240 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 70 // pixel speed
},//

{0x39004, "MULTISCAN:LowRes"// camg, name
, 320, 480 // text overscan
, 328, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0x39005, "MULTISCAN:LowRes Interlace"// camg, name
, 320, 960 // text overscan
, 328, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0x39020, "MULTISCAN:Productivity"// camg, name
, 640, 240 // text overscan
, 656, 240 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 35 // pixel speed
},//

{0x39024, "MULTISCAN:Productivity"// camg, name
, 640, 480 // text overscan
, 656, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0x39025, "MULTISCAN:Productivity Interl."// camg, name
, 640, 960 // text overscan
, 656, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0x39080, ""// camg, name
, 320, 240 // text overscan
, 328, 240 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 70 // pixel speed
},//

{0x39084, ""// camg, name
, 320, 480 // text overscan
, 328, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0x39085, ""// camg, name
, 320, 960 // text overscan
, 328, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0x390a0, ""// camg, name
, 640, 240 // text overscan
, 656, 240 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 35 // pixel speed
},//

{0x390a4, ""// camg, name
, 640, 480 // text overscan
, 656, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0x390a5, ""// camg, name
, 640, 960 // text overscan
, 656, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0x39404, ""// camg, name
, 320, 480 // text overscan
, 328, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0x39405, ""// camg, name
, 320, 960 // text overscan
, 328, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0x39424, ""// camg, name
, 640, 480 // text overscan
, 656, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0x39425, ""// camg, name
, 640, 960 // text overscan
, 656, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0x39444, ""// camg, name
, 320, 480 // text overscan
, 328, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0x39445, ""// camg, name
, 320, 960 // text overscan
, 328, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0x39464, ""// camg, name
, 640, 480 // text overscan
, 656, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0x39465, ""// camg, name
, 640, 960 // text overscan
, 656, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0x39800, ""// camg, name
, 320, 240 // text overscan
, 328, 240 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 70 // pixel speed
},//

{0x39804, ""// camg, name
, 320, 480 // text overscan
, 328, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0x39805, ""// camg, name
, 320, 960 // text overscan
, 328, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0x39820, ""// camg, name
, 640, 240 // text overscan
, 656, 240 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 35 // pixel speed
},//

{0x39824, ""// camg, name
, 640, 480 // text overscan
, 656, 480 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0x39825, ""// camg, name
, 640, 960 // text overscan
, 656, 960 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0x61000, "EURO72:ExtraLowRes"// camg, name
, 160, 200 // text overscan
, 164, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 44 // resolution
, 140 // pixel speed
},//

{0x61004, "EURO72:ExtraLowRes"// camg, name
, 160, 400 // text overscan
, 164, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0x61005, "EURO72:ExtraLowRes"// camg, name
, 160, 800 // text overscan
, 164, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0x61080, ""// camg, name
, 160, 200 // text overscan
, 164, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 44 // resolution
, 140 // pixel speed
},//

{0x61084, ""// camg, name
, 160, 400 // text overscan
, 164, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0x61085, ""// camg, name
, 160, 800 // text overscan
, 164, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0x61404, ""// camg, name
, 160, 400 // text overscan
, 164, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0x61405, ""// camg, name
, 160, 800 // text overscan
, 164, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0x61444, ""// camg, name
, 160, 400 // text overscan
, 164, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0x61445, ""// camg, name
, 160, 800 // text overscan
, 164, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0x61800, ""// camg, name
, 160, 200 // text overscan
, 164, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 44 // resolution
, 140 // pixel speed
},//

{0x61804, ""// camg, name
, 160, 400 // text overscan
, 164, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0x61805, ""// camg, name
, 160, 800 // text overscan
, 164, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0x69000, "EURO72:LowRes"// camg, name
, 320, 200 // text overscan
, 328, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 70 // pixel speed
},//

{0x69004, "EURO72:LowRes"// camg, name
, 320, 400 // text overscan
, 328, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0x69005, "EURO72:LowRes"// camg, name
, 320, 800 // text overscan
, 328, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0x69020, "EURO72:Productivity"// camg, name
, 640, 200 // text overscan
, 656, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 35 // pixel speed
},//

{0x69024, "EURO72:Productivity"// camg, name
, 640, 400 // text overscan
, 656, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0x69025, "EURO72:Productivity Interl."// camg, name
, 640, 800 // text overscan
, 656, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0x69080, ""// camg, name
, 320, 200 // text overscan
, 328, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 70 // pixel speed
},//

{0x69084, ""// camg, name
, 320, 400 // text overscan
, 328, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0x69085, ""// camg, name
, 320, 800 // text overscan
, 328, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0x690a0, ""// camg, name
, 640, 200 // text overscan
, 656, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 35 // pixel speed
},//

{0x690a4, ""// camg, name
, 640, 400 // text overscan
, 656, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0x690a5, ""// camg, name
, 640, 800 // text overscan
, 656, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0x69404, ""// camg, name
, 320, 400 // text overscan
, 328, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0x69405, ""// camg, name
, 320, 800 // text overscan
, 328, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0x69424, ""// camg, name
, 640, 400 // text overscan
, 656, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0x69425, ""// camg, name
, 640, 800 // text overscan
, 656, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0x69444, ""// camg, name
, 320, 400 // text overscan
, 328, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0x69445, ""// camg, name
, 320, 800 // text overscan
, 328, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0x69464, ""// camg, name
, 640, 400 // text overscan
, 656, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0x69465, ""// camg, name
, 640, 800 // text overscan
, 656, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0x69800, ""// camg, name
, 320, 200 // text overscan
, 328, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 70 // pixel speed
},//

{0x69804, ""// camg, name
, 320, 400 // text overscan
, 328, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0x69805, ""// camg, name
, 320, 800 // text overscan
, 328, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0x69820, ""// camg, name
, 640, 200 // text overscan
, 656, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 35 // pixel speed
},//

{0x69824, ""// camg, name
, 640, 400 // text overscan
, 656, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0x69825, ""// camg, name
, 640, 800 // text overscan
, 656, 800 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0x71000, "EURO36:LowRes"// camg, name
, 320, 200 // text overscan
, 362, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 140 // pixel speed
},//

{0x71004, "EURO36:LowRes Interlace"// camg, name
, 320, 400 // text overscan
, 362, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 140 // pixel speed
},//

{0x71080, ""// camg, name
, 320, 200 // text overscan
, 362, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 140 // pixel speed
},//

{0x71084, ""// camg, name
, 320, 400 // text overscan
, 362, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 140 // pixel speed
},//

{0x71400, ""// camg, name
, 320, 200 // text overscan
, 362, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 140 // pixel speed
},//

{0x71404, ""// camg, name
, 320, 400 // text overscan
, 362, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 140 // pixel speed
},//

{0x71440, ""// camg, name
, 320, 200 // text overscan
, 362, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 140 // pixel speed
},//

{0x71444, ""// camg, name
, 320, 400 // text overscan
, 362, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 140 // pixel speed
},//

{0x71800, ""// camg, name
, 320, 200 // text overscan
, 362, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 140 // pixel speed
},//

{0x71804, ""// camg, name
, 320, 400 // text overscan
, 362, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 140 // pixel speed
},//

{0x79000, "EURO36:HighRes"// camg, name
, 640, 200 // text overscan
, 724, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 70 // pixel speed
},//

{0x79004, "EURO36:HighRes Interlace"// camg, name
, 640, 400 // text overscan
, 724, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 70 // pixel speed
},//

{0x79020, "EURO36:SuperHighRes"// camg, name
, 1280, 200 // text overscan
, 1448, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 44 // resolution
, 35 // pixel speed
},//

{0x79024, "EURO36:SuperHighRes Interl."// camg, name
, 1280, 400 // text overscan
, 1448, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 22 // resolution
, 35 // pixel speed
},//

{0x79080, ""// camg, name
, 640, 200 // text overscan
, 724, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 70 // pixel speed
},//

{0x79084, ""// camg, name
, 640, 400 // text overscan
, 724, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 70 // pixel speed
},//

{0x790a0, ""// camg, name
, 1280, 200 // text overscan
, 1448, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 44 // resolution
, 35 // pixel speed
},//

{0x790a4, ""// camg, name
, 1280, 400 // text overscan
, 1448, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 22 // resolution
, 35 // pixel speed
},//

{0x79400, ""// camg, name
, 640, 200 // text overscan
, 724, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 70 // pixel speed
},//

{0x79404, ""// camg, name
, 640, 400 // text overscan
, 724, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 70 // pixel speed
},//

{0x79420, ""// camg, name
, 1280, 200 // text overscan
, 1448, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 44 // resolution
, 35 // pixel speed
},//

{0x79424, ""// camg, name
, 1280, 400 // text overscan
, 1448, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 22 // resolution
, 35 // pixel speed
},//

{0x79440, ""// camg, name
, 640, 200 // text overscan
, 724, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 70 // pixel speed
},//

{0x79444, ""// camg, name
, 640, 400 // text overscan
, 724, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 70 // pixel speed
},//

{0x79460, ""// camg, name
, 1280, 200 // text overscan
, 1448, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 44 // resolution
, 35 // pixel speed
},//

{0x79464, ""// camg, name
, 1280, 400 // text overscan
, 1448, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 22 // resolution
, 35 // pixel speed
},//

{0x79800, ""// camg, name
, 640, 200 // text overscan
, 724, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 70 // pixel speed
},//

{0x79804, ""// camg, name
, 640, 400 // text overscan
, 724, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 70 // pixel speed
},//

{0x79820, ""// camg, name
, 1280, 200 // text overscan
, 1448, 200 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 44 // resolution
, 35 // pixel speed
},//

{0x79824, ""// camg, name
, 1280, 400 // text overscan
, 1448, 400 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 11, 22 // resolution
, 35 // pixel speed
},//

{0x81000, "SUPER72:LowRes"// camg, name
, 200, 300 // text overscan
, 228, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 40 // resolution
, 140 // pixel speed
},//

{0x81004, "SUPER72:LowRes Interlace"// camg, name
, 200, 600 // text overscan
, 228, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 20 // resolution
, 140 // pixel speed
},//

{0x81008, "SUPER72:LowRes"// camg, name
, 200, 150 // text overscan
, 228, 153 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 80 // resolution
, 140 // pixel speed
},//

{0x81080, ""// camg, name
, 200, 300 // text overscan
, 228, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 40 // resolution
, 140 // pixel speed
},//

{0x81084, ""// camg, name
, 200, 600 // text overscan
, 228, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 20 // resolution
, 140 // pixel speed
},//

{0x81088, ""// camg, name
, 200, 150 // text overscan
, 228, 153 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 80 // resolution
, 140 // pixel speed
},//

{0x81400, ""// camg, name
, 200, 300 // text overscan
, 228, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 40 // resolution
, 140 // pixel speed
},//

{0x81404, ""// camg, name
, 200, 600 // text overscan
, 228, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 20 // resolution
, 140 // pixel speed
},//

{0x81440, ""// camg, name
, 200, 300 // text overscan
, 228, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 40 // resolution
, 140 // pixel speed
},//

{0x81444, ""// camg, name
, 200, 600 // text overscan
, 228, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 20 // resolution
, 140 // pixel speed
},//

{0x81800, ""// camg, name
, 200, 300 // text overscan
, 228, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 40 // resolution
, 140 // pixel speed
},//

{0x81804, ""// camg, name
, 200, 600 // text overscan
, 228, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 20 // resolution
, 140 // pixel speed
},//

{0x81808, ""// camg, name
, 200, 150 // text overscan
, 228, 153 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 68, 80 // resolution
, 140 // pixel speed
},//

{0x89000, "SUPER72:HighRes"// camg, name
, 400, 300 // text overscan
, 456, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 40 // resolution
, 70 // pixel speed
},//

{0x89004, "SUPER72:HighRes Interlace"// camg, name
, 400, 600 // text overscan
, 456, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 20 // resolution
, 70 // pixel speed
},//

{0x89008, "SUPER72:HighRes"// camg, name
, 400, 150 // text overscan
, 456, 153 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 80 // resolution
, 70 // pixel speed
},//

{0x89020, "SUPER72:SuperHighRes"// camg, name
, 800, 300 // text overscan
, 912, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 40 // resolution
, 35 // pixel speed
},//

{0x89024, "SUPER72:SuperHighRes Interlace"// camg, name
, 800, 600 // text overscan
, 912, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 20 // resolution
, 35 // pixel speed
},//

{0x89028, "SUPER72:HighRes"// camg, name
, 800, 150 // text overscan
, 912, 153 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 80 // resolution
, 35 // pixel speed
},//

{0x89080, ""// camg, name
, 400, 300 // text overscan
, 456, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 40 // resolution
, 70 // pixel speed
},//

{0x89084, ""// camg, name
, 400, 600 // text overscan
, 456, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 20 // resolution
, 70 // pixel speed
},//

{0x89088, ""// camg, name
, 400, 150 // text overscan
, 456, 153 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 80 // resolution
, 70 // pixel speed
},//

{0x890a0, ""// camg, name
, 800, 300 // text overscan
, 912, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 40 // resolution
, 35 // pixel speed
},//

{0x890a4, ""// camg, name
, 800, 600 // text overscan
, 912, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 20 // resolution
, 35 // pixel speed
},//

{0x890a8, ""// camg, name
, 800, 150 // text overscan
, 912, 153 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 80 // resolution
, 35 // pixel speed
},//

{0x89400, ""// camg, name
, 400, 300 // text overscan
, 456, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 40 // resolution
, 70 // pixel speed
},//

{0x89404, ""// camg, name
, 400, 600 // text overscan
, 456, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 20 // resolution
, 70 // pixel speed
},//

{0x89420, ""// camg, name
, 800, 300 // text overscan
, 912, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 40 // resolution
, 35 // pixel speed
},//

{0x89424, ""// camg, name
, 800, 600 // text overscan
, 912, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 20 // resolution
, 35 // pixel speed
},//

{0x89440, ""// camg, name
, 400, 300 // text overscan
, 456, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 40 // resolution
, 70 // pixel speed
},//

{0x89444, ""// camg, name
, 400, 600 // text overscan
, 456, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 20 // resolution
, 70 // pixel speed
},//

{0x89460, ""// camg, name
, 800, 300 // text overscan
, 912, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 40 // resolution
, 35 // pixel speed
},//

{0x89464, ""// camg, name
, 800, 600 // text overscan
, 912, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 20 // resolution
, 35 // pixel speed
},//

{0x89800, ""// camg, name
, 400, 300 // text overscan
, 456, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 40 // resolution
, 70 // pixel speed
},//

{0x89804, ""// camg, name
, 400, 600 // text overscan
, 456, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 20 // resolution
, 70 // pixel speed
},//

{0x89808, ""// camg, name
, 400, 150 // text overscan
, 456, 153 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 34, 80 // resolution
, 70 // pixel speed
},//

{0x89820, ""// camg, name
, 800, 300 // text overscan
, 912, 306 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 40 // resolution
, 35 // pixel speed
},//

{0x89824, ""// camg, name
, 800, 600 // text overscan
, 912, 612 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 20 // resolution
, 35 // pixel speed
},//

{0x89828, ""// camg, name
, 800, 150 // text overscan
, 912, 153 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 17, 80 // resolution
, 35 // pixel speed
},//

{0x91000, "DBLNTSC:LowRes"// camg, name
, 320, 200 // text overscan
, 360, 227 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 52 // resolution
, 70 // pixel speed
},//

{0x91004, "DBLNTSC:LowRes Flickerfree"// camg, name
, 320, 400 // text overscan
, 360, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 26 // resolution
, 70 // pixel speed
},//

{0x91005, "DBLNTSC:LowRes Interlace"// camg, name
, 320, 800 // text overscan
, 360, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 13 // resolution
, 70 // pixel speed
},//

{0x91080, ""// camg, name
, 320, 200 // text overscan
, 360, 227 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 52 // resolution
, 70 // pixel speed
},//

{0x91084, ""// camg, name
, 320, 400 // text overscan
, 360, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 26 // resolution
, 70 // pixel speed
},//

{0x91085, ""// camg, name
, 320, 800 // text overscan
, 360, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 13 // resolution
, 70 // pixel speed
},//

{0x91200, "DBLNTSC:ExtraLowRes"// camg, name
, 160, 200 // text overscan
, 180, 227 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 52 // resolution
, 140 // pixel speed
},//

{0x91204, "DBLNTSC:ExtraLowRes"// camg, name
, 160, 400 // text overscan
, 180, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 26 // resolution
, 140 // pixel speed
},//

{0x91205, "DBLNTSC:ExtraLowRes"// camg, name
, 160, 800 // text overscan
, 180, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 13 // resolution
, 140 // pixel speed
},//

{0x91280, ""// camg, name
, 160, 200 // text overscan
, 180, 227 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 52 // resolution
, 140 // pixel speed
},//

{0x91284, ""// camg, name
, 160, 400 // text overscan
, 180, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 26 // resolution
, 140 // pixel speed
},//

{0x91285, ""// camg, name
, 160, 800 // text overscan
, 180, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 13 // resolution
, 140 // pixel speed
},//

{0x91400, ""// camg, name
, 320, 400 // text overscan
, 360, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 26 // resolution
, 70 // pixel speed
},//

{0x91404, ""// camg, name
, 320, 400 // text overscan
, 360, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 26 // resolution
, 70 // pixel speed
},//

{0x91405, ""// camg, name
, 320, 800 // text overscan
, 360, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 13 // resolution
, 70 // pixel speed
},//

{0x91440, ""// camg, name
, 320, 400 // text overscan
, 360, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 26 // resolution
, 70 // pixel speed
},//

{0x91444, ""// camg, name
, 320, 400 // text overscan
, 360, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 26 // resolution
, 70 // pixel speed
},//

{0x91445, ""// camg, name
, 320, 800 // text overscan
, 360, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 13 // resolution
, 70 // pixel speed
},//

{0x91600, ""// camg, name
, 160, 400 // text overscan
, 180, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 26 // resolution
, 140 // pixel speed
},//

{0x91604, ""// camg, name
, 160, 400 // text overscan
, 180, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 26 // resolution
, 140 // pixel speed
},//

{0x91605, ""// camg, name
, 160, 800 // text overscan
, 180, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 13 // resolution
, 140 // pixel speed
},//

{0x91640, ""// camg, name
, 160, 400 // text overscan
, 180, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 26 // resolution
, 140 // pixel speed
},//

{0x91644, ""// camg, name
, 160, 400 // text overscan
, 180, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 26 // resolution
, 140 // pixel speed
},//

{0x91645, ""// camg, name
, 160, 800 // text overscan
, 180, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 13 // resolution
, 140 // pixel speed
},//

{0x91800, ""// camg, name
, 320, 200 // text overscan
, 360, 227 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 52 // resolution
, 70 // pixel speed
},//

{0x91804, ""// camg, name
, 320, 400 // text overscan
, 360, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 26 // resolution
, 70 // pixel speed
},//

{0x91805, ""// camg, name
, 320, 800 // text overscan
, 360, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 13 // resolution
, 70 // pixel speed
},//

{0x91a00, ""// camg, name
, 160, 200 // text overscan
, 180, 227 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 52 // resolution
, 140 // pixel speed
},//

{0x91a04, ""// camg, name
, 160, 400 // text overscan
, 180, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 26 // resolution
, 140 // pixel speed
},//

{0x91a05, ""// camg, name
, 160, 800 // text overscan
, 180, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 13 // resolution
, 140 // pixel speed
},//

{0x99000, "DBLNTSC:HighRes"// camg, name
, 640, 200 // text overscan
, 720, 227 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 52 // resolution
, 35 // pixel speed
},//

{0x99004, "DBLNTSC:HighRes Flickerfree"// camg, name
, 640, 400 // text overscan
, 720, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 26 // resolution
, 35 // pixel speed
},//

{0x99005, "DBLNTSC:HighRes Interlace"// camg, name
, 640, 800 // text overscan
, 720, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 13 // resolution
, 35 // pixel speed
},//

{0x99080, ""// camg, name
, 640, 200 // text overscan
, 720, 227 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 52 // resolution
, 35 // pixel speed
},//

{0x99084, ""// camg, name
, 640, 400 // text overscan
, 720, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 26 // resolution
, 35 // pixel speed
},//

{0x99085, ""// camg, name
, 640, 800 // text overscan
, 720, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 13 // resolution
, 35 // pixel speed
},//

{0x99400, ""// camg, name
, 640, 400 // text overscan
, 720, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 26 // resolution
, 35 // pixel speed
},//

{0x99404, ""// camg, name
, 640, 400 // text overscan
, 720, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 26 // resolution
, 35 // pixel speed
},//

{0x99405, ""// camg, name
, 640, 800 // text overscan
, 720, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 13 // resolution
, 35 // pixel speed
},//

{0x99440, ""// camg, name
, 640, 400 // text overscan
, 720, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 26 // resolution
, 35 // pixel speed
},//

{0x99444, ""// camg, name
, 640, 400 // text overscan
, 720, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 26 // resolution
, 35 // pixel speed
},//

{0x99445, ""// camg, name
, 640, 800 // text overscan
, 720, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 13 // resolution
, 35 // pixel speed
},//

{0x99800, ""// camg, name
, 640, 200 // text overscan
, 720, 227 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 52 // resolution
, 35 // pixel speed
},//

{0x99804, ""// camg, name
, 640, 400 // text overscan
, 720, 454 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 26 // resolution
, 35 // pixel speed
},//

{0x99805, ""// camg, name
, 640, 800 // text overscan
, 720, 908 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 13 // resolution
, 35 // pixel speed
},//

{0xa1000, "DBLPAL:LowRes"// camg, name
, 320, 256 // text overscan
, 360, 275 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 70 // pixel speed
},//

{0xa1004, "DBLPAL:LowRes Flickerfree"// camg, name
, 320, 512 // text overscan
, 360, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0xa1005, "DBLPAL:LowRes Interlace"// camg, name
, 320, 1024 // text overscan
, 360, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0xa1080, ""// camg, name
, 320, 256 // text overscan
, 360, 275 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 70 // pixel speed
},//

{0xa1084, ""// camg, name
, 320, 512 // text overscan
, 360, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0xa1085, ""// camg, name
, 320, 1024 // text overscan
, 360, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0xa1200, "DBLPAL:ExtraLowRes"// camg, name
, 160, 256 // text overscan
, 180, 275 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 44 // resolution
, 140 // pixel speed
},//

{0xa1204, "DBLPAL:ExtraLowRes"// camg, name
, 160, 512 // text overscan
, 180, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0xa1205, "DBLPAL:ExtraLowRes"// camg, name
, 160, 1024 // text overscan
, 180, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0xa1280, ""// camg, name
, 160, 256 // text overscan
, 180, 275 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 44 // resolution
, 140 // pixel speed
},//

{0xa1284, ""// camg, name
, 160, 512 // text overscan
, 180, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0xa1285, ""// camg, name
, 160, 1024 // text overscan
, 180, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0xa1400, ""// camg, name
, 320, 512 // text overscan
, 360, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0xa1404, ""// camg, name
, 320, 512 // text overscan
, 360, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0xa1405, ""// camg, name
, 320, 1024 // text overscan
, 360, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0xa1440, ""// camg, name
, 320, 512 // text overscan
, 360, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0xa1444, ""// camg, name
, 320, 512 // text overscan
, 360, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0xa1445, ""// camg, name
, 320, 1024 // text overscan
, 360, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0xa1600, ""// camg, name
, 160, 512 // text overscan
, 180, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0xa1604, ""// camg, name
, 160, 512 // text overscan
, 180, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0xa1605, ""// camg, name
, 160, 1024 // text overscan
, 180, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0xa1640, ""// camg, name
, 160, 512 // text overscan
, 180, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0xa1644, ""// camg, name
, 160, 512 // text overscan
, 180, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0xa1645, ""// camg, name
, 160, 1024 // text overscan
, 180, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0xa1800, ""// camg, name
, 320, 256 // text overscan
, 360, 275 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 44 // resolution
, 70 // pixel speed
},//

{0xa1804, ""// camg, name
, 320, 512 // text overscan
, 360, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 22 // resolution
, 70 // pixel speed
},//

{0xa1805, ""// camg, name
, 320, 1024 // text overscan
, 360, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 44, 11 // resolution
, 70 // pixel speed
},//

{0xa1a00, ""// camg, name
, 160, 256 // text overscan
, 180, 275 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 44 // resolution
, 140 // pixel speed
},//

{0xa1a04, ""// camg, name
, 160, 512 // text overscan
, 180, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 22 // resolution
, 140 // pixel speed
},//

{0xa1a05, ""// camg, name
, 160, 1024 // text overscan
, 180, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 88, 11 // resolution
, 140 // pixel speed
},//

{0xa9000, "DBLPAL:HighRes"// camg, name
, 640, 256 // text overscan
, 720, 275 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 35 // pixel speed
},//

{0xa9004, "DBLPAL:HighRes Flickerfree"// camg, name
, 640, 512 // text overscan
, 720, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0xa9005, "DBLPAL:HighRes Interlace"// camg, name
, 640, 1024 // text overscan
, 720, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0xa9080, ""// camg, name
, 640, 256 // text overscan
, 720, 275 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 35 // pixel speed
},//

{0xa9084, ""// camg, name
, 640, 512 // text overscan
, 720, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0xa9085, ""// camg, name
, 640, 1024 // text overscan
, 720, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0xa9400, ""// camg, name
, 640, 512 // text overscan
, 720, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0xa9404, ""// camg, name
, 640, 512 // text overscan
, 720, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0xa9405, ""// camg, name
, 640, 1024 // text overscan
, 720, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0xa9440, ""// camg, name
, 640, 512 // text overscan
, 720, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0xa9444, ""// camg, name
, 640, 512 // text overscan
, 720, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0xa9445, ""// camg, name
, 640, 1024 // text overscan
, 720, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//

{0xa9800, ""// camg, name
, 640, 256 // text overscan
, 720, 275 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 44 // resolution
, 35 // pixel speed
},//

{0xa9804, ""// camg, name
, 640, 512 // text overscan
, 720, 550 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 22 // resolution
, 35 // pixel speed
},//

{0xa9805, ""// camg, name
, 640, 1024 // text overscan
, 720, 1100 // max overscan
, 16, 1 // minimal size
, 16368, 16384 // maximal size
, 8 // color register depth
, 22, 11 // resolution
, 35 // pixel speed
},//
    };
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

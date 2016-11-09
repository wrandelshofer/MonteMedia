/* @(#)AmigaCAMGMainx.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.anim;

import java.util.Map;
import java.util.TreeMap;

/**
 * {@code AmigaCAMGMainx}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-08-30 Created.
 */
public class AmigaCAMGMainx {

    private final static int[] mask = {};

    public static void main(String[] args) {
        TreeMap<Integer, String> v = createValueMap();
        TreeMap<Integer, String> m = createModeMap();
        TreeMap<Integer, String> moni = createMonitorMap();

        for (Map.Entry<Integer, String> e : v.entrySet()) {
            //      System.out.println("0x"+Integer.toHexString(e.getKey())+"="+e.getValue());
            
            String mode="";
            for (Map.Entry<Integer, String> em : m.entrySet()) {
                
                
                if (isSet(e.getKey(), em.getKey())) {
                    if (mode.length()>0)mode+=", ";
                    mode+=em.getValue();
                }
            }
            String monitor="";
            for (Map.Entry<Integer, String> em : moni.entrySet()) {
                if (isEnum(e.getKey(), 0xffff1000, em.getKey())) {
                    monitor+=em.getValue();
                }
            }
            int p=monitor.indexOf(',');
            String fps=monitor.substring(p);
            
            int descKey=e.getKey(); if (isEnum(descKey,0xffff1000,0)) {descKey|=0x00011000;}
            System.out.println("  \"" + v.get(descKey)+fps + "\"=0x" + Integer.toHexString(e.getKey()) + ",");
        }
    }

    private static boolean isSet(int flag, int mask) {
        return (flag & mask) == mask;
    }
    private static boolean isEnum(int flag, int mask, int value) {
        return (flag & mask) == value;
    }

    private static TreeMap<Integer, String> createModeMap() {
        TreeMap<Integer, String> m = new TreeMap<Integer, String>();

        m.put(0x0004, "Interlace");
        m.put(0x0080, "ExtraHalfbrite");
        m.put(0x0400, "DualPlayfield");
        m.put(0x0800, "HoldAndModify");
        m.put(0x8000, "Hires");
        m.put(0x8020, "Super");
        return m;
    }
    private static TreeMap<Integer, String> createMonitorMap() {
        TreeMap<Integer, String> m = new TreeMap<Integer, String>();


        m.put(0x00000000, "default, OCS, 60fps");
        m.put(0x00011000, "NTSC, 60fps");
        m.put(0x00021000, "PAL, 50fps");
        m.put(0x00031000, "MULTISCAN, 58fps");
        m.put(0x00061000, "EURO72, 69fps");
        m.put(0x00071000, "EURO36, 73fps");
        m.put(0x00081000, "SUPER72, 71fps");
        m.put(0x00091000, "DBLNTSC, 58fps");
        m.put(0x000a1000, "DBLPAL, 48fps"); // 320x256, 70ns
        return m;
    }

    private static TreeMap<Integer, String> createValueMap() {
        TreeMap<Integer, String> m = new TreeMap<Integer, String>();

        m.put(0, "DBLPAL:LowRes, 320x256, 44:44, 70ns, PAL");
        m.put(32768, "DBLPAL:HighRes, 640x256, 22:44, 35ns, PAL");
        m.put(32800, "DBLPAL:HighRes, 640x256, 22:44, 35ns, PAL");
        m.put(2048, ", 320x256, 44:44, 70ns, HAM, PAL");
        m.put(128, ", 320x256, 44:44, 70ns, PAL, EHB");
        m.put(4, "DBLPAL:LowRes flicker-free, 320x512, 44:22, 70ns, PAL");
        m.put(32772, "DBLPAL:HighRes flicker-free, 640x512, 22:22, 35ns, PAL");
        m.put(32804, "DBLPAL:HighRes flicker-free, 640x512, 22:22, 35ns, PAL");
        m.put(2052, ", 320x512, 44:22, 70ns, HAM, PAL");
        m.put(132, ", 320x512, 44:22, 70ns, PAL, EHB");
        m.put(1024, ", 320x512, 44:22, 70ns, dual playfield, PAL");
        m.put(33792, ", 640x512, 22:22, 35ns, dual playfield, PAL");
        m.put(33824, ", 640x512, 22:22, 35ns, dual playfield, PAL");
        m.put(1028, ", 320x512, 44:22, 70ns, dual playfield, PAL");
        m.put(33796, ", 640x512, 22:22, 35ns, dual playfield, PAL");
        m.put(33828, ", 640x512, 22:22, 35ns, dual playfield, PAL");
        m.put(1088, ", 320x512, 44:22, 70ns, dual playfield, PAL");
        m.put(33856, ", 640x512, 22:22, 35ns, dual playfield, PAL");
        m.put(33888, ", 640x512, 22:22, 35ns, dual playfield, PAL");
        m.put(1092, ", 320x512, 44:22, 70ns, dual playfield, PAL");
        m.put(33860, ", 640x512, 22:22, 35ns, dual playfield, PAL");
        m.put(33892, ", 640x512, 22:22, 35ns, dual playfield, PAL");
        m.put(34816, ", 640x256, 22:44, 35ns, HAM, PAL");
        m.put(34848, ", 640x256, 22:44, 35ns, HAM, PAL");
        m.put(34820, ", 640x512, 22:22, 35ns, HAM, PAL");
        m.put(34852, ", 640x512, 22:22, 35ns, HAM, PAL");
        m.put(32896, ", 640x256, 22:44, 35ns, PAL, EHB");
        m.put(32928, ", 640x256, 22:44, 35ns, PAL, EHB");
        m.put(32900, ", 640x512, 22:22, 35ns, PAL, EHB");
        m.put(32932, ", 640x512, 22:22, 35ns, PAL, EHB");
        m.put(135168, "PAL:LowRes, 320x256, 44:44, 140ns, PAL");
        m.put(167936, "PAL:HighRes, 640x256, 22:44, 70ns, PAL");
        m.put(167968, "PAL:SuperHighRes, 1280x256, 11:44, 35ns, PAL");
        m.put(137216, ", 320x256, 44:44, 140ns, HAM, PAL");
        m.put(135296, ", 320x256, 44:44, 140ns, PAL, EHB");
        m.put(135172, "PAL:LowRes Interlace, 320x512, 44:22, 140ns, interlaced, PAL");
        m.put(167940, "PAL:HighRes Interlace, 640x512, 22:22, 70ns, interlaced, PAL");
        m.put(167972, "PAL:SuperHighRes Interlace, 1280x512, 11:22, 35ns, interlaced, PAL");
        m.put(137220, ", 320x512, 44:22, 140ns, interlaced, HAM, PAL");
        m.put(135300, ", 320x512, 44:22, 140ns, interlaced, PAL, EHB");
        m.put(136192, ", 320x256, 44:44, 140ns, dual playfield, PAL");
        m.put(168960, ", 640x256, 22:44, 70ns, dual playfield, PAL");
        m.put(168992, ", 1280x256, 11:44, 35ns, dual playfield, PAL");
        m.put(136196, ", 320x512, 44:22, 140ns, interlaced, dual playfield, PAL");
        m.put(168964, ", 640x512, 22:22, 70ns, interlaced, dual playfield, PAL");
        m.put(168996, ", 1280x512, 11:22, 35ns, interlaced, dual playfield, PAL");
        m.put(136256, ", 320x256, 44:44, 140ns, dual playfield, PAL");
        m.put(169024, ", 640x256, 22:44, 70ns, dual playfield, PAL");
        m.put(169056, ", 1280x256, 11:44, 35ns, dual playfield, PAL");
        m.put(136260, ", 320x512, 44:22, 140ns, interlaced, dual playfield, PAL");
        m.put(169028, ", 640x512, 22:22, 70ns, interlaced, dual playfield, PAL");
        m.put(169060, ", 1280x512, 11:22, 35ns, interlaced, dual playfield, PAL");
        m.put(169984, ", 640x256, 22:44, 70ns, HAM, PAL");
        m.put(170016, ", 1280x256, 11:44, 35ns, HAM, PAL");
        m.put(169988, ", 640x512, 22:22, 70ns, interlaced, HAM, PAL");
        m.put(170020, ", 1280x512, 11:22, 35ns, interlaced, HAM, PAL");
        m.put(168064, ", 640x256, 22:44, 70ns, PAL, EHB");
        m.put(168096, ", 1280x256, 11:44, 35ns, PAL, EHB");
        m.put(168068, ", 640x512, 22:22, 70ns, interlaced, PAL, EHB");
        m.put(168100, ", 1280x512, 11:22, 35ns, interlaced, PAL, EHB");
        m.put(561192, ", 800x150, 17:80, 35ns");
        m.put(563240, ", 800x150, 17:80, 35ns, HAM");
        m.put(561320, ", 800x150, 17:80, 35ns, EHB");
        m.put(561160, ", 400x150, 34:80, 70ns");
        m.put(563208, ", 400x150, 34:80, 70ns, HAM");
        m.put(561288, ", 400x150, 34:80, 70ns, EHB");
        m.put(528392, ", 200x150, 68:80, 140ns");
        m.put(530440, ", 200x150, 68:80, 140ns, HAM");
        m.put(528520, ", 200x150, 68:80, 140ns, EHB");
        m.put(561184, "SUPER72:SuperHighRes, 800x300, 17:40, 35ns");
        m.put(562208, ", 800x300, 17:40, 35ns, dual playfield");
        m.put(562272, ", 800x300, 17:40, 35ns, dual playfield");
        m.put(563232, ", 800x300, 17:40, 35ns, HAM");
        m.put(561312, ", 800x300, 17:40, 35ns, EHB");
        m.put(561152, "SUPER72:HighRes, 400x300, 34:40, 70ns");
        m.put(562176, ", 400x300, 34:40, 70ns, dual playfield");
        m.put(562240, ", 400x300, 34:40, 70ns, dual playfield");
        m.put(563200, ", 400x300, 34:40, 70ns, HAM");
        m.put(561280, ", 400x300, 34:40, 70ns, EHB");
        m.put(528384, "SUPER72:LowRes, 200x300, 68:40, 140ns");
        m.put(529408, ", 200x300, 68:40, 140ns, dual playfield");
        m.put(529472, ", 200x300, 68:40, 140ns, dual playfield");
        m.put(530432, ", 200x300, 68:40, 140ns, HAM");
        m.put(528512, ", 200x300, 68:40, 140ns, EHB");
        m.put(561188, "SUPER72:SuperHighRes Interlace, 800x600, 17:20, 35ns, interlaced");
        m.put(562212, ", 800x600, 17:20, 35ns, interlaced, dual playfield");
        m.put(562276, ", 800x600, 17:20, 35ns, interlaced, dual playfield");
        m.put(563236, ", 800x600, 17:20, 35ns, interlaced, HAM");
        m.put(561316, ", 800x600, 17:20, 35ns, interlaced, EHB");
        m.put(561156, "SUPER72:HighRes Interlace, 400x600, 34:20, 70ns, interlaced");
        m.put(562180, ", 400x600, 34:20, 70ns, interlaced, dual playfield");
        m.put(562244, ", 400x600, 34:20, 70ns, interlaced, dual playfield");
        m.put(563204, ", 400x600, 34:20, 70ns, interlaced, HAM");
        m.put(561284, ", 400x600, 34:20, 70ns, interlaced, EHB");
        m.put(528388, "SUPER72:LowRes Interlace, 200x600, 68:20, 140ns, interlaced");
        m.put(529412, ", 200x600, 68:20, 140ns, interlaced, dual playfield");
        m.put(529476, ", 200x600, 68:20, 140ns, interlaced, dual playfield");
        m.put(530436, ", 200x600, 68:20, 140ns, interlaced, HAM");
        m.put(528516, ", 200x600, 68:20, 140ns, interlaced, EHB");
        m.put(69632, "NTSC:LowRes, 320x200, 44:52, 140ns");
        m.put(102400, "NTSC:HighRes, 640x200, 22:52, 70ns");
        m.put(102432, "NTSC:SuperHighRes, 1280x200, 11:52, 35ns");
        m.put(71680, ", 320x200, 44:52, 140ns, HAM");
        m.put(69760, ", 320x200, 44:52, 140ns, EHB");
        m.put(69636, "NTSC:LowRes Interlace, 320x400, 44:26, 140ns, interlaced");
        m.put(102404, "NTSC:HighRes Interlace, 640x400, 22:26, 70ns, interlaced");
        m.put(102436, "NTSC:SuperHighRes Interlace, 1280x400, 11:26, 35ns, interlaced");
        m.put(71684, ", 320x400, 44:26, 140ns, interlaced, HAM");
        m.put(69764, ", 320x400, 44:26, 140ns, interlaced, EHB");
        m.put(70656, ", 320x200, 44:52, 140ns, dual playfield");
        m.put(103424, ", 640x200, 22:52, 70ns, dual playfield");
        m.put(103456, ", 1280x200, 11:52, 35ns, dual playfield");
        m.put(70660, ", 320x400, 44:26, 140ns, interlaced, dual playfield");
        m.put(103428, ", 640x400, 22:26, 70ns, interlaced, dual playfield");
        m.put(103460, ", 1280x400, 11:26, 35ns, interlaced, dual playfield");
        m.put(70720, ", 320x200, 44:52, 140ns, dual playfield");
        m.put(103488, ", 640x200, 22:52, 70ns, dual playfield");
        m.put(103520, ", 1280x200, 11:52, 35ns, dual playfield");
        m.put(70724, ", 320x400, 44:26, 140ns, interlaced, dual playfield");
        m.put(103492, ", 640x400, 22:26, 70ns, interlaced, dual playfield");
        m.put(103524, ", 1280x400, 11:26, 35ns, interlaced, dual playfield");
        m.put(104448, ", 640x200, 22:52, 70ns, HAM");
        m.put(104480, ", 1280x200, 11:52, 35ns, HAM");
        m.put(104452, ", 640x400, 22:26, 70ns, interlaced, HAM");
        m.put(104484, ", 1280x400, 11:26, 35ns, interlaced, HAM");
        m.put(102528, ", 640x200, 22:52, 70ns, EHB");
        m.put(102560, ", 1280x200, 11:52, 35ns, EHB");
        m.put(102532, ", 640x400, 22:26, 70ns, interlaced, EHB");
        m.put(102564, ", 1280x400, 11:26, 35ns, interlaced, EHB");
        m.put(233504, ", 640x240, 22:44, 35ns");
        m.put(235552, ", 640x240, 22:44, 35ns, HAM");
        m.put(233632, ", 640x240, 22:44, 35ns, EHB");
        m.put(233472, ", 320x240, 44:44, 70ns");
        m.put(235520, ", 320x240, 44:44, 70ns, HAM");
        m.put(233600, ", 320x240, 44:44, 70ns, EHB");
        m.put(200704, ", 160x240, 88:44, 140ns");
        m.put(202752, ", 160x240, 88:44, 140ns, HAM");
        m.put(200832, ", 160x240, 88:44, 140ns, EHB");
        m.put(233508, "MULTISCAN:Productivity, 640x480, 22:22, 35ns");
        m.put(234532, ", 640x480, 22:22, 35ns, dual playfield");
        m.put(234596, ", 640x480, 22:22, 35ns, dual playfield");
        m.put(235556, ", 640x480, 22:22, 35ns, HAM");
        m.put(233636, ", 640x480, 22:22, 35ns, EHB");
        m.put(233476, "MULTISCAN:LowRes, 320x480, 44:22, 70ns");
        m.put(234500, ", 320x480, 44:22, 70ns, dual playfield");
        m.put(234564, ", 320x480, 44:22, 70ns, dual playfield");
        m.put(235524, ", 320x480, 44:22, 70ns, HAM");
        m.put(233604, ", 320x480, 44:22, 70ns, EHB");
        m.put(200708, "MULTISCAN:ExtraLowRes, 160x480, 88:22, 140ns");
        m.put(201732, ", 160x480, 88:22, 140ns, dual playfield");
        m.put(201796, ", 160x480, 88:22, 140ns, dual playfield");
        m.put(202756, ", 160x480, 88:22, 140ns, HAM");
        m.put(200836, ", 160x480, 88:22, 140ns, EHB");
        m.put(233509, "MULTISCAN:Productivity Interl., 640x960, 22:11, 35ns, interlaced");
        m.put(234533, ", 640x960, 22:11, 35ns, interlaced, dual playfield");
        m.put(234597, ", 640x960, 22:11, 35ns, interlaced, dual playfield");
        m.put(235557, ", 640x960, 22:11, 35ns, interlaced, HAM");
        m.put(233637, ", 640x960, 22:11, 35ns, interlaced, EHB");
        m.put(233477, "MULTISCAN:LowRes Interlace, 320x960, 44:11, 70ns, interlaced");
        m.put(234501, ", 320x960, 44:11, 70ns, interlaced, dual playfield");
        m.put(234565, ", 320x960, 44:11, 70ns, interlaced, dual playfield");
        m.put(235525, ", 320x960, 44:11, 70ns, interlaced, HAM");
        m.put(233605, ", 320x960, 44:11, 70ns, interlaced, EHB");
        m.put(200709, "MULTISCAN:ExtraLowRes Interlace, 160x960, 88:11, 140ns, interlaced");
        m.put(201733, ", 160x960, 88:11, 140ns, interlaced, dual playfield");
        m.put(201797, ", 160x960, 88:11, 140ns, interlaced, dual playfield");
        m.put(202757, ", 160x960, 88:11, 140ns, interlaced, HAM");
        m.put(200837, ", 160x960, 88:11, 140ns, interlaced, EHB");
        m.put(430112, ", 640x200, 22:44, 35ns");
        m.put(432160, ", 640x200, 22:44, 35ns, HAM");
        m.put(430240, ", 640x200, 22:44, 35ns, EHB");
        m.put(430080, ", 320x200, 44:44, 70ns");
        m.put(432128, ", 320x200, 44:44, 70ns, HAM");
        m.put(430208, ", 320x200, 44:44, 70ns, EHB");
        m.put(397312, ", 160x200, 88:44, 140ns");
        m.put(399360, ", 160x200, 88:44, 140ns, HAM");
        m.put(397440, ", 160x200, 88:44, 140ns, EHB");
        m.put(430116, "EURO:72Hz Productivity, 640x400, 22:22, 35ns");
        m.put(431140, ", 640x400, 22:22, 35ns, dual playfield");
        m.put(431204, ", 640x400, 22:22, 35ns, dual playfield");
        m.put(432164, ", 640x400, 22:22, 35ns, HAM");
        m.put(430244, ", 640x400, 22:22, 35ns, EHB");
        m.put(430084, ", 320x400, 44:22, 70ns");
        m.put(431108, ", 320x400, 44:22, 70ns, dual playfield");
        m.put(431172, ", 320x400, 44:22, 70ns, dual playfield");
        m.put(432132, ", 320x400, 44:22, 70ns, HAM");
        m.put(430212, ", 320x400, 44:22, 70ns, EHB");
        m.put(397316, ", 160x400, 88:22, 140ns");
        m.put(398340, ", 160x400, 88:22, 140ns, dual playfield");
        m.put(398404, ", 160x400, 88:22, 140ns, dual playfield");
        m.put(399364, ", 160x400, 88:22, 140ns, HAM");
        m.put(397444, ", 160x400, 88:22, 140ns, EHB");
        m.put(430117, "EURO:72Hz Productivity Interl., 640x800, 22:11, 35ns, interlaced");
        m.put(431141, ", 640x800, 22:11, 35ns, interlaced, dual playfield");
        m.put(431205, ", 640x800, 22:11, 35ns, interlaced, dual playfield");
        m.put(432165, ", 640x800, 22:11, 35ns, interlaced, HAM");
        m.put(430245, ", 640x800, 22:11, 35ns, interlaced, EHB");
        m.put(430085, ", 320x800, 44:11, 70ns, interlaced");
        m.put(431109, ", 320x800, 44:11, 70ns, interlaced, dual playfield");
        m.put(431173, ", 320x800, 44:11, 70ns, interlaced, dual playfield");
        m.put(432133, ", 320x800, 44:11, 70ns, interlaced, HAM");
        m.put(430213, ", 320x800, 44:11, 70ns, interlaced, EHB");
        m.put(397317, ", 160x800, 88:11, 140ns, interlaced");
        m.put(398341, ", 160x800, 88:11, 140ns, interlaced, dual playfield");
        m.put(398405, ", 160x800, 88:11, 140ns, interlaced, dual playfield");
        m.put(399365, ", 160x800, 88:11, 140ns, interlaced, HAM");
        m.put(397445, ", 160x800, 88:11, 140ns, interlaced, EHB");
        m.put(495648, "EURO:36Hz SuperHighRes, 1280x200, 11:44, 35ns");
        m.put(496672, ", 1280x200, 11:44, 35ns, dual playfield");
        m.put(496736, ", 1280x200, 11:44, 35ns, dual playfield");
        m.put(497696, ", 1280x200, 11:44, 35ns, HAM");
        m.put(495776, ", 1280x200, 11:44, 35ns, EHB");
        m.put(495616, "EURO:36Hz HighRes, 640x200, 22:44, 70ns");
        m.put(496640, ", 640x200, 22:44, 70ns, dual playfield");
        m.put(496704, ", 640x200, 22:44, 70ns, dual playfield");
        m.put(497664, ", 640x200, 22:44, 70ns, HAM");
        m.put(495744, ", 640x200, 22:44, 70ns, EHB");
        m.put(462848, "EURO:36Hz LowRes, 320x200, 44:44, 140ns");
        m.put(463872, ", 320x200, 44:44, 140ns, dual playfield");
        m.put(463936, ", 320x200, 44:44, 140ns, dual playfield");
        m.put(464896, ", 320x200, 44:44, 140ns, HAM");
        m.put(462976, ", 320x200, 44:44, 140ns, EHB");
        m.put(495652, "EURO:36Hz SuperHighRes Interl., 1280x400, 11:22, 35ns, interlaced");
        m.put(496676, ", 1280x400, 11:22, 35ns, interlaced, dual playfield");
        m.put(496740, ", 1280x400, 11:22, 35ns, interlaced, dual playfield");
        m.put(497700, ", 1280x400, 11:22, 35ns, interlaced, HAM");
        m.put(495780, ", 1280x400, 11:22, 35ns, interlaced, EHB");
        m.put(495620, "EURO:36Hz HighRes Interlace, 640x400, 22:22, 70ns, interlaced");
        m.put(496644, ", 640x400, 22:22, 70ns, interlaced, dual playfield");
        m.put(496708, ", 640x400, 22:22, 70ns, interlaced, dual playfield");
        m.put(497668, ", 640x400, 22:22, 70ns, interlaced, HAM");
        m.put(495748, ", 640x400, 22:22, 70ns, interlaced, EHB");
        m.put(462852, "EURO:36Hz LowRes Interlace, 320x400, 44:22, 140ns, interlaced");
        m.put(463876, ", 320x400, 44:22, 140ns, interlaced, dual playfield");
        m.put(463940, ", 320x400, 44:22, 140ns, interlaced, dual playfield");
        m.put(464900, ", 320x400, 44:22, 140ns, interlaced, HAM");
        m.put(462980, ", 320x400, 44:22, 140ns, interlaced, EHB");
        m.put(692224, "DBLPAL:HighRes, 640x256, 22:44, 35ns, PAL");
        m.put(694272, ", 640x256, 22:44, 35ns, HAM, PAL");
        m.put(692352, ", 640x256, 22:44, 35ns, PAL, EHB");
        m.put(659456, "DBLPAL:LowRes, 320x256, 44:44, 70ns, PAL");
        m.put(661504, ", 320x256, 44:44, 70ns, HAM, PAL");
        m.put(659584, ", 320x256, 44:44, 70ns, PAL, EHB");
        m.put(659968, ", 160x256, 88:44, 140ns, PAL");
        m.put(662016, ", 160x256, 88:44, 140ns, HAM, PAL");
        m.put(660096, ", 160x256, 88:44, 140ns, PAL, EHB");
        m.put(692228, "DBLPAL:HighRes flicker-free, 640x512, 22:22, 35ns, PAL");
        m.put(693252, ", 640x512, 22:22, 35ns, dual playfield, PAL");
        m.put(693316, ", 640x512, 22:22, 35ns, dual playfield, PAL");
        m.put(694276, ", 640x512, 22:22, 35ns, HAM, PAL");
        m.put(692356, ", 640x512, 22:22, 35ns, PAL, EHB");
        m.put(659460, "DBLPAL:LowRes flicker-free, 320x512, 44:22, 70ns, PAL");
        m.put(660484, ", 320x512, 44:22, 70ns, dual playfield, PAL");
        m.put(660548, ", 320x512, 44:22, 70ns, dual playfield, PAL");
        m.put(661508, ", 320x512, 44:22, 70ns, HAM, PAL");
        m.put(659588, ", 320x512, 44:22, 70ns, PAL, EHB");
        m.put(659972, ", 160x512, 88:22, 140ns, PAL");
        m.put(660996, ", 160x512, 88:22, 140ns, dual playfield, PAL");
        m.put(661060, ", 160x512, 88:22, 140ns, dual playfield, PAL");
        m.put(662020, ", 160x512, 88:22, 140ns, HAM, PAL");
        m.put(660100, ", 160x512, 88:22, 140ns, PAL, EHB");
        m.put(692229, "DBLPAL:HighRes Interlace, 640x1024, 22:11, 35ns, interlaced, PAL");
        m.put(693253, ", 640x1024, 22:11, 35ns, interlaced, dual playfield, PAL");
        m.put(693317, ", 640x1024, 22:11, 35ns, interlaced, dual playfield, PAL");
        m.put(694277, ", 640x1024, 22:11, 35ns, interlaced, HAM, PAL");
        m.put(692357, ", 640x1024, 22:11, 35ns, interlaced, PAL, EHB");
        m.put(659461, "DBLPAL:LowRes Interlace, 320x1024, 44:11, 70ns, interlaced, PAL");
        m.put(660485, ", 320x1024, 44:11, 70ns, interlaced, dual playfield, PAL");
        m.put(660549, ", 320x1024, 44:11, 70ns, interlaced, dual playfield, PAL");
        m.put(661509, ", 320x1024, 44:11, 70ns, interlaced, HAM, PAL");
        m.put(659589, ", 320x1024, 44:11, 70ns, interlaced, PAL, EHB");
        m.put(659973, ", 160x1024, 88:11, 140ns, interlaced, PAL");
        m.put(660997, ", 160x1024, 88:11, 140ns, interlaced, dual playfield, PAL");
        m.put(661061, ", 160x1024, 88:11, 140ns, interlaced, dual playfield, PAL");
        m.put(662021, ", 160x1024, 88:11, 140ns, interlaced, HAM, PAL");
        m.put(660101, ", 160x1024, 88:11, 140ns, interlaced, PAL, EHB");
        m.put(660992, ", 160x512, 88:22, 140ns, dual playfield, PAL");
        m.put(661056, ", 160x512, 88:22, 140ns, dual playfield, PAL");
        m.put(660480, ", 320x512, 44:22, 70ns, dual playfield, PAL");
        m.put(660544, ", 320x512, 44:22, 70ns, dual playfield, PAL");
        m.put(693248, ", 640x512, 22:22, 35ns, dual playfield, PAL");
        m.put(693312, ", 640x512, 22:22, 35ns, dual playfield, PAL");
        m.put(626688, "DBLNTSC:HighRes, 640x200, 22:52, 35ns");
        m.put(628736, ", 640x200, 22:52, 35ns, HAM");
        m.put(626816, ", 640x200, 22:52, 35ns, EHB");
        m.put(593920, "DBLNTSC:LowRes, 320x200, 44:52, 70ns");
        m.put(595968, ", 320x200, 44:52, 70ns, HAM");
        m.put(594048, ", 320x200, 44:52, 70ns, EHB");
        m.put(594432, ", 160x200, 88:52, 140ns");
        m.put(596480, ", 160x200, 88:52, 140ns, HAM");
        m.put(594560, ", 160x200, 88:52, 140ns, EHB");
        m.put(626692, "DBLNTSC:HighRes flicker-free, 640x400, 22:26, 35ns");
        m.put(627716, ", 640x400, 22:26, 35ns, dual playfield");
        m.put(627780, ", 640x400, 22:26, 35ns, dual playfield");
        m.put(628740, ", 640x400, 22:26, 35ns, HAM");
        m.put(626820, ", 640x400, 22:26, 35ns, EHB");
        m.put(593924, "DBLNTSC:LowRes flicker-free, 320x400, 44:26, 70ns");
        m.put(594948, ", 320x400, 44:26, 70ns, dual playfield");
        m.put(595012, ", 320x400, 44:26, 70ns, dual playfield");
        m.put(595972, ", 320x400, 44:26, 70ns, HAM");
        m.put(594052, ", 320x400, 44:26, 70ns, EHB");
        m.put(594436, ", 160x400, 88:26, 140ns");
        m.put(595460, ", 160x400, 88:26, 140ns, dual playfield");
        m.put(595524, ", 160x400, 88:26, 140ns, dual playfield");
        m.put(596484, ", 160x400, 88:26, 140ns, HAM");
        m.put(594564, ", 160x400, 88:26, 140ns, EHB");
        m.put(626693, "DBLNTSC:HighRes Interlace, 640x800, 22:13, 35ns, interlaced");
        m.put(627717, ", 640x800, 22:13, 35ns, interlaced, dual playfield");
        m.put(627781, ", 640x800, 22:13, 35ns, interlaced, dual playfield");
        m.put(628741, ", 640x800, 22:13, 35ns, interlaced, HAM");
        m.put(626821, ", 640x800, 22:13, 35ns, interlaced, EHB");
        m.put(593925, "DBLNTSC:LowRes Interlace, 320x800, 44:13, 70ns, interlaced");
        m.put(594949, ", 320x800, 44:13, 70ns, interlaced, dual playfield");
        m.put(595013, ", 320x800, 44:13, 70ns, interlaced, dual playfield");
        m.put(595973, ", 320x800, 44:13, 70ns, interlaced, HAM");
        m.put(594053, ", 320x800, 44:13, 70ns, interlaced, EHB");
        m.put(594437, ", 160x800, 88:13, 140ns, interlaced");
        m.put(595461, ", 160x800, 88:13, 140ns, interlaced, dual playfield");
        m.put(595525, ", 160x800, 88:13, 140ns, interlaced, dual playfield");
        m.put(596485, ", 160x800, 88:13, 140ns, interlaced, HAM");
        m.put(594565, ", 160x800, 88:13, 140ns, interlaced, EHB");
        m.put(595456, ", 160x400, 88:26, 140ns, dual playfield");
        m.put(595520, ", 160x400, 88:26, 140ns, dual playfield");
        m.put(594944, ", 320x400, 44:26, 70ns, dual playfield");
        m.put(595008, ", 320x400, 44:26, 70ns, dual playfield");
        m.put(627712, ", 640x400, 22:26, 35ns, dual playfield");
        m.put(627776, ", 640x400, 22:26, 35ns, dual playfield");
        return m;
    }
}

/* @(#)SonyMakerNoteTagSet.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */
package org.monte.media.exif;

import org.monte.media.tiff.EnumValueFormatter;
import org.monte.media.tiff.TIFFTag;
import static org.monte.media.tiff.TIFFTag.LONG_MASK;
import static org.monte.media.tiff.TIFFTag.SHORT_MASK;
import static org.monte.media.tiff.TIFFTag.SRATIONAL_MASK;
import static org.monte.media.tiff.TIFFTag.UNDEFINED_MASK;
import org.monte.media.tiff.TagSet;

/**
 * Enumeration of Sony MakerNote tags.
 * <p>
 * Sources:
 * <p>
 * http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/Sony.html
 * <p>
 * Exiv2 Image metadata library and tools.
 * Copyright © 2004 - 2014 Andreas Huggel
 * <a href="http://www.exiv2.org/tags-sony.html">http://www.exiv2.org/tags-sony.html</a>
 * 
 * @author Werner Randelshofer
 * @version 1.0 2010-07-24 Created.
 */
public class SonyMakerNoteTagSet extends TagSet {

    private static SonyMakerNoteTagSet instance;

    private SonyMakerNoteTagSet(TIFFTag[] tags) {
        super("SonyMakerNote", tags);
    }

    /** Returns a shared instance of a BaselineTIFFTagSet. */
    public static SonyMakerNoteTagSet getInstance() {
        if (instance == null) {
            TIFFTag[] tags = {//
                new TIFFTag("Quality", 0x0102, LONG_MASK, new EnumValueFormatter(//
                "RAW", 0,//
                "Super Fine", 1,//
                "Fine", 2,//
                "Standard", 3,//
                "Economy", 4,//
                "Extra Fine", 5,//
                "RAW + JPEG", 6,//
                "Compressed RAW", 7,//
                "Compressed RAW + JPEG", 8//
                )),
                new TIFFTag("FlashExposureComp", 0x0104, SRATIONAL_MASK),
                new TIFFTag("Teleconverter", 0x0105, LONG_MASK, new EnumValueFormatter(//
                "None ", 0x0,//
                "Minolta AF 2x APO (D)", 0x48,//
                "Minolta AF 2x APO II", 0x50,//
                "Minolta AF 1.4x APO (D)", 0x88,//
                "Minolta AF 1.4x APO II", 0x90//
                )),
                new TIFFTag("WhiteBalanceFineTune", 0x0112, LONG_MASK),
                new TIFFTag("CameraSettings", 0x0114, LONG_MASK),
                new TIFFTag("WhiteBalance", 0x0115, LONG_MASK, new EnumValueFormatter(//
                "Auto ", 0x0,//
                "Color Temperature/Color Filter ", 0x1,//
                "Daylight ", 0x10,//
                "Cloudy ", 0x20,//
                "Shade ", 0x30,//
                "Tungsten ", 0x40,//
                "Flash ", 0x50,//
                "Fluorescent ", 0x60,//
                "Custom", 0x70//
                )),
                new TIFFTag("PrintIM", 0x0e00, SHORT_MASK),
                new TIFFTag("Panorama", 0x1003,LONG_MASK),
                new TIFFTag("PreviewImage", 0x2001, UNDEFINED_MASK),
                new TIFFTag("AutoHDR", 0x200a, SHORT_MASK, new EnumValueFormatter(//
                "Off ", 0x0,//
                "On", 0x10001//
                )),
                new TIFFTag("ShotInfo", 0x3000, SHORT_MASK),
                new TIFFTag("FileFormat", 0xb000, SHORT_MASK, new EnumValueFormatter(//
                "JPEG", 2,//
                "SR2 ", 1000,//
                "ARW 1.0 ", 2000,//
                "ARW 2.0 ", 3000,//
                "ARW 2.1", 3100//
                )),
                new TIFFTag("SonyModelID", 0xb001, SHORT_MASK, new EnumValueFormatter(//
                "DSC-R1", 2,//
                "DSLR-A100", 256,//
                "DSLR-A900", 257,//
                "DSLR-A700", 258,//
                "DSLR-A200", 259,//
                "DSLR-A350", 260,//
                "DSLR-A300", 261,//
                "DSLR-A380", 263,//
                "DSLR-A330", 264,//
                "DSLR-A230", 265,//
                "DSLR-A850", 269,//
                "DSLR-A550", 273,//
                "DSLR-A500", 274//
                )),
                new TIFFTag("ColorReproduction", 0xb020, SHORT_MASK),
                new TIFFTag("ColorTemperature", 0xb021, SHORT_MASK),
                new TIFFTag("ColorCompensationFilter", 0xb022, SHORT_MASK),
                new TIFFTag("SceneMode", 0xb023, SHORT_MASK, new EnumValueFormatter(//
                "Standard", 0,//
                "Portrait", 1,//
                "Text", 2,//
                "Night Scene", 3,//
                "Sunset", 4,//
                "Sports", 5,//
                "Landscape", 6,//
                "Night Portrait", 7,//
                "Macro", 8,//
                "Super Macro", 9,//
                "Auto", 16,//
                "Night View/Portrait", 17//
                )),
                new TIFFTag("ZoneMatching", 0xb024, SHORT_MASK, new EnumValueFormatter(//
                "ISO Setting Used", 0,//
                "High Key", 1,//
                "Low Key", 2//
                )),
                new TIFFTag("DynamicRangeOptimizer", 0xb025, SHORT_MASK, new EnumValueFormatter(//
                "Off", 0,//
                "Standard", 1,//
                "Advanced Auto", 2,//
                "Auto", 3,//
                "Advanced Lv1", 8,//
                "Advanced Lv2", 9,//
                "Advanced Lv3", 10,//
                "Advanced Lv4", 11,//
                "Advanced Lv5", 12//
                )),
                new TIFFTag("ImageStabilization", 0xb026, SHORT_MASK, new EnumValueFormatter(//
                "Off ", 0x0,//
                "On", 0x1//
                )),
                new TIFFTag("LensType", 0xb027, SHORT_MASK, new EnumValueFormatter(//
                "Minolta AF 28-85mm F3.5-4.5 New", 0,//
                "Minolta AF 80-200mm F2.8 HS-APO G ", 1,//
                "Minolta AF 28-70mm F2.8 G ", 2,//
                "Minolta AF 28-80mm F4-5.6 ", 3,//
                "Minolta AF 35-70mm F3.5-4.5 [II] ", 5,//
                "Minolta AF 24-85mm F3.5-4.5 [New] ", 6,//
                "Minolta AF 100-300mm F4.5-5.6 APO [New] or 100-400mm or Sigma Lens ", 7,//
                "Minolta AF 100-400mm F4.5-6.7 APO", 7,//
                "Sigma AF 100-300mm F4 EX DG IF", 7,//
                "Minolta AF 70-210mm F4.5-5.6 [II] ", 8,//
                "Minolta AF 50mm F3.5 Macro ", 9,//
                "Minolta AF 28-105mm F3.5-4.5 [New] ", 10,//
                "Minolta AF 300mm F4 HS-APO G ", 11,//
                "Minolta AF 100mm F2.8 Soft Focus ", 12,//
                "Minolta AF 75-300mm F4.5-5.6 (New or II) ", 13,//
                "Minolta AF 100-400mm F4.5-6.7 APO ", 14,//
                "Minolta AF 400mm F4.5 HS-APO G ", 15,//
                "Minolta AF 17-35mm F3.5 G ", 16,//
                "Minolta AF 20-35mm F3.5-4.5 ", 17,//
                "Minolta AF 28-80mm F3.5-5.6 II ", 18,//
                "Minolta AF 35mm F1.4 G ", 19,//
                "Minolta/Sony 135mm F2.8 [T4.5] STF ", 20,//
                "Minolta AF 35-80mm F4-5.6 II ", 22,//
                "Minolta AF 200mm F4 Macro APO G ", 23,//
                "Minolta/Sony AF 24-105mm F3.5-4.5 (D) or Sigma or Tamron Lens ", 24,//
                "Sigma 18-50mm F2.8", 24,//
                "Sigma 17-70mm F2.8-4.5 (D)", 24,//
                "Sigma 20-40mm F2.8 EX DG Aspherical IF", 24,//
                "Sigma 18-200mm F3.5-6.3 DC", 24,//
                "Sigma 20-40mm F2.8 EX DG Aspherical IF", 24,//
                "Tamron SP AF 28-75mm F2.8 XR Di (IF) Macro", 24,//
                "Minolta AF 100-300mm F4.5-5.6 APO (D) or Sigma Lens ", 25,//
                "Sigma 100-300mm F4 EX (APO (D) or D IF)", 25,//
                "Sigma 70mm F2.8 EX DG Macro", 25,//
                "Sigma 20mm F1.8 EX DG Aspherical RF", 25,//
                "Sigma 30mm F1.4 DG EX", 25,//
                "Minolta AF 85mm F1.4 G (D) ", 27,//
                "Minolta/Sony AF 100mm F2.8 Macro (D) or Tamron Lens ", 28,//
                "Tamron SP AF 90mm F2.8 Di Macro", 28,//
                "Minolta/Sony AF 75-300mm F4.5-5.6 (D) ", 29,//
                "Minolta AF 28-80mm F3.5-5.6 (D) or Sigma Lens ", 30,//
                "Sigma AF 10-20mm F4-5.6 EX DC", 30,//
                "Sigma AF 12-24mm F4.5-5.6 EX DG", 30,//
                "Sigma 28-70mm EX DG F2.8", 30,//
                "Sigma 55-200mm F4-5.6 DC", 30,//
                "Minolta/Sony AF 50mm F2.8 Macro (D) or F3.5 ", 31,//
                "Minolta/Sony AF 50mm F3.5 Macro", 31,//
                "Minolta/Sony AF 300mm F2.8 G or 1.5x Teleconverter ", 32,//
                "Minolta/Sony AF 70-200mm F2.8 G ", 33,//
                "Minolta AF 85mm F1.4 G (D) Limited ", 35,//
                "Minolta AF 28-100mm F3.5-5.6 (D) ", 36,//
                "Minolta AF 17-35mm F2.8-4 (D) ", 38,//
                "Minolta AF 28-75mm F2.8 (D) ", 39,//
                "Minolta/Sony AF DT 18-70mm F3.5-5.6 (D) or 18-200m F3.5-6.3 ", 40,//
                "Sony AF DT 18-200mm F3.5-6.3", 40,//
                "Minolta/Sony AF DT 11-18mm F4.5-5.6 (D) or Tamron Lens ", 41,//
                "Tamron SP AF 11-18mm F4.5-5.6 Di II LD Aspherical IF", 41,//
                "Minolta/Sony AF DT 18-200mm F3.5-6.3 (D) ", 42,//
                "Sony 35mm F1.4 G (SAL-35F14G) ", 43,//
                "Sony 50mm F1.4 (SAL-50F14) ", 44,//
                "Carl Zeiss Planar T* 85mm F1.4 ZA ", 45,//
                "Carl Zeiss Vario-Sonnar T* DT 16-80mm F3.5-4.5 ZA ", 46,//
                "Carl Zeiss Sonnar T* 135mm F1.8 ZA ", 47,//
                "Carl Zeiss Vario-Sonnar T* 24-70mm F2.8 ZA SSM (SAL-2470Z) ", 48,//
                "Sony AF DT 55-200mm F4-5.6 ", 49,//
                "Sony AF DT 18-250mm F3.5-6.3 ", 50,//
                "Sony AF DT 16-105mm F3.5-5.6 or 55-200mm F4-5.5 ", 51,//
                "Sony AF DT 55-200mm F4-5.5", 51,//
                "Sony 70-300mm F4.5-5.6 G SSM ", 52,//
                "Sony AF 70-400mm F4.5-5.6 G SSM (SAL-70400G) ", 53,//
                "Carl Zeiss Vario-Sonnar T* 16-35mm F2.8 ZA SSM (SAL-1635Z) ", 54,//
                "Sony DT 18-55mm F3.5-5.6 SAM (SAL-1855) ", 55,//
                "Sony AF DT 55-200mm F4-5.6 SAM ", 56,//
                "Sony AF DT 50mm F1.8 SAM ", 57,//
                "Sony AF DT 30mm F2.8 SAM Macro ", 58,//
                "Tamron or Sigma Lens (128) ", 128,//
                "Tamron 18-200mm F3.5-6.3", 128,//
                "Tamron 28-300mm F3.5-6.3", 128,//
                "Tamron 80-300mm F3.5-6.3", 128,//
                "Tamron AF 28-200mm F3.8-5.6 XR Di Aspherical [IF] MACRO", 128,//
                "Tamron SP AF 17-35mm F2.8-4 Di LD Aspherical IF", 128,//
                "Sigma AF 50-150mm F2.8 EX DC APO HSM II", 128,//
                "Sigma 10-20mm F3.5 EX DC HSM", 128,//
                "Sigma 70-200mm F2.8 II EX DG APO MACRO HSM", 128,//
                "Tamron Lens (129) ", 129,//
                "Tamron 200-400mm F5.6 LD", 129,//
                "Tamron 70-300mm F4-5.6 LD", 129,//
                "Tamron 20-40mm F2.7-3.5 SP Aspherical IF ", 131,//
                "Vivitar 28-210mm F3.5-5.6 ", 135,//
                "Tokina EMZ M100 AF 100mm F3.5 ", 136,//
                "Cosina 70-210mm F2.8-4 AF ", 137,//
                "Soligor 19-35mm F3.5-4.5 ", 138,//
                "Voigtlander 70-300mm F4.5-5.6 ", 142,//
                "Voigtlander Macro APO-Lanthar 125mm F2.5 SL ", 146,//
                "Tamron Lens (255) ", 255,//
                "Tamron SP AF 17-50mm F2.8 XR Di II LD Aspherical", 255,//
                "Tamron AF 18-250mm F3.5-6.3 XR Di II LD", 255,//
                "Tamron AF 55-200mm F4-5.6 Di II", 255,//
                "Tamron AF 70-300mm F4-5.6 Di LD MACRO 1:2", 255,//
                "Tamron SP AF 200-500mm F5.0-6.3 Di LD IF", 255,//
                "Tamron SP AF 10-24mm F3.5-4.5 Di II LD Aspherical IF", 255,//
                "Tamron SP AF 70-200mm F2.8 Di LD IF Macro", 255,//
                "Tamron SP AF 28-75mm F2.8 XR Di LD Aspherical IF", 255,//
                "Minolta AF 50mm F1.7 ", 2550,//
                "Minolta AF 35-70mm F4 or Other Lens ", 2551,//
                "Sigma UC AF 28-70mm F3.5-4.5", 2551,//
                "Sigma AF 28-70mm F2.8", 2551,//
                "Sigma M-AF 70-200mm F2.8 EX Aspherical", 2551,//
                "Quantaray M-AF 35-80mm F4-5.6", 2551,//
                "Minolta AF 28-85mm F3.5-4.5 or Other Lens ", 2552,//
                "Tokina 19-35mm F3.5-4.5", 2552,//
                "Tokina 28-70mm F2.8 AT-X", 2552,//
                "Tokina 80-400mm F4.5-5.6 AT-X AF II 840", 2552,//
                "Tokina AF PRO 28-80mm F2.8 AT-X 280", 2552,//
                "Tokina AT-X PRO II AF 28-70mm F2.6-2.8 270", 2552,//
                "Tamron AF 19-35mm F3.5-4.5", 2552,//
                "Angenieux AF 28-70mm F2.6", 2552,//F2.6
                "Minolta AF 28-135mm F4-4.5 or Sigma Lens ", 2553,//
                "Sigma ZOOM-alpha 35-135mm F3.5-4.5", 2553,//
                "Sigma 28-105mm F2.8-4 Aspherical", 2553,//
                "Minolta AF 35-105mm F3.5-4.5", 2554,//
                "Minolta AF 70-210mm F4 Macro or Sigma Lens ", 2555,//
                "Sigma 70-210mm F4-5.6 APO", 2555,//
                "Sigma M-AF 70-200mm F2.8 EX APO", 2555,//
                "Sigma 75-200mm F2.8-3.5", 2555,//
                "Minolta AF 135mm F2.8 ", 2556,//
                "Minolta/Sony AF 28mm F2.8 ", 2557,//
                "Minolta AF 24-50mm F4 ", 2558,//
                "Minolta AF 100-200mm F4.5 ", 2560,//
                "Minolta AF 75-300mm F4.5-5.6 or Sigma Lens ", 2561,//
                "Sigma 70-300mm F4-5.6 DL Macro", 2561,//
                "Sigma 300mm F4 APO Macro", 2561,//
                "Sigma AF 500mm F4.5 APO", 2561,//
                "Sigma AF 170-500mm F5-6.3 APO Aspherical", 2561,//
                "Tokina AT-X AF 300mm F4", 2561,//
                "Tokina AT-X AF 400mm F5.6 SD", 2561,//
                "Tokina AF 730 II 75-300mm F4.5-5.6", 2561,//
                "Minolta AF 50mm F1.4 [New] ", 2562,//
                "Minolta AF 300mm F2.8 APO or Sigma Lens ", 2563,//
                "Sigma AF 50-500mm F4-6.3 EX DG APO", 2563,//
                "Sigma AF 170-500mm F5-6.3 APO Aspherical", 2563,//
                "Sigma AF 500mm F4.5 EX DG APO", 2563,//
                "Sigma 400mm F5.6 APO", 2563,//
                "Minolta AF 50mm F2.8 Macro or Sigma Lens ", 2564,//
                "Sigma 50mm F2.8 EX Macro", 2564,//
                "Minolta AF 600mm F4 ", 2565,//
                "Minolta AF 24mm F2.8 ", 2566,//
                "Minolta/Sony AF 500mm F8 Reflex ", 2572,//
                "Minolta/Sony AF 16mm F2.8 Fisheye or Sigma Lens ", 2578,//
                "Sigma 8mm F4 EX [DG] Fisheye", 2578,//
                "Sigma 14mm F3.5", 2578,//
                "Sigma 15mm F2.8 Fisheye", 2578,//
                "Minolta/Sony AF 20mm F2.8 ", 2579,//
                "Minolta AF 100mm F2.8 Macro [New] or Sigma or Tamron Lens ", 2581,//
                "Sigma AF 90mm F2.8 Macro", 2581,//
                "Sigma AF 105mm F2.8 EX [DG] Macro", 2581,//
                "Sigma 180mm F5.6 Macro", 2581,//
                "Tamron 90mm F2.8 Macro", 2581,//
                "Minolta AF 35-105mm F3.5-4.5 New or Tamron Lens ", 2585,//
                "Beroflex 35-135mm F3.5-4.5", 2585,//
                "Tamron 24-135mm F3.5-5.6", 2585,//
                "Minolta AF 70-210mm F3.5-4.5", 2588,//
                "Minolta AF 80-200mm F2.8 APO or Tokina Lens ", 2589,//
                "Tokina 80-200mm F2.8", 2589,//
                "Minolta AF 35mm F1.4 ", 2591,//
                "Minolta AF 85mm F1.4 G (D) ", 2592,//
                "Minolta AF 200mm F2.8 G APO", 2593,//
                "Minolta AF 3x-1x F1.7-2.8 Macro ", 2594,//
                "Minolta AF 28mm F2 ", 2596,//
                "Minolta AF 35mm F2 [New] ", 2597,//
                "Minolta AF 100mm F2 ", 2598,//
                "Minolta AF 80-200mm F4.5-5.6", 2604,//
                "Minolta AF 35-80mm F4-5.6 ", 2605,//
                "Minolta AF 100-300mm F4.5-5.6 ", 2606,//
                "Minolta AF 35-80mm F4-5.6 ", 2607,//
                "Minolta AF 300mm F2.8 HS-APO G ", 2608,//
                "Minolta AF 600mm F4 HS-APO G ", 2609,//
                "Minolta AF 200mm F2.8 HS-APO G ", 2612,//
                "Minolta AF 50mm F1.7 New ", 2613,//
                "Minolta AF 28-105mm F3.5-4.5 xi ", 2615,//
                "Minolta AF 35-200mm F4.5-5.6 xi ", 2616,//
                "Minolta AF 28-80mm F4-5.6 xi ", 2618,//
                "Minolta AF 80-200mm F4.5-5.6 xi ", 2619,//
                "Minolta AF 28-70mm F2.8 G ", 2620,//
                "Minolta AF 100-300mm F4.5-5.6 xi ", 2621,//
                "Minolta AF 35-80mm F4-5.6 Power Zoom ", 2624,//
                "Minolta AF 80-200mm F2.8 G ", 2628,//
                "Minolta AF 85mm F1.4 New ", 2629,//
                "Minolta/Sony AF 100-300mm F4.5-5.6 APO ", 2631,//
                "Minolta AF 24-50mm F4 New ", 2632,//
                "Minolta AF 50mm F2.8 Macro New ", 2638,//
                "Minolta AF 100mm F2.8 Macro ", 2639,//
                "Minolta/Sony AF 20mm F2.8 New ", 2641,//
                "Minolta AF 24mm F2.8 New ", 2642,//
                "Minolta AF 100-400mm F4.5-6.7 APO ", 2644,//
                "Minolta AF 50mm F1.4 New ", 2662,//
                "Minolta AF 35mm F2 New ", 2667,//
                "Minolta AF 28mm F2 New ", 2668,//
                "Minolta AF 24-105mm F3.5-4.5 (D) ", 2672,//
                "Tokina 70-210mm F4-5.6 ", 4567,//
                "2x Teleconverter or Tamron or Tokina Lens ", 4574,//
                "Tamron SP AF 90mm F2.5", 4574,//
                "Tokina RF 500mm F8.0 x2", 4574,//
                "Tokina 300mm F2.8 x2", 4574,//
                "1.4x Teleconverter ", 4575,//
                "Tamron SP AF 300mm F2.8 LD IF ", 4585,//
                "T-Mount or Other Lens or no lens ", 6553,//
                "Arax MC 35mm F2.8 Tilt+Shift", 6553,//
                "Arax MC 80mm F2.8 Tilt+Shift", 6553,//
                "Zenitar MF 16mm F2.8 Fisheye M42", 6553,//
                "Samyang 500mm Mirror F8.0", 6553,//
                "Pentacon Auto 135mm F2.8", 6553,//
                "Pentacon Auto 29mm F2.8", 6553,//
                "Helios 44-2 58mm F2.0", 6553,//
                "Minolta AF 50mm F1.7 ", 25501,//
                "Minolta AF 35-70mm F4 or Other Lens ", 25511,//
                "Sigma UC AF 28-70mm F3.5-4.5", 25511,//
                "Sigma AF 28-70mm F2.8", 25511,//
                "Sigma M-AF 70-200mm F2.8 EX Aspherical", 25511,//
                "Quantaray M-AF 35-80mm F4-5.6", 25511,//
                "Minolta AF 28-85mm F3.5-4.5 or Other Lens ", 25521,//
                "Tokina 19-35mm F3.5-4.5", 25521,//
                "Tokina 28-70mm F2.8 AT-X", 25521,//
                "Tokina 80-400mm F4.5-5.6 AT-X AF II 840", 25521,//
                "Tokina AF PRO 28-80mm F2.8 AT-X 280", 25521,//
                "Tokina AT-X PRO II AF 28-70mm F2.6-2.8 270", 25521,//
                "Tamron AF 19-35mm F3.5-4.5", 25521,//
                "Angenieux AF 28-70mm F2.6", 25521,//
                "Minolta AF 28-135mm F4-4.5 or Sigma Lens ", 25531,//
                "Sigma ZOOM-alpha 35-135mm F3.5-4.5", 25531,//
                "Sigma 28-105mm F2.8-4 Aspherical", 25531,//
                "Minolta AF 35-105mm F3.5-4.5 ", 25541,//
                "Minolta AF 70-210mm F4 Macro or Sigma Lens ", 25551,//
                "Sigma 70-210mm F4-5.6 APO", 25551,//
                "Sigma M-AF 70-200mm F2.8 EX APO", 25551,//
                "Sigma 75-200mm F2.8-3.5", 25551,//
                "Minolta AF 135mm F2.8 ", 25561,//
                "Minolta/Sony AF 28mm F2.8 ", 25571,//
                "Minolta AF 24-50mm F4 ", 25581,//
                "Minolta AF 100-200mm F4.5 ", 25601,//
                "Minolta AF 75-300mm F4.5-5.6 or Sigma Lens ", 25611,//
                "Sigma 70-300mm F4-5.6 DL Macro", 25611,//
                "Sigma 300mm F4 APO Macro", 25611,//
                "Sigma AF 500mm F4.5 APO", 25611,//
                "Sigma AF 170-500mm F5-6.3 APO Aspherical", 25611,//
                "Tokina AT-X AF 300mm F4", 25611,//
                "Tokina AT-X AF 400mm F5.6 SD", 25611,//
                "Tokina AF 730 II 75-300mm F4.5-5.6", 25611,//
                "Minolta AF 50mm F1.4 [New] ", 25621,//
                "Minolta AF 300mm F2.8 APO or Sigma Lens ", 25631,//
                "Sigma AF 50-500mm F4-6.3 EX DG APO", 25631,//
                "Sigma AF 170-500mm F5-6.3 APO Aspherical", 25631,//
                "Sigma AF 500mm F4.5 EX DG APO", 25631,//
                "Sigma 400mm F5.6 APO", 25631,//
                "Minolta AF 50mm F2.8 Macro or Sigma Lens ", 25641,//
                "Sigma 50mm F2.8 EX Macro", 25641,//
                "Minolta AF 600mm F4 ", 25651,//
                "Minolta AF 24mm F2.8 ", 25661,//
                "Minolta/Sony AF 500mm F8 Reflex ", 25721,//
                "Minolta/Sony AF 16mm F2.8 Fisheye or Sigma Lens ", 25781,//
                "Sigma 8mm F4 EX [DG] Fisheye", 25781,//
                "Sigma 14mm F3.5", 25781,//
                "Sigma 15mm F2.8 Fisheye", 25781,//
                "Minolta/Sony AF 20mm F2.8 ", 25791,//
                "Minolta AF 100mm F2.8 Macro [New] or Sigma or Tamron Lens", 25811,//
                "Sigma AF 90mm F2.8 Macro", 25811,//
                "Sigma AF 105mm F2.8 EX [DG] Macro", 25811,//
                "Sigma 180mm F5.6 Macro", 25811,//
                "Tamron 90mm F2.8 Macro", 25811,//
                "Beroflex 35-135mm F3.5-4.5 ", 25851,//
                "Minolta AF 35-105mm F3.5-4.5 New or Tamron Lens ", 25858,//
                "Tamron 24-135mm F3.5-5.6", 25858,//
                "Minolta AF 70-210mm F3.5-4.5 ", 25881,//
                "Minolta AF 80-200mm F2.8 APO or Tokina Lens ", 25891,//
                "Tokina 80-200mm F2.8", 25891,//
                "Minolta AF 35mm F1.4 ", 25911,//
                "Minolta AF 85mm F1.4 G (D) ", 25921,//
                "Minolta AF 200mm F2.8 G APO ", 25931,//
                "Minolta AF 3x-1x F1.7-2.8 Macro ", 25941,//
                "Minolta AF 28mm F2 ", 25961,//
                "Minolta AF 35mm F2 [New] ", 25971,//
                "Minolta AF 100mm F2 ", 25981,//
                "Minolta AF 80-200mm F4.5-5.6 ", 26041,//
                "Minolta AF 35-80mm F4-5.6 ", 26051,//
                "Minolta AF 100-300mm F4.5-5.6 ", 26061,//
                "Minolta AF 35-80mm F4-5.6 ", 26071,//
                "Minolta AF 300mm F2.8 HS-APO G ", 26081,//
                "Minolta AF 600mm F4 HS-APO G ", 26091,//
                "Minolta AF 200mm F2.8 HS-APO G ", 26121,//
                "Minolta AF 50mm F1.7 New ", 26131,//
                "Minolta AF 28-105mm F3.5-4.5 xi ", 26151,//
                "Minolta AF 35-200mm F4.5-5.6 xi ", 26161,//
                "Minolta AF 28-80mm F4-5.6 xi ", 26181,//
                "Minolta AF 80-200mm F4.5-5.6 xi ", 26191,//
                "Minolta AF 28-70mm F2.8 G ", 26201,//
                "Minolta AF 100-300mm F4.5-5.6 xi ", 26211,//
                "Minolta AF 35-80mm F4-5.6 Power Zoom ", 26241,//
                "Minolta AF 80-200mm F2.8 G ", 26281,//
                "Minolta AF 85mm F1.4 New ", 26291,//
                "Minolta/Sony AF 100-300mm F4.5-5.6 APO ", 26311,//
                "Minolta AF 24-50mm F4 New ", 26321,//
                "Minolta AF 50mm F2.8 Macro New ", 26381,//
                "Minolta AF 100mm F2.8 Macro ", 26391,//
                "Minolta/Sony AF 20mm F2.8 New ", 26411,//
                "Minolta AF 24mm F2.8 New ", 26421,//
                "Minolta AF 100-400mm F4.5-6.7 APO ", 26441,//
                "Minolta AF 50mm F1.4 New ", 26621,//
                "Minolta AF 35mm F2 New ", 26671,//
                "Minolta AF 28mm F2 New ", 26681,//
                "Minolta AF 24-105mm F3.5-4.5 (D) ", 26721,//
                "Tokina 70-210mm F4-5.6 ", 45671,//
                "2x Teleconverter or Tamron or Tokina Lens ", 45741,//
                "Tamron SP AF 90mm F2.5", 45741,//
                "Tokina RF 500mm F8.0 x2", 45741,//
                "Tokina 300mm F2.8 x2", 45741,//
                "1.4x Teleconverter ", 45751,//
                "Tamron SP AF 300mm F2.8 LD IF ", 45851,//
                "T-Mount or Other Lens or no lens ", 65535,//
                "Arax MC 35mm F2.8 Tilt+Shift", 65535,//
                "Arax MC 80mm F2.8 Tilt+Shift", 65535,//
                "Zenitar MF 16mm F2.8 Fisheye M42", 65535,//
                "Samyang 500mm Mirror F8.0", 65535,//
                "Pentacon Auto 135mm F2.8", 65535,//
                "Pentacon Auto 29mm F2.8", 65535,//
                "Helios 44-2 58mm F2.0", 65535//
                )),
                new TIFFTag("MinoltaMakerNote", 0xb028, SHORT_MASK),
                new TIFFTag("ColorMode", 0xb029, SHORT_MASK, new EnumValueFormatter(//
                "Standard", 0,//
                "Vivid", 1,//
                "Portrait", 2,//
                "Landscape", 3,//
                "Sunset", 4,//
                "Night View/Portrait", 5,//
                "B&W", 6,//
                "Adobe RGB", 7,//
                "Neutral", 12,//
                "Neutral", 100,//
                "Clear", 101,//
                "Deep", 102,//
                "Light", 103,//
                "Night View", 104,//
                "Autumn Leaves", 105//
                )),
                new TIFFTag("FullImageSize", 0xb02b, SHORT_MASK),
                new TIFFTag("PreviewImageSize", 0xb02c, SHORT_MASK),
                new TIFFTag("Macro", 0xb040, SHORT_MASK, new EnumValueFormatter(//
                "Off ", 0x0,//
                "On", 0x1//
                )),
                new TIFFTag("ExposureMode", 0xb041, SHORT_MASK, new EnumValueFormatter(//
                "Auto", 0,//
                "Portrait", 1,//
                "Beach", 2,//
                "Snow", 4,//
                "Landscape", 5,//
                "Program", 6,//
                "Aperture Priority", 7,//
                "Shutter Priority", 8,//
                "Night Scene / Twilight", 9,//
                "Hi-Speed Shutter", 10,//
                "Twilight Portrait", 11,//
                "Soft Snap", 12,//
                "Fireworks", 13,//
                "Manual", 15,//
                "High Sensitivity", 18,//
                "Underwater", 29,//
                "Gourmet", 33,//
                "Panorama", 34,//
                "Handheld Twilight", 35,//
                "Anti Motion Blur", 36,//
                "Pet", 37,//
                "Backlight Correction HDR", 38//
                )),
                new TIFFTag("Quality2", 0xb047, SHORT_MASK, new EnumValueFormatter(//
                "Normal ", 0x0,//
                "Fine", 0x1//
                )),
                new TIFFTag("AntiBlur", 0xb04b, SHORT_MASK, new EnumValueFormatter(//
                "Off", 0,//
                "On (Continuous)", 1,//
                "On (Shooting)", 2,//
                "n/a", 65535//
                )),
                new TIFFTag("LongExposureNoiseReduction", 0xb04e, SHORT_MASK, new EnumValueFormatter(//
                "Off ", 0x0,//
                "On", 0x1//
                )),
                new TIFFTag("DynamicRangeOptimizer2", 0xb04f, SHORT_MASK, new EnumValueFormatter(//
                "Off ", 0x0,//
                "Standard", 0x1,//
                "Plus", 0x2//
                )),
                new TIFFTag("IntelligentAuto", 0xb052, SHORT_MASK, new EnumValueFormatter(//
                "Off ", 0x0,//
                "On", 0x1,//
                "Advanced", 0x2//
                )),
                new TIFFTag("WhiteBalance2", 0xb054, SHORT_MASK, new EnumValueFormatter(//
                "Auto ", 0x0,//
                "Manual", 0x4,//
                "Daylight", 0x5,//
                "Incandescent", 14 //
                ))//
            };
            instance = new SonyMakerNoteTagSet(tags);

        }
        return instance;
    }
}

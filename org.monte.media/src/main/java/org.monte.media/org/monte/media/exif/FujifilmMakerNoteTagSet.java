/*
 * @(#)FujifilmMakerNoteTagSet.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.exif;

import org.monte.media.tiff.ASCIIValueFormatter;
import org.monte.media.tiff.EnumValueFormatter;
import org.monte.media.tiff.TIFFTag;
import org.monte.media.tiff.TagSet;

import static org.monte.media.tiff.TIFFTag.ASCII_MASK;
import static org.monte.media.tiff.TIFFTag.LONG_MASK;
import static org.monte.media.tiff.TIFFTag.RATIONAL_MASK;
import static org.monte.media.tiff.TIFFTag.SHORT_MASK;
import static org.monte.media.tiff.TIFFTag.SRATIONAL_MASK;
import static org.monte.media.tiff.TIFFTag.UNDEFINED_MASK;

/**
 * Enumeration of Fujifilm MakerNote tags.
 * <p>
 * Sources:
 * <p>
 * <a href="http://www.exiv2.org/tags-fujifilm.html">http://www.exiv2.org/tags-fujifilm.html</a>
 * <br>
 * <a href="http://park2.wakwak.com/%7Etsuruzoh/Computer/Digicams/exif-e.html#APP4">http://park2.wakwak.com/%7Etsuruzoh/Computer/Digicams/exif-e.html#APP4</a>
 * <br>
 * <a href="http://homepage3.nifty.com/kamisaka/makernote/makernote_fuji.htm">http://homepage3.nifty.com/kamisaka/makernote/makernote_fuji.htm</a>
 *
 * @author Werner Randelshofer
 */
public class FujifilmMakerNoteTagSet extends TagSet {

    private static FujifilmMakerNoteTagSet instance;
    /**
     * ParallaxXShift (Pixel fraction value).
     * This is -2 times the value that is shown on the camera display.
     * This tag is only found in the maker note of the second image of a .MPO file.
     */
    public final static TIFFTag ParallaxXShift = new TIFFTag("ParallaxXShift", 0xb211, SRATIONAL_MASK);
    /**
     * ParallaxYShift (Pixel fraction value).
     * This is -2 times the value that is shown on the camera display.
     * This tag is only found in the maker note of the second image of a .MPO file.
     */
    public final static TIFFTag ParallaxYShift = new TIFFTag("ParallaxYShift", 0xb212, SRATIONAL_MASK);
    //
    public final static TIFFTag ConvergenceAngle = new TIFFTag("ConvergenceAngle", 0xb205, SRATIONAL_MASK);
    public final static TIFFTag BaselineLength = new TIFFTag("BaselineLength", 0xb206, RATIONAL_MASK);
    public final static TIFFTag SerialNumber = new TIFFTag("SerialNumber", 0x10, ASCII_MASK);

    //
    private FujifilmMakerNoteTagSet(TIFFTag[] tags) {
        super("FujifilmMakerNote", tags);
    }

    /**
     * Returns a shared instance of a BaselineTIFFTagSet.
     */
    public static FujifilmMakerNoteTagSet getInstance() {
        if (instance == null) {
            TIFFTag[] tags = {//
                    /** Fujifilm Makernote version */
                    new TIFFTag("Version", 0x0, UNDEFINED_MASK, new ASCIIValueFormatter()),
                    /** This number is unique, and contains the date of
                     * manufacture, but is not necessarily the same as the number printed
                     * on the camera body. */
                    SerialNumber,
                    /** Image quality setting. */
                    new TIFFTag("Quality", 0x1000, ASCII_MASK),
                    /** Sharpness setting. */
                    new TIFFTag("Sharpness", 0x1001, SHORT_MASK, new EnumValueFormatter(
                            "soft", 1,//
                            "soft2", 2,//
                            "normal", 3,//
                            "hard", 4,//
                            "hard2", 5,//
                            "mediumSoft", 0x82,//
                            "mediumHard", 0x84,//
                            "filmSimulationMode", 0x8000,//
                            "off", 0xffff//
                    )),
                    /** White balance setting. */
                    new TIFFTag("WhiteBalance", 0x1002, SHORT_MASK, new EnumValueFormatter(
                            "auto", 0,//
                            "daylight", 0x100,//
                            "cloudy", 0x200,//
                            "daylightColorFluorescence", 0x300,//
                            "daywhiteColorFluorescence", 0x301,//
                            "whiteFluorescence", 0x302,//
                            "fluorescence4", 0x303,//
                            "fluorescence5", 0x304,//
                            "incandescence", 0x400,//
                            "flash", 0x500,//
                            "customWhiteBalance", 0xf00,//
                            "custom2", 0xf01,//
                            "custom3", 0xf02,//
                            "custom4", 0xf03,//
                            "custom5", 0xf03//
                    )),
                    /** Chroma saturation setting. */
                    new TIFFTag("Color", 0x1003, SHORT_MASK, new EnumValueFormatter(
                            "normal", 0,//
                            "mediumHigh", 0x80,//
                            "high", 0x100,//
                            "mediumLow", 0x180,//
                            "low", 0x200,//
                            "blackAndWhite", 0x300,//
                            "filmSimulationMode", 0x8000//
                    )),
                    /** Contrast setting. */
                    new TIFFTag("Tone", 0x1004, SHORT_MASK, new EnumValueFormatter(
                            "normal", 0,//
                            "mediumHard", 0x80,//
                            "hard", 0x100,//
                            "mediumSoft", 0x180,//
                            "soft", 0x200,//
                            "filmSimulationMode", 0x8000//
                    )),
                    /** Flash firing mode setting. */
                    new TIFFTag("FlashMode", 0x1010, SHORT_MASK, new EnumValueFormatter(
                            "auto", 0,//
                            "on", 1,//
                            "off", 2,//
                            "redEyeReduction", 3//
                    )),
                    /** Flash firing strength compensation setting. */
                    new TIFFTag("FlashStrength", 0x1011, SRATIONAL_MASK),
                    /** Macro mode setting. */
                    new TIFFTag("Macro", 0x1020, SHORT_MASK, new EnumValueFormatter(
                            "off", 0,//
                            "on", 1//
                    )),
                    /** Focusing mode setting. */
                    new TIFFTag("FocusMode", 0x1021, SHORT_MASK, new EnumValueFormatter(
                            "auto", 0,//
                            "manual", 1//
                    )),
                    /** X- and Y-Offset of the principal point on the image plane. */
                    new TIFFTag("PrincipalPoint", 0x1023, SHORT_MASK),

                    /** Slow synchro mode setting. */
                    new TIFFTag("SlowSync", 0x1030, SHORT_MASK, new EnumValueFormatter(
                            "off", 0,//
                            "on", 1//
                    )),
                    /** Picture mode setting. */
                    new TIFFTag("PictureMode", 0x1031, SHORT_MASK, new EnumValueFormatter(
                            "auto", 0,//
                            "portraitScene", 1,//
                            "landscapeScene", 2,//
                            "sportsScene", 4,//
                            "nightScene", 5,//
                            "programAE", 6,//
                            "aperturePriorAE", 256,//
                            "shutterPriorAE", 512,//
                            "manualExposure", 768//
                    )),
                    /** Continuous shooting or auto bracketing setting. */
                    new TIFFTag("Continuous", 0x1100, SHORT_MASK, new EnumValueFormatter(
                            "continuous", 0,//
                            "autoBracketing", 1//
                    )),
                    /** Sequence number. */
                    new TIFFTag("SequenceNumber", 0x1101, SHORT_MASK),
                    /** Fuji FinePix color setting. */
                    new TIFFTag("FinePixColor", 0x1210, SHORT_MASK, new EnumValueFormatter(
                            "standard", 0,//
                            "chrome", 16//
                            //"black and white", ??//
                    )),
                    /** Blur warning status. */
                    new TIFFTag("BlurWarning", 0x1300, SHORT_MASK, new EnumValueFormatter(
                            "good", 0,//
                            "blurred", 1//
                    )),
                    /** Auto Focus warning status. */
                    new TIFFTag("FocusWarning", 0x1301, SHORT_MASK, new EnumValueFormatter(
                            "good", 0,//
                            "outOfFocus", 1//
                    )),
                    /** Auto exposure warning status. */
                    new TIFFTag("ExposureWarning", 0x1302, SHORT_MASK, new EnumValueFormatter(
                            "good", 0,//
                            "overExposure", 1//
                    )),
                    /** Dynamic range. */
                    new TIFFTag("DynamicRange", 0x1400, SHORT_MASK),
                    /** Film mode. */
                    new TIFFTag("FilmMode", 0x1401, SHORT_MASK),
                    /** Dynamic range settings. */
                    new TIFFTag("DynamicRangeSetting", 0x1402, SHORT_MASK),
                    /** Development dynamic range. */
                    new TIFFTag("DevelopmentDynamicRange", 0x1403, SHORT_MASK),
                    /** Minimum focal length. */
                    new TIFFTag("MinFocalLength", 0x1404, RATIONAL_MASK),
                    /** Maximum focal length. */
                    new TIFFTag("MaxFocalLength", 0x1405, RATIONAL_MASK),
                    /** Maximum aperture at mininimum focal. */
                    new TIFFTag("MaxApertureAtMinFocal", 0x1406, RATIONAL_MASK),
                    /** Maximum aperture at maxinimum focal. */
                    new TIFFTag("MaxApertureAtMaxFocal", 0x1407, RATIONAL_MASK),
                    /** File source. */
                    new TIFFTag("FileSource", 0x8000, ASCII_MASK),
                    /** Order number. */
                    new TIFFTag("OrderNumber", 0x8002, LONG_MASK),
                    /** Frame number. */
                    new TIFFTag("FrameNumber", 0x8003, SHORT_MASK),
                    ParallaxXShift, ParallaxYShift,
                    /** The following MP Tags occur in the Fujifilm MakerNote of AVI videos: */
                    new TIFFTag("MPIndividualImageNumber", 0xb101, SHORT_MASK),
                    new TIFFTag("BaseViewpointNumber", 0xb204, SHORT_MASK),
                    ConvergenceAngle,
                    BaselineLength,};
            instance = new FujifilmMakerNoteTagSet(tags);

        }
        return instance;
    }
}

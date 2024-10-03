/*
 * @(#)EXIFTagSet.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.exif;

import org.monte.media.tiff.ASCIIValueFormatter;
import org.monte.media.tiff.DateValueFormatter;
import org.monte.media.tiff.EnumValueFormatter;
import org.monte.media.tiff.SetValueFormatter;
import org.monte.media.tiff.TIFFTag;
import org.monte.media.tiff.TagSet;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static org.monte.media.tiff.TIFFTag.ASCII_MASK;
import static org.monte.media.tiff.TIFFTag.IFD_MASK;
import static org.monte.media.tiff.TIFFTag.LONG_MASK;
import static org.monte.media.tiff.TIFFTag.RATIONAL_MASK;
import static org.monte.media.tiff.TIFFTag.SHORT_MASK;
import static org.monte.media.tiff.TIFFTag.SRATIONAL_MASK;
import static org.monte.media.tiff.TIFFTag.UNDEFINED_MASK;

public class EXIFTagSet extends TagSet {

    public final static TIFFTag ExifVersion = new TIFFTag("ExifVersion", 0x9000, UNDEFINED_MASK, new ASCIIValueFormatter());
    public final static TIFFTag FlashpixVersion = new TIFFTag("FlashpixVersion", 0xa000, UNDEFINED_MASK, new ASCIIValueFormatter());
    // TIFFTag Relating to Image Data Characteristics
    public final static TIFFTag ColorSpace = new TIFFTag("ColorSpace", 0xa001, SHORT_MASK, new EnumValueFormatter(
            "sRGB", 1,//
            "uncalibrated", 0xffff//
    ));
    //
    // Tags Relating to Image Configuration
    public final static TIFFTag ComponentsConfiguration = new TIFFTag("ComponentsConfiguration", 0x9101, UNDEFINED_MASK, new EnumValueFormatter(
            "doesNotExist", 0,//
            "Y", 1,//
            "Cb", 2,//
            "Cr", 3,//
            "R", 4,//
            "G", 5,//
            "B", 6//
    ));
    public final static TIFFTag CompressedBitsPerPixel = new TIFFTag("CompressedBitsPerPixel", 0x9102, RATIONAL_MASK);
    public final static TIFFTag PixelXDimension = new TIFFTag("PixelXDimension", 0xa002, SHORT_MASK | LONG_MASK);
    public final static TIFFTag PixelYDimension = new TIFFTag("PixelYDimension", 0xa003, SHORT_MASK | LONG_MASK);
    //
    // Tags Relating to User Information
    public final static TIFFTag MakerNote = new TIFFTag("MakerNote", 0x927c, UNDEFINED_MASK | IFD_MASK);
    public final static TIFFTag UserComment = new TIFFTag("UserComment", 0x9286, UNDEFINED_MASK);
    //
    // TIFFTag Relating to Related File Information
    public final static TIFFTag RelatedSoundFile = new TIFFTag("RelatedSoundFile", 0xa004, ASCII_MASK);
    //
    // Tags Relating to Instant and Time
    public final static TIFFTag DateTimeOriginal = new TIFFTag("DateTimeOriginal", 0x9003, ASCII_MASK, new DateValueFormatter());
    public final static TIFFTag DateTimeDigitized = new TIFFTag("DateTimeDigitized", 0x9004, ASCII_MASK, new DateValueFormatter());
    public final static TIFFTag SubSecTime = new TIFFTag("SubSecTime", 0x9290, ASCII_MASK);
    public final static TIFFTag SubSecTimeOriginal = new TIFFTag("SubSecTimeOriginal", 0x9291, ASCII_MASK);
    public final static TIFFTag SubSecTimeDigitized = new TIFFTag("SubSecTimeDigitized", 0x9292, ASCII_MASK);
    //
    // Tags Relating to Picture-Taking Conditions
    public final static TIFFTag ExposureTime = new TIFFTag("ExposureTime", 0x829a, RATIONAL_MASK);
    public final static TIFFTag FNumber = new TIFFTag("FNumber", 0x829d, RATIONAL_MASK); /* Aperture */

    public final static TIFFTag ExposureProgram = new TIFFTag("ExposureProgram", 0x8822/*=34850*/, SHORT_MASK, new EnumValueFormatter(
            "notDefined", 0,//
            "manual", 1,//
            "normalProgram", 2,//
            "aperturePriority", 3,//
            "shutterPriority", 4,//
            "createProgram", 5,//
            "actionProgram", 6,//
            "portraitMode", 7,//
            "landscapeMode", 8//
    ));
    public final static TIFFTag SpectralSensitivity = new TIFFTag("SpectralSensitivity", 34852, ASCII_MASK);
    public final static TIFFTag ISOSpeedRatings = new TIFFTag("ISOSpeedRatings", 34855, SHORT_MASK);
    public final static TIFFTag OECF = new TIFFTag("OECF", 0x8828/*=34856*/, UNDEFINED_MASK);

    /**
     * The SensitivityType tag indicates PhotographicSensitivity tag. which one
     * of the parameters of ISO12232 is the Although it is an optional tag, it
     * should be recorded when a PhotographicSensitivity tag is recorded.
     * Value = 4, 5, 6, or 7 may be used in case that the values of plural
     * parameters are the same.
     * <p>
     * Description taken from www.exiv2.org. Copyright (c) Andreas Huggel.
     */
    public final static TIFFTag SensitivityType = new TIFFTag("SensitivityType", 0x8830/*=34864*/, SHORT_MASK, new EnumValueFormatter(
            "unknown", 0,//
            "standardOutputSensitivity", 1,//
            "recomendedExposureIndex", 2,//
            "isoSpeed", 3,//
            "standardOutputSensitivityAndRecommendedExposureIndex", 4,//
            "standardOutputSensitivityAndisoSpeed", 5,//
            "RecommendedExposureIndexAndisoSpeed", 6,//
            "standardOutputSensitivityAndRecommendedExposureIndexAndisoSpeed", 7//
    ));
    public final static TIFFTag RecommendedExposureIndex = new TIFFTag("RecommendedExposureIndex", 0x8832/*=34866*/, LONG_MASK);

    public final static TIFFTag ShutterSpeedValue = new TIFFTag("ShutterSpeedValue", 37377, SRATIONAL_MASK);
    public final static TIFFTag ApertureValue = new TIFFTag("ApertureValue", 37378, RATIONAL_MASK);
    public final static TIFFTag BrightnessValue = new TIFFTag("BrightnessValue", 37379, SRATIONAL_MASK);
    public final static TIFFTag ExposureBiasValue = new TIFFTag("ExposureBiasValue", 37380, SRATIONAL_MASK);
    public final static TIFFTag MaxApertureValue = new TIFFTag("MaxApertureValue", 37381, RATIONAL_MASK);
    public final static TIFFTag SubjectDistance = new TIFFTag("SubjectDistance", 37382, RATIONAL_MASK);
    public final static TIFFTag MeteringMode = new TIFFTag("MeteringMode", 37383, SHORT_MASK, new EnumValueFormatter(
            "unknown", 0,//
            "average", 1,//
            "centerWeightedAverage", 2,//
            "spot", 3,//
            "multiSpot", 4,//
            "pattern", 5,//
            "partial", 6,//
            "other", 255//
    ));
    public final static TIFFTag LightSource = new TIFFTag("LightSource", 37384, SHORT_MASK, new EnumValueFormatter(
            "unknown", 0,//
            "daylight", 1,//
            "fluorescent", 2,//
            "tungsten", 3,//
            "flash", 4,//
            "fineWeather", 9,//
            "cloudyWeather", 10,//
            "shade", 11,//
            "daylightFluorescent", 12,//
            "dayWhiteFluorescent", 13,//
            "coolWhiteFluorescent", 14,//
            "whiteFluorescent", 15,//
            "standardLightA", 17,//
            "standardLightB", 18,//
            "standardLightC", 19,//
            "D55", 20,//
            "D65", 21,//
            "D75", 22,//
            "D50", 23,//
            "ISOStudioTungsten", 24,//
            "otherLightSource", 255//
    ));
    public final static TIFFTag Flash = new TIFFTag("Flash", 37385, SHORT_MASK, new SetValueFormatter(
            "flashDidNotFire", 0, 1,//
            "flashFired", 1,//
            "strobeReturnLightNotDetected", 4,//
            "strobeReturnLightDetected", 2,//
            "compulsoryFiring", 8, 8 + 16,//
            "compulsorySuppression", 16, 8 + 16,//
            "autoMode", 8 + 16,//
            "noFlashFunction", 32,//
            "redEyeReductionSupported", 64//
    ));
    public final static TIFFTag FocalLength = new TIFFTag("FocalLength", 37386, RATIONAL_MASK); // focal length is given in mm.
    public final static TIFFTag SubjectArea = new TIFFTag("SubjectArea", 0x9214/*=37396*/, SHORT_MASK);

    public final static TIFFTag FlashEnergy = new TIFFTag("FlashEnergy", 41483, RATIONAL_MASK);
    public final static TIFFTag SpatialFrequencyResponse = new TIFFTag("SpatialFrequencyResponse", 41483, UNDEFINED_MASK);
    public final static TIFFTag FocalPlaneXResolution = new TIFFTag("FocalPlaneXResolution", 41486, RATIONAL_MASK);
    public final static TIFFTag FocalPlaneYResolution = new TIFFTag("FocalPlaneYResolution", 41487, RATIONAL_MASK);
    public final static TIFFTag FocalPlaneResolutionUnit = new TIFFTag("FocalPlaneResolutionUnit", 41488, SHORT_MASK, new EnumValueFormatter( //
            "noAbsoluteUnit", 1,//
            "inch", 2,//
            "centimeter", 3//
    ));
    public final static TIFFTag SubjectLocation = new TIFFTag("SubjectLocation", 41492, SHORT_MASK);
    public final static TIFFTag ExposureIndex = new TIFFTag("ExposureIndex", 41493, RATIONAL_MASK);
    public final static TIFFTag SensingMethod = new TIFFTag("SensingMethod", 41495, SHORT_MASK, new EnumValueFormatter( //
            "notDefined", 1,//
            "oneChipColorArea", 2,//
            "twoChipColorArea", 3,//
            "threeChipColorArea", 4,//
            "colorSequentialArea", 5,//
            "trilinear", 7,//
            "colorSequentialLinear", 8//
    ));
    public final static TIFFTag FileSource = new TIFFTag("FileSource", 41728, UNDEFINED_MASK, new EnumValueFormatter( //
            "DSC", 3//
    ));
    public final static TIFFTag SceneType = new TIFFTag("SceneType", 41729, UNDEFINED_MASK, new EnumValueFormatter( //
            "directlyPhotographed", 1//
    ));
    public final static TIFFTag CFAPattern = new TIFFTag("CFAPattern", 41730, UNDEFINED_MASK);
    public final static TIFFTag CustomRendered = new TIFFTag("CustomRendered", 41985, SHORT_MASK, new EnumValueFormatter( //
            "normalProcess", 0,//
            "customProcess", 1));
    public final static TIFFTag ExposureMode = new TIFFTag("ExposureMode", 41986, SHORT_MASK, new EnumValueFormatter( //
            "automatic", 0,//
            "manual", 1,//
            "autoBracket", 2//
    ));
    public final static TIFFTag WhiteBalance = new TIFFTag("WhiteBalance", 41987, SHORT_MASK, new EnumValueFormatter( //
            "automatic", 0,//
            "manual", 1));
    public final static TIFFTag DigitalZoomRatio = new TIFFTag("DigitalZoomRatio", 41988, RATIONAL_MASK);
    public final static TIFFTag FocalLengthIn35mmFilm = new TIFFTag("FocalLengthIn35mmFilm", 41989, SHORT_MASK);
    public final static TIFFTag SceneCaptureType = new TIFFTag("SceneCaptureType", 41990, SHORT_MASK, new EnumValueFormatter( //
            "standard", 0,//
            "landscape", 1,//
            "portrait", 2,//
            "nightScene", 3//
    ));
    public final static TIFFTag GainControl = new TIFFTag("GainControl", 41991, RATIONAL_MASK);
    public final static TIFFTag Contrast = new TIFFTag("Contrast", 41992, SHORT_MASK, new EnumValueFormatter( //
            "normal", 0,//
            "soft", 1,//
            "hard", 2//
    ));
    public final static TIFFTag Saturation = new TIFFTag("Saturation", 41993, SHORT_MASK, new EnumValueFormatter( //
            "normal", 0,//
            "low", 1,//
            "high", 2//
    ));
    public final static TIFFTag Sharpness = new TIFFTag("Sharpness", 41994, SHORT_MASK, new EnumValueFormatter( //
            "normal", 0,//
            "soft", 1,//
            "hard", 2//
    ));
    public final static TIFFTag DeviceSettingDescription = new TIFFTag("DeviceSettingDescription", 41995, UNDEFINED_MASK);
    public final static TIFFTag SubjectDistanceRange = new TIFFTag("SubjectDistanceRange", 41996, SHORT_MASK, new EnumValueFormatter( //
            "unknown", 0,//
            "macro", 1,//
            "closeView", 2,//
            "distantView", 3//
    ));
    // Other Tags
    public final static TIFFTag ImageUniqueID = new TIFFTag("ImageUniqueID", 0xa420, ASCII_MASK);

    /* This tag notes minimum focal length, maximum focal length,
     * minimum F number in the minimum focal length, and minimum F number in the maximum focal length,
     * which are specification information for the lens that was used in photography.
     * When the minimum F number is unknown, the notation is 0/0
     * <p>
     * Description taken from www.exiv2.org. Copyright (c) Andreas Huggel.
     */
    public final static TIFFTag LensSpecification = new TIFFTag("LensSpecification", 0xa432/*42034*/, RATIONAL_MASK);
    public final static TIFFTag LensMake = new TIFFTag("LensMake", 0xa433/*42035*/, ASCII_MASK);
    public final static TIFFTag LensModel = new TIFFTag("LensModel", 0xa434/*42036*/, ASCII_MASK);

    public final static TIFFTag Interoperability = new TIFFTag("Interoperability", 0xa005, SHORT_MASK); /* Interoperability IFD Pointer */ //


    private static EXIFTagSet instance;

    private EXIFTagSet(TIFFTag[] tags) {
        super("EXIF", tags);
        Enum<?> x;
    }

    /**
     * Returns a shared instance of a BaselineTIFFTagSet.
     */
    public static EXIFTagSet getInstance() {
        if (instance == null) {
            Field[] fields = EXIFTagSet.class.getDeclaredFields();
            ArrayList<TIFFTag> tags = new ArrayList<>(fields.length);
            try {
                for (Field f : fields) {
                    Object value = f.get(null);
                    if (value instanceof TIFFTag) {
                        tags.add((TIFFTag) value);
                    }
                }
                instance = new EXIFTagSet(tags.toArray(new TIFFTag[tags.size()]));

            } catch (IllegalArgumentException ex) {
                throw new InternalError("Can't read my own fields");
            } catch (IllegalAccessException ex) {
                throw new InternalError("Can't read my own fields");
            }

        }
        return instance;
    }
}

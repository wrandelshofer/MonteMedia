/* @(#)VideoFormatKeys.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.av.codec.video;

import org.monte.media.av.FormatKey;
import org.monte.media.av.FormatKeys;
import org.monte.media.math.Rational;

/**
 * Defines common format keys for video media.
 *
 * @author Werner Randelshofer
 * @version $Id: VideoFormatKeys.java 364 2016-11-09 19:54:25Z werner $
 */
public class VideoFormatKeys extends FormatKeys {
        // Standard video ENCODING strings for use with FormatKey.Encoding. 
    public static final String ENCODING_BUFFERED_IMAGE = "image";
    /** Cinepak format. */
    public static final String ENCODING_QUICKTIME_CINEPAK = "cvid";
    public static final String COMPRESSOR_NAME_QUICKTIME_CINEPAK = "Cinepak";
    /** JPEG format. */
    public static final String ENCODING_QUICKTIME_JPEG = "jpeg";
    public static final String COMPRESSOR_NAME_QUICKTIME_JPEG = "Photo - JPEG";
    /** PNG format. */
    public static final String ENCODING_QUICKTIME_PNG = "png ";
    public static final String COMPRESSOR_NAME_QUICKTIME_PNG = "PNG";
    /** Animation format. */
    public static final String ENCODING_QUICKTIME_ANIMATION = "rle ";
    public static final String COMPRESSOR_NAME_QUICKTIME_ANIMATION = "Animation";
    /** Raw format. */
    public static final String ENCODING_QUICKTIME_RAW = "raw ";
    public static final String COMPRESSOR_NAME_QUICKTIME_RAW = "NONE";
    // AVI Formats
    /** Microsoft Device Independent Bitmap (DIB) format. */
    public static final String ENCODING_AVI_DIB = "\u0000\u0000\u0000\u0000";
    /** Microsoft Run Length format. */
    public static final String ENCODING_AVI_RLE4 = "\u0001\u0000\u0000\u0000";
    /** Microsoft Run Length format. */
    public static final String ENCODING_AVI_RLE8 = "\u0002\u0000\u0000\u0000";
    /** Techsmith Screen Capture format. */
    public static final String ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE = "tscc";
    public static final String COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE = "Techsmith Screen Capture";
    /** DosBox Screen Capture format. */
    public static final String ENCODING_AVI_DOSBOX_SCREEN_CAPTURE = "ZMBV";
    /** JPEG format. */
    public static final String ENCODING_AVI_MJPG = "MJPG";
    /** PNG format. */
    public static final String ENCODING_AVI_PNG = "png ";
    /** Interleaved planar bitmap format. */
    public static final String ENCODING_BITMAP_IMAGE = "ILBM";

    //

    /** The WidthKey of a video frame. */
    public final static FormatKey<Integer> WidthKey = new FormatKey<>("dimX","width", Integer.class);
    /** The HeightKey of a video frame. */
    public final static FormatKey<Integer> HeightKey = new FormatKey<>("dimY","height", Integer.class);
    /** The number of bits per pixel. */
    public final static FormatKey<Integer> DepthKey = new FormatKey<>("dimZ","depth", Integer.class);
    
    /** Pixel format. */
    public enum PixelFormat {
        RGB, GRAY
    }
    
    /** The pixel format. */
    public final static FormatKey<PixelFormat> PixelFormatKey = new FormatKey<>("pixelFormat", PixelFormat.class);
    
    /** The data class. */
    @SuppressWarnings("rawtypes")
    public final static FormatKey<Class> DataClassKey = new FormatKey<>("dataClass", Class.class);
    /** The compressor name. */
    public final static FormatKey<String> CompressorNameKey = new FormatKey<>("compressorName", "compressorName",String.class, true);
    /** The pixel aspect ratio WidthKey : HeightKey;
     */
    public final static FormatKey<Rational> PixelAspectRatioKey = new FormatKey<>("pixelAspectRatio", Rational.class);
    /** Whether the frame rate must be fixed. False means variable frame rate. */
    public final static FormatKey<Boolean> FixedFrameRateKey = new FormatKey<>("fixedFrameRate", Boolean.class);
    /** Whether the video is interlaced. */
    public final static FormatKey<Boolean> InterlaceKey = new FormatKey<>("interlace", Boolean.class);
    /** Encoding quality. Value between 0 and 1. */
    public final static FormatKey<Float> QualityKey = new FormatKey<>("quality", Float.class);
}

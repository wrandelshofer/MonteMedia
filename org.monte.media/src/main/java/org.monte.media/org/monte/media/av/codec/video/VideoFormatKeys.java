/*
 * @(#)VideoFormatKeys.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.video;

import org.monte.media.av.FormatKey;
import org.monte.media.av.FormatKeys;
import org.monte.media.math.Rational;

import java.awt.image.ColorModel;

/**
 * Defines common format keys for video media.
 *
 * @author Werner Randelshofer
 */
public class VideoFormatKeys extends FormatKeys {
    public static final String COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE = "Techsmith Screen Capture";
    public static final String COMPRESSOR_NAME_QUICKTIME_ANIMATION = "Animation";
    public static final String COMPRESSOR_NAME_QUICKTIME_CINEPAK = "Cinepak";
    public static final String COMPRESSOR_NAME_QUICKTIME_JPEG = "Photo - JPEG";
    public static final String COMPRESSOR_NAME_QUICKTIME_PNG = "PNG";
    public static final String COMPRESSOR_NAME_QUICKTIME_RAW = "NONE";
    /**
     * The compressor name.
     */
    public final static FormatKey<String> CompressorNameKey = new FormatKey<>("compressorName", "compressorName", String.class, true, false);
    /**
     * The number of bits per pixel.
     */
    public final static FormatKey<Integer> DepthKey = new FormatKey<>("dimZ", "depth", Integer.class);
    /**
     * Microsoft Device Independent Bitmap (DIB) format.
     */
    public static final String ENCODING_AVI_DIB = "\u0000\u0000\u0000\u0000";
    /**
     * DosBox Screen Capture format.
     */
    public static final String ENCODING_AVI_DOSBOX_SCREEN_CAPTURE = "ZMBV";
    /**
     * JPEG format.
     */
    public static final String ENCODING_AVI_MJPG = "MJPG";
    // AVI Formats
    /**
     * PNG format.
     */
    public static final String ENCODING_AVI_PNG = "png ";
    /**
     * Microsoft Run Length format.
     */
    public static final String ENCODING_AVI_RLE4 = "\u0001\u0000\u0000\u0000";
    /**
     * Microsoft Run Length format.
     */
    public static final String ENCODING_AVI_RLE8 = "\u0002\u0000\u0000\u0000";
    /**
     * Techsmith Screen Capture format.
     */
    public static final String ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE = "tscc";
    /**
     * Interleaved planar bitmap format.
     */
    public static final String ENCODING_BITMAP_IMAGE = "ILBM";
    // Standard video ENCODING strings for use with FormatKey.Encoding.
    public static final String ENCODING_BUFFERED_IMAGE = "image";

    /**
     * H.264 Encoding format.
     */
    public static final String ENCODING_AVC1 = "avc1";
    /**
     * Animation format.
     */
    public static final String ENCODING_QUICKTIME_ANIMATION = "rle ";
    /**
     * Cinepak format.
     */
    public static final String ENCODING_QUICKTIME_CINEPAK = "cvid";

    //
    /**
     * JPEG format.
     */
    public static final String ENCODING_QUICKTIME_JPEG = "jpeg";
    /**
     * PNG format.
     */
    public static final String ENCODING_QUICKTIME_PNG = "png ";
    /**
     * Raw format.
     */
    public static final String ENCODING_QUICKTIME_RAW = "raw ";
    public static final String ENCODING_WRITABLE_IMAGE = "writableImage";
    /**
     * Whether the frame rate must be fixed. False means variable frame rate.
     */
    public final static FormatKey<Boolean> FixedFrameRateKey = new FormatKey<>("fixedFrameRate", Boolean.class);
    /**
     * The HeightKey of a video frame.
     */
    public final static FormatKey<Integer> HeightKey = new FormatKey<>("dimY", "height", Integer.class);
    /**
     * Whether the video is interlaced.
     */
    public final static FormatKey<Boolean> InterlaceKey = new FormatKey<>("interlace", Boolean.class);
    /**
     * Motion search range of the motion estimator.
     * <p>
     * A reasonable value is 16.
     * <p>
     * Set this value to 0 to disable motion estimation in the video codec.
     */
    public final static FormatKey<Integer> MotionSearchRangeKey = new FormatKey<>("motionSearchRange", Integer.class);
    /**
     * Color palette.
     */
    public final static FormatKey<ColorModel> PaletteKey = new FormatKey<>("palette", ColorModel.class);
    /**
     * The pixel aspect ratio WidthKey : HeightKey;
     */
    public final static FormatKey<Rational> PixelAspectRatioKey = new FormatKey<>("pixelAspectRatio", Rational.class);
    /**
     * The pixel format.
     */
    public final static FormatKey<PixelFormat> PixelFormatKey = new FormatKey<>("pixelFormat", PixelFormat.class);
    /**
     * Progressive image encoding. Boolean value. Default is false.
     * <p>
     * Setting this to true reduces the file size of JPEG encoded images by about 10 percent.
     * Unfortunately, the encoding time increases by factor 2 and the decoding time by factor 4.
     */
    public final static FormatKey<Boolean> ProgressiveImageEncodingKey = new FormatKey<>("progressiveImageEncodingMode", "progressiveMode", Boolean.class);
    /**
     * Encoding quality. Value between 0 and 1.
     */
    public final static FormatKey<Float> QualityKey = new FormatKey<>("quality", Float.class);
    /**
     * The affine transformation matrix of the video.
     */
    public final static FormatKey<AffineTransform> TransformKey = new FormatKey<>("transform", "transform", AffineTransform.class);
    /**
     * The WidthKey of a video frame.
     */
    public final static FormatKey<Integer> WidthKey = new FormatKey<>("dimX", "width", Integer.class);

    /**
     * Pixel format.
     */
    public enum PixelFormat {
        RGB, GRAY
    }
}

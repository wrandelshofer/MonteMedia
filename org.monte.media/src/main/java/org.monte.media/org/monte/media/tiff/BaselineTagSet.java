/* @(#)BaselineTagSet.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tiff;

import static org.monte.media.tiff.TIFFTag.ASCII_MASK;
import static org.monte.media.tiff.TIFFTag.BYTE_MASK;
import static org.monte.media.tiff.TIFFTag.IFD_MASK;
import static org.monte.media.tiff.TIFFTag.LONG_MASK;
import static org.monte.media.tiff.TIFFTag.RATIONAL_MASK;
import static org.monte.media.tiff.TIFFTag.SBYTE_MASK;
import static org.monte.media.tiff.TIFFTag.SHORT_MASK;
import static org.monte.media.tiff.TIFFTag.SLONG_MASK;
import static org.monte.media.tiff.TIFFTag.SSHORT_MASK;
import static org.monte.media.tiff.TIFFTag.UNDEFINED_MASK;

/**
 * A class representing the set of tags found in the baseline TIFF specification
 * as well as some common additional tags.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class BaselineTagSet extends TagSet {

    private static BaselineTagSet instance;
    public final static int TAG_EXIF = 0x8769;
    public final static int TAG_GPS = 0x8825;
    public final static int TAG_Interoperability = 0xa005;
    public final static int TAG_JPEGInterchangeFormat = 0x201;
    public final static int TAG_JPEGInterchangeFormatLength = 0x202;
    public final static TIFFTag ImageWidth = new TIFFTag("ImageWidth", 0x0100, SHORT_MASK | LONG_MASK);
    public final static TIFFTag ImageHeight = new TIFFTag("ImageLength", 0x0101, SHORT_MASK | LONG_MASK);
    public final static TIFFTag Make = new TIFFTag("Make", 0x010f, ASCII_MASK);
    public final static TIFFTag Model = new TIFFTag("Model", 0x0110, ASCII_MASK);
    public final static TIFFTag DateTime = new TIFFTag("DateTime", 0x0132, ASCII_MASK, new DateValueFormatter());
    public final static TIFFTag BitsPerSample = new TIFFTag("BitsPerSample", 0x0102, SHORT_MASK);
    public final static TIFFTag SamplesPerPixel = new TIFFTag("SamplesPerPixel", 0x0115, SHORT_MASK);
    /* Same as PrintIM */
    public final static TIFFTag PrintImageMatching = new TIFFTag("PrintImageMatching", 0xc4a5/*=50341*/, UNDEFINED_MASK);
    /**
     * Synthetic tags.
     */
    public final static int TAG_JPEGThumbnailImage = -1;
    public final static TIFFTag JPEGThumbnailImage = new TIFFTag("JPEGThumbnailImage", TAG_JPEGThumbnailImage, UNDEFINED_MASK);

    private BaselineTagSet(TIFFTag[] tags) {
        super("Baseline", tags);
    }

    /**
     * Returns a shared instance of a BaselineTagSet.
     */
    public static BaselineTagSet getInstance() {
        if (instance == null) {
            TIFFTag[] tags = {//
                    // Color:
                    new TIFFTag("PhotometricInterpretation", 0x0106, SHORT_MASK, new EnumValueFormatter(
                            "WhiteIsZero", (0x0), //
                            "BlackIsZero", (0x1), //
                            "RGB", (0x2), //
                            "RGBPalette", (0x3), //
                            "TransparencyMask", (0x4), //
                            "CMYK", (0x5), //
                            "YCbCR", (0x6), //
                            "CIELab", (0x8))),
                    // Compression: See CompressionValue enum.
                    new TIFFTag("Compression", 0x0103, SHORT_MASK, new EnumValueFormatter(
                            "Uncompressed", (0x1), //
                            "CCITT1D", (0x2), //
                            "Group3Fax", (0x3), //
                            "Group4Fax", (0x4), //
                            "LZW", (0x5), //
                            "JPEG", (0x6), //
                            "PackBits", (0x8005) //
                    )),
                    // Rows and columns:
                    ImageWidth, ImageHeight,

                    // Physical Dimensions
                    new TIFFTag("ResolutionUnit", 0x0128, SHORT_MASK, new EnumValueFormatter( //
                            "noAbsoluteUnitOfMeasurement", 1,//
                            "inch", 2,//
                            "centimeter", 3//
                    )),
                    new TIFFTag("XResolution", 0x11a, RATIONAL_MASK),
                    new TIFFTag("YResolution", 0x11b, RATIONAL_MASK),
                    // Location of the data
                    new TIFFTag("RowsPerStrip", 0x116, SHORT_MASK | LONG_MASK),
                    new TIFFTag("StripOffsets", 0x111, SHORT_MASK | LONG_MASK),
                    new TIFFTag("StripByteCounts", 0x117, SHORT_MASK | LONG_MASK),
                    //
                    new TIFFTag("Artist", 0x13b, ASCII_MASK),
                    BitsPerSample,
                    new TIFFTag("CellWidth", 0x108, SHORT_MASK),
                    new TIFFTag("CellLength", 0x109, SHORT_MASK),
                    new TIFFTag("ColorMap", 0x140, SHORT_MASK),
                    new TIFFTag("NewSubfileType", 0xfe, LONG_MASK, new SetValueFormatter( //
                            "reducedResolutionVersion", 1,//
                            "singlePageOfMultiPageImage", 2,//
                            "transparencyMask", 4//
                    )),
                    new TIFFTag("SubfileType", 0xff, SHORT_MASK, new EnumValueFormatter( //todo
                            "fullResolutionVersion", 1,//
                            "reducedResolutionVersion", 2,//
                            "singlePageOfMultiPageImage", 3//
                    )),
                    new TIFFTag("Threshholding", 0x107, SHORT_MASK, new EnumValueFormatter( //
                            "noDitheringWasApplied", 1,//
                            "orderedDither", 2,//
                            "errorDiffusion", 3//
                    )),
                    new TIFFTag("FillOrder", 0x10a, SHORT_MASK, new EnumValueFormatter( // the logical order of bits whithin a byte
                            "bigEndian", 1,//
                            "littleEndian", 2//
                    )),
                    new TIFFTag("DocumentName", 0x10d, ASCII_MASK),
                    new TIFFTag("ImageDescription", 0x10e, ASCII_MASK),
                    Make, Model,
                    new TIFFTag("Orientation", 0x112, SHORT_MASK, new EnumValueFormatter(
                            "topLeft", (0x1), //
                            "topRight", (0x2), //
                            "bottomRight", (0x3), //
                            "bottomLeft", (0x4), //
                            "leftTop", (0x5), //
                            "rightTop", (0x6), //
                            "rightBottom", (0x7), //
                            "leftBottom", (0x8) //
                    )),
                    SamplesPerPixel,
                    new TIFFTag("MinSampleValue", 0x118, SHORT_MASK),
                    new TIFFTag("MaxSampleValue", 0x119, SHORT_MASK),
                    new TIFFTag("PlanarConfiguration", 0x11c, SHORT_MASK, new EnumValueFormatter(
                            "chunky", 1,//
                            "planar", 2//
                    )),
                    new TIFFTag("PageName", 0x11d, ASCII_MASK),
                    new TIFFTag("XPosition", 0x11e, RATIONAL_MASK),
                    new TIFFTag("YPosition", 0x11f, RATIONAL_MASK),
                    new TIFFTag("FreeOffsets", 0x120, LONG_MASK),
                    new TIFFTag("FreeByteCounts", 0x1219, LONG_MASK),
                    new TIFFTag("GrayResponseUnit", 0x122, SHORT_MASK, new EnumValueFormatter( //
                            "tenths", 1,//
                            "hundredths", 2,//
                            "thousandths", 3,//
                            "then-thousandths", 4,//
                            "hundred-thousandths", 5//
                    )),
                    new TIFFTag("GrayResponseCurve", 0x123, SHORT_MASK),
                    new TIFFTag("T4Options", 0x124, LONG_MASK, new SetValueFormatter( //
                            "2dimensionalCoding", 1,//
                            "uncompressed", 2,//
                            "fillBitsBeforeEOL", 4//
                    )),
                    new TIFFTag("T6Options", 0x125, LONG_MASK, new SetValueFormatter( //
                            "uncompressed", 2//
                    )),
                    new TIFFTag("PageNumber", 0x129, SHORT_MASK),
                    new TIFFTag("TransferFunction", 0x12d, SHORT_MASK),
                    new TIFFTag("Software", 0x131, ASCII_MASK),
                    DateTime,
                    new TIFFTag("HostComputer", 0x13c, ASCII_MASK),
                    new TIFFTag("Predictor", 0x13d, SHORT_MASK, new EnumValueFormatter( //
                            "noPredictionScheme", 1,//
                            "horizontalDifferencing", 2//
                    )),
                    new TIFFTag("WhitePoint", 0x13e, RATIONAL_MASK),
                    new TIFFTag("PrimaryChromaticities", 0x13f, RATIONAL_MASK),
                    new TIFFTag("HalftoneHints", 0x141, SHORT_MASK),
                    new TIFFTag("TileWidth", 0x142, SHORT_MASK | LONG_MASK),
                    new TIFFTag("TileLength", 0x143, SHORT_MASK | LONG_MASK),
                    new TIFFTag("TileOffsets", 0x144, LONG_MASK),
                    new TIFFTag("TileByteCounts", 0x145, SHORT_MASK | LONG_MASK),
                    new TIFFTag("InkSet", 0x14c, SHORT_MASK),
                    new TIFFTag("InkNames", 0x14d, ASCII_MASK),
                    new TIFFTag("NumberOfInks", 0x14e, SHORT_MASK),
                    new TIFFTag("DotRange", 0x150, BYTE_MASK | SHORT_MASK),
                    new TIFFTag("TargetPrinter", 0x151, ASCII_MASK),
                    new TIFFTag("ExtraSamples", 0x152, SHORT_MASK, new EnumValueFormatter( //
                            "unspecifiedData", 0,//
                            "associatedAlphaData", 1,//
                            "unassociatedAlphaData", 2//
                    )),
                    new TIFFTag("SampleFormat", 0x153, SHORT_MASK, new EnumValueFormatter( //
                            "unsignedInteger", 1,//
                            "signedInteger", 2,//
                            "floatingPoint", 3,//
                            "undefinedDataFormat", 4//
                    )),
                    new TIFFTag("SMinSampleValue", 0x154, BYTE_MASK | SHORT_MASK | LONG_MASK | SBYTE_MASK | SSHORT_MASK | SLONG_MASK),
                    new TIFFTag("SMaxSampleValue", 0x155, BYTE_MASK | SHORT_MASK | LONG_MASK | SBYTE_MASK | SSHORT_MASK | SLONG_MASK),
                    new TIFFTag("TransferRange", 0x156, SHORT_MASK),
                    new TIFFTag("JPEGProc", 0x200, SHORT_MASK, new EnumValueFormatter( //
                            "baselineSequential", 1,//
                            "losslessHuffman", 14//
                    )),
                    new TIFFTag("JPEGInterchangeFormat", TAG_JPEGInterchangeFormat, LONG_MASK),
                    new TIFFTag("JPEGInterchangeFormatLength", TAG_JPEGInterchangeFormatLength, LONG_MASK),
                    JPEGThumbnailImage,
                    new TIFFTag("JPEGRestartInterval", 0x203, SHORT_MASK),
                    new TIFFTag("JPEGLosslessPredictors", 0x205, SHORT_MASK),
                    new TIFFTag("JPEGPointTransforms", 0x206, SHORT_MASK),
                    new TIFFTag("JPEGQTables", 0x207, LONG_MASK),
                    new TIFFTag("JPEGDCTables", 0x208, LONG_MASK),
                    new TIFFTag("JPEGACTables", 0x209, LONG_MASK),
                    new TIFFTag("YCbCrCoefficients", 0x211, RATIONAL_MASK),
                    new TIFFTag("YCbCrSubSampling", 0x212, SHORT_MASK),
                    new TIFFTag("YCbCrPositioning", 0x213, SHORT_MASK, new EnumValueFormatter( //
                            "centered", 1,//
                            "cosited", 2//
                    )),
                    new TIFFTag("ReferenceBlackWhite", 0x0214, RATIONAL_MASK),
                    //
                    new TIFFTag("Copyright", 0x8298, ASCII_MASK),
                    // EXIF-specific IFD. See JEITA CP-3451, Page 15

                    PrintImageMatching,

                    new TIFFTag("EXIF", TAG_EXIF, IFD_MASK | LONG_MASK), /* EXIF IFD Pointer */
                    new TIFFTag("GPS", TAG_GPS, IFD_MASK | LONG_MASK), /* GPS IFD Pointer */
                    new TIFFTag("Interoperability", TAG_Interoperability, IFD_MASK | LONG_MASK), /* Interoperability IFD Pointer */ //
            };
            instance = new BaselineTagSet(tags);

        }
        return instance;
    }
}

/* @(#)BaselineTIFFTagSet.java
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.exif;

import org.monte.media.tiff.TagSet;
import org.monte.media.tiff.SetValueFormatter;
import org.monte.media.tiff.*;
import static org.monte.media.tiff.TIFFTag.*;

/**
 * Enumeration of GPS EXIF tags.
 * 
 * Sources:
 * 
 * Exchangeable image file format for digital still cameras: EXIF Version 2.2.
 * (April, 2002). Standard of Japan Electronics and Information Technology
 * Industries Association. JEITA CP-3451.
 * <a href="http://www.exif.org/Exif2-2.PDF">http://www.exif.org/Exif2-2.PDF</a>
 * 
 * Exiv2 Image metadata library and tools.
 * Copyright © 2004 - 2014 Andreas Huggel
 * <a href="http://www.exiv2.org/tags.html">http://www.exiv2.org/tags.html</a>
 * 
 * @author Werner Randelshofer
 * @version 1.0 2010-07-24 Created.
 */
public class GPSTagSet extends TagSet {

    private static GPSTagSet instance;

    private GPSTagSet(TIFFTag[] tags) {
        super("GPS", tags);
    }

    /** Returns a shared instance of a BaselineTIFFTagSet. */
    public static GPSTagSet getInstance() {
        if (instance == null) {
            TIFFTag[] tags = {//
                // Tags Relating to GPS
                new TIFFTag("GPSVersionID", 0x0000, BYTE_MASK),
                new TIFFTag("GPSLatitudeRef", 0x0001, ASCII_MASK, new SetValueFormatter(//
                "north","N",//
                "south","S"//
                )),
                new TIFFTag("GPSLatitude", 0x0002, RATIONAL_MASK),
                new TIFFTag("GPSLongitudeRef", 0x0003, ASCII_MASK, new SetValueFormatter(//
                "east","E",//
                "west","W"//
                )),
                new TIFFTag("GPSLongitude", 0x0004, RATIONAL_MASK),
                new TIFFTag("GPSAltitudeRef", 0x0005, BYTE_MASK, new SetValueFormatter(//
                "aboveSeaLevel", 0,//
                "belowSeaLevel", 1//
                )),
                new TIFFTag("GPSAltitude", 0x0006, RATIONAL_MASK),
                new TIFFTag("GPSTimeStamp", 0x0007, RATIONAL_MASK),
                new TIFFTag("GPSSatellites", 0x0008, ASCII_MASK),
                new TIFFTag("GPSStatus", 0x0009, ASCII_MASK, new SetValueFormatter(//
                "measurementActive","A",//
                "measurementVoid","V"//
                )),
                new TIFFTag("GPSMeasureMode", 0x000a, ASCII_MASK, new SetValueFormatter(//
                "2-dimensionalMeasurement","2",//
                "3-dimensionalMeasurement","3"//
                )),
                /** Data degree of precision. 
                 * An HDOP value is written during two-dimensional measurement,
                 * and PDOP during three-dimensional measurement.
                 */
                new TIFFTag("GPSDOP", 0x000b, RATIONAL_MASK),
                new TIFFTag("GPSSpeedRef", 0x000c, ASCII_MASK, new SetValueFormatter(//
                "kmh","K",//
                "mph","M",//
                        "knots","N"//
                )),
                new TIFFTag("GPSTrackRef", 0x000e, ASCII_MASK, new SetValueFormatter(//
                "magneticNorth","M",//
                "trueNorth","T"//
                )),
                new TIFFTag("GPSTrack", 0x000f, RATIONAL_MASK),
                new TIFFTag("GPSImgDirectionRef", 0x0010, ASCII_MASK, new SetValueFormatter(//
                "magneticNorth","M",//
                "trueNorth","T"//
                )),
                new TIFFTag("GPSImgDirection", 0x00011, RATIONAL_MASK),
                new TIFFTag("GPSMapDatum", 0x0012, ASCII_MASK),
                new TIFFTag("GPSDestLatitudeRef", 0x0013, ASCII_MASK, new SetValueFormatter(//
                "north","N",//
                "south","S"//
                )),
                new TIFFTag("GPSDestLatitude", 0x0014, RATIONAL_MASK),
                new TIFFTag("GPSDestLongitudeRef", 0x0015, ASCII_MASK, new SetValueFormatter(//
                "east","E",//
                "west","W"//
                )),
                new TIFFTag("GPSDestLongitude", 0x0016, RATIONAL_MASK),
                new TIFFTag("GPSDestBearingRef", 0x0017, ASCII_MASK, new SetValueFormatter(//
                "magneticNorth","M",//
                "trueNorth","T"//
                )),
                new TIFFTag("GPSDestBearing", 0x0018, RATIONAL_MASK),
                new TIFFTag("GPSDestDistanceRef", 0x0019, ASCII_MASK, new SetValueFormatter(//
                "kmh","K",//
                "mph","M",//
                        "knots","N"//
                )),
                new TIFFTag("GPSDestDistance", 0x001a, RATIONAL_MASK),
                new TIFFTag("GPSProcessingMethod", 0x001b, UNDEFINED_MASK),
                new TIFFTag("GPSAreaInformation", 0x001c, UNDEFINED_MASK),
                new TIFFTag("GPSDateStamp", 0x001d, ASCII_MASK),
                new TIFFTag("GPSDifferential", 0x001e, SHORT_MASK, new SetValueFormatter(//
                "noCorrection","0",//
                "differentialCorrection","1"//
                )), //
                new TIFFTag("GPSHPositioningError", 0x001f, RATIONAL_MASK),//
            };
            instance = new GPSTagSet(tags);
        }
        return instance;
    }
}

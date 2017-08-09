/* @(#)BaselineTIFFTagSet.java
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.exif;

import org.monte.media.tiff.ASCIIValueFormatter;
import org.monte.media.tiff.TagSet;
import org.monte.media.tiff.*;
import static org.monte.media.tiff.TIFFTag.*;

/**
 * Enumeration of Interoperation EXIF tags.
 * <p>
 * Sources:
 * <p>
 * Exchangeable image file format for digital still cameras: EXIF Version 2.2.
 * (April, 2002). Standard of Japan Electronics and Information Technology
 * Industries Association. JEITA CP-3451.
 * <a href="http://www.exif.org/Exif2-2.PDF">http://www.exif.org/Exif2-2.PDF</a>
 * <p>
 * Multi-Picture Format
 * (February 4, 2009). Standard of the Camera &amp; Imaging Products Association.
 * CIPA DC-007-Translation-2009.
 * <a href="http://www.cipa.jp/english/hyoujunka/kikaku/pdf/DC-007_E.pdf">
 * http://www.cipa.jp/english/hyoujunka/kikaku/pdf/DC-007_E.pdf</a>
 * 
 * @author Werner Randelshofer
 * @version 1.0 2010-07-24 Created.
 */
public class InteroperabilityTagSet extends TagSet {

    private static InteroperabilityTagSet instance;

    private InteroperabilityTagSet(TIFFTag[] tags) {
        super("Interoperability", tags);
    }

    /** Returns a shared instance of a BaselineTIFFTagSet. */
    public static InteroperabilityTagSet getInstance() {
        if (instance == null) {
            TIFFTag[] tags = {//
    new TIFFTag("InteroperabilityIndex",0x1,SHORT_MASK),
    new TIFFTag("InteroperabilityVersion",0x2,SHORT_MASK, new ASCIIValueFormatter()),
    new TIFFTag("RelatedImageFileFormat",0x1000,SHORT_MASK),
    new TIFFTag("RelatedImageWidth",0x1001,SHORT_MASK),
    new TIFFTag("RelatedImageLength",0x1002,SHORT_MASK),
};
            instance = new InteroperabilityTagSet(tags);

        }
        return instance;
    }
}

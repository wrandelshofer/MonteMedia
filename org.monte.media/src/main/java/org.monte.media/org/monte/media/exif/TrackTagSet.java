/* @(#)TrackTagSet.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.exif;

import org.monte.media.tiff.TIFFTag;
import org.monte.media.tiff.TagSet;

/**
 * TrackTagSet.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class TrackTagSet extends TagSet {
    private static TrackTagSet instance;

    public static TrackTagSet getInstance() {
        if (instance == null) {
            instance = new TrackTagSet();
        }
        return instance;
    }


    private TrackTagSet() {
        super("Image", new TIFFTag[0]);
    }

}

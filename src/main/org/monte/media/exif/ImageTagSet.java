/* @(#)ImageTagSet.java
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */

package org.monte.media.exif;

import org.monte.media.tiff.TIFFTag;
import org.monte.media.tiff.TagSet;

/**
 * ImageTagSet.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-07-25 Created.
 */
public class ImageTagSet extends TagSet {
    private static ImageTagSet instance;

    public static ImageTagSet getInstance() {
        if (instance==null) {
            instance=new ImageTagSet();
        }
        return instance;
    }



    private ImageTagSet() {
        super("Image",new TIFFTag[0]);
    }

}

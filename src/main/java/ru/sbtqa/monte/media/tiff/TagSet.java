/* @(#)TagSet.java
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.tiff;

import java.util.HashMap;
import static ru.sbtqa.monte.media.tiff.TIFFTag.ALL_MASK;

/**
 * A class representing a set of TIFF tags. Each tag in the set must have a
 * unique number (this is a limitation of the TIFF specification itself).
 * 
 * This class and its subclasses are responsible for mapping between raw tag
 * numbers and TIFFTag objects, which contain additional information about each
 * tag, such as the tag's name, legal data types, and mnemonic names for some or
 * all of its data values.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-07-24 Created.
 */
public abstract class TagSet {

    private HashMap<Integer, TIFFTag> tagsByNumber = new HashMap<>();
    private String name;

    public TagSet(String name, TIFFTag[] tags) {
        this.name = name;
        for (TIFFTag tag : tags) {
            tag.setTagSet(this);
            tagsByNumber.put(tag.getNumber(), tag);
        }
    }

    /**
     * Returns the TIFFTag from this set that is associated with the given tag
     * number.
     * <br>
     * Returns a TIFFTag with name "unknown" if the tag is not defined.
     *
     * @param tagNumber TODO
     * @return TODO
     */
    public TIFFTag getTag(int tagNumber) {
        TIFFTag tag = tagsByNumber.get(tagNumber);
        if (tag == null) {
            synchronized (this) {
                tag = tagsByNumber.get(tagNumber);
                if (tag == null) {
                    tag = new TIFFTag("unknown", tagNumber, ALL_MASK, null);
                    tagsByNumber.put(tagNumber, tag);
                }
            }
        }
        return tag;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
}

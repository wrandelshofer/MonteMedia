/*
 * @(#)TagSet.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tiff;

import java.util.HashMap;

/**
 * A class representing a set of TIFF tags. Each tag in the set must have a
 * unique number (this is a limitation of the TIFF specification itself).
 * <p>
 * This class and its subclasses are responsible for mapping between raw tag
 * numbers and TIFFTag objects, which contain additional information about each
 * tag, such as the tag's name, legal data types, and mnemonic names for some or
 * all of its data values.
 *
 * @author Werner Randelshofer
 */
public abstract class TagSet {

    private final HashMap<Integer, TIFFTag> tagsByNumber = new HashMap<Integer, TIFFTag>();
    private final String                    name;

    public TagSet(String name, TIFFTag[] tags) {
        this.name = name;
        for (TIFFTag tag : tags) {
            tag.setTagSet(this);
            tagsByNumber.put(tag.getNumber(), tag);
        }
    }

    /**
     * Returns the TIFFTag from this set that is associated with the given
     * tag number.
     * <br>
     * Returns a TIFFTag with name "unknown" if the tag is not defined.
     */
    public TIFFTag getTag(int tagNumber) {
        TIFFTag tag = tagsByNumber.get(tagNumber);
        if (tag == null) {
            synchronized (this) {
	            tag = tagsByNumber.computeIfAbsent(tagNumber, n -> new TIFFTag("unknown", n, TIFFTag.ALL_MASK, null));
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

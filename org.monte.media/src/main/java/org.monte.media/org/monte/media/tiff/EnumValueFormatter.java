/*
 * @(#)EnumValueFormatter.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.tiff;

import java.util.HashMap;

/**
 * Formats integer values as an enumeration.
 *
 * @author Werner Randelshofer
 */
public class EnumValueFormatter implements ValueFormatter {

    private HashMap<Integer, String> enumMap;

    /**
     * Creates a new enumeration.
     * The enumeration consists of a list of String=Integer pairs.
     */
    public EnumValueFormatter(Object... enumeration) {
        enumMap = new HashMap<>();
        for (int i = 0; i < enumeration.length; i += 2) {
            String value = (String) enumeration[i];
            Integer key = (Integer) enumeration[i + 1];
            if (enumMap.containsKey(key)) {
                enumMap.put(key, enumMap.get(key) + ", " + value);
            } else {
                enumMap.put(key, value);
            }
        }
    }

    @Override
    public Object format(Object value) {
        if (value instanceof Number) {
            int intValue = ((Number) value).intValue();
            if (enumMap.containsKey(intValue)) {
                return enumMap.get(intValue);
            }
        }
        return value;
    }

    @Override
    public Object prettyFormat(Object value) {
        if (value instanceof Number) {
            int intValue = ((Number) value).intValue();
            if (enumMap.containsKey(intValue)) {
                return "0x" + Integer.toHexString(intValue) + " [" + enumMap.get(intValue) + "]";
            }
        }
        return value;
    }

    @Override
    public String descriptionFormat(Object value) {
        if (value instanceof Number) {
            int intValue = ((Number) value).intValue();
            if (enumMap.containsKey(intValue)) {
                return enumMap.get(intValue);
            }
        }
        return null;
    }
}

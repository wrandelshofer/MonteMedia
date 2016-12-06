/* @(#)EnumValueFormatter.java
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.tiff;

import static java.lang.Integer.toHexString;
import java.util.HashMap;

/**
 * Formats integer values as an enumeration.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-03-22 Created.
 */
public class EnumValueFormatter implements ValueFormatter {

    private HashMap<Integer, String> enumMap;

    /**
     * Creates a new enumeration. The enumeration consists of a list of
     * String=Integer pairs.
     *
     * @param enumeration TODO
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
                return "0x" + toHexString(intValue) + " [" + enumMap.get(intValue) + "]";
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

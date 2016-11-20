/* @(#)IFDEnum.java
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.tiff;

import static java.lang.Integer.toHexString;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Formats integer values as a set.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-03-22 Created.
 */
public class SetValueFormatter implements ValueFormatter {

    /** Defines an entry of the set. */
    private class Entry {

        public Entry(String name, int bits) {
            this.name = name;
            this.bits = bits;
            this.mask = bits;
        }

        public Entry(String name, int bits, int mask) {
            this.name = name;
            this.bits = bits;
            this.mask = mask;
        }
        public Entry(String name, String stringValue) {
            this.name = name;
            this.stringValue = stringValue;
            this.bits = this.mask = 0;
        }
        /* the bits that must be set. */
        int bits;
        /* a mask which is considered for the bits. */
        int mask;
        /* the descriptive name of the value. */
        String name;
        /* the value that must be set. */
        String stringValue;
    }
    private LinkedList<Entry> setDefinition;

    /** Creates a new enumeration.
     * The enumeration consists of a list of String=Integer,
     * String=Integer Integer, or String=String pairs.
     * <p>
     * <ul>
     * <li>String=Integer. If only one integer is provided, it specifies the bits which must
     * be set.</li>
     * <li>String=Integer Integer.
     * If two integers are provided, the second value specifies a bit mask.</li>
     * <li>String=String.
     * If a String is provided, it specifies the String that must be set.</li>
     * </ul>
     */
    public SetValueFormatter(Object... set) {
        setDefinition = new LinkedList<>();
        for (int i = 0; i < set.length;) {
            if (i < set.length - 2 && (set[i + 1] instanceof Integer)&& (set[i + 2] instanceof Integer)) {
                setDefinition.add(new Entry((String) set[i], (Integer) set[i + 1], (Integer) set[i + 2]));
                i += 3;
            } else if ((set[i + 1] instanceof Integer)) {
                setDefinition.add(new Entry((String) set[i], (Integer) set[i + 1]));
                i += 2;
            } else if ((set[i + 1] instanceof String)) {
                setDefinition.add(new Entry((String) set[i], (String) set[i + 1]));
                i += 2;
            } else {
                throw new IllegalArgumentException("illegal set:"+set);
            }
        }
    }

    @Override
    public Object format(Object value) {
        if (value instanceof Number) {
            HashSet<String> setValue = new HashSet<>();
            int intValue = ((Number) value).intValue();
            for (Entry elem : setDefinition) {
                if ((elem.mask & intValue) == elem.bits) {
                    setValue.add(elem.name);
                }
            }
            return setValue;
        } else if (value instanceof String) {
            HashSet<String> setValue = new HashSet<>();
            for (Entry elem : setDefinition) {
                if (value.equals(elem.stringValue)) {
                    setValue.add(elem.name);
                }
            }
            return setValue;
        }
        return value;
    }

    @Override
    public Object prettyFormat(Object value) {
        if (value instanceof Number) {
            int intValue = ((Number) value).intValue();
            StringBuilder prettyValue = new StringBuilder();
            for (Entry elem : setDefinition) {
                if ((elem.mask & intValue) == elem.bits) {
                    if (prettyValue.length() > 0) {
                        prettyValue.append(',');
                    }
                    prettyValue.append(elem.name);
                }
            }
            prettyValue.insert(0, " {");
            prettyValue.insert(0, toHexString(intValue));
            prettyValue.insert(0, "0x");
            prettyValue.append("}");
            return prettyValue.toString();
        }
        return value;
    }

    @Override
    public String descriptionFormat(Object value) {
        if (value instanceof Number) {
            int intValue = ((Number) value).intValue();
            StringBuilder prettyValue = new StringBuilder();
            for (Entry elem : setDefinition) {
                if ((elem.mask & intValue) == elem.bits) {
                    if (prettyValue.length() > 0) {
                        prettyValue.append(',');
                    }
                    prettyValue.append(elem.name);
                }
            }
            return prettyValue.toString();
        }
        return null;
    }
}

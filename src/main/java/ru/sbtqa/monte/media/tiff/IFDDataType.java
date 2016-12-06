/* @(#)IFDDataType.java
 * Copyright © 2009 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.tiff;

import java.util.HashMap;
import ru.sbtqa.monte.media.math.Rational;

/**
 * Enumeration of TIFF IFD data types.
 * 
 * Sources:
 * 
 * TIFF TM Revision 6.0. Final — June 3, 1992.<br>
 * Adobe Systems Inc.<br>
 * <a href="http://www.exif.org/specifications.html">http://www.exif.org/specifications.html</a>
 * 
 * Adobe PageMaker® 6.0 TIFF Technical Notes - September 14, 1995<br>
 * Adobe Systems Inc.<br>
 * <a href="http://www.alternatiff.com/resources/TIFFPM6.pdf">http://www.alternatiff.com/resources/TIFFPM6.pdf</a>
 *
 *
 * @author werni
 */
public enum IFDDataType {
    /**
     * 8-bit byte that contains a 7-bit ASCII code; the last byte must be NUL
     * (binary zero). Represented by a String object in Java.
     */
    ASCII(2, String.class),
    //
    /**
     * 8-bit unsigned integer. Represented by a Short object in Java.
     */
    BYTE(1, Short.class, Short.TYPE),
    //
    /**
     * 16-bit (2-byte) unsigned integer. Represented by an Int object in Java.
     */
    SHORT(3, Integer.class, Integer.TYPE),
    //
    /**
     * 32-bit (4-byte) unsigned integer. Represented by a Long object in Java.
     */
    LONG(4, Long.class, Long.TYPE),
    //
    /**
     * Two LONGs: the first represents the numerator of a fraction; the second,
     * the denominator. Represented by a Rational object in Java.
     */
    RATIONAL(5, Rational.class),
    //
    /**
     * An 8-bit signed (twos-complement) integer. Represented by a Byte object
     * in Java.
     */
    SBYTE(6, Byte.class, Byte.TYPE),
    //
    /**
     * An 8-bit byte that may contain anything, depending on the definition of
     * the field. Represented by a Byte object in Java.
     */
    UNDEFINED(7, Byte.class, Byte.TYPE),
    //
    /**
     * A 16-bit (2-byte) signed (twos-complement) integer. Represented by a
     * Short object in Java.
     */
    SSHORT(8, Short.class, Short.TYPE),
    //
    /**
     * A 32-bit (4-byte) signed (twos-complement) integer. Represented by an Int
     * object in Java.
     */
    SLONG(9, Integer.class, Integer.TYPE),
    //
    /**
     * Two SLONG’s: the first represents the numerator of a fraction, the second
     * the denominator. Represented by a Rational object in Java.
     */
    SRATIONAL(10, Rational.class),
    //
    /**
     * Single precision (4-byte) IEEE format. Represented by a Float object in
     * Java.
     */
    FLOAT(11, Float.class, Float.TYPE),
    //
    /**
     * Double precision (8-byte) IEEE format. Represented by a Double object in
     * Java.
     */
    DOUBLE(12, Double.class, Double.TYPE),
    /**
     * 32-bit (4-byte) unsigned integer pointing to another IFD, as defined in
     * TIFF Tech Note 1 in TIFF Specification Supplement 1. Represented by a
     * Long object in Java.
     */
    IFD(13, Long.class, Long.TYPE);
    //
    private final int typeNumber;
    private final Class<?>[] componentClasses;
    private final static HashMap<Integer, IFDDataType> valueToFieldType = new HashMap<Integer, IFDDataType>();

    static {
        for (IFDDataType t : IFDDataType.values()) {
            valueToFieldType.put(t.getTypeNumber(), t);
        }
    }

    private IFDDataType(int typeNumber, Class<?>... componentClasses) {
        this.typeNumber = typeNumber;
        this.componentClasses = componentClasses;
    }

    public int getTypeNumber() {
        return typeNumber;
    }

    /**
     * Gets the tag for the specified value.
     *
     * @param typeNumber TODO
     * @return TODO
     */
    public static IFDDataType valueOf(int typeNumber) {
        return valueToFieldType.get(typeNumber);
    }

    void checkType(Object data) {
        if (data != null) {
            Class<?> cc = data.getClass();
            while (cc.isArray()) {
                cc = cc.getComponentType();
            }
            for (Class<?> validcc : componentClasses) {
                if (cc == validcc) {
                    return;
                }
            }
            throw new IllegalArgumentException(data + " is illegal for " + this);
        }
    }

}

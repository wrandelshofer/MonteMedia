/* @(#)ExtendedReal.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.media.math;

/**
 * Represents an extended-real number as specified by IEEE 754.
 *
 * An extended-real number uses 80 bits to represent a floating point
 * number. It is able to represent numbers ranging from 3.37*10^-4932
 * up to 1.18*10^4932.
 *
 * Bit layout
 * <pre><code>
 * 79    78-64            63       62-0
 * Sign  Biased Exponent  Integer  Fraction
 * </code></pre>
 *
 * For the single-real and double-real formats, only the fraction part
 * of the significand is encoded. The integer is assumed to be 1 for
 * all numbers except 0 and denormalized finite numbers. For the
 * extended-real format, the integer is contained in bit 64, and the
 * most significant fraction bit is bit 62. Here, the integer is
 * explicitly set to 1 for normalized numbers, infinites, and NaNs,
 * and to 0 for zero and denormalized numbers.
 *
 * The exponent is encoded in biased format. The biasing constant is
 * 16'383 for the extended-real format.
 *
 * NaN Encodings for ExtendedReal:
 * <pre><code>
 * Class                   Sign   Biased     Significand
 *                                Exponent   Integer  Fraction
 * ------------------------------------------------------------
 * Positive +Infinity       0    11..11       1       00..00
 *          +Normals        0    11..10       1       11..11
 *                          .        .        .           .
 *                          .        .        .           .
 *                          0    00..01       1       00..00
 *          +Denormals      0    00..00       0       11..11
 *                          .        .        .           .
 *                          .        .        .           .
 *                          0    00..00       0       00..01
 *          +Zero           0    00..00       0       00..00
 * Negative -Zero           1    00..00       0       00..00
 *          -Denormals      1    00..00       0       00..01
 *                          .        .        .           .
 *                          .        .        .           .
 *                          1    00..00       0       11..11
 *          -Normals        1    00..01       1       00..01
 *                          .        .        .           .
 *                          .        .        .           .
 *                          1    11..10       1       11..11
 *          -Infinity       1    11..11       1       00..00
 * NaNs     SNaN            X    11..11       1       0X..XX(2
 *          QNaN            X    11..11       1       1X..XX
 *          Real Indefinite 1    11..11       1       10..00
 *
 * </code></pre>
 * (2 The fraction for SNaN encodings must be non zero.
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland.
 * @version 0.1 2000-10-06 Conversions from bit array, and to double only.
 */
public class ExtendedReal
extends Number {
    private final static long serialVersionUID = 1L;
    /** bit 79: negSign*/
    private boolean negSign;

    /** bit 78 - 64: biased exponent. */
    private int exponent;

    /** bit 63: Integer; 62 - 0: Fraction */
    private long mantissa;


    public final static ExtendedReal MAX_VALUE = new ExtendedReal(
    //              negSign -----exponent-----     int ------------------------------------------------fraction----------------------------------
    //               79     78..72      71..64      63 62..54   53..48      47..40      39..32      31..24       23..16      15..8        7..0
        new byte[] { (byte)0x7f,    (byte)0xfe, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff}
    );

    public final static ExtendedReal MIN_VALUE = new ExtendedReal(
    //              negSign -----exponent-----     int ------------------------------------------------fraction----------------------------------
    //               79     78..72      71..64      63 62..54   53..48      47..40      39..32      31..24       23..16      15..8        7..0
        new byte[] { (byte)0xff,    (byte)0xfe, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff}
    );
    public final static ExtendedReal NaN = new ExtendedReal(
    //              negSign -----exponent-----     int ------------------------------------------------fraction----------------------------------
    //               79     78..72      71..64      63 62..54   53..48      47..40      39..32      31..24       23..16      15..8        7..0
        new byte[] { (byte)0xff,    (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff}
    );
    public final static ExtendedReal NEGATIVE_INFINITY = new ExtendedReal(
    //              negSign -----exponent-----     int ------------------------------------------------fraction----------------------------------
    //               79     78..72      71..64      63 62..54   53..48      47..40      39..32      31..24       23..16      15..8        7..0
        new byte[] { (byte)0xff,    (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}
    );
    public final static ExtendedReal POSITIVE_INFINITY = new ExtendedReal(
    //              negSign -----exponent-----     int ------------------------------------------------fraction----------------------------------
    //               79     78..72      71..64      63 62..54   53..48      47..40      39..32      31..24       23..16      15..8        7..0
        new byte[] { (byte)0x7f,    (byte)0xff, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00}
    );

    public ExtendedReal(byte[] bits) {
        negSign = (bits[0] & 0x80) != 0;
        exponent = (bits[0] & 0x7f) << 8 | (bits[1] & 0xff);
        mantissa =
            (bits[2] & 0xffL) << 56 |
            (bits[3] & 0xffL) << 48 |
            (bits[4] & 0xffL) << 40 |
            (bits[5] & 0xffL) << 32 |
            (bits[6] & 0xffL) << 24 |
            (bits[7] & 0xffL) << 16 |
            (bits[8] & 0xffL) << 8  |
            (bits[9] & 0xffL) << 0;
    }

    public ExtendedReal(double d) {
        if (Double.isNaN(d)) {
            negSign = NaN.negSign;
            exponent = NaN.exponent;
            mantissa = NaN.mantissa;

        } else if (Double.isInfinite(d)) {
            if (d < 0.0) {
                negSign = NEGATIVE_INFINITY.negSign;
                exponent = NEGATIVE_INFINITY.exponent;
                mantissa = NEGATIVE_INFINITY.mantissa;
            } else {
                negSign = POSITIVE_INFINITY.negSign;
                exponent = POSITIVE_INFINITY.exponent;
                mantissa = POSITIVE_INFINITY.mantissa;
            }
        } else if (d == +0.0) {
            // nothing to do
        } else if (d == -0.0) {
            negSign = true;

        } else {
            long longBits = Double.doubleToLongBits(d);

            negSign = (longBits & 0x8000000000000000L) != 0L;
            exponent = ((int) (longBits & 0x7ff0000000000000L) >>> 52) - 1023 + 16383;
            mantissa = 0x8000000000000000L | (longBits & 0x000fffffffffffffL) << 11;
        }
    }

    public boolean isNaN() {
        return exponent == 0x7ffff && (mantissa & 0x7fffffffffffffffL) != 0;
    }

    public boolean isInfinite() {
        return exponent == 0x7ffff && mantissa == 0x7fffffffffffffffL;
    }

    public double doubleValue() {
        if (isNaN()) {
            return Double.NaN;
        }


        long longBits = 0;
        // biased exponent

        int biasedExponent = exponent - 16383 + 1023;
        if (biasedExponent > 2047) {
            // overflow
            return (negSign) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        }
        if (biasedExponent < 0 || (mantissa & 0x8000000000000000L) == 0L) {
            // underflow
            return 0.0;
        }

        // negSign
        if (negSign) {
            longBits = 0x8000000000000000L;
        }

        longBits = longBits | (((long) biasedExponent) << 52);
        longBits = longBits | ((mantissa & 0x7fffffffffffffffL) >>> 11);
        return Double.longBitsToDouble(longBits);
    }
    public float floatValue() {
        return (float) doubleValue();
    }
    public int intValue() {
        return (int) doubleValue();
    }
    public long longValue() {
        return (long) doubleValue();
    }

    public int hashCode() {
        long bits = Double.doubleToLongBits(doubleValue());
        return (int)(bits ^ (bits >>> 32));
    }

    public boolean equals(Object obj) {
        return (obj != null)
            && (obj instanceof ExtendedReal)
            && (equals((ExtendedReal) obj));
    }

    public boolean equals(ExtendedReal obj) {
        return negSign == obj.negSign && exponent == obj.exponent && mantissa == obj.mantissa;
    }

    /**
     * FIXME: Loss of precision, because we currently convert to double before
     * we create the String.
     */
    public String toString() {
        return Double.toString(doubleValue());
    }

}

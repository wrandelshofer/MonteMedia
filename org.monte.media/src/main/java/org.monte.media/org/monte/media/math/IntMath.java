/*
 * @(#)IntMath.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.math;

import java.math.BigInteger;

/**
 * Utility class for integer arithmetic.
 *
 * @author Werner Randelshofer
 */
public class IntMath {

    /**
     * Creates a new instance of IntMath
     */
    public IntMath() {
    }

    /**
     * Returns an int whose value is the greatest common divisor of
     * {@code abs(a)} and {@code abs(b)}.  Returns 0 if
     * {@code a==0 && b==0}.
     * <p>
     * References:
     * <dl>
     *     <dt>Stein's method</dt>
     *     <dd>
     *         <a href="https://www.geeksforgeeks.org/steins-algorithm-for-finding-gcd/"geeksforgeeks.org></a>
     *             </dd>
     * </dl>
     *
     * @param a value with the GCD is to be computed.
     * @param b value with the GCD is to be computed.
     * @return {@code GCD(a, b)}
     */
    public static int gcd(int a, int b) {
        // GCD(0, b) == b; GCD(a, 0) == a,
        // GCD(0, 0) == 0
        if (a == 0)
            return b;
        if (b == 0)
            return a;

        // Finding K, where K is the greatest
        // power of 2 that divides both a and b
        int k = Math.min(Integer.numberOfTrailingZeros(a), Integer.numberOfTrailingZeros(b));
        a >>>= k;
        b >>>= k;

        // Dividing a by 2 until a becomes odd
        a >>>= Integer.numberOfTrailingZeros(a);

        // From here on, 'a' is always odd.
        do {
            // If b is even, remove
            // all factor of 2 in b
            b >>>= Integer.numberOfTrailingZeros(b);

            // Now a and b are both odd. Swap
            // if necessary so a <= b, then set
            // b = b - a (which is even)
            if (a > b) {
                // Swap a and b.
                int temp = a;
                a = b;
                b = temp;
            }

            b = (b - a);
        } while (b != 0);

        // restore common factors of 2
        return a << k;
    }

    /**
     * Returns a long whose value is the greatest common divisor of
     * {@code abs(a)} and {@code abs(b)}.  Returns 0 if
     * {@code a==0 &amp;&amp; b==0}.
     * <p>
     * References:
     * <dl>
     *     <dt>Stein's method</dt>
     *     <dd>
     *         <a href="https://www.geeksforgeeks.org/steins-algorithm-for-finding-gcd/"geeksforgeeks.org></a>
     *             </dd>
     * </dl>
     *
     * @param a value with the GCD is to be computed.
     * @param b value with the GCD is to be computed.
     * @return {@code GCD(a, b)}
     */
    public static long gcd(long a, long b) {
        // GCD(0, b) == b; GCD(a, 0) == a,
        // GCD(0, 0) == 0
        if (a == 0)
            return b;
        if (b == 0)
            return a;

        // Finding K, where K is the greatest
        // power of 2 that divides both a and b
        int k = Math.min(Long.numberOfTrailingZeros(a), Long.numberOfTrailingZeros(b));
        a >>>= k;
        b >>>= k;

        // Dividing a by 2 until a becomes odd
        a >>>= Long.numberOfTrailingZeros(a);

        // From here on, 'a' is always odd.
        do {
            // If b is even, remove
            // all factor of 2 in b
            b >>>= Long.numberOfTrailingZeros(b);

            // Now a and b are both odd. Swap
            // if necessary so a <= b, then set
            // b = b - a (which is even)
            if (a > b) {
                // Swap a and b.
                long temp = a;
                a = b;
                b = temp;
            }

            b = (b - a);
        } while (b != 0);

        // restore common factors of 2
        return a << k;
    }

    /**
     * Returns a long whose value is the greatest common divisor of
     * {@code abs(a)} and {@code abs(b)}.  Returns 0 if
     * {@code a==0 &amp;&amp; b==0}.
     * <p>
     * References:
     * <dl>
     *     <dt>Herrmann, D. (1992). Algorithmen Arbeitsbuch</dt>
     *     <dd>Bonn, München Paris: Addison Wesley.
     *         ggt6, Page 63</dd>
     * </dl>
     *
     * @param a value with the GCD is to be computed.
     * @param b value with the GCD is to be computed.
     * @return {@code GCD(a, b)}
     */
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        a = a.abs();
        b = b.abs();

        while (a.compareTo(BigInteger.ZERO) > 0 && b.compareTo(BigInteger.ZERO) > 0) {
            a = a.mod(b);
            if (a.compareTo(BigInteger.ZERO) > 0) b = b.mod(a);
        }
        return a.add(b);
    }

    /**
     * Returns an int whose value is the smallest common multiple of
     * {@code abs(a)} and {@code abs(b)}.  Returns 0 if
     * {@code a==0 || b==0}.
     * <p>
     * References:
     * <dl>
     *     <dt>Herrmann, D. (1992). Algorithmen Arbeitsbuch</dt>
     *     <dd>Bonn, München Paris: Addison Wesley.
     *         gill, Page 141</dd>
     * </dl>
     *
     * @param a value with the SCM is to be computed.
     * @param b value with the SCM is to be computed.
     * @return {@code SCM(a, b)}
     */
    public static int scm(int a, int b) {
        if (a == 0 || b == 0) return 0;

        a = Math.abs(a);
        b = Math.abs(b);

        int u = a;
        int v = b;

        while (a != b) {
            if (a < b) {
                b -= a;
                v += u;
            } else {
                a -= b;
                u += v;
            }
        }


        //return a; // gcd
        return (u + v) / 2; // scm
    }

    /**
     * Returns an int whose value is the smallest common multiple of
     * {@code abs(a)} and {@code abs(b)}.  Returns 0 if
     * {@code a==0 || b==0}.
     * <p>
     * References:
     * <dl>
     *     <dt>Herrmann, D. (1992). Algorithmen Arbeitsbuch</dt>
     *     <dd>Bonn, München Paris: Addison Wesley.
     *         gill, Page 141</dd>
     * </dl>
     *
     * @param a value with the SCM is to be computed.
     * @param b value with the SCM is to be computed.
     * @return {@code SCM(a, b)}
     */
    public static long scm(long a, long b) {
        if (a == 0 || b == 0) return 0;

        a = Math.abs(a);
        b = Math.abs(b);
        if (b == 1) return a;
        if (a == 1) return b;

        long u = a;
        long v = b;

        while (a != b) {
            if (a < b) {
                b -= a;
                v += u;
            } else {
                a -= b;
                u += v;
            }
        }

        //return a; // gcd
        return (u + v) / 2; // scm
    }

    /**
     * Returns an int whose value is the smallest common multiple of
     * {@code abs(a)} and {@code abs(b)}.  Returns 0 if
     * {@code a==0 || b==0}.
     * <p>
     * References:
     * <dl>
     *     <dt>Herrmann, D. (1992). Algorithmen Arbeitsbuch</dt>
     *     <dd>Bonn, München Paris: Addison Wesley.
     *         gill, Page 141</dd>
     * </dl>
     *
     * @param a value with the SCM is to be computed.
     * @param b value with the SCM is to be computed.
     * @return {@code SCM(a, b)}
     */
    public static BigInteger scm(BigInteger a, BigInteger b) {
        if (a.compareTo(BigInteger.ZERO) == 0 || b.compareTo(BigInteger.ZERO) == 0) {
            return BigInteger.ZERO;
        }

        a = a.abs();
        b = b.abs();
        if (b.compareTo(BigInteger.ONE) == 0) return a;
        if (a.compareTo(BigInteger.ONE) == 0) return b;

        BigInteger u = a;
        BigInteger v = b;

        while (a.compareTo(b) != 0) {
            if (a.compareTo(b) < 0) {
                b = b.subtract(a);
                v = v.add(u);
            } else {
                a = a.subtract(b);
                u = u.add(v);
            }
        }


        //return a; // gcd
        return (u.add(v)).divide(BigInteger.valueOf(2)); // scm
    }

    /**
     * Reverses all 32 bits of the provided integer value.
     */
    public static int reverseBits(int a) {
        return reverseBits(a, 32);
    }

    /**
     * Reverses specified number of bits of the provided integer value.
     *
     * @param a       The number.
     * @param numBits The number of bits (must be between 1 and 32).
     */
    public static int reverseBits(int a, int numBits) {
        int b = 0;
        for (int i = 0; i < numBits; i++) {
            b <<= 1;
            b |= (a & 1);
            a >>>= 1;
        }
        return b;

    }

    public static void main(String[] args) {
        for (int i = 0; i < 8; i++) {
            int a = 1 << i;
            int b = reverseBits(a, 3);
            System.out.println(a + " - " + b);
        }
    }
}

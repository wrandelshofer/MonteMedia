/*
 * @(#)IntMath.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.math;

import java.math.BigInteger;

/// Utility class for integer arithmetic.
///
/// @author Werner Randelshofer
public class IntMath {

    /// Creates a new instance of IntMath
    public IntMath() {
    }

    /// Returns an int whose value is the greatest common divisor of
    /// `abs(a)` and `abs(b)`.  Returns 0 if
    /// `a==0 && b==0`.
    ///
    /// References:
    /// <dl>
    ///     <dt>Stein's method</dt>
    ///     <dd>
    ///         <a href="https://www.geeksforgeeks.org/steins-algorithm-for-finding-gcd/"geeksforgeeks.org></a>
    ///             </dd>
    /// </dl>
    ///
    /// @param a value with the GCD is to be computed.
    /// @param b value with the GCD is to be computed.
    /// @return `GCD(a, b)`
    public static int gcd(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);
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

    /// Returns a long whose value is the greatest common divisor of
    /// `abs(a)` and `abs(b)`.  Returns 0 if
    /// `a==0 &amp;&amp; b==0`.
    ///
    /// References:
    /// <dl>
    ///     <dt>Stein's method</dt>
    ///     <dd>
    ///         <a href="https://www.geeksforgeeks.org/steins-algorithm-for-finding-gcd/"geeksforgeeks.org></a>
    ///             </dd>
    /// </dl>
    ///
    /// @param a value with the GCD is to be computed.
    /// @param b value with the GCD is to be computed.
    /// @return `GCD(a, b)`
    public static long gcd(long a, long b) {
        // GCD(0, b) == b; GCD(a, 0) == a,
        // GCD(0, 0) == 0
        a = Math.abs(a);
        b = Math.abs(b);
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

    /// Returns a long whose value is the greatest common divisor of
    /// `abs(a)` and `abs(b)`.  Returns 0 if
    /// `a==0 &amp;&amp; b==0`.
    ///
    /// References:
    /// <dl>
    ///     <dt>Herrmann, D. (1992). Algorithmen Arbeitsbuch</dt>
    ///     <dd>Bonn, München Paris: Addison Wesley.
    ///         ggt6, Page 63</dd>
    /// </dl>
    ///
    /// @param a value with the GCD is to be computed.
    /// @param b value with the GCD is to be computed.
    /// @return `GCD(a, b)`
    public static BigInteger gcd(BigInteger a, BigInteger b) {
        a = a.abs();
        b = b.abs();

        while (a.compareTo(BigInteger.ZERO) > 0 && b.compareTo(BigInteger.ZERO) > 0) {
            a = a.mod(b);
            if (a.compareTo(BigInteger.ZERO) > 0) b = b.mod(a);
        }
        return a.add(b);
    }

    /// Returns an int whose value is the smallest common multiple of
    /// `abs(a)` and `abs(b)`.  Returns 0 if
    /// `a==0 || b==0`.
    ///
    /// References:
    /// <dl>
    ///     <dt>Herrmann, D. (1992). Algorithmen Arbeitsbuch</dt>
    ///     <dd>Bonn, München Paris: Addison Wesley.
    ///         gill, Page 141</dd>
    /// </dl>
    ///
    /// @param a value with the SCM is to be computed.
    /// @param b value with the SCM is to be computed.
    /// @return `SCM(a, b)`
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

    /// Returns an int whose value is the smallest common multiple of
    /// `abs(a)` and `abs(b)`.  Returns 0 if
    /// `a==0 || b==0`.
    ///
    /// References:
    /// <dl>
    ///     <dt>Herrmann, D. (1992). Algorithmen Arbeitsbuch</dt>
    ///     <dd>Bonn, München Paris: Addison Wesley.
    ///         gill, Page 141</dd>
    /// </dl>
    ///
    /// @param a value with the SCM is to be computed.
    /// @param b value with the SCM is to be computed.
    /// @return `SCM(a, b)`
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

    /// Returns an int whose value is the smallest common multiple of
    /// `abs(a)` and `abs(b)`.  Returns 0 if
    /// `a==0 || b==0`.
    ///
    /// References:
    /// <dl>
    ///     <dt>Herrmann, D. (1992). Algorithmen Arbeitsbuch</dt>
    ///     <dd>Bonn, München Paris: Addison Wesley.
    ///         gill, Page 141</dd>
    /// </dl>
    ///
    /// @param a value with the SCM is to be computed.
    /// @param b value with the SCM is to be computed.
    /// @return `SCM(a, b)`
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

    /// Reverses all 32 bits of the provided integer value.
    public static int reverseBits(int a) {
        return reverseBits(a, 32);
    }

    /// Reverses specified number of bits of the provided integer value.
    ///
    /// @param a       The number.
    /// @param numBits The number of bits (must be between 1 and 32).
    public static int reverseBits(int a, int numBits) {
        int b = 0;
        for (int i = 0; i < numBits; i++) {
            b <<= 1;
            b |= (a & 1);
            a >>>= 1;
        }
        return b;

    }

    /// Maps the high-bits of a 32-bit number `word` into the
    /// range `[0,p)`.
    ///
    /// This implementation uses the FastRange algorithm by Daniel Lemire.
    ///
    /// <dl>
    /// <dt>Daniel Lemire (2016).
    /// A fast alternative to the modulo reduction.
    /// </dt>
    /// <dd>[lemire.me](https://lemire.me/blog/2016/06/27/a-fast-alternative-to-the-modulo-reduction/)</dd>
    /// </dl>
    ///
    /// @param word a 32-bit word
    /// @param p    a range, must be {@literal > 0}.
    /// @return word in range `[0,p)`
    public static int fastRange(int word, int p) {
        return (int) (((0xffffffffL & word) * p) >>> 32);
    }


    /// Maps the low-bits of a 32-bit number `word` into the
    /// range `[0,p)`.
    ///
    /// This implementation uses the 'Faster Remainder by Direct Computation'
    /// algorithm by Daniel Lemire, Owen Kaser, Nathan Kurz.
    ///
    /// <dl>
    /// <dt>Daniel Lemire, Owen Kaser, Nathan Kurz (2018).
    /// Faster Remainder by Direct Computation.
    /// Applications to Compilers and Software Libraries.
    /// </dt>
    /// <dd>[arxiv.org](https://arxiv.org/pdf/1902.01961.pdf)</dd>
    /// </dl>
    ///
    /// @param word a 32-bit word
    /// @param p    range, must be {@literal > 0}.
    /// @param invp 64-bit inverse of the range, see [#compute64BitInverse(int)]
    /// @return word in range `[0,p)`
    public static int fastMod(int word, int p, long invp) {
        long lowbits = invp * word;
        int mod = (int) Math.multiplyHigh(lowbits, p);
        return mod < 0 ? p + mod : mod;
    }

    /// Computes a division by multiplying the value with the inverse
    /// of the divisor.
    ///
    /// <dl>
    /// <dt>Daniel Lemire, Owen Kaser, Nathan Kurz (2018).
    /// Faster Remainder by Direct Computation.
    /// Applications to Compilers and Software Libraries.
    /// </dt>
    /// <dd>[arxiv.org](https://arxiv.org/pdf/1902.01961.pdf)</dd>
    /// </dl>
    ///
    /// @param word a 32-bit word
    /// @param invp 64-bit inverse of the range, see [#compute64BitInverse(int)]
    /// @return word in range `[0,p)`
    public static int fastDiv(int word, long invp) {
        return (int) Math.multiplyHigh(word, invp);
    }

    /// Computes the 64-bit inverse of the given value.
    ///
    /// `ceil((1<<64)/ d )` = `Long.divideUnsigned(-1L , p) + 1`.
    /// <dl>
    /// <dt>Daniel Lemire, Owen Kaser, Nathan Kurz (2018).
    /// Faster Remainder by Direct Computation.
    /// Applications to Compilers and Software Libraries.
    /// </dt>
    /// <dd>[arxiv.org](https://arxiv.org/pdf/1902.01961.pdf)</dd>
    /// </dl>
    ///
    /// @param p a value
    /// @return the 64-bit inverse
    public static long compute64BitInverse(int p) {
        return Long.divideUnsigned(-1L, p) + 1;
    }

    /// Maps the low-bits of a 32-bit number `word` into the
    /// range `[0,p)`.
    ///
    /// This implementation uses the floorMod function.
    ///
    /// @param word a 32-bit word
    /// @param p    a range, must be {@literal > 0}.
    /// @return word in range `[0,p)`
    public static int floorModRange(int word, int p) {
        return Math.floorMod(word, p);
    }

    /// Maps the low-bits of a 32-bit number `word` into the
    /// range `[0,p)`.
    ///
    /// This implementation uses the modulo-operator.
    ///
    /// @param word a 32-bit word
    /// @param p    a range, must be {@literal > 0}.
    /// @return word in range `[0,p)`
    public static int moduloRange(int word, int p) {
        return Math.abs(word % p);
    }

    /// Maps the low-bits of a 32-bit number `word` into the
    /// range `[0,p)`.
    ///
    /// This implementation uses the bit-wise logical and-operator.
    ///
    /// @param word     a 32-bit word
    /// @param powerOf2 a range, must be {@literal > 0}, must be a power of 2.
    /// @return word in range `[0,p)`
    public static int powerOf2Range(int word, int powerOf2) {
        return word & (powerOf2 - 1);
    }

    /// Rounds the specified value up to a power of two, so that it
    /// can be used as a length with [#powerOf2Range(int,int)].
    ///
    /// @param value a value
    /// @return the value rounded up to a power of two, clamped to {@literal [0, 1<<30]}.
    public static int roundUpToPowerOf2(int value) {
        return Math.min(1 << 30, Integer.highestOneBit(value + (value << 1)));
    }
}

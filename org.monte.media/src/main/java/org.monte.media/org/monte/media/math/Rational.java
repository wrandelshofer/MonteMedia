/*
 * @(#)Rational.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.math;

import java.math.BigInteger;

import static java.lang.Math.abs;
import static java.lang.Math.signum;
import static org.monte.media.math.IntMath.gcd;
import static org.monte.media.math.IntMath.scm;

/**
 * Represents a Rational number {@code numerator/denominator}.
 * <p>
 * A number is represented by two longs: the first represents the numerator of
 * a fraction; the second, the denominator.
 * <p>
 * Invariants:
 * <ul>
 * <li>{@code denominator &gt;=0}, the denominator is always a positive integer</li>
 * <li>{@code 0/1} is the unique* representation of 0.</li>
 * <li>{code 1/0},{@code -1/}0 are the unique representations of positive and
 * negative infinity.</li>
 * </ul>
 *
 * @author Werner Randelshofer
 */
public class Rational extends Number implements Comparable<Rational> {

    public static final Rational ONE = new Rational(1, 1, false);
    public static final Rational ZERO = new Rational(0, 1, false);
    public static final long serialVersionUID = 1L;
    private final long num;
    private final long den;

    public Rational(long numerator) {
        this(numerator, 1);
    }

    public Rational(long numerator, long denominator) {
        this(numerator, denominator, true);
    }

    private Rational(long numerator, long denominator, boolean reduceFraction) {
        if (numerator == 0) {
            // Invariant: 0/1 is unique representation of 0
            denominator = 1;
        }

        if (denominator == 0) {
            // Invariant: 1/0, -1/0 are unique representations of infinity
            numerator = (numerator > 0) ? 1 : -1;
        } else if (denominator < 0) {
            // Invariant: denominator is always positive
            denominator = -denominator;
            numerator = -numerator;
        }

        if (reduceFraction) {
            long g = gcd(numerator, denominator);
            num = numerator / g;
            den = denominator / g;
        } else {
            num = numerator;
            den = denominator;
        }
    }

    private Rational(BigInteger numerator, BigInteger denominator, boolean reduceFraction) {
        if (numerator.equals(BigInteger.ZERO)) {
            // Invariant: 0/1 is unique representation of 0
            denominator = BigInteger.ONE;
        }

        if (denominator.equals(BigInteger.ZERO)) {
            // Invariant: 1/0, -1/0 are unique representations of infinity
            numerator = (numerator.compareTo(BigInteger.ZERO) > 0) ? BigInteger.ONE : BigInteger.ONE.negate();
        } else if (denominator.compareTo(BigInteger.ZERO) < 0) {
            // Invariant: denominator is always positive
            denominator = denominator.negate();
            numerator = numerator.negate();
        }

        BigInteger numB, denB;
        if (reduceFraction) {
            BigInteger g = gcd(numerator, denominator);
            numB = numerator.divide(g);
            denB = denominator.divide(g);
        } else {
            numB = numerator;
            denB = denominator;
        }
        int bitLength = Math.max(numB.bitLength(), denB.bitLength());
        if (bitLength > 63) {
            numB = numB.shiftRight(bitLength - 63);
            denB = denB.shiftRight(bitLength - 63);
            if (numB.equals(BigInteger.ZERO)) {
                // Invariant: 0/1 is unique representation of 0
                denB = BigInteger.ONE;
            }

            if (denB.equals(BigInteger.ZERO)) {
                // Invariant: 1/0, -1/0 are unique representations of infinity
                numB = (numB.compareTo(BigInteger.ZERO) > 0) ? BigInteger.ONE : BigInteger.ONE.negate();

            }
        }
        num = numB.longValue();
        den = denB.longValue();
    }

    public Rational(Rational r) {
        this(r.num, r.den);
    }

    public static Rational clamp(Rational v, Rational min, Rational max) {
        if (v.compareTo(min) < 0) return min;
        if (v.compareTo(max) > 0) return max;
        return v;
    }

    public long getNumerator() {
        return num;
    }

    public long getDenominator() {
        return den;
    }

    public Rational add(Rational that) {
        return add(that, true);
    }

    public Rational add(long num, long den) {
        return add(num, den, true);
    }

    private Rational add(Rational that, boolean reduceFraction) {
        return add(that.num, that.den, reduceFraction);
    }

    private Rational add(long thatNum, long thatDen, boolean reduceFraction) {
        if (this.den == thatDen) {
            // => same denominator: add numerators 
            return new Rational(this.num + thatNum, this.den, reduceFraction);
        }

        // FIXME - handle overflow
        long s = scm(this.den, thatDen);

        return new Rational(
                this.num * (s / this.den) + thatNum * (s / thatDen),
                s, reduceFraction);
    }

    public boolean isInRange(Rational minInclusive, Rational maxExclusive) {
        return minInclusive.compareTo(this) <= 0 &&
                this.compareTo(maxExclusive) < 0;
    }

    public Rational subtract(Rational that) {
        return add(that.negate());
    }

    public Rational negate() {
        return valueOf(-num, den);
    }

    public Rational inverse() {
        return valueOf(den, num, false);
    }

    /**
     * Returns the closest rational with the specified denominator which is
     * smaller or equal than this number.
     */
    public Rational floor(long d) {
        if (d == den) {
            return valueOf(num, den);
        }
        long s = scm(this.den, d);

        if (s == d) {
            return valueOf(num * s / den, d);
        } else if (s == den) {
            return valueOf(num * d / den, d);
        } else {
            return valueOf(num * d / den, d);
        }
    }

    /**
     * Returns the closest numerator for the specified denominator which is
     * smaller or equal than this number.
     */
    public long floorNumerator(long d) {
        if (d == den) {
            return num;
        }
        long s = scm(this.den, d);

        if (s == d) {
            return num * s / den;
        } else {
            return num * d / den;
        }
    }

    /**
     * Returns the closest rational with the specified denominator which is
     * greater or equal than this number.
     */
    public Rational ceil(long d) {
        if (d == den) {
            return valueOf(num, den);
        }
        long s = scm(this.den, d);

        if (s == d) {
            return valueOf((num * s + den - 1) / den, d);
        } else if (s == den) {
            return valueOf((num * d + den - 1) / den, d);
        } else {
            return valueOf((num * d + den - 1) / den, d);
        }
    }

    public Rational multiply(Rational that) {
        if (abs(this.num) < Integer.MAX_VALUE
                && abs(this.den) < Integer.MAX_VALUE
                && abs(that.num) < Integer.MAX_VALUE
                && abs(that.den) < Integer.MAX_VALUE) {
            return valueOf(this.num * that.num,
                    this.den * that.den);
        } else {
            return new Rational(
                    BigInteger.valueOf(this.num).multiply(BigInteger.valueOf(that.num)),
                    BigInteger.valueOf(this.den).multiply(BigInteger.valueOf(that.den)),
                    true);
        }
    }

    public Rational multiply(long integer) {
        if (integer == 0) {
            return ZERO;
        } else if (this.den % integer == 0) {
            return valueOf(
                    this.num,
                    this.den / integer);
        } else if (abs(this.num) < Integer.MAX_VALUE
                && abs(integer) < Integer.MAX_VALUE) {
            return valueOf(
                    this.num * integer,
                    this.den);
        } else {
            return new Rational(
                    BigInteger.valueOf(this.num).multiply(BigInteger.valueOf(integer)),
                    BigInteger.valueOf(this.den), true);
        }
    }

    public Rational divide(long integer) {
        if (integer == 0) {
            return ZERO;
        } else if (this.num % integer == 0) {
            return valueOf(
                    this.den,
                    this.num / integer);
        } else if (abs(this.den) < Integer.MAX_VALUE
                && abs(integer) < Integer.MAX_VALUE) {
            return valueOf(
                    this.den * integer,
                    this.num);
        } else {
            return new Rational(
                    BigInteger.valueOf(this.den).multiply(BigInteger.valueOf(integer)),
                    BigInteger.valueOf(this.num), true);
        }
    }

    public Rational divide(Rational that) {
        if (abs(this.num) < Integer.MAX_VALUE
                && abs(this.den) < Integer.MAX_VALUE
                && abs(that.num) < Integer.MAX_VALUE
                && abs(that.den) < Integer.MAX_VALUE) {
            return valueOf(this.num * that.den,
                    this.den * that.num);
        } else {
            return valueOf(
                    BigInteger.valueOf(this.num).multiply(BigInteger.valueOf(that.den)),
                    BigInteger.valueOf(this.den).multiply(BigInteger.valueOf(that.num)),
                    true);
        }
    }

    @Override
    public String toString() {
        //long gcd = IntMath.gcd(num, den);
        if (num == 0) {
            return "0";
        } else if (den == 1) {
            return Long.toString(num);
        } else {
            return num + "/" + den;
            /*
             } else {
             return Float.toString((float) num / den);
             */
        }
    }

    public String toDescriptiveString() {
        long gcd = IntMath.gcd(num, den);
        if (gcd == 0 || num == 0) {
            return num + "/" + den + " = " + 0;
        } else if (gcd == den) {
            return num + "/" + den + " = " + Long.toString(num / den);
        } else {
            return num + "/" + den + " ≈ " + ((float) num / den);
        }
    }

    @Override
    public int intValue() {
        return (int) (num / den);
    }

    @Override
    public long longValue() {
        return num / den;
    }

    @Override
    public float floatValue() {
        return (float) num / (float) den;
    }

    @Override
    public double doubleValue() {
        return (double) num / (double) den;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Rational that = (Rational) obj;

        return compareTo(that) == 0;
    }

    /**
     * return { -1, 0, +1 } if a &lt; b, a = b, or a &gt; b.
     */
    public int compareTo(Rational that) {
        // The following code avoids BigInteger allocation if the denominators 
        // are equal 
        if (this.den == that.den) {
            if (this.num < that.num) {
                return -1;
            } else if (this.num > that.num) {
                return 1;
            } else {
                return 0;
            }
        }

        // Work with longs if overflow can not occur
        if (abs(this.num) < Integer.MAX_VALUE
                && abs(this.den) < Integer.MAX_VALUE
                && abs(that.num) < Integer.MAX_VALUE
                && abs(that.den) < Integer.MAX_VALUE) {
            long lhs = this.num * that.den;
            long rhs = this.den * that.num;
            if (lhs < rhs) {
                return -1;
            } else if (lhs > rhs) {
                return 1;
            } else {
                return 0;
            }
        }

        // Use big integers to avoid overflows
        BigInteger lhs;
        BigInteger rhs;
        lhs = BigInteger.valueOf(this.num).multiply(BigInteger.valueOf(that.den));
        rhs = BigInteger.valueOf(this.den).multiply(BigInteger.valueOf(that.num));

        return lhs.compareTo(rhs);
    }

    @Override
    public int hashCode() {
        return (int) ((num ^ (num >>> 32))
                ^ (den ^ (den >>> 32)));

    }

    public static Rational max(Rational a, Rational b) {
        return (a.compareTo(b) >= 0) ? a : b;
    }

    public static Rational min(Rational a, Rational b) {
        return (a.compareTo(b) <= 0) ? a : b;
    }

    public boolean isZero() {
        return num == 0;
    }

    public boolean isLessOrEqualZero() {
        return num <= 0;
    }

    public static Rational valueOf(double d) {
        if (d == 0) {
            return valueOf(0, 1);
        }
        if (abs(d) > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Value " + d + " is too big.");
        }
        if (Double.isInfinite(d)) {
            return valueOf((long) signum(d), 0);
        }
        if (Double.isNaN(d)) {
            return valueOf(0, 1); // no way to express a NaN :-(
        }
        return toRational(d, Integer.MAX_VALUE, 100);
    }

    public static Rational valueOf(long num, long den) {
        return valueOf(num, den, true);
    }

    public static Rational valueOf(long num) {
        return valueOf(num, 1, false);
    }

    private static Rational valueOf(long num, long den, boolean reduceFraction) {
        if (num == den) {
            return ONE;
        }
        if (num == 0) {
            return ZERO;
        }
        return new Rational(num, den, reduceFraction);
    }

    public static Rational valueOf(BigInteger num) {
        return valueOf(num, BigInteger.ONE, false);
    }

    public static Rational valueOf(BigInteger num, BigInteger den) {
        return valueOf(num, den, true);
    }

    private static Rational valueOf(BigInteger num, BigInteger den, boolean reduceFraction) {
        if (num.equals(den)) {
            return ONE;
        }
        if (num.equals(BigInteger.ZERO)) {
            return ZERO;
        }
        return new Rational(num, den, reduceFraction);
    }

    /**
     * Iteratively computes rational from double.
     * <p>Reference:
     * <br> <a
     * href="http://www2.fz-juelich.de/video/cpp/html/exercises/exercise/Rational_cpp.html">
     * http://www2.fz-juelich.de/video/cpp/html/exercises/exercise/Rational_cpp.html</a>
     * </p>
     */
    private static Rational toRational(double x, double limit, int iterations) {
        double intpart = Math.floor(x);
        double fractpart = x - intpart;
        double d = 1.0 / fractpart;
        long left = (long) intpart;
        if (d > limit || iterations == 0) {
            return valueOf(left, 1, false);
        } else {
            return valueOf(left, 1, false).add(toRational(d, limit * 0.1, iterations - 1).inverse(), false);
        }
    }

    public Rational round(long d) {
        if (d == den) {
            return valueOf(num, den);
        }

        Rational fl = floor(d);
        Rational diffFl = subtract(fl);

        if (diffFl.isZero()) {
            return fl;
        }

        Rational cl = ceil(d);
        Rational diffCl = subtract(cl);
        if (diffCl.isZero()) {
            return cl;
        }

        if (diffFl.isNegative()) {
            diffFl = diffFl.negate();
        }
        if (diffCl.isNegative()) {
            diffCl = diffCl.negate();
        }
        return diffFl.compareTo(diffCl) <= 0 ? fl : cl;
    }

    private boolean isNegative() {
        return num < 0;
    }

    /**
     * Parses a string.
     * <p>
     * A rational can be represented in the following ways:
     * <ul><li>As a long
     * number</li> <li>As a double number</li> <li>As an integer/integer
     * rational number</li></ul>
     *
     * @throws NumberFormatException if str can not be parsed.
     */
    public static Rational valueOf(String str) {
        int p = str.indexOf('/');
        if (p != -1) {
            return valueOf(Long.valueOf(str.substring(0, p)), Long.valueOf(str.substring(p + 1)));
        }
        try {
            return valueOf(Long.valueOf(str));
        } catch (NumberFormatException e) {
            return valueOf(Double.valueOf(str));
        }
    }
}

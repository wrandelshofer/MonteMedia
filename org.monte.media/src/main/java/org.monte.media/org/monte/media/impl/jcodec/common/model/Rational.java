package org.monte.media.impl.jcodec.common.model;

import org.monte.media.impl.jcodec.common.tools.MathUtil;

import static java.lang.Integer.parseInt;
import static org.monte.media.impl.jcodec.common.model.RationalLarge.reduceLong;


/**
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * @author The JCodec project
 */
public class Rational {

    public static final Rational ONE = new Rational(1, 1);
    public static final Rational HALF = new Rational(1, 2);
    public static final Rational ZERO = new Rational(0, 1);
    public final int num;
    public final int den;

    public static Rational R(int num, int den) {
        return new Rational(num, den);
    }

    public static Rational R1(int num) {
        return R(num, 1);
    }

    public Rational(int num, int den) {
        this.num = num;
        this.den = den;
    }

    public int getNum() {
        return num;
    }

    public int getDen() {
        return den;
    }

    public static Rational parseRational(String string) {
        return parse(string);
    }

    public static Rational parse(String string) {
        int idx = string.indexOf(":");
        if (idx < 0) {
            idx = string.indexOf("/");
        }
        if (idx > 0) {
            String num = string.substring(0, idx);
            String den = string.substring(idx + 1);
            return new Rational(parseInt(num), parseInt(den));
        }
        return R(parseInt(string), 1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + den;
        result = prime * result + num;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Rational other = (Rational) obj;
        if (den != other.den)
            return false;
        if (num != other.num)
            return false;
        return true;
    }

    public int multiplyS(int val) {
        return (int) (((long) num * val) / den);
    }

    public int divideS(int val) {
        return (int) (((long) den * val) / num);
    }

    public int divideByS(int val) {
        return num / (den * val);
    }

    public long multiplyLong(long val) {
        return (num * val) / den;
    }

    public long divideLong(long val) {
        return (den * val) / num;
    }

    public Rational flip() {
        return new Rational(den, num);
    }

    public boolean smallerThen(Rational sec) {
        return num * sec.den < sec.num * den;
    }

    public boolean greaterThen(Rational sec) {
        return num * sec.den > sec.num * den;
    }

    public boolean smallerOrEqualTo(Rational sec) {
        return num * sec.den <= sec.num * den;
    }

    public boolean greaterOrEqualTo(Rational sec) {
        return num * sec.den >= sec.num * den;
    }

    public boolean equalsRational(Rational other) {
        return num * other.den == other.num * den;
    }

    public Rational plus(Rational other) {
        return reduce(num * other.den + other.num * den, den * other.den);
    }

    public RationalLarge plusLarge(RationalLarge other) {
        return reduceLong(num * other.den + other.num * den, den * other.den);
    }

    public Rational minus(Rational other) {
        return reduce(num * other.den - other.num * den, den * other.den);
    }

    public RationalLarge minusLarge(RationalLarge other) {
        return reduceLong(num * other.den - other.num * den, den * other.den);
    }

    public Rational plusInt(int scalar) {
        return new Rational(num + scalar * den, den);
    }

    public Rational minusInt(int scalar) {
        return new Rational(num - scalar * den, den);
    }

    public Rational multiplyInt(int scalar) {
        return new Rational(num * scalar, den);
    }

    public Rational divideInt(int scalar) {
        return new Rational(den * scalar, num);
    }

    public Rational divideByInt(int scalar) {
        return new Rational(num, den * scalar);
    }

    public Rational multiply(Rational other) {
        return reduce(num * other.num, den * other.den);
    }

    public RationalLarge multiplyLarge(RationalLarge other) {
        return reduceLong(num * other.num, den * other.den);
    }

    public Rational divide(Rational other) {
        return reduce(other.num * den, other.den * num);
    }

    public RationalLarge divideLarge(RationalLarge other) {
        return reduceLong(other.num * den, other.den * num);
    }

    public Rational divideBy(Rational other) {
        return reduce(num * other.den, den * other.num);
    }

    public RationalLarge divideByLarge(RationalLarge other) {
        return reduceLong(num * other.den, den * other.num);
    }

    public float scalar() {
        return (float) num / den;
    }

    public int scalarClip() {
        return num / den;
    }

    public static Rational reduce(int num, int den) {
        int gcd = MathUtil.gcd(num, den);
        return new Rational(num / gcd, den / gcd);
    }

    public static Rational reduceRational(Rational r) {
        return reduce(r.getNum(), r.getDen());
    }

    @Override
    public String toString() {
        return num + "/" + den;
    }

    public double toDouble() {
        return (double) num / den;
    }
}

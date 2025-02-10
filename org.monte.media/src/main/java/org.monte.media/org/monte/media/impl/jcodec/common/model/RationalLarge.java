package org.monte.media.impl.jcodec.common.model;

import org.monte.media.impl.jcodec.common.tools.MathUtil;

import static org.monte.media.impl.jcodec.common.StringUtils.splitS;

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
public class RationalLarge {

    public static final RationalLarge ONE = new RationalLarge(1, 1);
    public static final RationalLarge HALF = new RationalLarge(1, 2);
    public static final RationalLarge ZERO = new RationalLarge(0, 1);

    final long num;
    final long den;

    public RationalLarge(long num, long den) {
        this.num = num;
        this.den = den;
    }

    public long getNum() {
        return num;
    }

    public long getDen() {
        return den;
    }

    public static RationalLarge parse(String string) {
        String[] split = splitS(string, ":");
        return split.length > 1 ? RationalLarge.R(Long.parseLong(split[0]), Long.parseLong(split[1])) : RationalLarge
                .R(Long.parseLong(string), 1);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (den ^ (den >>> 32));
        result = prime * result + (int) (num ^ (num >>> 32));
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
        RationalLarge other = (RationalLarge) obj;
        if (den != other.den)
            return false;
        if (num != other.num)
            return false;
        return true;
    }

    public long multiplyS(long scalar) {
        return (num * scalar) / den;
    }

    public long divideS(long scalar) {
        return (den * scalar) / num;
    }

    public long divideByS(long scalar) {
        return num / (den * scalar);
    }

    public RationalLarge flip() {
        return new RationalLarge(den, num);
    }

    public static RationalLarge R(long num, long den) {
        return new RationalLarge(num, den);
    }

    public static RationalLarge R1(long num) {
        return R(num, 1);
    }

    public boolean lessThen(RationalLarge sec) {
        return num * sec.den < sec.num * den;
    }

    public boolean greaterThen(RationalLarge sec) {
        return num * sec.den > sec.num * den;
    }

    public boolean smallerOrEqualTo(RationalLarge sec) {
        return num * sec.den <= sec.num * den;
    }

    public boolean greaterOrEqualTo(RationalLarge sec) {
        return num * sec.den >= sec.num * den;
    }

    public boolean equalsLarge(RationalLarge other) {
        return num * other.den == other.num * den;
    }

    public RationalLarge plus(RationalLarge other) {
        return reduceLong(num * other.den + other.num * den, den * other.den);
    }

    public RationalLarge plusR(Rational other) {
        return reduceLong(num * other.den + other.num * den, den * other.den);
    }

    public RationalLarge minus(RationalLarge other) {
        return reduceLong(num * other.den - other.num * den, den * other.den);
    }

    public RationalLarge minusR(Rational other) {
        return reduceLong(num * other.den - other.num * den, den * other.den);
    }

    public RationalLarge plusLong(long scalar) {
        return new RationalLarge(num + scalar * den, den);
    }

    public RationalLarge minusLong(long scalar) {
        return new RationalLarge(num - scalar * den, den);
    }

    public RationalLarge multiplyLong(long scalar) {
        return new RationalLarge(num * scalar, den);
    }

    public RationalLarge divideLong(long scalar) {
        return new RationalLarge(den * scalar, num);
    }

    public RationalLarge divideByLong(long scalar) {
        return new RationalLarge(num, den * scalar);
    }

    public RationalLarge multiply(RationalLarge other) {
        return reduceLong(num * other.num, den * other.den);
    }

    public RationalLarge multiplyR(Rational other) {
        return reduceLong(num * other.num, den * other.den);
    }

    public RationalLarge divideRL(RationalLarge other) {
        return reduceLong(other.num * den, other.den * num);
    }

    public RationalLarge divideR(Rational other) {
        return reduceLong(other.num * den, other.den * num);
    }

    public RationalLarge divideBy(RationalLarge other) {
        return reduceLong(num * other.den, den * other.num);
    }

    public RationalLarge divideByR(Rational other) {
        return reduceLong(num * other.den, den * other.num);
    }

    public double scalar() {
        return ((double) num) / den;
    }

    public long scalarClip() {
        return num / den;
    }

    @Override
    public String toString() {
        return num + ":" + den;
    }

    public static RationalLarge reduceLong(long num, long den) {
        long gcd = MathUtil.gcdLong(num, den);
        return new RationalLarge(num / gcd, den / gcd);
    }
}

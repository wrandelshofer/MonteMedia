/*
 * @(#)IntGcdJmh.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.math;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * Benchmark                    Mode  Cnt     Score     Error  Units
 * BigIntegerGcdJmh.gcdCheck    avgt    4  1559.873 ±  95.787  ns/op
 * BigIntegerGcdJmh.gcdHermann  avgt    4   325.661 ± 144.705  ns/op
 * BigIntegerGcdJmh.gcdStein1   avgt    4   579.999 ±  87.601  ns/op
 * BigIntegerGcdJmh.gcdStein2   avgt    4   501.797 ±  10.737  ns/op
 * </pre>
 */
@Measurement(iterations = 4)
@Warmup(iterations = 2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@Fork(value = 1)
public class BigIntegerGcdJmh {
    private BigInteger[] a;
    private BigInteger[] b;
    private int index;

    public static void main(String[] args) throws RunnerException {
        //BigInteger actual=gcdStein2(BigInteger.valueOf(367151),BigInteger.valueOf(43242));

        Options options = new OptionsBuilder()
                .include(BigIntegerGcdJmh.class.getSimpleName())
                .build();
        new Runner(options).run();

    }

    @Setup(Level.Trial)
    public void setUp() {
        Random rng = new Random();
        a = new BigInteger[2023];
        b = new BigInteger[2023];
        for (int i = 0; i < a.length; i++) {
            a[i] = BigInteger.valueOf(rng.nextInt(1_000_000));
            b[i] = BigInteger.valueOf(rng.nextInt(1_000_000));
        }
    }

    @Benchmark
    public void gcdCheck() {
        index = index + 1;
        if (index >= a.length) index = 0;
        BigInteger ai = this.a[index];
        BigInteger bi = b[index];
        BigInteger gcdH = gcdHermann(ai, bi);
        BigInteger gcdS1 = gcdStein1(ai, bi);
        BigInteger gcdS2 = gcdStein2(ai, bi);
        if (!gcdH.equals(gcdS1) || !gcdH.equals(gcdS2)) {
            throw new RuntimeException("gcds(" + ai + "," + bi + ") disagree " + gcdH + " " + gcdS1 + " " + gcdS2);
        }
    }

    @Benchmark
    public BigInteger gcdStein1() {
        index = index + 1;
        if (index >= a.length) index = 0;
        return gcdStein1(a[index], b[index]);
    }

    @Benchmark
    public BigInteger gcdStein2() {
        index = index + 1;
        if (index >= a.length) index = 0;
        return gcdStein2(a[index], b[index]);
    }

    @Benchmark
    public BigInteger gcdHermann() {
        index = index + 1;
        if (index >= a.length) index = 0;
        return gcdHermann(a[index], b[index]);
    }

    /**
     * References:
     * <dl>
     *     <dt>Herrmann, D. (1992). Algorithmen Arbeitsbuch</dt>
     *     <dd>Bonn, München Paris: Addison Wesley.
     *         ggt6, Page 63</dd>
     * </dl>
     */
    static BigInteger gcdHermann(BigInteger a, BigInteger b) {
        a = a.abs();
        b = b.abs();

        while (a.compareTo(BigInteger.ZERO) > 0 && b.compareTo(BigInteger.ZERO) > 0) {
            a = a.mod(b);
            if (a.compareTo(BigInteger.ZERO) > 0) b = b.mod(a);
        }
        return a.add(b);
    }

    /**
     * References:
     * <dl>
     *     <dt>Stein</dt>
     *     <dd>
     *         <a href="https://www.geeksforgeeks.org/steins-algorithm-for-finding-gcd/"geeksforgeeks.org></a>
     *             </dd>
     * </dl>
     */
    static BigInteger gcdStein1(BigInteger a, BigInteger b) {
        // GCD(0, b) == b; GCD(a, 0) == a,
        // GCD(0, 0) == 0
        if (a.equals(BigInteger.ZERO))
            return b;
        if (b.equals(BigInteger.ZERO))
            return a;

        // Finding K, where K is the greatest
        // power of 2 that divides both a and b
        int k;
        for (k = 0; !a.testBit(0) && !b.testBit(0); ++k) {
            a = a.shiftRight(1);
            b = b.shiftRight(1);
        }

        // Dividing a by 2 until a becomes odd
        while (!a.testBit(0))
            a = a.shiftRight(1);

        // From here on, 'a' is always odd.
        do {
            // If b is even, remove
            // all factor of 2 in b
            while (!b.testBit(0))
                b = b.shiftRight(1);

            // Now a and b are both odd. Swap
            // if necessary so a <= b, then set
            // b = b - a (which is even)
            if (a.compareTo(b) > 0) {
                // Swap a and b.
                BigInteger temp = a;
                a = b;
                b = temp;
            }

            b = (b.subtract(a));
        } while (b.compareTo(BigInteger.ZERO) != 0);

        // restore common factors of 2
        return a.shiftLeft(k);
    }

    /**
     * References:
     * <dl>
     *     <dt>Stein</dt>
     *     <dd>
     *         <a href="https://www.geeksforgeeks.org/steins-algorithm-for-finding-gcd/"geeksforgeeks.org></a>
     *             </dd>
     * </dl>
     */
    static BigInteger gcdStein2(BigInteger a, BigInteger b) {
        // GCD(0, b) == b; GCD(a, 0) == a,
        // GCD(0, 0) == 0
        if (a.equals(BigInteger.ZERO))
            return b;
        if (b.equals(BigInteger.ZERO))
            return a;

        // Finding K, where K is the greatest
        // power of 2 that divides both a and b
        int k = Math.min(numberOfTrailingZeros(a), numberOfTrailingZeros(b));
        a = a.shiftRight(k);
        b = b.shiftRight(k);

        // Dividing a by 2 until a becomes odd
        a = a.shiftRight(numberOfTrailingZeros(a));

        // From here on, 'a' is always odd.
        do {
            // If b is even, remove
            // all factor of 2 in b
            b = b.shiftRight(numberOfTrailingZeros(b));

            // Now a and b are both odd. Swap
            // if necessary so a <= b, then set
            // b = b - a (which is even)
            if (a.compareTo(b) > 0) {
                // Swap a and b.
                BigInteger temp = a;
                a = b;
                b = temp;
            }

            b = (b.subtract(a));
        } while (b.compareTo(BigInteger.ZERO) != 0);

        // restore common factors of 2
        return a.shiftLeft(k);
    }

    private static int numberOfTrailingZeros(BigInteger a) {
        int k = 0;
        while (!a.testBit(k)) {
            k++;
        }
        return k;
    }

}
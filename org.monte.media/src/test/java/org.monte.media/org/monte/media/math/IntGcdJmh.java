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

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * Benchmark             Mode  Cnt    Score   Error  Units
 * IntGcdJmh.gcdCheck    avgt    4  245.136 ± 0.163  ns/op
 * IntGcdJmh.gcdHermann  avgt    4   82.966 ± 0.120  ns/op
 * IntGcdJmh.gcdStein1   avgt    4  115.451 ± 5.423  ns/op
 * IntGcdJmh.gcdStein2   avgt    4   52.087 ± 0.167  ns/op
 * </pre>
 */
@Measurement(iterations = 4)
@Warmup(iterations = 2)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
@Fork(value = 1)
public class IntGcdJmh {
    private int[] a;
    private int[] b;
    private int index;

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
                .include(IntGcdJmh.class.getSimpleName())
                .build();
        new Runner(options).run();
    }

    @Setup(Level.Trial)
    public void setUp() {
        Random rng = new Random();
        a = new int[2023];
        b = new int[2023];
        for (int i = 0; i < a.length; i++) {
            a[i] = rng.nextInt(1_000_000);
            b[i] = rng.nextInt(1_000_000);
        }
    }

    @Benchmark
    public void gcdCheck() {
        index = index + 1;
        if (index >= a.length) index = 0;
        int ai = this.a[index];
        int bi = b[index];
        int gcdH = gcdHermann(ai, bi);
        int gcdS1 = gcdStein1(ai, bi);
        int gcdS2 = gcdStein2(ai, bi);
        if (gcdH != gcdS1 || gcdH != gcdS2) {
            throw new RuntimeException("gcds(" + ai + "," + bi + ") disagree " + gcdH + " " + gcdS1 + " " + gcdS2);
        }
    }

    @Benchmark
    public int gcdStein1() {
        index = index + 1;
        if (index >= a.length) index = 0;
        return gcdStein1(a[index], b[index]);
    }

    @Benchmark
    public int gcdStein2() {
        index = index + 1;
        if (index >= a.length) index = 0;
        return gcdStein2(a[index], b[index]);
    }

    @Benchmark
    public int gcdHermann() {
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
    static int gcdHermann(int a, int b) {
        a = Math.abs(a);
        b = Math.abs(b);

        while (a > 0 && b > 0) {
            a = a % b;
            if (a > 0) b = b % a;
        }
        return a + b;
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
    static int gcdStein1(int a, int b) {
        // GCD(0, b) == b; GCD(a, 0) == a,
        // GCD(0, 0) == 0
        if (a == 0)
            return b;
        if (b == 0)
            return a;

        // Finding K, where K is the greatest
        // power of 2 that divides both a and b
        int k;
        for (k = 0; ((a | b) & 1) == 0; ++k) {
            a >>>= 1;
            b >>>= 1;
        }

        // Dividing a by 2 until a becomes odd
        while ((a & 1) == 0)
            a >>>= 1;

        // From here on, 'a' is always odd.
        do {
            // If b is even, remove
            // all factor of 2 in b
            while ((b & 1) == 0)
                b >>>= 1;

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
     * References:
     * <dl>
     *     <dt>Stein</dt>
     *     <dd>
     *         <a href="https://www.geeksforgeeks.org/steins-algorithm-for-finding-gcd/"geeksforgeeks.org></a>
     *             </dd>
     * </dl>
     */
    static int gcdStein2(int a, int b) {
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

}
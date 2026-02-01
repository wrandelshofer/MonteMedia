/*
 * @(#)Debug.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.h264.impl.jcodec.common.tools;

import org.monte.media.h264.impl.jcodec.common.ArrayUtil;

import java.nio.ShortBuffer;

/// References:
///
/// JCodecProject. Copyright 2008-2019 JCodecProject.
/// : [BSD 2-Clause License.](https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE)
/// : [github.com](https://github.com/jcodec/jcodec)
///
/// @author The JCodec project
public class Debug {
    public final static void print8x8i(int[] output) {
        int i = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                System.out.printf("%3d, ", output[i]);
                i++;
            }
            System.out.println();
        }
    }

    public final static void print8x8s(short[] output) {
        int i = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                System.out.printf("%3d, ", output[i]);
                i++;
            }
            System.out.println();
        }
    }

    public final static void print8x8sb(ShortBuffer output) {
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                System.out.printf("%3d, ", output.get());
            }
            System.out.println();
        }
    }

    public static void prints(short[] table) {
        int i = 0;
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                System.out.printf("%3d, ", table[i]);
                i++;
            }
            System.out.println();
        }
    }

    public static void trace(Object... arguments) {
        if (debug && arguments.length > 0) {
            String format = (String) arguments[0];
            ArrayUtil.shiftLeft1(arguments);
            System.out.printf(format + ": %d\n", arguments);
        }
    }

    public static boolean debug = false;

    public static void printInt(int i) {
        if (debug)
            System.out.print(i);
    }

    public static void print(String string) {
        if (debug)
            System.out.print(string);
    }

    public static void println(String string) {
        if (debug)
            System.out.println(string);
    }
}

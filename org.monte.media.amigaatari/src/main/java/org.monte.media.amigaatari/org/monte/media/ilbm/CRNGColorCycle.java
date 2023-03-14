/*
 * @(#)CRNGColorCycle.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.ilbm;

/**
 * Implements CRNG and CCRT color cycling for an IFF ILBM image.
 * <p>
 * This class supports CRNG and CCRT color cycling as published in
 * AMIGA ROM Kernel Reference Manual: Devices,
 * Third Edition,
 * Addison-Wesley, Reading
 * ISBN 0-201-56775-X
 *
 * <pre>
 * //ILBM CRNG Color range cycling
 * //--------------------------------------------
 *
 * #define RNG_NORATE  36   // Dpaint uses this rate to mean non-active
 *  set {
 *  active = 1, reverse = 2
 *  } crngActive;
 *
 *  // A CRange is store in a CRNG chunk.
 *  typedef struct {
 *  WORD  pad1;              // reserved for future use; store 0 here *
 *  WORD  rate;              // 60/sec=16384, 30/sec=8192, 1/sec=16384/60=273
 *  WORD set crngActive flags;     // bit0 set = active, bit 1 set = reverse
 *  UBYTE low; UBYTE high;         // lower and upper color registers selected
 *  } ilbmColorRegisterRangeChunk;
 * </pre>
 *
 * <pre>
 * ILBM CCRT Color cycling range and timing
 * --------------------------------------------
 * /
 * enum {
 * dontCycle = 0, forward = 1, backwards = -1
 * } ccrtDirection;
 * typedef struct {
 * WORD enum ccrtDirection direction;  /* 0=don't cycle, 1=forward, -1=backwards * /
 * UBYTE start;      /* range lower * /
 * UBYTE end;        /* range upper * /
 * LONG  seconds;    /* seconds between cycling * /
 * LONG  microseconds; /* msecs between cycling * /
 * WORD  pad;        /* future exp - store 0 here * /
 * } ilbmColorCyclingAndTimingChunk;
 *
 * </pre>
 *
 * @author Werner Randelshofer
 */
public class CRNGColorCycle extends ColorCycle {

    /**
     * Lowest color register of the range.
     */
    private int low;
    /**
     * Highest color register of the range.
     */
    private int high;
    /**
     * Whether the color cycle is reverse.
     */
    private boolean isReverse;
    /**
     * Whether the image is in EHB mode.
     */
    private boolean isEHB;

    public CRNGColorCycle(int rate, int timeScale, int low, int high, boolean isActive, boolean isReverse, boolean isEHB) {
        super(rate, timeScale, isActive);
        this.low = low;
        this.high = high;
        this.isReverse = isReverse;
        this.isEHB = isEHB;
    }

    public boolean isReverse() {
        return isReverse;
    }

    public int getLow() {
        return low;
    }

    public int getHigh() {
        return high;
    }

    @Override
    public void doCycle(int[] rgbs, long time) {
        if (isBlended) {
            doBlendedCycle(rgbs, time);
        } else {
            doHardCycle(rgbs, time);
        }
    }

    public void doBlendedCycle(int[] rgbs, long time) {
        if (isActive) {
            doHardCycle(rgbs, time);
            double blendf = Math.IEEEremainder((time * rate / timeScale / 1000f), high - low + 1);
            blendf = blendf - Math.floor(blendf);
            int blend = 255 - (int) (blendf * 255);
            if (isReverse) {
                {
                    blend = 255 - blend;
                    int tmp = rgbs[high];
                    for (int i = high; i > low; i--) {
                        rgbs[i] = ((((rgbs[i] & 0xff) * blend + (rgbs[i - 1] & 0xff) * (255 - blend)) >> 8) & 0xff)
                                | ((((rgbs[i] & 0xff00) * blend + (rgbs[i - 1] & 0xff00) * (255 - blend)) >> 8) & 0xff00)
                                | ((((rgbs[i] & 0xff0000) * blend + (rgbs[i - 1] & 0xff0000) * (255 - blend)) >> 8) & 0xff0000);
                    }
                    rgbs[low] = ((((rgbs[low] & 0xff) * blend + (tmp & 0xff) * (255 - blend)) >> 8) & 0xff)
                            | ((((rgbs[low] & 0xff00) * blend + (tmp & 0xff00) * (255 - blend)) >> 8) & 0xff00)
                            | ((((rgbs[low] & 0xff0000) * blend + (tmp & 0xff0000) * (255 - blend)) >> 8) & 0xff0000);
                    ;
                }
                if (isEHB) {
                    // TO DO
                }
            } else {
                {
                    int tmp = rgbs[high];
                    for (int i = high; i > low; i--) {
                        rgbs[i] = ((((rgbs[i] & 0xff) * blend + (rgbs[i - 1] & 0xff) * (255 - blend)) >> 8) & 0xff)
                                | ((((rgbs[i] & 0xff00) * blend + (rgbs[i - 1] & 0xff00) * (255 - blend)) >> 8) & 0xff00)
                                | ((((rgbs[i] & 0xff0000) * blend + (rgbs[i - 1] & 0xff0000) * (255 - blend)) >> 8) & 0xff0000);
                    }
                    rgbs[low] = ((((rgbs[low] & 0xff) * blend + (tmp & 0xff) * (255 - blend)) >> 8) & 0xff)
                            | ((((rgbs[low] & 0xff00) * blend + (tmp & 0xff00) * (255 - blend)) >> 8) & 0xff00)
                            | ((((rgbs[low] & 0xff0000) * blend + (tmp & 0xff0000) * (255 - blend)) >> 8) & 0xff0000);
                    ;
                }
                if (isEHB) {
                    // TO DO
                }
            }
        }
    }

    public void doHardCycle(int[] rgbs, long time) {
        if (isActive) {

            int shift = (int) ((time * rate / timeScale / 1000) % (high - low + 1));
            if (isReverse) {
                for (int j = 0; j < shift; j++) {
                    int tmp = rgbs[low];
                    for (int i = low; i < high; i++) {
                        rgbs[i] = rgbs[i + 1];
                    }
                    rgbs[high] = tmp;
                }
                if (isEHB) {
                    for (int j = 0; j < shift; j++) {
                        int tmp = rgbs[low + 32];
                        for (int i = low + 32; i < high + 32; i++) {
                            rgbs[i] = rgbs[i + 1];
                        }
                        rgbs[high + 32] = tmp;
                    }
                }
            } else {
                for (int j = 0; j < shift; j++) {
                    int tmp = rgbs[high];
                    for (int i = high; i > low; i--) {
                        rgbs[i] = rgbs[i - 1];
                    }
                    rgbs[low] = tmp;
                }
                if (isEHB) {
                    for (int j = 0; j < shift; j++) {
                        int tmp = rgbs[high + 32];
                        for (int i = high + 32; i > low + 32; i--) {
                            rgbs[i] = rgbs[i - 1];
                        }
                        rgbs[low + 32] = tmp;
                    }
                }
            }
        }
    }
}

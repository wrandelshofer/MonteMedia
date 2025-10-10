package org.monte.media.impl.jcodec.codecs.h264.decode.aso;

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
 * <p>
 * A block map that that maps macroblocks sequentially in scan order
 *
 * @author The JCodec project
 */
public class FlatMBlockMapper implements Mapper {
    private int frameWidthInMbs;
    private int firstMBAddr;

    public FlatMBlockMapper(int frameWidthInMbs, int firstMBAddr) {
        this.frameWidthInMbs = frameWidthInMbs;
        this.firstMBAddr = firstMBAddr;
    }

    public boolean leftAvailable(int index) {
        int mbAddr = index + firstMBAddr;
        boolean atTheBorder = mbAddr % frameWidthInMbs == 0;
        return !atTheBorder && (mbAddr > firstMBAddr);
    }

    public boolean topAvailable(int index) {
        int mbAddr = index + firstMBAddr;
        return mbAddr - frameWidthInMbs >= firstMBAddr;
    }

    public int getAddress(int index) {
        return firstMBAddr + index;
    }

    public int getMbX(int index) {
        return getAddress(index) % frameWidthInMbs;
    }

    public int getMbY(int index) {
        return getAddress(index) / frameWidthInMbs;
    }

    public boolean topRightAvailable(int index) {
        int mbAddr = index + firstMBAddr;
        boolean atTheBorder = (mbAddr + 1) % frameWidthInMbs == 0;
        return !atTheBorder && mbAddr - frameWidthInMbs + 1 >= firstMBAddr;
    }

    public boolean topLeftAvailable(int index) {
        int mbAddr = index + firstMBAddr;
        boolean atTheBorder = mbAddr % frameWidthInMbs == 0;
        return !atTheBorder && mbAddr - frameWidthInMbs - 1 >= firstMBAddr;
    }
}

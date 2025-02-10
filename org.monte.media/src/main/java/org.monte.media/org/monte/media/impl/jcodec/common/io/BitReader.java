package org.monte.media.impl.jcodec.common.io;

import java.nio.ByteBuffer;

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
public class BitReader {
    public static BitReader createBitReader(ByteBuffer bb) {
        BitReader r = new BitReader(bb);
        r.curInt = r.readInt();
        r.deficit = 0;
        return r;
    }

    private int deficit = -1;
    private int curInt = -1;
    private ByteBuffer bb;
    private int initPos;

    private BitReader(ByteBuffer bb) {
        this.bb = bb;
        this.initPos = bb.position();
    }

    public BitReader fork() {
        BitReader fork = new BitReader(this.bb.duplicate());
        fork.initPos = 0;
        fork.curInt = this.curInt;
        fork.deficit = this.deficit;
        return fork;
    }

    public final int readInt() {
        if (((java.nio.Buffer) bb).remaining() >= 4) {
            deficit -= 32;
            return ((bb.get() & 0xff) << 24) | ((bb.get() & 0xff) << 16) | ((bb.get() & 0xff) << 8) | (bb.get() & 0xff);
        } else
            return readIntSafe();
    }

    private int readIntSafe() {
        deficit -= (((java.nio.Buffer) bb).remaining() << 3);
        int res = 0;
        if (bb.hasRemaining())
            res |= bb.get() & 0xff;
        res <<= 8;
        if (bb.hasRemaining())
            res |= bb.get() & 0xff;
        res <<= 8;
        if (bb.hasRemaining())
            res |= bb.get() & 0xff;
        res <<= 8;
        if (bb.hasRemaining())
            res |= bb.get() & 0xff;
        return res;
    }

    public int read1Bit() {

        int ret = curInt >>> 31;
        curInt <<= 1;
        ++deficit;
        if (deficit == 32) {
            curInt = readInt();
        }
        // System.out.println(ret);

        return ret;
    }

    public int readNBitSigned(int n) {
        int v = readNBit(n);
        return read1Bit() == 0 ? v : -v;
    }

    public int readNBit(int n) {
        if (n > 32)
            throw new IllegalArgumentException("Can not read more then 32 bit");

        int ret = 0;
        if (n + deficit > 31) {
            ret |= (curInt >>> deficit);
            n -= 32 - deficit;
            ret <<= n;
            deficit = 32;
            curInt = readInt();
        }

        if (n != 0) {
            ret |= curInt >>> (32 - n);
            curInt <<= n;
            deficit += n;
        }

        return ret;
    }

    public boolean moreData() {
        int remaining = ((java.nio.Buffer) bb).remaining() + 4 - ((deficit + 7) >> 3);
        return remaining > 1 || (remaining == 1 && curInt != 0);
    }

    public int remaining() {
        return (((java.nio.Buffer) bb).remaining() << 3) + 32 - deficit;
    }

    public final boolean isByteAligned() {
        return (deficit & 0x7) == 0;
    }

    public int skip(int bits) {
        int left = bits;

        if (left + deficit > 31) {
            left -= 32 - deficit;
            deficit = 32;
            if (left > 31) {
                int skip = Math.min(left >> 3, ((java.nio.Buffer) bb).remaining());
                ((java.nio.Buffer) bb).position(((java.nio.Buffer) bb).position() + skip);
                left -= skip << 3;
            }
            curInt = readInt();
        }

        deficit += left;
        curInt <<= left;

        return bits;
    }

    public int skipFast(int bits) {
        deficit += bits;
        curInt <<= bits;

        return bits;
    }

    public int bitsToAlign() {
        return (deficit & 0x7) > 0 ? 8 - (deficit & 0x7) : 0;
    }

    public int align() {
        return (deficit & 0x7) > 0 ? skip(8 - (deficit & 0x7)) : 0;
    }

    public int check24Bits() {
        if (deficit > 16) {
            deficit -= 16;
            curInt |= nextIgnore16() << deficit;
        }

        if (deficit > 8) {
            deficit -= 8;
            curInt |= nextIgnore() << deficit;
        }

        return curInt >>> 8;
    }

    public int check16Bits() {
        if (deficit > 16) {
            deficit -= 16;
            curInt |= nextIgnore16() << deficit;
        }
        return curInt >>> 16;
    }

    public int readFast16(int n) {
        if (n == 0)
            return 0;
        if (deficit > 16) {
            deficit -= 16;
            curInt |= nextIgnore16() << deficit;
        }

        int ret = curInt >>> (32 - n);
        deficit += n;
        curInt <<= n;

        return ret;
    }

    public int checkNBit(int n) {
        if (n > 24) {
            throw new IllegalArgumentException("Can not check more then 24 bit");
        }

        return checkNBitDontCare(n);
    }

    public int checkNBitDontCare(int n) {
        while (deficit + n > 32) {
            deficit -= 8;
            curInt |= nextIgnore() << deficit;
        }
        int res = curInt >>> (32 - n);
        return res;
    }

    private int nextIgnore16() {
        return ((java.nio.Buffer) bb).remaining() > 1 ? bb.getShort() & 0xffff : (((java.nio.Buffer) bb).hasRemaining() ? ((bb.get() & 0xff) << 8) : 0);
    }

    private int nextIgnore() {
        return ((java.nio.Buffer) bb).hasRemaining() ? bb.get() & 0xff : 0;
    }

    public int curBit() {
        return deficit & 0x7;
    }

    public boolean lastByte() {
        return ((java.nio.Buffer) bb).remaining() + 4 - (deficit >> 3) <= 1;
    }

    public void terminate() {
        int putBack = (32 - deficit) >> 3;
        ((java.nio.Buffer) bb).position(((java.nio.Buffer) bb).position() - putBack);
    }

    public int position() {
        return ((((java.nio.Buffer) bb).position() - initPos - 4) << 3) + deficit;
    }

    /**
     * Stops this bit reader. Returns underlying ByteBuffer pointer to the next
     * byte unread byte
     */
    public void stop() {
        ((java.nio.Buffer) bb).position(((java.nio.Buffer) bb).position() - ((32 - deficit) >> 3));
    }

    public int checkAllBits() {
        return curInt;
    }

    public boolean readBool() {
        return read1Bit() == 1;
    }
}
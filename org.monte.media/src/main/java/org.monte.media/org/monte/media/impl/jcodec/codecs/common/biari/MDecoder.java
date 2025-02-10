package org.monte.media.impl.jcodec.codecs.common.biari;

import java.io.IOException;
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
 * <p>
 * H264 CABAC M-Coder ( decoder module )
 *
 * @author The JCodec project
 */
public class MDecoder {

    private ByteBuffer _in;
    private int range;
    private int code;
    private int nBitsPending;
    private int[][] cm;

    public MDecoder(ByteBuffer _in, int[][] cm) {
        this._in = _in;
        this.range = 510;
        this.cm = cm;

        initCodeRegister();
    }

    /**
     * Initializes code register. Loads 9 bits from the stream into working area
     * of code register ( bits 8 - 16) leaving 7 bits in the pending area of
     * code register (bits 0 - 7)
     *
     * @throws IOException
     */
    protected void initCodeRegister() {
        readOneByte();
        if (nBitsPending != 8)
            throw new RuntimeException("Empty stream");
        code <<= 8;
        readOneByte();
        code <<= 1;
        nBitsPending -= 9;
    }

    protected void readOneByte() {
        if (!_in.hasRemaining())
            return;
        int b = _in.get() & 0xff;
        code |= b;
        nBitsPending += 8;
    }

    /**
     * Decodes one bin from arithmetice code word
     * <p>
     * //@param cm
     *
     * @return
     * @throws IOException
     */
    public int decodeBin(int m) {
        int bin;

        int qIdx = (range >> 6) & 0x3;
        int rLPS = MConst.rangeLPS[qIdx][cm[0][m]];
        range -= rLPS;
        int rs8 = range << 8;

        if (code < rs8) {
            // MPS
            if (cm[0][m] < 62)
                cm[0][m]++;

            renormalize();

            bin = cm[1][m];
        } else {
            // LPS
            range = rLPS;
            code -= rs8;

            renormalize();

            bin = 1 - cm[1][m];

            if (cm[0][m] == 0)
                cm[1][m] = 1 - cm[1][m];

            cm[0][m] = MConst.transitLPS[cm[0][m]];
        }

//        System.out.println("CABAC BIT [" + m + "]: " + bin);
        return bin;
    }

    /**
     * Special decoding process for 'end of slice' flag. Uses probability state
     * 63.
     *
     * @param cm
     * @return
     * @throws IOException
     */
    public int decodeFinalBin() {
        range -= 2;

        if (code < (range << 8)) {
            renormalize();
//            System.out.println("CABAC BIT [-2]: 0");
            return 0;
        } else {
//            System.out.println("CABAC BIT [-2]: 1");
            return 1;
        }
    }

    /**
     * Special decoding process for symbols with uniform distribution
     *
     * @return
     * @throws IOException
     */
    public int decodeBinBypass() {
        code <<= 1;

        --nBitsPending;
        if (nBitsPending <= 0)
            readOneByte();

        int tmp = code - (range << 8);
        if (tmp < 0) {
//            System.out.println("CABAC BIT [-1]: 0");
            return 0;
        } else {
//            System.out.println("CABAC BIT [-1]: 1");
            code = tmp;
            return 1;
        }
    }

    /**
     * Shifts the current interval to either 1/2 or 0 (code = (code << 1) &
     * 0x1ffff) and scales it by 2 (range << 1).
     * <p>
     * Reads new byte from the input stream into code value if there are no more
     * bits pending
     *
     * @throws IOException
     */
    private void renormalize() {
        while (range < 256) {
            range <<= 1;
            code <<= 1;
            code &= 0x1ffff;

            --nBitsPending;
            if (nBitsPending <= 0)
                readOneByte();
        }
    }
}
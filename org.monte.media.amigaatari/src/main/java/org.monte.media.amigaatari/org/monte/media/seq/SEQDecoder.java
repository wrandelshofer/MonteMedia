/*
 * @(#)SEQDecoder.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.seq;

import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

/**
 * {@code SEQDecoder}.
 * <p>
 * References:<br>
 * <a href="http://www.fileformat.info/format/atari/egff.htm">http://www.fileformat.info/format/atari/egff.htm</a><br>
 * <a href="http://www.atari-forum.com/wiki/index.php/ST_Picture_Formats">http://www.atari-forum.com/wiki/index.php/ST_Picture_Formats</a>
 *
 * @author Werner Randelshofer
 */
public class SEQDecoder {

    private ImageInputStream in;
    /**
     * Number of frames.
     */
    private int nFrames;
    /**
     * Speed given in a timebase of 6000 nanoseconds.
     */
    private int speed;
    /**
     * Offsets of the video frames.
     */
    private long[] offsets;

    /**
     * The resolution. -1 if unknown.
     */
    private int resolution = -1;

    /**
     * The number of colors. -1 if unknown.
     */
    private int nColors = -1;

    /**
     * The movie track.
     */
    private SEQMovieTrack track;


    private boolean enforce8BitColorModel = true;

    /**
     * Creates a decoder for the specified input stream.
     */
    public SEQDecoder(InputStream in) {
        this.in = new MemoryCacheImageInputStream(in);
        this.in.setByteOrder(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Creates a decoder for the specified image input stream.
     */
    public SEQDecoder(ImageInputStream in) {
        this.in = in;
        this.in.setByteOrder(ByteOrder.BIG_ENDIAN);
    }

    /**
     * Decodes the stream and produces animation frames into the specified
     * movie track.
     * <p>
     * This method can only be called once.
     *
     * @param track     The decoded data is stored in this track.
     * @param loadAudio Whether to decode audio (currently unused).
     */
    public void produce(SEQMovieTrack track, boolean loadAudio) throws IOException {
        this.track = track;
        readHeader();
        readOffsets();
        readFrames();
    }

    public void setEnforce8BitColorModel(boolean b) {
        enforce8BitColorModel = b;
    }


    /**
     * Reads the SEQ Header. Assumes that the input stream is positioned
     * At the start of the file.
     * <pre>
     * // Seq Header. 128 bytes.
     * typedef struct {
     * ubyte[2] magicNumber;       // [$FEDB or $FEDC]
     * WORD version;           // version number
     * LONG numberOfFrames;        // number of frames
     * WORD speed;             // maybe given in a timebase of 6000 nanoseconds
     * ubyte[16] reserved[7];
     * ubyte[6] reserved;
     * } SeqHeader;
     * </pre>
     */
    private void readHeader() throws IOException {
        int magic = in.readUnsignedShort();
        if (magic != 0xfedb && magic != 0xfedc) {
            throw new IOException("SEQ Header: Invalid magic number 0x" + Integer.toHexString(magic) + ", expected 0xfedb or 0xfedc.");
        }
        int version = in.readUnsignedShort();
        if (version != 0) {
            throw new IOException("SEQ Header: Invalid version " + version + ", expected 0.");
        }
        long numberOfFrames = in.readUnsignedInt();
        if (numberOfFrames > Integer.MAX_VALUE) {
            throw new IOException("SEQ Header: Too many frames " + numberOfFrames + ", expected 0.");
        }
        nFrames = (int) numberOfFrames;
        speed = in.readUnsignedShort();

        track.setJiffies(6000); // timebase is 6000 nanoseconds
        //track.setPlayWrapupFrames(true);

        int skipped = in.skipBytes(118);
        if (skipped != 118) {
            throw new IOException("SEQ Header: Unexpected EOF.");
        }
    }

    /**
     * Reads the SEQ Offsets. Assumes that the input stream is positioned
     * at the beginning of the offsets and that the header has been read.
     * <pre>
     * typedef struct {
     *  ULONG offset;
     * } frofOffset;
     *
     * typedef struct {
     *  frofOffset[] frame;
     * } FrameOffsets;
     * </pre>
     */
    private void readOffsets() throws IOException {
        offsets = new long[nFrames];
        for (int i = 0; i < nFrames; i++) {
            offsets[i] = in.readUnsignedInt();
        }
    }

    /**
     * Reads the video frames. Assumes that the input stream is positioned
     * at the beginning of the frames and that the header and the offsets have
     * been read.
     */
    private void readFrames() throws IOException {
        for (int i = 0; i < nFrames; i++) {
            readFrame(i);
        }
    }

    /**
     * Reads a video frame. Assumes that the input stream is positioned
     * at the beginning of the frame and that the header and the offsets have
     * been read.
     * <pre>
     *    typedef struct {
     *    ubyte[2] type;              // (ignored?)
     *    WORD enum frhdResolution resolution;        // [always 0]
     *    frhdColor[16] palette;
     *    CHAR[12] filename;      // [usually "        .   "]
     *    ColorAnimation colorAnimation;
     *    WORD xOffset;           // x offset for this frame [0 - 319]
     *    WORD yOffset;           // y offset for this frame [0 - 199]
     *    WORD width;             // width of this frame, in pixels (may be 0, see below)
     *    WORD height;            // height of this frame, in pixels (may be 0, see below)
     *    UBYTE enum frhdOp operation;       // operation [0 = copy, 1 = exclusive or]
     *    UBYTE enum frhdMthd storageMethod; // storage method [0 = uncompressed, 1 = compressed]
     *    ULONG lengthOfData;      // length of data in bytes (if the data is compressed, this
     *    // will be the size of the compressed data BEFORE decompression)
     *    ubyte[16] reserved[3];
     *    ubyte[12] reserved;
     *  } FrameHeader;     * </pre>
     */
    private void readFrame(int i) throws IOException {
        // Type and Resolution
        // ===================
        int type = in.readUnsignedShort();
        if (type != 0xffff) {
            throw new IOException("Frame Header " + i + ": Invalid type " + type + ", expected 0xffff.");
        }
        int res = in.readUnsignedShort();
        if (res > 2) {
            throw new IOException("Frame Header " + i + ": Illegal resolution " + res + ", expected range [0,2].");
        }
        if (resolution == -1) {
            resolution = res;
            switch (res) {
                case 0:
                    track.setWidth(320);
                    track.setHeight(200);
                    track.setNbPlanes(4);
                    nColors = 16;
                    break;
                case 1:
                    track.setWidth(640);
                    track.setHeight(200);
                    track.setNbPlanes(2);
                    nColors = 4;
                    break;
                case 2:
                    track.setWidth(640);
                    track.setHeight(400);
                    track.setNbPlanes(1);
                    nColors = 2;
                    break;
            }
        }
        if (res != resolution) {
            throw new IOException("Frame Header " + i + ": Illegal resolution change " + res + ", expected " + resolution + ".");
        }

        // Palette
        // =============
        byte[] r = new byte[nColors];
        byte[] g = new byte[nColors];
        byte[] b = new byte[nColors];
        for (int j = 0; j < nColors; j++) {
            int clr = in.readUnsignedShort();
            int red = (clr & 0x700) >> 8;
            int green = (clr & 0x70) >> 4;
            int blue = (clr & 0x7);
            r[j] = (byte) ((red << 5) | (red << 2) | (red >>> 1));
            g[j] = (byte) ((green << 5) | (green << 2) | (green >>> 1));
            b[j] = (byte) ((blue << 5) | (blue << 2) | (blue >>> 1));
        }
        ColorModel cm = new IndexColorModel(enforce8BitColorModel ? 8 : 4, nColors, r, g, b);

        // Filename
        // =============
        if (in.skipBytes(12) != 12) {
            throw new IOException("Frame Header " + i + ": Unexpected EOF in filename.");
        }

        // Color cycling
        // =============
        // color animation flag
        if (in.skipBytes(1) != 1) {
            throw new IOException("Frame Header " + i + ": Unexpected EOF in color animation flag.");
        }
        // range start, range end
        if (in.skipBytes(1) != 1) {
            throw new IOException("Frame Header " + i + ": Unexpected EOF in color animation range.");
        }
        // active
        if (in.skipBytes(1) != 1) {
            throw new IOException("Frame Header " + i + ": Unexpected EOF in color animation activation flag.");
        }
        // speeddir
        if (in.skipBytes(1) != 1) {
            throw new IOException("Frame Header " + i + ": Unexpected EOF in color animation speeddir.");
        }
        // steps
        if (in.skipBytes(2) != 2) {
            throw new IOException("Frame Header " + i + ": Unexpected EOF in color animation steps.");
        }

        // Dimensions
        // ==========
        int xOffset = in.readUnsignedShort();
        int yOffset = in.readUnsignedShort();
        int width = in.readUnsignedShort();
        int height = in.readUnsignedShort();

        // Encoding
        // ==============
        int operation = in.readUnsignedByte();
        if (operation > 1) {
            throw new IOException("Frame Header " + i + ": Unexpected operation " + operation + ", expected range [0,1|.");
        }
        int storageMethod = in.readUnsignedByte();
        if (storageMethod > 1) {
            throw new IOException("Frame Header " + i + ": Unexpected storage method " + storageMethod + ", expected range [0,1|.");
        }
        long nData = in.readUnsignedInt();
        if (nData > Integer.MAX_VALUE) {
            throw new IOException("Frame Header " + i + ": Too much data " + nData + ", expected range [0," + Integer.MAX_VALUE + "|.");
        }

        // Reserved
        if (in.skipBytes(60) != 60) {
            throw new IOException("Frame Header " + i + ": Unexpected EOF in reserved fields.");
        }

        // Read image data
        byte[] data = new byte[(int) nData];
        in.readFully(data);

        SEQDeltaFrame f = new SEQDeltaFrame();
        f.setBounds(xOffset, yOffset, width, height);
        f.setOperation(operation);
        f.setStorageMethod(storageMethod);
        f.setData(data);
        f.setColorModel(cm);
        f.setRelTime(speed);
        f.setInterleave(1);
        track.addFrame(f);
    }
}

package org.monte.media.impl.jcodec.containers.mp4.boxes;

import org.monte.media.impl.jcodec.common.JCodecUtil2;
import org.monte.media.impl.jcodec.common.io.NIOUtils;
import org.monte.media.impl.jcodec.common.model.Size;

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
 * Describes video payload sample
 *
 * @author The JCodec project
 */
public class VideoSampleEntry extends SampleEntry {
    public static VideoSampleEntry videoSampleEntry(String fourcc, Size size, String encoderName) {
        return createVideoSampleEntry(new Header(fourcc), (short) 0, (short) 0, "jcod", 0, 768,
                (short) size.getWidth(), (short) size.getHeight(), 72, 72, (short) 1,
                encoderName != null ? encoderName : "jcodec", (short) 24, (short) 1, (short) -1);
    }

    public static VideoSampleEntry createVideoSampleEntry(Header atom, short version, short revision, String vendor,
                                                          int temporalQual, int spacialQual, short width, short height, long hRes, long vRes, short frameCount,
                                                          String compressorName, short depth, short drefInd, short clrTbl) {
        VideoSampleEntry e = new VideoSampleEntry(atom);
        e.drefInd = drefInd;
        e.version = version;
        e.revision = revision;
        e.vendor = vendor;
        e.temporalQual = temporalQual;
        e.spacialQual = spacialQual;
        e.width = width;
        e.height = height;
        e.hRes = hRes;
        e.vRes = vRes;
        e.frameCount = frameCount;
        e.compressorName = compressorName;
        e.depth = depth;
        e.clrTbl = clrTbl;
        return e;
    }

    private short version;
    private short revision;
    private String vendor;
    private int temporalQual;
    private int spacialQual;
    private short width;
    private short height;
    private float hRes;
    private float vRes;
    private short frameCount;
    private String compressorName;
    private short depth;
    private short clrTbl;

    public VideoSampleEntry(Header atom) {
        super(atom);
    }

    public void parse(ByteBuffer input) {
        super.parse(input);

        version = input.getShort();
        revision = input.getShort();
        vendor = NIOUtils.readString(input, 4);
        temporalQual = input.getInt();
        spacialQual = input.getInt();

        width = input.getShort();
        height = input.getShort();

        hRes = (float) input.getInt() / 65536f;
        vRes = (float) input.getInt() / 65536f;

        input.getInt(); // Reserved

        frameCount = input.getShort();

        compressorName = NIOUtils.readPascalStringL(input, 31);

        depth = input.getShort();

        clrTbl = input.getShort();

        parseExtensions(input);
    }

    @Override
    public void doWrite(ByteBuffer out) {

        super.doWrite(out);

        out.putShort(version);
        out.putShort(revision);
        out.put(JCodecUtil2.asciiString(vendor), 0, 4);
        out.putInt(temporalQual);
        out.putInt(spacialQual);

        out.putShort((short) width);
        out.putShort((short) height);

        out.putInt((int) (hRes * 65536));
        out.putInt((int) (vRes * 65536));

        out.putInt(0); // data size

        out.putShort(frameCount);

        NIOUtils.writePascalStringL(out, compressorName, 31);

        out.putShort(depth);

        out.putShort(clrTbl);

        writeExtensions(out);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float gethRes() {
        return hRes;
    }

    public float getvRes() {
        return vRes;
    }

    public long getFrameCount() {
        return frameCount;
    }

    public String getCompressorName() {
        return compressorName;
    }

    public long getDepth() {
        return depth;
    }

    public String getVendor() {
        return vendor;
    }

    public short getVersion() {
        return version;
    }

    public short getRevision() {
        return revision;
    }

    public int getTemporalQual() {
        return temporalQual;
    }

    public int getSpacialQual() {
        return spacialQual;
    }

    public short getClrTbl() {
        return clrTbl;
    }
}
package org.monte.media.impl.jcodec.common.model;

import java.nio.ByteBuffer;
import java.util.Comparator;

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
 * Encoded stream packet
 *
 * @author The JCodec project
 */
public class Packet {

    public enum FrameType {
        KEY, INTER, UNKNOWN
    }

    ;

    public ByteBuffer data;
    public long pts;
    public int timescale;
    public long duration;
    public long frameNo;
    public FrameType frameType;
    public TapeTimecode tapeTimecode;
    public int displayOrder;

    public static Packet createPacket(ByteBuffer data, long pts, int timescale, long duration, long frameNo,
                                      FrameType frameType, TapeTimecode tapeTimecode) {
        return new Packet(data, pts, timescale, duration, frameNo, frameType, tapeTimecode, 0);
    }

    public static Packet createPacketWithData(Packet other, ByteBuffer data) {
        return new Packet(data, other.pts, other.timescale, other.duration, other.frameNo, other.frameType,
                other.tapeTimecode, other.displayOrder);
    }

    public Packet(ByteBuffer data, long pts, int timescale, long duration, long frameNo, FrameType frameType,
                  TapeTimecode tapeTimecode, int displayOrder) {
        this.data = data;
        this.pts = pts;
        this.timescale = timescale;
        this.duration = duration;
        this.frameNo = frameNo;
        this.frameType = frameType;
        this.tapeTimecode = tapeTimecode;
        this.displayOrder = displayOrder;
    }

    public ByteBuffer getData() {
        return data.duplicate();
    }

    public long getPts() {
        return pts;
    }

    public int getTimescale() {
        return timescale;
    }

    public long getDuration() {
        return duration;
    }

    public long getFrameNo() {
        return frameNo;
    }

    public void setTimescale(int timescale) {
        this.timescale = timescale;
    }

    public TapeTimecode getTapeTimecode() {
        return tapeTimecode;
    }

    public void setTapeTimecode(TapeTimecode tapeTimecode) {
        this.tapeTimecode = tapeTimecode;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public FrameType getFrameType() {
        return frameType;
    }

    public void setFrameType(FrameType frameType) {
        this.frameType = frameType;
    }

    public RationalLarge getPtsR() {
        return RationalLarge.R(pts, timescale);
    }

    public double getPtsD() {
        return ((double) pts) / timescale;
    }

    public double getDurationD() {
        return ((double) duration) / timescale;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }

    public void setPts(long pts) {
        this.pts = pts;
    }

    public static final Comparator<Packet> FRAME_ASC = new Comparator<Packet>() {
        public int compare(Packet o1, Packet o2) {
            if (o1 == null && o2 == null)
                return 0;
            if (o1 == null)
                return -1;
            if (o2 == null)
                return 1;
            return o1.frameNo < o2.frameNo ? -1 : (o1.frameNo == o2.frameNo ? 0 : 1);
        }
    };

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public boolean isKeyFrame() {
        return frameType == FrameType.KEY;
    }
}

package org.monte.media.impl.jcodec.common.model;

import static org.monte.media.impl.jcodec.common.StringUtils.zeroPad2;

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
 * Tape timecode
 *
 * @author The JCodec project
 */
public class TapeTimecode {
    public static final TapeTimecode ZERO_TAPE_TIMECODE = new TapeTimecode((short) 0, (byte) 0, (byte) 0, (byte) 0, false, 0);

    private final short hour;
    private final byte minute;
    private final byte second;
    private final byte frame;
    private final boolean dropFrame;
    private final int tapeFps;

    public TapeTimecode(short hour, byte minute, byte second, byte frame, boolean dropFrame, int tapeFps) {
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.frame = frame;
        this.dropFrame = dropFrame;
        this.tapeFps = tapeFps;
    }

    public short getHour() {
        return hour;
    }

    public byte getMinute() {
        return minute;
    }

    public byte getSecond() {
        return second;
    }

    public byte getFrame() {
        return frame;
    }

    public boolean isDropFrame() {
        return dropFrame;
    }

    public int getTapeFps() {
        return tapeFps;
    }

    @Override
    public String toString() {
        return zeroPad2(hour) + ":" +
                zeroPad2(minute) + ":" +
                zeroPad2(second) + (dropFrame ? ";" : ":") +
                zeroPad2(frame);
    }

    public static TapeTimecode tapeTimecode(long frame, boolean dropFrame, int tapeFps) {
        if (dropFrame) {
            long D = frame / 17982;
            long M = frame % 17982;
            frame += 18 * D + 2 * ((M - 2) / 1798);
        }
        long sec = frame / tapeFps;
        return new TapeTimecode((short) (sec / 3600), (byte) ((sec / 60) % 60), (byte) (sec % 60),
                (byte) (frame % tapeFps), dropFrame, tapeFps);
    }
}

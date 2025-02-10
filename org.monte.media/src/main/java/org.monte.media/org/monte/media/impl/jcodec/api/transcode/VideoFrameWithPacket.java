package org.monte.media.impl.jcodec.api.transcode;

import org.monte.media.impl.jcodec.api.transcode.PixelStore.LoanerPicture;
import org.monte.media.impl.jcodec.common.model.Packet;

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
public class VideoFrameWithPacket implements Comparable<VideoFrameWithPacket> {
    private Packet packet;
    private LoanerPicture frame;

    public VideoFrameWithPacket(Packet inFrame, LoanerPicture dec2) {
        this.packet = inFrame;
        this.frame = dec2;
    }

    @Override
    public int compareTo(VideoFrameWithPacket arg) {
        if (arg == null)
            return -1;
        else {
            long pts1 = packet.getPts();
            long pts2 = arg.packet.getPts();
            return pts1 > pts2 ? 1 : (pts1 == pts2 ? 0 : -1);
        }
    }

    public Packet getPacket() {
        return packet;
    }

    public LoanerPicture getFrame() {
        return frame;
    }
}
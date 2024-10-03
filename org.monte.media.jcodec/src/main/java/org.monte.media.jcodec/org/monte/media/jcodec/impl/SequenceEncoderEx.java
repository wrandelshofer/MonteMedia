/*
 * @(#)SequenceEncoderEx.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.jcodec.impl;


import org.jcodec.api.transcode.PixelStore;
import org.jcodec.api.transcode.PixelStore.LoanerPicture;
import org.jcodec.api.transcode.PixelStoreImpl;
import org.jcodec.api.transcode.VideoFrameWithPacket;
import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.common.Codec;
import org.jcodec.common.Format;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Packet.FrameType;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Rational;
import org.jcodec.scale.ColorUtil;
import org.jcodec.scale.Transform;

import java.io.File;
import java.io.IOException;

import static org.jcodec.common.Codec.H264;
import static org.jcodec.common.Format.MOV;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * <p>
 * Encodes a sequence of images as a video.
 *
 * @author The JCodec project
 */
public class SequenceEncoderEx {

    private Transform transform;
    private int frameNo;
    private int timestamp;
    private Rational fps;
    private SinkImplEx sink;
    private PixelStore pixelStore;

    public static SequenceEncoderEx createSequenceEncoder(File out, int fps) throws IOException {
        return new SequenceEncoderEx(NIOUtils.writableChannel(out), Rational.R(fps, 1), MOV, H264, null);
    }

    public static SequenceEncoderEx create25Fps(File out) throws IOException {
        return new SequenceEncoderEx(NIOUtils.writableChannel(out), Rational.R(25, 1), MOV, H264, null);
    }

    public static SequenceEncoderEx create30Fps(File out) throws IOException {
        return new SequenceEncoderEx(NIOUtils.writableChannel(out), Rational.R(30, 1), MOV, H264, null);
    }

    public static SequenceEncoderEx create2997Fps(File out) throws IOException {
        return new SequenceEncoderEx(NIOUtils.writableChannel(out), Rational.R(30000, 1001), MOV, H264, null);
    }

    public static SequenceEncoderEx create24Fps(File out) throws IOException {
        return new SequenceEncoderEx(NIOUtils.writableChannel(out), Rational.R(24, 1), MOV, H264, null);
    }

    public static SequenceEncoderEx createWithFps(SeekableByteChannel out, Rational fps) throws IOException {
        return new SequenceEncoderEx(out, fps, MOV, H264, null);
    }

    public SequenceEncoderEx(SeekableByteChannel out, Rational fps, Format outputFormat, Codec outputVideoCodec,
                             Codec outputAudioCodec) throws IOException {
        this.fps = fps;

        sink = SinkImplEx.createWithStream(out, outputFormat, outputVideoCodec, outputAudioCodec);
        sink.init();

        if (sink.getInputColor() != null)
            transform = ColorUtil.getTransform(ColorSpace.RGB, sink.getInputColor());

        pixelStore = new PixelStoreImpl();
    }

    public Rational getFps() {
        return fps;
    }

    public void setFps(Rational fps) {
        this.fps = fps;
    }

    public void setMotionSearchRange(int newValue) {
        if (sink.getVideoEncoder() instanceof H264Encoder) {
            H264Encoder e = (H264Encoder) sink.getVideoEncoder();
            e.setMotionSearchRange(newValue);
        }
    }

    /**
     * Encodes a frame into a movie.
     *
     * @param pic
     * @throws IOException
     */
    public void encodeNativeFrame(Picture pic) throws IOException {
        if (pic.getColor() != ColorSpace.RGB)
            throw new IllegalArgumentException("The input images is expected in RGB color.");

        ColorSpace sinkColor = sink.getInputColor();
        LoanerPicture toEncode;
        if (sinkColor != null) {
            toEncode = pixelStore.getPicture(pic.getWidth(), pic.getHeight(), sinkColor);
            transform.transform(pic, toEncode.getPicture());
        } else {
            toEncode = new LoanerPicture(pic, 0);
        }

        Packet pkt = Packet.createPacket(null, timestamp, fps.getNum(), fps.getDen(), frameNo, FrameType.KEY, null);
        sink.outputVideoFrame(new VideoFrameWithPacket(pkt, toEncode));

        if (sinkColor != null)
            pixelStore.putBack(toEncode);

        timestamp += fps.getDen();
        frameNo++;
    }

    public void finish() throws IOException {
        sink.finish();
    }
}

/*
 * @(#)H264Codec.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.jcodec.codec;

import org.jcodec.api.transcode.PixelStore;
import org.jcodec.api.transcode.VideoFrameWithPacket;
import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.codecs.h264.io.model.SeqParameterSet;
import org.jcodec.common.VideoEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Picture;
import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.codec.video.AbstractVideoCodec;
import org.monte.media.jcodec.impl.AWTUtil;
import org.monte.media.qtff.AvcDecoderConfigurationRecord;
import org.monte.media.util.ArrayUtil;
import org.monte.media.util.ByteArray;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.KEYFRAME;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.av.FormatKeys.MIME_AVI;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVC1;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.MotionSearchRangeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;
import static org.monte.media.jcodec.codec.JCodecPictureCodec.ENCODING_PICTURE;

/**
 * Codec for {@link Picture} to {@code H264} byte array.
 */
public class JCodecH264Codec extends AbstractVideoCodec {
    private VideoEncoder videoEncoder = null;
    private ByteBuffer byteBuffer;

    public JCodecH264Codec() {
        super(new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                                EncodingKey, ENCODING_BUFFERED_IMAGE,
                                DataClassKey, BufferedImage.class), //
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                                EncodingKey, ENCODING_PICTURE,
                                DataClassKey, Picture.class), //
                },
                new Format[]{
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                                MimeTypeKey, MIME_QUICKTIME,
                                DepthKey, 24,
                                EncodingKey, ENCODING_AVC1,
                                DataClassKey, byte[].class), //
                        new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                                MimeTypeKey, MIME_AVI,
                                DepthKey, 24,
                                EncodingKey, ENCODING_AVC1,
                                DataClassKey, byte[].class), //
                }//
        );
        name = "JCodec H264 Codec";
    }

    @Override
    public Format setOutputFormat(Format f) {
        super.setOutputFormat(f);
        // This codec can not scale an image.
        // Enforce these properties
        if (outputFormat != null) {
            if (inputFormat != null) {
                outputFormat = outputFormat.prepend(inputFormat.intersectKeys(WidthKey, HeightKey, DepthKey));
            }
            // Suggest a keyframe rate and motion compensation
            outputFormat = outputFormat.append(KeyFrameIntervalKey, 60, MotionSearchRangeKey, 16);
        }
        return this.outputFormat;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (ENCODING_PICTURE.equals(outputFormat.get(EncodingKey))) {
            return decode(in, out);
        } else {
            return encode(in, out);
        }
    }

    public int decode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        out.setException(new UnsupportedOperationException("decoding is not supported"));
        out.setFlag(DISCARD);
        return CODEC_FAILED;

    }

    public int encode(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        Picture picture = getPicture(in);
        if (picture == null) {
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
        var enc = getEncoder(outputFormat);

        PixelStore.LoanerPicture toEncode = new PixelStore.LoanerPicture(picture, 0);

        Packet pkt = Packet.createPacket(null, 0, outputFormat.get(FrameRateKey).intValue(),
                out.sampleDuration.divide(outputFormat.get(FrameRateKey)).intValue(),
                out.sequenceNumber,
                out.sequenceNumber % outputFormat.get(KeyFrameIntervalKey) == 0 ? Packet.FrameType.KEY : Packet.FrameType.INTER,
                null);
        VideoFrameWithPacket videoFrame = new VideoFrameWithPacket(pkt, toEncode);
        Packet outputVideoPacket;
        int bufferSize = enc.estimateBufferSize(picture);
        if (byteBuffer == null || bufferSize < byteBuffer.capacity()) {
            byteBuffer = ByteBuffer.allocate(bufferSize);
        }
        byteBuffer.clear();
        VideoEncoder.EncodedFrame encodedFrame = enc.encodeFrame(picture, byteBuffer);
        outputVideoPacket = Packet.createPacketWithData(videoFrame.getPacket(), NIOUtils.clone(encodedFrame.getData()));
        outputVideoPacket.setFrameType(encodedFrame.isKeyFrame() ? Packet.FrameType.KEY : Packet.FrameType.INTER);

        // compute header
        out.header = null;
        if (encodedFrame.isKeyFrame()) {
            List<ByteBuffer> spsList = new ArrayList<>();
            List<ByteBuffer> ppsList = new ArrayList<>();
            H264Utils.wipePSinplace(outputVideoPacket.data, spsList, ppsList);
            if (!spsList.isEmpty()) {
                SeqParameterSet p = H264Utils.readSPS(spsList.get(0));
                Function<ByteBuffer, ByteArray> byteBufferFunction = b -> new ByteArray(ArrayUtil.copyOf(b.array(), b.arrayOffset(), b.remaining()));
                out.header = new AvcDecoderConfigurationRecord(p.profileIdc, 0, p.levelIdc, 4,
                        spsList.stream().map(byteBufferFunction).collect(Collectors.toCollection(LinkedHashSet::new)),
                        ppsList.stream().map(byteBufferFunction).collect(Collectors.toCollection(LinkedHashSet::new)));
            }
        }

        out.setFlag(KEYFRAME, encodedFrame.isKeyFrame());
        ByteBuffer packetBuf = outputVideoPacket.data;
        if (out.data instanceof byte[] && ((byte[]) out.data).length >= packetBuf.remaining()) {
            byte[] byteArray = (byte[]) out.data;
            System.arraycopy(packetBuf.array(), packetBuf.position(), byteArray, 0, packetBuf.remaining());
            out.offset = 0;
            out.length = packetBuf.remaining();
        } else {
            out.data = packetBuf.array().clone();
            out.offset = packetBuf.position();
            out.length = packetBuf.remaining();
        }

        return CODEC_OK;
    }

    private VideoEncoder getEncoder(Format outputFormat) {
        if (videoEncoder == null) {
            H264Encoder enc = H264Encoder.createH264Encoder();
            enc.setMotionSearchRange(outputFormat.get(MotionSearchRangeKey));
            enc.setKeyInterval(outputFormat.get(KeyFrameIntervalKey));
            videoEncoder = enc;
        }
        return videoEncoder;
    }

    private Picture getPicture(Buffer buf) {
        if (buf.data instanceof BufferedImage) {
            BufferedImage img = (BufferedImage) buf.data;
            return AWTUtil.fromBufferedImage(img, ColorSpace.YUV420J);
        } else if (buf.data instanceof Picture) {
            Picture picture = (Picture) buf.data;
            return picture;
        }
        return null;
    }


}

/*
 * @(#)JCodecMP4Writer.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.jcodec.mp4;

import org.jcodec.api.transcode.AudioFrameWithPacket;
import org.jcodec.api.transcode.PixelStore;
import org.jcodec.api.transcode.VideoFrameWithPacket;
import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.common.AudioCodecMeta;
import org.jcodec.common.AudioEncoder;
import org.jcodec.common.AudioFormat;
import org.jcodec.common.Muxer;
import org.jcodec.common.MuxerTrack;
import org.jcodec.common.VideoCodecMeta;
import org.jcodec.common.VideoEncoder;
import org.jcodec.common.io.FileChannelWrapper;
import org.jcodec.common.io.IOUtils;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.model.AudioBuffer;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Packet;
import org.jcodec.common.model.Picture;
import org.jcodec.common.model.Size;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.monte.media.av.Buffer;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.Registry;
import org.monte.media.math.Rational;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.jcodec.common.Codec.H264;
import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.av.FormatKeys.MIME_MP4;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ByteOrderKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ChannelsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleRateKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleSizeInBitsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SignedKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.FixedFrameRateKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.MotionSearchRangeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.QualityKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;
import static org.monte.media.jcodec.h264.JCodecPictureCodec.ENCODING_PICTURE;

public class JCodecMP4Writer implements MovieWriter {
    public final static Format MP4 = new Format(MediaTypeKey, FormatKeys.MediaType.FILE, MimeTypeKey, MIME_MP4);
    private final List<Track> tracks = new ArrayList<>();
    private Muxer muxer;
    private FileChannelWrapper destStream;

    private static class Track {
        public Codec codec;
        public int frameNo;
        public Buffer outputBuffer;
        public ByteBuffer jcodecByteBuffer;
        public int timestamp;
        public org.jcodec.common.model.Rational fps;
        public MuxerTrack jcodecMuxerTrack;
        private Rational duration = Rational.ZERO;
        private Format format;
        private AudioEncoder audioEncoder;
        private VideoEncoder videoEncoder;
    }

    public JCodecMP4Writer(File file) throws IOException {
        destStream = NIOUtils.writableChannel(file);
        muxer = MP4Muxer.createMP4MuxerToChannel(destStream);
    }

    public JCodecMP4Writer(FileOutputStream out) throws IOException {
        destStream = new FileChannelWrapper(out.getChannel());
        muxer = MP4Muxer.createMP4MuxerToChannel(destStream);
    }


    @Override
    public Format getFileFormat() throws IOException {
        return MP4;
    }

    @Override
    public void setFileFormat(Format newValue) throws IOException {
        // ignore
    }

    @Override
    public int addTrack(Format format) throws IOException {
        switch (format.get(MediaTypeKey, FormatKeys.MediaType.VIDEO)) {
            case VIDEO:
                return addVideoTrack(format);
            case AUDIO:
                return addAudioTrack(format);
            default:
                throw new IllegalArgumentException("VIDEO or AUDIO format expected: " + format);

        }
    }

    @Override
    public void setCodec(int trackIndex, Codec codec) {
        tracks.get(trackIndex).codec = codec;
    }

    /**
     * Adds a video track.
     *
     * @param fmt The format of the track.
     * @return The track number.
     */
    private int addVideoTrack(Format fmt) throws IOException {
        fmt.requireKeys(FrameRateKey, WidthKey, HeightKey);
        fmt = fmt.prepend(MediaTypeKey, FormatKeys.MediaType.VIDEO)
                .append(MotionSearchRangeKey, 16, KeyFrameIntervalKey, 60);

        Track tr = new Track();
        tr.format = fmt;


        Format jcodecPictureFormat = fmt.prepend(EncodingKey, ENCODING_PICTURE, DataClassKey, Picture.class);
        tr.codec = Registry.getInstance().getEncoder(jcodecPictureFormat);
        if (tr.codec == null) {
            throw new UnsupportedOperationException("No codec found for format=" + jcodecPictureFormat);
        }
        tr.codec.setInputFormat(fmt.prepend(
                EncodingKey, ENCODING_BUFFERED_IMAGE,
                DataClassKey, BufferedImage.class));
        if (null == tr.codec.setOutputFormat(
                fmt.prepend(EncodingKey, ENCODING_PICTURE, DataClassKey, Picture.class))) {
            throw new UnsupportedOperationException("Codec does not support format " + fmt + ". codec=" + tr.codec);
        }

        tr.format = fmt;
        H264Encoder enc = H264Encoder.createH264Encoder();
        enc.setMotionSearchRange(fmt.get(MotionSearchRangeKey));
        enc.setKeyInterval(fmt.get(KeyFrameIntervalKey));
        tr.videoEncoder = enc;
        Rational frameRate = fmt.get(FrameRateKey);
        tr.fps = org.jcodec.common.model.Rational.R((int) frameRate.getNumerator(), (int) frameRate.getDenominator());
        VideoCodecMeta codecMeta = VideoCodecMeta.createSimpleVideoCodecMeta(new Size(fmt.get(WidthKey), fmt.get(HeightKey)),
                ColorSpace.RGB);
        tr.jcodecMuxerTrack = muxer.addVideoTrack(H264, codecMeta);
        tracks.add(tr);
        return tracks.size() - 1;
    }

    /**
     * Adds an audio track.
     *
     * @param fmt The format of the track.
     * @return The track number.
     */
    private int addAudioTrack(Format fmt) throws IOException {
        fmt.requireKeys(SampleRateKey, SampleSizeInBitsKey, ChannelsKey);
        Track tr = new Track();
        tr.format = fmt.prepend(MediaTypeKey, FormatKeys.MediaType.AUDIO, SignedKey, Boolean.TRUE, ByteOrderKey, ByteOrder.BIG_ENDIAN);
        tracks.add(tr);
        tr.codec = Registry.getInstance().getEncoder(fmt);
        if (tr.codec == null) {
            throw new UnsupportedOperationException("No codec found.");
        }
        tr.codec.setInputFormat(null);
        if (null == tr.codec.setOutputFormat(
                fmt.prepend(FixedFrameRateKey, true,
                        QualityKey, 1f,
                        MimeTypeKey, MIME_QUICKTIME,//
                        DataClassKey, byte[].class))) {
            throw new UnsupportedOperationException("Track " + tr + " codec " + tr.codec + " does not support format. " + fmt);
        }
        tr.format = fmt;
        RawAudioEncoder enc = new RawAudioEncoder();

        tr.audioEncoder = enc;
        var audioFormat = new AudioFormat(fmt.get(SampleRateKey).intValue(),
                fmt.get(SampleSizeInBitsKey),
                fmt.get(ChannelsKey),
                fmt.get(SignedKey),
                fmt.get(ByteOrderKey).equals(ByteOrder.BIG_ENDIAN));
        tr.jcodecMuxerTrack = muxer.addAudioTrack(org.jcodec.common.Codec.PCM, org.jcodec.common.AudioCodecMeta.fromAudioFormat(audioFormat));

        return tracks.size() - 1;
    }

    @Override
    public Format getFormat(int track) {
        return tracks.get(track).format;
    }

    @Override
    public int getTrackCount() {
        return tracks.size();
    }

    protected void ensureStarted() throws IOException {
        if (!destStream.isOpen()) {
            throw new IOException("stream is closed");
        }
    }

    @Override
    public void write(int track, Buffer buf) throws IOException {
        ensureStarted();
        if (buf.flags.contains(DISCARD)) {
            return;
        }

        Track tr = tracks.get(track);

        // Encode sample data

        // We got here, because the buffer format does not match the track
        // format. Let's see if we can create a codec which can perform the
        // encoding for us.
        if (tr.codec == null) {
            throw new UnsupportedOperationException("No codec for this format " + tr.format);
        }

        if (tr.outputBuffer == null) {
            tr.outputBuffer = new Buffer();
        }
        Buffer outBuf = tr.outputBuffer;
        if (tr.codec.process(buf, outBuf) != Codec.CODEC_OK) {
            throw new IOException("Codec failed or could not encode the sample in a single step. codec:" + tr.codec);
        }
        if (outBuf.isFlag(DISCARD)) {
            return;
        }

        boolean keyFrame = tr.format.get(KeyFrameIntervalKey, tr.format.get(FrameRateKey, new Rational(60)).intValue()) == 0;

        if (tr.videoEncoder != null) {
            PixelStore.LoanerPicture toEncode;
            toEncode = new PixelStore.LoanerPicture((Picture) outBuf.data, 0);

            Packet pkt = Packet.createPacket(null, tr.timestamp, tr.fps.getNum(), tr.fps.getDen(), tr.frameNo,
                    keyFrame ? Packet.FrameType.KEY : Packet.FrameType.INTER,
                    null);
            outputVideoFrame(tr, new VideoFrameWithPacket(pkt, toEncode));
            tr.timestamp += tr.fps.getDen();
            tr.frameNo++;
        } else if (tr.audioEncoder != null) {

        } else {
            throw new IOException("No encoder");
        }


    }

    @Override
    public void write(int track, BufferedImage image, long duration) throws IOException {
        var buf = new Buffer();
        buf.data = image;
        buf.sampleDuration = tracks.get(track).format.get(FrameRateKey).multiply(duration);
        write(track, buf);
    }

    @Override
    public void close() throws IOException {
        try {
            if (!tracks.isEmpty()) {
                muxer.finish();
            }
        } finally {
            IOUtils.closeQuietly(destStream);
        }
    }

    @Override
    public boolean isDataLimitReached() {
        return false;
    }

    @Override
    public Rational getDuration(int track) {
        return tracks.get(track).duration;
    }

    @Override
    public boolean isEmpty(int track) {
        return tracks.get(track).duration.equals(Rational.ZERO);
    }


    private static class RawAudioEncoder implements AudioEncoder {
        @Override
        public ByteBuffer encode(ByteBuffer audioPkt, ByteBuffer buf) {
            return audioPkt;
        }
    }

    /**
     * Returns the compression quality of a track.
     *
     * @return compression quality
     */
    public float getCompressionQuality(int track) {
        return tracks.get(track).format.get(QualityKey);
    }

    private void outputVideoFrame(Track tr, VideoFrameWithPacket videoFrame) throws IOException {

        Packet outputVideoPacket;
        ByteBuffer buffer = tr.jcodecByteBuffer;
        int bufferSize = tr.videoEncoder.estimateBufferSize(videoFrame.getFrame().getPicture());
        if (buffer == null || bufferSize < buffer.capacity()) {
            buffer = ByteBuffer.allocate(bufferSize);
            tr.jcodecByteBuffer = buffer;
        }
        buffer.clear();
        Picture frame = videoFrame.getFrame().getPicture();
        VideoEncoder.EncodedFrame enc = tr.videoEncoder.encodeFrame(frame, buffer);
        outputVideoPacket = Packet.createPacketWithData(videoFrame.getPacket(), NIOUtils.clone(enc.getData()));
        outputVideoPacket.setFrameType(enc.isKeyFrame() ? Packet.FrameType.KEY : Packet.FrameType.INTER);

        tr.jcodecMuxerTrack.addFrame(outputVideoPacket);
    }

    private void outputAudioFrame(Track tr, AudioFrameWithPacket audioFrame) throws IOException {

        outputAudioPacket(tr, Packet.createPacketWithData(audioFrame.getPacket(), encodeAudio(tr, audioFrame.getAudio())),
                org.jcodec.common.AudioCodecMeta.fromAudioFormat(audioFrame.getAudio().getFormat()));
    }

    private ByteBuffer encodeAudio(Track tr, AudioBuffer audioBuffer) {

        return tr.audioEncoder.encode(audioBuffer.getData(), null);
    }

    private void outputAudioPacket(Track tr, Packet audioPkt, AudioCodecMeta audioCodecMeta) throws IOException {
        tr.jcodecMuxerTrack.addFrame(audioPkt);

    }
}

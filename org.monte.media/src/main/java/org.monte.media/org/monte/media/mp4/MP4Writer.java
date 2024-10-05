/*
 * @(#)MP4Writer.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.mp4;

import org.monte.media.av.Buffer;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.Registry;
import org.monte.media.av.codec.video.VideoFormatKeys;
import org.monte.media.math.Rational;
import org.monte.media.qtff.AbstractQTFFMovieStream;
import org.monte.media.qtff.AvcDecoderConfigurationRecord;

import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.KEYFRAME;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MIME_MP4;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ByteOrderKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ChannelsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_PCM_SIGNED;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_PCM_UNSIGNED;
import static org.monte.media.av.codec.audio.AudioFormatKeys.FrameSizeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleRateKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleSizeInBitsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SignedKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.PaletteKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.QualityKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * Supports writing of time-based video and audio data into a MP4 movie
 * file (.MP4) without the need of native code.
 *
 * @author Werner Randelshofer
 */
public class MP4Writer extends MP4OutputStream implements MovieWriter {
    private static class TrackEncoder {
        /**
         * The codec.
         */
        public Codec codec;
        public Buffer outputBuffer;
        public Buffer inputBuffer;
    }

    private List<TrackEncoder> trackEncoders = new ArrayList<>();

    public final static Format MP4 = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_MP4);

    /**
     * Creates a new MP4 writer.
     *
     * @param file the output file
     */
    public MP4Writer(File file) throws IOException {
        super(file);
    }

    /**
     * Creates a new MP4 writer.
     *
     * @param out the output stream.
     */
    public MP4Writer(ImageOutputStream out) throws IOException {
        super(out);
    }


    @Override
    public Format getFileFormat() throws IOException {
        return MP4;
    }

    @Override
    public Format getFormat(int track) {
        return tracks.get(track).format;
    }

    /**
     * Adds a track.
     *
     * @param fmt The format of the track.
     * @return The track number.
     */
    @Override
    public int addTrack(Format fmt) throws IOException {
        if (fmt.get(MediaTypeKey) == MediaType.VIDEO) {
            int t = addVideoTrack(fmt.get(EncodingKey),
                    fmt.get(CompressorNameKey, AbstractQTFFMovieStream.DEFAULT_COMPONENT_NAME),
                    Math.min(6000, fmt.get(FrameRateKey).getNumerator() * fmt.get(FrameRateKey).getDenominator()),
                    fmt.get(WidthKey), fmt.get(HeightKey), fmt.get(DepthKey, 24),
                    fmt.get(KeyFrameIntervalKey, fmt.get(FrameRateKey).floor(1).intValue()), fmt);
            setCompressionQuality(t, fmt.get(QualityKey, 1.0f));
            return t;
        } else if (fmt.get(MediaTypeKey) == MediaType.AUDIO) {
            // fill in unspecified values
            int sampleSizeInBits = fmt.get(SampleSizeInBitsKey, 16);
            ByteOrder bo = fmt.get(ByteOrderKey, ByteOrder.BIG_ENDIAN);
            boolean signed = fmt.get(SignedKey, true);
            String encoding = fmt.get(EncodingKey, null);
            Rational frameRate = fmt.get(FrameRateKey, fmt.get(SampleRateKey));
            int channels = fmt.get(ChannelsKey, 1);
            int frameSize = fmt.get(FrameSizeKey, (sampleSizeInBits + 7) / 8);
            if (encoding == null || encoding.length() != 4) {
                if (signed) {
                    encoding = bo == ByteOrder.BIG_ENDIAN ? "twos" : "sowt";
                } else {
                    encoding = "raw ";
                }
            }

            return addAudioTrack(encoding,
                    fmt.get(SampleRateKey).longValue(),
                    fmt.get(SampleRateKey).doubleValue(),
                    channels,
                    sampleSizeInBits,
                    false, // FIXME - We should support compressed formats
                    fmt.get(SampleRateKey).divide(frameRate).intValue(),
                    frameSize,
                    signed,
                    bo);
            //return addAudioTrack(AudioFormatKeys.toAudioFormat(fmt)); // FIXME Add direct support for AudioFormat
        } else {
            throw new IOException("Unsupported media type:" + fmt.get(MediaTypeKey));
        }
    }

    /**
     * Adds a video track.
     *
     * @param format    The MP4 video format.
     * @param timeScale The media timescale. This is typically the frame rate.
     *                  If the frame rate is not an integer fraction of a second, specify a
     *                  multiple of the frame rate and specify a correspondingly multiplied
     *                  sampleDuration when writing frames. For example, for a rate of 23.976 fps
     *                  specify a timescale of 23976 and multiply the sampleDuration of a video
     *                  frame by 1000.
     * @param width     The width of a video image. Must be larger than 0.
     * @param height    The height of a video image. Must be larger than 0.
     * @return Returns the track index.
     * @throws IllegalArgumentException if the width or the height is smaller
     *                                  than 1.
     */
    public int addVideoTrack(Format format, long timeScale, int width, int height) throws IOException {
        int tr = addVideoTrack(format.get(EncodingKey), format.get(CompressorNameKey), timeScale, width, height, 24, 30, format);
        setVideoColorTable(tr, format.get(PaletteKey));
        return tr;
    }

    /**
     * Adds a video track.
     *
     * @param format The MP4 video format.
     * @param width  The width of a video image. Must be larger than 0.
     * @param height The height of a video image. Must be larger than 0.
     * @return Returns the track index.
     * @throws IllegalArgumentException if the width or the height is smaller
     *                                  than 1.
     */
    public int addVideoTrack(Format format, int width, int height, int depth, int syncInterval) throws IOException {
        int tr = addVideoTrack(format.get(EncodingKey), format.get(CompressorNameKey), format.get(FrameRateKey).getDenominator() * format.get(FrameRateKey).getNumerator(), width, height, depth, syncInterval, format);
        setVideoColorTable(tr, format.get(PaletteKey));
        return tr;
    }

    /**
     * Adds an audio track, and configures it using an {@code AudioFormat}
     * object from the javax.sound API.
     * <p>
     * Use this method for writing audio data from an {@code AudioInputStream}
     * into a MP4 Movie file.
     *
     * @param format The javax.sound audio format.
     * @return Returns the track index.
     */
    public int addAudioTrack(javax.sound.sampled.AudioFormat format) throws IOException {
        ensureStarted();
        String qtAudioFormat;
        double sampleRate = format.getSampleRate();
        long timeScale = (int) Math.floor(sampleRate);
        int sampleSizeInBits = format.getSampleSizeInBits();
        int numberOfChannels = format.getChannels();
        ByteOrder byteOrder = format.isBigEndian() ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
        int frameDuration = (int) (format.getSampleRate() / format.getFrameRate());
        int frameSize = format.getFrameSize();
        boolean isCompressed = format.getProperty("vbr") != null && ((Boolean) format.getProperty("vbr")).booleanValue();
        boolean signed = false;
        javax.sound.sampled.AudioFormat.Encoding enc = format.getEncoding();
        if (enc.equals(javax.sound.sampled.AudioFormat.Encoding.ALAW)) {
            qtAudioFormat = "alaw";
            if (sampleSizeInBits != 8) {
                throw new IllegalArgumentException("Sample size of 8 for ALAW required:" + sampleSizeInBits);
            }
        } else if (javax.sound.sampled.AudioFormat.Encoding.PCM_SIGNED.equals(enc)) {
            qtAudioFormat = switch (sampleSizeInBits) {
                case 8 ->// Requires conversion to PCM_UNSIGNED!
                        "raw ";
                case 16 -> (byteOrder == ByteOrder.BIG_ENDIAN) ? "twos" : "sowt";
                case 24 -> "in24";
                case 32 -> "in32";
                default ->
                        throw new IllegalArgumentException("Unsupported sample size for PCM_SIGNED:" + sampleSizeInBits);
            };
        } else if (javax.sound.sampled.AudioFormat.Encoding.PCM_UNSIGNED.equals(enc)) {
            qtAudioFormat = switch (sampleSizeInBits) {
                case 8 -> "raw ";
                case 16 ->// Requires conversion to PCM_SIGNED!
                        (byteOrder == ByteOrder.BIG_ENDIAN) ? "twos" : "sowt";
                case 24 ->// Requires conversion to PCM_SIGNED!
                        "in24";
                case 32 ->// Requires conversion to PCM_SIGNED!
                        "in32";
                default ->
                        throw new IllegalArgumentException("Unsupported sample size for PCM_UNSIGNED:" + sampleSizeInBits);
            };
        } else if (javax.sound.sampled.AudioFormat.Encoding.ULAW.equals(enc)) {
            if (sampleSizeInBits != 8) {
                throw new IllegalArgumentException("Sample size of 8 for ULAW required:" + sampleSizeInBits);
            }
            qtAudioFormat = "ulaw";
        } else if ("MP3".equals(enc.toString())) {
            qtAudioFormat = ".mp3";
        } else {
            qtAudioFormat = format.getEncoding().toString();
            if (qtAudioFormat == null || qtAudioFormat.length() != 4) {
                throw new IllegalArgumentException("Unsupported encoding:" + format.getEncoding());
            }
        }

        return addAudioTrack(qtAudioFormat, timeScale, sampleRate,
                numberOfChannels, sampleSizeInBits,
                isCompressed, frameDuration, frameSize, signed, byteOrder);
    }

    @Override
    public int getTrackCount() {
        return tracks.size();
    }

    /**
     * Returns the sampleDuration of the track in seconds.
     */
    @Override
    public Rational getDuration(int track) {
        AbstractQTFFMovieStream.Track tr = tracks.get(track);
        return new Rational(tr.mediaDuration, tr.mediaTimeScale);
    }

    private Codec createCodec(Format fmt) {
        return Registry.getInstance().getEncoder(fmt.prepend(MimeTypeKey, MIME_QUICKTIME));
    }

    private void createCodec(int track) {
        AbstractQTFFMovieStream.Track tr = tracks.get(track);
        TrackEncoder tre = getTrackEncoder(track);
        Format fmt = tr.format;
        tre.codec = createCodec(fmt);
        if (tre.codec != null) {
            if (fmt.get(MediaTypeKey) == MediaType.VIDEO) {
                tre.codec.setInputFormat(fmt.prepend(
                        MimeTypeKey, MIME_JAVA, EncodingKey, ENCODING_BUFFERED_IMAGE,
                        DataClassKey, BufferedImage.class));

                if (null == tre.codec.setOutputFormat(
                        fmt.prepend(
                                QualityKey, getCompressionQuality(track),
                                MimeTypeKey, MIME_QUICKTIME,
                                DataClassKey, byte[].class))) {
                    throw new UnsupportedOperationException("Input format not supported:" + fmt);
                }
            } else {
                tre.codec.setInputFormat(fmt.prepend(
                        MimeTypeKey, MIME_JAVA, EncodingKey, fmt.containsKey(SignedKey) && fmt.get(SignedKey) ? ENCODING_PCM_SIGNED : ENCODING_PCM_UNSIGNED,
                        DataClassKey, byte[].class));
                if (tre.codec.setOutputFormat(fmt) == null) {
                    throw new UnsupportedOperationException("Codec output format not supported:" + fmt + " codec:" + tre.codec);
                } else {
                    tr.format = tre.codec.getOutputFormat();
                }
            }
        }
    }

    /**
     * Returns the codec of the specified track.
     */
    public Codec getCodec(int track) {
        return getTrackEncoder(track).codec;
    }

    /**
     * Sets the codec for the specified track.
     */
    public void setCodec(int track, Codec codec) {
        getTrackEncoder(track).codec = codec;
    }

    /**
     * Writes a sample. Does nothing if the discard-flag in the buffer is set to
     * true.
     *
     * @param track The track number.
     * @param buf   The buffer containing the sample data.
     */
    @Override
    public void write(int track, Buffer buf) throws IOException {
        ensureStarted();
        AbstractQTFFMovieStream.Track tr = tracks.get(track);
        TrackEncoder tre = getTrackEncoder(track);

        // Encode sample data
        {
            if (tre.outputBuffer == null) {
                tre.outputBuffer = new Buffer();
                tre.outputBuffer.format = tr.format;
            }
            Buffer outBuf;
            if (tr.format.matchesWithout(buf.format, FrameRateKey)) {
                outBuf = buf;
            } else {
                outBuf = tre.outputBuffer;
                boolean isSync = tr.syncInterval == 0 ? false : tr.sampleCount % tr.syncInterval == 0;
                buf.setFlag(KEYFRAME, isSync);
                if (tre.codec == null) {
                    createCodec(track);
                    if (tre.codec == null) {
                        throw new UnsupportedOperationException("No codec for this format " + tr.format);
                    }
                }

                tre.codec.process(buf, outBuf);
            }
            if (outBuf.isFlag(DISCARD) || outBuf.sampleCount == 0) {
                return;
            }

            // Compute sample sampleDuration in media timescale
            Rational sampleDuration;
            if (tr.startTime == null) {
                tr.startTime = buf.timeStamp;
            }
            Rational exactSampleDuration = outBuf.sampleDuration.multiply(outBuf.sampleCount);
            sampleDuration = exactSampleDuration.floor(tr.mediaTimeScale);
            if (sampleDuration.compareTo(new Rational(0, 1)) <= 0) {
                sampleDuration = new Rational(1, tr.mediaTimeScale);
            }
            long sampleDurationInMediaTS = sampleDuration.getNumerator() * (tr.mediaTimeScale / sampleDuration.getDenominator());

            writeSamples(track, outBuf.sampleCount, (byte[]) outBuf.data, outBuf.offset, outBuf.length,
                    sampleDurationInMediaTS / outBuf.sampleCount, outBuf.isFlag(KEYFRAME));

            if (outBuf.header instanceof AvcDecoderConfigurationRecord) {
                AvcDecoderConfigurationRecord r = (AvcDecoderConfigurationRecord) outBuf.header;
                writeAvcDecoderConfigurationRecord(track, r);
            }
        }
    }


    /**
     * Encodes an image as a video frame and writes it into a video track.
     *
     * @param track    The track index.
     * @param image    The image of the video frame.
     * @param duration The sampleDuration of the video frame in media timescale
     *                 units.
     * @throws IOException if writing the sample data failed.
     */
    public void write(int track, BufferedImage image, long duration) throws IOException {
        Track tr = tracks.get(track);
        TrackEncoder tre = getTrackEncoder(track);
        Buffer buf = new Buffer();
        buf.data = image;
        buf.sampleDuration = Rational.valueOf(duration, tr.mediaTimeScale);
        buf.format = new Format(FormatKeys.MediaTypeKey, FormatKeys.MediaType.VIDEO,
                VideoFormatKeys.DataClassKey, BufferedImage.class,
                WidthKey, image.getWidth(),
                HeightKey, image.getHeight()
        );
        write(track, buf);
    }

    private TrackEncoder getTrackEncoder(int track) {
        while (trackEncoders.size() <= track) {
            trackEncoders.add(new TrackEncoder());
        }
        return trackEncoders.get(track);
    }

    /**
     * Writes a sample from a byte array into a track.
     * <p>
     * This method encodes the sample if the format of the track does not match
     * the format of the media in this track.
     *
     * @param track    The track index.
     * @param data     The sample data.
     * @param off      The start offset in the data.
     * @param len      The number of bytes to write.
     * @param duration The duration of the sample in media timescale units.
     * @param isSync   Whether the sample is a sync sample (keyframe).
     * @throws IllegalArgumentException if the sampleDuration is less than 1.
     * @throws IOException              if writing the sample data failed.
     */
    @Deprecated
    public void write(int track, byte[] data, int off, int len, long duration, boolean isSync) throws IOException {
        writeSamples(track, 1, data, off, len, duration, isSync);
    }

    /**
     * Writes multiple already encoded samples from a byte array into a track.
     * <p>
     * This method does not inspect the contents of the data. The contents has
     * to match the format and dimensions of the media in this track.
     *
     * @param track          The track index.
     * @param sampleCount    The number of samples.
     * @param data           The encoded sample data.
     * @param off            The start offset in the data.
     * @param len            The number of bytes to write. Must be dividable by
     *                       sampleCount.
     * @param sampleDuration The sampleDuration of a sample. All samples must
     *                       have the same sampleDuration.
     * @param isSync         Whether the samples are sync samples. All samples must
     *                       either be sync samples or non-sync samples.
     * @throws IllegalArgumentException if the sampleDuration is less than 1.
     * @throws IOException              if writing the sample data failed.
     */
    @Deprecated
    public void write(int track, int sampleCount, byte[] data, int off, int len, long sampleDuration, boolean isSync) throws IOException {
        AbstractQTFFMovieStream.Track tr = tracks.get(track);
        TrackEncoder tre = getTrackEncoder(track);
        if (tre.codec == null) {
            writeSamples(track, sampleCount, data, off, len, sampleDuration, isSync);
        } else {
            if (tre.outputBuffer == null) {
                tre.outputBuffer = new Buffer();
            }
            if (tre.inputBuffer == null) {
                tre.inputBuffer = new Buffer();
            }
            Buffer outb = tre.outputBuffer;
            Buffer inb = tre.inputBuffer;
            inb.data = data;
            inb.offset = off;
            inb.length = len;
            inb.sampleDuration = new Rational(sampleDuration, tr.mediaTimeScale);
            inb.sampleCount = sampleCount;
            inb.setFlag(KEYFRAME, isSync);
            tre.codec.process(inb, outb);
            if (!outb.isFlag(DISCARD)) {
                writeSample(track, (byte[]) outb.data, outb.offset, outb.length, outb.sampleCount, outb.isFlag(KEYFRAME));
            }
        }
    }

    /**
     * Returns true because MP4 supports variable frame rates.
     */
    public boolean isVFRSupported() {
        return true;
    }


    @Override
    public boolean isEmpty(int track) {
        return tracks.get(track).isEmpty();
    }
}

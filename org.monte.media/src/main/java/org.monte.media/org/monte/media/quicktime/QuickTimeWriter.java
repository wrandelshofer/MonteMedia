/*
 * @(#)MP4Writer.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime;

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
import static org.monte.media.av.codec.video.VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_ANIMATION;
import static org.monte.media.av.codec.video.VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_JPEG;
import static org.monte.media.av.codec.video.VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_RAW;
import static org.monte.media.av.codec.video.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_ANIMATION;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_JPEG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_RAW;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.PaletteKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.QualityKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * Supports writing of time-based video and audio data into a QuickTime movie
 * file (.MOV) without the need of native code.
 * <p>
 * {@code MP4Writer} works with tracks and samples. After creating a
 * {@code MP4Writer} one or more video and audio tracks can be added to
 * it. Then samples can be written into the track(s). A sample is a single
 * element in a sequence of time-ordered data. For video data a sample typically
 * consists of a single video frame, for uncompressed stereo audio data a sample
 * contains one PCM impulse per channel. Samples of compressed media data may
 * encompass larger time units.
 * <p>
 * Tracks support edit lists. An edit list specifies when to play which portion
 * of the media data at what speed. An empty edit can be used to insert an empty
 * time span, for example to offset a track from the start of the movie. Edits
 * can also be used to play the same portion of media data multiple times
 * without having it to store it more than once in the track.<br>
 * Moreover edit lists are useful for lossless cutting of media data at non-sync
 * frames. For example, MP3 layer III audio data can not be cut at arbitrary
 * frames, because audio data can be 'borrowed' from previous frames. An edit
 * list can be used to select the desired portion of the audio data, while the
 * track stores the media starting from the nearest sync frame.
 * <p>
 * Samples are stored in a QuickTime file in the same sequence as they are
 * written. In order to getCodec optimal movie playback, the samples from
 * different tracks should be interleaved from time to time. An interleave
 * should occur about twice per second. Furthermore, to overcome any latencies
 * in sound playback, at least one second of sound data needs to be placed at
 * the beginning of the movie. So that the sound and video data is offset from
 * each other in the file by one second.
 * <p>
 * For convenience, this class has built-in encoders for video frames in the
 * following formats: RAW, ANIMATION, JPEG and PNG. Media data in other formats,
 * including all audio data, must be encoded before it can be written with
 * {@code MP4Writer}. Alternatively, you can plug in your own codec.
 * <p>
 * <b>Example:</b> Writing 10 seconds of a movie with 640x480 pixel, 30 fps,
 * PNG-encoded video and 16-bit stereo, 44100 Hz, PCM-encoded audio.
 * <pre>
 * MP4Writer w = new MP4Writer(new File("mymovie.mov"));
 * w.addAudioTrack(new AudioFormat(AudioFormat.Encoding.PCM_SIGNED), 44100, 2, 16, 2, 44100, true)); // audio in track 0
 * w.addVideoTrack(MP4Writer.VIDEO_PNG, 30, 640, 480);  // video in track 1
 *
 * // calculate total movie sampleDuration in media time units for each track
 * long atmax = w.getMediaTimeScale(0) * 10;
 * long vtmax = w.getMediaTimeScale(1) * 10;
 *
 * // sampleDuration of a single sample
 * long asduration = 1;
 * long vsduration = 1;
 *
 * // half a second in media time units (we interleave twice per second)
 * long atstep = w.getMediaTimeScale(0) / 2;
 * long vtstep = w.getMediaTimeScale(1) / 2;
 *
 * // the time when the next interleave occurs in media time units
 * long atnext = w.getMediaTimeScale(0); // offset audio by 1 second
 * long vtnext = 0;
 *
 * // the current time in media time units
 * long atime = 0;
 * long vtime = 0;
 *
 * // create buffers
 * int asamplesize = 2 * 2; // 16-bit stereo * 2 channels
 * byte[] audio=new byte[atstep * asamplesize];
 * BufferedImage img=new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
 *
 * // main loop
 * while (atime &lt; atmax || vtime &lt; vtmax) {
 *      atnext = Math.min(atmax, atnext + atstep); // advance audio to next interleave time
 *      while (atime &lt; atnext) { // catch up with audio time
 *          int sampleDuration = (int) Math.min(audio.length / asamplesize, atmax - atime);
 *          ...fill in audio data for time "atime" and sampleDuration "sampleDuration" here...
 *          w.writeSamples(0, sampleDuration, audio, 0, sampleDuration * asamplesize, asduration);
 *          atime += sampleDuration;
 *      }
 *      vtnext = Math.min(vtmax, vtnext + vtstep); // advance video to next interleave time
 *      while (vtime &lt; vtnext) { // catch up with video time
 *          int sampleDuration = (int) Math.min(1, vtmax - vtime);
 *          ...fill in image data for time "vtime" and sampleDuration "sampleDuration" here...
 *          w.write(1, img, vsduration);
 *          vtime += sampleDuration;
 *      }
 * }
 * w.close();
 * </pre>
 * <p>
 * For information about the QuickTime file format see the "QuickTime File
 * Format Specification", Apple Inc. 2010-08-03. (qtff)
 * <a href="http://developer.apple.com/library/mac/documentation/QuickTime/QTFF/qtff.pdf/">
 * http://developer.apple.com/library/mac/documentation/QuickTime/QTFF/qtff.pdf
 * </a>
 *
 * @author Werner Randelshofer
 */
public class QuickTimeWriter extends QuickTimeOutputStream implements MovieWriter {
    private static class TrackEncoder {
        /**
         * The codec.
         */
        public Codec codec;
        public Buffer outputBuffer;
        public Buffer inputBuffer;
    }

    private List<TrackEncoder> trackEncoders = new ArrayList<>();

    public final static Format QUICKTIME = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_QUICKTIME);
    public final static Format VIDEO_RAW = new Format(
            MediaTypeKey, MediaType.VIDEO,//
            MimeTypeKey, MIME_QUICKTIME,
            EncodingKey, ENCODING_QUICKTIME_RAW,//
            CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_RAW);
    public final static Format VIDEO_ANIMATION = new Format(
            MediaTypeKey, MediaType.VIDEO, //
            MimeTypeKey, MIME_QUICKTIME,
            EncodingKey, ENCODING_QUICKTIME_ANIMATION, //
            CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_ANIMATION);
    public final static Format VIDEO_JPEG = new Format(
            MediaTypeKey, MediaType.VIDEO,//
            MimeTypeKey, MIME_QUICKTIME,
            EncodingKey, ENCODING_QUICKTIME_JPEG, //
            CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_JPEG);
    public final static Format VIDEO_PNG = new Format(
            MediaTypeKey, MediaType.VIDEO,//
            MimeTypeKey, MIME_QUICKTIME,
            EncodingKey, ENCODING_QUICKTIME_PNG, //
            CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_PNG);

    /**
     * Creates a new QuickTime writer.
     *
     * @param file the output file
     */
    public QuickTimeWriter(File file) throws IOException {
        super(file);
    }

    /**
     * Creates a new QuickTime writer.
     *
     * @param out the output stream.
     */
    public QuickTimeWriter(ImageOutputStream out) throws IOException {
        super(out);
    }


    @Override
    public Format getFileFormat() throws IOException {
        return QUICKTIME;
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
     * @param format    The QuickTime video format.
     * @param timeScale The media time scale. This is typically the frame rate.
     *                  If the frame rate is not an integer fraction of a second, specify a
     *                  multiple of the frame rate and specify a correspondingly multiplied
     *                  sampleDuration when writing frames. For example, for a rate of 23.976 fps
     *                  specify a time scale of 23976 and multiply the sampleDuration of a video
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
     * @param format The QuickTime video format.
     * @param width  The width of a video image. Must be larger than 0.
     * @param height The height of a video image. Must be larger than 0.
     * @return Returns the track index.
     * @throws IllegalArgumentException if the width or the height is smaller
     *                                  than 1.
     */
    public int addVideoTrack(Format format, int width, int height, int depth, int syncInterval) throws IOException {
        int tr = addVideoTrack(format.get(EncodingKey), format.get(CompressorNameKey),
                format.get(FrameRateKey).getDenominator() * format.get(FrameRateKey).getNumerator(), width, height, depth, syncInterval, format);
        setVideoColorTable(tr, format.get(PaletteKey));
        return tr;
    }

    /**
     * Adds an audio track, and configures it using an {@code AudioFormat}
     * object from the javax.sound API.
     * <p>
     * Use this method for writing audio data from an {@code AudioInputStream}
     * into a QuickTime Movie file.
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

        if (tre.outputBuffer == null) {
            tre.outputBuffer = new Buffer();
        }
        Buffer outBuf;
        if (tr.format.matchesWithout(buf.format, FrameRateKey)) {
            outBuf = buf;
        } else {
            outBuf = tre.outputBuffer;
            if (tre.codec == null) {
                createCodec(track);
                if (tre.codec == null) {
                    throw new UnsupportedOperationException("No codec for this format " + tr.format);
                }
            }
            //FIXME we assume a single-step encoding process
            tre.codec.process(buf, outBuf);
        }
        if (outBuf.isFlag(DISCARD) || outBuf.sampleCount == 0) {
            return;
        }


        // Compute sample sampleDuration in media time scale
        long sampleDuration = Math.max(1, outBuf.sampleDuration.multiply(tr.mediaTimeScale).longValue());
        writeSamples(track, outBuf.sampleCount, (byte[]) outBuf.data, outBuf.offset, outBuf.length,
                sampleDuration, outBuf.isFlag(KEYFRAME));

        if (outBuf.header instanceof AvcDecoderConfigurationRecord r && tr instanceof VideoTrack vtr) {
            vtr.avcDecoderConfigurationRecord = r;
        }
    }


    private TrackEncoder getTrackEncoder(int track) {
        while (trackEncoders.size() <= track) {
            trackEncoders.add(new TrackEncoder());
        }
        return trackEncoders.get(track);
    }


    /**
     * Encodes an image as a video frame and writes it into a video track.
     *
     * @param track    The track index.
     * @param image    The image of the video frame.
     * @param duration The sampleDuration of the video frame in media time scale
     *                 units.
     * @throws IOException if writing the sample data failed.
     */
    public void write(int track, BufferedImage image, long duration) throws IOException {
        Track tr = tracks.get(track);

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
     * @param duration The duration of the sample in media time scale units.
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
     * Returns true because QuickTime supports variable frame rates.
     */
    public boolean isVFRSupported() {
        return true;
    }


    @Override
    public boolean isEmpty(int track) {
        return tracks.get(track).isEmpty();
    }
}

/*
 * @(#)AVIWriter.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.avi;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.Registry;
import org.monte.media.io.ByteArrayImageOutputStream;
import org.monte.media.math.Rational;
import org.monte.media.riff.RIFFParser;

import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.KEYFRAME;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ChannelsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_ALAW;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_MP3;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_PCM_SIGNED;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_PCM_UNSIGNED;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_ULAW;
import static org.monte.media.av.codec.audio.AudioFormatKeys.EncodingKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.FrameRateKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.FrameSizeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.KeyFrameIntervalKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.MIME_AVI;
import static org.monte.media.av.codec.audio.AudioFormatKeys.MediaType;
import static org.monte.media.av.codec.audio.AudioFormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleRateKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleSizeInBitsKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.COMPRESSOR_NAME_QUICKTIME_RAW;
import static org.monte.media.av.codec.video.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_DIB;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_MJPG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_RLE8;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.FixedFrameRateKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.MotionSearchRangeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.PaletteKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.QualityKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * Provides high-level support for encoding and writing audio and video samples
 * into an AVI 1.0 file.
 *
 * @author Werner Randelshofer
 */
public class AVIWriter extends AVIOutputStream implements MovieWriter {
    private static class TrackEncoder {
        /**
         * The codec.
         */
        public Codec codec;
        public Buffer outputBuffer;
        public Buffer inputBuffer;
    }

    private List<TrackEncoder> trackEncoders = new ArrayList<>();

    public final static Format AVI = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI);
    public final static Format VIDEO_RAW = new Format(
            MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
            EncodingKey, ENCODING_AVI_DIB, CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_RAW);
    public final static Format VIDEO_JPEG = new Format(
            MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
            EncodingKey, ENCODING_AVI_MJPG, CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_RAW);
    public final static Format VIDEO_PNG = new Format(
            MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
            EncodingKey, ENCODING_AVI_PNG, CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_RAW);
    public final static Format VIDEO_RLE = new Format(
            MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
            EncodingKey, ENCODING_AVI_RLE8, CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_RAW);
    public final static Format VIDEO_SCREEN_CAPTURE = new Format(
            MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_AVI,
            EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE, CompressorNameKey, COMPRESSOR_NAME_QUICKTIME_RAW);
    /**
     * Creates a new AVI writer.
     *
     * @param file the output file
     */
    public AVIWriter(File file) throws IOException {
        super(file);
    }

    /**
     * Creates a new AVI writer.
     *
     * @param out the output stream.
     */
    public AVIWriter(ImageOutputStream out) throws IOException {
        super(out);
    }


    @Override
    public Format getFileFormat() throws IOException {
        return AVI;
    }

    @Override
    public Format getFormat(int track) {
        return tracks.get(track).format;
    }

    /**
     * Returns the media duration of the track in seconds.
     */
    @Override
    public Rational getDuration(int track) {
        AVIOutputStream.Track tr = tracks.get(track);
        long duration = getMediaDuration(track);
        return new Rational(duration * tr.scale, tr.rate);
    }

    /**
     * Adds a track.
     *
     * @param format The format of the track.
     * @return The track number.
     */
    @Override
    public int addTrack(Format format) throws IOException {
        switch (format.get(MediaTypeKey, MediaType.VIDEO)) {
            case VIDEO:
                return addVideoTrack(format);
            case AUDIO:
                return addAudioTrack(format);
            default:
                throw new IllegalArgumentException("VIDEO or AUDIO format expected: " + format);

        }
    }

    /**
     * Adds a video track.
     *
     * @param vf The format of the track.
     * @return The track number.
     */
    private int addVideoTrack(Format vf) throws IOException {
        vf.requireKeys(EncodingKey, FrameRateKey, WidthKey, HeightKey);
        int tr = addVideoTrack(vf.get(EncodingKey),
                vf.get(FrameRateKey).getDenominator(), vf.get(FrameRateKey).getNumerator(),
                vf.get(WidthKey), vf.get(HeightKey), vf.get(DepthKey, 24),
                vf.get(KeyFrameIntervalKey, vf.get(FrameRateKey).floor(1).intValue())
        );
        setPalette(tr, vf.get(PaletteKey));
        setCompressionQuality(tr, vf.get(QualityKey, 1.0f));
        setMotionSearchRange(tr, vf.get(MotionSearchRangeKey, -1));
        return tr;
    }

    /**
     * Adds an audio track.
     *
     * @param format The format of the track.
     * @return The track number.
     */
    private int addAudioTrack(Format format) throws IOException {
        int waveFormatTag = 0x0001; // WAVE_FORMAT_PCM

        long timeScale = 1;
        long sampleRate = format.get(SampleRateKey, new Rational(41000, 1)).longValue();
        int numberOfChannels = format.get(ChannelsKey, 1);
        int sampleSizeInBits = format.get(SampleSizeInBitsKey, 16); //
        boolean isCompressed = false; // FIXME
        int frameDuration = 1;
        int frameSize = format.get(FrameSizeKey, (sampleSizeInBits + 7) / 8 * numberOfChannels);

        String enc = format.get(EncodingKey);
        if (enc == null) {
            waveFormatTag = 0x0001; // WAVE_FORMAT_PCM
        } else if (enc.equals(ENCODING_ALAW)) {
            waveFormatTag = 0x0001; // WAVE_FORMAT_PCM
        } else if (enc.equals(ENCODING_PCM_SIGNED)) {
            waveFormatTag = 0x0001; // WAVE_FORMAT_PCM
        } else if (enc.equals(ENCODING_PCM_UNSIGNED)) {
            waveFormatTag = 0x0001; // WAVE_FORMAT_PCM
        } else if (enc.equals(ENCODING_ULAW)) {
            waveFormatTag = 0x0001; // WAVE_FORMAT_PCM
        } else if (enc.equals(ENCODING_MP3)) {
            waveFormatTag = 0x0001; // WAVE_FORMAT_PCM - FIXME
        } else {
            waveFormatTag = RIFFParser.stringToID(format.get(EncodingKey)) & 0xffff;
        }

        return addAudioTrack(waveFormatTag, //
                timeScale, sampleRate, //
                numberOfChannels, sampleSizeInBits, //
                isCompressed, //
                frameDuration, frameSize);
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

    @Override
    public int getTrackCount() {
        return tracks.size();
    }

    /**
     * Encodes the provided image and writes its sample data into the specified
     * track.
     *
     * @param track    The track index.
     * @param image    The image of the video frame.
     * @param duration Duration given in media time units (=number of frames to be written).
     * @throws IOException if writing the sample data failed.
     */
    public void write(int track, BufferedImage image, long duration) throws IOException {
        ensureStarted();

        AbstractAVIStream.VideoTrack vt = (AbstractAVIStream.VideoTrack) tracks.get(track);
        TrackEncoder tre = getTrackEncoder(track);
        if (tre.codec == null) {
            createCodec(track);
            if (tre.codec == null) {
                throw new IOException("No codec for this format: " + vt.format);
            }
        }

        // The dimension of the image must match the dimension of the video track
        Format fmt = vt.format;
        if (fmt.get(WidthKey) != image.getWidth() || fmt.get(HeightKey) != image.getHeight()) {
            throw new IllegalArgumentException("Dimensions of image[" + vt.samples.size()
                    + "] (width=" + image.getWidth() + ", height=" + image.getHeight()
                    + ") differs from video format of track: " + fmt);
        }

        boolean isKeyframe = vt.syncInterval == 0 ? false : vt.samples.size() % vt.syncInterval == 0;
        Buffer inputBuffer = new Buffer();
        inputBuffer.flags = (isKeyframe) ? EnumSet.of(KEYFRAME) : EnumSet.noneOf(BufferFlag.class);
        inputBuffer.data = image;
        inputBuffer.header = image.getColorModel();
        inputBuffer.format = new Format(EncodingKey, ENCODING_BUFFERED_IMAGE);
        inputBuffer.sampleDuration = new Rational(vt.scale, vt.rate);
        write(track, inputBuffer);
    }

    /**
     * Encodes the data provided in the buffer and then writes it into the
     * specified track.
     * <p>
     * Does nothing if the discard-flag in the buffer is set to true.
     *
     * @param track The track number.
     * @param buf   The buffer containing a data sample. The duration of the buffer must match the
     *              sample rate of the track.
     */
    @Override
    public void write(int track, Buffer buf) throws IOException {
        ensureStarted();
        if (buf.flags.contains(DISCARD)) {
            return;
        }

        AbstractAVIStream.Track tr = tracks.get(track);
        TrackEncoder tre = getTrackEncoder(track);

        boolean isKeyframe = buf.flags.contains(KEYFRAME);
        if (buf.data instanceof BufferedImage) {
            if (tr.syncInterval != 0) {
                isKeyframe = buf.flags.contains(KEYFRAME) | (tr.samples.size() % tr.syncInterval == 0);
            }
        }
        // Encode palette data
        final boolean paletteChange;
        if (buf.header instanceof IndexColorModel && tr instanceof AbstractAVIStream.VideoTrack) {
            paletteChange = writePalette(track, (IndexColorModel) buf.header, isKeyframe);
        } else {
            paletteChange = false;
        }
        // Encode sample data
        {
            if (buf.format == null) {
                throw new IllegalArgumentException("Buffer.format must not be null");
            }
            if (buf.format.matchesWithout(tr.format, FrameRateKey) && buf.data instanceof byte[]) {
                writeSamples(track, buf.sampleCount, (byte[]) buf.data, buf.offset, buf.length,
                        buf.isFlag(KEYFRAME) && !paletteChange);
                return;
            }

            // We got here, because the buffer format does not match the track 
            // format. Let's see if we can create a codec which can perform the
            // encoding for us.
            if (tre.codec == null) {
                createCodec(track);
                if (tre.codec == null) {
                    throw new UnsupportedOperationException("No codec for this format " + tr.format);
                }
            }

            if (tre.outputBuffer == null) {
                tre.outputBuffer = new Buffer();
            }
            Buffer outBuf = tre.outputBuffer;
            if (tre.codec.process(buf, outBuf) != Codec.CODEC_OK) {
                throw new IOException("Codec failed or could not encode the sample in a single step. codec:" + tre.codec);
            }
            if (outBuf.isFlag(DISCARD)) {
                return;
            }
            writeSamples(track, outBuf.sampleCount, (byte[]) outBuf.data, outBuf.offset, outBuf.length,
                    isKeyframe && !paletteChange);
        }
    }

    private TrackEncoder getTrackEncoder(int track) {
        while (trackEncoders.size() <= track) {
            trackEncoders.add(new TrackEncoder());
        }
        return trackEncoders.get(track);
    }


    private boolean writePalette(int track, BufferedImage image, boolean isKeyframe) throws IOException {
        if ((image.getColorModel() instanceof IndexColorModel)) {
            return writePalette(track, (IndexColorModel) image.getColorModel(), isKeyframe);
        }
        return false;
    }

    private boolean writePalette(int track, IndexColorModel imgPalette, boolean isKeyframe) throws IOException {
        ensureStarted();

        AbstractAVIStream.VideoTrack vt = (AbstractAVIStream.VideoTrack) tracks.get(track);
        int imgDepth = vt.bitCount;
        ByteArrayImageOutputStream tmp = null;
        boolean paletteChange = false;
        if (vt.samples.isEmpty()) {
            vt.palette = imgPalette;
            vt.previousPalette = imgPalette;
        }

        switch (imgDepth) {
            case 4: {
                int[] imgRGBs = new int[16];
                imgPalette.getRGBs(imgRGBs);
                int[] previousRGBs = new int[16];
                vt.previousPalette.getRGBs(previousRGBs);
                if (isKeyframe || !Arrays.equals(imgRGBs, previousRGBs)) {
                    paletteChange = true;
                    /*
                     int first = imgPalette.getMapSize();
                     int last = -1;
                     for (int i = 0; i < 16; i++) {
                     if (previousRGBs[i] != imgRGBs[i] && i < first) {
                     first = i;
                     }
                     if (previousRGBs[i] != imgRGBs[i] && i > last) {
                     last = i;
                     }
                     }*/
                    int first = 0;
                    int last = imgPalette.getMapSize() - 1;
                    /*
                     * typedef struct {
                     BYTE         bFirstEntry;
                     BYTE         bNumEntries;
                     WORD         wFlags;
                     PALETTEENTRY peNew[];
                     } AVIPALCHANGE;
                     *
                     * typedef struct tagPALETTEENTRY {
                     BYTE peRed;
                     BYTE peGreen;
                     BYTE peBlue;
                     BYTE peFlags;
                     } PALETTEENTRY;
                     */
                    tmp = new ByteArrayImageOutputStream(ByteOrder.LITTLE_ENDIAN);
                    tmp.writeByte(first);//bFirstEntry
                    tmp.writeByte(last - first + 1);//bNumEntries
                    tmp.writeShort(0);//wFlags

                    for (int i = first; i <= last; i++) {
                        tmp.writeByte((imgRGBs[i] >>> 16) & 0xff); // red
                        tmp.writeByte((imgRGBs[i] >>> 8) & 0xff); // green
                        tmp.writeByte(imgRGBs[i] & 0xff); // blue
                        tmp.writeByte(0); // reserved*/
                    }

                }
                break;
            }
            case 8: {
                //IndexColorModel imgPalette = (IndexColorModel) image.getColorModel();
                int[] imgRGBs = new int[256];
                imgPalette.getRGBs(imgRGBs);
                int[] previousRGBs = new int[256];
                if (vt.previousPalette != null) {
                    vt.previousPalette.getRGBs(previousRGBs);
                }
                if (isKeyframe || !Arrays.equals(imgRGBs, previousRGBs)) {
                    paletteChange = true;
                    int first = 0;
                    int last = imgPalette.getMapSize() - 1;
                    /*
                     * typedef struct {
                     BYTE         bFirstEntry;
                     BYTE         bNumEntries;
                     WORD         wFlags;
                     PALETTEENTRY peNew[];
                     } AVIPALCHANGE;
                     *
                     * typedef struct tagPALETTEENTRY {
                     BYTE peRed;
                     BYTE peGreen;
                     BYTE peBlue;
                     BYTE peFlags;
                     } PALETTEENTRY;
                     */
                    tmp = new ByteArrayImageOutputStream(ByteOrder.LITTLE_ENDIAN);
                    tmp.writeByte(first);//bFirstEntry
                    tmp.writeByte(last - first + 1);//bNumEntries
                    tmp.writeShort(0);//wFlags
                    for (int i = first; i <= last; i++) {
                        tmp.writeByte((imgRGBs[i] >>> 16) & 0xff); // red
                        tmp.writeByte((imgRGBs[i] >>> 8) & 0xff); // green
                        tmp.writeByte(imgRGBs[i] & 0xff); // blue
                        tmp.writeByte(0); // reserved*/
                    }
                }
                break;
            }
        }
        if (tmp != null) {
            tmp.close();
            writePalette(track, tmp.getBuffer(), 0, (int) tmp.length(), isKeyframe);
        }

        if (paletteChange) {
            vt.previousPalette = imgPalette;
        }

        return paletteChange;
    }

    private void createCodec(int track) {
        AbstractAVIStream.Track tr = tracks.get(track);
        TrackEncoder tre = getTrackEncoder(track);
        Format fmt = tr.format;
        tre.codec = Registry.getInstance().getEncoder(fmt);
        if (tre.codec != null) {
            if (fmt.get(MediaTypeKey) == MediaType.VIDEO) {
                tre.codec.setInputFormat(fmt.prepend(
                        EncodingKey, ENCODING_BUFFERED_IMAGE,
                        DataClassKey, BufferedImage.class));
                if (null == tre.codec.setOutputFormat(
                        fmt.prepend(FixedFrameRateKey, true,
                                QualityKey, getCompressionQuality(track),
                                MotionSearchRangeKey, getMotionSearchRange(track),
                                MimeTypeKey, MIME_AVI,
                                DataClassKey, byte[].class))) {
                    throw new UnsupportedOperationException("Track " + tr + " codec does not support format " + fmt + ". codec=" + tre.codec);
                }
            } else {
                tre.codec.setInputFormat(null);
                if (null == tre.codec.setOutputFormat(
                        fmt.prepend(FixedFrameRateKey, true,
                                QualityKey, getCompressionQuality(track),
                                MimeTypeKey, MIME_AVI,
                                DataClassKey, byte[].class))) {
                    throw new UnsupportedOperationException("Track " + tr + " codec " + tre.codec + " does not support format. " + fmt);
                }
            }
        }
    }

    public boolean isVFRSupported() {
        return false;
    }

    @Override
    public boolean isEmpty(int track) {
        return tracks.get(track).samples.isEmpty();
    }
}

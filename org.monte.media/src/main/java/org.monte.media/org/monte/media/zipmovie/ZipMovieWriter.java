/*
 * @(#)ZipMovieWriter.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.zipmovie;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.Registry;
import org.monte.media.av.codec.video.VideoFormatKeys;
import org.monte.media.math.Rational;
import org.monte.media.util.MathUtil;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.KEYFRAME;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_AVI;
import static org.monte.media.av.FormatKeys.MIME_ZIP;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.FixedFrameRateKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.QualityKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * Supports writing of images into a ZIP file.
 * <p>
 * All files with extension .png are treated as video frames.
 */
public class ZipMovieWriter implements MovieWriter {
    private final ZipOutputStream out;
    public final static Format ZIP = new Format(MediaTypeKey, FormatKeys.MediaType.FILE, MimeTypeKey, MIME_ZIP);

    public ZipMovieWriter(File file) throws FileNotFoundException {
        this(new ZipOutputStream(new FileOutputStream(file)));
    }

    public ZipMovieWriter(OutputStream out) {
        this(new ZipOutputStream(out));
    }

    private static class Track {
        private final Format format;
        public Codec codec;
        public Buffer outputBuffer;
        public float quality = 1;
        public int scale = 1;
        public int rate = 10;
        private int numSamples;
        private final int id;

        private Track(int id, Format format) {
            this.format = format;
            this.id = id;
        }
    }

    private final List<Track> tracks = new ArrayList<>();


    public ZipMovieWriter(ZipOutputStream out) {
        this.out = out;
    }

    @Override
    public Format getFileFormat() throws IOException {
        return ZIP;
    }

    @Override
    public void setFileFormat(Format newValue) throws IOException {
        // ignore
    }

    @Override
    public int addTrack(Format format) throws IOException {
        Track tr = new Track(tracks.size(), format.prepend(EncodingKey, VideoFormatKeys.ENCODING_QUICKTIME_PNG));
        tr.quality = format.get(QualityKey, 1f);
        tracks.add(tr);
        return tracks.size() - 1;
    }

    @Override
    public void setCodec(int trackIndex, Codec codec) {
        // do nothing
    }

    @Override
    public Format getFormat(int track) {
        return tracks.get(track).format;
    }

    @Override
    public int getTrackCount() {
        return tracks.size();
    }

    @Override
    public void write(int track, Buffer buf) throws IOException {
        if (buf.flags.contains(DISCARD)) {
            return;
        }

        Track tr = tracks.get(track);
        if (!(buf.data instanceof byte[])) {
            if (tr.codec == null) {
                createCodec(track);
                if (tr.codec == null) {
                    throw new UnsupportedOperationException("No codec for this format " + tr.format);
                }
            }
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
        writeSamples(track, outBuf.sampleCount, (byte[]) outBuf.data, outBuf.offset, outBuf.length);
    }

    private void writeSamples(int track, int sampleCount, byte[] data, int offset, int length) throws IOException {
        if (sampleCount != 1) throw new IOException("only sampleCount=1 supported");

        Track tr = tracks.get(track);
        out.putNextEntry(new ZipEntry("tr%02d-fr%04d.png".formatted(track, tr.numSamples++)));
        out.write(data, offset, length);
        out.closeEntry();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public boolean isDataLimitReached() {
        return false;
    }

    @SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
    @Override
    public Rational getDuration(int track) {
        Track tr = tracks.get(track);
        return Rational.valueOf(tr.numSamples * tr.rate, tr.scale);
    }

    @Override
    public boolean isEmpty(int track) {
        return tracks.get(track).numSamples == 0;
    }

    public void setCompressionQuality(int track, float newValue) {
        Track t = tracks.get(track);
        t.quality = MathUtil.clamp((int) (newValue * 10_000f), 0, 10_000);
    }

    /**
     * Returns the compression quality of a track.
     *
     * @return compression quality
     */
    public float getCompressionQuality(int track) {
        Track t = tracks.get(track);
        return t.quality == -1 ? 0.97f : MathUtil.clamp(t.quality / 10_000f, 0f, 1f);
    }

    private Codec createCodec(Format fmt) {
        return Registry.getInstance().getEncoder(fmt.prepend(MimeTypeKey, MIME_AVI));
    }


    private void createCodec(int track) {
        Track tr = tracks.get(track);
        Format fmt = tr.format;
        tr.codec = createCodec(fmt);
        if (tr.codec != null) {
            if (fmt.get(MediaTypeKey) == FormatKeys.MediaType.VIDEO) {
                tr.codec.setInputFormat(fmt.prepend(
                        EncodingKey, ENCODING_BUFFERED_IMAGE,
                        DataClassKey, BufferedImage.class));
                if (null == tr.codec.setOutputFormat(
                        fmt.prepend(FixedFrameRateKey, true,
                                QualityKey, getCompressionQuality(track),
                                MimeTypeKey, MIME_ZIP,
                                DataClassKey, byte[].class))) {
                    throw new UnsupportedOperationException("Track " + tr + " codec does not support format " + fmt + ". codec=" + tr.codec);
                }
            } else {
                tr.codec.setInputFormat(null);
                if (null == tr.codec.setOutputFormat(
                        fmt.prepend(FixedFrameRateKey, true,
                                QualityKey, getCompressionQuality(track),
                                MimeTypeKey, MIME_ZIP,
                                DataClassKey, byte[].class))) {
                    throw new UnsupportedOperationException("Track " + tr + " codec " + tr.codec + " does not support format. " + fmt);
                }
            }
        }
    }


    public void write(int track, BufferedImage image, long duration) throws IOException {


        Track vt = tracks.get(track);
        if (vt.codec == null) {
            createCodec(track);
            if (vt.codec == null) {
                throw new IOException("No codec for this format: " + vt.format);
            }
        }

        // The dimension of the image must match the dimension of the video track
        Format fmt = vt.format;
        if (fmt.get(WidthKey) != image.getWidth() || fmt.get(HeightKey) != image.getHeight()) {
            throw new IllegalArgumentException("Dimensions of image[" + vt.numSamples
                    + "] (width=" + image.getWidth() + ", height=" + image.getHeight()
                    + ") differs from video format of track: " + fmt);
        }

        boolean isKeyframe = true;
        Buffer inputBuffer = new Buffer();
        inputBuffer.flags = (isKeyframe) ? EnumSet.of(KEYFRAME) : EnumSet.noneOf(BufferFlag.class);
        inputBuffer.data = image;
        inputBuffer.header = image.getColorModel();
        inputBuffer.format = new Format(EncodingKey, ENCODING_BUFFERED_IMAGE);
        inputBuffer.sampleDuration = new Rational(vt.scale, vt.rate);
        write(track, inputBuffer);
    }
}

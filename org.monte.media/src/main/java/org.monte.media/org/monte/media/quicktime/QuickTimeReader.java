/*
 * @(#)QuickTimeReader.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.MovieReader;
import org.monte.media.av.Registry;
import org.monte.media.math.Rational;
import org.monte.media.util.ArrayUtil;
import org.monte.media.util.MathUtil;

import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;

import static java.lang.Math.min;
import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.END_OF_MEDIA;
import static org.monte.media.av.BufferFlag.KEYFRAME;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.FrameSizeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

/**
 * {@code QuickTimeReader}.
 *
 * @author Werner Randelshofer
 */
public class QuickTimeReader extends QuickTimeInputStream implements MovieReader {

    private Buffer[] inputBuffers = null;
    private Codec[] codecs = null;
    public final static Format QUICKTIME = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_QUICKTIME);

    /**
     * Creates a new instance.
     *
     * @param file the input file
     */
    public QuickTimeReader(File file) throws IOException {
        super(file);
    }

    /**
     * Creates a new instance.
     *
     * @param in the input stream.
     */
    public QuickTimeReader(ImageInputStream in) throws IOException {
        super(in);
    }

    @Override
    public long findSampleAtTime(int track, Rational seconds) throws IOException {
        ensureRealized();
        QuickTimeMeta.Track tr = meta.tracks.get(track);
        long timeStamp = seconds.multiply(meta.timeScale).longValue();
        QuickTimeMeta.TrackSample key = new QuickTimeMeta.TrackSample(null, timeStamp, 0, timeStamp, 0);
        int result = Collections.binarySearch(tr.trackSamplesList, key, Comparator.comparingLong(a -> a.timeStamp));
        if (result < 0) result = ~result - 1;
        result = MathUtil.clamp(result, 0, tr.trackSamplesList.size() - 1);
        return result;
    }

    @Override
    public Rational getSampleTime(int track, long sample) throws IOException {
        ensureRealized();
        QuickTimeMeta.Track tr = meta.tracks.get(track);
        return new Rational(tr.trackSamplesList.get((int) sample).timeStamp, meta.timeScale);
    }

    @Override
    public Rational getSampleDuration(int track, long sample) throws IOException {
        ensureRealized();
        QuickTimeMeta.Track tr = meta.tracks.get(track);
        return new Rational(tr.trackSamplesList.get((int) sample).duration, meta.timeScale);
    }

    @Override
    public Format getFileFormat() throws IOException {
        ensureRealized();
        return meta.getFileFormat();
    }

    @Override
    public Format getFormat(int track) throws IOException {
        ensureRealized();
        return meta.getFormat(track);
    }

    @Override
    public long getChunkCount(int track) throws IOException {
        ensureRealized();
        return meta.tracks.get(track).media.sampleCount;
    }

    protected void ensureRealized() throws IOException {
        if (inputBuffers == null) {
            super.ensureRealized();
            inputBuffers = new Buffer[meta.getTrackCount()];
            codecs = new Codec[meta.getTrackCount()];
            for (int i = 0; i < inputBuffers.length; i++) {
                inputBuffers[i] = new Buffer();
            }
        }
    }

    /**
     * Reads an image.
     *
     * @param track The track number
     * @param img   An image that can be reused if it fits the media format of the
     *              track. Pass null to create a new image on each read.
     * @return An image or null if the end of the media has been reached.
     * @throws IOException if an I/O error occurs
     */
    public BufferedImage read(int track, BufferedImage img) throws IOException {
        ensureRealized();
        QuickTimeMeta.Track tr = meta.tracks.get(track);
        if (codecs[track] == null) {
            createCodec(track);
        }
        Buffer buf = new Buffer();
        buf.data = img;
        do {
            read(track, inputBuffers[track]);
            // FIXME - We assume a one-step codec here!
            codecs[track].process(inputBuffers[track], buf);
        } while (buf.isFlag(DISCARD) && !buf.isFlag(END_OF_MEDIA));

        if (buf.isFlag(END_OF_MEDIA)) {
            return null;
        }

        return (BufferedImage) buf.data;
    }

    @Override
    public void read(int track, Buffer buffer) throws IOException {
        ensureRealized();
        QuickTimeMeta.Track tr = meta.tracks.get(track);
        if (tr.readIndex >= tr.trackSamplesList.size()) {
            buffer.setFlagsTo(END_OF_MEDIA, DISCARD);
            buffer.length = 0;
            return;
        }
        buffer.sequenceNumber = tr.readIndex;
        var ts = tr.trackSamplesList.get((int) tr.readIndex);
        var ms = ts.mediaSample;

        in.seek(ms.offset);
        byte[] b;
        buffer.data = b = ArrayUtil.reuseByteArray(buffer.data, (int) ms.length);
        in.readFully(b, 0, (int) ms.length);
        buffer.offset = 0;
        buffer.length = (int) ms.length;


        switch (tr.mediaType) {
            case AUDIO -> {
                Format af = tr.format;
                buffer.sampleCount = buffer.length / Math.max(1, af.get(FrameSizeKey));
            }
            default -> buffer.sampleCount = 1;
        }
        buffer.format = tr.format;
        buffer.track = track;
        buffer.sampleDuration = new Rational(ts.duration, meta.timeScale);
        buffer.timeStamp = new Rational(ts.timeStamp, meta.timeScale);
        buffer.flags = ms.isKeyframe ? EnumSet.of(KEYFRAME) : EnumSet.noneOf(BufferFlag.class);
        tr.readIndex++;
    }

    @Override
    public int nextTrack() throws IOException {
        ensureRealized();
        throw new UnsupportedOperationException("nextTrack() not supported yet.");
    }


    @Override
    public void setMovieReadTime(Rational newValue) throws IOException {
        ensureRealized();
        for (int t = 0, n = meta.tracks.size(); t < n; t++) {
            QuickTimeMeta.Track tr = meta.tracks.get(t);
            int sample = (int) min(findSampleAtTime(t, newValue), tr.media.sampleCount - 1);
            for (; sample > 0 && !tr.trackSamplesList.get(sample).mediaSample.isKeyframe; sample--) ;
            tr.readIndex = sample;
        }
    }

    @Override
    public Rational getReadTime(int track) throws IOException {
        QuickTimeMeta.Track tr = meta.tracks.get(track);
        return getSampleTime(track, tr.readIndex);
    }

    @Override
    public Rational getMovieDuration() throws IOException {
        return new Rational(getMovieDurationInMovieTimeScale(), getMovieTimeScale());
    }

    @Override
    public Rational getTrackDuration(int track) throws IOException {
        ensureRealized();
        QuickTimeMeta.Track tr = meta.tracks.get(track);
        return new Rational(tr.duration, meta.timeScale);
    }


    @Override
    public int findTrack(int fromTrack, Format format) throws IOException {
        for (int i = fromTrack, n = getTrackCount(); i < n; i++) {
            if (getFormat(i).matches(format)) {
                return i;
            }
        }
        return -1;
    }

    private void createCodec(int track) throws IOException {
        QuickTimeMeta.Track tr = meta.tracks.get(track);
        Format fmt = meta.getFormat(track);
        Codec codec = createCodec(fmt);
        if (codec == null) {
            throw new IOException("Track " + tr + " no codec found for format " + fmt);
        } else {
            if (fmt.get(MediaTypeKey) == MediaType.VIDEO) {
                if (null == codec.setInputFormat(fmt)) {
                    throw new IOException("Track " + tr + " codec " + codec + " does not support input format " + fmt + ". codec=" + codec);
                }
                Format outFormat = fmt.prepend(MediaTypeKey, MediaType.VIDEO,//
                        MimeTypeKey, MIME_JAVA,
                        EncodingKey, ENCODING_BUFFERED_IMAGE, DataClassKey, BufferedImage.class);
                if (null == codec.setOutputFormat(outFormat)) {
                    throw new IOException("Track " + tr + " codec does not support output format " + outFormat + ". codec=" + codec);
                }
            }
        }

        codecs[track] = codec;
    }

    private Codec createCodec(Format fmt) {
        return Registry.getInstance().getDecoder(fmt.prepend(MimeTypeKey, MIME_QUICKTIME));
    }
}

/*
 * @(#)AVIReader.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.avi;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.av.MovieReader;
import org.monte.media.av.Registry;
import org.monte.media.math.Rational;

import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.EnumSet;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.END_OF_MEDIA;
import static org.monte.media.av.BufferFlag.KEYFRAME;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_AVI;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.FrameSizeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;

/**
 * Provides high-level support for decoding and reading audio and video samples
 * from an AVI 1.0 file.
 *
 * @author Werner Randelshofer
 */
public class AVIReader extends AVIInputStream implements MovieReader {

    public final static Format AVI = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_AVI);
    private Rational movieDuration = null;

    public AVIReader(ImageInputStream in) throws IOException {
        super(in);
    }

    public AVIReader(File file) throws IOException {
        super(file);
    }

    @Override
    public Format getFileFormat() throws IOException {
        return AVI;
    }

    @Override
    public Format getFormat(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).format;
    }

    /**
     * Reads a chunk of media data from the specified track. <p> If the track is
     * a video track with palette change "..PC" chunks, then the body of the
     * palette change chunk can be found in the buffer.header.
     *
     * @param track  The track number.
     * @param buffer The buffer for the media data.
     * @throws IOException
     */
    @Override
    public void read(int track, Buffer buffer) throws IOException {
        ensureRealized();
        Track tr = tracks.get(track);
        if (tr.readIndex >= tr.samples.size()) {
            buffer.setFlagsTo(END_OF_MEDIA, DISCARD);
            buffer.length = 0;
            return;
        }

        buffer.sequenceNumber = tr.readIndex;
        Sample s = tr.samples.get((int) tr.readIndex);

        // FIXME - This should be done using AVIInputStream.readPalette()
        if (s.header != null) {
            byte[] b;
            if (buffer.data instanceof byte[]) {
                b = (byte[]) buffer.data;
                if (b.length < s.header.length) {
                    buffer.data = b = new byte[(((int) s.header.length + 1023) / 1024) * 1024];
                }
            } else {
                buffer.data = b = new byte[(((int) s.header.length + 1023) / 1024) * 1024];
            }
            in.seek(s.header.offset);
            in.readFully(b, 0, (int) s.header.length);
        } else {
            buffer.header = null;
        }

        // FIXME - This should be done using AVIInputStream.readSample()
        in.seek(s.offset);
        {
            byte[] b;
            if (buffer.data instanceof byte[]) {
                b = (byte[]) buffer.data;
                if (b.length < s.length) {
                    buffer.data = b = new byte[(((int) s.length + 1023) / 1024) * 1024];
                }
            } else {
                buffer.data = b = new byte[(((int) s.length + 1023) / 1024) * 1024];
            }
            in.readFully(b, 0, (int) s.length);
        }
        buffer.offset = 0;
        buffer.length = (int) s.length;


        switch (tr.mediaType) {
            case AUDIO: {
                Format af = tr.format;
                buffer.sampleCount = buffer.length / af.get(FrameSizeKey);
            }
            break;
            case VIDEO: {
                buffer.sampleCount = 1;
            }
            break;
            case MIDI:
            case TEXT:
            default:
                throw new UnsupportedOperationException("Unsupported media type " + tr.mediaType);
        }
        buffer.format = tr.format;
        buffer.track = track;
        buffer.sampleDuration = new Rational(tr.scale, tr.rate);
        buffer.timeStamp = new Rational((s.timeStamp + tr.startTime) * tr.scale, tr.rate);
        buffer.flags = s.isKeyframe ? EnumSet.of(KEYFRAME) : EnumSet.noneOf(BufferFlag.class);


        tr.readIndex++;

    }

    /**
     * Decodes the PC palette change chunk.
     */
    private void readPalette(byte[] pc, int offset, int length, byte[] r, byte[] g, byte[] b) throws IOException {
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
        int read = offset;
        int firstEntry = (int) pc[read++]; //bFirstEntry
        int numEntries = (int) pc[read++];//bNumEntries
        int flags = (pc[read++] & 0xff) | ((pc[read++] & 0xff) << 8); //wFlags
        for (int i = 0; i < numEntries; i++) {
            r[firstEntry + 1] = pc[read++];//peRed
            g[firstEntry + 1] = pc[read++];//peGreen
            b[firstEntry + 1] = pc[read++];//peBlue
            read++; //peFlags
        }

    }

    /**
     * Reads an image.
     *
     * @param track The track number
     * @param img   An image that can be reused if it fits the media format of the
     *              track. Pass null to create a new image on each read.
     * @return An image or null if the end of the media has been reached.
     * @throws IOException
     */
    public BufferedImage read(int track, BufferedImage img) throws IOException {
        Track tr = tracks.get(track);
        if (tr.inputBuffer == null) {
            tr.inputBuffer = new Buffer();
        }
        if (tr.codec == null) {
            createCodec(tr);
        }
        Buffer buf = new Buffer();
        buf.data = img;
        do {
            read(track, tr.inputBuffer);
            // FIXME - We assume a one-step codec here!
            tr.codec.process(tr.inputBuffer, buf);
        } while (buf.isFlag(DISCARD) && !buf.isFlag(END_OF_MEDIA));

        if (tr.inputBuffer.isFlag(END_OF_MEDIA)) {
            return null;
        }

        return (BufferedImage) buf.data;
    }

    private void createCodec(Track tr) throws IOException {
        Format fmt = tr.format;
        Codec codec = createCodec(fmt);
        String enc = fmt.get(EncodingKey);
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
                    throw new IOException("Track " + tr + " codec " + codec + " does not support output format " + outFormat + ". codec=" + codec);
                }
            }
        }

        tr.codec = codec;
    }

    private Codec createCodec(Format fmt) {
        return Registry.getInstance().getDecoder(fmt.prepend(MimeTypeKey, MIME_AVI));
    }

    @Override
    public Rational getReadTime(int track) throws IOException {
        Track tr = tracks.get(track);
        if (tr.samples.size() > tr.readIndex) {
            Sample s = tr.samples.get((int) tr.readIndex);
            return new Rational((s.timeStamp + tr.startTime) * tr.scale, tr.rate);
        }
        return new Rational(0, 1);
    }

    @Override
    public int nextTrack() throws IOException {
        ensureRealized();
        Rational ts = new Rational(Integer.MAX_VALUE, 1);
        int nextTrack = -1;
        for (int i = 0, n = tracks.size(); i < n; i++) {
            Track tr = tracks.get(i);

            if (tr.samples.isEmpty()) {
                continue;
            }

            Sample currentSample = tr.readIndex < tr.samples.size() ? tr.samples.get((int) tr.readIndex) : tr.samples.get(tr.samples.size() - 1);

            long readTimeStamp = currentSample.timeStamp;
            if (tr.readIndex >= tr.samples.size()) {
                readTimeStamp += currentSample.duration;
            }

            Rational trts = new Rational((readTimeStamp + tr.startTime) * tr.scale, tr.rate);
            if (trts.compareTo(ts) < 0 && tr.readIndex < tr.samples.size()) {
                ts = trts;
                nextTrack = i;
            }
        }
        return nextTrack;
    }

    @Override
    public Rational getDuration() {
        try {
            ensureRealized();
        } catch (IOException ex) {
            ex.printStackTrace();
            return new Rational(0, 1);
        }
        if (movieDuration == null) {
            Rational maxDuration = new Rational(0, 1);
            for (Track tr : tracks) {
                Rational trackDuration = new Rational((tr.length * tr.scale + tr.startTime), tr.rate);
                if (maxDuration.compareTo(trackDuration) < 0) {
                    maxDuration = trackDuration;
                }
            }
            movieDuration = maxDuration;
        }
        return movieDuration;
    }

    @Override
    public Rational getDuration(int track) {
        Track tr = tracks.get(track);
        Rational trackDuration = new Rational((tr.length * tr.scale + tr.startTime), tr.rate);
        return trackDuration;
    }

    @Override
    public long getTimeScale(int track) {
        return tracks.get(track).rate;
    }

    @Override
    public long timeToSample(int track, Rational time) {
        Track tr = tracks.get(track);
        // This only works, if all samples contain only one sample!
        // FIXME - We foolishly assume that only audio tracks have more than one
        // sample in a frame.
        // FIXME - We foolishly assume that all samples have a sampleDuration != 0.
        long index = time.getNumerator() * tr.rate / time.getDenominator() / tr.scale - tr.startTime;
        if (tr.mediaType == AVIMediaType.AUDIO) {
            int count = 0;
            // FIXME This is very inefficient, perform binary search with sample.timestamp
            // this will work for all media types!
            for (int i = 0, n = tr.samples.size(); i < n; i++) {
                long d = tr.samples.get(i).duration * tr.scale; // foolishly assume that sampleDuration = sample count
                if (count + d > index) {
                    index = i;
                    break;
                }
                count += d;

            }
        }

        return max(0, min(index, tr.samples.size()));
    }

    @Override
    public Rational sampleToTime(int track, long sampleIndex) throws IOException {
        ensureRealized();
        Track tr = tracks.get(track);
        Sample sample = tr.samples.get((int) max(0, min(tr.samples.size() - 1, sampleIndex)));
        long time = (tr.startTime + sample.timeStamp) * tr.scale;//
        if (sampleIndex >= tr.samples.size()) {
            time += sample.duration * tr.scale;
        }
        return new Rational(time, tr.rate);
    }

    @Override
    public void setMovieReadTime(Rational newValue) throws IOException {
        ensureRealized();
        for (int t = 0, n = tracks.size(); t < n; t++) {
            Track tr = tracks.get(t);
            int sample = (int) min(timeToSample(t, newValue), tr.samples.size() - 1);
            if (tr.readIndex > sample) {
                tr.readIndex = 0;
            }
            for (; sample >= 0 && sample > tr.readIndex && !tr.samples.get(sample).isKeyframe; sample--) ;
            tr.readIndex = sample;
        }
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
}

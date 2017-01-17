/* @(#)QuickTimeReader.java
 * Copyright © 2012 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.quicktime;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.stream.ImageInputStream;
import ru.sbtqa.monte.media.Buffer;
import static ru.sbtqa.monte.media.BufferFlag.*;
import ru.sbtqa.monte.media.Codec;
import ru.sbtqa.monte.media.Format;
import ru.sbtqa.monte.media.FormatKeys.MediaType;
import ru.sbtqa.monte.media.MovieReader;
import ru.sbtqa.monte.media.Registry;
import static ru.sbtqa.monte.media.VideoFormatKeys.*;
import ru.sbtqa.monte.media.math.Rational;

/**
 * {@code QuickTimeReader}.
 *
 * @author Werner Randelshofer
 * @version $Id: QuickTimeReader.java 364 2016-11-09 19:54:25Z werner $
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
    public long timeToSample(int track, Rational seconds) throws IOException {
        ensureRealized();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rational sampleToTime(int track, long sample) throws IOException {
        ensureRealized();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Format getFileFormat() throws IOException {
        return QUICKTIME;
    }

    @Override
    public Format getFormat(int track) throws IOException {
        ensureRealized();
        return meta.tracks.get(track).format;
    }

    @Override
    public long getChunkCount(int track) throws IOException {
        ensureRealized();
        return meta.tracks.get(track).mediaList.get(0).sampleCount;
    }

    @Override
    protected void ensureRealized() throws IOException {
        super.ensureRealized();
        inputBuffers = new Buffer[meta.getTrackCount()];
        codecs = new Codec[meta.getTrackCount()];
        for (int i = 0; i < inputBuffers.length; i++) {
            inputBuffers[i] = new Buffer();
        }
    }

    /**
     * Reads an image.
     *
     * @param track The track number
     * @param img An image that can be reused if it fits the media format of the
     * track. Pass null to create a new image on each read.
     * @return An image or null if the end of the media has been reached.
     * @throws IOException TODO
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int nextTrack() throws IOException {
        ensureRealized();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setMovieReadTime(Rational newValue) throws IOException {
        ensureRealized();
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rational getReadTime(int track) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rational getDuration() throws IOException {
        return new Rational(getMovieDuration(), getMovieTimeScale());
    }

    @Override
    public Rational getDuration(int track) throws IOException {
        ensureRealized();
        QuickTimeMeta.Track tr = meta.tracks.get(track);
        // FIXME - This method must take the edit list of the track into account
        QuickTimeMeta.Media m=tr.mediaList.get(0);
        Rational trackDuration = new Rational(m.mediaDuration, m.mediaTimeScale);
        return trackDuration;
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

    private void createCodec(int track) {
        QuickTimeMeta.Track tr = meta.tracks.get(track);
        Format fmt = tr.format;
        Codec codec = createCodec(fmt);
        String enc = fmt.get(EncodingKey);
        if (codec == null) {
            throw new UnsupportedOperationException("Track " + tr + " no codec found for format " + fmt);
        } else {
            if (fmt.get(MediaTypeKey) == MediaType.VIDEO) {
                if (null == codec.setInputFormat(fmt)) {
                    throw new UnsupportedOperationException("Track " + tr + " codec " + codec + " does not support input format " + fmt + ". codec=" + codec);
                }
                Format outFormat = fmt.prepend(MediaTypeKey, MediaType.VIDEO,//
                        MimeTypeKey, MIME_JAVA,
                        EncodingKey, ENCODING_BUFFERED_IMAGE, DataClassKey, BufferedImage.class);
                if (null == codec.setOutputFormat(outFormat)) {
                    throw new UnsupportedOperationException("Track " + tr + " codec does not support output format " + outFormat + ". codec=" + codec);
                }
            }
        }

        codecs[track] = codec;
    }

    private Codec createCodec(Format fmt) {
        Codec[] codecs = Registry.getInstance().getDecoders(fmt.prepend(MimeTypeKey, MIME_QUICKTIME));
        return codecs.length == 0 ? null : codecs[0];
    }
}

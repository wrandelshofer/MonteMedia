/* @(#)ImageSequenceWriter.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.imgseq;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.String.format;
import java.util.ArrayList;
import ru.sbtqa.monte.media.Buffer;
import static ru.sbtqa.monte.media.BufferFlag.*;
import ru.sbtqa.monte.media.Codec;
import ru.sbtqa.monte.media.Format;
import static ru.sbtqa.monte.media.FormatKeys.MediaType.FILE;
import static ru.sbtqa.monte.media.FormatKeys.MediaType.VIDEO;
import ru.sbtqa.monte.media.MovieWriter;
import static ru.sbtqa.monte.media.VideoFormatKeys.*;
import static ru.sbtqa.monte.media.io.IOStreams.copy;
import ru.sbtqa.monte.media.jpeg.JPEGCodec;
import ru.sbtqa.monte.media.math.Rational;
import ru.sbtqa.monte.media.png.PNGCodec;

/**
 * {@code ImageSequenceWriter}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-04-20 Created.
 */
public class ImageSequenceWriter implements MovieWriter {

    private Format fileFormat = new Format(MediaTypeKey, FILE);

    @Override
    public int addTrack(Format format) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Format getFileFormat() throws IOException {
        return fileFormat;
    }

    @Override
    public int getTrackCount() {
        return 1;
    }

    @Override
    public Format getFormat(int track) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class VideoTrack {

        Format videoFormat;
        File dir;
        String nameFormat;
        int count;
        Codec codec;
        Buffer inputBuffer;
        Buffer outputBuffer;
        int width;
        int height;

        public VideoTrack(File dir, String filenameFormatter, Format fmt, Codec codec, int width, int height) {
            this.dir = dir;
            this.nameFormat = filenameFormatter;
            this.videoFormat = fmt;
            this.codec = codec;
            this.width = width;
            this.height = height;
        }
    }
    private ArrayList<VideoTrack> tracks = new ArrayList<>();

    /**
     * Adds a video track.
     *
     * @param dir The output directory.
     * @param filenameFormatter a format string for a filename with a number,
     * for example "frame_%d0000$.png";
     *
     * @param width the image width
     * @param height the image height
     *
     * @return Returns the track index.
     *
     * @throws IllegalArgumentException if the width or the height is smaller
     * than 1.
     */
    public int addVideoTrack(File dir, String filenameFormatter, int width, int height) {
        VideoTrack t;
        Format fmt = filenameFormatter.toLowerCase().endsWith(".png")//
              ? new Format(MediaTypeKey, VIDEO, EncodingKey, ENCODING_QUICKTIME_PNG, WidthKey, width, HeightKey, height, DepthKey, 24) //
              : new Format(MediaTypeKey, VIDEO, EncodingKey, ENCODING_QUICKTIME_JPEG, WidthKey, width, HeightKey, height, DepthKey, 24) // //
              ;
        tracks.add(t = new VideoTrack(dir, filenameFormatter,
              fmt,
              null, width, height));
        createCodec(t);
        return tracks.size() - 1;
    }

    private void createCodec(VideoTrack vt) {
        Format fmt = vt.videoFormat;
        String enc = fmt.get(EncodingKey);
        if (enc.equals(ENCODING_AVI_MJPG)//
              || enc.equals(ENCODING_QUICKTIME_JPEG)//
              ) {
            vt.codec = new JPEGCodec();
        } else if (enc.equals(ENCODING_AVI_PNG)//
              || enc.equals(ENCODING_QUICKTIME_PNG)//
              ) {
            vt.codec = new PNGCodec();
        }

        vt.codec.setInputFormat(fmt.prepend(MediaTypeKey, VIDEO, MimeTypeKey, MIME_JAVA, EncodingKey, ENCODING_BUFFERED_IMAGE, DataClassKey, BufferedImage.class));
        vt.codec.setOutputFormat(fmt.prepend(MediaTypeKey, VIDEO, EncodingKey, enc, DataClassKey, byte[].class));
//    vt.codec.setQuality(vt.videoQuality);
    }

    public void write(int track, BufferedImage image, long duration) throws IOException {
        VideoTrack t = tracks.get(track);
        if (t.inputBuffer == null) {
            t.inputBuffer = new Buffer();
        }
        if (t.outputBuffer == null) {
            t.outputBuffer = new Buffer();
        }
        t.inputBuffer.setFlagsTo(KEYFRAME);
        t.inputBuffer.data = image;

        t.codec.process(t.inputBuffer, t.outputBuffer);
        write(track, t.outputBuffer);
    }

    @Override
    public void write(int track, Buffer buf) throws IOException {
        VideoTrack t = tracks.get(track);

        // FIXME - Meybe we should not have built-in support for some data types?
        if (buf.data instanceof BufferedImage) {
            if (t.outputBuffer == null) {
                t.outputBuffer = new Buffer();
            }
            if (buf.isFlag(DISCARD)) {
                return;
            }
            t.codec.process(buf, t.outputBuffer);
            buf = t.outputBuffer;
        }

        File file = new File(t.dir, format(t.nameFormat, t.count + 1));

        if (buf.data instanceof byte[]) {
            try (FileOutputStream out = new FileOutputStream(file)) {
                out.write((byte[]) buf.data, buf.offset, buf.length);
            }
        } else if (buf.data instanceof File) {
            copy((File) buf.data, file);
        } else {
            throw new IllegalArgumentException("Can't process buffer data:" + buf.data);
        }

        t.count++;
    }

    public void writeSample(int track, byte[] data, int off, int len, long duration, boolean isSync) throws IOException {
        VideoTrack t = tracks.get(track);

        File file = new File(t.dir, format(t.nameFormat, t.count + 1));

        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write(data, off, len);
        }

        t.count++;
    }

    public void writeSamples(int track, int sampleCount, byte[] data, int off, int len, long sampleDuration, boolean isSync) throws IOException {
        for (int i = 0; i < sampleCount; i++) {
            writeSample(track, data, off, len / sampleCount, sampleDuration, isSync);
            off += len / sampleCount;
        }

    }

    @Override
    public void close() throws IOException {
        //nothing to do
    }

    public boolean isVFRSupported() {
        return false;
    }

    @Override
    public boolean isDataLimitReached() {
        return false;
    }

    /**
     * Returns the sampleDuration of the track in seconds.
     *
     * @return TODO
     */
    @Override
    public Rational getDuration(int track) {
        VideoTrack tr = tracks.get(track);
        return new Rational(tr.count, 30);
    }

    @Override
    public boolean isEmpty(int track) {
        return tracks.get(track).count == 0;
    }

}

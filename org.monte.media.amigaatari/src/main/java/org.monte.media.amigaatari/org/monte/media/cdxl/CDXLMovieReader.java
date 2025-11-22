/*
 * @(#)CDXLMovieReader.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.cdxl;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKey;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.MovieReader;
import org.monte.media.math.Rational;
import org.monte.media.util.ArrayUtil;
import org.monte.media.util.MathUtil;

import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Collections;
import java.util.Comparator;

import static org.monte.media.av.FormatKeys.DataClassKey;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ByteOrderKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ChannelsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_PCM_SIGNED;
import static org.monte.media.av.codec.audio.AudioFormatKeys.FrameSizeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleRateKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleSizeInBitsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SignedKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BITMAP_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.FixedFrameRateKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.PixelAspectRatioKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

public class CDXLMovieReader extends CDXLInputStream implements MovieReader {
    public final static String MIME_CDXL = "video/x-cdxl";
    public final static Format CDXL = new Format(MediaTypeKey, FormatKeys.MediaType.FILE, MimeTypeKey, MIME_CDXL);
    /// Whether the frame rate must be fixed. False means variable frame rate.
    public final static FormatKey<Boolean> HamKey = new FormatKey<>("hamKey", Boolean.class);
    /// Whether the scanlines are interleaved.
    public final static FormatKey<Boolean> InterleavedKey = new FormatKey<>("interleavedKey", Boolean.class);
    /// Number of bits per color.
    public final static FormatKey<Integer> BitsPerColor = new FormatKey<>("NumberOfBitsPerColor", Integer.class);

    private abstract static class Track {
        int frameIndex;
        Format trackFormat;

        public abstract void read(CDXLMovieReader r, PanFrame frame, Buffer buffer) throws IOException;

        public abstract BufferedImage read(CDXLMovieReader r, PanFrame frame, BufferedImage img) throws IOException;


    }

    private static class VideoTrack extends Track {
        CDXLImageDecoder codec;

        public void read(CDXLMovieReader r, PanFrame frame, Buffer buffer) throws IOException {
            buffer.sampleCount = 1;
            buffer.format = trackFormat;
            buffer.sampleDuration = frame.duration();
            buffer.timeStamp = frame.timestamp();
            ImageInputStream in = r.in;

            // read colormap
            in.seek(frame.streamPosition() + HEADER_SIZE);
            buffer.headerOffset = 0;
            buffer.headerLength = frame.colormapSize();
            buffer.header = ArrayUtil.reuseByteArray(buffer.header, buffer.headerLength);
            in.readFully((byte[]) buffer.header, 0, frame.colormapSize());

            // read bitmap
            buffer.offset = 0;
            buffer.length = frame.bitmapSize();
            buffer.data = ArrayUtil.reuseByteArray(buffer.data, buffer.length);
            try {
                in.readFully((byte[]) buffer.data, 0, buffer.length);
            } catch (EOFException e) {
                System.err.println("Frame " + frame.streamPosition() + " does not fully fit into the stream.");
            }

        }

        @Override
        public BufferedImage read(CDXLMovieReader r, PanFrame frame, BufferedImage img) throws IOException {
            ensureCodec();
            throw new IOException("reading an image from the video track not yet implemented");
        }

        private void ensureCodec() {
            if (codec == null) {
                codec = new CDXLImageDecoder();
                codec.setInputFormat(trackFormat);
                codec.setOutputFormat(new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                        EncodingKey, ENCODING_BUFFERED_IMAGE));
            }
        }

    }

    private static class AudioTrack extends Track {
        public void read(CDXLMovieReader r, PanFrame frame, Buffer buffer) throws IOException {
            PanFrame header = r.header;
            buffer.headerLength = 0;
            int rawSoundSize = frame.rawSoundSize();
            buffer.sampleCount = rawSoundSize;
            buffer.format = trackFormat;
            buffer.timeStamp = frame.timestamp();
            buffer.sampleDuration = Rational.valueOf(1, header.frequency());
            ImageInputStream in = r.in;
            in.seek(frame.streamPosition() + HEADER_SIZE + frame.colormapSize() + frame.bitmapSize());


            // read samples
            buffer.offset = 0;
            if (frame.audioType() == AudioType.STEREO) {
                // create a buffer twice the size
                // load the data at the end of the buffer
                // interleave the left and the right channel
                int length = rawSoundSize * 2;
                byte[] data = ArrayUtil.reuseByteArray(buffer.data, length * 2);
                in.readFully(data, length, length);
                int j = 0;
                for (int i = 0; i < rawSoundSize; i++) {
                    data[j] = data[i + length];
                    data[j + 1] = data[i + length + rawSoundSize];
                    j += 2;
                }
                buffer.length = length;
                buffer.data = data;
            } else {
                buffer.length = rawSoundSize;
                buffer.data = ArrayUtil.reuseByteArray(buffer.data, buffer.length);
                in.readFully((byte[]) buffer.data, 0, buffer.length);
            }
        }

        @Override
        public BufferedImage read(CDXLMovieReader r, PanFrame frame, BufferedImage img) throws IOException {
            throw new IOException("can not read an image from an audio track");
        }

    }

    private Track[] tracks = {};
    private int trackIndex = 0;
    private Format fileFormat;

    public CDXLMovieReader(File file) throws IOException {
        super(file);
    }

    public CDXLMovieReader(ImageInputStream in) {
        super(in);
    }

    @Override
    public int getSampleCount(int track) throws IOException {
        ensureRealized();
        return frames.size();
    }

    @Override
    public int getTrackCount() throws IOException {
        ensureRealized();
        return tracks.length;
    }

    @Override
    public int findTrack(int fromTrack, Format format) throws IOException {
        ensureRealized();
        for (int i = fromTrack, n = getTrackCount(); i < n; i++) {
            if (getFormat(i).matches(format)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Rational getMovieDuration() throws IOException {
        ensureRealized();
        PanFrame last = frames.getLast();
        return last.duration().add(last.timestamp());
    }

    @Override
    public Rational getTrackDuration(int track) throws IOException {
        return getMovieDuration();
    }

    @Override
    public long findSampleAtTime(int track, Rational seconds) throws IOException {
        ensureRealized();
        int result = Collections.binarySearch(frames,
                null,
                new Comparator<PanFrame>() {
                    @Override
                    public int compare(PanFrame o1, PanFrame o2) {
                        return o1.timestamp().compareTo(seconds);
                    }
                });
        if (result < 0) result = ~result - 1;
        result = MathUtil.clamp(result, 0, frames.size() - 1);
        return result;
    }

    @Override
    public Rational getSampleTime(int track, long sample) throws IOException {
        ensureRealized();
        return sample < frames.size() ? frames.get((int) sample).timestamp() : getMovieDuration();
    }

    @Override
    public Rational getSampleDuration(int track, long sample) throws IOException {
        ensureRealized();
        return frames.get((int) sample).duration();
    }

    @Override
    public Format getFileFormat() throws IOException {
        ensureRealized();
        return CDXL.append(WidthKey, header.xSize(), HeightKey, header.ySize());
    }

    @Override
    public Format getFormat(int track) throws IOException {
        ensureRealized();
        return tracks[track].trackFormat;
    }

    @Override
    public long getChunkCount(int track) throws IOException {
        ensureRealized();
        return frames.size();
    }

    @Override
    public void read(int track, Buffer buffer) throws IOException {
        ensureRealized();
        Track tr = tracks[track];
        int index = tr.frameIndex;
        if (index >= frames.size()) {
            buffer.setFlagsTo(BufferFlag.DISCARD, BufferFlag.END_OF_MEDIA);
            return;
        }
        PanFrame frame = frames.get(index);
        buffer.clearFlags();
        buffer.sequenceNumber = index;
        tr.frameIndex++;
        tr.read(this, frame, buffer);
    }

    @Override
    public BufferedImage read(int track, BufferedImage img) throws IOException {
        ensureRealized();
        Track tr = tracks[track];
        int index = tr.frameIndex;
        PanFrame frame = frames.get(index);
        tr.frameIndex++;
        return tr.read(this, frame, img);
    }

    @Override
    public int nextTrack() throws IOException {
        ensureRealized();
        if (trackIndex < 2) trackIndex++;
        return trackIndex < 2 ? trackIndex : -1;
    }

    @Override
    public void setMovieReadTime(Rational newValue) throws IOException {
        int index = (int) findSampleAtTime(0, newValue);
        for (Track tr : tracks) {
            tr.frameIndex = index;
        }
    }

    @Override
    public void setTrackReadTime(int track, Rational newValue) throws IOException {
        int index = (int) findSampleAtTime(0, newValue);
        Track tr = tracks[track];
        tr.frameIndex = index;

    }

    @Override
    public Rational getReadTime(int track) throws IOException {
        return frames.get(tracks[track].frameIndex).timestamp();
    }

    @Override
    protected void realize() throws IOException {
        super.realize();

        if (header.hasSound()) {
            tracks = new Track[]{new VideoTrack(), new AudioTrack()};
        } else {
            tracks = new Track[]{new VideoTrack()};
        }

        tracks[0].trackFormat = new Format(
                //MimeTypeKey, MIME_CDXL,
                MediaTypeKey, FormatKeys.MediaType.VIDEO,
                EncodingKey, ENCODING_BITMAP_IMAGE,
                DataClassKey, byte[].class,
                WidthKey, header.xSize(),
                HeightKey, header.ySize(),
                DepthKey, header.pixelSize(),
                PixelAspectRatioKey, new Rational(1, 1),// FIXME should be PAL or NTSC
                FixedFrameRateKey, false,
                BitsPerColor, header.colorSize(),
                InterleavedKey, header.pixelValueOrientation() == PixelValueOrientation.LINES,
                HamKey, header.videoType() == VideoType.HAM);
        if (header.hasSound()) {
            tracks[1].trackFormat = new Format(
                    //MimeTypeKey, MIME_CDXL,
                    MediaTypeKey, FormatKeys.MediaType.AUDIO,
                    EncodingKey, ENCODING_PCM_SIGNED,
                    SignedKey, true,
                    SampleRateKey, Rational.valueOf(header.frequency()),
                    SampleSizeInBitsKey, 8,
                    ChannelsKey, switch (header.audioType()) {
                case MONO -> 1;
                case STEREO -> 2;
            },
                    FrameSizeKey, switch (header.audioType()) {
                case MONO -> 1;
                case STEREO -> 2;
            },
                    FrameRateKey, Rational.valueOf(header.frequency()),
                    ByteOrderKey, ByteOrder.BIG_ENDIAN,
                    FixedFrameRateKey, true);
        }

    }
}

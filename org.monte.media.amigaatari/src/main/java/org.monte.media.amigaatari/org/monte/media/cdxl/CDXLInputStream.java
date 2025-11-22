/*
 * @(#)CDXLInputStream.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.cdxl;

import org.monte.media.math.Rational;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class CDXLInputStream extends AbstractCDXLStream implements Closeable {
    protected final static int HEADER_SIZE = 32;
    protected int defaultAudioFrequency = 11025;//8000;//11025;//22050;
    protected Rational defaultFrameDuration = new Rational(1, 24);
    /// The image input stream.
    protected final ImageInputStream in;
    protected PanFrame header;
    protected List<PanFrame> frames;
    /// This variable is set to true when all meta-data has been read from the
    /// file.
    private boolean isRealized = false;

    /// Creates a new instance.
    ///
    /// @param file the input file
    public CDXLInputStream(File file) throws IOException {
        this.in = new FileImageInputStream(file);
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
    }

    /// Creates a new instance.
    ///
    /// @param in the input stream.
    public CDXLInputStream(ImageInputStream in) {
        this.in = in;
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    /// Ensures that all meta-data has been read from the file.
    protected void ensureRealized() throws IOException {
        if (!isRealized) {
            isRealized = true;
            realize();
        }
    }

    /// Discards the meta-data that has been read from the file.
    protected void unrealize() {
        isRealized = true;
    }

    /// Reads all metadata of the file.
    protected void realize() throws IOException {
        in.seek(0);
        long length = in.length();
        Rational time = Rational.ZERO;
        frames = new ArrayList<>();
        try {
            while ((in.getStreamPosition() < length)) {
                PanFrame frame = readFrame(time);
                frames.add(frame);
                time = time.add(frame.duration);
            }
        } catch (EOFException e) {
            // The file is incomplete. We just go with the frames that we have.
        }
        if (frames.isEmpty()) {
            throw new IOException("CDXL file does not contain any frames");
        }
        header = frames.getFirst();
    }

    /// Reads a PanFrame at the current stream position and returns a PanFrame object.
    ///
    /// @return PanFrame object
    /// @throws IOException if the frame does not match with the header
    private PanFrame readFrame(Rational time) throws IOException {
        long streamPosition = in.getStreamPosition();

        int type = in.read();
        int info = in.read();
        VideoType videoType = VideoType.decode(info & 0b1111);//4 bits
        PixelValueOrientation pixelValueOrientation = PixelValueOrientation.decode(info >>> 5);// 3 bits
        AudioType audioType = AudioType.decode((info >>> 4) & 1);//1 bit

        int size = in.readInt();
        int back = in.readInt();
        int frameSequenceNumber = in.readInt();
        int xSize = in.readShort();
        int ySize = in.readShort();
        int reserved = in.readByte();
        int pixelSize = in.readByte() & 0xff;
        int colormapSize = in.readShort() & 0xffff;
        int rawSoundSize = in.readShort() & 0xffff;
        int bitmapSize = size - HEADER_SIZE - colormapSize - (audioType == AudioType.STEREO ? rawSoundSize * 2 : rawSoundSize);
        int frequency = in.readShort() & 0xffff;
        if (frequency == 0) {
            frequency = defaultAudioFrequency;
        }

        int framesPerSecond = in.readByte() & 0xff;
        int info2 = in.readByte();
        AGABlasterColorMode colorMode = AGABlasterColorMode.decode((info2 >>> 4) & 0b1);
        int reserved2 = in.readInt();
        int skipped = in.skipBytes(size - HEADER_SIZE);
        if (in.getStreamPosition() > in.length()) {
            // the current frame does not fit into the file
            throw new EOFException("The frame at streamPosition=" + streamPosition + " does not fully fit into the file");
        }
        Rational duration;
        if (rawSoundSize == 0) {
            duration = framesPerSecond == 0
                    ? defaultFrameDuration
                    : Rational.valueOf(1, framesPerSecond);
        } else {
            duration = Rational.valueOf(rawSoundSize, frequency);
        }

        return new PanFrame(
                PanStructureType.decode(type),
                pixelValueOrientation,
                audioType,
                videoType,
                xSize,
                ySize,
                pixelSize,
                colorMode == AGABlasterColorMode._24_BIT_COLORS ? 24 : 12,
                frequency,
                rawSoundSize > 0,
                streamPosition,
                time, duration,
                colormapSize, bitmapSize, rawSoundSize
        );
    }

    public int getDefaultAudioFrequency() {
        return defaultAudioFrequency;
    }

    /// Sets the default audio frequency.
    ///
    /// The default audio frequency is used to determine the
    /// audio frequency of a video frame that has no specified
    /// audio frequency.
    ///
    /// Setting this value unrealizes the movie.
    ///
    /// @param defaultAudioFrequency new value
    public void setDefaultAudioFrequency(int defaultAudioFrequency) {
        this.defaultAudioFrequency = defaultAudioFrequency;
        unrealize();
    }

    public Rational getDefaultFrameDuration() {
        return defaultFrameDuration;
    }

    /// Sets the default frame duration.
    ///
    /// The default frame duration is used to determine the duration
    /// of a video frame that has neither audio data, nor a
    /// specified duration.
    ///
    /// Setting this value unrealizes the movie.
    ///
    /// @param defaultFrameDuration new value
    public void setDefaultFrameDuration(Rational defaultFrameDuration) {
        this.defaultFrameDuration = defaultFrameDuration;
        unrealize();
    }

    public record PanFrame(
            PanStructureType type,
            PixelValueOrientation pixelValueOrientation,
            AudioType audioType,
            VideoType videoType,
            int xSize,
            int ySize,
            int pixelSize,
            int colorSize,
            int frequency,
            boolean hasSound,
            long streamPosition,
            Rational timestamp,
            Rational duration,
            int colormapSize,
            int bitmapSize,
            int rawSoundSize) {

    }
}

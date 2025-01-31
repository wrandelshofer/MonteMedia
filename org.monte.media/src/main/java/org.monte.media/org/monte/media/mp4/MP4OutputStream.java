/*
 * @(#)MP4OutputStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.mp4;

import org.monte.media.av.Format;
import org.monte.media.av.codec.video.VideoFormatKeys;
import org.monte.media.io.ByteArrayImageOutputStream;
import org.monte.media.io.IOStreams;
import org.monte.media.io.ImageOutputStreamAdapter;
import org.monte.media.math.Rational;
import org.monte.media.qtff.AbstractQTFFMovieStream;
import org.monte.media.qtff.AvcDecoderConfigurationRecord;
import org.monte.media.qtff.QTFFImageOutputStream;
import org.monte.media.qtff.atom.CompositeAtom;
import org.monte.media.qtff.atom.DataAtom;
import org.monte.media.qtff.atom.WideDataAtom;
import org.monte.media.util.ByteArray;
import org.monte.media.util.MathUtil;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.Instant;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.zip.DeflaterOutputStream;

import static java.lang.Math.max;
import static org.monte.media.av.FormatKeys.DataClassKey;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaType;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ByteOrderKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ChannelsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.FrameSizeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleRateKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SampleSizeInBitsKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SignedKey;

/**
 * This class provides low-level support for writing already encoded audio and
 * video samples into a MP4 file.
 *
 * @author Werner Randelshofer
 */
public class MP4OutputStream extends AbstractQTFFMovieStream {

    /**
     * Creates a new instance.
     *
     * @param file the output file
     */
    public MP4OutputStream(File file) throws IOException {
        if (file.exists()) {
            if (!file.delete()) throw new IOException("can not delete file");
        }
        this.out = new FileImageOutputStream(file);
        this.streamOffset = 0;
        init();
    }

    /**
     * Creates a new instance.
     *
     * @param out the output stream.
     */
    public MP4OutputStream(ImageOutputStream out) throws IOException {
        this.out = out;
        this.streamOffset = out.getStreamPosition();
        init();
    }

    private void init() {
        creationTime = modificationTime = Instant.ofEpochMilli(0);
    }

    /**
     * Sets the time scale for this movie, that is, the number of time units
     * that pass per second in its time coordinate system. <p> The default value
     * is 600.
     *
     * @param timeScale
     */
    public void setMovieTimeScale(long timeScale) {
        if (timeScale < 1 || timeScale > (2L << 32)) {
            throw new IllegalArgumentException("timeScale must be between 1 and 2^32:" + timeScale);
        }
        this.movieTimeScale = timeScale;
    }

    /**
     * Returns the time scale of the movie.
     *
     * @return time scale
     * @see #setMovieTimeScale(long)
     */
    public long getMovieTimeScale() {
        return movieTimeScale;
    }

    /**
     * Returns the time scale of the media in a track.
     *
     * @param track Track index.
     * @return time scale
     * @see #setMovieTimeScale(long)
     */
    public long getMediaTimeScale(int track) {
        return tracks.get(track).mediaTimeScale;
    }

    /**
     * Returns the media duration of a track in the media's time scale.
     *
     * @param track Track index.
     * @return media duration
     */
    public long getMediaDuration(int track) {
        return tracks.get(track).mediaDuration;
    }

    /**
     * Returns the track duration in the movie's time scale without taking the
     * edit list into account. <p> The returned value is the media duration of
     * the track in the movies's time scale.
     *
     * @param track Track index.
     * @return unedited track duration
     */
    public long getUneditedTrackDuration(int track) {
        Track t = tracks.get(track);
        return t.mediaDuration * t.mediaTimeScale / movieTimeScale;
    }

    /**
     * Returns the track duration in the movie's time scale. <p> If the track
     * has an edit-list, the track duration is the sum of all edit durations.
     * <p> If the track does not have an edit-list, then this method returns the
     * media duration of the track in the movie's time scale.
     *
     * @param track Track index.
     * @return track duration
     */
    public long getTrackDuration(int track) {
        return tracks.get(track).getTrackDuration(movieTimeScale);
    }

    /**
     * Returns the total duration of the movie in the movie's time scale.
     *
     * @return media duration
     */
    public long getMovieDuration() {
        long duration = 0;
        for (Track t : tracks) {
            duration = Math.max(duration, t.getTrackDuration(movieTimeScale));
        }
        return duration;
    }

    /**
     * Sets the color table for videos with indexed color models.
     *
     * @param track The track number.
     * @param icm   IndexColorModel. Specify null to use the standard Macintosh
     *              color table.
     */
    public void setVideoColorTable(int track, ColorModel icm) {
        if (icm instanceof IndexColorModel) {
            VideoTrack t = (VideoTrack) tracks.get(track);
            t.videoColorTable = (IndexColorModel) icm;
        }
    }

    /**
     * Gets the preferred color table for displaying the movie on devices that
     * support only 256 colors.
     *
     * @param track The track number.
     * @return The color table or null, if the video uses the standard Macintosh
     * color table.
     */
    public IndexColorModel getVideoColorTable(int track) {
        VideoTrack t = (VideoTrack) tracks.get(track);
        return t.videoColorTable;
    }

    /**
     * Sets the edit list for the specified track. <p> In the absence of an edit
     * list, the presentation of the track starts immediately. An empty edit is
     * used to offset the start time of a track. <p>
     *
     * @throws IllegalArgumentException If the edit list ends with an empty
     *                                  edit.
     */
    public void setEditList(int track, Edit[] editList) {
        if (editList != null && editList.length > 0 && editList[editList.length - 1].mediaTime == -1) {
            throw new IllegalArgumentException("Edit list must not end with empty edit.");
        }
        tracks.get(track).editList = editList;
    }

    /**
     * Adds a video track.
     *
     * @param compressionType The QuickTime "image compression format"
     *                        4-Character code. A list of supported 4-Character codes is given in qtff,
     *                        table 3-1, page 96.
     * @param compressorName  The QuickTime compressor name. Can be up to 32
     *                        characters long.
     * @param timeScale       The media time scale between 1 and 2^32.
     * @param width           The width of a video frame.
     * @param height          The height of a video frame.
     * @param depth           The number of bits per pixel.
     * @param syncInterval    Interval for sync-samples. 0=automatic. 1=all frames
     *                        are keyframes. Values larger than 1 specify that for every n-th frame is
     *                        a keyframe. Apple's QuickTime will not work properly if there is not at
     *                        least one keyframe every second.
     * @param format
     * @return Returns the track index.
     * @throws IllegalArgumentException if {@code width} or {@code height} is
     *                                  smaller than 1, if the length of {@code compressionType} is not equal to
     *                                  4, if the length of the {@code compressorName} is not between 1 and 32,
     *                                  if the tiimeScale is not between 1 and 2^32.
     */
    public int addVideoTrack(String compressionType, String compressorName, long timeScale, int width, int height, int depth, int syncInterval, Format format) throws IOException {
        ensureStarted();
        if (compressionType == null || compressionType.length() != 4) {
            throw new IllegalArgumentException("compressionType must be 4 characters long:" + compressionType);
        }
        if (compressorName == null || compressorName.isEmpty() || compressorName.length() > 32) {
            throw new IllegalArgumentException("compressorName must be between 1 and 32 characters long:" + (compressorName == null ? "null" : "\"" + compressorName + "\""));
        }
        if (timeScale < 1 || timeScale > (2L << 32)) {
            throw new IllegalArgumentException("timeScale must be between 1 and 2^32:" + timeScale);
        }
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Width and height must be greater than 0, width:" + width + " height:" + height);
        }

        VideoTrack t = new VideoTrack();
        t.mediaCompressionType = compressionType;
        t.mediaCompressorName = compressorName;
        t.mediaTimeScale = timeScale;
        t.width = width;
        t.height = height;
        t.videoDepth = depth;
        t.syncInterval = syncInterval;
        t.format = format.prepend(VideoFormatKeys.DataClassKey, byte[].class);
        tracks.add(t);
        return tracks.size() - 1;
    }

    /**
     * Adds an audio track.
     *
     * @param compressionType     The QuickTime 4-character code. A list of
     *                            supported 4-Character codes is given in qtff, table 3-7, page 113.
     * @param timeScale           The media time scale between 1 and 2^32.
     * @param sampleRate          The sample rate. The integer portion must match the
     *                            {@code timeScale}.
     * @param numberOfChannels    The number of channels: 1 for mono, 2 for stereo.
     * @param sampleSizeInBits    The number of bits in a sample: 8 or 16.
     * @param isCompressed        Whether the sound is compressed.
     * @param frameDuration       The frame duration, expressed in the media’s
     *                            timescale, where the timescale is equal to the sample rate. For
     *                            uncompressed formats, this field is always 1.
     * @param soundBytesPerPacket For uncompressed audio, the number of bytes in a sample
     *                            for a single channel (sampleSize divided by 8). For compressed audio, the
     *                            number of bytes in a frame.
     * @return Returns the track index.
     * @throws IllegalArgumentException if the audioFormat is not 4 characters
     *                                  long, if the time scale is not between 1 and 2^32, if the integer portion
     *                                  of the sampleRate is not equal to the timeScale, if numberOfChannels is
     *                                  not 1 or 2.
     */
    public int addAudioTrack(String compressionType, //
                             long timeScale, double sampleRate, //
                             int numberOfChannels, int sampleSizeInBits, //
                             boolean isCompressed, //
                             int frameDuration, int soundBytesPerPacket, boolean signed, ByteOrder byteOrder) throws IOException {
        ensureStarted();
        if (compressionType == null || compressionType.length() != 4) {
            throw new IllegalArgumentException("audioFormat must be 4 characters long:" + compressionType);
        }
        if (timeScale < 1 || timeScale > (2L << 32)) {
            throw new IllegalArgumentException("timeScale must be between 1 and 2^32:" + timeScale);
        }
        if (timeScale != (int) Math.floor(sampleRate)) {
            throw new IllegalArgumentException("timeScale: " + timeScale + " must match integer portion of sampleRate: " + sampleRate);
        }
        if (numberOfChannels != 1 && numberOfChannels != 2) {
            throw new IllegalArgumentException("numberOfChannels must be 1 or 2: " + numberOfChannels);
        }
        if (sampleSizeInBits != 8 && sampleSizeInBits != 16) {
            throw new IllegalArgumentException("sampleSize must be 8 or 16: " + numberOfChannels);
        }

        AudioTrack t = new AudioTrack();
        t.mediaCompressionType = compressionType;
        t.mediaTimeScale = timeScale;
        t.soundSampleRate = sampleRate;
        t.soundCompressionId = isCompressed ? -2 : -1;
        t.soundNumberOfChannels = numberOfChannels;
        t.soundSampleSize = sampleSizeInBits;
        t.soundSamplesPerPacket = frameDuration;
        if (isCompressed) {
            t.soundBytesPerPacket = soundBytesPerPacket;
            t.soundBytesPerFrame = soundBytesPerPacket * numberOfChannels;
        } else {
            t.soundBytesPerPacket = soundBytesPerPacket;
            t.soundBytesPerFrame = soundBytesPerPacket * numberOfChannels;
        }
        t.soundBytesPerSample = sampleSizeInBits / 8;

        t.format = new Format(
                DataClassKey, byte[].class,
                MediaTypeKey, MediaType.AUDIO,
                MimeTypeKey, MIME_QUICKTIME,
                EncodingKey, compressionType,
                SampleRateKey, Rational.valueOf(sampleRate),
                SampleSizeInBitsKey, sampleSizeInBits,
                ChannelsKey, numberOfChannels,
                FrameSizeKey, soundBytesPerPacket,
                SampleRateKey, Rational.valueOf(sampleRate),
                SignedKey, signed,
                ByteOrderKey, byteOrder);
        tracks.add(t);
        return tracks.size() - 1;
    }

    /**
     * Sets the compression quality of a track. <p> A value of 0 stands for
     * "high compression is important" a value of 1 for "high image quality is
     * important". <p> Changing this value affects the encoding of video frames
     * which are subsequently written into the track. Frames which have already
     * been written are not changed. <p> This value has no effect on videos
     * encoded with lossless encoders such as the PNG format. <p> The default
     * value is 0.97.
     *
     * @param newValue the new value
     */
    public void setCompressionQuality(int track, float newValue) {
        VideoTrack vt = (VideoTrack) tracks.get(track);
        vt.videoQuality = newValue;
    }

    /**
     * Returns the compression quality of a track.
     *
     * @return compression quality
     */
    public float getCompressionQuality(int track) {
        return ((VideoTrack) tracks.get(track)).videoQuality;
    }

    /**
     * Sets the sync interval for the specified video track.
     *
     * @param track The track number.
     * @param i     Interval between sync samples (keyframes). 0 = automatic. 1 =
     *              write all samples as sync samples. n = sync every n-th sample.
     */
    public void setSyncInterval(int track, int i) {
        tracks.get(track).syncInterval = i;
    }

    /**
     * Gets the sync interval from the specified video track.
     */
    public int getSyncInterval(int track) {
        return tracks.get(track).syncInterval;
    }

    /**
     * Sets the creation time of the movie.
     */
    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * Gets the creation time of the movie.
     */
    public Instant getCreationTime() {
        return creationTime;
    }

    /**
     * Sets the modification time of the movie.
     */
    public void setModificationTime(Instant modificationTime) {
        this.modificationTime = modificationTime;
    }

    /**
     * Gets the modification time of the movie.
     */
    public Instant getModificationTime() {
        return modificationTime;
    }

    /**
     * Gets the preferred rate at which to play this movie. A value of 1.0
     * indicates normal rate.
     */
    public double getPreferredRate() {
        return preferredRate;
    }

    /**
     * Sets the preferred rate at which to play this movie. A value of 1.0
     * indicates normal rate.
     */
    public void setPreferredRate(double preferredRate) {
        this.preferredRate = preferredRate;
    }

    /**
     * Gets the preferred volume of this movie’s sound. A value of 1.0 indicates
     * full volume.
     */
    public double getPreferredVolume() {
        return preferredVolume;
    }

    /**
     * Sets the preferred volume of this movie’s sound. A value of 1.0 indicates
     * full volume.
     */
    public void setPreferredVolume(double preferredVolume) {
        this.preferredVolume = preferredVolume;
    }

    /**
     * Gets the time value for current time position within the movie.
     */
    public long getCurrentTime() {
        return currentTime;
    }

    /**
     * Sets the time value for current time position within the movie.
     */
    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    /**
     * Gets the time value of the time of the movie poster.
     */
    public long getPosterTime() {
        return posterTime;
    }

    /**
     * Sets the time value of the time of the movie poster.
     */
    public void setPosterTime(long posterTime) {
        this.posterTime = posterTime;
    }

    /**
     * Gets the duration of the movie preview in movie time scale units.
     */
    public long getPreviewDuration() {
        return previewDuration;
    }

    /**
     * Gets the duration of the movie preview in movie time scale units.
     */
    public void setPreviewDuration(long previewDuration) {
        this.previewDuration = previewDuration;
    }

    /**
     * Gets the time value in the movie at which the preview begins.
     */
    public long getPreviewTime() {
        return previewTime;
    }

    /**
     * The time value in the movie at which the preview begins.
     */
    public void setPreviewTime(long previewTime) {
        this.previewTime = previewTime;
    }

    /**
     * The duration of the current selection in movie time scale units.
     */
    public long getSelectionDuration() {
        return selectionDuration;
    }

    /**
     * The duration of the current selection in movie time scale units.
     */
    public void setSelectionDuration(long selectionDuration) {
        this.selectionDuration = selectionDuration;
    }

    /**
     * The time value for the start time of the current selection.
     */
    public long getSelectionTime() {
        return selectionTime;
    }

    /**
     * The time value for the start time of the current selection.
     */
    public void setSelectionTime(long selectionTime) {
        this.selectionTime = selectionTime;
    }

    /**
     * Sets the transformation matrix of the entire movie.
     * <pre>
     * {a, b, u,
     *  c, d, v,
     *  tx,ty,w} // X- and Y-Translation
     *
     *           [ a  b  u
     * [x y 1] *   c  d  v   = [x' y' 1]
     * </pre> tx ty w ]
     *
     * @param matrix The transformation matrix.
     */
    public void setMovieTransformationMatrix(double[] matrix) {
        if (matrix.length != 9) {
            throw new IllegalArgumentException("matrix must have 9 elements, matrix.length=" + matrix.length);
        }

        System.arraycopy(matrix, 0, movieMatrix, 0, 9);
    }

    /**
     * Gets the transformation matrix of the entire movie.
     *
     * @return The transformation matrix.
     */
    public double[] getMovieTransformationMatrix() {
        return movieMatrix.clone();
    }

    /**
     * Sets the transformation matrix of the specified track.
     * <pre>
     * {a, b, u,
     *  c, d, v,
     *  tx,ty,w} // X- and Y-Translation
     *
     *           [ a  b  u
     * [x y 1] *   c  d  v   = [x' y' 1]
     * </pre> tx ty w ]
     *
     * @param track  The track number.
     * @param matrix The transformation matrix.
     */
    public void setTransformationMatrix(int track, double[] matrix) {
        if (matrix.length != 9) {
            throw new IllegalArgumentException("matrix must have 9 elements, matrix.length=" + matrix.length);
        }

        System.arraycopy(matrix, 0, tracks.get(track).matrix, 0, 9);
    }

    /**
     * Gets the transformation matrix of the specified track.
     *
     * @param track The track number.
     * @return The transformation matrix.
     */
    public double[] getTransformationMatrix(int track) {
        return tracks.get(track).matrix.clone();
    }

    /**
     * Sets the state of the MP4Writer to started. <p> If the state is
     * changed by this method, the prolog is written.
     */
    protected void ensureStarted() throws IOException {
        ensureOpen();
        if (state == States.FINISHED) {
            throw new IOException("Can not write into finished movie.");
        }
        if (state != States.STARTED) {
            writeProlog();
            mdatAtom = new WideDataAtom("mdat", out);
            state = States.STARTED;
        }
    }


    /**
     * Writes an already encoded sample from a file into a track. <p> This
     * method does not inspect the contents of the samples. The contents has to
     * match the format and dimensions of the media in this track.
     *
     * @param track    The track index.
     * @param file     The file which holds the encoded data sample.
     * @param duration The duration of the sample in media time scale units.
     * @param isSync   whether the sample is a sync sample (key frame).
     * @throws IOException if writing the sample data failed.
     */
    public void writeSample(int track, File file, long duration, boolean isSync) throws IOException {
        ensureStarted();
        FileInputStream in = null;
        try {
            in = new FileInputStream(file);
            writeSample(track, in, duration, isSync);
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    /**
     * Writes an already encoded sample from an input stream into a track. <p>
     * This method does not inspect the contents of the samples. The contents
     * have to match the format and dimensions of the media in this track.
     *
     * @param track    The track index.
     * @param in       The input stream which holds the encoded sample data.
     * @param duration The duration of the video frame in media time scale
     *                 units.
     * @param isSync   Whether the sample is a sync sample (keyframe).
     * @throws IllegalArgumentException if the duration is less than 1.
     * @throws IOException              if writing the sample data failed.
     */
    public void writeSample(int track, InputStream in, long duration, boolean isSync) throws IOException {
        ensureStarted();
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be greater 0");
        }
        Track t = tracks.get(track); // throws index out of bounds exception if illegal track index
        ensureOpen();
        ensureStarted();
        long offset = getRelativeStreamPosition();
        QTFFImageOutputStream mdatOut = mdatAtom.getOutputStream();
        IOStreams.copy(in, mdatOut);
        long length = getRelativeStreamPosition() - offset;
        t.addSample(new Sample(duration, offset, length), 1, isSync);
    }

    /**
     * Writes an already encoded sample from a byte array into a track. <p> This
     * method does not inspect the contents of the samples. The contents has to
     * match the format and dimensions of the media in this track.
     *
     * @param track    The track index.
     * @param data     The encoded sample data.
     * @param duration The duration of the sample in media time scale units.
     * @param isSync   Whether the sample is a sync sample.
     * @throws IllegalArgumentException if the duration is less than 1.
     * @throws IOException              if writing the sample data failed.
     */
    public void writeSample(int track, byte[] data, long duration, boolean isSync) throws IOException {
        writeSample(track, data, 0, data.length, duration, isSync);
    }

    /**
     * Writes an already encoded sample from a byte array into a track. <p> This
     * method does not inspect the contents of the samples. The contents has to
     * match the format and dimensions of the media in this track.
     *
     * @param track    The track index.
     * @param data     The encoded sample data.
     * @param off      The start offset in the data.
     * @param len      The number of bytes to write.
     * @param duration The duration of the sample in media time scale units.
     * @param isSync   Whether the sample is a sync sample (keyframe).
     * @throws IllegalArgumentException if the duration is less than 1.
     * @throws IOException              if writing the sample data failed.
     */
    public void writeSample(int track, byte[] data, int off, int len, long duration, boolean isSync) throws IOException {
        ensureStarted();
        if (duration <= 0) {
            throw new IllegalArgumentException("duration must be greater 0");
        }
        Track t = tracks.get(track); // throws index out of bounds exception if illegal track index
        ensureOpen();
        ensureStarted();
        long offset = getRelativeStreamPosition();
        ImageOutputStream mdatOut = mdatAtom.getOutputStream();
        if (false) {
            ByteBuffer bb = scrapeAvcDecoderConfigurationRecord(track, data, off, len, isSync);
            mdatOut.write(bb.array(), bb.arrayOffset(), bb.remaining());
        } else {
            mdatOut.write(data, off, len);
        }
        t.addSample(new Sample(duration, offset, len), 1, isSync);
    }

    private ByteBuffer scrapeAvcDecoderConfigurationRecord(int track, byte[] data, int off, int len, boolean isSync) {
        return null;
    }

    /**
     * Writes multiple sync samples from a byte array into a track. <p> This
     * method does not inspect the contents of the samples. The contents has to
     * match the format and dimensions of the media in this track.
     *
     * @param track          The track index.
     * @param sampleCount    The number of samples.
     * @param data           The encoded sample data. The length of data must be dividable
     *                       by sampleCount.
     * @param sampleDuration The duration of a sample. All samples must have the
     *                       same duration.
     * @throws IllegalArgumentException if {@code sampleDuration} is less than 1
     *                                  or if the length of {@code data} is not dividable by {@code sampleCount}.
     * @throws IOException              if writing the chunk failed.
     */
    public void writeSamples(int track, int sampleCount, byte[] data, long sampleDuration, boolean isSync) throws IOException {
        writeSamples(track, sampleCount, data, 0, data.length, sampleDuration, isSync);
    }

    /**
     * Writes multiple sync samples from a byte array into a track. <p> This
     * method does not inspect the contents of the samples. The contents has to
     * match the format and dimensions of the media in this track.
     *
     * @param track          The track index.
     * @param sampleCount    The number of samples.
     * @param data           The encoded sample data.
     * @param off            The start offset in the data.
     * @param len            The number of bytes to write. Must be dividable by
     *                       sampleCount.
     * @param sampleDuration The duration of a sample. All samples must have the
     *                       same duration.
     * @throws IllegalArgumentException if the duration is less than 1.
     * @throws IOException              if writing the sample data failed.
     */
    public void writeSamples(int track, int sampleCount, byte[] data, int off, int len, long sampleDuration) throws IOException {
        writeSamples(track, sampleCount, data, off, len, sampleDuration, true);
    }

    /**
     * Writes multiple samples from a byte array into a track. <p> This method
     * does not inspect the contents of the data. The contents has to match the
     * format and dimensions of the media in this track.
     *
     * @param track          The track index.
     * @param sampleCount    The number of samples.
     * @param data           The encoded sample data.
     * @param off            The start offset in the data.
     * @param len            The number of bytes to write. Must be dividable by
     *                       sampleCount.
     * @param sampleDuration The duration of a sample. All samples must have the
     *                       same duration.
     * @param isSync         Whether the samples are sync samples. All samples must
     *                       either be sync samples or non-sync samples.
     * @throws IllegalArgumentException if the duration is less than 1.
     * @throws IOException              if writing the sample data failed.
     */
    public void writeSamples(int track, int sampleCount, byte[] data, int off, int len, long sampleDuration, boolean isSync) throws IOException {
        ensureStarted();
        if (sampleDuration <= 0) {
            throw new IllegalArgumentException("sampleDuration must be greater 0, sampleDuration=" + sampleDuration + " track=" + track);
        }
        if (sampleCount <= 0) {
            throw new IllegalArgumentException("sampleCount must be greater 0, sampleCount=" + sampleCount + " track=" + track);
        }
        if (len % sampleCount != 0) {
            throw new IllegalArgumentException("len must be divisable by sampleCount len=" + len + " sampleCount=" + sampleCount + " track=" + track);
        }
        Track t = tracks.get(track); // throws index out of bounds exception if illegal track index
        ensureOpen();
        ensureStarted();
        long offset = getRelativeStreamPosition();
        ImageOutputStream mdatOut = mdatAtom.getOutputStream();
        int sampleLength;
        mdatOut.write(data, off, len);
        sampleLength = len / sampleCount;

        Sample first = new Sample(sampleDuration, offset, sampleLength);
        Sample last = new Sample(sampleDuration, offset + (long) sampleLength * (sampleCount - 1), sampleLength);
        t.addChunk(new Chunk(first, last, sampleCount, 1), isSync);
    }

    /**
     * Returns true if the limit for media samples has been reached. If this
     * limit is reached, no more samples should be added to the movie. <p>
     * QuickTime files can be up to 64 TB long, but there are other values that
     * may overflow before this size is reached. This method returns true when
     * the files size exceeds 2^60 or when the media duration value of a track
     * exceeds 2^61.
     */
    public boolean isDataLimitReached() {
        try {
            long maxMediaDuration = 0;
            for (Track t : tracks) {
                maxMediaDuration = max(t.mediaDuration, maxMediaDuration);
            }

            return getRelativeStreamPosition() > (1L << 61) //
                    || maxMediaDuration > 1L << 61;
        } catch (IOException ex) {
            return true;
        }
    }

    /**
     * Closes the movie file as well as the stream being filtered.
     *
     * @throws IOException if an I/O error has occurred
     */
    public void close() throws IOException {
        try {
            if (state == States.STARTED) {
                finish();
            }
        } finally {
            if (state != States.CLOSED) {
                out.close();
                state = States.CLOSED;
            }
        }
    }

    /**
     * Finishes writing the contents of the QuickTime output stream without
     * closing the underlying stream. Use this method when applying multiple
     * filters in succession to the same output stream.
     *
     * @throws IllegalStateException if the dimension of the video track has
     *                               not been specified or determined yet.
     * @throws IOException           if an I/O exception has occurred
     */
    public void finish() throws IOException {
        ensureOpen();
        if (state != States.FINISHED) {
            for (int i = 0, n = tracks.size(); i < n; i++) {
            }
            mdatAtom.finish();
            writeEpilog();
            state = States.FINISHED;
            /*
             for (int i = 0, n = tracks.size(); i < n; i++) {
             if (tracks.get(i) instanceof VideoTrack) {
             VideoTrack t = (VideoTrack) tracks.get(i);
             t.videoWidth = t.videoHeight = -1;
             }
             }*/
        }
    }

    /**
     * Check to make sure that this stream has not been closed
     */
    protected void ensureOpen() throws IOException {
        if (state == States.CLOSED) {
            throw new IOException("Stream closed");
        }
    }

    /**
     * Writes the stream prolog.
     */
    private void writeProlog() throws IOException {
        /* File type atom
         *
         typedef struct {
         magic brand;
         bcd4 versionYear;
         bcd2 versionMonth;
         bcd2 versionMinor;
         magic[4] compatibleBrands;
         } ftypAtom;
         */
        DataAtom ftypAtom = new DataAtom("ftyp", out);
        QTFFImageOutputStream d = ftypAtom.getOutputStream();
        d.writeType("isom"); // brand
        d.writeBCD4(0); // versionYear
        d.writeBCD2(2); // versionMonth
        d.writeBCD2(0); // versionMinor
        d.writeType("isom"); // compatibleBrands
        d.writeType("iso2"); // compatibleBrands
        d.writeType("avc1"); // compatibleBrands
        d.writeType("mp41"); // compatibleBrands
        ftypAtom.finish();
    }

    private void writeEpilog() throws IOException {
        long duration = getMovieDuration();

        DataAtom leaf;

        /* Movie Atom ========= */
        moovAtom = new CompositeAtom("moov", out);

        /* Movie Header Atom -------------
         * The data contained in this atom defines characteristics of the entire
         * QuickTime movie, such as time scale and duration. It has an atom type
         * value of 'mvhd'.
         *
         * typedef struct {
         byte version;
         byte[3] flags;
         mactimestamp creationTime;
         mactimestamp modificationTime;
         int timeScale;
         int duration;
         fixed16d16 preferredRate;
         fixed8d8 preferredVolume;
         byte[10] reserved;
         fixed16d16 matrixA;
         fixed16d16 matrixB;
         fixed2d30 matrixU;
         fixed16d16 matrixC;
         fixed16d16 matrixD;
         fixed2d30 matrixV;
         fixed16d16 matrixX;
         fixed16d16 matrixY;
         fixed2d30 matrixW;
         int previewTime;
         int previewDuration;
         int posterTime;
         int selectionTime;
         int selectionDuration;
         int currentTime;
         int nextTrackId;
         } movieHeaderAtom;
         */
        leaf = new DataAtom("mvhd", out);
        moovAtom.add(leaf);
        QTFFImageOutputStream d = leaf.getOutputStream();
        d.writeByte(0); // version
        // A 1-byte specification of the version of this movie header atom.

        d.writeByte(0); // flags[0]
        d.writeByte(0); // flags[1]
        d.writeByte(0); // flags[2]
        // Three bytes of space for future movie header flags.

        d.writeMacTimestamp(creationTime); // creationTime
        // A 32-bit integer that specifies the calendar date and time (in
        // seconds since midnight, January 1, 1904) when the movie atom was
        // created. It is strongly recommended that this value should be
        // specified using coordinated universal time (UTC).

        d.writeMacTimestamp(modificationTime); // modificationTime
        // A 32-bit integer that specifies the calendar date and time (in
        // seconds since midnight, January 1, 1904) when the movie atom was
        // changed. BooleanIt is strongly recommended that this value should be
        // specified using coordinated universal time (UTC).

        d.writeUInt(movieTimeScale); // timeScale
        // A time value that indicates the time scale for this movie—that is,
        // the number of time units that pass per second in its time coordinate
        // system. A time coordinate system that measures time in sixtieths of a
        // second, for example, has a time scale of 60.

        d.writeUInt(duration); // duration
        // A time value that indicates the duration of the movie in time scale
        // units. Note that this property is derived from the movie’s tracks.
        // The value of this field corresponds to the duration of the longest
        // track in the movie.

        d.writeFixed16D16(preferredRate); // preferredRate
        // A 32-bit fixed-point number that specifies the rate at which to play
        // this movie. A value of 1.0 indicates normal rate.

        d.writeFixed8D8(preferredVolume); // preferredVolume
        // A 16-bit fixed-point number that specifies how loud to play this
        // movie’s sound. A value of 1.0 indicates full volume.

        d.write(new byte[10]); // reserved;
        // Ten bytes reserved for use by Apple. Set to 0.

        d.writeFixed16D16(movieMatrix[0]); // matrix[0]
        d.writeFixed16D16(movieMatrix[1]); // matrix[1]
        d.writeFixed2D30(movieMatrix[2]); // matrix[2]
        d.writeFixed16D16(movieMatrix[3]); // matrix[3]
        d.writeFixed16D16(movieMatrix[4]); // matrix[4]
        d.writeFixed2D30(movieMatrix[5]); // matrix[5]
        d.writeFixed16D16(movieMatrix[6]); // matrix[6]
        d.writeFixed16D16(movieMatrix[7]); // matrix[7]
        d.writeFixed2D30(movieMatrix[8]); // matrix[8]

        // The matrix structure associated with this movie. A matrix shows how
        // to map points from one coordinate space into another. See “Matrices”
        // for a discussion of how display matrices are used in QuickTime:
        // http://developer.apple.com/documentation/QuickTime/QTFF/QTFFChap4/chapter_5_section_4.html#//apple_ref/doc/uid/TP40000939-CH206-18737

        d.writeUInt(previewTime); // previewTime
        // The time value in the movie at which the preview begins.

        d.writeUInt(previewDuration); // previewDuration
        // The duration of the movie preview in movie time scale units.

        d.writeUInt(posterTime); // posterTime
        // The time value of the time of the movie poster.

        d.writeUInt(selectionTime); // selectionTime
        // The time value for the start time of the current selection.

        d.writeUInt(selectionDuration); // selectionDuration
        // The duration of the current selection in movie time scale units.

        d.writeUInt(currentTime); // currentTime;
        // The time value for current time position within the movie.

        d.writeUInt(tracks.size() + 1); // nextTrackId
        // A 32-bit integer that indicates a value to use for the track ID
        // number of the next track added to this movie. Note that 0 is not a
        // valid track ID value.

        for (int i = 0, n = tracks.size(); i < n; i++) {
            /* Track Atom ======== */
            writeTrackAtoms(i, moovAtom, modificationTime);
        }

        // Optional color table atom
        for (Track t : tracks) {
            if (t instanceof VideoTrack) {
                VideoTrack vt = (VideoTrack) t;
                if (vt.videoColorTable != null) {
                    writeVideoColorTableAtom(vt, moovAtom);
                    break;
                }
            }
        }


        //
        moovAtom.finish();
    }

    protected void writeTrackAtoms(int trackIndex, CompositeAtom moovAtom, Instant modificationTime) throws IOException {
        Track t = tracks.get(trackIndex);

        DataAtom leaf;
        QTFFImageOutputStream d;

        /* Track Atom ======== */
        CompositeAtom trakAtom = new CompositeAtom("trak", out);
        moovAtom.add(trakAtom);

        /* Track Header Atom -----------
         * The track header atom specifies the characteristics of a single track
         * within a movie. A track header atom contains a size field that
         * specifies the number of bytes and a type field that indicates the
         * format of the data (defined by the atom type 'tkhd').
         *
         typedef struct {
         byte version;
         byte flag0;
         byte flag1;
         byte set TrackHeaderFlags flag2;
         mactimestamp creationTime;
         mactimestamp modificationTime;
         int trackId;
         byte[4] reserved;
         int duration;
         byte[8] reserved;
         short layer;
         short alternateGroup;
         short volume;
         byte[2] reserved;
         int[9] matrix;
         int trackWidth;
         int trackHeight;
         } trackHeaderAtom;     */
        leaf = new DataAtom("tkhd", out);
        trakAtom.add(leaf);
        d = leaf.getOutputStream();
        d.write(0); // version
        // A 1-byte specification of the version of this track header.

        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(t.headerFlags); // flag[2]
        // Three bytes that are reserved for the track header flags. These flags
        // indicate how the track is used in the movie. The following flags are
        // valid (all flags are enabled when set to 1):
        //
        // Track enabled
        //      Indicates that the track is enabled. Flag value is 0x0001.
        // Track in movie
        //      Indicates that the track is used in the movie. Flag value is
        //      0x0002.
        // Track in preview
        //      Indicates that the track is used in the movie’s preview. Flag
        //      value is 0x0004.
        // Track in poster
        //      Indicates that the track is used in the movie’s poster. Flag
        //      value is 0x0008.

        d.writeMacTimestamp(creationTime); // creationTime
        // A 32-bit integer that indicates the calendar date and time (expressed
        // in seconds since midnight, January 1, 1904) when the track header was
        // created. It is strongly recommended that this value should be
        // specified using coordinated universal time (UTC).

        d.writeMacTimestamp(modificationTime); // modificationTime
        // A 32-bit integer that indicates the calendar date and time (expressed
        // in seconds since midnight, January 1, 1904) when the track header was
        // changed. It is strongly recommended that this value should be
        // specified using coordinated universal time (UTC).

        d.writeInt(trackIndex + 1); // trackId
        // A 32-bit integer that uniquely identifies the track. The value 0
        // cannot be used.

        d.writeInt(0); // reserved;
        // A 32-bit integer that is reserved for use by Apple. Set this field to 0.

        d.writeUInt(t.getTrackDuration(movieTimeScale)); // duration
        // A time value that indicates the duration of this track (in the
        // movie’s time coordinate system). Note that this property is derived
        // from the track’s edits. The value of this field is equal to the sum
        // of the durations of all of the track’s edits. If there is no edit
        // list, then the duration is the sum of the sample durations, converted
        // into the movie timescale.

        d.writeLong(0); // reserved
        // An 8-byte value that is reserved for use by Apple. Set this field to 0.

        d.writeShort(0); // layer;
        // A 16-bit integer that indicates this track’s spatial priority in its
        // movie. The QuickTime Movie Toolbox uses this value to determine how
        // tracks overlay one another. Tracks with lower layer values are
        // displayed in front of tracks with higher layer values.

        d.writeShort(0); // alternate group
        // A 16-bit integer that specifies a collection of movie tracks that
        // contain alternate data for one another. QuickTime chooses one track
        // from the group to be used when the movie is played. The choice may be
        // based on such considerations as playback quality, language, or the
        // capabilities of the computer.

        d.writeFixed8D8(t.mediaType == MediaType.AUDIO ? 1 : 0); // volume
        // A 16-bit fixed-point value that indicates how loudly this track’s
        // sound is to be played. A value of 1.0 indicates normal volume.

        d.writeShort(0); // reserved
        // A 16-bit integer that is reserved for use by Apple. Set this field to 0.

        double[] m = t.matrix;
        d.writeFixed16D16(m[0]); // matrix[0]
        d.writeFixed16D16(m[1]); // matrix[1]
        d.writeFixed2D30(m[2]); // matrix[2]
        d.writeFixed16D16(m[3]); // matrix[3]
        d.writeFixed16D16(m[4]); // matrix[4]
        d.writeFixed2D30(m[5]); // matrix[5]
        d.writeFixed16D16(m[6]); // matrix[6]
        d.writeFixed16D16(m[7]); // matrix[7]
        d.writeFixed2D30(m[8]); // matrix[8]
        // The matrix structure associated with this track.
        // See Figure 2-8 for an illustration of a matrix structure:
        // http://developer.apple.com/documentation/QuickTime/QTFF/QTFFChap2/chapter_3_section_3.html#//apple_ref/doc/uid/TP40000939-CH204-32967

        d.writeFixed16D16(t.mediaType == MediaType.VIDEO ? t.width : 0); // width
        // A 32-bit fixed-point number that specifies the width of this track in pixels.

        d.writeFixed16D16(t.mediaType == MediaType.VIDEO ? t.height : 0); // height
        // A 32-bit fixed-point number that indicates the height of this track in pixels.

        if (t instanceof VideoTrack) {
            VideoTrack vt = (VideoTrack) t;
            //writeTaptAtoms(trakAtom, vt);
        }
        writeEditAtoms(trakAtom, t);
        writeMediaAtoms(trackIndex, modificationTime, trakAtom, t);
    }

    /*
    private void writeTaptAtoms(CompositeAtom trakAtom, VideoTrack t) throws IOException {
        DataAtom leaf;
        QTFFImageOutputStream d;
        /* TAPT Atom ========= * /
        CompositeAtom taptAtom = new CompositeAtom("tapt");
        trakAtom.add(taptAtom);

        /* Track Clean Aperture Dimensions,
          Track Production Aperture Dimensions,
          Track Encoded Pixels Dimensions
         */
        /*
        typedef struct {
            byte version;
            byte[3] flags;
            fixed16d16 width;
            fixed16d16 height;
        } clefAtom;
         * /
        for (String id : List.of("clef", "prof", "enof")) {
            leaf = new DataAtom(id);
            taptAtom.add(leaf);
            d = leaf.getOutputStream();
            d.write(0); // version
            d.write(0); // flag[0]
            d.write(0); // flag[1]
            d.write(0); // flag[2]
            d.writeFixed16D16(t.width);
            d.writeFixed16D16(t.height);
        }
    }*/

    private void writeMediaAtoms(int trackIndex, Instant modificationTime, CompositeAtom trakAtom, Track t) throws IOException {
        DataAtom leaf;
        QTFFImageOutputStream d;
        /* Media Atom ========= */
        CompositeAtom mdiaAtom = new CompositeAtom("mdia", out);
        trakAtom.add(mdiaAtom);

        /* Media Header atom -------
         typedef struct {
         byte version;
         byte[3] flags;
         mactimestamp creationTime;
         mactimestamp modificationTime;
         int timeScale;
         int duration;
         short language;
         short quality;
         } mediaHeaderAtom;*/
        leaf = new DataAtom("mdhd", out);
        mdiaAtom.add(leaf);
        d = leaf.getOutputStream();
        d.write(0); // version
        // One byte that specifies the version of this header atom.

        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(0); // flag[2]
        // Three bytes of space for media header flags. Set this field to 0.

        d.writeMacTimestamp(creationTime); // creationTime
        // A 32-bit integer that specifies (in seconds since midnight, January
        // 1, 1904) when the media atom was created. It is strongly recommended
        // that this value should be specified using coordinated universal time
        // (UTC).

        d.writeMacTimestamp(modificationTime); // modificationTime
        // A 32-bit integer that specifies (in seconds since midnight, January
        // 1, 1904) when the media atom was changed. It is strongly recommended
        // that this value should be specified using coordinated universal time
        // (UTC).

        d.writeUInt(t.mediaTimeScale); // timeScale
        // A time value that indicates the time scale for this media—that is,
        // the number of time units that pass per second in its time coordinate
        // system.

        d.writeUInt(t.mediaDuration); // duration
        // The duration of this media in units of its time scale.

        d.writeShort(0); // language;
        // A 16-bit integer that specifies the language code for this media.
        // See “Language Code Values” for valid language codes:
        // http://developer.apple.com/documentation/QuickTime/QTFF/QTFFChap4/chapter_5_section_2.html#//apple_ref/doc/uid/TP40000939-CH206-27005

        d.writeShort(0); // quality
        // A 16-bit integer that specifies the media’s playback quality—that is,
        // its suitability for playback in a given environment.

        /*
         * Media Handler Reference Atom -------
         */
        leaf = new DataAtom("hdlr", out);
        mdiaAtom.add(leaf);
        /*typedef struct {
        byte version;
        byte[3] flags;
        magic componentType;
        magic componentSubtype;
        magic componentManufacturer;
        int componentFlags;
        int componentFlagsMask;
        cstring componentName;
        ubyte[] extraData;
        } handlerReferenceAtom;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        // A 1-byte specification of the version of this handler information.

        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(0); // flag[2]
        // A 3-byte space for handler information flags. Set this field to 0.

        d.writeType("mhlr"); // componentType
        // A four-character code that identifies the type of the handler. Only
        // two values are valid for this field: 'mhlr' for media handlers and
        // 'dhlr' for data handlers.

        d.writeType(t.mediaType == MediaType.VIDEO ? "vide" : "soun"); // componentSubtype
        // A four-character code that identifies the type of the media handler
        // or data handler. For media handlers, this field defines the type of
        // data—for example, 'vide' for video data or 'soun' for sound data.
        //
        // For data handlers, this field defines the data reference type—for
        // example, a component subtype value of 'alis' identifies a file alias.


        d.writeType(t.componentManufacturer);        // componentManufacturer

        d.writeUInt(t.mediaType == MediaType.AUDIO ? 268435456L : 0); // componentFlags
        // Reserved. Set to 0.

        d.writeUInt(t.mediaType == MediaType.AUDIO ? 65941 : 0); // componentFlagsMask
        // Reserved. Set to 0.

        d.writeCString(t.componentName); // componentName (empty string)
        // A (counted) string that specifies the name of the component—that is,
        // the media handler used when this media was created. This field may
        // contain a zero-length (empty) string.

        /* Media Information atom ========= */
        writeMediaInformationAtoms(trackIndex, mdiaAtom);
    }

    private void writeEditAtoms(CompositeAtom trakAtom, Track t) throws IOException {
        Edit[] elist = t.editList;
        if (elist == null || elist.length == 0) {
            return;
        }

        QTFFImageOutputStream d;
        DataAtom leaf;
        /* Edit Atom ========= */
        CompositeAtom edtsAtom = new CompositeAtom("edts", out);
        trakAtom.add(edtsAtom);

        /* Edit List atom ------- */
        /*
         typedef struct {
         byte version;
         byte[3] flags;
         int numberOfEntries;
         editListTable editListTable[numberOfEntries];
         } editListAtom;

         typedef struct {
         int trackDuration;
         int mediaTime;
         fixed16d16 mediaRate;
         } editListTable;
         */
        leaf = new DataAtom("elst", out);
        edtsAtom.add(leaf);
        d = leaf.getOutputStream();

        d.write(0); // version
        // One byte that specifies the version of this header atom.

        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(0); // flag[2]

        d.writeUInt(elist.length); // numberOfEntries
        for (Edit edit : elist) {
            d.writeUInt(edit.trackDuration); // trackDuration
            d.writeUInt(edit.mediaTime); // mediaTime
            d.writeUInt(edit.mediaRate); // mediaRate
        }
    }


    protected void writeMediaInformationAtoms(int trackIndex, CompositeAtom mdiaAtom) throws IOException {
        Track t = tracks.get(trackIndex);
        DataAtom leaf;
        QTFFImageOutputStream d;
        /* Media Information atom ========= */
        CompositeAtom minfAtom = new CompositeAtom("minf", out);
        mdiaAtom.add(minfAtom);

        /* Video or Audio media information atom -------- */
        switch (t.mediaType) {
            case VIDEO -> writeVideoMediaInformationHeaderAtom(trackIndex, minfAtom);
            case AUDIO -> writeSoundMediaInformationHeaderAtom(trackIndex, minfAtom);
            default -> throw new UnsupportedOperationException("Media type " + t.mediaType + " not supported yet.");
        }


        /* Data Handler Reference Atom -------- */
        // The handler reference atom specifies the media handler component that
        // is to be used to interpret the media’s data. The handler reference
        // atom has an atom type value of 'hdlr'.
        leaf = new DataAtom("hdlr", out);
        minfAtom.add(leaf);
        /*typedef struct {
         byte version;
         byte[3] flags;
         magic componentType;
         magic componentSubtype;
         magic componentManufacturer;
         int componentFlags;
         int componentFlagsMask;
         cstring componentName;
         ubyte[] extraData;
         } handlerReferenceAtom;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        // A 1-byte specification of the version of this handler information.

        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(0); // flag[2]
        // A 3-byte space for handler information flags. Set this field to 0.

        d.writeType("dhlr"); // componentType
        // A four-character code that identifies the type of the handler. Only
        // two values are valid for this field: 'mhlr' for media handlers and
        // 'dhlr' for data handlers.

        d.writeType("alis"); // componentSubtype
        // A four-character code that identifies the type of the media handler
        // or data handler. For media handlers, this field defines the type of
        // data—for example, 'vide' for video data or 'soun' for sound data.
        // For data handlers, this field defines the data reference type—for
        // example, a component subtype value of 'alis' identifies a file alias.

        d.writeType(t.componentManufacturer);        // componentManufacturer

        d.writeUInt(t.mediaType == MediaType.AUDIO ? 268435457L : 0); // componentFlags
        // Reserved. Set to 0.

        d.writeInt(t.mediaType == MediaType.AUDIO ? 65967 : 0); // componentFlagsMask
        // Reserved. Set to 0.

        d.writeCString(t.componentName); // componentName (empty string)
        // A (counted) string that specifies the name of the component—that is,
        // the media handler used when this media was created. This field may
        // contain a zero-length (empty) string.

        /* Data information atom ===== */
        CompositeAtom dinfAtom = new CompositeAtom("dinf", out);
        minfAtom.add(dinfAtom);

        /* Data reference atom ----- */
        // Data reference atoms contain tabular data that instructs the data
        // handler component how to access the media’s data.
        leaf = new DataAtom("dref", out);
        dinfAtom.add(leaf);
        /*typedef struct {
         ubyte version;
         ubyte[3] flags;
         int numberOfEntries;
         dataReferenceEntry dataReference[numberOfEntries];
         } dataReferenceAtom;

         set {
         dataRefSelfReference=1 // I am not shure if this is the correct value for this flag
         } drefEntryFlags;

         typedef struct {
         int size;
         magic type;
         byte version;
         ubyte flag1;
         ubyte flag2;
         ubyte set drefEntryFlags flag3;
         byte[size - 12] data;
         } dataReferenceEntry;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        // A 1-byte specification of the version of this data reference atom.

        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(0); // flag[2]
        // A 3-byte space for data reference flags. Set this field to 0.

        d.writeInt(1); // numberOfEntries
        // A 32-bit integer containing the count of data references that follow.

        d.writeInt(12); // dataReference.size
        // A 32-bit integer that specifies the number of bytes in the data
        // reference.

        d.writeType("alis"); // dataReference.type
        // A 32-bit integer that specifies the type of the data in the data
        // reference. Table 2-4 lists valid type values:
        // http://developer.apple.com/documentation/QuickTime/QTFF/QTFFChap2/chapter_3_section_4.html#//apple_ref/doc/uid/TP40000939-CH204-38840

        d.write(0); // dataReference.version
        // A 1-byte specification of the version of the data reference.

        d.write(0); // dataReference.flag1
        d.write(0); // dataReference.flag2
        d.write(0x1); // dataReference.flag3
        // A 3-byte space for data reference flags. There is one defined flag.
        //
        // Self reference
        //      This flag indicates that the media’s data is in the same file as
        //      the movie atom. On the Macintosh, and other file systems with
        //      multifork files, set this flag to 1 even if the data resides in
        //      a different fork from the movie atom. This flag’s value is
        //      0x0001.


        /* Sample Table atom ========= */
        writeSampleTableAtoms(trackIndex, minfAtom);
    }

    protected void writeVideoMediaInformationHeaderAtom(int trackIndex, CompositeAtom minfAtom) throws IOException {
        DataAtom leaf;
        QTFFImageOutputStream d;

        /* Video media information atom -------- */
        leaf = new DataAtom("vmhd", out);
        minfAtom.add(leaf);
            /*typedef struct {
             byte version;
             byte flag1;
             byte flag2;
             byte set vmhdFlags flag3;
             short graphicsMode;
             ushort[3] opcolor;
             } videoMediaInformationHeaderAtom;*/
        d = leaf.getOutputStream();
        d.write(0); // version
        // One byte that specifies the version of this header atom.

        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(0x1); // flag[2]
        // Three bytes of space for media header flags.
        // This is a compatibility flag that allows QuickTime to distinguish
        // between movies created with QuickTime 1.0 and newer movies. You
        // should always set this flag to 1, unless you are creating a movie
        // intended for playback using version 1.0 of QuickTime. This flag’s
        // value is 0x0001.

        d.writeShort(0x0); // graphicsMode (0x40 = DitherCopy, 0x0 = Copy)
        // A 16-bit integer that specifies the transfer mode. The transfer mode
        // specifies which Boolean operation QuickDraw should perform when
        // drawing or transferring an image from one location to another.
        // See “Graphics Modes” for a list of graphics modes supported by
        // QuickTime:
        // http://developer.apple.com/documentation/QuickTime/QTFF/QTFFChap4/chapter_5_section_5.html#//apple_ref/doc/uid/TP40000939-CH206-18741

        d.writeUShort(0); // opcolor[0]
        d.writeUShort(0); // opcolor[1]
        d.writeUShort(0); // opcolor[2]
        // Three 16-bit values that specify the red, green, and blue colors for
        // the transfer mode operation indicated in the graphics mode field.
    }

    protected void writeSoundMediaInformationHeaderAtom(int trackIndex, CompositeAtom minfAtom) throws IOException {
        DataAtom leaf;
        QTFFImageOutputStream d;

        /* Sound media information header atom -------- */
        leaf = new DataAtom("smhd", out);
        minfAtom.add(leaf);
            /*typedef struct {
             ubyte version;
             ubyte[3] flags;
             short balance;
             short reserved;
             } soundMediaInformationHeaderAtom;*/
        d = leaf.getOutputStream();
        d.write(0); // version
        // A 1-byte specification of the version of this sound media information header atom.

        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(0); // flag[2]
        // A 3-byte space for sound media information flags. Set this field to 0.

        d.writeFixed8D8(0); // balance
        // A 16-bit integer that specifies the sound balance of this
        // sound media. Sound balance is the setting that controls
        // the mix of sound between the two speakers of a computer.
        // This field is normally set to 0.
        // Balance values are represented as 16-bit, fixed-point
        // numbers that range from -1.0 to +1.0. The high-order 8
        // bits contain the integer portion of the value; the
        // low-order 8 bits contain the fractional part. Negative
        // values weight the balance toward the left speaker;
        // positive values emphasize the right channel. Setting the
        // balance to 0 corresponds to a neutral setting.

        d.writeUShort(0); // reserved
        // Reserved for use by Apple. Set this field to 0.

    }

    protected void writeSampleTableAtoms(int trackIndex, CompositeAtom minfAtom) throws IOException {
        Track t = tracks.get(trackIndex);
        DataAtom leaf;
        QTFFImageOutputStream d;

        /* Sample Table atom ========= */
        CompositeAtom stblAtom = new CompositeAtom("stbl", out);
        minfAtom.add(stblAtom);

        /* Sample Description atom ------- */
        if (Objects.requireNonNull(t) instanceof VideoTrack) {
            VideoTrack vt = (VideoTrack) Objects.requireNonNull(t);
            writeVideoSampleDescriptionAtom(vt, stblAtom);
        } else if (t instanceof AudioTrack) {
            AudioTrack at = (AudioTrack) t;
            writeAudioSampleDescriptionAtom(at, stblAtom);
        } else {
            writeGenericSampleDescriptionAtom(t, stblAtom);
        }


        /* Time to Sample atom ---- */
        // Time-to-sample atoms store duration information for a media’s
        // samples, providing a mapping from a time in a media to the
        // corresponding data sample. The time-to-sample atom has an atom type
        // of 'stts'.
        leaf = new DataAtom("stts", out);
        stblAtom.add(leaf);
        /*
         typedef struct {
         byte version;
         byte[3] flags;
         int numberOfEntries;
         timeToSampleTable timeToSampleTable[numberOfEntries];
         } timeToSampleAtom;

         typedef struct {
         int sampleCount;
         int sampleDuration;
         } timeToSampleTable;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        // A 1-byte specification of the version of this time-to-sample atom.

        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(0); // flag[2]
        // A 3-byte space for time-to-sample flags. Set this field to 0.

        d.writeUInt(t.timeToSamples.size()); // numberOfEntries
        // A 32-bit integer containing the count of entries in the
        // time-to-sample table.

        for (TimeToSampleGroup tts : t.timeToSamples) {
            d.writeUInt(tts.getSampleCount()); // timeToSampleTable[0].sampleCount
            // A 32-bit integer that specifies the number of consecutive
            // samples that have the same duration.

            d.writeUInt(tts.getSampleDuration()); // timeToSampleTable[0].sampleDuration
            // A 32-bit integer that specifies the duration of each
            // sample.
        }
        /* sample to chunk atom -------- */
        // The sample-to-chunk atom contains a table that maps samples to chunks
        // in the media data stream. By examining the sample-to-chunk atom, you
        // can determine the chunk that contains a specific sample.
        leaf = new DataAtom("stsc", out);
        stblAtom.add(leaf);
        /*
         typedef struct {
         byte version;
         byte[3] flags;
         int numberOfEntries;
         sampleToChunkTable sampleToChunkTable[numberOfEntries];
         } sampleToChunkAtom;

         typedef struct {
         int firstChunk;
         int samplesPerChunk;
         int sampleDescription;
         } sampleToChunkTable;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        // A 1-byte specification of the version of this time-to-sample atom.

        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(0); // flag[2]
        // A 3-byte space for time-to-sample flags. Set this field to 0.

        int entryCount = 0;
        long previousSampleCount = -1;
        long previousSampleDescriptionId = -1;
        for (Chunk c : t.chunks) {
            if (c.sampleCount != previousSampleCount//
                    || c.sampleDescriptionId != previousSampleDescriptionId) {
                previousSampleCount = c.sampleCount;
                previousSampleDescriptionId = c.sampleDescriptionId;
                entryCount++;
            }
        }

        d.writeInt(entryCount); // number of entries
        // A 32-bit integer containing the count of entries in the sample-to-chunk table.

        int firstChunk = 1;
        previousSampleCount = -1;
        previousSampleDescriptionId = -1;
        for (Chunk c : t.chunks) {
            if (c.sampleCount != previousSampleCount//
                    || c.sampleDescriptionId != previousSampleDescriptionId) {
                previousSampleCount = c.sampleCount;
                previousSampleDescriptionId = c.sampleDescriptionId;

                d.writeUInt(firstChunk); // first chunk
                // The first chunk number using this table entry.

                d.writeUInt(c.sampleCount); // samples per chunk
                // The number of samples in each chunk.

                d.writeInt(c.sampleDescriptionId); // sample description

                // The identification number associated with the sample description for
                // the sample. For details on sample description atoms, see “Sample
                // Description Atoms.”:
                // http://developer.apple.com/documentation/QuickTime/QTFF/QTFFChap2/chapter_3_section_5.html#//apple_ref/doc/uid/TP40000939-CH204-25691
            }
            firstChunk++;
        }
        //
        /* sync sample atom -------- */
        if (t.syncSamples != null) {
            leaf = new DataAtom("stss", out);
            stblAtom.add(leaf);
            /*
             typedef struct {
             byte version;
             byte[3] flags;
             int numberOfEntries;
             syncSampleTable syncSampleTable[numberOfEntries];
             } syncSampleAtom;

             typedef struct {
             int number;
             } syncSampleTable;
             */
            d = leaf.getOutputStream();
            d.write(0); // version
            // A 1-byte specification of the version of this time-to-sample atom.

            d.write(0); // flag[0]
            d.write(0); // flag[1]
            d.write(0); // flag[2]
            // A 3-byte space for time-to-sample flags. Set this field to 0.

            d.writeUInt(t.syncSamples.size());
            // Number of entries
            //A 32-bit integer containing the count of entries in the sync sample table.

            for (Long number : t.syncSamples) {
                d.writeUInt(number);
                // Sync sample table A table of sample numbers; each sample
                // number corresponds to a key frame.
            }
        }


        /* sample size atom -------- */
        // The sample size atom contains the sample count and a table giving the
        // size of each sample. This allows the media data itself to be
        // unframed. The total number of samples in the media is always
        // indicated in the sample count. If the default size is indicated, then
        // no table follows.
        leaf = new DataAtom("stsz", out);
        stblAtom.add(leaf);
        /*
         typedef struct {
         byte version;
         byte[3] flags;
         int sampleSize;
         int numberOfEntries;
         sampleSizeTable sampleSizeTable[numberOfEntries];
         } sampleSizeAtom;

         typedef struct {
         int size;
         } sampleSizeTable;
         */
        d = leaf.getOutputStream();
        d.write(0); // version
        // A 1-byte specification of the version of this time-to-sample atom.

        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(0); // flag[2]
        // A 3-byte space for time-to-sample flags. Set this field to 0.

        int sampleUnit = t.mediaType == MediaType.AUDIO//
                && ((AudioTrack) t).soundCompressionId != -2 //
                ? ((AudioTrack) t).soundSampleSize / 8 * ((AudioTrack) t).soundNumberOfChannels//
                : 1;
        if (t.sampleSizes.size() == 1) {
            d.writeUInt(t.sampleSizes.get(0).getSampleLength() / sampleUnit); // sample size
            // A 32-bit integer specifying the sample size. If all the samples are
            // the same size, this field contains that size value. If this field is
            // set to 0, then the samples have different sizes, and those sizes are
            // stored in the sample size table.

            d.writeUInt(t.sampleSizes.get(0).getSampleCount()); // number of entries
            // A 32-bit integer containing the count of entries in the sample size
            // table.

        } else {
            d.writeUInt(0); // sample size
            // A 32-bit integer specifying the sample size. If all the samples are
            // the same size, this field contains that size value. If this field is
            // set to 0, then the samples have different sizes, and those sizes are
            // stored in the sample size table.


            long count = 0;
            for (SampleSizeGroup s : t.sampleSizes) {
                count += s.sampleCount;
            }
            d.writeUInt(count); // number of entries
            // A 32-bit integer containing the count of entries in the sample size
            // table.

            for (SampleSizeGroup s : t.sampleSizes) {
                long sampleSize = s.getSampleLength() / sampleUnit;
                for (int i = 0; i < s.sampleCount; i++) {
                    d.writeUInt(sampleSize); // sample size
                    // The size field contains the size, in bytes, of the sample in
                    // question. The table is indexed by sample number—the first entry
                    // corresponds to the first sample, the second entry is for the
                    // second sample, and so on.
                }
            }
        }
        //
        /* chunk offset atom -------- */
        // The chunk-offset table gives the index of each chunk into the
        // QuickTime Stream. There are two variants, permitting the use of
        // 32-bit or 64-bit offsets. The latter is useful when managing very
        // large movies. Only one of these variants occurs in any single
        // instance of a sample table atom.
        if (t.chunks.isEmpty() || t.chunks.get(t.chunks.size() - 1).getChunkOffset() <= 0xffffffffL) {
            /* 32-bit chunk offset atom -------- */
            leaf = new DataAtom("stco", out);
            stblAtom.add(leaf);
            /*
             typedef struct {
             byte version;
             byte[3] flags;
             int numberOfEntries;
             chunkOffsetEntry[numberOfEntries] chunkOffsetTable;
             } chunkOffsetAtom;

             typedef struct {
               int offset;
             } chunkOffsetEntry;
             */
            d = leaf.getOutputStream();
            d.write(0); // version
            // A 1-byte specification of the version of this time-to-sample atom.

            d.write(0); // flag[0]
            d.write(0); // flag[1]
            d.write(0); // flag[2]
            // A 3-byte space for time-to-sample flags. Set this field to 0.

            d.writeUInt(t.chunks.size()); // number of entries
            // A 32-bit integer containing the count of entries in the chunk
            // offset table.
            for (Chunk c : t.chunks) {
                d.writeUInt(c.getChunkOffset() + mdatOffset); // offset
                // The offset contains the byte offset from the beginning of the
                // data stream to the chunk. The table is indexed by chunk
                // number—the first table entry corresponds to the first chunk,
                // the second table entry is for the second chunk, and so on.
            }
        } else {
            /* 64-bit chunk offset atom -------- */
            leaf = new DataAtom("co64", out);
            stblAtom.add(leaf);
            /*
             typedef struct {
             byte version;
             byte[3] flags;
             int numberOfEntries;
             chunkOffset64Entry[numberOfEntries] chunkOffset64Table;
             } chunkOffset64Atom;

             typedef struct {
             long offset;
             } chunkOffset64Entry;
             */
            d = leaf.getOutputStream();
            d.write(0); // version
            // A 1-byte specification of the version of this time-to-sample atom.

            d.write(0); // flag[0]
            d.write(0); // flag[1]
            d.write(0); // flag[2]
            // A 3-byte space for time-to-sample flags. Set this field to 0.

            d.writeUInt(t.chunks.size()); // number of entries
            // A 32-bit integer containing the count of entries in the chunk
            // offset table.

            for (Chunk c : t.chunks) {
                d.writeLong(c.getChunkOffset()); // offset
                // The offset contains the byte offset from the beginning of the
                // data stream to the chunk. The table is indexed by chunk
                // number—the first table entry corresponds to the first chunk,
                // the second table entry is for the second chunk, and so on.
            }
        }
    }

    private void writeGenericSampleDescriptionAtom(Track t, CompositeAtom stblAtom) {
        // empty, for now
    }

    /**
     * Writes a version of the movie which is optimized for the web into the
     * specified output file. <p> This method finishes the movie and then copies
     * its content into the specified file. The web-optimized file starts with
     * the movie header.
     *
     * @param outputFile     The output file
     * @param compressHeader Whether the movie header shall be compressed.
     */
    public void toWebOptimizedMovie(File outputFile, boolean compressHeader) throws IOException {
        finish();
        long originalMdatOffset = mdatAtom.getOffset();
        CompositeAtom originalMoovAtom = moovAtom;
        mdatOffset = 0;

        ImageOutputStream originalOut = out;
        try {
            out = null;

            if (compressHeader) {
                ByteArrayImageOutputStream buf = new ByteArrayImageOutputStream();
                int maxIteration = 5;
                long compressionHeadersSize = 40 + 8;
                long headerSize = 0;
                long freeSize = 0;
                while (true) {
                    mdatOffset = compressionHeadersSize + headerSize + freeSize;
                    buf.reset();
                    DeflaterOutputStream deflater = new DeflaterOutputStream(new ImageOutputStreamAdapter(buf));
                    out = new MemoryCacheImageOutputStream(deflater);
                    writeEpilog();
                    out.close();
                    deflater.close();

                    if (buf.size() > headerSize + freeSize && --maxIteration > 0) {
                        if (headerSize != 0) {
                            freeSize = Math.max(freeSize, buf.size() - headerSize - freeSize);
                        }
                        headerSize = buf.size();
                    } else {
                        freeSize = headerSize + freeSize - buf.size();
                        headerSize = buf.size();
                        break;
                    }
                }

                if (buf.size() == 0) {
                    compressHeader = false;
                    System.err.println("WARNING MP4Writer failed to compress header.");
                } else {
                    out = new FileImageOutputStream(outputFile);
                    writeProlog();

                    // 40 bytes compression headers
                    QTFFImageOutputStream daos = new QTFFImageOutputStream(out);
                    daos.writeUInt(headerSize + 40);
                    daos.writeType("moov");

                    daos.writeUInt(headerSize + 32);
                    daos.writeType("cmov");

                    daos.writeUInt(12);
                    daos.writeType("dcom");
                    daos.writeType("zlib");

                    daos.writeUInt(headerSize + 12);
                    daos.writeType("cmvd");
                    daos.writeUInt(originalMoovAtom.size());

                    daos.write(buf.getBuffer(), 0, buf.size());

                    // 8 bytes "free" atom + free data
                    daos.writeUInt(freeSize + 8);
                    daos.writeType("free");
                    for (int i = 0; i < freeSize; i++) {
                        daos.write(0);
                    }
                }
            }
            if (!compressHeader) {
                out = new FileImageOutputStream(outputFile);
                mdatOffset = moovAtom.size();
                writeProlog();
                writeEpilog();
            }


            byte[] buf = new byte[4096];
            originalOut.seek((originalMdatOffset));
            for (long count = 0, n = mdatAtom.size(); count < n; ) {
                int read = originalOut.read(buf, 0, (int) Math.min(buf.length, n - count));
                out.write(buf, 0, read);
                count += read;
            }
            out.close();
        } finally {
            mdatOffset = 0;
            moovAtom = originalMoovAtom;
            out = originalOut;
        }
    }

    protected void writeVideoSampleDescriptionAtom(VideoTrack t, CompositeAtom stblAtom) throws IOException {
        CompositeAtom leaf;
        QTFFImageOutputStream d;

        /* Sample Description box ------- */
        leaf = new CompositeAtom("stsd", out);
        stblAtom.add(leaf);
            /*
            typedef struct {
                byte version;
                byte[3] flags;
                int numberOfEntries;
                SampleDescriptionEntry table[numberOfEntries];
            } SampleDescriptionBox;

             typedef struct {
                int size;
                magic type;
                byte[6] reserved; // six bytes that must be zero
                short dataReferenceIndex; // A 16-bit integer that contains the index of the data reference to
                                         // use to retrieve data associated with samples that use this sample description.
                                         // Data references are stored in data reference atoms.
                ubyte[size - 16] data;
            } SampleDescriptionEntry;

            typedef struct {
                uint16 predefined; // always 0
                uint16 reserved; // always 0
                magic vendor; // A 32-bit integer that specifies the developer of the
                          // compressor that generated the compressed data. Often
                          //  this field contains 'appl' to indicate Apple
                            // Computer, Inc.
                uint temporalQuality;// A 32-bit integer containing a value from 0 to 1023
                                    // indicating the degree of temporal compression.
                uint spatialQuality;// A 32-bit integer containing a value from 0 to 1024
                                    // indicating the degree of spatial compression.
                uint16 width;
                uint16 height;
                fixed16d16 horizontalResolution;
                fixed16d16 verticalResolution;
                uint32 reserved;  // always 0
                uint16 frameCount;//how many frames of compressed data are stored in each sample.
                                  // Usually set to 1.
                pstring32 compressorname;
                uint8 reserved; // always 0
                uint8 depth;
                int16 predefined3; // should always be -1
                uint8 undocumentedByte[predefined3!=-1];
                SampleDescriptionExtension[] extendedData;
            } VisualSampleEntry;
             */
        d = leaf.getOutputStream();
        d.write(0); // version
        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(0); // flag[2]
        d.writeInt(1); // number of Entries

        // Sample Description Entry
        // ------------------------
        long sizeStreamPosition = d.getStreamPosition();
        d.writeInt(0); // sampleDescriptionTable[0].size
        d.writeType(t.mediaCompressionType); // sampleDescriptionTable[0].type
        d.write(new byte[6]); // sampleDescriptionTable[0].reserved
        d.writeShort(1); // sampleDescriptionTable[0].dataReferenceIndex

        // Visual Sample Entry
        // ------------------------------

        d.writeShort(0); // predefined
        d.writeShort(0); // reserved
        d.writeType(t.componentManufacturer);//vendor
        d.writeInt(0); // temporalQuality
        d.writeInt(MathUtil.clamp((int) (1024 * (1 - t.videoQuality)), 0, 1024)); // spatialQuality
        d.writeUShort((int) t.width); // width
        d.writeUShort((int) t.height); // height
        d.writeFixed16D16(72.0); // horizontalResolution
        d.writeFixed16D16(72.0); // verticalResolution
        d.writeInt(0);//reserved
        d.writeShort(1); // frameCount
        d.writePString(t.mediaCompressorName, 32);
        d.write(0); // reserved
        d.write(t.videoDepth); // depth
        d.writeShort(-1); // predefined3

        if (t.avcDecoderConfigurationRecord != null) {
            writeMandatoryAvcAtoms(t, leaf);
        }

        long size = (d.getStreamPosition() - sizeStreamPosition);
        d.mark();
        d.seek(sizeStreamPosition);
        d.writeInt((int) size);
        d.reset();
    }

    /**
     * Writes the avcC atom.
     *
     * @param t      the track
     * @param parent the composite atom
     * @throws IOException on IO failure
     */
    private void writeMandatoryAvcAtoms(VideoTrack t, CompositeAtom parent) throws IOException {
        DataAtom leaf = new DataAtom("avcC", out);
        parent.add(leaf);
        /*
        typedef struct {
            ubyte configurationVersion; // always = 1
            ubyte AVCProfileIndication; // Contains the profile code as defined in ISO/IEC 14496-10.
                                        // profile_compatibility is a byte defined exactly the same as the byte which occurs
                                        // between the profile_IDC and level_IDC in a sequence parameter set (SPS),
                                        // as defined in ISO/IEC 14496-10
            ubyte profile_compatibility; //
            ubyte AVCLevelIndication; // Contains the level code as defined in ISO/IEC 14496-10.
            uint6 reserved1; // always 111111
            uint2 lengthSizeMinusOne; // Indicates the length in bytes of the NALUnitLength field in an AVC
                                      // video sample or AVC parameter set sample of the associated stream minus one.
                                      // For example, a size of one byte is indicated with a value of 0. The value of this field
                                       // shall be one of 0, 1, or 3 corresponding to a length encoded with 1, 2, or 4 bytes,
                                       // respectively.
            uint3 reserved2; // always 111
            uint5 numOfSequenceParameterSets; // numOfSequenceParameterSets indicates the number of SPSs that are used as the
                                              // initial set of SPSs for decoding the AVC elementary stream.
            AvcSequenceParameterSet[numOfSequenceParameterSets] sequenceParameterSet;
            uint8 numOfPictureParameterSets; // Indicates the number of picture parameter sets (PPSs) that are used as the
                                             // initial set of PPSs for decoding the AVC elementary stream.
            AvcPictureParameterSet[numOfPictureParameterSets] pictureParameterSet
        } AvcDecoderConfigurationRecord;
         */
        var d = leaf.getOutputStream();
        AvcDecoderConfigurationRecord r = t.avcDecoderConfigurationRecord;
        d.writeByte(1);//version
        d.writeByte(r.avcProfileIndication());
        d.writeByte(r.profileCompatibility());
        d.writeByte(r.avcLevelIndication());
        d.writeByte(0b111111_00 | (r.nalLengthSize() - 1));

        Set<ByteArray> spsList = r.sequenceParameterSetNALUnit();
        int n = Math.min(spsList.size(), (1 << 5) - 1);
        d.writeByte(0b111_00000 | n);
        Iterator<ByteArray> it = spsList.iterator();
        for (int i = 0; i < n; i++) {
            byte[] sps = it.next().getArray();
            d.writeShort((short) (sps.length + 1));
            d.writeByte((byte) 0x67);
            d.write(sps);
        }

        Set<ByteArray> ppsList = r.pictureParameterSetNALUnit();
        n = Math.min(ppsList.size(), (1 << 8) - 1);
        d.writeByte(n);
        it = ppsList.iterator();
        for (int i = 0; i < n; i++) {
            byte[] pps = it.next().getArray();
            d.writeShort((short) (pps.length + 1));
            d.writeByte((byte) 0x68);
            d.write(pps);
        }

        /* colr atom */
        /*---------*/
        /*
        typedef struct {
            magic colorParameterType; // An unsigned 32-bit field.
                                     // The currently defined types are 'nclc' for video, and 'prof' for print.
            uint16 primariesIndex; // A 16-bit unsigned integer containing an index into a table specifying the
                                   // CIE 1931 xy chromaticity coordinates of the white point and the red, green,
                                   // and blue primaries.
                                   //  Index 1
                                   //      Recommendation ITU-R BT.709 white x = 0.3127 y = 0.3290 (CIE III. D65) red x = 0.640 y = 0.330 green x = 0.300 y = 0.600 blue x = 0.150 y = 0.060

            uint16 transferFunctionIndex; // A 16-bit unsigned integer containing an index into a table specifying the
                                    // nonlinear transfer function coefficients used to translate between RGB color space
                                    // values and Y´CbCr values.
                                    //  Index 1
                                    //      Recommendation ITU-R BT.709-2, SMPTE 274M-1995, 296M-1997, 293M-1996, 170M-1994 An image that shows two formulas for transfer functions for index 1. The first formula is E’ with subscript W is equal to four point five zero zero for zero is less than or equal to W is less than zero point zero one eight. The second formula is E’ with subscript W is equal to one point zero nine nine W raised to the power zero point four five, minus zero point zero nine nine for zero point zero one eight is less than or equal to W is less than or equal to one.

            uint16 matrixIndex; // A 16-bit unsigned integer containing an index into a table specifying the
                                // transformation matrix coefficients used to translate between RGB color space values
                                 // and Y´CbCr values.
                                 //  Index 1
                                 //      Recommendation ITU-R BT.709-2 (1125/60/2:1 only), SMPTE 274M-1995, 296M-1997 An image that shows the formula for matrix index 1. The formula is E’ with subscript Y is equal to zero point seven one five two E’ with subscript G, plus zero point zero seven two two E’ with subscript B, plus zero point two one two six E’ with subscript R.
                                 // https://developer.apple.com/documentation/quicktime-file-format/color_parameter_atom

        } videoColrSampleDescriptionExtensionAtom;
        */
        /*
        leaf = new DataAtom("colr");
        parent.add(leaf);
        d = leaf.getOutputStream();
        d.writeType("nclc");
        d.writeUShort(1);
        d.writeUShort(1);
        d.writeUShort(1);
         */

        /* pasp atom */
        /*---------*/
        /*
        typedef struct {
            uint32 hSpacing; // An unsigned 32-bit integer specifying the horizontal spacing of pixels,
                     // such as luma sampling instants for Y´CbCr or YUV video.
            uint32 vSpacing; // An unsigned 32-bit integer specifying the vertical spacing of pixels,
                     // such as video picture lines.
        } videoPaspSampleDescriptionExtensionAtom;
        https://developer.apple.com/documentation/quicktime-file-format/pixel_aspect_ratio
         */
        Rational pixelAspectRatio = t.format.get(VideoFormatKeys.PixelAspectRatioKey, Rational.ONE);
        if (!pixelAspectRatio.equals(Rational.ONE)) {
            leaf = new DataAtom("pasp", out);
            parent.add(leaf);
            d = leaf.getOutputStream();
            d.writeUInt(pixelAspectRatio.getNumerator());
            d.writeUInt(pixelAspectRatio.getDenominator());
        }
    }

    /**
     * Color table atoms define a list of preferred colors for displaying
     * the movie on devices that support only 256 colors. The list may
     * contain up to 256 colors. These optional atoms have a type value of
     * 'ctab'. The color table atom contains a Macintosh color table data
     * structure.
     *
     * @param stblAtom
     * @throws IOException
     */
    protected void writeVideoColorTableAtom(VideoTrack t, CompositeAtom stblAtom) throws IOException {
        DataAtom leaf;
        QTFFImageOutputStream d;
        leaf = new DataAtom("ctab", out);
        stblAtom.add(leaf);

        d = leaf.getOutputStream();

        d.writeUInt(0); // Color table seed. A 32-bit integer that must be set to 0.
        d.writeUShort(0x8000); // Color table flags. A 16-bit integer that must be set to 0x8000.
        IndexColorModel videoColorTable = t.videoColorTable;
        d.writeUShort(videoColorTable.getMapSize() - 1);
        // Color table size. A 16-bit integer that indicates the number of
        // colors in the following color array. This is a zero-relative value;
        // setting this field to 0 means that there is one color in the array.

        for (int i = 0, n = videoColorTable.getMapSize(); i < n; ++i) {
            // An array of colors. Each color is made of four unsigned 16-bit integers.
            // The first integer must be set to 0, the second is the red value,
            // the third is the green value, and the fourth is the blue value.
            d.writeUShort(0);
            d.writeUShort((videoColorTable.getRed(i) << 8) | videoColorTable.getRed(i));
            d.writeUShort((videoColorTable.getGreen(i) << 8) | videoColorTable.getGreen(i));
            d.writeUShort((videoColorTable.getBlue(i) << 8) | videoColorTable.getBlue(i));
        }
    }

    protected void writeAudioSampleDescriptionAtom(AudioTrack t, CompositeAtom stblAtom) throws IOException {
        // TO DO
        DataAtom leaf;
        QTFFImageOutputStream d;

        /* Sample Description atom ------- */
        // The sample description atom stores information that allows you to
        // decode samples in the media. The data stored in the sample
        // description varies, depending on the media type. For example, in the
        // case of video media, the sample descriptions are image description
        // structures. The sample description information for each media type is
        // explained in “Media Data Atom Types”:
        // http://developer.apple.com/documentation/QuickTime/QTFF/QTFFChap3/chapter_4_section_1.html#//apple_ref/doc/uid/TP40000939-CH205-SW1
        leaf = new DataAtom("stsd", out);
        stblAtom.add(leaf);
            /*
             typedef struct {
             byte version;
             byte[3] flags;
             int numberOfEntries;
             soundSampleDescriptionEntry sampleDescriptionTable[numberOfEntries];
             } soundSampleDescriptionAtom;

             typedef struct {
             int size;
             magic type;
             byte[6] reserved;
             short dataReferenceIndex;
             soundSampleDescription data;
             } soundSampleDescriptionEntry;

             typedef struct {
             ushort version;
             ushort revisionLevel;
             uint vendor;
             ushort numberOfChannels;
             ushort sampleSize;
             short compressionId;
             ushort packetSize;
             fixed16d16 sampleRate;
             byte[] extendedData;
             } soundSampleDescription;
             */
        d = leaf.getOutputStream();

        // soundSampleDescriptionAtom:
        // ---------------------------
        d.write(0); // version
        // A 1-byte specification of the version of this sample description atom.

        d.write(0); // flag[0]
        d.write(0); // flag[1]
        d.write(0); // flag[2]
        // A 3-byte space for sample description flags. Set this field to 0.

        d.writeInt(1); // number of Entries
        // A 32-bit integer containing the number of sample descriptions that follow.

        // soundSampleDescriptionEntry:
        // ----------------------------
        // A 32-bit integer indicating the number of bytes in the sample description.
        d.writeUInt(4 + 12 + 20 + 16 + t.stsdExtensions.length); // sampleDescriptionTable[0].size

        // Common header: 12 bytes
        d.writeType(t.mediaCompressionType); // sampleDescriptionTable[0].type
        // A 32-bit integer indicating the format of the stored data.
        // This depends on the media type, but is usually either the
        // compression format or the media type.

        d.write(new byte[6]); // sampleDescriptionTable[0].reserved
        // Six bytes that must be set to 0.

        d.writeUShort(1); // sampleDescriptionTable[0].dataReferenceIndex
        // A 16-bit integer that contains the index of the data
        // reference to use to retrieve data associated with samples
        // that use this sample description. Data references are stored
        // in data reference atoms.

        // Sound Sample Description (Version 0) 20 bytes
        // ------------------------

        d.writeUShort(1); // version
        // A 16-bit integer that holds the sample description version (currently 0 or 1).

        d.writeUShort(0); // revisionLevel
        // A 16-bit integer that must be set to 0.

        d.writeUInt(0); // vendor
        // A 32-bit integer that must be set to 0.

        d.writeUShort(t.soundNumberOfChannels);  // numberOfChannels
        // A 16-bit integer that indicates the number of sound channels used by
        // the sound sample. Set to 1 for monaural sounds, 2 for stereo sounds.
        // Higher numbers of channels are not supported.

        d.writeUShort(t.soundSampleSize); // sampleSize (bits)
        // A 16-bit integer that specifies the number of bits in each
        // uncompressed sound sample. Allowable values are 8 or 16. Formats
        // using more than 16 bits per sample set this field to 16 and use sound
        // description version 1.

        d.writeUShort(t.soundCompressionId); // compressionId
        // XXX - This must be set to -1, or the QuickTime player won't accept this file.
        // A 16-bit integer that must be set to 0 for version 0 sound
        // descriptions. This may be set to –2 for some version 1 sound
        // descriptions; see “Redefined Sample Tables” (page 135).

        d.writeUShort(0); // packetSize
        // A 16-bit integer that must be set to 0.

        d.writeFixed16D16(t.soundSampleRate); // sampleRate
        // A 32-bit unsigned fixed-point number (16.16) that indicates the rate
        // at which the sound samples were obtained. The integer portion of this
        // number should match the media’s time scale. Many older version 0
        // files have values of 22254.5454 or 11127.2727, but most files have
        // integer values, such as 44100. Sample rates greater than 2^16 are not
        // supported.

        // Sound Sample Description Additional fields (only in Version 1) 16 bytes
        // ------------------------
        d.writeUInt(t.soundSamplesPerPacket); // samplesPerPacket
        // A 32-bit integer.
        // The number of uncompressed samples generated by a
        // compressed sample (an uncompressed sample is one sample
        // from each channel). This is also the sample duration,
        // expressed in the media’s timescale, where the
        // timescale is equal to the sample rate. For
        // uncompressed formats, this field is always 1.
        //
        d.writeUInt(t.soundBytesPerPacket); // bytesPerPacket
        // A 32-bit integer.
        // For uncompressed audio, the number of bytes in a
        // sample for a single channel. This replaces the older
        // sampleSize field, which is set to 16.
        // This value is calculated by dividing the frame size
        // by the number of channels. The same calculation is
        // performed to calculate the value of this field for
        // compressed audio, but the result of the calculation
        // is not generally meaningful for compressed audio.
        //
        d.writeUInt(t.soundBytesPerFrame); // bytesPerFrame
        // A 32-bit integer.
        // The number of bytes in a sample: for uncompressed
        // audio, an uncompressed frame; for compressed audio, a
        // compressed frame. This can be calculated by
        // multiplying the bytes per packet field by the number
        // of channels.
        //
        d.writeUInt(t.soundBytesPerSample); // bytesPerSample
        // A 32-bit integer.
        // The size of an uncompressed sample in bytes. This is
        // set to 1 for 8-bit audio, 2 for all other cases, even
        // if the sample size is greater than 2 bytes.

        // Write stsd Extensions
        // Extensions must be atom-based fields
        // ------------------------------------
        d.write(t.stsdExtensions);
    }
}

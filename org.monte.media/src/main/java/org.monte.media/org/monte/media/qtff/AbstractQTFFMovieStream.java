/*
 * @(#)AbstractQTFFMovieStream.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.qtff;

import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.math.Rational;

import javax.imageio.stream.ImageOutputStream;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * This is the base class for low-level QuickTime stream IO.
 *
 * @author Werner Randelshofer
 */
public class AbstractQTFFMovieStream {

    public static final String DEFAULT_COMPONENT_NAME = "Monte Media";
    public static final String DEFAULT_COMPONENT_MANUFACTURER = "java";
    /**
     * Underlying output stream.
     */
    protected ImageOutputStream out;
    /**
     * The offset in the underlying ImageOutputStream. Normally this is 0 unless
     * the underlying stream already contained data when it was passed to the
     * constructor.
     */
    protected long streamOffset;
    /**
     * This atom holds the media data.
     */
    protected WideDataAtom mdatAtom;
    /**
     * Offset for the mdat atom.
     */
    protected long mdatOffset;
    /**
     * This atom holds the moovie header.
     */
    protected CompositeAtom moovAtom;
    /**
     * Creation time of the movie.
     */
    protected Instant creationTime;
    /**
     * Modification time of the movie.
     */
    protected Instant modificationTime;
    /**
     * The timeScale of the movie. A time value that indicates the timescale
     * for this media—that is, the number of time units that pass per second in
     * its time coordinate system.
     */
    protected long movieTimeScale = 600;
    /**
     * The preferred rate at which to play this movie. A value of 1.0 indicates
     * normal rate.
     */
    protected double preferredRate = 1d;
    /**
     * The preferred volume of this movie’s sound. A value of 1.0 indicates full
     * volume.
     */
    protected double preferredVolume = 1d;
    /**
     * The time value in the movie at which the preview begins.
     */
    protected long previewTime = 0;
    /**
     * The duration of the movie preview in movie timescale units.
     */
    protected long previewDuration = 0;
    /**
     * The time value of the time of the movie poster.
     */
    protected long posterTime = 0;
    /**
     * The time value for the start time of the current selection.
     */
    protected long selectionTime = 0;
    /**
     * The duration of the current selection in movie timescale units.
     */
    protected long selectionDuration = 0;
    /**
     * The time value for current time position within the movie.
     */
    protected long currentTime = 0;
    /**
     * The list of tracks in the movie.
     */
    protected ArrayList<Track> tracks = new ArrayList<>();
    /**
     * The transformation matrix for the entire movie.
     */
    protected double[] movieMatrix = {1, 0, 0, 0, 1, 0, 0, 0, 1};
    /**
     * The current state of the movie output stream.
     */
    protected States state = States.REALIZED;

    protected static String intToType(int id) {
        char[] b = new char[4];

        b[0] = (char) ((id >>> 24) & 0xff);
        b[1] = (char) ((id >>> 16) & 0xff);
        b[2] = (char) ((id >>> 8) & 0xff);
        b[3] = (char) (id & 0xff);
        return String.valueOf(b);
    }

    protected static int typeToInt(String str) {
        int value = ((str.charAt(0) & 0xff) << 24) |//
                ((str.charAt(1) & 0xff) << 16) | //
                ((str.charAt(2) & 0xff) << 8) | //
                (str.charAt(3) & 0xff);
        return value;
    }

    /**
     * Gets the position relative to the beginning of the QuickTime stream. <p>
     * Usually this value is equal to the stream position of the underlying
     * ImageOutputStream, but can be larger if the underlying stream already
     * contained data.
     *
     * @return The relative stream position.
     * @throws IOException
     */
    protected long getRelativeStreamPosition() throws IOException {
        return out.getStreamPosition() - streamOffset;
    }

    public boolean isTrackEnabled(int track) {
        return tracks.get(track).isEnabled();
    }

    public boolean isTrackInMovie(int track) {
        return tracks.get(track).isInMovie();
    }

    public boolean isTrackInPoster(int track) {
        return tracks.get(track).isInPoster();
    }

    public boolean isTrackInPreview(int track) {
        return tracks.get(track).isInPreview();
    }

    /**
     * Seeks relative to the beginning of the QuickTime stream. <p> Usually this
     * equal to seeking in the underlying ImageOutputStream, but can be
     * different if the underlying stream already contained data.
     */
    protected void seekRelative(long newPosition) throws IOException {
        out.seek(newPosition + streamOffset);
    }

    public void setTrackEnabled(int track, boolean newValue) {
        tracks.get(track).setEnabled(newValue);
    }

    public void setTrackInMovie(int track, boolean newValue) {
        tracks.get(track).setInMovie(newValue);
    }

    public void setTrackInPoster(int track, boolean newValue) {
        tracks.get(track).setInPoster(newValue);
    }

    public void setTrackInPreview(int track, boolean newValue) {
        tracks.get(track).setInPreview(newValue);
    }

    /**
     * The states of the movie output stream.
     */
    protected static enum States {

        REALIZED, STARTED, FINISHED, CLOSED;
    }

    /**
     * Groups consecutive samples with same characteristics.
     */
    protected abstract static class Group {

        protected final static long maxSampleCount = Integer.MAX_VALUE;
        public long sampleCount;
        protected Sample firstSample;
        protected Sample lastSample;

        protected Group(Sample firstSample) {
            this.firstSample = this.lastSample = firstSample;
            sampleCount = 1;
        }

        protected Group(Sample firstSample, Sample lastSample, long sampleCount) {
            this.firstSample = firstSample;
            this.lastSample = lastSample;
            this.sampleCount = sampleCount;
            if (sampleCount > maxSampleCount) {
                throw new IllegalArgumentException("Capacity exceeded");
            }
        }

        protected Group(Group group) {
            this.firstSample = group.firstSample;
            this.lastSample = group.lastSample;
            sampleCount = group.sampleCount;
        }

        public long getSampleCount() {
            return sampleCount;
        }

        /**
         * Returns true, if the chunk was added to the group. If false is
         * returned, the chunk must be added to a new group. <p> A chunk can
         * only be added to a group, if the capacity of the group is not
         * exceeded.
         */
        protected boolean maybeAddChunk(Chunk chunk) {
            if (sampleCount + chunk.sampleCount <= maxSampleCount) {
                lastSample = chunk.lastSample;
                sampleCount += chunk.sampleCount;
                return true;
            }
            return false;
        }

        /**
         * Returns true, if the samples was added to the group. If false is
         * returned, the sample must be added to a new group. <p> A sample can
         * only be added to a group, if the capacity of the group is not
         * exceeded.
         */
        protected boolean maybeAddSample(Sample sample) {
            if (sampleCount < maxSampleCount) {
                lastSample = sample;
                sampleCount++;
                return true;
            }
            return false;
        }
    }

    /**
     * QuickTime stores media data in samples. A sample is a single element in a
     * sequence of time-ordered data. Samples are stored in the mdat atom.
     */
    protected static class Sample {

        /**
         * Offset of the sample relative to the start of the QuickTime file.
         */
        long offset;
        /**
         * Data length of the sample.
         */
        long length;
        /**
         * The duration of the sample in media timescale units.
         */
        long duration;

        /**
         * Creates a new sample.
         *
         * @param duration
         * @param offset
         * @param length
         */
        public Sample(long duration, long offset, long length) {
            this.duration = duration;
            this.offset = offset;
            this.length = length;
        }
    }

    /**
     * Groups consecutive smples of the same duration.
     */
    protected static class TimeToSampleGroup extends Group {

        public TimeToSampleGroup(Sample firstSample) {
            super(firstSample);
        }

        public TimeToSampleGroup(Group group) {
            super(group);
        }

        /**
         * Returns the duration that all samples in this group share.
         */
        public long getSampleDuration() {
            return firstSample.duration;
        }

        @Override
        public boolean maybeAddChunk(Chunk chunk) {
            if (firstSample.duration == chunk.firstSample.duration) {
                return super.maybeAddChunk(chunk);
            }
            return false;
        }

        /**
         * Returns true, if the sample was added to the group. If false is
         * returned, the sample must be added to a new group. <p> A sample can
         * only be added to a TimeToSampleGroup, if it has the same duration as
         * previously added samples, and if the capacity of the group is not
         * exceeded.
         */
        @Override
        public boolean maybeAddSample(Sample sample) {
            if (firstSample.duration == sample.duration) {
                return super.maybeAddSample(sample);
            }
            return false;
        }
    }

    /**
     * Groups consecutive samples of the same size.
     */
    protected static class SampleSizeGroup extends Group {

        public SampleSizeGroup(Sample firstSample) {
            super(firstSample);
        }

        public SampleSizeGroup(Group group) {
            super(group);
        }

        /**
         * Returns the length that all samples in this group share.
         */
        public long getSampleLength() {
            return firstSample.length;
        }

        @Override
        public boolean maybeAddChunk(Chunk chunk) {
            if (firstSample.length == chunk.firstSample.length) {
                return super.maybeAddChunk(chunk);
            }
            return false;
        }

        /**
         * Returns true, if the sample was added to the group. If false is
         * returned, the sample must be added to a new group. <p> A sample can
         * only be added to a SampleSizeGroup, if it has the same size as
         * previously added samples, and if the capacity of the group is not
         * exceeded.
         */
        @Override
        public boolean maybeAddSample(Sample sample) {
            if (firstSample.length == sample.length) {
                return super.maybeAddSample(sample);
            }
            return false;
        }
    }

    /**
     * Groups consecutive samples with the same sample description Id and with
     * adjacent offsets in the movie file.
     */
    protected static class Chunk extends Group {

        public int sampleDescriptionId;

        /**
         * Creates a new Chunk.
         *
         * @param firstSample         The first sample contained in this chunk.
         * @param sampleDescriptionId The description Id of the sample.
         */
        public Chunk(Sample firstSample, int sampleDescriptionId) {
            super(firstSample);
            this.sampleDescriptionId = sampleDescriptionId;
        }

        /**
         * Creates a new Chunk.
         *
         * @param firstSample         The first sample contained in this chunk.
         * @param sampleDescriptionId The description Id of the sample.
         */
        public Chunk(Sample firstSample, Sample lastSample, int sampleCount, int sampleDescriptionId) {
            super(firstSample, lastSample, sampleCount);
            this.sampleDescriptionId = sampleDescriptionId;
        }

        /**
         * Returns the offset of the chunk in the movie file.
         */
        public long getChunkOffset() {
            return firstSample.offset;
        }

        @Override
        public boolean maybeAddChunk(Chunk chunk) {
            if (sampleDescriptionId == chunk.sampleDescriptionId //
                    && lastSample.offset + lastSample.length == chunk.firstSample.offset) {
                return super.maybeAddChunk(chunk);
            }
            return false;
        }

        /**
         * Returns true, if the sample was added to the chunk. If false is
         * returned, the sample must be added to a new chunk. <p> A sample can
         * only be added to a chunk, if it has the same sample description Id as
         * previously added samples, if the capacity of the chunk is not
         * exceeded and if the sample offset is adjacent to the last sample in
         * this chunk.
         */
        public boolean maybeAddSample(Sample sample, int sampleDescriptionId) {
            if (sampleDescriptionId == this.sampleDescriptionId
                    && lastSample.offset + lastSample.length == sample.offset) {
                return super.maybeAddSample(sample);
            }
            return false;
        }
    }

    /**
     * An {@code Edit} define the portions of the media that are to be used to
     * build up a track for a movie. The edits themselves are stored in an edit
     * list table, which consists of time offset and duration values for each
     * segment. <p> In the absence of an edit list, the presentation of the
     * track starts immediately. An empty edit is used to offset the start time
     * of a track.
     */
    public static class Edit {

        /**
         * A 32-bit fixed-point number (16.16) that specifies the relative rate
         * at which to play the media corresponding to this edit segment. This
         * rate value cannot be 0 or negative.
         */
        public int mediaRate;
        /**
         * A 32-bit integer containing the start time within the media of this
         * edit segment (in media timescale units). If this field is set to -1,
         * it is an empty edit. The last edit in a track should never be an
         * empty edit. Any differece between the movie's duration and the
         * track's duration is expressed as an implicit empty edit.
         */
        public int mediaTime;
        /**
         * A 32-bit integer that specifies the duration of this edit segment in
         * units of the movie's timescale.
         */
        public int trackDuration;

        /**
         * Creates an edit.
         *
         * @param trackDuration Duration of this edit in the movie's timescale.
         * @param mediaTime     Start time of this edit in the media's timescale.
         *                      Specify -1 for an empty edit. The last edit in a track should never
         *                      be an empty edit.
         * @param mediaRate     The relative rate at which to play this edit.
         */
        public Edit(int trackDuration, int mediaTime, double mediaRate) {
            if (trackDuration < 0) {
                throw new IllegalArgumentException("trackDuration must not be < 0:" + trackDuration);
            }
            if (mediaTime < -1) {
                throw new IllegalArgumentException("mediaTime must not be < -1:" + mediaTime);
            }
            if (mediaRate <= 0) {
                throw new IllegalArgumentException("mediaRate must not be <= 0:" + mediaRate);
            }
            this.trackDuration = trackDuration;
            this.mediaTime = mediaTime;
            this.mediaRate = (int) (mediaRate * (1 << 16));
        }

        /**
         * Creates an edit. <p> Use this constructor only if you want to compute
         * the fixed point media rate by yourself.
         *
         * @param trackDuration Duration of this edit in the movie's timescale.
         * @param mediaTime     Start time of this edit in the media's timescale.
         *                      Specify -1 for an empty edit. The last edit in a track should never
         *                      be an empty edit.
         * @param mediaRate     The relative rate at which to play this edit given
         *                      as a 16.16 fixed point value.
         */
        public Edit(int trackDuration, int mediaTime, int mediaRate) {
            if (trackDuration < 0) {
                throw new IllegalArgumentException("trackDuration must not be < 0:" + trackDuration);
            }
            if (mediaTime < -1) {
                throw new IllegalArgumentException("mediaTime must not be < -1:" + mediaTime);
            }
            if (mediaRate <= 0) {
                throw new IllegalArgumentException("mediaRate must not be <= 0:" + mediaRate);
            }
            this.trackDuration = trackDuration;
            this.mediaTime = mediaTime;
            this.mediaRate = mediaRate;
        }
    }

    /**
     * Atom base class.
     */
    protected abstract class Atom {

        /**
         * The type of the atom. A String with the length of 4 characters.
         */
        protected String type;
        /**
         * The offset of the atom relative to the start of the
         * ImageOutputStream.
         */
        protected long offset;

        /**
         * Creates a new Atom at the current position of the ImageOutputStream.
         *
         * @param type The type of the atom. A string with a length of 4
         *             characters.
         */
        public Atom(String type, long offset) {
            this.type = type;
            this.offset = offset;
        }

        /**
         * Writes the atom to the ImageOutputStream and disposes it.
         */
        public abstract void finish() throws IOException;

        /**
         * Returns the size of the atom including the size of the atom header.
         *
         * @return The size of the atom.
         */
        public abstract long size();
    }

    /**
     * A CompositeAtom contains an ordered list of Atoms.
     */
    protected class CompositeAtom extends DataAtom {

        protected LinkedList<Atom> children;

        /**
         * Creates a new CompositeAtom at the current position of the
         * ImageOutputStream.
         *
         * @param type The type of the atom.
         */
        public CompositeAtom(String type) throws IOException {
            super(type);
            children = new LinkedList<>();
        }

        public void add(Atom child) throws IOException {
            if (children.size() > 0) {
                children.get(children.size() - 1).finish();
            }
            children.add(child);
        }

        /**
         * Writes the atom and all its children to the ImageOutputStream and
         * disposes of all resources held by the atom.
         *
         * @throws java.io.IOException
         */
        @Override
        public void finish() throws IOException {
            if (!finished) {
                if (size() > 0xffffffffL) {
                    throw new IOException("CompositeAtom \"" + type + "\" is too large: " + size());
                }

                long pointer = getRelativeStreamPosition();
                seekRelative(offset);

                QTFFImageOutputStream headerData = new QTFFImageOutputStream(out);
                headerData.writeInt((int) size());
                headerData.writeType(type);
                for (Atom child : children) {
                    child.finish();
                }
                seekRelative(pointer);
                finished = true;
            }
        }

        @Override
        public long size() {
            long length = 8 + data.length();
            for (Atom child : children) {
                length += child.size();
            }
            return length;
        }
    }

    /**
     * Data Atom.
     */
    protected class DataAtom extends Atom {

        protected QTFFImageOutputStream data;
        protected boolean finished;

        /**
         * Creates a new DataAtom at the current position of the
         * ImageOutputStream.
         *
         * @param type The type name of the atom.
         */
        public DataAtom(String type) throws IOException {
            super(type, getRelativeStreamPosition());
            out.writeLong(0); // make room for the atom header
            data = new QTFFImageOutputStream(out);
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                long sizeBefore = size();

                if (size() > 0xffffffffL) {
                    throw new IOException("DataAtom \"" + type + "\" is too large: " + size());
                }

                long pointer = getRelativeStreamPosition();
                seekRelative(offset);

                QTFFImageOutputStream headerData = new QTFFImageOutputStream(out);
                headerData.writeInt((int) size());
                headerData.writeType(type);
                seekRelative(pointer);
                finished = true;
                long sizeAfter = size();
                if (sizeBefore != sizeAfter) {
                    System.err.println("size mismatch " + sizeBefore + ".." + sizeAfter);
                }
            }
        }

        /**
         * Returns the offset of this atom to the beginning of the random access
         * file
         */
        public long getOffset() {
            return offset;
        }

        public QTFFImageOutputStream getOutputStream() {
            if (finished) {
                throw new IllegalStateException("DataAtom is finished");
            }
            return data;
        }

        @Override
        public long size() {
            return 8 + data.length();
        }
    }

    /**
     * WideDataAtom can grow larger then 4 gigabytes.
     */
    protected class WideDataAtom extends Atom {

        protected QTFFImageOutputStream data;
        protected boolean finished;

        /**
         * Creates a new DataAtom at the current position of the
         * ImageOutputStream.
         *
         * @param type The type of the atom.
         */
        public WideDataAtom(String type) throws IOException {
            super(type, getRelativeStreamPosition());
            out.writeLong(0); // make room for the atom header
            out.writeLong(0); // make room for the atom header
            data = new QTFFImageOutputStream(out) {
                @Override
                public void flush() throws IOException {
                    // DO NOT FLUSH UNDERLYING STREAM!
                }
            };
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                long pointer = getRelativeStreamPosition();
                seekRelative(offset);

                QTFFImageOutputStream headerData = new QTFFImageOutputStream(out);
                long finishedSize = size();
                if (finishedSize <= 0xffffffffL) {
                    headerData.writeInt(8);
                    headerData.writeType("wide");
                    headerData.writeInt((int) (finishedSize - 8));
                    headerData.writeType(type);
                } else {
                    headerData.writeInt(1); // special value for extended size atoms
                    headerData.writeType(type);
                    headerData.writeLong(finishedSize - 8);
                }

                seekRelative(pointer);
                finished = true;
            }
        }

        /**
         * Returns the offset of this atom to the beginning of the random access
         * file
         */
        public long getOffset() {
            return offset;
        }

        public QTFFImageOutputStream getOutputStream() {
            if (finished) {
                throw new IllegalStateException("Atom is finished");
            }
            return data;
        }

        @Override
        public long size() {
            return 16 + data.length();
        }
    }

    /**
     * Represents a track.
     */
    protected abstract class Track {

        // Common metadata
        private final static int TrackEnable = 0x1; // enabled track
        private final static int TrackInMovie = 0x2;// track in playback
        private final static int TrackInPreview = 0x4; // track in preview
        private final static int TrackInPoster = 0x8; // track in posterTrackEnable = 0x1, // enabled track
        /**
         * The media type of the track.
         */
        public final MediaType mediaType;
        /**
         * List of chunks.
         */
        public ArrayList<Chunk> chunks = new ArrayList<>();
        public String componentName = DEFAULT_COMPONENT_NAME;
        public String componentManufacturer = DEFAULT_COMPONENT_MANUFACTURER;
        /**
         * The edit list of the track.
         */
        public Edit[] editList;
        /**
         * The format of the media in the track.
         */
        public Format format;
        /**
         * <pre>
         * // Enumeration for track header flags
         * set {
         * TrackEnable = 0x1, // enabled track
         * TrackInMovie = 0x2, // track in playback
         * TrackInPreview = 0x4, // track in preview
         * TrackInPoster = 0x8 // track in poster
         * } TrackHeaderFlags;
         * </pre>
         */
        public int headerFlags = TrackEnable | TrackInMovie | TrackInPreview | TrackInPoster;
        public double height;
        /**
         * The transformation matrix of the track.
         */
        public double[] matrix = {//
                1, 0, 0,//
                0, 1, 0,//
                0, 0, 1
        };
        /**
         * The compression type of the media.
         */
        public String mediaCompressionType;
        /**
         * The compressor name.
         */
        public String mediaCompressorName;
        /**
         * The duration of the media in this track in media time units.
         */
        public long mediaDuration = 0;
        /**
         * The timeScale of the media in the track. A time value that indicates
         * the timescale for this media. That is, the number of time units that
         * pass per second in its time coordinate system.
         */
        public long mediaTimeScale = 600;
        /**
         * The number of samples in this track.
         */
        public long sampleCount = 0;
        /**
         * List of SampleSize entries.
         */
        public ArrayList<SampleSizeGroup> sampleSizes = new ArrayList<>();
        /**
         * Start time of the track.
         */
        public Rational startTime;
        /**
         * Interval between sync samples (keyframes). 0 = automatic. 1 = write
         * all samples as sync samples. n = sync every n-th sample.
         */
        public int syncInterval;
        /**
         * List of sync samples. This list is null as long as all samples in
         * this track are sync samples.
         */
        public ArrayList<Long> syncSamples = null;
        /**
         * List of TimeToSample entries.
         */
        public ArrayList<TimeToSampleGroup> timeToSamples = new ArrayList<>();
        public double width;

        public Track(MediaType mediaType) {
            this.mediaType = mediaType;
        }

        public void addChunk(Chunk chunk, boolean isSyncSample) {
            mediaDuration += chunk.firstSample.duration * chunk.sampleCount;
            sampleCount += chunk.sampleCount;

            // Keep track of sync samples. If all samples in a track are sync
            // samples, we do not need to create a syncSample list.
            if (isSyncSample) {
                if (syncSamples != null) {
                    for (long i = sampleCount - chunk.sampleCount; i < sampleCount; i++) {
                        syncSamples.add(i + 1);
                    }
                }
            } else {
                if (syncSamples == null) {
                    syncSamples = new ArrayList<>();
                    for (long i = 1; i < sampleCount; i++) {
                        syncSamples.add(i);
                    }
                }
            }

            //
            if (timeToSamples.isEmpty()//
                    || !timeToSamples.get(timeToSamples.size() - 1).maybeAddChunk(chunk)) {
                timeToSamples.add(new TimeToSampleGroup(chunk));
            }
            if (sampleSizes.isEmpty()//
                    || !sampleSizes.get(sampleSizes.size() - 1).maybeAddChunk(chunk)) {
                sampleSizes.add(new SampleSizeGroup(chunk));
            }
            if (chunks.isEmpty()//
                    || !chunks.get(chunks.size() - 1).maybeAddChunk(chunk)) {
                chunks.add(chunk);
            }
        }

        public void addSample(Sample sample, int sampleDescriptionId, boolean isSyncSample) {
            mediaDuration += sample.duration;
            sampleCount++;

            // Keep track of sync samples. If all samples in a track are sync
            // samples, we do not need to create a syncSample list.
            if (isSyncSample) {
                if (syncSamples != null) {
                    syncSamples.add(sampleCount);
                }
            } else {
                if (syncSamples == null) {
                    syncSamples = new ArrayList<>();
                    for (long i = 1; i < sampleCount; i++) {
                        syncSamples.add(i);
                    }
                }
            }

            //
            if (timeToSamples.isEmpty()//
                    || !timeToSamples.get(timeToSamples.size() - 1).maybeAddSample(sample)) {
                timeToSamples.add(new TimeToSampleGroup(sample));
            }
            if (sampleSizes.isEmpty()//
                    || !sampleSizes.get(sampleSizes.size() - 1).maybeAddSample(sample)) {
                sampleSizes.add(new SampleSizeGroup(sample));
            }
            if (chunks.isEmpty()//
                    || !chunks.get(chunks.size() - 1).maybeAddSample(sample, sampleDescriptionId)) {
                chunks.add(new Chunk(sample, sampleDescriptionId));
            }
        }

        /**
         * Gets the time of the first sample in the movie timescale.
         *
         * @param movieTimeScale The timescale of the movie.
         */
        public int getFirstSampleTime(long movieTimeScale) {
            return startTime == null ? 0 : startTime.multiply(movieTimeScale).intValue();
        }

        public long getSampleCount() {
            return sampleCount;
        }

        /**
         * Gets the track duration in the movie timescale.
         *
         * @param movieTimeScale The timescale of the movie.
         */
        public long getTrackDuration(long movieTimeScale) {
            if (editList == null || editList.length == 0) {
                return mediaDuration * movieTimeScale / mediaTimeScale;
            } else {
                long duration = 0;
                for (int i = 0; i < editList.length; ++i) {
                    duration += editList[i].trackDuration;
                }
                return duration;
            }
        }

        public boolean isEmpty() {
            return sampleCount == 0;
        }

        public boolean isEnabled() {
            return (headerFlags & TrackEnable) != 0;
        }

        public void setEnabled(boolean newValue) {
            headerFlags = (newValue) ? headerFlags | TrackEnable : headerFlags & (0xff ^ TrackEnable);
        }

        public boolean isInMovie() {
            return (headerFlags & TrackInPreview) != 0;
        }

        public void setInMovie(boolean newValue) {
            headerFlags = (newValue) ? headerFlags | TrackInMovie : headerFlags & (0xff ^ TrackInMovie);
        }

        public boolean isInPoster() {
            return (headerFlags & TrackInPoster) != 0;
        }

        public void setInPoster(boolean newValue) {
            headerFlags = (newValue) ? headerFlags | TrackInPoster : headerFlags & (0xff ^ TrackInPoster);
        }

        public boolean isInPreview() {
            return (headerFlags & TrackInPreview) != 0;
        }

        public void setInPreview(boolean newValue) {
            headerFlags = (newValue) ? headerFlags | TrackInPreview : headerFlags & (0xff ^ TrackInPreview);
        }
    }

    protected class VideoTrack extends Track {
        // Video metadata

        /**
         * AVC decoder configuration record.
         */
        public AvcDecoderConfigurationRecord avcDecoderConfigurationRecord;
        /**
         * The color table used for rendering the video. This variable is only
         * used when the video uses an index color model.
         */
        public IndexColorModel videoColorTable;
        /**
         * Number of bits per ixel. All frames must have the same depth. The
         * value -1 is used to mark unspecified depth.
         */
        public int videoDepth = -1;
        /**
         * The video compression quality.
         */
        public float videoQuality = 0.97f;

        public VideoTrack() {
            super(MediaType.VIDEO);
        }

    }

    protected class AudioTrack extends Track {
        // Audio metadata

        /**
         * The number of bytes in a frame: for uncompressed audio, an
         * uncompressed frame; for compressed audio, a compressed frame. This
         * can be calculated by multiplying the bytes per packet field by the
         * number of channels.
         */
        public long soundBytesPerFrame;
        /**
         * For uncompressed audio, the number of bytes in a sample for a single
         * channel. This replaces the older sampleSize field, which is set to
         * 16. This value is calculated by dividing the frame size by the number
         * of channels. The same calculation is performed to calculate the value
         * of this field for compressed audio, but the result of the calculation
         * is not generally meaningful for compressed audio.
         */
        public long soundBytesPerPacket;
        /**
         * The size of an uncompressed sample in bytes. This is set to 1 for
         * 8-bit audio, 2 for all other cases, even if the sample size is
         * greater than 2 bytes.
         */
        public long soundBytesPerSample;
        /**
         * Sound compressionId. The value -1 means fixed bit rate, -2 means
         * variable bit rate.
         */
        public int soundCompressionId;
        /**
         * Number of sound channels used by the sound sample.
         */
        public int soundNumberOfChannels;
        /**
         * Sound sample rate. The integer portion must match the media's time
         * scale.
         */
        public double soundSampleRate;
        /**
         * Number of bits per audio sample before compression.
         */
        public int soundSampleSize;
        /**
         * Sound stsd samples per packet. The number of uncompressed samples
         * generated by a compressed sample (an uncompressed sample is one
         * sample from each channel). This is also the sample duration,
         * expressed in the media’s timescale, where the timescale is equal to
         * the sample rate. For uncompressed formats, this field is always 1.
         */
        public long soundSamplesPerPacket;
        /**
         * Extensions to the stsd chunk. Must contain atom-based fields: ([long
         * size, long type, some data], repeat)
         */
        public byte[] stsdExtensions = new byte[0];

        public AudioTrack() {
            super(MediaType.AUDIO);
        }


    }
}

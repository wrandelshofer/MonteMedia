/*
 * @(#)AbstractQuickTimeStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime;

import org.monte.media.av.Buffer;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.io.ImageOutputStreamAdapter;
import org.monte.media.math.Rational;

import javax.imageio.stream.ImageOutputStream;
import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * This is the base class for low-level QuickTime stream IO.
 *
 * <p>FIXME - Separation between AbstractQuickTimeStream and
 * QuickTimeOutputStream is not clean. Move write methods in the track classes
 * down to QuickTimeOutputStream.</p>
 *
 * @author Werner Randelshofer
 */
public class AbstractQuickTimeStream {

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
    protected Date creationTime;
    /**
     * Modification time of the movie.
     */
    protected Date modificationTime;
    /**
     * The timeScale of the movie. A time value that indicates the time scale
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
     * The duration of the movie preview in movie time scale units.
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
     * The duration of the current selection in movie time scale units.
     */
    protected long selectionDuration = 0;
    /**
     * The time value for current time position within the movie.
     */
    protected long currentTime = 0;
    /**
     * The list of tracks in the movie.
     */
    protected ArrayList<Track> tracks = new ArrayList<Track>();
    /**
     * The transformation matrix for the entire movie.
     */
    protected double[] movieMatrix = {1, 0, 0, 0, 1, 0, 0, 0, 1};

    /**
     * The states of the movie output stream.
     */
    protected static enum States {

        REALIZED, STARTED, FINISHED, CLOSED;
    }

    /**
     * The current state of the movie output stream.
     */
    protected States state = States.REALIZED;

    public void setTrackEnabled(int track, boolean newValue) {
        tracks.get(track).setEnabled(newValue);
    }

    public boolean isTrackEnabled(int track) {
        return tracks.get(track).isEnabled();
    }

    public void setTrackInMovie(int track, boolean newValue) {
        tracks.get(track).setInMovie(newValue);
    }

    public boolean isTrackInMovie(int track) {
        return tracks.get(track).isInMovie();
    }

    public void setTrackInPreview(int track, boolean newValue) {
        tracks.get(track).setInPreview(newValue);
    }

    public boolean isTrackInPreview(int track) {
        return tracks.get(track).isInPreview();
    }

    public void setTrackInPoster(int track, boolean newValue) {
        tracks.get(track).setInPoster(newValue);
    }

    public boolean isTrackInPoster(int track) {
        return tracks.get(track).isInPoster();
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

    /**
     * Seeks relative to the beginning of the QuickTime stream. <p> Usually this
     * equal to seeking in the underlying ImageOutputStream, but can be
     * different if the underlying stream already contained data.
     */
    protected void seekRelative(long newPosition) throws IOException {
        out.seek(newPosition + streamOffset);
    }

    protected static int typeToInt(String str) {
        int value = ((str.charAt(0) & 0xff) << 24) |//
                ((str.charAt(1) & 0xff) << 16) | //
                ((str.charAt(2) & 0xff) << 8) | //
                (str.charAt(3) & 0xff);
        return value;
    }

    protected static String intToType(int id) {
        char[] b = new char[4];

        b[0] = (char) ((id >>> 24) & 0xff);
        b[1] = (char) ((id >>> 16) & 0xff);
        b[2] = (char) ((id >>> 8) & 0xff);
        b[3] = (char) (id & 0xff);
        return String.valueOf(b);
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
            children = new LinkedList<Atom>();
        }

        public void add(Atom child) throws IOException {
            if (children.size() > 0) {
                children.getLast().finish();
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

                DataAtomOutputStream headerData = new DataAtomOutputStream(new ImageOutputStreamAdapter(out));
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
            long length = 8 + data.size();
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

        protected DataAtomOutputStream data;
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
            data = new DataAtomOutputStream(new ImageOutputStreamAdapter(out));
        }

        public DataAtomOutputStream getOutputStream() {
            if (finished) {
                throw new IllegalStateException("DataAtom is finished");
            }
            return data;
        }

        /**
         * Returns the offset of this atom to the beginning of the random access
         * file
         */
        public long getOffset() {
            return offset;
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

                DataAtomOutputStream headerData = new DataAtomOutputStream(new ImageOutputStreamAdapter(out));
                headerData.writeUInt(size());
                headerData.writeType(type);
                seekRelative(pointer);
                finished = true;
                long sizeAfter = size();
                if (sizeBefore != sizeAfter) {
                    System.err.println("size mismatch " + sizeBefore + ".." + sizeAfter);
                }
            }
        }

        @Override
        public long size() {
            return 8 + data.size();
        }
    }

    /**
     * WideDataAtom can grow larger then 4 gigabytes.
     */
    protected class WideDataAtom extends Atom {

        protected DataAtomOutputStream data;
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
            data = new DataAtomOutputStream(new ImageOutputStreamAdapter(out)) {
                @Override
                public void flush() throws IOException {
                    // DO NOT FLUSH UNDERLYING STREAM!
                }
            };
        }

        public DataAtomOutputStream getOutputStream() {
            if (finished) {
                throw new IllegalStateException("Atom is finished");
            }
            return data;
        }

        /**
         * Returns the offset of this atom to the beginning of the random access
         * file
         */
        public long getOffset() {
            return offset;
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                long pointer = getRelativeStreamPosition();
                seekRelative(offset);

                DataAtomOutputStream headerData = new DataAtomOutputStream(new ImageOutputStreamAdapter(out));
                long finishedSize = size();
                if (finishedSize <= 0xffffffffL) {
                    headerData.writeUInt(8);
                    headerData.writeType("wide");
                    headerData.writeUInt(finishedSize - 8);
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

        @Override
        public long size() {
            return 16 + data.size();
        }
    }

    /**
     * Groups consecutive samples with same characteristics.
     */
    protected abstract static class Group {

        protected Sample firstSample;
        protected Sample lastSample;
        protected long sampleCount;
        protected final static long maxSampleCount = Integer.MAX_VALUE;

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

        public long getSampleCount() {
            return sampleCount;
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
         * The duration of the sample in media time scale units.
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

        @Override
        public boolean maybeAddChunk(Chunk chunk) {
            if (firstSample.duration == chunk.firstSample.duration) {
                return super.maybeAddChunk(chunk);
            }
            return false;
        }

        /**
         * Returns the duration that all samples in this group share.
         */
        public long getSampleDuration() {
            return firstSample.duration;
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

        @Override
        public boolean maybeAddChunk(Chunk chunk) {
            if (firstSample.length == chunk.firstSample.length) {
                return super.maybeAddChunk(chunk);
            }
            return false;
        }

        /**
         * Returns the length that all samples in this group share.
         */
        public long getSampleLength() {
            return firstSample.length;
        }
    }

    /**
     * Groups consecutive samples with the same sample description Id and with
     * adjacent offsets in the movie file.
     */
    protected static class Chunk extends Group {

        protected int sampleDescriptionId;

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

        @Override
        public boolean maybeAddChunk(Chunk chunk) {
            if (sampleDescriptionId == chunk.sampleDescriptionId //
                    && lastSample.offset + lastSample.length == chunk.firstSample.offset) {
                return super.maybeAddChunk(chunk);
            }
            return false;
        }

        /**
         * Returns the offset of the chunk in the movie file.
         */
        public long getChunkOffset() {
            return firstSample.offset;
        }
    }

    /**
     * Represents a track.
     */
    protected abstract class Track {

        // Common metadata
        /**
         * The media type of the track.
         */
        protected final MediaType mediaType;
        /**
         * The format of the media in the track.
         */
        protected Format format;
        /**
         * The timeScale of the media in the track. A time value that indicates
         * the time scale for this media. That is, the number of time units that
         * pass per second in its time coordinate system.
         */
        protected long mediaTimeScale = 600;
        /**
         * The compression type of the media.
         */
        protected String mediaCompressionType;
        /**
         * The compressor name.
         */
        protected String mediaCompressorName;
        /**
         * List of chunks.
         */
        protected ArrayList<Chunk> chunks = new ArrayList<Chunk>();
        /**
         * List of TimeToSample entries.
         */
        protected ArrayList<TimeToSampleGroup> timeToSamples = new ArrayList<TimeToSampleGroup>();
        /**
         * List of SampleSize entries.
         */
        protected ArrayList<SampleSizeGroup> sampleSizes = new ArrayList<SampleSizeGroup>();
        /**
         * List of sync samples. This list is null as long as all samples in
         * this track are sync samples.
         */
        protected ArrayList<Long> syncSamples = null;
        /**
         * The number of samples in this track.
         */
        protected long sampleCount = 0;
        /**
         * The duration of the media in this track in media time units.
         */
        protected long mediaDuration = 0;
        /**
         * The edit list of the track.
         */
        protected Edit[] editList;
        /**
         * Interval between sync samples (keyframes). 0 = automatic. 1 = write
         * all samples as sync samples. n = sync every n-th sample.
         */
        protected int syncInterval;
        /**
         * The codec.
         */
        protected Codec codec;
        protected Buffer outputBuffer;
        protected Buffer inputBuffer;
        /**
         * Start time of the first buffer that was added to the track.
         */
        protected Rational inputTime;
        /**
         * Current write time.
         */
        protected Rational writeTime;
        /**
         * The transformation matrix of the track.
         */
        protected double[] matrix = {//
                1, 0, 0,//
                0, 1, 0,//
                0, 0, 1
        };
        protected double width, height;

        private final static int TrackEnable = 0x1; // enabled track
        private final static int TrackInMovie = 0x2;// track in playback
        private final static int TrackInPreview = 0x4; // track in preview
        private final static int TrackInPoster = 0x8; // track in posterTrackEnable = 0x1, // enabled track

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
        protected int headerFlags = TrackEnable | TrackInMovie | TrackInPreview | TrackInPoster;

        public Track(MediaType mediaType) {
            this.mediaType = mediaType;
        }

        public void setEnabled(boolean newValue) {
            headerFlags = (newValue) ? headerFlags | TrackEnable : headerFlags & (0xff ^ TrackEnable);
        }

        public boolean isEnabled() {
            return (headerFlags & TrackEnable) != 0;
        }

        public void setInMovie(boolean newValue) {
            headerFlags = (newValue) ? headerFlags | TrackInMovie : headerFlags & (0xff ^ TrackInMovie);
        }

        public boolean isInMovie() {
            return (headerFlags & TrackInPreview) != 0;
        }

        public void setInPreview(boolean newValue) {
            headerFlags = (newValue) ? headerFlags | TrackInPreview : headerFlags & (0xff ^ TrackInPreview);
        }

        public boolean isInPreview() {
            return (headerFlags & TrackInPreview) != 0;
        }

        public void setInPoster(boolean newValue) {
            headerFlags = (newValue) ? headerFlags | TrackInPoster : headerFlags & (0xff ^ TrackInPoster);
        }

        public boolean isInPoster() {
            return (headerFlags & TrackInPoster) != 0;
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
                    syncSamples = new ArrayList<Long>();
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

        public void addChunk(Chunk chunk, boolean isSyncSample) {
            mediaDuration += chunk.firstSample.duration * chunk.sampleCount;
            sampleCount += chunk.sampleCount;

            // Keep track of sync samples. If all samples in a track are sync
            // samples, we do not need to create a syncSample list.
            if (isSyncSample) {
                if (syncSamples != null) {
                    for (long i = sampleCount - chunk.sampleCount; i < sampleCount; i++) {
                        syncSamples.add(i);
                    }
                }
            } else {
                if (syncSamples == null) {
                    syncSamples = new ArrayList<Long>();
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

        public boolean isEmpty() {
            return sampleCount == 0;
        }

        public long getSampleCount() {
            return sampleCount;
        }

        /**
         * Gets the track duration in the movie time scale.
         *
         * @param movieTimeScale The time scale of the movie.
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

        /**
         * Gets the time of the first sample in the movie time scale.
         *
         * @param movieTimeScale The time scale of the movie.
         */
        public int getFirstSampleTime(long movieTimeScale) {
            return inputTime == null ? 0 : inputTime.multiply(movieTimeScale).intValue();
        }

        //   protected abstract void writeMediaInformationHeaderAtom(CompositeAtom minfAtom) throws IOException;
        protected abstract void writeSampleDescriptionAtom(CompositeAtom stblAtom) throws IOException;
    }

    protected class VideoTrack extends Track {
        // Video metadata

        /**
         * The video compression quality.
         */
        protected float videoQuality = 0.97f;
        /**
         * Number of bits per ixel. All frames must have the same depth. The
         * value -1 is used to mark unspecified depth.
         */
        protected int videoDepth = -1;
        /**
         * The color table used for rendering the video. This variable is only
         * used when the video uses an index color model.
         */
        protected IndexColorModel videoColorTable;

        public VideoTrack() {
            super(MediaType.VIDEO);
        }

        @Override
        protected void writeSampleDescriptionAtom(CompositeAtom stblAtom) throws IOException {
            CompositeAtom leaf;
            DataAtomOutputStream d;

            /* Sample Description atom ------- */
            // The sample description atom stores information that allows you to
            // decode samples in the media. The data stored in the sample
            // description varies, depending on the media type. For example, in the
            // case of video media, the sample descriptions are image description
            // structures. The sample description information for each media type is
            // explained in “Media Data Atom Types”:
            // http://developer.apple.com/documentation/QuickTime/QTFF/QTFFChap3/chapter_4_section_1.html#//apple_ref/doc/uid/TP40000939-CH205-SW1
            leaf = new CompositeAtom("stsd");
            stblAtom.add(leaf);
            /*
             typedef struct {
             byte version;
             byte[3] flags;
             int numberOfEntries;
             sampleDescriptionEntry sampleDescriptionTable[numberOfEntries];
             } sampleDescriptionAtom;

             typedef struct {
             int size;
             magic type;
             byte[6] reserved; // six bytes that must be zero
             short dataReferenceIndex; // A 16-bit integer that contains the index
                                      //of the data reference to use to retrieve
                                      //data associated with samples that use this
                                      //sample description. Data references are
                                      //stored in data reference atoms.
             byte[size - 16] data;
             } sampleDescriptionEntry;
             */
            d = leaf.getOutputStream();
            d.write(0); // version
            // A 1-byte specification of the version of this sample description atom.

            d.write(0); // flag[0]
            d.write(0); // flag[1]
            d.write(0); // flag[2]
            // A 3-byte space for sample description flags. Set this field to 0.

            d.writeInt(1); // number of Entries
            // A 32-bit integer containing the number of sample descriptions that follow.

            // A 32-bit integer indicating the number of bytes in the sample description.
            d.writeInt(86); // sampleDescriptionTable[0].size

            d.writeType(mediaCompressionType); // sampleDescriptionTable[0].type

            // A 32-bit integer indicating the format of the stored data.
            // This depends on the media type, but is usually either the
            // compression format or the media type.

            d.write(new byte[6]); // sampleDescriptionTable[0].reserved
            // Six bytes that must be set to 0.

            d.writeShort(1); // sampleDescriptionTable[0].dataReferenceIndex
            // A 16-bit integer that contains the index of the data
            // reference to use to retrieve data associated with samples
            // that use this sample description. Data references are stored
            // in data reference atoms.

            // Video Sample Description
            // ------------------------
            // The format of the following fields is described here:
            // http://developer.apple.com/documentation/QuickTime/QTFF/QTFFChap3/chapter_4_section_2.html#//apple_ref/doc/uid/TP40000939-CH205-BBCGICBJ

            d.writeShort(0); // sampleDescriptionTable.videoSampleDescription.version
            // A 16-bit integer indicating the version number of the
            // compressed data. This is set to 0, unless a compressor has
            // changed its data format.

            d.writeShort(0); // sampleDescriptionTable.videoSampleDescription.revisionLevel
            // A 16-bit integer that must be set to 0.

            d.writeType("java"); // sampleDescriptionTable.videoSampleDescription.manufacturer
            // A 32-bit integer that specifies the developer of the
            // compressor that generated the compressed data. Often this
            // field contains 'appl' to indicate Apple Computer, Inc.

            d.writeInt(0);  // sampleDescriptionTable.videoSampleDescription.temporalQuality
            // A 32-bit integer containing a value from 0 to 1023 indicating
            // the degree of temporal compression.

            d.writeInt((int) (1024 * (1 - videoQuality))); // sampleDescriptionTable.videoSampleDescription.spatialQuality
            // A 32-bit integer containing a value from 0 to 1024 indicating
            // the degree of spatial compression.

            d.writeUShort((int) width); // sampleDescriptionTable.videoSampleDescription.width
            // A 16-bit integer that specifies the width of the source image
            // in pixels.

            d.writeUShort((int) height); // sampleDescriptionTable.videoSampleDescription.height
            // A 16-bit integer that specifies the height of the source image in pixels.

            d.writeFixed16D16(72.0); // sampleDescriptionTable.videoSampleDescription.horizontalResolution
            // A 32-bit fixed-point number containing the horizontal
            // resolution of the image in pixels per inch.

            d.writeFixed16D16(72.0); // sampleDescriptionTable.videoSampleDescription.verticalResolution
            // A 32-bit fixed-point number containing the vertical
            // resolution of the image in pixels per inch.

            d.writeInt(0); // sampleDescriptionTable.videoSampleDescription.dataSize
            // A 32-bit integer that must be set to 0.

            d.writeShort(1); // sampleDescriptionTable.videoSampleDescription.sampleCount
            // A 16-bit integer that indicates how many bytes of compressed
            // data are stored in each sample. Usually set to 1.

            d.writePString(mediaCompressorName, 32); // sampleDescriptionTable.videoSampleDescription.compressorName
            // A 32-byte Pascal string containing the name of the compressor
            // that created the image, such as "jpeg".

            d.writeShort(videoDepth); // sampleDescriptionTable.videoSampleDescription.depth
            // A 16-bit integer that indicates the pixel depth of the
            // compressed image. Values of 1, 2, 4, 8 ,16, 24, and 32
            // indicate the depth of color images. The value 32 should be
            // used only if the image contains an alpha channel. Values of
            // 34, 36, and 40 indicate 2-, 4-, and 8-bit grayscale,
            // respectively, for grayscale images.

            d.writeShort(videoColorTable == null ? -1 : 0); // sampleDescriptionTable.videoSampleDescription.colorTableID
            // A 16-bit integer that identifies which color table to use.
            // If this field is set to –1, the default color table should be
            // used for the specified depth. For all depths below 16 bits
            // per pixel, this indicates a standard Macintosh color table
            // for the specified depth. Depths of 16, 24, and 32 have no
            // color table.
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
        protected void writeColorTableAtom(CompositeAtom stblAtom) throws IOException {
            DataAtom leaf;
            DataAtomOutputStream d;
            leaf = new DataAtom("ctab");
            stblAtom.add(leaf);

            d = leaf.getOutputStream();

            d.writeUInt(0); // Color table seed. A 32-bit integer that must be set to 0.
            d.writeUShort(0x8000); // Color table flags. A 16-bit integer that must be set to 0x8000.
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
    }

    protected class AudioTrack extends Track {
        // Audio metadata

        /**
         * Number of sound channels used by the sound sample.
         */
        protected int soundNumberOfChannels;
        /**
         * Number of bits per audio sample before compression.
         */
        protected int soundSampleSize;
        /**
         * Sound compressionId. The value -1 means fixed bit rate, -2 means
         * variable bit rate.
         */
        protected int soundCompressionId;
        /**
         * Sound stsd samples per packet. The number of uncompressed samples
         * generated by a compressed sample (an uncompressed sample is one
         * sample from each channel). This is also the sample duration,
         * expressed in the media’s timescale, where the timescale is equal to
         * the sample rate. For uncompressed formats, this field is always 1.
         */
        protected long soundSamplesPerPacket;
        /**
         * For uncompressed audio, the number of bytes in a sample for a single
         * channel. This replaces the older sampleSize field, which is set to
         * 16. This value is calculated by dividing the frame size by the number
         * of channels. The same calculation is performed to calculate the value
         * of this field for compressed audio, but the result of the calculation
         * is not generally meaningful for compressed audio.
         */
        protected long soundBytesPerPacket;
        /**
         * The number of bytes in a frame: for uncompressed audio, an
         * uncompressed frame; for compressed audio, a compressed frame. This
         * can be calculated by multiplying the bytes per packet field by the
         * number of channels.
         */
        protected long soundBytesPerFrame;
        /**
         * The size of an uncompressed sample in bytes. This is set to 1 for
         * 8-bit audio, 2 for all other cases, even if the sample size is
         * greater than 2 bytes.
         */
        protected long soundBytesPerSample;
        /**
         * Sound sample rate. The integer portion must match the media's time
         * scale.
         */
        protected double soundSampleRate;
        /**
         * Extensions to the stsd chunk. Must contain atom-based fields: ([long
         * size, long type, some data], repeat)
         */
        protected byte[] stsdExtensions = new byte[0];

        public AudioTrack() {
            super(MediaType.AUDIO);
        }

        @Override
        protected void writeSampleDescriptionAtom(CompositeAtom stblAtom) throws IOException {
            // TO DO
            DataAtom leaf;
            DataAtomOutputStream d;

            /* Sample Description atom ------- */
            // The sample description atom stores information that allows you to
            // decode samples in the media. The data stored in the sample
            // description varies, depending on the media type. For example, in the
            // case of video media, the sample descriptions are image description
            // structures. The sample description information for each media type is
            // explained in “Media Data Atom Types”:
            // http://developer.apple.com/documentation/QuickTime/QTFF/QTFFChap3/chapter_4_section_1.html#//apple_ref/doc/uid/TP40000939-CH205-SW1
            leaf = new DataAtom("stsd");
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
            d.writeUInt(4 + 12 + 20 + 16 + stsdExtensions.length); // sampleDescriptionTable[0].size

            // Common header: 12 bytes
            d.writeType(mediaCompressionType); // sampleDescriptionTable[0].type
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

            d.writeUShort(soundNumberOfChannels);  // numberOfChannels
            // A 16-bit integer that indicates the number of sound channels used by
            // the sound sample. Set to 1 for monaural sounds, 2 for stereo sounds.
            // Higher numbers of channels are not supported.

            d.writeUShort(soundSampleSize); // sampleSize (bits)
            // A 16-bit integer that specifies the number of bits in each
            // uncompressed sound sample. Allowable values are 8 or 16. Formats
            // using more than 16 bits per sample set this field to 16 and use sound
            // description version 1.

            d.writeUShort(soundCompressionId); // compressionId
            // XXX - This must be set to -1, or the QuickTime player won't accept this file.
            // A 16-bit integer that must be set to 0 for version 0 sound
            // descriptions. This may be set to –2 for some version 1 sound
            // descriptions; see “Redefined Sample Tables” (page 135).

            d.writeUShort(0); // packetSize
            // A 16-bit integer that must be set to 0.

            d.writeFixed16D16(soundSampleRate); // sampleRate
            // A 32-bit unsigned fixed-point number (16.16) that indicates the rate
            // at which the sound samples were obtained. The integer portion of this
            // number should match the media’s time scale. Many older version 0
            // files have values of 22254.5454 or 11127.2727, but most files have
            // integer values, such as 44100. Sample rates greater than 2^16 are not
            // supported.

            // Sound Sample Description Additional fields (only in Version 1) 16 bytes
            // ------------------------
            d.writeUInt(soundSamplesPerPacket); // samplesPerPacket
            // A 32-bit integer.
            // The number of uncompressed samples generated by a
            // compressed sample (an uncompressed sample is one sample
            // from each channel). This is also the sample duration,
            // expressed in the media’s timescale, where the
            // timescale is equal to the sample rate. For
            // uncompressed formats, this field is always 1.
            //
            d.writeUInt(soundBytesPerPacket); // bytesPerPacket
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
            d.writeUInt(soundBytesPerFrame); // bytesPerFrame
            // A 32-bit integer.
            // The number of bytes in a sample: for uncompressed
            // audio, an uncompressed frame; for compressed audio, a
            // compressed frame. This can be calculated by
            // multiplying the bytes per packet field by the number
            // of channels.
            //
            d.writeUInt(soundBytesPerSample); // bytesPerSample
            // A 32-bit integer.
            // The size of an uncompressed sample in bytes. This is
            // set to 1 for 8-bit audio, 2 for all other cases, even
            // if the sample size is greater than 2 bytes.

            // Write stsd Extensions
            // Extensions must be atom-based fields
            // ------------------------------------
            d.write(stsdExtensions);
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
         * A 32-bit integer that specifies the duration of this edit segment in
         * units of the movie's time scale.
         */
        public int trackDuration;
        /**
         * A 32-bit integer containing the start time within the media of this
         * edit segment (in media time scale units). If this field is set to -1,
         * it is an empty edit. The last edit in a track should never be an
         * empty edit. Any differece between the movie's duration and the
         * track's duration is expressed as an implicit empty edit.
         */
        public int mediaTime;
        /**
         * A 32-bit fixed-point number (16.16) that specifies the relative rate
         * at which to play the media corresponding to this edit segment. This
         * rate value cannot be 0 or negative.
         */
        public int mediaRate;

        /**
         * Creates an edit.
         *
         * @param trackDuration Duration of this edit in the movie's time scale.
         * @param mediaTime     Start time of this edit in the media's time scale.
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
         * @param trackDuration Duration of this edit in the movie's time scale.
         * @param mediaTime     Start time of this edit in the media's time scale.
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
}

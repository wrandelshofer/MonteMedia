/* @(#)QuickTimeMeta.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime;

import org.monte.media.av.AbstractMovie;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.MovieReader;
import org.monte.media.math.Rational;

import java.awt.image.IndexColorModel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaType;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * {@code QuickTimeMeta} holds the meta-data contained in a QuickTime movie.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuickTimeMeta extends AbstractMovie {

    private final static long serialVersionUID = 1L;
    public final static Locale[] LANGUAGE_CODES = {
            new Locale("en"),//0 English
            new Locale("fr"),//1 French
            new Locale("de"),//2 German
            new Locale("it"),//3 Italian
            new Locale("nld"),//4 Dutch
            new Locale("swe"),//5 Swedish
            new Locale("spa"),//6 Spanish
            new Locale("dan"),//7 Danish
            new Locale("por"),//8 Portuguese
            new Locale("nor"),//9 Norwegian
            new Locale("heb"),//10 Hebrew
            new Locale("ja"),//11 Japanese
            new Locale("ara"),//12 Arabic
            new Locale("fin"),//13 Finnish
            new Locale("ell"),//14 Greek
            new Locale("isl"),//15 Icelandic
            new Locale("mlt"),//16 Maltese
            new Locale("tur"),//17 Turkish
            new Locale("hrv"),//18 Croatian
            new Locale("zh"),//19 Traditional Chinese
            new Locale("urd"),//20 Urdu
            new Locale("hin"),//21 Hindi
            new Locale("tha"),//22 Thai
            new Locale("ko"),//23 Koeran
            new Locale("lit"),//24 Lithuanian
            new Locale("pol"),//25 Polish
            new Locale("hun"),//26 Hungarian
            new Locale("est"),//27 Estonian
            new Locale("lav"),//28 LettishLatvian
            new Locale("sme"),//29 SaamishLappish
            null,//30
            null,//31
            null,//32
            null,//33
            null,//34
            null,//35
            null,//36
            null,//37
            null,//38
            null,//39
            null,//40
            null,//41
            null,//42
            null,//43
            null,//44
            null,//45
            null,//46
            null,//47
            null,//48
            null,//49
            null,//50
            null,//51
            new Locale("kat"),//52 Georgian
            new Locale("ron"),//53 Moldavian
            new Locale("kir"),//54 Kirghiz
            new Locale("tgk"),//55 Tajiki
            new Locale("tuk"),//56 Turkmen
            new Locale("mon"),//57 Mongolian
            new Locale("mon"),//58 MongolianCyr
            new Locale("pus"),//59 Pashto
            new Locale("kur"),//60 Kurdish
            new Locale("kas"),//61 Kashmiri
            new Locale("snd"),//62 Sindhi
            new Locale("bod"),//63 Tibetan
            new Locale("npi"),//64 Nepali
            new Locale("san"),//65 Sanskrit
            new Locale("mar"),//66 Marathi
            new Locale("ben"),//67 Bengali
            new Locale("asm"),//68 Assamese
            new Locale("guj"),//69 Gujarati
            new Locale("pan"),//70 Punjabi
            new Locale("ory"),//71 Oriya
            new Locale("mal"),//72 Malayalam
            new Locale("kan"),//73 Kannada
            new Locale("tam"),//74 Tamil
            new Locale("tel"),//75 Telugu
            new Locale("sin"),//76 Sinhalese
            new Locale("mya"),//77 Burmese
            new Locale("khm"),//78 Khmer
            new Locale("lao"),//79 Lao
            new Locale("vie"),//80 Vietnamese
            new Locale("ind"),//81 Indonesian
            new Locale("tgl"),//82 Tagalog
            new Locale("mal"),//83 MalayRoman
    };
    /**
     * The file format.
     */
    protected Format fileFormat = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_QUICKTIME);
    // BEGIN File Type
    protected String brand;
    protected int versionYear;
    protected int versionMonth;
    protected int versionMinor;
    protected ArrayList<String> compatibleBrands = new ArrayList<String>();
    // END File Type
    // BEGIN Movie Header
    /**
     * Creation time of the movie.
     */
    protected Date creationTime;
    /**
     * Modification time of the movie.
     */
    protected Date modificationTime;
    /**
     * The time scale of the movie. A time value that indicates the time scale
     * for this media—that is, the number of time units that pass per second in
     * its time coordinate system.
     */
    protected long timeScale;
    /**
     * The duration of the movie in time scale units (derived value). The value
     * of this field corresponds to the duration of the longest track in the
     * movie.
     */
    protected long duration;
    /**
     * The preferred rate at which to play this movie. A value of 1.0 indicates
     * normal rate.
     */
    protected double preferredRate;
    /**
     * The preferred volume of this movie’s sound. A value of 1.0 indicates full
     * volume.
     */
    protected double preferredVolume;
    /**
     * The transformation matrix [a,b,u;c,d,v;x,y,w] for mapping 2d points of
     * this movie to the screen.
     * <pre>
     *           [a b u;
     * [x y 1] *  c d v; = [x' y' 1]
     *            x y w]
     * </pre>
     */
    protected double[] matrix;
    /**
     * The time value in the movie at which the preview begins.
     */
    protected long previewTime;
    /**
     * The duration of the movie preview in movie time scale units.
     */
    protected long previewDuration;
    /**
     * The time value of the time of the movie poster.
     */
    protected long posterTime;
    /**
     * The time value for the start time of the current selection.
     */
    protected long selectionTime;
    /**
     * The duration of the current selection in movie time scale units.
     */
    protected long selectionDuration;
    /**
     * The time value for current time position within the movie.
     */
    protected long currentTime;
    /**
     * The ID to use for the next track added to this movie (derived value). The
     * value of this field corresponds to the number of tracks.
     */
    protected long nextTrackId;
    // END Movie Header
    /**
     * The list of tracks in the movie.
     */
    protected ArrayList<Track> tracks = new ArrayList<Track>();

    public QuickTimeMeta() {
        clear();
    }

    private void deriveTrackFormat(int trackIndex) {
        Track track = tracks.get(trackIndex);
        Format format = new Format(MimeTypeKey, MIME_QUICKTIME,
                MediaTypeKey, track.mediaType);
        if (track.mediaList.size() != 1) {
            throw new UnsupportedOperationException("not implemented for tracks with multiple media. " + trackIndex + " " + track.mediaType + " " + track.mediaList);
        }
        Media m = track.mediaList.get(0);
        // FIXME implement me
        switch (track.mediaType) {
            case VIDEO:
                if (m.sampleDescriptions.size() != 1) {
                    throw new UnsupportedOperationException("not implemented for media with multiple sample descriptions.. " + trackIndex + " " + track.mediaType + " " + m + " " + m.sampleDescriptions);
                }

                SampleDescription desc = m.sampleDescriptions.get(0);
                format = format.append(
                        EncodingKey, desc.mediaType,
                        CompressorNameKey, desc.videoCompressorName,
                        HeightKey, desc.videoHeight,
                        WidthKey, desc.videoWidth,
                        DepthKey, desc.videoDepth
                );
                if (m.timeToSamples.size() == 1) {
                    TimeToSampleGroup ttsg = m.timeToSamples.get(0);
                    format = format.append(FrameRateKey, new Rational(ttsg.getSampleDuration(), m.mediaTimeScale));
                } else {
                    format = format.append(FrameRateKey, new Rational(1, m.mediaTimeScale));
                }
                break;
            case AUDIO:
            case META:
            case MIDI:
            case FILE:
            case TEXT:
            default:
                throw new UnsupportedOperationException("not implemented " + track.mediaType);
        }
        track.format = format;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public Date getModificationTime() {
        return modificationTime;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public long getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(long movieTimeScale) {
        this.timeScale = movieTimeScale;
    }

    public double getPreferredRate() {
        return preferredRate;
    }

    public void setPreferredRate(double preferredRate) {
        this.preferredRate = preferredRate;
    }

    public double getPreferredVolume() {
        return preferredVolume;
    }

    public void setPreferredVolume(double preferredVolume) {
        this.preferredVolume = preferredVolume;
    }

    public long getPreviewTime() {
        return previewTime;
    }

    public void setPreviewTime(long previewTime) {
        this.previewTime = previewTime;
    }

    public long getPreviewDuration() {
        return previewDuration;
    }

    public void setPreviewDuration(long previewDuration) {
        this.previewDuration = previewDuration;
    }

    public long getPosterTime() {
        return posterTime;
    }

    public void setPosterTime(long posterTime) {
        this.posterTime = posterTime;
    }

    public long getSelectionTime() {
        return selectionTime;
    }

    public void setSelectionTime(long selectionTime) {
        this.selectionTime = selectionTime;
    }

    public long getSelectionDuration() {
        return selectionDuration;
    }

    public void setSelectionDuration(long selectionDuration) {
        this.selectionDuration = selectionDuration;
    }

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public long getNextTrackId() {
        return nextTrackId;
    }

    public void setNextTrackId(long nextTrackId) {
        this.nextTrackId = nextTrackId;
    }

    @Override
    public Rational getDuration() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public long timeToSample(int track, Rational seconds) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Rational sampleToTime(int track, long sample) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getTrackCount() {
        return tracks.size();
    }

    @Override
    public Format getFormat(int track) {
        if (tracks.get(track).format == null) {
            deriveTrackFormat(track);
        }
        return tracks.get(track).format;
    }

    @Override
    public Format getFileFormat() {
        return fileFormat;
    }

    @Override
    public MovieReader getReader() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Clears this movie.
     */
    protected void clear() {
        // File Type
        brand = "qt";
        versionYear = versionMonth = versionMinor = 0;
        compatibleBrands.clear();
        compatibleBrands.add(brand);
        // Movie Header 
        creationTime = modificationTime = new Date();
        timeScale = 600;
        duration = 0;
        preferredRate = 1.0;
        preferredVolume = 1.0;
        previewTime = 0;
        previewDuration = 0;
        posterTime = 0;
        selectionTime = 0;
        selectionDuration = 0;
        currentTime = 0;
        nextTrackId = 0;
        matrix = new double[9];

        // Movie Tracks
        tracks.clear();
    }

    public double[] getTransformationMatrix() {
        return matrix;
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
            sampleCount = firstSample == null ? 0 : 1;
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
         * returned, the sample must be added to a new group.
         * <p>
         * A sample can only be added to a group, if the capacity of the group
         * is not exceeded.
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
         * returned, the chunk must be added to a new group.
         * <p>
         * A chunk can only be added to a group, if the capacity of the group is
         * not exceeded.
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
     * sequence of time-ordered data. Information about samples is stored in the
     * mdat atom.
     * <p>
     * Please note that {@code Sample} objects are created on demand. This helps
     * to save memory, because a highly multiplexed audio track may consist of
     * many samples per second. As a consequence multiple instances of
     * {@code Sample} objects may represent the same data sample in the movie.
     */
    protected static class Sample {

        /**
         * Offset of the sample relative to the start of the QuickTime file. The
         * value -1 is used if the offset is unknown.
         */
        long offset;
        /**
         * Data length of the sample. The value -1 is used if the data length is
         * unknown.
         */
        long length;
        /**
         * The duration of the sample in media time scale units. The value -1 is
         * used if the duration is unknown.
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
     * Groups consecutive samples of the same duration.
     */
    protected static class TimeToSampleGroup extends Group {

        public TimeToSampleGroup(Sample firstSample) {
            super(firstSample);
        }

        protected TimeToSampleGroup(Sample firstSample, Sample lastSample, long sampleCount) {
            super(firstSample, lastSample, sampleCount);
        }

        public TimeToSampleGroup(Group group) {
            super(group);
        }

        /**
         * Returns true, if the sample was added to the group. If false is
         * returned, the sample must be added to a new group.
         * <p>
         * A sample can only be added to a TimeToSampleGroup, if it has the same
         * duration as previously added samples, and if the capacity of the
         * group is not exceeded.
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
     * Groups consecutive samples into a chunk.
     */
    protected static class SampleToChunk {

        int firstChunk;
        int samplesPerChunk;
        int sampleDescription;

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

        public SampleSizeGroup(Sample firstSample, Sample lastSample, long sampleCount) {
            super(firstSample, lastSample, sampleCount);
        }

        /**
         * Returns true, if the sample was added to the group. If false is
         * returned, the sample must be added to a new group.
         * <p>
         * A sample can only be added to a SampleSizeGroup, if it has the same
         * size as previously added samples, and if the capacity of the group is
         * not exceeded.
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
         * returned, the sample must be added to a new chunk.
         * <p>
         * A sample can only be added to a chunk, if it has the same sample
         * description Id as previously added samples, if the capacity of the
         * chunk is not exceeded and if the sample offset is adjacent to the
         * last sample in this chunk.
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
    protected static class Track {

        /**
         * The media type of the track.
         */
        protected FormatKeys.MediaType mediaType;
        /**
         * The format of the media in the track.
         */
        protected Format format;
        // BEGIN Track Header
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
        /**
         * Creation time of the track.
         */
        protected Date creationTime;
        /**
         * Modification time of the track.
         */
        protected Date modificationTime;
        /**
         * The id of the track. The value 0 cannot be used.
         */
        protected int trackId;
        /**
         * The duration of the track given in the movie time scale (derived
         * value). The value is equal to the sum of all track edits. If there
         * are no edits, then the value is the sum of all sample durations
         * converted into the movie time scale.
         */
        protected long duration;
        /**
         * The track layer. Tracks with lower layers are displayed in front of
         * tracks with higher layers.
         */
        protected int layer;
        /**
         * An identifier which specifies a collection of tracks that contain
         * alternate data for one other. Only one track of an alternate group is
         * displayed based on selection criteria such as quality, language or
         * computer capabilities.
         */
        protected int alternateGroup;
        /**
         * The audio volume of the track. 1.0 means normal volume.
         */
        protected double volume;
        /**
         * The transformation matrix of the track.
         */
        protected double[] matrix = {//
                1, 0, 0,//
                0, 1, 0,//
                0, 0, 1
        };
        /**
         * The track dimension.
         */
        protected double width, height;
        // END Track Header
        // BEGIN Edit List
        /**
         * The edit list of the track.
         */
        protected ArrayList<Edit> editList = new ArrayList<Edit>();
        // END Edit List
        // BEGIN Media List
        /**
         * The media list of the track.
         */
        protected ArrayList<Media> mediaList = new ArrayList<Media>();
        // END Media List

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

        /**
         * Gets the track duration in the movie time scale.
         *
         * @param movieTimeScale The time scale of the movie.
         */
        public long getTrackDuration(long movieTimeScale) {
            if (editList.isEmpty()) {
                long sum = 0;
                for (Media m : mediaList) {
                    sum += m.mediaDuration * movieTimeScale / m.mediaTimeScale;
                }
                return sum;
            } else {
                long sum = 0;
                for (Edit e : editList) {
                    sum += e.trackDuration;
                }
                return sum;
            }
        }

        @Override
        public String toString() {
            return "Track{" //
                    + " format=" + format //
                    + "\ntkhd:"//
                    + " creationTime=" + creationTime //
                    + ", modificationTime=" + modificationTime //
                    + ", trackId=" + trackId //
                    + ", duration=" + duration //
                    + ", layer=" + layer //
                    + ", alternateGroup=" + alternateGroup //
                    + ", volume=" + volume //
                    + ", matrix=" + Arrays.toString(matrix) //
                    + ", width=" + width //
                    + ", height=" + height //
                    + "\nelst:"//
                    + " editList=" + editList //
                    + "\nmdia:"//
                    + " mediaList=" + mediaList //
                    + '}';
        }
    }

    /**
     * Represents a media.
     */
    protected static class Media {
        // BEGIN Media Header

        protected Date mediaCreationTime;
        protected Date mediaModificationTime;
        /**
         * The timeScale of the media in the track. A time value that indicates
         * the time scale for this media. That is, the number of time units that
         * pass per second in its time coordinate system.
         */
        protected long mediaTimeScale = 600;
        /**
         * The duration of the media in this track in media time units (derived
         * value). The value is equal to the sum of all sample durations.
         */
        protected long mediaDuration = 0;
        protected Locale mediaLanguage = Locale.ENGLISH;
        protected String mediaLanguageEncoding = "UTF-8";
        protected int mediaQuality;
        // BEGIN Sound Media Header
        protected double soundBalance;
        // END Sound Media Header

        // END Media Header
        // BEGIN Data Reference List
        /**
         * The data reference list of the track.
         */
        protected ArrayList<DataReference> dataReferenceList = new ArrayList<DataReference>();
        // END Data Reference List
        /**
         * List of chunks.
         */
        protected ArrayList<Chunk> chunks = new ArrayList<Chunk>();
        /**
         * List of TimeToSample entries.
         */
        protected ArrayList<TimeToSampleGroup> timeToSamples = new ArrayList<TimeToSampleGroup>();
        /**
         * List of SampleToChunk entries.
         */
        protected ArrayList<SampleToChunk> samplesToChunks = new ArrayList<SampleToChunk>();
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
         * Interval between sync samples (keyframes). 0 = automatic. 1 = write
         * all samples as sync samples. n = sync every n-th sample.
         */
        protected int syncInterval;
        // BEGIN Video Header
        /**
         * The video compression quality.
         */
        protected float videoQuality = 0.97f;
        /**
         * The color table used for rendering the video. This variable is only
         * used when the video uses an index color model.
         */
        protected IndexColorModel videoColorTable;
        // END Video Header
        // BEGIN Video Media Header
        boolean videoFlagNoLeanAhead;
        int graphicsMode;
        int[] opcolor = new int[3];
        // END Video Media Header

        private ArrayList<SampleDescription> sampleDescriptions = new ArrayList<>();

        public void addSampleDescription(SampleDescription d) {
            sampleDescriptions.add(d);
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

        @Override
        public String toString() {
            return "Media{" //
                    + "mediaCreationTime=" + mediaCreationTime //
                    + ", mediaModificationTime=" + mediaModificationTime //
                    + ", mediaTimeScale=" + mediaTimeScale//
                    + ", mediaDuration=" + mediaDuration //
                    + ", mediaLanguage=" + mediaLanguage//
                    + ", mediaQuality=" + mediaQuality //
                    + ", videoColorTable=" + videoColorTable//
                    + ", soundBalance=" + soundBalance//
                    + ", dataReferenceList=" + dataReferenceList //
                    + ", chunks=" + chunks//
                    + ", timeToSamples=" + timeToSamples //
                    + ", sampleSizes=" + sampleSizes
                    + ", syncSamples=" + syncSamples
                    + ", sampleCount=" + sampleCount
                    + ", syncInterval=" + syncInterval
                    + ", videoQuality=" + videoQuality
                    + ", videoColorTable=" + videoColorTable
                    + '}';
        }
    }

    /**
     * Represents a sample description.
     */
    protected static class SampleDescription {

        /**
         * The media type.
         */
        protected String mediaType;
        /**
         * The data reference index.
         */
        protected int dataReferenceIndex;

        // BEGIN Video Sample Description
        protected float videoTemporalQuality;
        protected float videoSpatialQuality;
        protected int videoWidth;
        protected int videoHeight;
        protected double videoHorizontalResolution = 72;
        protected double videoVerticalResolution = 72;
        protected int videoFrameCount;
        protected String videoCompressorName;
        /**
         * Number of bits per Pixel. All frames must have the same depth. The
         * value -1 is used to mark unspecified depth.
         */
        protected int videoDepth = -1;
        protected int videoColorTableId = -1;
        // END Video Sample Description
        // BEGIN Sound Sample Description
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
        // END Sound Sample Description

        @Override
        public String toString() {
            return "SampleDescription{"//
                    + ", videoDepth=" + videoDepth //
                    + ", soundNumberOfChannels=" + soundNumberOfChannels //
                    + ", soundSampleSize=" + soundSampleSize//
                    + ", soundCompressionId=" + soundCompressionId//
                    + ", soundSamplesPerPacket=" + soundSamplesPerPacket//
                    + ", soundBytesPerPacket=" + soundBytesPerPacket //
                    + ", soundBytesPerFrame=" + soundBytesPerFrame //
                    + ", soundBytesPerSample=" + soundBytesPerSample //
                    + ", soundSampleRate=" + soundSampleRate//
                    + ", stsdExtensions=" + stsdExtensions//
                    + '}';
        }
    }

    /**
     * An {@code Edit} define the portions of the media that are to be used to
     * build up a track for a movie. The edits themselves are stored in an edit
     * list table, which consists of time offset and duration values for each
     * segment.
     * <p>
     * In the absence of an edit list, the presentation of the track starts
     * immediately. An empty edit is used to offset the start time of a track.
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
         * Creates an edit.
         * <p>
         * Use this constructor only if you want to compute the fixed point
         * media rate by yourself.
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

        @Override
        public String toString() {
            return "Edit{" + "trackDuration=" + trackDuration + ", mediaTime=" + mediaTime + ", mediaRate=" + mediaRate + '}';
        }
    }

    /**
     * An {@code DataReference} references the media data. Typically, the media
     * data is contained in the same file.
     */
    public static class DataReference {

        /**
         * Reference type. Can be "alis", "rsrc" or "url ".
         */
        protected String referenceType;
        private final static int DataRefSelfReference = 0x1; //
        /**
         * <pre>
         * // Enumeration for reference flags
         * set {
         * DataRefSelfReference = 0x1, // data is in same file
         * } TrackHeaderFlags;
         * </pre>
         */
        protected int referenceFlags = DataRefSelfReference;
        /**
         * Reference data.
         */
        protected byte[] data;

        @Override
        public String toString() {
            return "DataReference{" + "referenceType=" + referenceType + ", referenceFlags=" + referenceFlags + ", data=" + data + '}';
        }
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("QuickTimeMovie{"//
                + " fileFormat=" + fileFormat//
                + "\nftyp:"//
                + " brand=" + brand//
                + ", versionYear=" + versionYear//
                + ", versionMonth=" + versionMonth//
                + ", versionMinor=" + versionMinor//
                + ", compatibleBrands=" + compatibleBrands//
                + "\nmvhd:"//
                + " creationTime=" + creationTime//
                + ", modificationTime=" + modificationTime//
                + ", timeScale=" + timeScale//
                + ", duration=" + duration//
                + ", preferredRate=" + preferredRate//
                + ", preferredVolume=" + preferredVolume//
                + ", matrix=" + Arrays.toString(matrix)//
                + ", previewTime=" + previewTime//
                + ", previewDuration=" + previewDuration//
                + ", posterTime=" + posterTime//
                + ", selectionTime=" + selectionTime//
                + ", selectionDuration=" + selectionDuration//
                + ", currentTime=" + currentTime//
                + ", nextTrackId=" + nextTrackId);//
        for (Track t : tracks) {
            buf.append("\ntrak: ");//
            buf.append(t.toString());
        }
        buf.append('}');
        return buf.toString();
    }
}

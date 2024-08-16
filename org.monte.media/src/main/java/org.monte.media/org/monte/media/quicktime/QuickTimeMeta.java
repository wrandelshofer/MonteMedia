/*
 * @(#)QuickTimeMeta.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime;

import org.monte.media.av.AbstractMovie;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.MovieReader;
import org.monte.media.color.Colors;
import org.monte.media.math.Rational;

import java.awt.image.IndexColorModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaType;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.FormatKeys.SampleFormatKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.PaletteKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * {@code QuickTimeMeta} holds the meta-data contained in a QuickTime movie.
 *
 * @author Werner Randelshofer
 */
public class QuickTimeMeta extends AbstractMovie {

    private final static long serialVersionUID = 1L;
    public final static Locale[] LANGUAGE_CODES = {
            Locale.forLanguageTag("en"),//0 English
            Locale.forLanguageTag("fr"),//1 French
            Locale.forLanguageTag("de"),//2 German
            Locale.forLanguageTag("it"),//3 Italian
            Locale.forLanguageTag("nld"),//4 Dutch
            Locale.forLanguageTag("swe"),//5 Swedish
            Locale.forLanguageTag("spa"),//6 Spanish
            Locale.forLanguageTag("dan"),//7 Danish
            Locale.forLanguageTag("por"),//8 Portuguese
            Locale.forLanguageTag("nor"),//9 Norwegian
            Locale.forLanguageTag("heb"),//10 Hebrew
            Locale.forLanguageTag("ja"),//11 Japanese
            Locale.forLanguageTag("ara"),//12 Arabic
            Locale.forLanguageTag("fin"),//13 Finnish
            Locale.forLanguageTag("ell"),//14 Greek
            Locale.forLanguageTag("isl"),//15 Icelandic
            Locale.forLanguageTag("mlt"),//16 Maltese
            Locale.forLanguageTag("tur"),//17 Turkish
            Locale.forLanguageTag("hrv"),//18 Croatian
            Locale.forLanguageTag("zh"),//19 Traditional Chinese
            Locale.forLanguageTag("urd"),//20 Urdu
            Locale.forLanguageTag("hin"),//21 Hindi
            Locale.forLanguageTag("tha"),//22 Thai
            Locale.forLanguageTag("ko"),//23 Koeran
            Locale.forLanguageTag("lit"),//24 Lithuanian
            Locale.forLanguageTag("pol"),//25 Polish
            Locale.forLanguageTag("hun"),//26 Hungarian
            Locale.forLanguageTag("est"),//27 Estonian
            Locale.forLanguageTag("lav"),//28 LettishLatvian
            Locale.forLanguageTag("sme"),//29 SaamishLappish
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
            Locale.forLanguageTag("kat"),//52 Georgian
            Locale.forLanguageTag("ron"),//53 Moldavian
            Locale.forLanguageTag("kir"),//54 Kirghiz
            Locale.forLanguageTag("tgk"),//55 Tajiki
            Locale.forLanguageTag("tuk"),//56 Turkmen
            Locale.forLanguageTag("mon"),//57 Mongolian
            Locale.forLanguageTag("mon"),//58 MongolianCyr
            Locale.forLanguageTag("pus"),//59 Pashto
            Locale.forLanguageTag("kur"),//60 Kurdish
            Locale.forLanguageTag("kas"),//61 Kashmiri
            Locale.forLanguageTag("snd"),//62 Sindhi
            Locale.forLanguageTag("bod"),//63 Tibetan
            Locale.forLanguageTag("npi"),//64 Nepali
            Locale.forLanguageTag("san"),//65 Sanskrit
            Locale.forLanguageTag("mar"),//66 Marathi
            Locale.forLanguageTag("ben"),//67 Bengali
            Locale.forLanguageTag("asm"),//68 Assamese
            Locale.forLanguageTag("guj"),//69 Gujarati
            Locale.forLanguageTag("pan"),//70 Punjabi
            Locale.forLanguageTag("ory"),//71 Oriya
            Locale.forLanguageTag("mal"),//72 Malayalam
            Locale.forLanguageTag("kan"),//73 Kannada
            Locale.forLanguageTag("tam"),//74 Tamil
            Locale.forLanguageTag("tel"),//75 Telugu
            Locale.forLanguageTag("sin"),//76 Sinhalese
            Locale.forLanguageTag("mya"),//77 Burmese
            Locale.forLanguageTag("khm"),//78 Khmer
            Locale.forLanguageTag("lao"),//79 Lao
            Locale.forLanguageTag("vie"),//80 Vietnamese
            Locale.forLanguageTag("ind"),//81 Indonesian
            Locale.forLanguageTag("tgl"),//82 Tagalog
            Locale.forLanguageTag("mal"),//83 MalayRoman
    };
    public List<IndexColorModel> colorTables = new ArrayList<>();
    /**
     * The compression method used for compressing the compressed movie data atom cmvd.
     */
    public String compressionMethod;
    public long movieDataStreamPosition = -1;
    public long movieDataSize = -1;
    /**
     * The file format.
     */
    protected Format fileFormat = new Format(MediaTypeKey, MediaType.FILE, MimeTypeKey, MIME_QUICKTIME);
    // BEGIN File Type
    protected String brand;
    protected int versionYear;
    protected int versionMonth;
    protected int versionMinor;
    protected ArrayList<String> compatibleBrands = new ArrayList<>();
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
    protected ArrayList<Track> tracks = new ArrayList<>();

    public QuickTimeMeta() {
        clear();
    }

    private void deriveTrackFormat(int trackIndex) {
        Track track = tracks.get(trackIndex);
        Format format = new Format(MimeTypeKey, MIME_QUICKTIME,
                MediaTypeKey, track.mediaType
        );
        String dataFormat = (track.media != null
                && !track.media.sampleDescriptions.isEmpty())
                ? track.media.sampleDescriptions.getFirst().dataFormat : null;
        if (dataFormat != null) {
            format = format.append(EncodingKey, dataFormat);
        }
        if (track.media == null) {
            throw new UnsupportedOperationException("not implemented for tracks without media. " + trackIndex + " " + track.mediaType + " " + track.media);
        }
        Media m = track.media;
        switch (track.mediaType) {
            case VIDEO: {
                if (m.sampleDescriptions.size() != 1) {
                    throw new UnsupportedOperationException("not implemented for media with multiple sample descriptions.. " + trackIndex + " " + track.mediaType + " " + m + " " + m.sampleDescriptions);
                }
                SampleDescription desc = m.sampleDescriptions.getFirst();

                if (desc.videoDepth == 8) {
                    if (0 <= desc.videoColorTableId && desc.videoColorTableId < this.colorTables.size()) {
                        format = format.append(PaletteKey, this.colorTables.get(desc.videoColorTableId));
                    } else {
                        format = format.append(PaletteKey, Colors.createMacColors());
                    }
                }

                format = format.append(
                        SampleFormatKey, desc.dataFormat,
                        CompressorNameKey, desc.videoCompressorName,
                        HeightKey, desc.videoHeight,
                        WidthKey, desc.videoWidth,
                        DepthKey, desc.videoDepth
                );
                if (m.timeToSamples.size() == 1) {
                    TimeToSampleGroup ttsg = m.timeToSamples.getFirst();
                    format = format.append(FrameRateKey, new Rational(ttsg.getSampleDuration(), m.mediaTimeScale));
                } else {
                    format = format.append(FrameRateKey, new Rational(1, m.mediaTimeScale));
                }
                break;
            }
            default: {
                if (m.sampleDescriptions.size() != 1) {
                    throw new UnsupportedOperationException("not implemented for media with multiple sample descriptions.. " + trackIndex + " " + track.mediaType + " " + m + " " + m.sampleDescriptions);
                }

                SampleDescription desc = m.sampleDescriptions.getFirst();
                format = format.append(
                        SampleFormatKey, desc.dataFormat
                );
                break;
            }
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

        protected MediaSample firstSample;
        protected MediaSample lastSample;
        protected long sampleCount;
        protected final static long maxSampleCount = Integer.MAX_VALUE;

        protected Group(MediaSample firstSample) {
            this.firstSample = this.lastSample = firstSample;
            sampleCount = firstSample == null ? 0 : 1;
        }

        protected Group(MediaSample firstSample, MediaSample lastSample, long sampleCount) {
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
        protected boolean maybeAddSample(MediaSample sample) {
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
     * QuickTime stores media data in media samples. A media sample is a single
     * element in a sequence of time-ordered data. Information about samples is
     * stored in the mdat atom.
     * <p>
     * Please note that {@code MediaSample} objects are created on demand. This helps
     * to save memory, because a highly multiplexed audio track may consist of
     * many samples per second. As a consequence multiple instances of
     * {@code MediaSample} objects may represent the same data sample in the movie.
     */
    protected static class MediaSample {

        /**
         * Byte offset of the sample relative to the start of the QuickTime file.
         * The value -1 is used if the offset is unknown.
         */
        long offset;
        /**
         * Data length of the sample in bytes. The value -1 is used if the data length is
         * unknown.
         */
        long length;
        /**
         * The duration of the sample in media timescale units. The value -1 is
         * used if the duration is unknown.
         */
        long duration;
        /**
         * The timestamp of the first sample in the chunk in media timescale units.
         * The value -1 is used if the timestamp is unknown.
         */
        public long timeStamp = -1;
        /**
         * Whether the sample is a sync-sample.
         */
        public boolean isKeyframe;

        /**
         * Creates a new sample.
         */
        public MediaSample(long duration, long offset, long length) {
            this.duration = duration;
            this.offset = offset;
            this.length = length;
        }
    }

    /**
     * QuickTime plays media in tracks which reference media samples.
     * <p>
     * Please note that {@code TrackSample} objects are created on demand. This helps
     * to save memory, because a highly multiplexed audio track may consist of
     * many samples per second. As a consequence multiple instances of
     * {@code TrackSample} objects may represent the same data sample in the movie.
     */
    protected static class TrackSample {

        /**
         * Byte offset of the sample relative to the start of the QuickTime file.
         * The value -1 is used if the offset is unknown.
         */
        MediaSample mediaSample;

        /**
         * The timestamp of the sample in movie timescale units.
         */
        public long timeStamp;
        /**
         * The duration of the sample in movie timescale units. The value -1 is
         * used if the duration is unknown.
         */
        long duration;

        /**
         * The edited start time in the media time sample in media timescale.
         * <p>
         * The value is 0 the track sample starts at the beginning of the
         * media sample.
         */
        long startTimeInMediaSample;
        /**
         * The edited end time in the media time sample in media timescale.
         * <p>
         * The value is mediaSample.duration if the track sample ends at the
         * end of the media sample.
         */
        long endTimeInMediaSample;

        /**
         * Creates a new sample.
         */
        public TrackSample(MediaSample mediaSample, long timeStamp, long duration,
                           long startTimeInMediaSample, long endTimeInMediaSample) {
            this.mediaSample = mediaSample;
            this.timeStamp = timeStamp;
            this.duration = duration;
            this.startTimeInMediaSample = startTimeInMediaSample;
            this.endTimeInMediaSample = endTimeInMediaSample;
        }
    }

    /**
     * Groups consecutive samples of the same duration.
     */
    protected static class TimeToSampleGroup extends Group {

        public TimeToSampleGroup(MediaSample firstSample) {
            super(firstSample);
        }

        protected TimeToSampleGroup(MediaSample firstSample, MediaSample lastSample, long sampleCount) {
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
        public boolean maybeAddSample(MediaSample sample) {
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

        public SampleSizeGroup(MediaSample firstSample) {
            super(firstSample);
        }

        public SampleSizeGroup(Group group) {
            super(group);
        }

        public SampleSizeGroup(MediaSample firstSample, MediaSample lastSample, long sampleCount) {
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
        public boolean maybeAddSample(MediaSample sample) {
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
         * @param sampleDescriptionId The description id of the sample.
         */
        public Chunk(MediaSample firstSample, int sampleDescriptionId) {
            super(firstSample);
            this.sampleDescriptionId = sampleDescriptionId;
        }

        /**
         * Creates a new Chunk.
         *
         * @param firstSample         The first sample contained in this chunk.
         * @param sampleDescriptionId The description Id of the sample.
         */
        public Chunk(MediaSample firstSample, MediaSample lastSample, int sampleCount, int sampleDescriptionId) {
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
        public boolean maybeAddSample(MediaSample sample, int sampleDescriptionId) {
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
         * Table of samples in this track.
         * <p>
         * This value is derived from the media data in this track,
         * and from the edit list of this track.
         * <p>
         * This value is set to null to indicate that it must be recomputed.
         * <dl>
         *     <dt>Map.key</dt><dd>absolute movie time of a track sample</dd>
         *     <dt>Map.value</dt><dd>track sample</dd>
         * </dl>
         */
        public NavigableMap<Long, ArrayList<TrackSample>> trackSampleMap = null;
        public ArrayList<TrackSample> trackSamplesList = null;
        public int readIndex;
        /**
         * The media type of the track.
         */
        protected FormatKeys.MediaType mediaType;
        /**
         * The fourcc type of the track.
         */
        protected String encoding;
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
        protected ArrayList<Edit> editList = new ArrayList<>();
        // END Edit List
        // BEGIN Media List
        /**
         * The media of the track.
         */
        public Media media = null;
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
         * Gets the track duration in the movie timescale.
         *
         * @param movieTimeScale The timescale of the movie.
         */
        public long getTrackDuration(long movieTimeScale) {
            long sum = 0;
            if (editList.isEmpty()) {
                Media m = media;
                sum += m.mediaDuration * movieTimeScale / m.mediaTimeScale;
            } else {
                for (Edit e : editList) {
                    sum += e.trackDuration;
                }
            }
            return sum;
        }

        /**
         * Builds the samples table for this track.
         */
        public void buildSamplesTable(long movieTimeScale) throws IOException {
            buildMediaSamplesTable(movieTimeScale);
            buildTrackSamplesTable(movieTimeScale);
        }

        public void buildTrackSamplesTable(long movieTimeScale) throws IOException {
            trackSampleMap = new TreeMap<>();
            trackSamplesList = new ArrayList<>();
            NavigableMap<Long, ArrayList<MediaSample>> mediaSamplesMap = media == null ? null : media.mediaSamples;
            if (mediaSamplesMap == null) {
                throw new IOException("track " + trackId + ": 'mdia' atom does not exist or is incomplete");
            }

            long mediaTimeScale = media.mediaTimeScale;
            if (editList.isEmpty()) {
                editList.add(new Edit(duration, 0, 1.0));
            }
            long editTrackTime = 0;
            for (final Edit edit : editList) {
                if (edit.mediaTime == -1) {
                    editTrackTime += edit.trackDuration;
                    continue;
                }
                double mediaRate = edit.mediaRate;
                long editMediaEndTime = (long) (edit.mediaTime + edit.trackDuration * mediaRate * mediaTimeScale / movieTimeScale);
                long sampleTrackTime = editTrackTime;
                double invMediaRate = 1.0 / edit.mediaRate;
                long mediaDuration = (long) (edit.trackDuration * mediaRate * mediaTimeScale);
                //long mediaDurationInMovieTimeScale = (long) (edit.trackDuration * mediaRate * mediaTimeScale / movieTimeScale);
                Long floorKey = mediaSamplesMap.floorKey(edit.mediaTime);
                floorKey = media.syncSamples == null ? floorKey : media.syncSamples.floor(floorKey);
                if (floorKey == null) {
                    // We do not have a key frame. Skip this edit.
                    editTrackTime += edit.trackDuration;
                    continue;
                }

                for (var entry : mediaSamplesMap.subMap(floorKey, edit.mediaTime + mediaDuration).entrySet()) {
                    ArrayList<MediaSample> mediaSamples = entry.getValue();
                    if (mediaSamples.isEmpty()) {
                        continue;
                    }
                    long mediaSampleTime = entry.getKey();

                    // if multiple samples have the same timestamp, then only the last one has a duration >=0
                    MediaSample lastMediaSample = mediaSamples.getLast();
                    long mediaSampleDuration = lastMediaSample.duration;
                    // cut duration if the media sample ends after the end time of the edit
                    long cutStart = Math.max(0, mediaSampleTime + mediaSampleDuration - editMediaEndTime);
                    // cut duration if the media sample starts before the start time of the edit
                    long cutEnd = Math.max(0, edit.mediaTime - mediaSampleTime);
                    mediaSampleDuration = Math.max(0, mediaSampleDuration - cutStart + cutEnd);

                    long trackSampleDuration = Math.max(0, (long) (mediaSampleDuration * invMediaRate * movieTimeScale / mediaTimeScale));

                    for (int i = 0, n = mediaSamples.size(); i < n - 1; i++) {
                        MediaSample mediaSample = mediaSamples.get(i);
                        TrackSample trackSample = new TrackSample(mediaSample, sampleTrackTime, 0, 0, 0);
                        trackSampleMap.computeIfAbsent(sampleTrackTime, k -> new ArrayList<>()).add(trackSample);
                        trackSamplesList.add(trackSample);
                    }
                    TrackSample trackSample = new TrackSample(lastMediaSample, sampleTrackTime, trackSampleDuration, cutStart, mediaSampleDuration);
                    trackSampleMap.computeIfAbsent(sampleTrackTime, k -> new ArrayList<>()).add(trackSample);
                    trackSamplesList.add(trackSample);

                    sampleTrackTime += trackSampleDuration;
                }
                editTrackTime += edit.trackDuration;
            }
        }

        public void buildMediaSamplesTable(long movieTimeScale) throws IOException {
            // XXX For PCM audio media, we must create one sample per chunk


            Media m = media;
            m.mediaSamples = new TreeMap<>();
            if (m.sampleSizes.isEmpty()) {
                throw new IOException("track " + trackId + ": 'mdia' atom does not contain an 'stsz' atom.");
            }
            if (m.samplesToChunks.isEmpty()) {
                throw new IOException("track " + trackId + ": 'mdia' atom does not contain an 'stsc' atom.");
            }
            if (m.timeToSamples.isEmpty()) {
                throw new IOException("track " + trackId + ": 'mdia' atom does not contain an 'stts' atom.");
            }
            if (m.chunkOffsets.isEmpty()) {
                throw new IOException("track " + trackId + ": 'mdia' atom does neither contain an 'stco' nor an 'co64' atom.");
            }

            TreeMap<Integer, SampleSizeGroup> sampleSizeMap = new TreeMap<>();
            int sampleIndex = 0;
            for (SampleSizeGroup ssg : m.sampleSizes) {
                sampleSizeMap.put(sampleIndex, ssg);
                sampleIndex += ssg.sampleCount;
            }
            TreeMap<Integer, SampleToChunk> sampleChunkMap = new TreeMap<>();
            sampleIndex = 0;
            int prevFirstChunk = 1;
            int prevSamplesPerChunk = 0;
            for (SampleToChunk stc : m.samplesToChunks) {
                sampleIndex += (stc.firstChunk - prevFirstChunk) * prevSamplesPerChunk;
                sampleChunkMap.put(sampleIndex, stc);
                prevFirstChunk = stc.firstChunk;
                prevSamplesPerChunk = stc.samplesPerChunk;
            }


            sampleIndex = 0;
            long time = 0;
            int firstChunkId = -1;
            int chunkId = -1;
            int remainingSamplesInChunk = 0;
            long offset = -1;
            for (TimeToSampleGroup tsg : m.timeToSamples) {
                long duration = tsg.getSampleDuration();
                for (int i = 0; i < tsg.sampleCount; i++) {
                    var sizeEntry = sampleSizeMap.floorEntry(sampleIndex);
                    long length = sizeEntry == null ? -1 : sizeEntry.getValue().getSampleLength();

                    if (remainingSamplesInChunk == 0) {
                        Map.Entry<Integer, SampleToChunk> chunkEntry = sampleChunkMap.floorEntry(sampleIndex);
                        if (chunkEntry == null) {
                            throw new IOException("track " + trackId + ": 'stsc' atom does not contain required chunk entry");
                        }
                        SampleToChunk stsc = chunkEntry.getValue();
                        if (stsc.firstChunk != firstChunkId) {
                            firstChunkId = stsc.firstChunk;
                            chunkId = stsc.firstChunk;
                        } else {
                            chunkId++;
                        }
                        remainingSamplesInChunk = stsc.samplesPerChunk;
                        if (chunkId < 0 || chunkId > m.chunkOffsets.size()) {
                            throw new IOException("track " + trackId + ": 'stco' or 'co64' atom does not contain an entry for chunkId=" + chunkId);
                        }
                        offset = m.chunkOffsets.get(chunkId - 1);
                    }

                    // We can have samples with zero duration.
                    MediaSample sample = new MediaSample(duration, offset, length);
                    sample.timeStamp = time;
                    sample.isKeyframe = m.syncSamples == null || m.syncSamples.contains((long) (sampleIndex));
                    m.mediaSamples.computeIfAbsent(time, k -> new ArrayList<>(1)).add(sample);

                    time += duration;
                    remainingSamplesInChunk--;
                    offset += length;
                    sampleIndex++;
                }
                time += duration;
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
                    + " media=" + media //
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
         * The timescale of the media in the track. A time value that indicates
         * the timescale for this media. That is, the number of time units that
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
        protected ArrayList<DataReference> dataReferenceList = new ArrayList<>();
        // END Data Reference List
        /**
         * List of chunk offsets.
         */
        protected ArrayList<Long> chunkOffsets = new ArrayList<>();
        /**
         * List of TimeToSample entries.
         */
        protected ArrayList<TimeToSampleGroup> timeToSamples = new ArrayList<>();
        /**
         * List of SampleToChunk entries.
         */
        protected ArrayList<SampleToChunk> samplesToChunks = new ArrayList<>();
        /**
         * List of SampleSize entries.
         */
        protected ArrayList<SampleSizeGroup> sampleSizes = new ArrayList<>();
        /**
         * List of sync samples. This list is null if all samples are sync samples.
         */
        protected NavigableSet<Long> syncSamples = null;
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
        // END Video Header
        // BEGIN Video Media Header
        boolean videoFlagNoLeanAhead;
        int graphicsMode;
        int[] opcolor = new int[3];
        // END Video Media Header

        private ArrayList<SampleDescription> sampleDescriptions = new ArrayList<>();
        /**
         * Table of samples in the media of this track.
         * <p>
         * This value is derived from the media data in this track.
         * <p>
         * This value is set to null to indicate that it must be recomputed.
         * <dl>
         *     <dt>Map.key</dt><dd>absolute media time of a media sample</dd>
         *     <dt>Map.value</dt><dd>media samples, may contain multiple
         *     entries, if there are samples with duration=0. In this
         *     case, only the last sample has a duration &gt;= 0.</dd>
         * </dl>
         */
        public NavigableMap<Long, ArrayList<MediaSample>> mediaSamples = null;

        public void addSampleDescription(SampleDescription d) {
            sampleDescriptions.add(d);
        }

        /*
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
        */
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
                    + ", soundBalance=" + soundBalance//
                    + ", dataReferenceList=" + dataReferenceList //
                    + ", chunks=" + chunkOffsets//
                    + ", timeToSamples=" + timeToSamples //
                    + ", sampleSizes=" + sampleSizes
                    + ", syncSamples=" + syncSamples
                    + ", sampleCount=" + sampleCount
                    + ", syncInterval=" + syncInterval
                    + ", videoQuality=" + videoQuality
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
        protected String dataFormat;
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
        protected byte[] extendData;
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
        public long trackDuration;
        /**
         * A 32-bit integer containing the start time within the media of this
         * edit segment (in media time scale units). If this field is set to -1,
         * it is an empty edit. The last edit in a track should never be an
         * empty edit. Any differece between the movie's duration and the
         * track's duration is expressed as an implicit empty edit.
         */
        public long mediaTime;
        /**
         * A 32-bit fixed-point number (16.16) that specifies the relative rate
         * at which to play the media corresponding to this edit segment. This
         * rate value cannot be 0 or negative.
         */
        public double mediaRate;

        /**
         * Creates an edit.
         *
         * @param trackDuration Duration of this edit in the movie's timescale.
         * @param mediaTime     Start time of this edit in the media's timescale.
         *                      Specify -1 for an empty edit. The last edit in a track should never
         *                      be an empty edit.
         * @param mediaRate     The relative rate at which to play this edit.
         */
        public Edit(long trackDuration, int mediaTime, double mediaRate) {
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

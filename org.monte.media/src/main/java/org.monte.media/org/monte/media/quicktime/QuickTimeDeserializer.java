/*
 * @(#)QuickTimeDeserializer.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime;

import org.monte.media.av.FormatKeys.MediaType;
import org.monte.media.io.ByteArrayImageInputStream;
import org.monte.media.qtff.AtomInputStream;
import org.monte.media.qtff.QTFFImageInputStream;
import org.monte.media.util.MathUtil;

import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.DataBuffer;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.InflaterInputStream;

/**
 * {@code QuickTimeDeserializer}. This is an internal class of
 * QuickTimeInputStream.
 *
 * @author Werner Randelshofer
 */
public class QuickTimeDeserializer {

    static final Set<String> compositeAtoms = Set.of(
            "moov",
            "cmov",
            "gmhd",
            "trak",
            "tref",
            "meta",// sometimes has a special 4 byte header before its contents
            "ilst",
            "mdia",
            "minf",
            "udta",
            "stbl",
            "dinf",
            "edts",
            "clip",
            "matt",
            "rmra",
            "rmda",
            "tapt",
            "mvex"
    );

    private static class Atom {

        public long offset;
        public long size;
        public String type;
        public long headerSize;
        public byte[] data;
        public HashMap<String, Atom> children = new HashMap<>();

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append(type);
            b.append(':');
            b.append(Long.toString(size));
            b.append('@');
            b.append(Long.toString(offset));
            return b.toString();
        }
    }

    public QuickTimeMeta read(URI uri) throws IOException {
        QuickTimeMeta m = new QuickTimeMeta();
        QTFFImageInputStream in = new QTFFImageInputStream(new FileImageInputStream(new File(uri)));
        parse(in, m);
        return m;
    }

    public QuickTimeMeta read(ImageInputStream iin) throws IOException {
        QuickTimeMeta m = new QuickTimeMeta();
        QTFFImageInputStream in = new QTFFImageInputStream(iin);
        parse(in, m);
        return m;
    }

    /**
     * Parses a QuickTime file. This method invokes other parse methods for
     * individual data structures in the file.
     */
    protected void parse(QTFFImageInputStream in, QuickTimeMeta m) throws IOException {
        parseRecursively(in, in.length(), m);
        for (QuickTimeMeta.Track track : m.tracks) {
            track.buildSamplesTable(m.timeScale);
        }
    }

    /**
     * Parses a QuickTime file. This method invokes other parse methods for
     * individual data structures in the file.
     *
     * <pre>
     * struct atom {
     *    uint32 size;
     *    type   type;  // exists only if size &gt;= 8
     *    byte[size-8] body; // exists only if size &gt; 8
     *
     * }
     * </pre>
     */
    protected void parseRecursively(QTFFImageInputStream in, long remainingSize, QuickTimeMeta m) throws IOException {
        while (remainingSize > 0) {
            Atom atom = new Atom();
            atom.offset = in.getStreamPosition();
            atom.size = in.readInt() & 0xffffffffL;
            if (atom.size >= 8) {
                atom.type = in.readType();
                atom.headerSize = 8;
            } else {
                atom.type = "free";
                atom.headerSize = 4;
            }

            if (atom.size == 0) {
                // A zero size indicates that the size is the remainder of
                // the parent atom.
                atom.size = remainingSize;

                // skip 4 bytes after zero size.
                if (atom.headerSize + 4 <= remainingSize) {
                    in.skipBytes(4);
                    atom.headerSize += 4;
                }
            } else if (atom.size == 1) {
                // A size of 1 indicates an extended size field.
                atom.headerSize = 16;
                atom.size = in.readLong();
            }

            if (atom.size > remainingSize) {
                //truncate size
                atom.size = remainingSize;
            }

            String t = atom.type;

            if (compositeAtoms.contains(atom.type)) {
                // Perform pre-processing
                if ("trak".equals(t)) {
                    m.tracks.add(new QuickTimeMeta.Track());
                } else if ("mdia".equals(t)) {
                    m.tracks.get(m.getTrackCount() - 1).media = new QuickTimeMeta.Media();
                }
                // Perform recursion:
                parseRecursively(in, atom.size - atom.headerSize, m);
            } else {
                QuickTimeMeta.Track track = (m.tracks.isEmpty()) ? null : m.tracks.get(m.tracks.size() - 1);
                QuickTimeMeta.Media media = (track == null) ? null : track.media;

                if (null != t) {
                    switch (t) {
                        case "ftyp":
                            parseFileType(in, atom.size - atom.headerSize, m);
                            break;
                        case "wide":
                            break;
                        case "mdat":
                            parseMovieData(in, atom.size - atom.headerSize, m);
                            break;
                        case "mvhd":
                            parseMovieHeader(in, atom.size - atom.headerSize, m);
                            break;
                        case "dcom":
                            parseDataCompressionAtom(in, atom.size - atom.headerSize, m);
                            break;
                        case "cmvd":
                            parseCompressedMovieAtom(in, atom.size - atom.headerSize, m);
                            break;
                        case "tkhd":
                            parseTrackHeader(in, atom.size - atom.headerSize, track);
                            break;
                        case "elst":
                            parseEditList(in, atom.size - atom.headerSize, track);
                            break;
                        case "mdhd":
                            parseMediaHeader(in, atom.size - atom.headerSize, media);
                            break;
                        case "hdlr":
                            parseHandlerReference(in, atom.size - atom.headerSize, track, media);
                            break;
                        case "smhd":
                            parseSoundMediaHeader(in, atom.size - atom.headerSize, media);
                            break;
                        case "dref":
                            parseDataReference(in, atom.size - atom.headerSize, media);
                            break;
                        case "stsd":
                            if (track == null || track.mediaType == null) {
                                break;
                            }
                            switch (track.mediaType) {
                                case AUDIO:
                                    parseSoundSampleDescription(in, atom.size - atom.headerSize, media);
                                    break;
                                case VIDEO:
                                    parseVideoSampleDescription(in, atom.size - atom.headerSize, media);
                                    break;
                                default:
                                    parseGenericSampleDescription(in, atom.size - atom.headerSize, media);
                                    break;
                            }
                            break;
                        case "vmhd":
                            parseVideoMediaHeader(in, atom.size - atom.headerSize, media);
                            break;
                        case "stts":
                            parseTimeToSample(in, atom.size - atom.headerSize, media);
                            break;
                        case "stsc":
                            parseSampleToChunk(in, atom.size - atom.headerSize, media);
                            break;
                        case "stco":
                            parseChunkOffsets(in, atom.size - atom.headerSize, media);
                            break;
                        case "co64":
                            parseChunkOffsets64(in, atom.size - atom.headerSize, media);
                            break;
                        case "stss":
                            parseSyncSample(in, atom.size - atom.headerSize, media);
                            break;
                        case "stsz":
                            parseSampleSize(in, atom.size - atom.headerSize, media);
                            break;
                        case "ctab":
                            parseColorTable(in, atom.size - atom.headerSize, m);
                            break;
                        default:
                            atom.data = new byte[(int) (atom.size - atom.headerSize)];
                            in.read(atom.data);
                            break;
                    }
                }

                in.seek(atom.offset + atom.size);
            }

            remainingSize -= atom.size;
        }
    }

    /**
     * The file type ("ftyp"-atom).
     * <pre>
     * typedef struct {
     * magic brand;
     * bcd4 versionYear;
     * bcd2 versionMonth;
     * bcd2 versionMinor;
     * magic[] compatibleBrands;
     * } ftypAtom;
     * </pre>
     */
    protected void parseFileType(QTFFImageInputStream in, long remainingSize, QuickTimeMeta m) throws IOException {
        m.brand = in.readType();
        m.versionYear = in.readUnsignedBCD4();
        m.versionMonth = in.readUnsignedBCD2();
        m.versionMinor = in.readUnsignedBCD2();
        m.compatibleBrands.clear();
        remainingSize -= 10;
        while (remainingSize > 4) {
            m.compatibleBrands.add(in.readType());
            remainingSize -= 4;
        }
    }

    /**
     * <p>
     * The data compression atom ("dcom"-atom) specifies how the 'cmvd' atom
     * is compressed.
     * <pre>
     * typedef struct {
     *      type compressionMethod;
     * } dataCompressionAtom;
     * </pre>
     */
    protected void parseDataCompressionAtom(QTFFImageInputStream in, long remainingSize, QuickTimeMeta m) throws IOException {
        if (remainingSize != 4) return;
        m.compressionMethod = in.readType();
    }


    /**
     * The compressed movie data atom ("cmvd"-atom) contains a compressed
     * 'moov' atom.
     * <pre>
     * typedef struct {
     *      uint32 sizeOfDecompressedData;
     *      byte[] compressedData;
     * } cmvdAtom.
     * <p>
     */
    protected void parseCompressedMovieAtom(QTFFImageInputStream in, long remainingSize, QuickTimeMeta m) throws IOException {
        int sizeOfDecompressedData = remainingSize > 4 ? in.readInt() : -1;
        if (sizeOfDecompressedData > 0 && "zlib".equals(m.compressionMethod)) {
            // decompress the header into a byte array and then parse it
            byte[] compressed = new byte[(int) remainingSize - 4];
            in.readFully(compressed);
            var inflater = new InflaterInputStream(new ByteArrayInputStream(compressed));
            byte[] decompressedBytes = new byte[sizeOfDecompressedData];
            int remaining = decompressedBytes.length;
            int off = 0;
            while (remaining > 0) {
                int read = inflater.read(decompressedBytes, off, remaining);
                if (read < 0) break;
                off += read;
                remaining -= read;
            }
            QTFFImageInputStream decompressed = new QTFFImageInputStream(new ByteArrayImageInputStream(decompressedBytes));
            parseRecursively(decompressed, sizeOfDecompressedData, m);
        }
    }

    /**
     * The movie data ("mdat"-atom).
     * <pre>
     * typedef struct {
     *      byte[] data;
     * } movieDataAtom;
     * </pre>
     */
    protected void parseMovieData(QTFFImageInputStream in, long remainingSize, QuickTimeMeta m) throws IOException {
        m.movieDataStreamPosition = in.getStreamPosition();
        m.movieDataSize = remainingSize;
    }


    /**
     * The movie header ("mvhd"-atom).
     * <pre>
     * typedef struct {
     *      byte version;
     *      byte[3] flags;
     *      mactimestamp creationTime;
     *      mactimestamp modificationTime;
     *      uint timeScale;
     *      uint duration;
     *      fixed16d16 preferredRate;
     *      fixed8d8 preferredVolume;
     *      byte[10] reserved;
     *      fixed16d16 matrixA;
     *      fixed16d16 matrixB;
     *      fixed2d30 matrixU;
     *      fixed16d16 matrixC;
     *      fixed16d16 matrixD;
     *      fixed2d30 matrixV;
     *      fixed16d16 matrixX;
     *      fixed16d16 matrixY;
     *      fixed2d30 matrixW;
     *      uint previewTime;
     *      uint previewDuration;
     *      uint posterTime;
     *      uint selectionTime;
     *      uint selectionDuration;
     *      uint currentTime;
     *      uint nextTrackId;
     * } movieHeaderAtom;
     * </pre>
     */
    protected void parseMovieHeader(QTFFImageInputStream in, long remainingSize, QuickTimeMeta m) throws IOException {
        if (remainingSize < 100) return;
        int version = in.readUnsignedByte();
        if (version != 0) return;
        in.skipBytes(3);
        m.creationTime = in.readMacTimestamp();
        m.modificationTime = in.readMacTimestamp();
        m.timeScale = in.readUnsignedInt();
        m.duration = in.readUnsignedInt();
        m.preferredRate = in.readFixed16D16();
        m.preferredVolume = in.readFixed8D8();
        in.skipBytes(10);
        m.matrix[0] = in.readFixed16D16();
        m.matrix[1] = in.readFixed16D16();
        m.matrix[2] = in.readFixed2D30();
        m.matrix[3] = in.readFixed16D16();
        m.matrix[4] = in.readFixed16D16();
        m.matrix[5] = in.readFixed2D30();
        m.matrix[6] = in.readFixed16D16();
        m.matrix[7] = in.readFixed16D16();
        m.matrix[8] = in.readFixed2D30();
        m.previewTime = in.readUnsignedInt();
        m.previewDuration = in.readUnsignedInt();
        m.posterTime = in.readUnsignedInt();
        m.selectionTime = in.readUnsignedInt();
        m.selectionDuration = in.readUnsignedInt();
        m.currentTime = in.readUnsignedInt();
        m.nextTrackId = in.readUnsignedInt();
    }

    /**
     * <p>
     * The track header ("tkhd"-atom).
     * <pre>
     * Enumeration for track header flags
     * set {
     *    TrackEnable = 0x1, // enabled track
     *    TrackInMovie = 0x2, // track in playback
     *    TrackInPreview = 0x4, // track in preview
     *    TrackInPoster = 0x8 // track in poster
     * } TrackHeaderFlags;
     *
     * typedef struct {
     *    byte version;
     *    byte flag0;
     *    byte flag1;
     *   byte set TrackHeaderFlags flag2;
     *    mactimestamp creationTime;
     *    mactimestamp modificationTime;
     *    int trackId;
     *    byte[4] reserved;
     *    int duration;
     *    byte[8] reserved;
     *    short layer;
     *    short alternateGroup;
     *    fixed8d8 volume;
     *    byte[2] reserved;
     *    fixed16d16 matrixA;
     *    fixed16d16 matrixB;
     *    fixed2d30 matrixU;
     *    fixed16d16 matrixC;
     *    fixed16d16 matrixD;
     *    fixed2d30 matrixV;
     *    fixed16d16 matrixY;
     *    fixed2d30 matrixW;
     *    fixed16d16 trackWidth;
     *    fixed16d16 trackHeight;
     * trackHeaderAtom;
     * </pre>
     */
    protected void parseTrackHeader(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Track t) throws IOException {
        int version = in.readUnsignedByte();
        if (version != 0) return;
        in.skipBytes(2);
        t.headerFlags = in.readUnsignedByte();
        t.creationTime = in.readMacTimestamp();
        t.modificationTime = in.readMacTimestamp();
        t.trackId = in.readInt();
        in.skipBytes(4);
        t.duration = in.readUnsignedInt();
        in.skipBytes(8);
        t.layer = in.readUnsignedShort();
        t.alternateGroup = in.readUnsignedShort();
        t.volume = in.readFixed8D8();
        in.skipBytes(2);
        t.matrix[0] = in.readFixed16D16();
        t.matrix[1] = in.readFixed16D16();
        t.matrix[2] = in.readFixed2D30();
        t.matrix[3] = in.readFixed16D16();
        t.matrix[4] = in.readFixed16D16();
        t.matrix[5] = in.readFixed2D30();
        t.matrix[6] = in.readFixed16D16();
        t.matrix[7] = in.readFixed16D16();
        t.matrix[8] = in.readFixed2D30();
        t.width = in.readFixed16D16();
        t.height = in.readFixed16D16();

        remainingSize -= 84;
    }

    /**
     * The edit list ("elst"-Atom).
     *
     * <pre>
     * typedef struct {
     *    byte version;
     *    byte[3] flags;
     *    uint numberOfEntries;
     *    editListTable editListTable[numberOfEntries];
     * } editListAtom;
     *
     * typedef struct {
     *    int trackDuration;
     *    int mediaTime;
     *    fixed16d16 mediaRate;
     * } editListTable;
     * </pre>
     */
    protected void parseEditList(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Track t) throws IOException {
        int version = in.readUnsignedByte();
        in.skipBytes(3);
        int numberOfEntries = (int) in.readUnsignedInt();
        for (int i = 0; i < numberOfEntries; i++) {
            QuickTimeMeta.Edit edit = new QuickTimeMeta.Edit(//
                    in.readInt(),//
                    in.readInt(),//
                    in.readFixed16D16()//
            );
            t.editList.add(edit);
        }

        remainingSize -= 8 + numberOfEntries * 12L;
    }

    /**
     * The media header (mdhd-Atom).
     * <pre>
     * typedef struct {
     * byte version;
     * byte[3] flags;
     * mactimestamp creationTime;
     * mactimestamp modificationTime;
     * int timeScale;
     * int duration;
     * short language;
     * short quality;
     * } mediaHeaderAtom;
     * </pre>
     */
    protected void parseMediaHeader(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        in.skipBytes(3);
        m.mediaCreationTime = in.readMacTimestamp();
        m.mediaModificationTime = in.readMacTimestamp();
        m.mediaTimeScale = in.readUnsignedInt();
        m.mediaDuration = in.readUnsignedInt();
        int languageCode = in.readUnsignedShort();
        if (languageCode < 0x800) {
            m.mediaLanguageEncoding = "MacRoman";

            if (languageCode < QuickTimeMeta.LANGUAGE_CODES.length) {
                m.mediaLanguage = QuickTimeMeta.LANGUAGE_CODES[languageCode];
            } else {
                // language is undetermined
                m.mediaLanguage = null;
            }
        } else {
            m.mediaLanguageEncoding = "UTF-8";
            /*
             * One algorithm for performing this packing is to treat each ISO
             * character as a 16-bit integer.
             * Subtract 0x60 from the first character and multiply by
             * 2^10 (0x400),
             * subtract 0x60 from the second character and multiply by
             * 2^5 (0x20),
             * subtract 0x60 from the third character,
             * and add the three 16-bit values.
             * This will result in a single 16-bit value with the three codes
             * correctly packed into the 15 least significant bits and the
             * most significant bit set to zero.
             */
            if ((languageCode & 0x8000) == 0) {

                char[] isochars = {//
                        (char) (((languageCode >>> 10) & 0x1f) + 0x60),//
                        (char) (((languageCode >>> 5) & 0x1f) + 0x60),//
                        (char) (((languageCode >>> 0) & 0x1f) + 0x60),//
                };
                String iso = new String(isochars);

                // "und" means undetermined
                m.mediaLanguage = "und".equals(iso) ? null : Locale.forLanguageTag(iso);
            } else {
                m.mediaLanguage = null;
            }
        }
        m.mediaQuality = in.readShort();
    }

    /**
     * Handler Reference Atom ("hdlr"-Atom).
     * <pre>
     * typedef struct {
     * byte version;
     * byte[3] flags;
     * magic componentType;
     * magic componentSubtype;
     * magic componentManufacturer;
     * int componentFlags;
     * int componentFlagsMask;
     * pstring componentName;
     * ubyte[] extraData;
     * } handlerReferenceAtom;
     * </pre>
     */
    protected void parseHandlerReference(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Track t, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        in.skipBytes(3);
        String componentType = in.readType();
        String componentSubtype = in.readType();
        String componentManufacturer = in.readType();
        int componentFlags = in.readInt();
        int componentFlagsMask = in.readInt();
        String componentName = in.readPString();

        if ("mhlr".equals(componentType)) {
            switch (componentSubtype) {
                case "vide" -> t.mediaType = MediaType.VIDEO;
                case "soun" -> t.mediaType = MediaType.AUDIO;
                case "midi" -> t.mediaType = MediaType.MIDI;
                case "text", "clcp" -> t.mediaType = MediaType.TEXT;
                case "meta" -> t.mediaType = MediaType.META;
                case "sprt" -> t.mediaType = MediaType.SPRITE;
                default -> t.mediaType = MediaType.UNKNOWN;
            }
        } else if ("dhlr".equals(componentType)) {
            // FIXME - is "dhlr" useful at all?
        }
        //ignore extraData
    }

    /**
     * The sound media header ("smhd"-Atom).
     * <pre>
     * typedef struct {
     *    ubyte version;  // A 1-byte specification of the version of this sound media information header atom.
     *    byte[3] flags; // A 3-byte space for sound media information flags. Set this field to 0.
     *    fixed8d8 balance;  // A 16-bit integer that specifies the sound balance of this
     *                    // sound media. Sound balance is the setting that controls
     *                    // the mix of sound between the two speakers of a computer.
     *                    // This field is normally set to 0.
     *                    // Balance values are represented as 16-bit, fixed-point
     *                    // numbers that range from -1.0 to +1.0. The high-order 8
     *                    // bits contain the integer portion of the value; the
     *                    // low-order 8 bits contain the fractional part. Negative
     *                    // values weight the balance toward the left speaker;
     *                    // positive values emphasize the right channel. Setting the
     *                    // balance to 0 corresponds to a neutral setting.
     *    short reserved; // Reserved for use by Apple. Set this field to 0.
     * } soundMediaInformationHeaderAtom;
     * </pre>
     */
    protected void parseSoundMediaHeader(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        in.skipBytes(3);
        m.soundBalance = in.readFixed8D8();
        in.skipBytes(2);
    }

    /**
     * The video media header ("vmhd"-Atom).
     * <pre>
     * set {
     * videoFlagNoLeanAhead=1 // I am not shure if this is the correct value for this flag
     * } vmhdFlags;
     *
     * enum {
     * Copy = 0x0,
     * DitherCopy = 0x40,
     * Blend = 0x20, // uses opcolor
     * Transparent = 0x24, // uses opcolor
     * StraightAlpha = 0x100,
     * PremulWhiteAlpha = 0x101,
     * PremulBlackAlpha = 0x102,
     * StraightAlphaBlend = 0x104, // uses opclor
     * Composition = 0x103
     * } GraphicsModes;
     *
     * typedef struct {
     * byte version;
     * byte flag1;
     * byte flag2;
     * byte set vmhdFlags flag3;
     * short enum GraphicsModes graphicsMode;
     * ushort[3] opcolor;
     * } videoMediaInformationHeaderAtom;
     * </pre>
     */
    protected void parseVideoMediaHeader(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        if (version != 0 || remainingSize != 12) return;
        in.skipBytes(2);
        int vmhdFlags = in.readUnsignedByte();

        m.videoFlagNoLeanAhead = (vmhdFlags & 1) != 0;

        m.graphicsMode = in.readUnsignedShort();
        for (int i = 0; i < 3; i++) {
            m.opcolor[i] = in.readUnsignedShort();
        }
    }

    /**
     * The data reference ("dref"-Atom).
     * <pre>
     * set {
     *   dataRefSelfReference=1 // I am not shure if this is the correct value for this flag
     * } drefEntryFlags;
     *
     * typedef struct {
     *  uint size;
     *  magic type;
     *  byte version;
     *  ubyte flag1;
     *  ubyte flag2;
     *  ubyte set drefEntryFlags flag3;
     *  ubyte[size - 12] data;
     * } dataReferenceEntry;
     *
     * typedef struct {
     *  ubyte version;
     *  ubyte[3] flags;
     *  int numberOfEntries;
     *  dataReferenceEntry dataReference[numberOfEntries];
     * } dataReferenceAtom;
     *
     * </pre>
     */
    protected void parseDataReference(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        in.skipBytes(3);
        int numberOfEntries = in.readInt();
        for (int i = 0; i < numberOfEntries; i++) {
            QuickTimeMeta.DataReference dref = new QuickTimeMeta.DataReference();
            long size = in.readUnsignedInt();
            dref.referenceType = in.readType();
            version = in.readUnsignedByte();
            in.skipBytes(2);
            dref.referenceFlags = in.readUnsignedByte();
            dref.data = new byte[(int) (size - 12)];
            in.readFully(dref.data);
            m.dataReferenceList.add(dref);
        }
    }

    /**
     * The sound sample description ("stsd"-Atom in a sound track).
     *
     * <pre>
     * enum {
     * version0 = 0, // compressionId must be 0 for version 0 sound sample description.
     * uncompressedAudio = -1,
     * compressedAudio = -2,
     * } soundSampleCompressionId;
     *
     *
     * typedef struct {
     *  ushort version;     // A 16-bit integer that holds the sample description
     *                      // version (currently 0 or 1).
     *  ushort revisionLevel;// A 16-bit integer that must be set to 0.
     *  uint vendor;        // A 32-bit integer that must be set to 0.
     *  ushort numberOfChannels;
     *                      // A 16-bit integer that indicates the number of sound
     *                      // channels used by the sound sample. Set to 1 for
     *                      // monaural sounds, 2 for stereo sounds.
     *                      // Higher numbers of channels are not supported.
     * ushort sampleSize;  // A 16-bit integer that specifies the number of bits in
     * // each uncompressed sound sample. Allowable values are
     * // 8 or 16. Formats using more than 16 bits per sample
     * // set this field to 16 and use sound description
     * // version 1.
     * short enum soundSampleCompressionId compressionId;// A 16-bit integer that must be set to 0 for version 0
     * // sound descriptions. This may be set to –2 for some
     * // version 1 sound descriptions; see “Redefined Sample
     * // Tables” (page 115).
     * ushort packetSize;  // A 16-bit integer that must be set to 0.
     *  fixed16d16 sampleRate;// A 32-bit unsigned fixed-point number (16.16) that
     *  // indicates the rate at which the sound samples were
     *  // obtained. The integer portion of this number should
     *  // match the media’s time scale. Many older version 0
     *  // files have values of 22254.5454 or 11127.2727, but
     *  // most files have integer values, such as 44100. Sample
     *  // rates greater than 2^16 are not supported.
     *  soundSampleDescriptionV1[version] v1; // Additional fields for version 1
     *  soundSampleDescriptionExtension[] extendedData;
     * } soundSampleDescription;
     *
     * typedef struct {
     *  int size; // A 32-bit integer indicating the number of bytes in the sample description.
     *  magic type;         // A 32-bit integer indicating the format of the stored data.
     *  // This depends on the media type, but is usually either the
     *  // compression format or the media type.
     *  byte[6] reserved; // six bytes that must be zero
     *  short dataReferenceIndex; // A 16-bit integer that contains the index of the data reference to use to retrieve data associated with samples that use this sample description. Data references are stored in data reference atoms.
     *  soundSampleDescription data;
     * } soundSampleDescriptionEntry;
     *
     * typedef struct {
     * uint samplesPerPacket;
     * // This field is only present if version == 1.
     * // A 32-bit integer.
     * // The number of uncompressed frames generated by a
     * // compressed frame (an uncompressed frame is one sample
     * // from each channel). This is also the frame duration,
     * // expressed in the media’s timescale, where the
     * // timescale is equal to the sample rate. For
     * // uncompressed formats, this field is always 1.
     * uint bytesPerPacket;
     * // This field is only present if version == 1.
     * // A 32-bit integer.
     * // For uncompressed audio, the number of bytes in a
     * // sample for a single channel. This replaces the older
     * // sampleSize field, which is set to 16.
     * // This value is calculated by dividing the frame size
     * // by the number of channels. The same calculation is
     * // performed to calculate the value of this field for
     * // compressed audio, but the result of the calculation
     * // is not generally meaningful for compressed audio.
     * uint bytesPerFrame;
     * // This field is only present if version == 1.
     * // A 32-bit integer.
     * // The number of bytes in a frame: for uncompressed
     * // audio, an uncompressed frame; for compressed audio, a
     * // compressed frame. This can be calculated by
     * // multiplying the bytes per packet field by the number
     * // of channels.
     * uint bytesPerSample;
     * // This field is only present if version == 1.
     * // A 32-bit integer.
     * // The size of an uncompressed sample in bytes. This is
     * // set to 1 for 8-bit audio, 2 for all other cases, even
     * // if the sample size is greater than 2 bytes.
     * } soundSampleDescriptionV1;
     *
     * typedef struct {
     *  uint dataSize;
     *  magic type;
     *  ubyte[dataSize - 8] data;
     * } soundSampleDescriptionExtension;
     *
     * typedef struct {
     * byte version; // A 1-byte specification of the version of this sample description atom.
     * byte[3] flags; // A 3-byte space for sample description flags. Set this field to 0.
     * int numberOfEntries; // A 32-bit integer containing the number of sample descriptions that follow.
     * soundSampleDescriptionEntry sampleDescriptionTable[numberOfEntries];
     * } soundSampleDescriptionAtom;
     * </pre>
     */
    protected void parseSoundSampleDescription(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        if (version != 0) throw new IOException("unsupported stsd version=" + version);
        in.skipBytes(3);
        int numberOfEntries = in.readInt();
        remainingSize -= 12;

        for (int i = 0; i < numberOfEntries; i++) {
            final QuickTimeMeta.SampleDescription d = new QuickTimeMeta.SampleDescription();
            m.addSampleDescription(d);

            int size = in.readInt();
            remainingSize -= size;
            int remainingEntrySize = size;
            d.dataFormat = in.readType();
            in.skipBytes(6);
            d.dataReferenceIndex = in.readUnsignedShort();

            int descriptionVersion = in.readUnsignedShort();
            int revisionLevel = in.readUnsignedShort();
            int vendor = in.readInt();
            d.soundNumberOfChannels = in.readUnsignedShort();
            d.soundSampleSize = in.readUnsignedShort();
            d.soundCompressionId = in.readShort();
            int packetSize = in.readUnsignedShort();
            d.soundSampleRate = in.readFixed16D16();
            remainingEntrySize -= 38;

            if (descriptionVersion == 1) {
                d.soundSamplesPerPacket = in.readUnsignedInt();
                d.soundBytesPerPacket = in.readUnsignedInt();
                d.soundBytesPerFrame = in.readUnsignedInt();
                d.soundBytesPerSample = in.readUnsignedInt();
                remainingEntrySize -= 16;
            } else {
                d.soundBytesPerFrame = ((d.soundNumberOfChannels * d.soundSampleSize) + 7) / 8;
            }

            while (remainingEntrySize > 0) {
                long atomSize = in.readUnsignedInt();
                if (atomSize < 4) {
                    remainingEntrySize -= 4;
                    continue;
                }
                if (atomSize < 8) {
                    in.skipBytes(atomSize - 4);
                    remainingEntrySize -= atomSize;
                    continue;
                }
                String atomType = in.readType();
                System.out.println("stsd  atom:" + atomType);
                // FIXME Parse Atom
                byte[] atomData = new byte[(int) (atomSize - 8)];
                in.readFully(atomData);

                remainingEntrySize -= atomSize;
            }
        }
    }

    /**
     * The sample description ("stsd"-Atom in generic tracks).
     *
     * <pre>
     * typedef struct {
     *  byte version;
     *  byte[3] flags;
     *  int numberOfEntries;
     *  sampleDescriptionEntry sampleDescriptionTable[numberOfEntries];
     * } videoSampleDescriptionAtom;
     *
     * typedef struct {
     *  int size;
     *  magic dataFormat; // A 32-bit integer indicating the format of the stored data. This depends on the media type, but is usually either the compression format or the media type.
     *  byte[6] reserved; // six bytes that must be zero
     *  short dataReferenceIndex; // A 16-bit integer that contains the index of the data reference to use to retrieve data associated with samples that use this sample description. Data references are stored in data reference atoms.
     *  byte[size-16] data;
     * } sampleDescriptionEntry;
     * </pre>
     */
    protected void parseGenericSampleDescription(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        if (version != 0) throw new IOException("unsupported stsd version=" + version);
        in.skipBytes(3);
        int numberOfEntries = in.readInt();
        remainingSize -= 8;
        for (int i = 0; i < numberOfEntries; i++) {
            final QuickTimeMeta.SampleDescription d = new QuickTimeMeta.SampleDescription();
            m.addSampleDescription(d);

            int size = in.readInt();
            remainingSize -= size;
            d.dataFormat = in.readType();
            in.skipBytes(6);
            d.dataReferenceIndex = in.readUnsignedShort();
            d.extendData = new byte[size - 16];
            in.readFully(d.extendData);
        }
    }


    /**
     * The video sample description ("stsd"-Atom in a video track).
     *
     * <pre>
     * typedef struct {
     *  byte version;
     *  byte[3] flags;
     *  int numberOfEntries;
     *  videoSampleDescriptionEntry sampleDescriptionTable[numberOfEntries];
     * } videoSampleDescriptionAtom;
     *
     * typedef struct {
     *  int size;
     *  magic type;
     *  byte[6] reserved; // six bytes that must be zero
     *  short dataReferenceIndex; // A 16-bit integer that contains the index of the data reference to use to retrieve data associated with samples that use this sample description. Data references are stored in data reference atoms.
     *  videoSampleDescription data;
     * } videoSampleDescriptionEntry;
     *
     * typedef struct {
     *  ushort version;     // A 16-bit integer indicating the version number of the
     *                      // compressed data. This is set to 0, unless a
     *                      // compressor has changed its data format.
     *  ushort revisionLevel;// A 16-bit integer that must be set to 0.
     *  magic vendor;       // A 32-bit integer that specifies the developer of the
     *                      // compressor that generated the compressed data. Often
     *                      //  this field contains 'appl' to indicate Apple
     *                      // Computer, Inc.
     *  uint temporalQuality;// A 32-bit integer containing a value from 0 to 1023
     *                       // indicating the degree of temporal compression.
     *  uint spatialQuality; // A 32-bit integer containing a value from 0 to 1024
     *                       // indicating the degree of spatial compression.
     *  ushort width;       // A 16-bit integer that specifies the width of the
     *                      // source image in pixels.
     *  ushort height;      // A 16-bit integer that specifies the height of the
     *                      // source image in pixels.
     *  fixed16d16 horizontalResolution;
     *                      // A 32-bit fixed-point number containing the horizontal
     *                      // resolution of the image in pixels per inch.
     *  fixed16d16 verticalResolution;
     *                      // A 32-bit fixed-point number containing the vertical
     *                      // resolution of the image in pixels per inch.
     *  uint dataSize;      // A 32-bit integer that must be set to 0.
     *  ushort frameCount;  // A 16-bit integer that indicates how many frames of
     *                      // compressed data are stored in each sample. Usually
     *                      // set to 1.
     *  pstring32 compressorName;// A 32-byte Pascal string containing the name of the
     *                      // compressor that created the image, such as "jpeg".
     *  ushort depth;       // A 16-bit integer that indicates the pixel depth of
     *                      // the compressed image. Values of 1, 2, 4, 8 ,16, 24,
     *                      // and 32 indicate the depth of color images. The value
     *                      // 32 should be used only if the image contains an alpha
     *                      // channel. Values of 34, 36, and 40 indicate 2-, 4-,
     *                      // and 8-bit grayscale, respectively, for grayscale
     *                      // images.
     *  ushort colorTableId; // A 16-bit integer that identifies which color table to
     *      // use. If this field is set to –1, the default color
     *      // table should be used for the specified depth. For all
     *      // depths below 16 bits per pixel, this indicates a
     *      // standard Macintosh color table for the specified
     *      // depth. Depths of 16, 24, and 32 have no color table.
     *      // If the color table ID is set to 0, a color table is
     *      // contained within the sample description itself. The
     *      // color table immediately follows the color table ID
     *      // field in the sample description. See “Color Table
     *      // Atoms” (page 41) for a complete description of a color table.
     *
     *  videoSampleDescriptionExtension[] extendedData;
     * } videoSampleDescription;
     *
     * typedef struct {
     *  uint dataSize;
     *  magic type;
     *  ubyte[dataSize - 8] data;
     * } videoSampleDescriptionExtension;
     *
     * typedef struct {
     * byte version; // A 1-byte specification of the version of this sample description atom.
     * byte[3] flags; // A 3-byte space for sample description flags. Set this field to 0.
     * int numberOfEntries; // A 32-bit integer containing the number of sample descriptions that follow.
     * soundSampleDescriptionEntry sampleDescriptionTable[numberOfEntries];
     * } soundSampleDescriptionAtom;
     * </pre>
     */
    protected void parseVideoSampleDescription(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        if (version != 0) throw new IOException("unsupported stsd version=" + version);
        in.skipBytes(3);
        int numberOfEntries = in.readInt();
        remainingSize -= 8;
        for (int i = 0; i < numberOfEntries; i++) {
            final QuickTimeMeta.SampleDescription d = new QuickTimeMeta.SampleDescription();
            m.addSampleDescription(d);

            int size = in.readInt();
            remainingSize -= size;
            d.dataFormat = in.readType();
            in.skipBytes(6);
            d.dataReferenceIndex = in.readUnsignedShort();

            int descriptionVersion = in.readUnsignedShort();
            int revisionLevel = in.readUnsignedShort();
            int vendor = in.readInt();
            float value1 = in.readInt() / 1024f;

            d.videoTemporalQuality = MathUtil.clamp(value1, 0.0f, 1.0f);
            float value = in.readInt() / 1024f;

            d.videoSpatialQuality = MathUtil.clamp(value, 0.0f, 1.0f);
            d.videoWidth = in.readUnsignedShort();
            d.videoHeight = in.readUnsignedShort();
            d.videoHorizontalResolution = in.readFixed16D16();
            d.videoVerticalResolution = in.readFixed16D16();
            long dataSize = in.readUnsignedInt();
            d.videoFrameCount = in.readUnsignedShort();
            d.videoCompressorName = in.readPString(32);
            d.videoDepth = in.readUnsignedShort();
            int videoColorTableId = in.readShort();

            d.extendData = new byte[size - 86];
            in.readFully(d.extendData);

            // If the color table ID is set to 0, a color table is contained within the sample description itself. The color
            //table immediately follows the color table ID field in the sample description. See “Color Table
            //Atoms” (page 41) for a complete description of a color table.
            if (videoColorTableId == 0) {
                final AtomInputStream atomIn = new AtomInputStream(new ByteArrayImageInputStream(d.extendData));
                while (atomIn.available() > 0) {
                    String atomType = atomIn.openAtom();
                    if (atomType.equals("ctab")) {
                        d.videoColorTable = readVideoColorTable(atomIn);
                    }
                    atomIn.closeAtom(atomType);
                }
            }
        }
    }

    private IndexColorModel readVideoColorTable(AtomInputStream in) throws IOException {

        in.readUnsignedInt(); // Color table seed. A 32-bit integer that must be set to 0.
        in.readUnsignedShort(); // Color table flags. A 16-bit integer that must be set to 0x8000.
        int colorTableSize = in.readUnsignedShort() + 1;
        // Color table size. A 16-bit integer that indicates the number of
        // colors in the following color array. This is a zero-relative value;
        // setting this field to 0 means that there is one color in the array.
        int[] rgbs = new int[Math.min(256, colorTableSize)];
        for (int i = 0, n = colorTableSize; i < n; ++i) {
            // An array of colors. Each color is made of four unsigned 16-bit integers.
            // The first integer must be set to 0, the second is the red value,
            // the third is the green value, and the fourth is the blue value.
            in.readUnsignedShort();
            int red = in.readUnsignedShort();
            int green = in.readUnsignedShort();
            int blue = in.readUnsignedShort();
            int rgb = ((red & 0xff00) << 8) | (green & 0xff00) | ((blue & 0xff00) >>> 8);
            if (i < rgbs.length) rgbs[i] = rgb;
        }
        return new IndexColorModel(8, rgbs.length, rgbs, 0, false, -1, DataBuffer.TYPE_BYTE);
    }

    /**
     * The Time-to-Sample atom ("stts"-Atom in a media information section).
     * Time-to-sample atoms store duration information for the samples in a
     * media, providing a mapping from a time in a media to the corresponding
     * data sample.
     *
     * <pre>
     * typedef struct {
     *    byte version;
     *    byte[3] flags;
     *    int numberOfEntries;
     *    timeToSampleTable timeToSampleTable[numberOfEntries];
     * } timeToSampleAtom;
     *
     * typedef struct {
     *    int sampleCount;
     *    int sampleDuration;
     * } timeToSampleTable;
     * </pre>
     * <p>
     * Note: this method adds {@code Sample} objects to the
     * {@code Media.samples} list.
     *
     * @param in
     * @param remainingSize
     * @param m
     * @throws IOException
     */
    protected void parseTimeToSample(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        in.skipBytes(3);
        int numberOfEntries = in.readInt();
        m.sampleCount = 0;
        m.timeToSamples.clear();
        for (int i = 0; i < numberOfEntries; i++) {
            int sampleCount = in.readInt();
            int sampleDuration = in.readInt();
            m.sampleCount += sampleCount;
            QuickTimeMeta.MediaSample sFirst = new QuickTimeMeta.MediaSample(sampleDuration, -1, -1);
            QuickTimeMeta.MediaSample sLast = sFirst;
            QuickTimeMeta.TimeToSampleGroup group = new QuickTimeMeta.TimeToSampleGroup(sFirst, sLast, sampleCount);
            m.timeToSamples.add(group);
        }
    }

    /**
     * The Sample-to-Chunk atom ("stsc"-Atom in a media information section). As
     * samples are added to a media, they are collected into chunks that allow
     * optimized data access. A chunk may contain one or more samples. Chunks in
     * a media may have different sizes, and the samples within a chunk may have
     * different sizes.
     *
     * <pre>
     * typedef struct {
     *     byte version;
     *     byte[3] flags;
     *     int numberOfEntries;
     *     sampleToChunkEntry[numberOfEntries] sampleToChunkTable;
     * } sampleToChunkAtom;
     *
     * typedef struct {
     *     int firstChunk;
     *     int samplesPerChunk;
     *     int sampleDescription;
     * } sampleToChunkEntry;
     * </pre>
     */
    protected void parseSampleToChunk(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        if (remainingSize < 20) return;
        int version = in.readUnsignedByte();
        if (version != 0) return;
        in.skipBytes(3);
        int numberOfEntries = in.readInt();
        m.samplesToChunks.clear();
        for (int i = 0; i < numberOfEntries; i++) {
            QuickTimeMeta.SampleToChunk stc = new QuickTimeMeta.SampleToChunk();
            stc.firstChunk = in.readInt();
            stc.samplesPerChunk = in.readInt();
            stc.sampleDescription = in.readInt();
            m.samplesToChunks.add(stc);
        }
    }

    /**
     * The chunk offset atom ("stco"-Atom in a media information section)
     * identifies the location of each chunk of data in the media’s data stream.
     * <pre>
     * typedef struct {
     *     byte version;
     *     byte[3] flags;
     *     int numberOfEntries;
     *     chunkOffsetEntry[numberOfEntries] chunkOffsetTable;
     * } chunkOffsetAtom;
     *
     * typedef struct {
     *     int offset;
     * } chunkOffsetEntry;
     * </pre>
     */
    protected void parseChunkOffsets(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        if (version != 0) return;
        in.skipBytes(3);
        int numberOfEntries = in.readInt();
        m.chunkOffsets.clear();
        for (int i = 0; i < numberOfEntries; i++) {
            m.chunkOffsets.add(in.readUnsignedInt());
        }
    }

    /**
     * The chunk offset 64 atom ("co64"-Atom in a media information section)
     * identifies the location of each chunk of data in the media’s data stream.
     * <pre>
     * typedef struct {
     *     byte version;
     *     byte[3] flags;
     *     int numberOfEntries;
     *     chunkOffset64Entry[numberOfEntries] chunkOffsetTable;
     * } chunkOffset64Atom;
     *
     * typedef struct {
     *     long offset;
     * } chunkOffset64Entry;
     * </pre>
     */
    protected void parseChunkOffsets64(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        if (version != 0) return;
        in.skipBytes(3);
        int numberOfEntries = in.readInt();
        m.chunkOffsets.clear();
        for (int i = 0; i < numberOfEntries; i++) {
            m.chunkOffsets.add(in.readLong());
        }
    }

    /**
     * The Sync Sample atom ("stss"-Atom in a media information section). The
     * sync sample atom identifies the key frames in the media. In a media that
     * contains compressed data, key frames define starting points for portions
     * of a temporally compressed sequence. The key frame is self-contained -
     * that is, it is independent of preceding frames. Subsequent frames may
     * depend on the key frame.
     *
     * <pre>
     * typedef struct {
     *     byte version;
     *     byte[3] flags;
     *     int numberOfEntries;
     *     syncSampleTable syncSampleTable[numberOfEntries];
     * } syncSampleAtom;
     *
     * typedef struct {
     *     int number;
     * } syncSampleTable;
     * </pre>
     */
    protected void parseSyncSample(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        if (version != 0) return;
        in.skipBytes(3);
        int numberOfEntries = in.readInt();
        if (numberOfEntries == 0) {
            m.syncSamples = null;
        } else {
            m.syncSamples = new TreeSet<>();
            for (int i = 0; i < numberOfEntries; i++) {
                // the sample ids are one-based, but we want zero-based indices
                m.syncSamples.add(in.readUnsignedInt() - 1);
            }
        }
    }

    /**
     * The Sample Size atom ("stsz"-Atom in a media information section). Sample
     * size atoms identify the size of each sample in the media.
     *
     * <pre>
     * typedef struct {
     *     byte version;
     *     byte[3] flags;
     *     int sampleSize;
     *     int numberOfEntries;
     *     sampleSizeTable sampleSizeTable[numberOfEntries];
     * } sampleSizeAtom;
     *
     * typedef struct {
     *    int size;
     * } sampleSizeTable;
     * </pre>
     */
    protected void parseSampleSize(QTFFImageInputStream in, long remainingSize, QuickTimeMeta.Media m) throws IOException {
        int version = in.readUnsignedByte();
        if (version != 0) return;
        in.skipBytes(3);
        int sampleSize = in.readInt();
        int numberOfEntries = in.readInt();
        m.sampleSizes.clear();
        if (sampleSize != 0) {
            // all samples have the same size
            QuickTimeMeta.MediaSample firstSample = new QuickTimeMeta.MediaSample(-1, -1, sampleSize);
            QuickTimeMeta.MediaSample lastSample = new QuickTimeMeta.MediaSample(-1, -1, sampleSize);
            QuickTimeMeta.SampleSizeGroup ssg = new QuickTimeMeta.SampleSizeGroup(firstSample, lastSample, numberOfEntries);
            m.sampleSizes.add(ssg);
        } else {
            QuickTimeMeta.SampleSizeGroup ssg = null;
            for (int i = 0; i < numberOfEntries; i++) {
                int size = in.readInt();
                QuickTimeMeta.MediaSample s = new QuickTimeMeta.MediaSample(-1, -1, size);
                if (ssg == null || !ssg.maybeAddSample(s)) {
                    ssg = new QuickTimeMeta.SampleSizeGroup(s);
                    m.sampleSizes.add(ssg);
                }
            }
        }
    }

    /**
     * Color table atoms define a list of preferred colors for displaying
     * the movie on devices that support only 256 colors.
     * The list may contain up to 256 colors. These optional atoms have a type value of 'ctab'.
     * The color table atom contains a Macintosh color table data structure.
     *
     * <pre>
     * magic colorTableAtom "ctab";
     *
     * typedef struct {
     *     int colorTableSeed;      // A 32-bit integer that must be set to 0.
     *     ushort colorTableFlags;   // A 16-bit integer that must be set to 0x8000.
     *     ushort colorTableSize;    //A 16-bit integer that indicates the number
     *                              // of colors in the following color array.
     *                              // This is a zero-relative value; setting this field
     *                              // to 0 means that there is one color in the array.
     *     colorArrayEntry[] colorArray;
     *                              // An array of colors. Each color is made of four
     *                              // unsigned 16-bit integers. The first integer
     *                              // must be set to 0, the second is the red value,
     *                              // the third is the green value, and the fourth
     *                              // is the blue value.
     * } colorTableAtom;
     *
     * typedef struct {
     *     ushort unused; // Must be set to 0
     *     ushort red;
     *     ushort green;
     *     ushort blue;
     * } colorArrayEntry;
     * </pre>
     */
    protected void parseColorTable(QTFFImageInputStream in, long remainingSize, QuickTimeMeta meta) throws IOException {
        int colorTableSeed = in.readInt();
        assert colorTableSeed == 0;
        int colorTableFlags = in.readUnsignedShort();
        assert colorTableFlags == 0x8000;
        int colorTableSize = in.readUnsignedShort() + 1;
        int[] cmap = new int[colorTableSize];
        for (int i = 0; i < colorTableSize; i++) {
            int unused = in.readUnsignedShort();
            assert unused == 0;
            int red = in.readUnsignedShort();
            int green = in.readUnsignedShort();
            int blue = in.readUnsignedShort();
            cmap[i] = ((red & 0xff00) << 8) | (green & 0xff00) | ((blue & 0xff00) >>> 8);
        }
        meta.colorTables.add(new IndexColorModel(8, colorTableSize, cmap, 0, false, -1, DataBuffer.TYPE_BYTE));
    }
}

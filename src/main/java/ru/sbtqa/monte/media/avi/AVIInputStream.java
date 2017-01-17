/* @(#)AVIInputStream.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.avi;

import java.awt.Dimension;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import ru.sbtqa.monte.media.AbortException;
import static ru.sbtqa.monte.media.AudioFormatKeys.*;
import ru.sbtqa.monte.media.Format;
import ru.sbtqa.monte.media.FormatKeys.MediaType;
import ru.sbtqa.monte.media.ParseException;
import static ru.sbtqa.monte.media.VideoFormatKeys.*;
import ru.sbtqa.monte.media.io.ByteArrayImageInputStream;
import ru.sbtqa.monte.media.math.Rational;
import ru.sbtqa.monte.media.riff.RIFFChunk;
import ru.sbtqa.monte.media.riff.RIFFParser;
import ru.sbtqa.monte.media.riff.RIFFVisitor;

/**
 * Provides low-level support for reading encoded audio and video samples from
 * an AVI 1.0 or an AVI 2.0 file.
 * 
 * The length of an AVI 1.0 file is limited to 1 GB. This class supports lengths
 * of up to 4 GB, but such files may not work on all players.
 * 
 * Support for AVI 2.0 file is incomplete. This class currently ignores the
 * extended index chunks. Instead all chunks in the "movi" list are scanned.
 * With scanning, the reader is not able to distinguish between keyframes and
 * non-keyframes. As a consequence opening an AVI 2.0 file is very slow, and
 * decoding of frames may fail.
 * 
 * For detailed information about the AVI 1.0 file format see:<br>
 * <a href="http://msdn.microsoft.com/en-us/library/ms779636.aspx">msdn.microsoft.com
 * AVI RIFF</a><br>
 * <a href="http://www.microsoft.com/whdc/archive/fourcc.mspx">www.microsoft.com
 * FOURCC for Video Compression</a><br>
 * <a href="http://www.saettler.com/RIFFMCI/riffmci.html">www.saettler.com
 * RIFF</a><br>
 * 
 * For detailed information about the AVI 2.0 file format see:<br>
 * <a href="http://www.the-labs.com/Video/odmlff2-avidef.pdf">OpenDML AVI File
 * Format Extensions, Version 1.02</a><br>
 *
 * FIXME - This class lacks readSample() methods.
 *
 * @author Werner Randelshofer
 * @version $Id: AVIInputStream.java 364 2016-11-09 19:54:25Z werner $
 */
public class AVIInputStream extends AbstractAVIStream {

    /**
     * The image input stream.
     */
    protected final ImageInputStream in;
    /**
     * This variable is set to true when all meta-data has been read from the
     * file.
     */
    private boolean isRealized = false;
    protected MainHeader mainHeader;
    protected ArrayList<Sample> idx1 = new ArrayList<Sample>();
    private long moviOffset = 0;

    /**
     * Creates a new instance.
     *
     * @param file the input file
     * @throws java.io.IOException TODO
     */
    public AVIInputStream(File file) throws IOException {

        this.in = new FileImageInputStream(file);
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        this.streamOffset = 0;
    }

    /**
     * Creates a new instance.
     *
     * @param in the input stream.
     * @throws java.io.IOException TODO
     */
    public AVIInputStream(ImageInputStream in) throws IOException {
        this.in = in;
        this.streamOffset = in.getStreamPosition();
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Ensures that all meta-data has been read from the file.
     *
     * @throws java.io.IOException TODO
     */
    protected void ensureRealized() throws IOException {
        if (!isRealized) {
            isRealized = true;
            readAllMetadata();
        }
        if (mainHeader == null) {
            throw new IOException("AVI main header missing.");
        }
    }

    /**
     * Returns the main header flags. The flags are an or-combination of the
     * {@code AVIH_...} values.
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public int getHeaderFlags() throws IOException {
        ensureRealized();
        return mainHeader.flags;
    }

    public Dimension getVideoDimension() throws IOException {
        ensureRealized();
        return (Dimension) mainHeader.size.clone();
    }

    public int getTrackCount() throws IOException {
        ensureRealized();
        return tracks.size();
    }

    /**
     * Returns the number of microseconds (10^-6 seconds) per frame. This is
     * used as a time basis for the start time of tracks within a movie.
     *
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public long getMicroSecPerFrame() throws IOException {
        ensureRealized();
        return mainHeader.microSecPerFrame;
    }

    /**
     * Returns the time scale of the specified track.
     *
     * @param track TODO
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public long getTimeScale(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).scale;
    }

    /**
     * Returns the start time of the track given as the number of frames in
     * microSecPerFrame units.
     *
     * @param track TODO
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public long getStartTime(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).startTime;
    }

    /**
     * Returns the number of media data chunks in the track. This includes
     * chunks which do not affect the timing of the media, such as palette
     * changes.
     *
     * @param track TODO
     * @return the number of chunks
     * @throws IOException TODO
     */
    public long getChunkCount(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).samples.size();
    }

    /**
     * Returns the name of the track, or null if the name is not specified.
     *
     * @param track TODO
     * @return TODO
     * @throws java.io.IOException TODO
     */
    public String getName(int track) throws IOException {
        ensureRealized();
        return tracks.get(track).name;
    }

    /**
     * Returns the contents of the extra track header. Returns null if the
     * header is not present.
     *
     * @param track TODO
     * @param fourcc TODO
     * @return The extra header as a byte array
     * @throws IOException TODO
     */
    public byte[] getExtraHeader(int track, String fourcc) throws IOException {
        ensureRealized();
        int id = RIFFParser.stringToID(fourcc);
        for (RIFFChunk c : tracks.get(track).extraHeaders) {
            if (c.getID() == id) {
                return c.getData();
            }
        }
        return null;
    }

    /**
     * Returns the fourcc's of all extra stream headers.
     *
     * @param track TODO
     * @return An array of fourcc's of all extra stream headers.
     * @throws IOException TODO
     */
    public String[] getExtraHeaderFourCCs(int track) throws IOException {
        Track tr = tracks.get(track);
        String[] fourccs = new String[tr.extraHeaders.size()];
        for (int i = 0; i < fourccs.length; i++) {
            fourccs[i] = RIFFParser.idToString(tr.extraHeaders.get(i).getID());
        }
        return fourccs;
    }

    /**
     * Reads all metadata of the file.
     *
     * @throws java.io.IOException TODO
     */
    protected void readAllMetadata() throws IOException {
        in.seek(streamOffset);
        final RIFFParser p = new RIFFParser();
        //p.declareStopChunkType(MOVI_ID);
        //p.declareStopChunkType(REC_ID);
        try {
            RIFFVisitor v = new RIFFVisitor() {
                private Track currentTrack;

                @Override
                public boolean enteringGroup(RIFFChunk group) {
                    //System.out.println("AVIInputStream enteringGroup " + group + "  0x" + Integer.toHexString(group.getType()) + " 0x" + Integer.toHexString(group.getID()));
                    if (group.getType() == MOVI_ID) {
                        moviOffset = group.getScan() + 8;
                    }

                    if (group.getType() == MOVI_ID && group.getID() == LIST_ID) {
                        if (mainHeader != null
                              && (mainHeader.flags & AVIH_FLAG_HAS_INDEX) != 0
                              && p.getStreamOffset() == 0) {
                            // => skip movi list if an index is available
                            //System.out.println("AVIInputStream skipping movi list.");                            
                            return false;
                        }
                    }
                    return true;
                }

                @Override
                public void enterGroup(RIFFChunk group) throws ParseException, AbortException {
                    //System.out.println("AVIInputStream enterGroup " + group);
                }

                @Override
                public void leaveGroup(RIFFChunk group) throws ParseException, AbortException {
                    //System.out.println("AVIInputStream leaveGroup " + group);
                    if (group.getType() == HDRL_ID) {
                        currentTrack = null;
                    }
                }

                @Override
                public void visitChunk(RIFFChunk group, RIFFChunk chunk) throws ParseException, AbortException {
                    try {
                        //System.out.print("group " + intToType(group.getType()) + " " + intToType(group.getID()));
                        //System.out.println(" chunk " + intToType(chunk.getType()) + " " + intToType(chunk.getID()));
                        switch (chunk.getType()) {
                            case HDRL_ID:
                                switch (chunk.getID()) {
                                    case AVIH_ID:
                                        mainHeader = readAVIH(chunk.getData());
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case STRL_ID:
                                // FIXME - The code below depends too much on
                                // the sequence of the chunks in the STRL.
                                // We should just collect all chunks in the STRL
                                // and process them when we leave the STRL. 
                                switch (chunk.getID()) {
                                    case STRH_ID:
                                        currentTrack = readSTRH(chunk.getData());
                                        tracks.add(currentTrack);
                                        break;
                                    case STRF_ID:
                                        switch (currentTrack.mediaType) {
                                            case AUDIO:
                                                readAudioSTRF((AudioTrack) currentTrack, chunk.getData());
                                                break;
                                            case VIDEO:
                                                readVideoSTRF((VideoTrack) currentTrack, chunk.getData());
                                                break;
                                            default:
                                                throw new ParseException("Unsupported media type:" + currentTrack.mediaType);
                                        }
                                        break;
                                    case STRN_ID:
                                        readSTRN(currentTrack, chunk.getData());
                                        break;
                                    default:
                                        currentTrack.extraHeaders.add(chunk);
                                        break;
                                }
                                break;
                            case AVI_ID:
                                switch (chunk.getID()) {
                                    case IDX1_ID:
                                        if (isFlagSet(mainHeader.flags, AVIH_FLAG_HAS_INDEX)) {
                                            readIDX1(tracks, idx1, chunk.getData());
                                        }
                                        break;
                                    default:
                                        break;
                                }
                                break;
                            case MOVI_ID:
                            // fall through
                            case REC_ID: {
                                int chunkIdInt = chunk.getID();
                                int id = chunkIdInt;
                                int track = (((chunkIdInt >> 24) & 0xff) - '0') * 10 + (((chunkIdInt >>> 16) & 0xff) - '0');
                                if (track >= 0 && track < tracks.size()) {
                                    Track tr = tracks.get(track);
                                    Sample s = new Sample(id, (id & 0xffff) == PC_ID ? 0 : 1, chunk.getScan(), chunk.getSize(), false);
                                    // Audio chunks may contain multiple samples
                                    if (tr.format.get(MediaTypeKey) == MediaType.AUDIO) {
                                        s.duration = (int) (s.length / (tr.format.get(FrameSizeKey) * tr.format.get(ChannelsKey)));
                                    }
                                    // The first chunk and all uncompressed chunks are keyframes
                                    s.isKeyframe = tr.samples.isEmpty() || (id & 0xffff) == WB_ID || (id & 0xffff) == DB_ID;
                                    if (tr.samples.size() > 0) {
                                        Sample lastSample = tr.samples.get(tr.samples.size() - 1);
                                        s.timeStamp = lastSample.timeStamp + lastSample.duration;
                                    }
                                    tr.length = s.timeStamp + s.duration;
                                    idx1.add(s);
                                    tr.samples.add(s);

                                }
                            }
                            break;
                            default:
                                break;
                        }
                        // System.out.println("AVIInputStream visitChunk " + group + " " + chunk);
                    } catch (IOException ex) {
                        throw new ParseException("Error parsing " + RIFFParser.idToString(group.getID()) + "." + RIFFParser.idToString(chunk.getID()), ex);
                    }
                }
            };

            // Parse all RIFF structures in the file
            int count = 0;
            while (true) {
                long offset = p.parse(in, v);
                p.setStreamOffset(offset);
                count++;
            }
        } catch (EOFException ex) {
            //ex.printStackTrace();
        } catch (ParseException ex) {
            throw new IOException("Error Parsing AVI stream", ex);
        } catch (AbortException ex) {
            throw new IOException("Parsing aborted", ex);
        }
    }

    /**
     * Reads the AVI Main Header and returns a MainHeader object.
     */
    private MainHeader readAVIH(byte[] data) throws IOException, ParseException {
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(data, ByteOrder.LITTLE_ENDIAN);
        MainHeader mh = new MainHeader();
        mh.microSecPerFrame = in.readUnsignedInt();
        mh.maxBytesPerSec = in.readUnsignedInt();
        mh.paddingGranularity = in.readUnsignedInt();
        mh.flags = in.readInt();
        mh.totalFrames = in.readUnsignedInt();
        mh.initialFrames = in.readUnsignedInt();
        mh.streams = in.readUnsignedInt();
        mh.suggestedBufferSize = in.readUnsignedInt();

        mh.size = new Dimension(in.readInt(), in.readInt());
        return mh;
    }

    /**
     * Reads an AVI Stream Header and returns a Track object.
     */
    /*typedef struct {
     *     FOURCC enum aviStrhType type; 
     *        // Contains a FOURCC that specifies the type of the data contained in 
     *        // the stream. The following standard AVI values for video and audio are 
     *        // defined.
     *     FOURCC handler;
     *     DWORD  set aviStrhFlags flags;
     *     WORD   priority;
     *     WORD   language;
     *     DWORD  initialFrames;
     *     DWORD  scale;
     *     DWORD  rate;
     *     DWORD  startTime;
     *     DWORD  length;
     *     DWORD  suggestedBufferSize;
     *     DWORD  quality;
     *     DWORD  sampleSize;
     *    aviRectangle frame;
     * } AVISTREAMHEADER;     */
    private Track readSTRH(byte[] data) throws IOException, ParseException {
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(data, ByteOrder.LITTLE_ENDIAN);
        Track tr = null;

        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        String type = intToType(in.readInt());
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        int handler = in.readInt();

        if (type.equals(AVIMediaType.AUDIO.fccType)) {
            tr = new AudioTrack(tracks.size(), handler);
        } else if (type.equals(AVIMediaType.VIDEO.fccType)) {
            tr = new VideoTrack(tracks.size(), handler, null);
        } else if (type.equals(AVIMediaType.MIDI.fccType)) {
            tr = new MidiTrack(tracks.size(), handler);
        } else if (type.equals(AVIMediaType.TEXT.fccType)) {
            tr = new TextTrack(tracks.size(), handler);
        } else {
            throw new ParseException("Unknown track type " + type);
        }

        tr.fccHandler = handler;

        tr.flags = in.readInt();
        tr.priority = in.readUnsignedShort();
        tr.language = in.readUnsignedShort();
        tr.initialFrames = in.readUnsignedInt();
        tr.scale = in.readUnsignedInt();
        tr.rate = in.readUnsignedInt();
        tr.startTime = in.readUnsignedInt();
        tr.length = in.readUnsignedInt();
        /*tr.suggestedBufferSize=*/ in.readUnsignedInt();
        tr.quality = in.readInt();
        /*tr.sampleSize=*/ in.readUnsignedInt();
        tr.frameLeft = in.readShort();
        tr.frameTop = in.readShort();
        tr.frameRight = in.readShort();
        tr.frameBottom = in.readShort();

        return tr;
    }

    /**
     * 
     * typedef struct {
     *   cstring name;
     * } STREAMNAME;
     * 
     *
     * @param tr TODO
     * @param data TODO
     * @throws IOException TODO
     */
    private void readSTRN(Track tr, byte[] data) throws IOException {
        tr.name = new String(data, 0, data.length - 1, "ASCII");
    }

    /**
     *  //---------------------- // AVI Bitmap Info Header //
     * ---------------------- typedef struct { BYTE blue; BYTE green; BYTE red;
     * BYTE reserved; } RGBQUAD;
     *
     * // Values for this enum taken from: //
     * http://www.fourcc.org/index.php?http%3A//www.fourcc.org/rgb.php enum {
     * BI_RGB = 0x00000000, RGB = 0x32424752, // Alias for BI_RGB BI_RLE8 =
     * 0x01000000, RLE8 = 0x38454C52, // Alias for BI_RLE8 BI_RLE4 = 0x00000002,
     * RLE4 = 0x34454C52, // Alias for BI_RLE4 BI_BITFIELDS = 0x00000003, raw =
     * 0x32776173, RGBA = 0x41424752, RGBT = 0x54424752, cvid = "cvid" }
     * bitmapCompression;
     *
     * typedef struct { DWORD structSize; DWORD width; DWORD height; WORD
     * planes; WORD bitCount; FOURCC enum bitmapCompression compression; DWORD
     * imageSizeInBytes; DWORD xPelsPerMeter; DWORD yPelsPerMeter; DWORD
     * numberOfColorsUsed; DWORD numberOfColorsImportant; RGBQUAD colors[]; }
     * BITMAPINFOHEADER;
     * 
     *
     *
     * @param tr TODO
     * @param data TODO
     * @throws IOException TODO
     */
    private void readVideoSTRF(VideoTrack tr, byte[] data) throws IOException {
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(data, ByteOrder.LITTLE_ENDIAN);

        long structSize = in.readUnsignedInt();
        tr.width = in.readInt();
        tr.height = in.readInt();
        tr.planes = in.readUnsignedShort();
        tr.bitCount = in.readUnsignedShort();
        in.setByteOrder(ByteOrder.BIG_ENDIAN);
        tr.compression = intToType(in.readInt());
        in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
        long imageSizeInBytes = in.readUnsignedInt();
        tr.xPelsPerMeter = in.readUnsignedInt();
        tr.yPelsPerMeter = in.readUnsignedInt();
        tr.clrUsed = in.readUnsignedInt();
        tr.clrImportant = in.readUnsignedInt();
        if (tr.bitCount == 0) {
            tr.bitCount = (int) (imageSizeInBytes / tr.width / tr.height * 8);
        }

        tr.format = new Format(MimeTypeKey, MIME_AVI,
              MediaTypeKey, MediaType.VIDEO,
              EncodingKey, tr.compression,
              DataClassKey, byte[].class,
              WidthKey, tr.width,
              HeightKey, tr.height,
              DepthKey, tr.bitCount,
              PixelAspectRatioKey, new Rational(1, 1),
              FrameRateKey, new Rational(tr.rate, tr.scale),
              FixedFrameRateKey, true);
    }

    /**
     * /
     **
     * 
     * The format of a video track is defined in a "strf" chunk, which contains
     * a {@code WAVEFORMATEX} struct.
     * 
     * ----------------------
     * AVI Wave Format Header
     * ----------------------
     * // values for this enum taken from mmreg.h
     * enum {
     *         WAVE_FORMAT_PCM = 0x0001,
     *         //  Microsoft Corporation
     *       ...many more...
     * } wFormatTagEnum;
     *
     * typedef struct {
     *   WORD enum wFormatTagEnum formatTag;
     *   WORD  numberOfChannels;
     *   DWORD samplesPerSec;
     *   DWORD avgBytesPerSec;
     *   WORD  blockAlignment;
     *   WORD  bitsPerSample;
     *   WORD  cbSize;
     *     // Size, in bytes, of extra format information appended to the end of the
     *     // WAVEFORMATEX structure. This information can be used by non-PCM formats
     *     // to store extra attributes for the "wFormatTag". If no extra information
     *     // is required by the "wFormatTag", this member must be set to zero. For
     *     // WAVE_FORMAT_PCM formats (and only WAVE_FORMAT_PCM formats), this member
     *     // is ignored.
     *   byte[cbSize] extra;
     * } WAVEFORMATEX;
     * 
     *
     *
     * @param tr TODO
     * @param data TODO
     * @throws IOException TODO
     */
    private void readAudioSTRF(AudioTrack tr, byte[] data) throws IOException {
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(data, ByteOrder.LITTLE_ENDIAN);

        String formatTag = RIFFParser.idToString(in.readUnsignedShort());
        tr.channels = in.readUnsignedShort();
        tr.samplesPerSec = in.readUnsignedInt();
        tr.avgBytesPerSec = in.readUnsignedInt();
        tr.blockAlign = in.readUnsignedShort();
        tr.bitsPerSample = in.readUnsignedShort();
        if (data.length > 16) {
            long cbSize = in.readUnsignedShort();
            // FIXME - Don't ignore extra format information
        }

        tr.format = new Format(MimeTypeKey, MIME_AVI,
              MediaTypeKey, MediaType.AUDIO,
              EncodingKey, formatTag,
              SampleRateKey, Rational.valueOf(tr.samplesPerSec),
              SampleSizeInBitsKey, tr.bitsPerSample,
              ChannelsKey, tr.channels,
              FrameSizeKey, tr.blockAlign,
              FrameRateKey, new Rational(tr.samplesPerSec, 1),
              SignedKey, tr.bitsPerSample != 8,
              ByteOrderKey, ByteOrder.LITTLE_ENDIAN,
              FixedFrameRateKey, true);

    }

    /**
     * 
     * // The values for this set have been taken from:
     * // http://graphics.cs.uni-sb.de/NMM/dist-0.4.0/Docs/Doxygen/html/avifmt_8h.html
     * set {
     *     AVIIF_KEYFRAME = 0x00000010,
     *         // The data chunk is a key frame.
     *     AVIIF_LIST = 0x00000001,
     *         // The data chunk is a 'rec ' list.
     *     AVIIF_NO_TIME = 0x00000100,
     *         // The data chunk does not affect the timing of the stream. For example,
     *         // this flag should be set for palette changes.
     *     AVIIF_COMPUSE = 0x0fff0000
     *         // These bits are for compressor use
     * } avioldindex_flags;
     *
     * typedef struct {
     *       FOURCC   chunkId;
     *       // Specifies a FOURCC that identifies a stream in the AVI file. The
     *       // FOURCC must have the form 'xxyy' where xx is the stream number and yy
     *       // is a two-character code that identifies the contents of the stream:
     *       DWORD  set avioldindex_flags flags;
     *       // Specifies a bitwise combination of zero or more of flags.
     *       DWORD   offset;
     *       // Specifies the location of the data chunk in the file. The value should
     *       // be specified as an offset, in bytes, from the start of the 'movi' list;
     *       // however, in some AVI files it is given as an offset from the start of
     *       // the file.
     *       DWORD   size;
     *       // Specifies the size of the data chunk, in bytes.
     * } avioldindex_entry;
     * 
     *
     * @param tracks TODO
     * @param data TODO
     * @return The idx1 list of samples.
     * @throws IOException TODO
     */
    private void readIDX1(ArrayList<Track> tracks, ArrayList<Sample> idx1, byte[] data) throws IOException {
        ByteArrayImageInputStream in = new ByteArrayImageInputStream(data, ByteOrder.LITTLE_ENDIAN);

        long[] trReadTimeStamp = new long[tracks.size()];

        Sample paletteChange = null;
        while (in.getStreamPosition() < data.length) {
            in.setByteOrder(ByteOrder.BIG_ENDIAN);
            int chunkIdInt = in.readInt();
            in.setByteOrder(ByteOrder.LITTLE_ENDIAN);
            int chunkId = chunkIdInt;
            int track = (((chunkIdInt >>> 24) & 0xff) - '0') * 10 + (((chunkIdInt >>> 16) & 0xff) - '0');
            if (track < 0 || track > 99 || track > tracks.size()) {
                throw new IOException("Illegal chunkId in IDX1:" + chunkId);
            }
            int flags = in.readInt();
            long offset = in.readUnsignedInt();
            long size = in.readUnsignedInt();
            Track tr = tracks.get(track);
            int duration = ((flags & 0x100) != 0) ? 0 : 1;
            if (tr.mediaType == AVIMediaType.AUDIO) {
                Format af = tr.format;
                duration = (int) (size * duration / af.get(FrameSizeKey));
                flags |= 0x10; // all audio samples are keyframes
            }
            Sample s = new Sample(chunkId, duration, offset + moviOffset, size, (flags & 0x10) != 0);
            s.timeStamp = trReadTimeStamp[track];
            idx1.add(s);
            trReadTimeStamp[track] += duration;

            // special treatment for palette changes
            // FIXME - We should coalesce multiple palette changes
            if ((s.chunkType & CHUNK_SUBTYPE_MASK) == PC_ID) {
                paletteChange = s;
            } else {
                if (paletteChange != null) {
                    s.header = paletteChange;
                }
                tr.samples.add(s);
            }
        }

        for (Track tr : tracks) {
            tr.readIndex = 0;
        }
    }

    public void close() throws IOException {
        in.close();
        for (Track tr : tracks) {
            tr.samples.clear();
        }
        tracks.clear();
    }
}

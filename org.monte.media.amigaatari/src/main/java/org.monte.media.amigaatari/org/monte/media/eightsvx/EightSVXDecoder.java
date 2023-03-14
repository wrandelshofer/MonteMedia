/*
 * @(#)EightSVXDecoder.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.eightsvx;

import org.monte.media.exception.AbortException;
import org.monte.media.exception.ParseException;
import org.monte.media.iff.IFFChunk;
import org.monte.media.iff.IFFParser;
import org.monte.media.iff.IFFVisitor;
import org.monte.media.iff.MC68000InputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Creates a collection of EightSVXAudioClip objects by
 * reading an IFF 8SVX file.
 *
 * <p><b>8SVX Type Definitions</b>
 * <pre>
 * #define ID_8SVX MakeID('8', 'S', 'V', 'X')
 * #define ID_VHDR MakeID('V', 'H', 'D', 'R')
 *
 * typedef LONG Fixed;     // A Fixed-point value, 16 bits to the left of
 * // the point and 16 to the right. A Fixed is a number
 * // of 2^16ths, i.e., 65536ths.
 * #define Unity 0x10000L  // Unity = Fixed 1.0 = maximum volume
 *
 * // sCompression: Choice of compression algorithm applied to the samples.
 * #define sCmpNone   0  // not compressed
 * #define sCmpFibDelta 1  // Fibonacci-delta encoding.
 * // Can be more kinds in the future.
 *
 * typedef struct {
 * ULONG oneShotHiSamples,   // # samples in the high octave 1-shot part
 * repeatHiSamples,   // # samples in the high octave repeat part
 * samplesPerHiCycle; // # samples/cycle in high octave, else 0
 * UWORD samplesPerSec;     // data sampling rate
 * UBYTE ctOctave,          // # octaves of waveform
 * sCompression;      // data compression technique used
 * Fixed volume;            // playback volume form 0 to Unity (full
 * // volume). Map this value into the output
 * // hardware's dynamic range.
 * } Voice8Header;
 *
 * #define ID_NAME MakeID('N', 'A', 'M', 'E')
 * // NAME chunk contains a CHAR[], the voice's name.
 *
 * #define ID_Copyright MakeID('(', 'c', ')', ' ')
 * // "(c) " chunk contains a CHAR[], the FORM's copyright notice.
 *
 * #define ID_AUTH MakeID('A', 'U', 'T', 'H')
 * // AUTH chunk contains a CHAR[], the author's name.
 *
 * #define ID_ANNO MakeID('A', 'N', 'N', 'O')
 * // ANNO chunk contains a CHAR[], author's text annotations.
 *
 * #define ID_ATAK MakeID('A', 'T', 'A', 'K')
 * #define ID_RLSE MakeID('R', 'L', 'S', 'E')
 *
 * typedef struct {
 * UWORD duration; // segment duration in milliseconds, &gt; 0
 * Fixed dest;     // destination volume factor
 * } EGPoint;
 *
 * // ATAK and RLSE chunks contain an EGPoint[], piecewise-linear envelope.
 * // The envelope defines a function of time returning Fixed values. It's
 * // used to scale the nominal volume specified in the Voice8Header.
 *
 * #define RIGHT    4L
 * #define LEFT     2L
 * #define STEREO   6L
 *
 * #define ID_CHAN MakeID('C', 'H', 'A', 'N')
 * typedef sampletype LONG;
 *
 * #define ID_PAN MakeID('P', 'A', 'N', ' ')
 * typedef sposition Fixed; // 0 &lt;= sposition &lt;= Unity
 * // Unity refers to the maximum possible volume.
 *
 *
 * #define ID_BODY MakeID('B', 'O', 'D', 'Y')
 * typedef character BYTE; // 8 bit signed number, -128 thru 127.
 * // BODY chunk contains a BYTE[], array of audio data samples
 * </pre>
 *
 * <p><b>8SVX Regular Expression</b>
 * <pre>
 * 8SVX       ::= "FORM" #{ "8SVX" VHDR [NAME] [Copyright] [AUTH] ANNO* [ATAK] [RLSE] [CHAN] [PAN] BODY }
 *
 * VHDR       ::= "VHDR" #{ Voice8Header }
 * NAME       ::= "NAME" #{ CHAR* } [0]
 * Copyright  ::= "(c) " #{ CHAR* } [0]
 * AUTH       ::= "AUTH" #{ CHAR* } [0]
 * ANNO       ::= "ANNO" #{ CHAR* } [0]
 *
 * ATAK       ::= "ATAK" #{ EGPoint* }
 * RLSE       ::= "RLSE" #{ EGPoint* }
 * CHAN       ::= "CHAN" #{ sampletype }
 * PAN        ::= "PAN " #{ sposition }
 * BODY       ::= "BODY" #{ BYTE* } [0]
 * </pre>
 * The token "#" represents a ckSize LONG count of the following {braced} data bytes.
 * E.g., a VHDR's "#" should equal sizeof(Voice8Header). Literal items are shown in
 * "quotes", [square bracket items] are optional, and "*" means 0 ore more replications.
 * A sometimes-needed pad byte is shown as "[0]".
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 */
public class EightSVXDecoder
        implements IFFVisitor {
    /* Constants */
    public final static int EIGHT_SVX_ID = IFFParser.stringToID("8SVX");
    public final static int VHDR_ID = IFFParser.stringToID("VHDR");
    public final static int NAME_ID = IFFParser.stringToID("NAME");
    public final static int COPYRIGHT_ID = IFFParser.stringToID("(c) ");
    public final static int ANNO_ID = IFFParser.stringToID("ANNO");
    public final static int AUTH_ID = IFFParser.stringToID("AUTH");
    //public final static int ATAK_ID = IFFParser.stringToID("ATAK");
    //public final static int RLSE_ID = IFFParser.stringToID("RLSE");
    public final static int CHAN_ID = IFFParser.stringToID("CHAN");
    //public final static int PAN_ID = IFFParser.stringToID("PAN ");
    public final static int BODY_ID = IFFParser.stringToID("BODY");

    /* Instance variables */
    private ArrayList<AudioClip> samples = new ArrayList<AudioClip>();
    private boolean within8SVXGroup = false;

    /* Constructors  */

    /**
     * Creates a new Audio Source from the specified InputStream.
     * <p>
     * Pre condition
     * InputStream must contain IFF 8SVX data.
     * Post condition
     * -
     * Obligation
     * -
     *
     * @param in The input stream.
     */
    public EightSVXDecoder(InputStream in)
            throws IOException {
        try {
            IFFParser iff = new IFFParser();
            registerChunks(iff);
            iff.parse(in, this);
        } catch (ParseException e) {
            throw new IOException(e.toString());
        } catch (AbortException e) {
            throw new IOException(e.toString());
        } finally {
            in.close();
        }
    }

    public EightSVXDecoder() {
    }

    /* Accessors */
    public ArrayList<AudioClip> getSamples() {
        return samples;
    }

    /* Actions */
    public void registerChunks(IFFParser iff) {
        iff.declareGroupChunk(EIGHT_SVX_ID, IFFParser.ID_FORM);
        iff.declarePropertyChunk(EIGHT_SVX_ID, VHDR_ID);
        iff.declarePropertyChunk(EIGHT_SVX_ID, NAME_ID);
        iff.declarePropertyChunk(EIGHT_SVX_ID, COPYRIGHT_ID);
        iff.declareCollectionChunk(EIGHT_SVX_ID, ANNO_ID);
        iff.declarePropertyChunk(EIGHT_SVX_ID, AUTH_ID);
        iff.declarePropertyChunk(EIGHT_SVX_ID, CHAN_ID);
        iff.declareDataChunk(EIGHT_SVX_ID, BODY_ID);
    }

    /**
     * Visits the start of an IFF GroupChunkExpression.
     * <p>
     * Although this method is declared as public it may only
     * be called from an IFFParser that has been invoked
     * by this class.
     * <p>
     * Pre condition
     * Vector "clips" must not be null.
     * This method expects only FORM groups of type 8SVX.
     * Post condition
     * -
     * Obligation
     * -
     *
     * @param group Group Chunk to be visited.
     */
    public void enterGroup(IFFChunk group) {
        if (group.getType() == EIGHT_SVX_ID) {
            within8SVXGroup = true;
        }
    }

    public void leaveGroup(IFFChunk group) {
        if (group.getType() == EIGHT_SVX_ID) {
            within8SVXGroup = false;
        }
    }

    public void visitChunk(IFFChunk group, IFFChunk chunk)
            throws ParseException {
        if (within8SVXGroup) {
            if (chunk.getID() == BODY_ID) // && group.getID() == EIGHT_SVX_ID)
            {
                if (group.getPropertyChunk(VHDR_ID) == null) {
                    throw new ParseException("Sorry: Without 8SVX.VHDR-Chunk no sound possible");
                }
                EightSVXAudioClip newSample = new EightSVXAudioClip();
                decodeVHDR(newSample, group.getPropertyChunk(VHDR_ID));
                decodeCHAN(newSample, group.getPropertyChunk(CHAN_ID));
                decodeNAME(newSample, group.getPropertyChunk(NAME_ID));
                decodeCOPYRIGHT(newSample, group.getPropertyChunk(COPYRIGHT_ID));
                decodeAUTH(newSample, group.getPropertyChunk(COPYRIGHT_ID));
                decodeANNO(newSample, group.getCollectionChunks(ANNO_ID));
                decodeBODY(newSample, chunk);
                addAudioClip(newSample);
            }
        }
    }

    public void addAudioClip(AudioClip clip) {
        samples.add(clip);
    }

    /**
     * The Voice 8 Header (VHDR) property chunk holds the playback parameters for the
     * sampled waveform.
     * <pre>
     * typedef LONG Fixed;     // A Fixed-point value, 16 bits to the left of
     * // the point and 16 to the right. A Fixed is a number
     * // of 2^16ths, i.e., 65536ths.
     * #define Unity 0x10000L  // Unity = Fixed 1.0 = maximum volume
     *
     * // sCompression: Choice of compression algorithm applied to the samples.
     * #define sCmpNone   0  // not compressed
     * #define sCmpFibDelta 1  // Fibonacci-delta encoding.
     * // Can be more kinds in the future.
     *
     * typedef struct {
     * ULONG oneShotHiSamples,   // # samples in the high octave 1-shot part
     * repeatHiSamples,   // # samples in the high octave repeat part
     * samplesPerHiCycle; // # samples/cycle in high octave, else 0
     * UWORD samplesPerSec;     // data sampling rate
     * UBYTE ctOctave,          // # octaves of waveform
     * sCompression;      // data compression technique used
     * Fixed volume;            // playback volume form 0 to Unity (full
     * // volume). Map this value into the output
     * // hardware's dynamic range.
     * } Voice8Header;
     * </pre>
     */
    protected void decodeVHDR(EightSVXAudioClip sample, IFFChunk chunk)
            throws ParseException {
        try {
            if (chunk != null) {
                MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));
                sample.setOneShotHiSamples(in.readULONG());
                sample.setRepeatHiSamples(in.readULONG());
                sample.setSamplesPerHiCycle(in.readULONG());
                sample.setSampleRate(in.readUWORD());
                sample.setCtOctave(in.readUBYTE());
                sample.setSCompression(in.readUBYTE());
                sample.setVolume(in.readLONG());
            }
        } catch (IOException e) {
            throw new ParseException("Error parsing 8SVX VHDR:" + e.getMessage());
        }
    }

    protected void decodeCHAN(EightSVXAudioClip sample, IFFChunk chunk)
            throws ParseException {
        if (chunk != null) {
            sample.setSampleType(chunk.getData()[3]);
        }
    }

    protected void decodeNAME(EightSVXAudioClip sample, IFFChunk chunk)
            throws ParseException {
        if (chunk != null) {
            sample.setName(new String(chunk.getData()));
        }
    }

    protected void decodeCOPYRIGHT(EightSVXAudioClip sample, IFFChunk chunk)
            throws ParseException {
        if (chunk != null) {
            sample.setCopyright(new String(chunk.getData()));
        }
    }

    protected void decodeAUTH(EightSVXAudioClip sample, IFFChunk chunk)
            throws ParseException {
        if (chunk != null) {
            sample.setAuthor(new String(chunk.getData()));
        }
    }

    protected void decodeANNO(EightSVXAudioClip sample, IFFChunk[] chunks)
            throws ParseException {
        if (chunks != null) {
            for (int i = 0; i < chunks.length; i++) {
                IFFChunk chunk = chunks[i];
                sample.setRemark(sample.getRemark() + new String(chunk.getData()));
            }
        }
    }

    protected void decodeBODY(EightSVXAudioClip sample, IFFChunk chunk)
            throws ParseException {
        if (chunk != null) {
            byte[] data = chunk.getData();
            sample.set8SVXBody(data);
        }
    }

}

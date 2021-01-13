/* @(#)EightSVXAudioClip.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.eightsvx;

import java.io.IOException;
import java.io.OutputStream;


/**
 * Represents an audio sample of type IFF 8SVX.
 * <p>
 * <b>Supported audio formats:</b>
 * <br>8 bit linear and fibonacci encoded data samples.
 * <br>All sample rates
 * <br>Stereo and Mono
 * <p>
 * <b>Unsupported features:</b>
 * <br>Attack and Release information is ignored.
 * <br>Multi octave samples are not handled.
 * <p>
 * <b>Known Issues</b>
 * <br>This class has been implemented with JDK 1.1 in mind. JDK 1.1 does not
 * have a public API for Sound. This class will thus work only on a small number
 * of Java VMS.
 * <br>Poor sound qualitiy: All data is being converted to U-Law 8000 Hertz,
 * since this is the only kind of audio data that JDK 1.1 supports (As far as I know).
 * <br>Stereo sound is converted to mono. As far as I know there is now stereo
 * support built in JDK 1.1.
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version $Id$
 */
public class EightSVXAudioClip
        implements LoopableAudioClip {
    /* Instance variables */
    private String name_ = "";
    private String author_ = "";
    private String copyright_ = "";
    private String remark_ = "";
    private byte[] body_;

    private long
            oneShotHiSamples_,  // # samples in the high octave 1-shot part
            repeatHiSamples_,  // # samples in the high octave repeat part
            samplesPerHiCycle_;  // # samples/cycle in high octave, else 0
    private int
            sampleRate_,    // data sampling rate
            ctOctave_;      // # octaves of waveforms

    public final static int S_CMP_NONE = 0;  // not compressed
    public final static int S_CMP_FIB_DELTA = 1;  // Fibonacci-delta encoding.
    private int sCompression_;
    // data compression technique used

    private final static double UNITY = 0x10000;
    private int volume_;  // playback volume from 0 to UNITY (full
    // volume). Map this value into the output
    // hardware's dynamic range.

    private LoopableAudioClip cachedAudioClip_;
    private int cachedSampleRate_;

    public final static int RIGHT = 4, LEFT = 2, STEREO = 6;
    private int sampleType_;

    /* Constructors  */

    /* Accessors */
    protected void setName(String value) {
        name_ = value;
    }

    public String getName() {
        return name_;
    }

    protected void setAuthor(String value) {
        author_ = value;
    }

    protected String getAuthor() {
        return author_;
    }

    protected void setCopyright(String value) {
        copyright_ = value;
    }

    protected String getCopyright() {
        return copyright_;
    }

    protected void setRemark(String value) {
        remark_ = value;
    }

    protected String getRemark() {
        return remark_;
    }

    public void set8SVXBody(byte[] value) {
        body_ = value;
        cachedAudioClip_ = null;
        //toAudioData();
    }

    public byte[] get8SVXBody() {
        return body_;
    }

    public void setOneShotHiSamples(long value) {
        oneShotHiSamples_ = value;
    }

    public void setRepeatHiSamples(long value) {
        repeatHiSamples_ = value;
    }

    public void setSamplesPerHiCycle(long value) {
        samplesPerHiCycle_ = value;
    }

    public void setSampleType(int value) {
        sampleType_ = value;
    }

    public void setSampleRate(int value) {
        sampleRate_ = value;
    }

    public void setCtOctave(int value) {
        ctOctave_ = value;
    }

    public void setSCompression(int value) {
        sCompression_ = value;
    }

    public void setVolume(int value) {
        volume_ = value;
    }

    public long getOneShotHiSamples() {
        return oneShotHiSamples_;
    }

    public long getRepeatHiSamples() {
        return repeatHiSamples_;
    }

    public long getSamplesPerHiCycle() {
        return samplesPerHiCycle_;
    }

    public long getSampleType() {
        return sampleType_;
    }

    public int getSampleRate() {
        return sampleRate_;
    }

    public int getCtOctave() {
        return ctOctave_;
    }

    public int getVolume() {
        return volume_;
    }

    public int getSCompression() {
        return sCompression_;
    }

    /* Actions */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (getName().length() == 0) {
            buf.append("<unnamed>");
        } else {
            buf.append(getName());
        }
        if (getAuthor().length() != 0) {
            buf.append(", ");
            buf.append(getAuthor());
        }
        if (getCopyright().length() != 0) {
            buf.append(", � ");
            buf.append(getCopyright());
        }
        buf.append(' ');
        buf.append(Integer.toString(getSampleRate()));
        buf.append(" Hz");
        return buf.toString();
    }

    public LoopableAudioClip createAudioClip() {
        return createAudioClip(getSampleRate(), volume_, 0f);
    }

    /*
     * Does the real work of creating an AudioClip.
     *
     * @param volume The volume setting controls the loudness of the sound.
     * range 0 (mute) to 64 (maximal volume).
     * @param pan The relative pan of a stereo signal between two stereo
     * speakers. The valid range of values is -1.0 (left channel only) to 1.0
     * (right channel  only). The default is 0.0 (centered).
     */
    public LoopableAudioClip createAudioClip(int sampleRate, int volume, float pan) {
        LoopableAudioClip clip = createJDK13AudioClip(sampleRate, volume, pan);
        return clip;
    }

    /**
     * Gets the audio data in 8-bit linear PCM.
     * @return the audio data as 8-bit linear PCM
     */
    public byte[] to8BitLinearPcm() {
        // Decompress the sound data
        if (sCompression_ == S_CMP_FIB_DELTA) {
            return unpackFibonacciDeltaCompression(body_);
        } else {
            return body_.clone();
        }
    }

    public LoopableAudioClip createJDK13AudioClip(int sampleRate, int volume, float pan) {
        // Decompress the sound data
        if (sCompression_ == S_CMP_FIB_DELTA) {
            body_ = unpackFibonacciDeltaCompression(body_);
            sCompression_ = S_CMP_NONE;
        }

        // Make it mono -- FIXME why do we make it mono?
        if (sampleType_ == STEREO) {
            double volumeCorrection = computeStereoVolumeCorrection(body_);
            body_ = linear8StereoToMono(body_, volumeCorrection);
            sampleType_ = LEFT;
        }

        byte[] samples = get8SVXBody();
        if (samples.length > 1000000) {
            return new JDK13LongAudioClip(samples, sampleRate, volume, pan);
        } else {
            return new JDK13ShortAudioClip(samples, sampleRate, volume, pan);
        }
        /*
        try {
            return new JDK13AppletAudioClip(get8SVXBody(), sampleRate, volume, pan);
        } catch (IOException e) {
            throw new InternalError(e.toString());
        }*/
    }


    public void play() {
        stop();
        if (cachedAudioClip_ == null) {
            cachedAudioClip_ = createAudioClip();
        }
        cachedAudioClip_.play();
    }

    public void loop() {
        stop();
        if (cachedAudioClip_ == null) {
            cachedAudioClip_ = createAudioClip();
        }
        cachedAudioClip_.loop();
    }

    public void stop() {
        if (cachedAudioClip_ != null) {
            cachedAudioClip_.stop();
        }
    }

    /**
     * Make this clip ready for playback.
     */
    public void prepare() {
        if (cachedAudioClip_ == null) {
            cachedAudioClip_ = createAudioClip();
        }
    }

    /* Class methods */

    /**
     * This finds the volume correction needed when converting
     * this stereo sample to mono.
     *
     * @param stereo Stereo data linear 8. The first half of the
     *               array contains the sound for the left speaker,
     *               the second half the sound for the right speaker.
     * @return volumeCorrection
     * Combining the two channels into one increases the
     * sound volume. This can exceed the maximum volume
     * that can be represented by the linear8 sample model.
     * To avoid this, the volume must be corrected to fit
     * into the sample model.
     */
    public static double computeStereoVolumeCorrection(byte[] stereo) {
        int half = stereo.length / 2;
        int max = 0;
        for (int i = 0; i < half; i++) {
            max = Math.max(max, Math.abs(stereo[i] + stereo[half + i]));
        }
        if (max < 128) {
            return 1.0;
        } else {
            return 128d / max;
        }
    }

    /**
     * This converts a stereo sample to mono.
     *
     * @param stereo           Stereo data linear 8. The first half of the
     *                         array contains the sound for the left speaker,
     *                         the second half the sound for the right speaker.
     * @param volumeCorrection Combining the two channels into one increases the
     *                         sound volume. This can exceed the maximum volume
     *                         that can be represented by the linear8 sample model.
     *                         To avoid this, the volume must be corrected to fit
     *                         into the sample model.
     */
    public static byte[] linear8StereoToMono(byte[] stereo, double volumeCorrection) {
        int half = stereo.length / 2;
        byte[] mono = new byte[half];
        for (int i = 0; i < half; i++) {
            mono[i] = (byte) ((stereo[i] + stereo[half + i]) * volumeCorrection);
        }
        return mono;
    }

    /**
     * Resamples audio data to match the given sample rate and applies
     * a lowpass filter if necessary.
     *
     * @param input            Linear8 encoded audio data.
     * @param inputSampleRate  The sample rate of the input data
     * @param outputSampleRate The sample rate of the output data.
     * @return Linear8 encoded audio data.
     */
    public static byte[] resample(byte[] input, int inputSampleRate, int outputSampleRate) {
        if (inputSampleRate == outputSampleRate) {

            // No sample rate conversion needed.
            return input;

        } else if (inputSampleRate > outputSampleRate) {
            // Sample rate conversion with downsampling needed.
            // We have to apply a lowpass filter to remove sound
            // frequencies that are higher than half of our destSampleRate
            // (this is the Nyquist frequency).
            //input = lowpassV2(input, inputSampleRate, outputSampleRate / 4f, 128f);
            float factor = inputSampleRate / (float) outputSampleRate;
            byte[] output = new byte[(int) Math.floor(input.length / factor)];

            for (int i = 0; i < output.length; i++) {
                output[i] = input[(int) (i * factor)];
            }
            return output;
        } else {
            // Sample rate conversion with upsampling needed.
            // We insert samples from our input array multiple times into
            // the output data array.
            float factor = inputSampleRate / (float) outputSampleRate;
            byte[] output = new byte[(int) Math.ceil(input.length / factor)];

            for (int i = 0; i < output.length; i++) {
                output[i] = input[(int) (i * factor)];
            }

            return output;
        }
    }

    /**
     * Converts a buffer of signed 8bit samples to uLaw.
     * The uLaw bytes overwrite the original 8 bit values.
     * The first byte-offset of the uLaw bytes is byteOffset.
     * It will be written sampleCount bytes.
     */
    public static byte[] linear8ToULaw(byte[] linear8) {
        byte[] ulaw = new byte[linear8.length];

        for (int i = 0; i < linear8.length; i++) {
            ulaw[i] = linear16ToULaw(linear8[i] << 8);
        }

        return ulaw;
    }

    /**
     * Converts a buffer of signed 8bit samples to uLaw.
     * The uLaw bytes overwrite the original 8 bit values.
     * The first byte-offset of the uLaw bytes is byteOffset.
     * It will be written sampleCount bytes.
     */
    public static byte[] linear16ToULaw(int[] linear16) {
        byte[] ulaw = new byte[linear16.length];

        for (int i = 0; i < linear16.length; i++) {
            ulaw[i] = linear16ToULaw(linear16[i]);
        }

        return ulaw;
    }

    /* ---------------------------------------------------------------------
     * The following section of this software is
     * Copyright 1989 by Steve Hayes
     */
    /**
     * This is Steve Hayes' Fibonacci Delta sound compression technique.
     * It's like the traditional delta encoding but encodes each delta
     * in a mere 4 bits. The compressed data is half the size of the
     * original data plus a 2-byte overhead for the initial value.
     * This much compression introduces some distortion, so try it out
     * and use it with discretion.
     *
     * To achieve a reasonable slew rate, this algorithm looks up each
     * stored 4-bit value in a table of Fibonacci numbers. So very small
     * deltas are encoded precisely while larger deltas are approximated.
     * When it has to make approximations, the compressor should adjust
     * all the values (forwards and backwards in time) for minimal overall
     * distortion.
     */
    /**
     * Fibonacci delta encoding for sound data.
     */
    private final static byte[] CODE_TO_DELTA = {-34, -21, -13, -8, -5, -3, -2, -1, 0, 1, 2, 3, 5, 8, 13, 21};

    /**
     * Unpack Fibonacci-delta encoded data from n byte source buffer
     * into 2*(n-2) byte dest buffer. Source buffer has a pad byte, an 8-bit
     * initial value, followed by n bytes comprising 2*(n) 4-bit
     * encoded samples.
     */
    public static byte[] unpackFibonacciDeltaCompression(byte[] source) {
        /* Original algorithm by Steve Hayes
        int n = source.length - 2;
        int lim = n * 2;
        byte[] dest = new byte[lim];
        int x = source[1];
        int d;
         
        int j=2;
        for (int i=0; i < lim; i++)
          { // Decode a data nibble; high nibble then low nibble.
          d = source[j];       // get a pair of nibbles
          if ( (i & 1) == 1)   // select low or high nibble?
            {
            j++;
            }
          else
            { d >>= 4; }  // shift to get the high nibble
         
          x += CODE_TO_DELTA[d & 0xf]; // add in the decoded delta
          dest[i] = (byte)x; // store a 1-byte sample
          }
         */

        /* Improved algorithm (faster) */
        int n = source.length - 2;
        int lim = n * 2;
        byte[] dest = new byte[lim];
        int x = source[1];
        int d;
        int i = 0;
        for (int j = 2; j < n; j++) {
            // Decode a data nibble; high nibble then low nibble.

            d = source[j];    // Get one byte containing a pair of nibbles

            x += CODE_TO_DELTA[(d >> 4) & 0xf];
            // shift to get the high nibble and add in the
            // decoded delta.
            dest[i++] = (byte) x;
            // store a 1-byte sample

            x += CODE_TO_DELTA[d & 0xf];
            // get the low nibble and add in the
            // decoded delta.
            dest[i++] = (byte) x;
            // store a 1-byte sample
        }

        return dest;
    }

    /* ---------------------------------------------------------------------
     * The following section of this software is
     * Copyright © 1989 by Rich Gopstein and Harris Corporation
     */

    /**
     * Write a "standard" sun header.
     *
     * @param sampleType Specify STEREO, LEFT or RIGHT.
     */
    public static void writeSunAudioHeader(OutputStream outfile, int dataSize, int sampleRate, int sampleType)
            throws IOException {
        wrulong(outfile, 0x2e736e64);  // Sun magic = ".snd"
        wrulong(outfile, 24);  // header size in bytes
        wrulong(outfile, dataSize);  // data size
        wrulong(outfile, 1);  // Sun uLaw format
        wrulong(outfile, sampleRate);  // sample rate (only 8000 is supported by Java 1.1)

        // two channels for stereo sound,
        // one channel for mono (don't care for left or right speakers).
        wrulong(outfile, sampleType == STEREO ? 2 : 1);
    }

    /**
     * Write an unsigned long (Motorola 68000 CPU format).
     */
    public static void wrulong(OutputStream outfile, int ulong)
            throws IOException {
        outfile.write(ulong >> 24 & 0xff);
        outfile.write(ulong >> 16 & 0xff);
        outfile.write(ulong >> 8 & 0xff);
        outfile.write(ulong >> 0 & 0xff);
    }

    /* ---------------------------------------------------------------------
     * The following section of this software is
     * Copyright © 1999,2000 by Florian Bomers <florian@bome.com>
     * Copyright © 2000 by Matthias Pfisterer <matthias.pfisterer@gmx.de>
     */
    private static final boolean ZEROTRAP = true;
    private static final short BIAS = 0x84;
    private static final int CLIP = 32635;
    private static final int exp_lut1[] = {
            0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3,
            4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
            7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
    };


    /**
     * Converts a linear signed 16bit sample to a uLaw byte.
     * Ported to Java by fb.
     * <BR>Originally by:<BR>
     * Craig Reese: IDA/Supercomputing Research Center <BR>
     * Joe Campbell: Department of Defense <BR>
     * 29 September 1989 <BR>
     */
    private static byte linear16ToULaw(int sample) {
        int sign, exponent, mantissa, ulawbyte;

        if (sample > 32767) {
            sample = 32767;
        } else if (sample < -32768) {
            sample = -32768;
        }
        /* Get the sample into sign-magnitude. */
        sign = (sample >> 8) & 0x80;    /* set aside the sign */
        if (sign != 0) {
            sample = -sample;    /* get magnitude */
        }
        if (sample > CLIP) {
            sample = CLIP;    /* clip the magnitude */
        }

        /* Convert from 16 bit linear to ulaw. */
        sample = sample + BIAS;
        exponent = exp_lut1[(sample >> 7) & 0xFF];
        mantissa = (sample >> (exponent + 3)) & 0x0F;
        ulawbyte = ~(sign | (exponent << 4) | mantissa);
        if (ZEROTRAP) {
            if (ulawbyte == 0) {
                ulawbyte = 0x02;  /* optional CCITT trap */
            }
        }
        return ((byte) ulawbyte);
    }

    /**
     * Starts looping playback from the current position.   Playback will
     * continue to the loop's end point, then loop back to the loop start point
     * <code>count</code> times, and finally continue playback to the end of
     * the clip.
     * <p>
     * If the current position when this method is invoked is greater than the
     * loop end point, playback simply continues to the
     * end of the clip without looping.
     * <p>
     * A <code>count</code> value of 0 indicates that any current looping should
     * cease and playback should continue to the end of the clip.  The behavior
     * is undefined when this method is invoked with any other value during a
     * loop operation.
     * <p>
     * If playback is stopped during looping, the current loop status is
     * cleared; the behavior of subsequent loop and start requests is not
     * affected by an interrupted loop operation.
     *
     * @param count the number of times playback should loop back from the
     *              loop's end position to the loop's  start position, or
     *              <code>{@link #LOOP_CONTINUOUSLY}</code> to indicate that looping should
     *              continue until interrupted
     */
    public void loop(int count) {
        stop();
        if (cachedAudioClip_ == null) {
            cachedAudioClip_ = createAudioClip();
        }
        cachedAudioClip_.loop(count);
    }

}

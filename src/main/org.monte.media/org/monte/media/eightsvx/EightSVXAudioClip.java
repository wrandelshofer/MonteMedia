/* @(#)EightSVXAudioClip.java
 * Copyright © 1999 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 *
 *
 * Parts of this software (as marked) is
 *   Copyright (c) 21 Jan 1985 Steve Hayes, Electronic Arts
 *
 *   This software is in the public domain.
 *
 *   Published in:
 *   Commodore Electronics Ltd. (1991) Amiga ROM Kernel Manual. Reference Manual.
 *   Devices. Third Edition. Addison-Wesley: Reading.
 *
 * Parts of this software (as marked) is
 *   Copyright (c) 30.4.2000 Olli Niemitalo.
 *
 *   I, as the author and copyright holder, allow you to do anything you wish
 *   with this book free of charge, including copying, printing and republishing.
 *   In return, you must preserve this notification and the book�s website URL
 *   on the title page.
 *
 *   Published In:
 *   Niemitalo, O. (2000). Audio DSP for the Braindead.
 *   Online: http://www.student.oulu.fi/�oniemita/DSP/INDEX.HTM
 *
 * Parts of this software (as marked) is
 *   Copyright (c) 1999,2000 by Florian Bomers <florian@bome.com>
 *   Copyright (c) 2000 by Matthias Pfisterer <matthias.pfisterer@gmx.de>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Library General Public License as published
 *   by the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Library General Public License for more details.
 *
 *   You should have received a copy of the GNU Library General Public
 *   License along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *   Published In:
 *   Tritonus 0.3.1.
 */
package org.monte.media.eightsvx;

import java.io.*;
import javax.sound.sampled.Clip;


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
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.1 2003-04-05 Revised.
 * <br>1.0  1999-10-19
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
    
    public final static int S_CMP_NONE =  0;  // not compressed
    public final static int S_CMP_FIB_DELTA = 1;  // Fibonacci-delta encoding.
    private int sCompression_;
    // data compression technique used
    
    private final static double UNITY = 0x10000;
    private int volume_;  // playback volume from 0 to UNITY (full
    // volume). Map this value into the output
    // hardware's dynamic range.
    
    private LoopableAudioClip cachedAudioClip_;
    private int cachedSampleRate_;
    
    public final static int RIGHT=4, LEFT=2, STEREO=6;
    private int sampleType_;
    
    /* Constructors  */
    
    /* Accessors */
    protected void setName(String value) { name_ = value; }
    protected String getName() { return name_; }
    
    protected void setAuthor(String value) {author_ = value; }
    protected String getAuthor() { return author_; }
    
    protected void setCopyright(String value) { copyright_ = value; }
    protected String getCopyright() { return copyright_; }
    
    protected void setRemark(String value) { remark_ = value; }
    protected String getRemark() { return remark_; }
    
    public void set8SVXBody(byte[] value) {
        body_ = value;
        cachedAudioClip_ = null;
        //toAudioData();
    }
    public byte[] get8SVXBody() { return body_; }
    
    public void setOneShotHiSamples(long value) { oneShotHiSamples_ = value; }
    public void setRepeatHiSamples(long value) { repeatHiSamples_ = value; }
    public void setSamplesPerHiCycle(long value) { samplesPerHiCycle_ = value; }
    public void setSampleType(int value) { sampleType_ = value; }
    
    public void setSampleRate(int value) { sampleRate_ = value; }
    public void setCtOctave(int value) { ctOctave_ = value; }
    public void setSCompression(int value) { sCompression_ = value; }
    public void setVolume(int value) { volume_ = value; }
    
    public long getOneShotHiSamples() { return oneShotHiSamples_; }
    public long getRepeatHiSamples() { return repeatHiSamples_; }
    public long getSamplesPerHiCycle() { return samplesPerHiCycle_; }
    public long getSampleType() { return sampleType_; }
    
    public int getSampleRate() { return sampleRate_; }
    public int getCtOctave() { return ctOctave_; }
    public int getVolume() { return volume_; }
    public int getSCompression() { return sCompression_; }
    /* Actions */
    public String toString() {
        StringBuffer buf = new StringBuffer();
        if (getName().length() == 0) buf.append("<unnamed>");
        else buf.append(getName());
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
    
    public LoopableAudioClip createJDK13AudioClip(int sampleRate , int volume, float pan) {
        // Decompress the sound data
        if (sCompression_ == S_CMP_FIB_DELTA) {
            body_ = unpackFibonacciDeltaCompression(body_);
            sCompression_ = S_CMP_NONE;
        }
        
        // Make it mono
        if (sampleType_ == STEREO) {
            double volumeCorrection = computeStereoVolumeCorrection(body_);
            body_ = linear8StereoToMono(body_,volumeCorrection);
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
     * @param  stereo  Stereo data linear 8. The first half of the
     * array contains the sound for the left speaker,
     * the second half the sound for the right speaker.
     * @return  volumeCorrection
     * Combining the two channels into one increases the
     * sound volume. This can exceed the maximum volume
     * that can be represented by the linear8 sample model.
     * To avoid this, the volume must be corrected to fit
     * into the sample model.
     */
    public static double computeStereoVolumeCorrection(byte[] stereo) {
        int half = stereo.length / 2;
        int max = 0;
        for (int i=0; i < half; i++) {
            max = Math.max(max,Math.abs(stereo[i]+stereo[half+i]));
        }
        if (max < 128) {
            return 1.0;
        } else {
            return 128d / max;
        }
    }
    /**
     *  This converts a stereo sample to mono.
     *
     *  @param  stereo  Stereo data linear 8. The first half of the
     *          array contains the sound for the left speaker,
     *          the second half the sound for the right speaker.
     *  @param  volumeCorrection
     *          Combining the two channels into one increases the
     *          sound volume. This can exceed the maximum volume
     *          that can be represented by the linear8 sample model.
     *          To avoid this, the volume must be corrected to fit
     *          into the sample model.
     */
    public static byte[] linear8StereoToMono(byte[] stereo, double volumeCorrection) {
        int half = stereo.length / 2;
        byte[] mono = new byte[half];
        for (int i=0; i < half; i++) {
            mono[i] = (byte) ((stereo[i]+stereo[half+i]) * volumeCorrection);
        }
        return mono;
    }
    
    /**
     * Resamples audio data to match the given sample rate and applies
     * a lowpass filter if necessary.
     *
     * @param input Linear8 encoded audio data.
     * @param inputSampleRate The sample rate of the input data
     * @param outputSampleRate The sample rate of the output data.
     *
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
            
            for (int i=0; i < output.length; i++) {
                output[i] = input[(int) (i * factor)];
            }
            return output;
        } else {
            // Sample rate conversion with upsampling needed.
            // We insert samples from our input array multiple times into
            // the output data array.
            float factor = inputSampleRate / (float) outputSampleRate;
            byte[] output = new byte[(int) Math.ceil(input.length / factor)];
            
            for (int i=0; i < output.length; i++) {
                output[i] = input[(int) (i * factor)];
            }
            
            return output;
        }
    }
    
    
    /**
     * Applies a lowpass filter to the linear 8 data to avoid distortion when
     * converting from the source sample rate to the destination sample rate.
     * After that the sample data is converted into uLAW.
     *
     * @param  linear8  The samples to convert from linear 8 to uLAW.
     * @param  off    Start offset in the linear8 array.
     * @param  len    The number of bytes to convert.
     * @param  sourceSampleRate  Samples per second of the linear8 data.
     * @param  destSampleRate  Samples per second of the uLAW data (must be 8000 hertz).
     * @param  volume  The volume of the sound (1.0 == Unity).
     * /
     * public static byte[] linear8ToULawWithLowpassAndHeader(byte[] linear8, int off, int len, int sourceSampleRate, int destSampleRate, double volume) {
     * // Compute the factor between the source sample rate and destination sample rate.
     * double factor = (double)sourceSampleRate / (double)destSampleRate;
     *
     * int headersize = 24;
     * int datasize = (sourceSampleRate == destSampleRate) ? len : (int) Math.ceil(len / factor);
     *
     * // create the header
     * byte[] header = {
     * // Sun magic = ".snd"
     * (byte) 0x2e, (byte) 0x73, (byte) 0x6e, (byte) 0x64,
     * // header size in bytes
     * (byte) (headersize >>> 24 & 0xff), (byte) (headersize >>> 16 & 0xff),
     * (byte) (headersize >>> 8 & 0xff), (byte) (headersize & 0xff),
     * // data size in bytes
     * (byte) (datasize >>> 24 & 0xff), (byte) (datasize >>> 16 & 0xff),
     * (byte) (datasize >>> 8 & 0xff), (byte) (datasize & 0xff),
     * /*
     * // data size in bytes (0)
     * (byte) 0, (byte) 0, (byte) 0, 0,
     * /
     * // Sun uLaw format
     * (byte) 0, (byte) 0, (byte) 0, (byte) 1,
     * // sample rate (only 8000 is supported by Java 1.1)
     * (byte) (destSampleRate >>> 24 & 0xff), (byte) (destSampleRate >>> 16 & 0xff),
     * (byte) (destSampleRate >>> 8 & 0xff), (byte) (destSampleRate & 0xff),
     * // one channel for mono (don't care for left or right speakers).
     * (byte) 0, (byte) 0, (byte) 0, (byte) 1
     * };
     *
     * // Create the output array
     * byte[] ulaw = new byte[datasize + headersize];
     * System.arraycopy(header, 0, ulaw, 0, header.length);
     *
     * // uLaw encodes sound values from -8192 to +8191,
     * // This is 32 times more accurate than linear8.
     * // We multiply this value into the volume value
     * // to save one computional step within the for-loop.
     * volume = volume * 32;
     *
     * if (sourceSampleRate == destSampleRate) {
     * // No sample rate conversion needed.
     * // We simply convert each linear8 sample into a ulaw sample.
     *
     * int mask;
     * for (int i=0; i < datasize; i++) {
     * // pick a sample of the linear 8-bit data,
     * // convert it to two's complement,
     * // and finally multiply it with the volume factor.
     * int ch  = (int)( (0x80 - linear8[i + off]) * volume);
     *
     * if (ch < 0) {
     * ch = -ch;
     * mask = 0x7f;
     * }
     * else {
     * mask = 0xff;
     * }
     *
     * if      (ch <   32) { ch = 0xf0 | 15 - (ch / 2); }
     * else if (ch <   96) { ch = 0xe0 | 15 - (ch - 32) / 4; }
     * else if (ch <  224) { ch = 0xd0 | 15 - (ch - 96) / 8; }
     * else if (ch <  480) { ch = 0xc0 | 15 - (ch - 224) / 16; }
     * else if (ch <  992) { ch = 0xb0 | 15 - (ch - 480) / 32; }
     * else if (ch < 2016) { ch = 0xa0 | 15 - (ch - 992) / 64; }
     * else if (ch < 4064) { ch = 0x90 | 15 - (ch - 2016) / 128; }
     * else if (ch < 8160) { ch = 0x80 | 15 - (ch - 4064) /  256; }
     * else                { ch = 0x80; }
     *
     * ulaw[i + headersize] = (byte)(mask & ch);
     * }
     * } else if (factor > 1.0) {
     * // Sample rate conversion with downsampling needed.
     * // We have to apply a lowpass filter to remove sound
     * // frequencies that are higher than half of our destSampleRate
     * // (this is the Nyquist frequency).
     *
     * // Prepare the lowpass filter
     * double resofreq = destSampleRate / 4; // resonation frequency (must be < sampleRate / 4)
     * double amp = 1.0; // magnitude of the resonation frequency (?)
     * double fx = Math.cos(2*Math.PI*resofreq / sourceSampleRate);
     * double c = 2-2*fx;
     * double r = (Math.sqrt(2)*Math.sqrt(Math.pow(-(fx-1),3))+amp*(fx-1))/(amp*fx-1);
     * double pos = 0;
     * double speed = 0;
     *
     *
     * int mask; // mask for the sign in the uLaw code
     * for (int i=0; i < len; i++) {
     * speed = speed + (linear8[i+off] - pos) * c;
     * pos += speed;
     * speed *= r;
     *
     * int j = (int)( i / factor );
     * if (ulaw[j + headersize] == 0) // try to optimize a little bit
     * {
     * int ch = (int)( (0x80 - (byte)pos) * volume );
     *
     * if (ch < 0) {
     * ch = -ch;
     * mask = 0x7f;
     * } else {
     * mask = 0xff;
     * }
     *
     * if       (ch <   32) { ch = 0xf0 | 15 - (ch / 2); }
     * else if (ch <   96) { ch = 0xe0 | 15 - (ch - 32) / 4; }
     * else if (ch <  224) { ch = 0xd0 | 15 - (ch - 96) / 8; }
     * else if (ch <  480) { ch = 0xc0 | 15 - (ch - 224) / 16; }
     * else if (ch <  992) { ch = 0xb0 | 15 - (ch - 480) / 32; }
     * else if (ch < 2016) { ch = 0xa0 | 15 - (ch - 992) / 64; }
     * else if (ch < 4064) { ch = 0x90 | 15 - (ch - 2016) / 128; }
     * else if (ch < 8160) { ch = 0x80 | 15 - (ch - 4064) /  256; }
     * else                { ch = 0x80; }
     *
     * ulaw[j + headersize] = (byte)(mask & ch);
     * }
     * }
     * } else {
     * // Sample rate conversion with upsampling needed.
     * // We insert samples from our source data multiple times into
     * // the ulaw data array.
     *
     * int mask;
     * for (int i=0; i < datasize; i++) {
     * // pick a sample of the linear 8-bit data,
     * // convert it to two's complement,
     * // and finally multiply it with the volume factor.
     * int ch  = (int)( (0x80 - linear8[ (int)(off+i*factor) ]) * volume);
     *
     * if (ch < 0) {
     * ch = -ch;
     * mask = 0x7f;
     * } else {
     * mask = 0xff;
     * }
     *
     * if      (ch <   32) { ch = 0xf0 | 15 - (ch / 2); }
     * else if (ch <   96) { ch = 0xe0 | 15 - (ch - 32) / 4; }
     * else if (ch <  224) { ch = 0xd0 | 15 - (ch - 96) / 8; }
     * else if (ch <  480) { ch = 0xc0 | 15 - (ch - 224) / 16; }
     * else if (ch <  992) { ch = 0xb0 | 15 - (ch - 480) / 32; }
     * else if (ch < 2016) { ch = 0xa0 | 15 - (ch - 992) / 64; }
     * else if (ch < 4064) { ch = 0x90 | 15 - (ch - 2016) / 128; }
     * else if (ch < 8160) { ch = 0x80 | 15 - (ch - 4064) /  256; }
     * else                { ch = 0x80; }
     *
     * ulaw[i + headersize] = (byte)(mask & ch);
     * }
     * }
     * return ulaw;
     * }*/
    /**
     * Applies a lowpass filter to the linear 8 data to avoid distortion when
     * converting from the source sample rate to the destination sample rate.
     * After that the sample data is converted into uLAW.
     *
     * @param  linear8  The samples to convert from linear 8 to uLAW.
     * @param  off    Start offset in the linear8 array.
     * @param  len    The number of bytes to convert.
     * @param  sourceSampleRate  Samples per second of the linear8 data.
     * @param  destSampleRate  Samples per second of the uLAW data (must be 8000 hertz).
     * @param  volume  The volume of the sound (1.0 == Unity).
     * /
     * public static byte[] linear8ToULawWithLowpass(byte[] linear8, int off, int len, int sourceSampleRate, int destSampleRate, double volume) {
     * // Compute the factor between the source sample rate and destination sample rate.
     * double factor = (double)sourceSampleRate / (double)destSampleRate;
     * int datasize = (sourceSampleRate == destSampleRate) ? len : (int) Math.ceil(len / factor);
     *
     * // Create the output array
     * byte[] ulaw = new byte[datasize];
     *
     * // uLaw encodes sound values from -8192 to +8191,
     * // This is 32 times more accurate than linear8.
     * // We multiply this value into the volume value
     * // to save one computional step within the for-loop.
     * volume = volume * 32;
     *
     * if (sourceSampleRate == destSampleRate) {
     * // No sample rate conversion needed.
     * // We simply convert each linear8 sample into a ulaw sample.
     *
     * int mask;
     * for (int i=0; i < datasize; i++) {
     * // pick a sample of the linear 8-bit data,
     * // convert it to two's complement,
     * // and finally multiply it with the volume factor.
     * int ch  = (int)( (0x80 - linear8[i + off]) * volume);
     *
     * if (ch < 0) {
     * ch = -ch;
     * mask = 0x7f;
     * }
     * else {
     * mask = 0xff;
     * }
     *
     * if      (ch <   32) { ch = 0xf0 | 15 - (ch / 2); }
     * else if (ch <   96) { ch = 0xe0 | 15 - (ch - 32) / 4; }
     * else if (ch <  224) { ch = 0xd0 | 15 - (ch - 96) / 8; }
     * else if (ch <  480) { ch = 0xc0 | 15 - (ch - 224) / 16; }
     * else if (ch <  992) { ch = 0xb0 | 15 - (ch - 480) / 32; }
     * else if (ch < 2016) { ch = 0xa0 | 15 - (ch - 992) / 64; }
     * else if (ch < 4064) { ch = 0x90 | 15 - (ch - 2016) / 128; }
     * else if (ch < 8160) { ch = 0x80 | 15 - (ch - 4064) /  256; }
     * else                { ch = 0x80; }
     *
     * ulaw[i] = (byte)(mask & ch);
     * }
     * } else if (factor > 1.0) {
     * // Sample rate conversion with downsampling needed.
     * // We have to apply a lowpass filter to remove sound
     * // frequencies that are higher than half of our destSampleRate
     * // (this is the Nyquist frequency).
     *
     * // Prepare the lowpass filter
     * double resofreq = destSampleRate / 4; // resonation frequency (must be < sampleRate / 4)
     * double amp = 1.0; // magnitude of the resonation frequency (?)
     * double fx = Math.cos(2*Math.PI*resofreq / sourceSampleRate);
     * double c = 2-2*fx;
     * double r = (Math.sqrt(2)*Math.sqrt(Math.pow(-(fx-1),3))+amp*(fx-1))/(amp*fx-1);
     * double pos = 0;
     * double speed = 0;
     *
     *
     * int mask; // mask for the sign in the uLaw code
     * for (int i=0; i < len; i++) {
     * speed = speed + (linear8[i+off] - pos) * c;
     * pos += speed;
     * speed *= r;
     *
     * int j = (int)( i / factor );
     * if (ulaw[j] == 0) // try to optimize a little bit
     * {
     * int ch = (int)( (0x80 - (byte)pos) * volume );
     *
     * if (ch < 0) {
     * ch = -ch;
     * mask = 0x7f;
     * } else {
     * mask = 0xff;
     * }
     *
     * if       (ch <   32) { ch = 0xf0 | 15 - (ch / 2); }
     * else if (ch <   96) { ch = 0xe0 | 15 - (ch - 32) / 4; }
     * else if (ch <  224) { ch = 0xd0 | 15 - (ch - 96) / 8; }
     * else if (ch <  480) { ch = 0xc0 | 15 - (ch - 224) / 16; }
     * else if (ch <  992) { ch = 0xb0 | 15 - (ch - 480) / 32; }
     * else if (ch < 2016) { ch = 0xa0 | 15 - (ch - 992) / 64; }
     * else if (ch < 4064) { ch = 0x90 | 15 - (ch - 2016) / 128; }
     * else if (ch < 8160) { ch = 0x80 | 15 - (ch - 4064) /  256; }
     * else                { ch = 0x80; }
     *
     * ulaw[j] = (byte)(mask & ch);
     * }
     * }
     * } else {
     * // Sample rate conversion with upsampling needed.
     * // We insert samples from our source data multiple times into
     * // the ulaw data array.
     *
     * int mask;
     * for (int i=0; i < datasize; i++) {
     * // pick a sample of the linear 8-bit data,
     * // convert it to two's complement,
     * // and finally multiply it with the volume factor.
     * int ch  = (int)( (0x80 - linear8[ (int)(off+i*factor) ]) * volume);
     *
     * if (ch < 0) {
     * ch = -ch;
     * mask = 0x7f;
     * } else {
     * mask = 0xff;
     * }
     *
     * if      (ch <   32) { ch = 0xf0 | 15 - (ch / 2); }
     * else if (ch <   96) { ch = 0xe0 | 15 - (ch - 32) / 4; }
     * else if (ch <  224) { ch = 0xd0 | 15 - (ch - 96) / 8; }
     * else if (ch <  480) { ch = 0xc0 | 15 - (ch - 224) / 16; }
     * else if (ch <  992) { ch = 0xb0 | 15 - (ch - 480) / 32; }
     * else if (ch < 2016) { ch = 0xa0 | 15 - (ch - 992) / 64; }
     * else if (ch < 4064) { ch = 0x90 | 15 - (ch - 2016) / 128; }
     * else if (ch < 8160) { ch = 0x80 | 15 - (ch - 4064) /  256; }
     * else                { ch = 0x80; }
     *
     * ulaw[i] = (byte)(mask & ch);
     * }
     * }
     *
     * return ulaw;
     * }
     */
    /**
     * Applies a lowpass filter to the audio data.
     *
     * @param input Linear8 encoded audio data.
     * @param c The filter ratio. 1.0 passes all, 0.0 passes nothing.
     *
     * @return Linear 8 encoded audio data.
     * /
     * public static byte[] lowpass(byte[] input, float c) {
     * /* Algorithm taken from
     * "Yehar's digital sound processing tutorial for the braindead!"
     * version 2001.05.02
     * http://www.student.oulu.fi/~oniemita/DSP/DSPSTUFF.TXT
     *
     *        *** Fastest and simplest "lowpass" ever! ***
     *
     * c = 0..1  (1 = passes all, 0 = passes nothing)
     *
     * output(t) = output(t-1) + c*(input(t)-output(t-1))
     *
     * /
     *
     * // Create the output array
     * byte[] output = new byte[input.length];
     *
     * output[0] = 0;
     * for (int t=1; t < input.length; t++) {
     * output[t] = (byte) (output[t - 1] + c * (input[t] - output[t - 1]));
     * }
     *
     * return output;
     * }*/
    /**
     * Applies a lowpass filter to the audio data.
     *
     * @param input Linear8 encoded audio data.
     * @param resofreq Resonation frequency (must be < sampleRate / 4)
     * @param r 0..1, but not 1
     *
     * @return Linear8 encoded audio data.
     * /
     * public static byte[] lowpassV1(byte[] input, int sampleRate, float resofreq, float r) {
     * /* Algorithm taken from
     * "Yehar's digital sound processing tutorial for the braindead!"
     * version 2001.05.02
     * http://www.student.oulu.fi/~oniemita/DSP/DSPSTUFF.TXT
     *
     * *** Fast lowpass with resonance v1 ***
     *
     * Parameters:
     * resofreq = resonation frequency  (must be < SR/4)
     * r = 0..1, but not 1
     *
     * Init:
     * c = 2-2*cos(2*pi*resofreq / samplerate)
     * pos = 0
     * speed = 0
     *
     * Loop:
     * speed = speed + (input(t) - pos) * c
     * pos = pos + speed
     * speed = speed * r
     * output(t) = pos
     * /
     *
     * // Create the output array
     * byte[] output = new byte[input.length];
     *
     * // Init
     * float c = (float) (2d - 2d * Math.cos(2d * Math.PI * resofreq / sampleRate));
     * float pos = 0;
     * float speed = 0;
     *
     * for (int t=0; t < input.length; t++) {
     * speed = speed + (input[t] - pos) * c;
     * pos += speed;
     * speed *= r;
     *
     * output[t] = (byte) pos;
     * }
     *
     * return output;
     * }*/
    /**
     * Applies a lowpass filter to the audio data.
     *
     * @param input Linear8 encoded audio data.
     * @param resofreq Resonation frequency (must be &lt; sampleRate / 4)
     * @param amp Magnitude at the resonation frequency
     *
     * @return Linear8 encoded audio data.
     * /
     * public static byte[] lowpassV2(byte[] input, int sampleRate, float resofreq, float amp) {
     * /* Algorithm taken from
     * "Yehar's digital sound processing tutorial for the braindead!"
     * version 2001.05.02
     * http://www.student.oulu.fi/~oniemita/DSP/DSPSTUFF.TXT
     *
     * *** Fast lowpass with resonance v2 ***
     *
     * Parameters:
     *   resofreq = resonation frequency  (must be &lt; SR/4)
     *   amp = magnitude at the resonation frequency
     *
     * Init:
     *   fx = cos(2*pi*resofreq / samplerate)
     *   c = 2-2*fx
     *   r = (sqrt(2)*sqrt(-(fx-1)^3)+amp*(fx-1))/(amp*(fx-1))
     *   pos = 0
     *   speed = 0
     *
     * Loop:
     *   speed = speed + (input(t) - pos) * c
     *   pos = pos + speed
     *   speed = speed * r
     *   output(t) = pos
     * /
     *
     * // Create the output array
     * byte[] output = new byte[input.length];
     *
     * // Init
     * float fx = (float) Math.cos(2*Math.PI*resofreq / sampleRate);
     * float c = 2 - 2 * fx;
     * float r = (float) (Math.sqrt(2)*Math.sqrt(Math.pow(-(fx-1),3))+amp*(fx-1))/(amp*(fx-1));
     * float pos = 0;
     * float speed = 0;
     *
     * for (int t=0; t &lt; input.length; t++) {
     * speed = speed + (input[t] - pos) * c;
     * pos += speed;
     * speed *= r;
     *
     * output[t] = (byte) pos;
     * }
     *
     * return output;
     * }*/
    /**
     * Applies a lowpass filter to the audio data.
     *
     * @param input Linear8 encoded audio data.
     * @param resofreq Resonation frequency (must be &lt; sampleRate / 4)
     * @param amp Magnitude at the resonation frequency
     *
     * @return Linear8 encoded audio data.
     * /
     * public static int[] lowpassV2(int[] input, int sampleRate, float resofreq, float amp) {
     * /* Algorithm taken from
     * "Yehar's digital sound processing tutorial for the braindead!"
     * version 2001.05.02
     * http://www.student.oulu.fi/~oniemita/DSP/DSPSTUFF.TXT
     *
     * *** Fast lowpass with resonance v2 ***
     *
     * Parameters:
     *   resofreq = resonation frequency  (must be &lt; SR/4)
     *   amp = magnitude at the resonation frequency
     *
     * Init:
     *   fx = cos(2*pi*resofreq / samplerate)
     *   c = 2-2*fx
     *   r = (sqrt(2)*sqrt(-(fx-1)^3)+amp*(fx-1))/(amp*(fx-1))
     *   pos = 0
     *   speed = 0
     *
     * Loop:
     *   speed = speed + (input(t) - pos) * c
     *   pos = pos + speed
     *   speed = speed * r
     *   output(t) = pos
     * /
     *
     * // Create the output array
     * int[] output = new int[input.length];
     *
     * // Init
     * double fx = Math.cos(2*Math.PI*resofreq / sampleRate);
     * double c = 2 - 2 * fx;
     * double r = (Math.sqrt(2)*Math.sqrt(Math.pow(-(fx-1),3))+amp*(fx-1))/(amp*(fx-1));
     * double pos = 0;
     * double speed = 0;
     *
     * for (int t=0; t &lt; input.length; t++) {
     * speed = speed + (input[t] - pos) * c;
     * pos += speed;
     * speed *= r;
     *
     * output[t] = (int) pos;
     * }
     *
     * return output;
     * }*/
    
    /**
     * Halfband lowpass.
     * /
     * public static byte[] halfbandLowpass(byte[] input) {
     * /* Algorithm taken from
     * "Yehar's digital sound processing tutorial for the braindead!"
     * version 2001.05.02
     * http://www.student.oulu.fi/~oniemita/DSP/DSPSTUFF.TXT
     *** Halfband lowpass ***
     *
     * b1  =  0.641339     b10 = -0.0227141
     * b2  = -3.02936
     * b3  =  1.65298      a0a12 = 0.008097
     * b4  = -3.4186       a1a11 = 0.048141
     * b5  =  1.50021      a2a10 = 0.159244
     * b6  = -1.73656      a3a9  = 0.365604
     * b7  =  0.554138     a4a8  = 0.636780
     * b8  = -0.371742     a5a7  = 0.876793
     * b9  =  0.0671787    a6    = 0.973529
     *
     * output(t) = a0a12*(input(t  ) + input(t-12))
     * + a1a11*(input(t-1) + input(t-11))
     * + a2a10*(input(t-2) + input(t-10))
     * + a3a9* (input(t-3) + input(t-9 ))
     * + a4a8* (input(t-4) + input(t-8 ))
     * + a5a7* (input(t-5) + input(t-7 )) + a6*input(t-6)
     * + b1*output(t-1) + b2*output(t-2) + b3*output(t-3)
     * + b4*output(t-4) + b5*output(t-5) + b6*output(t-6)
     * + b7*output(t-7) + b8*output(t-8) + b9*output(t-9)
     * + b10*output(t-10)
     * /
     * double b1  =  0.641339,     b10 = -0.0227141,
     * b2  = -3.02936,
     * b3  =  1.65298,     a0a12 = 0.008097,
     * b4  = -3.4186 ,    a1a11 = 0.048141,
     * b5  =  1.50021,   a2a10 = 0.159244,
     * b6  = -1.73656,      a3a9  = 0.365604,
     * b7  =  0.554138,     a4a8  = 0.636780,
     * b8  = -0.371742 ,    a5a7  = 0.876793,
     * b9  =  0.0671787 ,   a6    = 0.973529;
     *
     * // Create the output array
     * byte[] output = new byte[input.length];
     *
     * for (int t=12; t &lt; input.length; t++) {
     * output[t] = (byte) (
     * a0a12*(input[t] + input[t-12])
     * + a1a11*(input[t-1] + input[t-11])
     * + a2a10*(input[t-2] + input[t-10])
     * + a3a9* (input[t-3] + input[t-9 ])
     * + a4a8* (input[t-4] + input[t-8 ])
     * + a5a7* (input[t-5] + input[t-7 ]) + a6*input[t-6]
     * + b1*output[t-1] + b2*output[t-2] + b3*output[t-3]
     * + b4*output[t-4] + b5*output[t-5] + b6*output[t-6]
     * + b7*output[t-7] + b8*output[t-8] + b9*output[t-9]
     * + b10*output[t-10]
     * );
     * }
     * return output;
     * }
     */
     /**
     * Converts a buffer of signed 8bit samples to uLaw.
     * The uLaw bytes overwrite the original 8 bit values.
     * The first byte-offset of the uLaw bytes is byteOffset.
     * It will be written sampleCount bytes.
     */
    public static byte[] linear8ToULaw(byte[] linear8) {
        byte[] ulaw = new byte[linear8.length];
        
        for (int i=0; i < linear8.length; i++) {
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
        
        for (int i=0; i < linear16.length; i++) {
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
    /** Fibonacci delta encoding for sound data. */
    private final static byte[] CODE_TO_DELTA = {-34,-21,-13,-8,-5,-3,-2,-1,0,1,2,3,5,8,13,21};
    /**
     * Unpack Fibonacci-delta encoded data from n byte source buffer
     * into 2*(n-2) byte dest buffer. Source buffer has a pad byte, an 8-bit
     * initial value, followed by n bytes comprising 2*(n) 4-bit
     * encoded samples.
     *
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
        int i=0;
        for (int j=2; j < n; j++) {
            // Decode a data nibble; high nibble then low nibble.
            
            d = source[j];    // Get one byte containig a pair of nibbles
            
            x += CODE_TO_DELTA[ (d >> 4) & 0xf];
            // shift to get the high nibble and add in the
            // decoded delta.
            dest[i++] = (byte)x;
            // store a 1-byte sample
            
            x += CODE_TO_DELTA[ d & 0xf ];
            // get the low nibble and add in the
            // decoded delta.
            dest[i++] = (byte)x;
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
        wrulong(outfile,0x2e736e64);  // Sun magic = ".snd"
        wrulong(outfile,24);  // header size in bytes
        wrulong(outfile,dataSize);  // data size
        wrulong(outfile,1);  // Sun uLaw format
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
        outfile.write(ulong >>  8 & 0xff);
        outfile.write(ulong >>  0 & 0xff);
    }
    
    /* ---------------------------------------------------------------------
     * The following section of this software is
     * Copyright © 1999,2000 by Florian Bomers <florian@bome.com>
     * Copyright © 2000 by Matthias Pfisterer <matthias.pfisterer@gmx.de>
     */
    private static final boolean ZEROTRAP=true;
    private static final short BIAS=0x84;
    private static final int CLIP=32635;
    private static final int exp_lut1[] ={
        0,0,1,1,2,2,2,2,3,3,3,3,3,3,3,3,
        4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
        5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
        5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
        6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
        6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
        6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
        6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
        7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7
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
        
        if (sample>32767) sample=32767;
        else if (sample<-32768) sample=-32768;
        /* Get the sample into sign-magnitude. */
        sign = (sample >> 8) & 0x80;    /* set aside the sign */
        if (sign != 0) sample = -sample;    /* get magnitude */
        if (sample > CLIP) sample = CLIP;    /* clip the magnitude */
        
        /* Convert from 16 bit linear to ulaw. */
        sample = sample + BIAS;
        exponent = exp_lut1[(sample >> 7) & 0xFF];
        mantissa = (sample >> (exponent + 3)) & 0x0F;
        ulawbyte = ~(sign | (exponent << 4) | mantissa);
        if (ZEROTRAP)
            if (ulawbyte == 0) ulawbyte = 0x02;  /* optional CCITT trap */
        return((byte) ulawbyte);
    }
    
    /** Starts looping playback from the current position.   Playback will
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
     * loop's end position to the loop's  start position, or
     * <code>{@link #LOOP_CONTINUOUSLY}</code> to indicate that looping should
     * continue until interrupted
     *
     */
    public void loop(int count) {
        stop();
        if (cachedAudioClip_ == null) {
            cachedAudioClip_ = createAudioClip();
        }
        cachedAudioClip_.loop(count);
    }
    
}

/*
 * @(#)ANIMAudioTrack.java
 * Copyright © 2021 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.anim;

import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.Track;
import org.monte.media.av.codec.audio.AudioFormatKeys;
import org.monte.media.eightsvx.EightSVXAudioClip;
import org.monte.media.math.Rational;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.END_OF_MEDIA;
import static org.monte.media.av.BufferFlag.KEYFRAME;

public class ANIMAudioTrack implements Track {
    private Format format;
    private ANIMDemultiplexer demux;

    private AudioMixer mixer;

    private long position;

    public ANIMAudioTrack(ANIMDemultiplexer demux) {
        this(demux,false);

    }

    public ANIMAudioTrack(ANIMDemultiplexer demux, boolean swapLeftRightChannels) {
        this.demux = demux;
        this.mixer = new AudioMixer();

        int jiffies = demux.getJiffies();
        int frameTimeInJiffies = 0;
        ANIMMovieResources res = demux.getResources();
        try {
            for (int i = 0, n = demux.getFrameCount(); i < n; i++) {
                ANIMFrame frame = res.getFrame(i);
                System.out.println("#"+i+" frame op "+frame.getOperation()+" "+demux.getDuration(i)+"");
                Rational frameTime = new Rational(frameTimeInJiffies, jiffies);
                if (frame.getAudioCommands() != null) {
                    for (ANIMAudioCommand cmd : frame.getAudioCommands()) {
                        if (cmd.getCommand() == ANIMAudioCommand.COMMAND_PLAY_SOUND) {
                            EightSVXAudioClip audioClip = (EightSVXAudioClip) res.getAudioClip(cmd.getSound() - 1);
                            byte[] linearPcm = audioClip.to8BitLinearPcm();
                            AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(linearPcm), getSourceFormat(cmd, res), linearPcm.length);

                            System.out.println(
                                    " #"+i
                                            +" "
                                            +new Rational(frameTimeInJiffies,jiffies).floatValue()
                                            +" "
                                            +     cmd.getSound()+" "
                                            +audioClip.getName()+" "+cmd.getPan());

                            int pan = (int) cmd.getPan();

                            mixer.add(audioInputStream, frameTime,
                                    cmd.getRepeats(),
                                    cmd.getVolume() / 64f,
                                    swapLeftRightChannels?-pan:pan);
                        }
                    }
                }

                frameTimeInJiffies += demux.getDuration(i);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public long getSampleCount() {
        return mixer.getSampleCount();
    }

    @Override
    public void setPosition(long pos) {
        this.position = pos;
    }

    @Override
    public long getPosition() {
        return position;
    }

    @Override
    public void read(Buffer buf) throws IOException {
        if (position == 0) {
            byte[] convertedBytes = mixer.toByteArray();

            buf.data = convertedBytes;
            buf.format = getFormat();
            buf.timeStamp = new Rational(0, 1);//new Rational(startTimeInJiffies, res.getJiffies());
            buf.sampleCount = convertedBytes.length / 4;
            buf.sampleDuration = new Rational(1,
                    44100);
            buf.offset = 0;
            buf.length = convertedBytes.length;

            buf.setFlagsTo(buf.sampleCount == 0 ? DISCARD : KEYFRAME);

            position += mixer.getSampleCount();
        } else {
            buf.setFlagsTo(END_OF_MEDIA, DISCARD);
        }
    }

    @Override
    public Format getFormat() {
        if (format == null) {
            format = new Format(
                    FormatKeys.MediaTypeKey, FormatKeys.MediaType.AUDIO,
                    AudioFormatKeys.SampleRateKey, new Rational(44100, 1),
                    AudioFormatKeys.SampleSizeInBitsKey, 16,
                    AudioFormatKeys.ChannelsKey, 2,
                    AudioFormatKeys.FrameSizeKey, 4,
                    FormatKeys.EncodingKey, AudioFormatKeys.ENCODING_PCM_SIGNED
            );
        }
        return format;
    }


    private AudioFormat getSourceFormat(ANIMAudioCommand cmd,
                                        ANIMMovieResources res) {
        EightSVXAudioClip eightSVXAudioClip = (EightSVXAudioClip) res.getAudioClip(cmd.getSound() - 1);
        int sampleRate = cmd.getFrequency() == 0 ? eightSVXAudioClip.getSampleRate() : cmd.getFrequency();
        float pan = cmd.getPan();
        return new AudioFormat(
                sampleRate,
                8,
                1, true, true);

    }

    private AudioFormat getTargetFormat() {
        return new AudioFormat(
                44100,
                16,
                2, true, true);

    }
}

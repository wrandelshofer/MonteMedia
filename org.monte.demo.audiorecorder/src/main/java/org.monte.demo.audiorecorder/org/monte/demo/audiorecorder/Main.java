/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.audiorecorder;

import org.monte.media.av.Buffer;
import org.monte.media.av.codec.audio.AudioFormatKeys;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * {@code Main} records audio into an AVI file.
 *
 * @author Werner Randelshofer
 */
public class Main implements Runnable {

    private volatile Thread worker;
    private final File file;
    private final AudioTargetInfo audioTargetInfo;

    public Main(File file, AudioTargetInfo audioTargetInfo) {
        this.file = file;
        this.audioTargetInfo = audioTargetInfo;
    }

    public void start() throws LineUnavailableException {
        stop();
        worker = new Thread(this);
        worker.start();
    }

    public void stop() {
        if (worker != null) {
            Thread stopMe = worker;
            worker = null;
            try {
                if (stopMe != null) {
                    stopMe.join();
                }
            } catch (InterruptedException ex) {
                //ignore
            }
        }
    }

    /**
     * This method is called from the worker thread.
     */
    @Override
    public void run() {
        AVIWriter writer = null;
        TargetDataLine line = null;
        try {
            line = (TargetDataLine) audioTargetInfo.mixer.getLine(audioTargetInfo.info);
            AudioFormat lineFormat = line.getFormat();
            Buffer buf = new Buffer();
            buf.format = AudioFormatKeys.fromAudioFormat(lineFormat);
            buf.sampleDuration = new Rational(1, (long) lineFormat.getSampleRate());
            buf.data = new byte[(int) (lineFormat.getFrameSize() * lineFormat.getSampleRate())];
            writer = new AVIWriter(file);
            writer.addTrack(buf.format);
            line.open();
            // Make sure the volume of the line is bigger than 0.2
            try {
                FloatControl ctrl = (FloatControl) line.getControl(FloatControl.Type.VOLUME);
                ctrl.setValue(Math.max(ctrl.getValue(), 0.2f));
            } catch (IllegalArgumentException e) {
                // We can't change the volume from Java
            }
            line.start();


            while (worker == Thread.currentThread()) {
                buf.length = line.read((byte[]) buf.data, 0, ((byte[]) buf.data).length);
                buf.sampleCount = buf.length / lineFormat.getFrameSize();
                writer.write(0, buf);
            }
        } catch (IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        } finally {
            if (line != null) {
                line.close();
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    record AudioTargetInfo(Mixer mixer, Line.Info info, AudioFormat format) {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, LineUnavailableException {
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH.mm.ss").withZone(ZoneId.systemDefault());
        ;
        File file = new File(System.getProperty("user.home"), "Movies/AudioRecording " + dateFormat.format(Instant.now()) + ".avi");
        if (!file.getParentFile().isDirectory()) {
            file.getParentFile().mkdirs();
        }

        System.out.println("Available target data lines:\n");
        List<AudioTargetInfo> targetLines = new ArrayList<>();
        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
            System.out.println("  " + info);
            Mixer mixer = AudioSystem.getMixer(info);
            for (Line.Info info1 : mixer.getTargetLineInfo()) {
                System.out.println("    " + info1);
                if (info1 instanceof DataLine.Info) {
                    DataLine.Info dlInfo = (DataLine.Info) info1;
                    for (AudioFormat format : dlInfo.getFormats()) {
                        if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED && format.getSampleRate() != AudioSystem.NOT_SPECIFIED) {
                            System.out.println((targetLines.size() + 1) + ".    " + format);
                            targetLines.add(new AudioTargetInfo(mixer, info1, format));
                        }
                    }
                }
            }
        }

        System.out.println("\nEnter the number of the desired source line and press ENTER.");
        StringBuffer line = new StringBuffer();
        for (int ch = System.in.read(); ch != '\n'; ch = System.in.read()) line.append((char) ch);
        Integer index = Integer.valueOf(line.toString());
        AudioTargetInfo sourceLineInfo = targetLines.get(index - 1);
        System.out.println("You have selected line " + index);


        System.out.println("Press ENTER to start audio recording.");
        while (System.in.read() != '\n') ;
        Main r = new Main(file, sourceLineInfo);
        r.start();
        System.out.println("Recording...\nPress ENTER to stop audio recording.");
        while (System.in.read() != '\n') ;
        r.stop();

        System.out.println("You can find the recorded audio in file");
        System.out.println(file);
    }
}

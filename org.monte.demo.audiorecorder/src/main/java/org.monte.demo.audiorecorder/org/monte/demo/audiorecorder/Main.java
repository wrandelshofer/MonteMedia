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
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * {@code Main} records audio into an AVI file.
 *
 * @author Werner Randelshofer
 */
public class Main implements Runnable {

    private volatile Thread worker;
    private File file;

    public Main(File file) {
        this.file = file;
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
                stopMe.join();
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
        DataLine.Info lineInfo = new DataLine.Info(
                TargetDataLine.class, new AudioFormat(44100, 16, 2, true, true));

        AVIWriter writer = null;
        TargetDataLine line = null;
        try {
            line = (TargetDataLine) AudioSystem.getLine(lineInfo);
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
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (LineUnavailableException ex) {
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, LineUnavailableException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd 'at' HH.mm.ss");
        File file = new File(System.getProperty("user.home"), "Movies/AudioRecording " + dateFormat.format(new Date()) + ".avi");
        if (!file.getParentFile().isDirectory()) {
            file.getParentFile().mkdirs();
        }


        System.out.println("Press ENTER to start audio recording.");
        while (System.in.read() != '\n') ;
        Main r = new Main(file);
        r.start();
        System.out.println("Recording...\nPress ENTER to stop audio recording.");
        while (System.in.read() != '\n') ;
        r.stop();

        System.out.println("You can find the recorded audio in file");
        System.out.println(file);
    }
}

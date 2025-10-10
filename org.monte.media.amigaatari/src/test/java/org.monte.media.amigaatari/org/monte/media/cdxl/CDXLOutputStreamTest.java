/*
 * @(#)CDXLOutputStreamTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.cdxl;

import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.ilbm.ILBMDecoder;

import javax.imageio.stream.FileImageOutputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class CDXLOutputStreamTest {
    public static void main(String... args) throws Exception {
        /*
        Path audioFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/Tron/Tron-Stereo-22050.aiff");
        Path outputFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/Tron/Tron.cdxl");
        Path framesDir = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/Tron/Frames");
        new CDXLOutputStreamTest().convert(framesDir, audioFile, outputFile, Integer.MAX_VALUE);
        /*
        Path audioFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/OnePiece/OnePiece-Stereo-22050.aiff");
        Path outputFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/OnePiece/OnePiece.cdxl");
        Path framesDir = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/OnePiece/Frames");
        new CDXLOutputStreamTest().convert(framesDir, audioFile, outputFile, Integer.MAX_VALUE);
        /*
        Path audioFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/Monument/Monument-Stereo-22050.aiff");
        Path outputFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/Monument/Monument.cdxl");
        Path framesDir = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/Monument/Frames");
        new CDXLOutputStreamTest().convert(framesDir, audioFile, outputFile, Integer.MAX_VALUE);
        /*
        Path audioFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/Anoana/Anoana-Stereo-22050.aiff");
        Path outputFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/Anoana/Anoana.cdxl");
        Path framesDir = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/Anoana/Frames");
        new CDXLOutputStreamTest().convert(framesDir, audioFile, outputFile, Integer.MAX_VALUE);
        * /
        Path audioFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/DanDaDan3/DanDaDan3-Stereo-22050.aiff");
        Path outputFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/DanDaDan3/DanDaDan3.cdxl");
        Path framesDir = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/DanDaDan3/Frames");
        new CDXLOutputStreamTest().convert(framesDir, audioFile, outputFile, 2991);
        */
        Path audioFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/DanDaDan2/DanDaDan2-Stereo-22050.aiff");
        Path outputFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/DanDaDan2/DanDaDan2.cdxl");
        Path framesDir = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/DanDaDan2/Frames");
        new CDXLOutputStreamTest().convert(framesDir, audioFile, outputFile, 2847);
        /*
        Path audioFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/Mulan/MulanHi-Stereo-22050.aiff");
        Path outputFile = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/Mulan/Mulan-24fps-HAM8-HamConvertTest.cdxl");
        Path framesDir = Paths.get(System.getProperty("user.home"), "Documents/Virtualization/Amiga3000/Work/Media/Mulan/Frames");
        new CDXLOutputStreamTest().convert(framesDir, audioFile, outputFile,Integer.MAX_VALUE);
         */
    }

    public void convert(Path framesDir, Path audioFile, Path outputFile, int maxFrames) throws Exception {

        Files.deleteIfExists(outputFile);
        try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile.toFile());
             CDXLOutputStream out = new CDXLOutputStream(new FileImageOutputStream(outputFile.toFile()));
             Stream<Path> stream = Files.find(
                     framesDir,
                     Integer.MAX_VALUE,
                     (p, attr) ->
                             p.getFileName().toString().endsWith("_output.iff")
                                     //&& p.getFileName().toString().startsWith("040")
                                     && attr.isRegularFile()
             )) {

            AudioFormat audioFormat = audioInputStream.getFormat();
            float sampleRate = audioFormat.getSampleRate();
            int frequency = (int) sampleRate;
            int audioFrameSize = audioFormat.getFrameSize();

            int videoFrameCount = 0;
            int audioFrameCount = 0;

            List<Path> imageFiles = stream.
                    sorted(Comparator.comparing(p -> p.getFileName().toString())).toList();
            double frameRate = (double) imageFiles.size() / ((double) audioInputStream.available() / (double) audioFrameSize / sampleRate);
            boolean stereo = audioFormat.getChannels() > 1 ? true : false;
            System.out.println("AudioFormat: " + audioFormat);

            int count = 0;
            for (Path p : imageFiles) {
                ILBMDecoder d = new ILBMDecoder(p.toUri().toURL());
                d.setForce8BitsPerChannel(true);
                ArrayList<AmigaBitmapImage> bitmaps = d.produceBitmaps();
                AmigaBitmapImage bitmap = bitmaps.getFirst();
                videoFrameCount++;

                int requiredAudioFrames = (int) (videoFrameCount * frequency / frameRate);
                int delta = requiredAudioFrames - audioFrameCount;
                delta += delta % 2; // we must write an even number of samples
                audioFrameCount += delta;

                byte[] audioSamples = new byte[delta * audioFrameSize];
                int actualSampleCount = audioInputStream.read(audioSamples);
                if (actualSampleCount != audioSamples.length) {
                    audioSamples = Arrays.copyOf(audioSamples, actualSampleCount);
                }
                if (stereo) {
                    // audio samples must not be interleaved
                    byte[] tmp = audioSamples;
                    audioSamples = new byte[audioSamples.length];
                    int halfLength = audioSamples.length / 2;
                    for (int i = 0, j = 0; i < tmp.length; i += 2, j++) {
                        audioSamples[j] = tmp[i];
                        audioSamples[j + halfLength] = tmp[i + 1];
                    }
                }

                out.write(bitmap, stereo, frequency, audioSamples);

                System.out.println(p + " delta=" + delta * audioFrameSize + ":" + actualSampleCount + " bitmap" + bitmap);
                count++;
                if (count >= maxFrames) {
                    break;
                }
            }
            System.out.println("remaining audio samples=" + audioInputStream.available());
        }
    }
}

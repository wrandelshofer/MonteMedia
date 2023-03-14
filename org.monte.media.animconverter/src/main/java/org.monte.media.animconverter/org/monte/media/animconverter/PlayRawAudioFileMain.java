/*
 * @(#)PlayRawAudioFileMain.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.animconverter;

import org.monte.media.iff.IFFOutputStream;
import org.monte.media.math.ExtendedReal;

import javax.imageio.stream.FileImageOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PlayRawAudioFileMain {
    public static void main(String[] args) throws IOException {
        ExtendedReal r = new ExtendedReal(44100);

        byte[] linearPcm = Files.readAllBytes(Paths.get(args[0]));

        try (IFFOutputStream out = new IFFOutputStream(new FileImageOutputStream(Paths.get(args[0] + ".aiff").toFile()))) {
            out.pushCompositeChunk("FORM", "AIFF");
            out.pushDataChunk("COMM");
            out.writeWORD(1);//numChannels
            out.writeULONG(linearPcm.length);//numSampleFrames
            out.writeWORD(8);//bits per sample
            out.write(new ExtendedReal(44100).toByteArray());//sampleRate
            out.popChunk();
            out.pushDataChunk("SSND");
            out.writeULONG(0);//offset
            out.writeULONG(linearPcm.length);//blockSize
            out.write(linearPcm);//soundData
            out.popChunk();
            out.popChunk();
        }
    }

}

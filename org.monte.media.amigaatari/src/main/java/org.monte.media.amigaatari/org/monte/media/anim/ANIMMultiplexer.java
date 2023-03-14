/*
 * @(#)ANIMMultiplexer.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.anim;

import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.av.Buffer;
import org.monte.media.av.Multiplexer;
import org.monte.media.math.Rational;

import javax.imageio.stream.ImageOutputStream;
import java.io.File;
import java.io.IOException;

import static java.lang.Math.max;
import static org.monte.media.av.BufferFlag.DISCARD;

/**
 * {@code ANIMMultiplexer}.
 *
 * @author Werner Randelshofer
 */
public class ANIMMultiplexer extends ANIMOutputStream implements Multiplexer {

    protected Rational inputTime;

    public ANIMMultiplexer(File file) throws IOException {
        super(file);
    }

    public ANIMMultiplexer(ImageOutputStream out) throws IOException {
        super(out);
    }

    @Override
    public void write(int trackIndex, Buffer buf) throws IOException {
        if (!buf.isFlag(DISCARD)) {
            // FIXME - For each track, fix accumulating rounding errors!!!
            //         Or maybe, just let them accumulate. In case the
            //         frames are compressed, we can't do anything at this
            //         stage anyway.
            long jiffies = getJiffies();

            if (inputTime == null) {
                inputTime = new Rational(0, 1);
            }
            inputTime = inputTime.add(buf.sampleDuration.multiply(buf.sampleCount));

            Rational outputTime = new Rational(getMovieTime(), jiffies);
            Rational outputDuration = inputTime.subtract(outputTime);


            outputDuration = outputDuration.round(jiffies);
            int outputMediaDuration = max(1, (int) (outputDuration.getNumerator() * jiffies / outputDuration.getDenominator()));

            outputTime =
                    outputTime.add(new Rational(outputMediaDuration, jiffies));
            // System.out.println("ANIMMultiplexer #" + frameCount + " jiffies:"+jiffies+" movieT:" + outputTime + " inputT:" + inputTime+" diff:"+(outputTime.subtract(inputTime))+ " sampleDuration:" + outputMediaDuration + " == " + outputDuration+" ~= "+buf.sampleDuration);

            writeFrame((AmigaBitmapImage) buf.data, outputMediaDuration);
        }
    }
}

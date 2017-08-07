/* @(#)ANIMMultiplexer.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.anim;

import org.monte.media.player.Buffer;
import org.monte.media.player.Multiplexer;
import org.monte.media.image.BitmapImage;
import org.monte.media.math.Rational;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.*;
import static org.monte.media.player.BufferFlag.*;

/**
 * {@code ANIMMultiplexer}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-20 Created.
 */
public class ANIMMultiplexer extends ANIMOutputStream implements Multiplexer {

    protected Rational inputTime;

    public ANIMMultiplexer(File file) throws IOException {
        super(file);
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
            inputTime=inputTime.add(buf.sampleDuration.multiply(buf.sampleCount));
            
            Rational outputTime = new Rational(getMovieTime(), jiffies);
            Rational outputDuration = inputTime.subtract(outputTime);


            outputDuration = outputDuration.round(jiffies);
            int outputMediaDuration =max(1,(int)( outputDuration.getNumerator() *jiffies/outputDuration.getDenominator()));

            outputTime=
            outputTime.add(new Rational(outputMediaDuration,jiffies));
           // System.out.println("ANIMMultiplexer #" + frameCount + " jiffies:"+jiffies+" movieT:" + outputTime + " inputT:" + inputTime+" diff:"+(outputTime.subtract(inputTime))+ " sampleDuration:" + outputMediaDuration + " == " + outputDuration+" ~= "+buf.sampleDuration);

            writeFrame((BitmapImage) buf.data, outputMediaDuration);
        }
    }
}

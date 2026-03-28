/*
 * @(#)ANIMMultiplexer.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.anim;

import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.amigabitmap.AmigaBitmapImageConverter;
import org.monte.media.av.Buffer;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.Multiplexer;
import org.monte.media.math.Rational;

import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static java.lang.Math.max;
import static org.monte.media.av.BufferFlag.DISCARD;

/// `ANIMMultiplexer`.
///
/// @author Werner Randelshofer
public class ANIMMultiplexer extends ANIMOutputStream implements Multiplexer {

    protected Rational inputTime;

    public ANIMMultiplexer(File file) throws IOException {
        super(file);
    }

    public ANIMMultiplexer(ImageOutputStream out) throws IOException {
        super(out);
    }

    @Override
    public int addTrack(Format fmt) throws IOException {
        return 0;
    }

    @Override
    public void setCodec(int trackIndex, Codec codec) {
        // do nothing
    }

    @Override
    public void write(int trackIndex, Buffer buf) throws IOException {
        if (!buf.isFlag(DISCARD)) {
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

            AmigaBitmapImage amigaBitmap = toAmigaBitmap(trackIndex, buf);
            writeFrame(amigaBitmap, outputMediaDuration);
        }
    }

    private static AmigaBitmapImage toAmigaBitmap(int trackIndex, Buffer buf) throws IOException {
        if (buf.data instanceof AmigaBitmapImage bmp) {
            return bmp;
        }
        if (buf.data instanceof BufferedImage img) {
            var bmp = new AmigaBitmapImage(img.getWidth(), img.getHeight(), img.getColorModel().getPixelSize(), img.getColorModel());
            AmigaBitmapImageConverter factory = AmigaBitmapImageConverter.newInstance();
            bmp = factory.toBitmapImage(img, bmp);
            return bmp;
        }
        throw new IOException("can not convert buffer to amiga bitmap, buf.data=" + buf.data);
    }

    @Override
    public void close() throws IOException {
        close();
    }

    /// Sets the Commodore Amiga Graphics Mode. The default value is 0.
    ///
    /// The graphics mode is an or-combination of the monitor ID and the mode ID.
    ///
    /// Example:
    /// ```
    /// setCAMG(PAL_MONITOR_ID|HAM_MODE);
    /// ```
    ///
    /// Also sets the Jiffies for the Graphics Mode.
    public void setCAMG(int newValue) {
        setCAMG(newValue);
    }

    public int getCAMG() {
        return getCAMG();
    }

}

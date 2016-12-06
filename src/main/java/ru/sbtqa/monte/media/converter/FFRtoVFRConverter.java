/* @(#)FFRtoVFRConverter.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.converter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import ru.sbtqa.monte.media.AbstractVideoCodec;
import ru.sbtqa.monte.media.Buffer;
import static ru.sbtqa.monte.media.BufferFlag.*;
import ru.sbtqa.monte.media.Format;
import static ru.sbtqa.monte.media.VideoFormatKeys.*;
import ru.sbtqa.monte.media.math.Rational;

/**
 * This codec converts frames from a fixed frame rate into a variable frame rate
 * by coalescing identical frames.
 * 
 * This codec can be used when the input source has a fixed frame rate and the
 * output sink supports a variable frame rate.
 *
 * @author Werner Randelshofer
 * @version $Id: FFRtoVFRConverter.java 364 2016-11-09 19:54:25Z werner $
 */
public class FFRtoVFRConverter extends AbstractVideoCodec {

    private Rational timeStamp;
    private Rational duration;
    private int[] inputColors;
    private int[] previousColors;
    private Object previousPixels;

    public FFRtoVFRConverter() {
        super(new Format[]{
            new Format(DataClassKey, BufferedImage.class), //
        },
              new Format[]{
                  new Format(DataClassKey, BufferedImage.class, FixedFrameRateKey, false), //
              });
        name = "FFR to VFR";
    }

    @Override
    public Format[] getOutputFormats(Format input) {
        Format forceVFR = new Format(FixedFrameRateKey, false);

        ArrayList<Format> of = new ArrayList<Format>(outputFormats.length);
        for (Format f : outputFormats) {
            of.add(forceVFR.append(f.append(input)));
        }
        return of.toArray(new Format[of.size()]);
    }

    @Override
    public Format setOutputFormat(Format f) {
        Format forceFFR = new Format(FixedFrameRateKey, true);
        Format forceVFR = new Format(FixedFrameRateKey, false);

        for (Format sf : getOutputFormats(f)) {
            if (sf.matches(f)
                  || forceFFR.append(sf).matches(f)
                  || forceVFR.append(sf).matches(f)) {
                this.outputFormat = forceVFR.append(f);
                return sf;
            }
        }
        this.outputFormat = null;
        return null;
    }

    @Override
    public void reset() {
        this.timeStamp = null;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        Format vf = inputFormat;

        if (!in.isFlag(KEYFRAME)) {
            // This codec can only process keyframes.
            return CODEC_FAILED;
        }

        if (in.isFlag(END_OF_MEDIA) && in.isFlag(DISCARD)) {
            // => End of media reached. Flush buffer.
            out.setFlag(END_OF_MEDIA, true);
            if (duration.isZero()) {
                out.setFlag(DISCARD, true);
            } else {
                out.setFlag(END_OF_MEDIA, false);
                out.setFlag(DISCARD, false);
                IndexColorModel newColorModel = new IndexColorModel(8, 256, previousColors, 0, false, -1, DataBuffer.TYPE_BYTE);

                WritableRaster newRaster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                      vf.get(WidthKey), vf.get(HeightKey), 1, null);
                byte[] newPixels = ((DataBufferByte) newRaster.getDataBuffer()).getData();
                System.arraycopy(previousPixels, 0, newPixels, 0, newPixels.length);

                out.data = new BufferedImage(newColorModel, newRaster, false, null);
                out.sampleDuration = duration;
                out.timeStamp = timeStamp;
                duration = new Rational(0, 1);
            }
            return CODEC_OK;
        }
        if (in.isFlag(DISCARD)) {
            // => Discard discarded buffer.
            out.setFlag(DISCARD, true);
            return CODEC_OK;
        }

        BufferedImage inputImage = (BufferedImage) in.data;
        byte[] inputPixels = ((DataBufferByte) inputImage.getRaster().getDataBuffer()).getData();
        IndexColorModel inputColorModel = (IndexColorModel) inputImage.getColorModel();
        if (inputColors == null) {
            inputColors = new int[256];
        }
        inputColorModel.getRGBs(inputColors);

        if (previousPixels == null) {
            // => First image. Copy data and discard output buffer.
            previousPixels = inputPixels.clone();
            previousColors = inputColors.clone();
            duration = in.sampleDuration;
            timeStamp = in.timeStamp;
            out.setFlag(DISCARD, true);
        } else // => Not the first image. Convert fixed rate to variable rate if images are the same.
        if (Arrays.equals((byte[]) previousPixels, inputPixels)
              && Arrays.equals(previousColors, inputColors)) {
            duration = duration.add(in.sampleDuration);
            out.setFlag(DISCARD, true);
        } else {
            IndexColorModel newColorModel = new IndexColorModel(8, 256, previousColors, 0, false, -1, DataBuffer.TYPE_BYTE);

            WritableRaster newRaster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                  vf.get(WidthKey), vf.get(HeightKey), 1, null);
            byte[] newPixels = ((DataBufferByte) newRaster.getDataBuffer()).getData();
            System.arraycopy(previousPixels, 0, newPixels, 0, newPixels.length);

            out.data = new BufferedImage(newColorModel, newRaster, false, null);
            out.sampleDuration = duration;
            out.timeStamp = timeStamp;
            duration = in.sampleDuration;
            timeStamp = in.timeStamp;
            System.arraycopy(inputPixels, 0, previousPixels, 0, inputPixels.length);
            System.arraycopy(inputColors, 0, previousColors, 0, inputColors.length);

        }
        return CODEC_OK;
    }
}

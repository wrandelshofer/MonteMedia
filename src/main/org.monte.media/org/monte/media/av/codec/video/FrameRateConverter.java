/* @(#)FrameRateConverter.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */
package org.monte.media.av.codec.video;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import org.monte.media.av.Buffer;
import static org.monte.media.av.BufferFlag.DISCARD;
import org.monte.media.av.Format;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import org.monte.media.av.FormatKeys.MediaType;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.FixedFrameRateKey;
import org.monte.media.math.Rational;

/**
 * This codec converts frames from one frame rate into another.
 * <p>
 * Makes frames longer if the output time is behind the input time.
 * Drops frames if the output time runs away from the input time.
 * <p>
 * The output of the converter has a variable frame rate but it may still
 * contain identical frames.
 * Thus an additional conversion step with {@link FFRtoVFRConverter} is needed.
 * <p>
 * This codec is needed when the input source has a different frame rate than
 * the output sink.
 * 
 * @author Werner Randelshofer
 * @version $Id: FrameRateConverter.java 364 2016-11-09 19:54:25Z werner $
 */
public class FrameRateConverter extends AbstractVideoCodec {

    private Rational inputTime;
    private Rational outputTime;

    public FrameRateConverter() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO,//
                            EncodingKey,ENCODING_BUFFERED_IMAGE,
                             DataClassKey, BufferedImage.class), //
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO,//
                            EncodingKey,ENCODING_BUFFERED_IMAGE,
                            DataClassKey, BufferedImage.class,
                            FixedFrameRateKey,false), //
                });
        name = "Frame Rate Converter";
    }

    @Override
    public Format[] getOutputFormats(Format input) {
        Format forceVFR = new Format(MediaTypeKey, MediaType.VIDEO,ENCODING_BUFFERED_IMAGE,
                             DataClassKey, BufferedImage.class, FixedFrameRateKey,false);

        ArrayList<Format> of = new ArrayList<Format>(outputFormats.length);
        for (Format f : outputFormats) {
            of.add(forceVFR.append(f.append(input)));
        }
        return of.toArray(new Format[of.size()]);
    }

    @Override
    public Format setOutputFormat(Format f) {
        Format forceFFR = new Format(MediaTypeKey, MediaType.VIDEO,ENCODING_BUFFERED_IMAGE,
                             DataClassKey, BufferedImage.class, FixedFrameRateKey,true);
        Format forceVFR = new Format(MediaTypeKey, MediaType.VIDEO,ENCODING_BUFFERED_IMAGE,
                             DataClassKey, BufferedImage.class, FixedFrameRateKey,false);

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
        inputTime = null;
        outputTime = null;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        // Pass discarded buffers.
        out.setMetaTo(in);
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }

        // Initialize codec.
        if (inputTime == null) {
            inputTime = new Rational(0, 1);
            outputTime = new Rational(0, 1);
        }

        // Convert from input frame rate to output frame rate.
        Format vf = outputFormat;
        inputTime = inputTime.add(in.sampleDuration);
        Rational outputDuration = inputTime.subtract(outputTime);
        long jiffies = vf.get(FrameRateKey).getNumerator();
        outputDuration = outputDuration.round(jiffies);
        long outputMediaDuration = (int) (outputDuration.getNumerator() * jiffies / outputDuration.getDenominator());
        long remainder = outputMediaDuration % vf.get(FrameRateKey).getDenominator();
        outputDuration = new Rational(outputMediaDuration, jiffies);
        
        // Drop frame if outputTime is too far ahead of inputTime.
        if (outputDuration.isLessOrEqualZero()) {
            out.setFlag(DISCARD, true);
        out.sampleDuration = outputDuration;
        //System.out.println("FrameRateConverter inTS=" + in.timeStamp + " outTS=" + out.timeStamp + " inDur=" + in.sampleDuration + " outDur=" + out.sampleDuration.toDescriptiveString()+"  DISCARD");
            return CODEC_OK;
        }

        // Produce time converted frame.
        out.format = outputFormat;
        out.setDataTo(in);
        out.timeStamp = outputTime;
        out.sampleDuration = outputDuration;
        outputTime = outputTime.add(outputDuration);
        
       // System.out.println("FrameRateConverter inTS=" + in.timeStamp + " outTS=" + out.timeStamp + " inDur=" + in.sampleDuration + " outDur=" + out.sampleDuration.toDescriptiveString());


        return CODEC_OK;
    }
}

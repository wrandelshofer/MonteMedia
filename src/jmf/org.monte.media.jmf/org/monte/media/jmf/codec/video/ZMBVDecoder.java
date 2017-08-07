/* @(#)ZMBVDecoder.java  1.0  2011-08-27
 * Copyright (c) 2011 Werner Randelshofer, Switzerland. 
 * You may only use this file in compliance with the accompanying license terms.
 */
package org.monte.media.jmf.codec.video;

import org.monte.media.avi.codec.video.ZMBVCodecCore;
import java.awt.Dimension;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

/**
 * {@code ZMBVDecoder}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-08-27 Created.
 */
public class ZMBVDecoder extends AbstractVideoDecoder {

    /** RGB bit masks */
    protected static final int bMask = 0x000000ff;
    protected static final int gMask = 0x0000ff00;
    protected static final int rMask = 0x00ff0000;
    private int[] pixels;
    private int[] prevPixels;
    private long previousSequenceNumber = 0;
    private ZMBVCodecCore state;

    public ZMBVDecoder() {
        supportedInputFormats = new VideoFormat[]{new VideoFormat("ZMBV")};
        defaultOutputFormats = new VideoFormat[]{new RGBFormat(
            null, Format.NOT_SPECIFIED,
            Format.intArray,
            Format.NOT_SPECIFIED, // frame rate
            32,
            rMask, gMask, bMask,
            1, Format.NOT_SPECIFIED,
            Format.FALSE, // flipped
            Format.NOT_SPECIFIED // endian
            )};
        pluginName = "DosBox Screen Capture \"ZMBV\"";
    }

    @Override
    protected Format[] getMatchingOutputFormats(Format input) {

        if (supportedInputFormats[0].matches(input)) {
            VideoFormat inf = (VideoFormat) input;

            Dimension s = inf.getSize();

            RGBFormat outf = new RGBFormat(s,
                    s.width < 0 || s.height < 0 ? Format.NOT_SPECIFIED : s.width * s.height,
                    Format.intArray, inf.getFrameRate(), 32, rMask, gMask, bMask, 1, s.width, Format.FALSE, RGBFormat.BIG_ENDIAN);

            return new Format[]{outf};
        }
        return new Format[0];
    }

    @Override
    public int process(Buffer input, Buffer output) {
        //System.err.println("ZMBVDecoder " + input.getTimeStamp());
        if (input.isDiscard()) {
            output.setDiscard(true);
            return BUFFER_PROCESSED_OK;
        }
        if (input.isEOM()) {
            output.setEOM(true);
            output.setData(null);
            return BUFFER_PROCESSED_OK;
        }

        output.copy(input);
        output.setFormat(outputFormat);
        output.setData(pixels);
        output.setOffset(0);
        output.setLength(pixels.length);
        //if (input.)

        byte[] inDat = (byte[]) input.getData();
        int[] swap = prevPixels;
        prevPixels = pixels;
        pixels = swap;

        // Detect if frames were skipped
        long sequenceNumber = input.getSequenceNumber();
        boolean framesWereSkipped = (sequenceNumber != previousSequenceNumber + 1);
        boolean isKeyframe = state.decode(inDat, input.getOffset(), input.getLength(), pixels, prevPixels, outputFormat.getSize().width, outputFormat.getSize().height,
                framesWereSkipped);
        if (framesWereSkipped && !isKeyframe) {
            output.setDiscard(true);
        } else {
            previousSequenceNumber = sequenceNumber;
        } 
        
        setFlag(output, Buffer.FLAG_KEY_FRAME, isKeyframe);
        return BUFFER_PROCESSED_OK;
    }

    @Override
    public void close() {
        pixels = null;
        state = null;
    }

    @Override
    public void open() throws ResourceUnavailableException {
        state = new ZMBVCodecCore();
        pixels = null;
        pixels = new int[outputFormat.getSize().width * outputFormat.getSize().height];
        prevPixels = new int[outputFormat.getSize().width * outputFormat.getSize().height];

    }
}

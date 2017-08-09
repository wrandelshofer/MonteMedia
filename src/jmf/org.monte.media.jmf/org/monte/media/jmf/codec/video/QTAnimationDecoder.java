/* @(#)QTAnimationDecoder.java  
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.jmf.codec.video;

import org.monte.media.quicktime.codec.video.AnimationCodec;
import java.awt.image.BufferedImage;
import javax.media.Buffer;
import javax.media.Format;
import javax.media.ResourceUnavailableException;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;

/**
 * {@code QTAnimationDecoder}.
 * <p>
 * FIXME - Implement this class.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QTAnimationDecoder extends AbstractVideoDecoder {

    @Override
    protected Format[] getMatchingOutputFormats(Format input) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int process(Buffer input, Buffer output) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /*
    private AnimationCodec codec;
    /** RGB bit masks * /
    protected static final int rMask = 0x000000ff;
    protected static final int gMask = 0x0000ff00;
    protected static final int bMask = 0x00ff0000;
    
    public QTAnimationDecoder() {
    supportedInputFormats = new VideoFormat[]{new VideoFormat(VideoFormat.RLE)};
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
    pluginName = "Apple Animation Decoder";
    }
    
    @Override
    public int process(Buffer input, Buffer output) {
    throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void open() throws ResourceUnavailableException {
    initDecoder();
    }
    
    @Override
    public void close() {
    codec = null;
    }
    
    @Override
    public void reset() {
    // no need to init decoder as first frame is always a key frame
    }
    
    // called when video resize is detected, by checkFormat()
    @Override
    protected void videoResized() {
    initDecoder();
    }
    
    protected void initDecoder() {
    codec = new AnimationCodec();
    codec.setInputFormat(new org.monte.media.VideoFormat(//
    org.monte.media.VideoFormat.QUICKTIME_ANIMATION,
    org.monte.media.VideoFormat.QUICKTIME_ANIMATION_COMPRESSOR_NAME,
    byte[].class,//
    ((VideoFormat)inputFormat).getSize().width,//
    ((VideoFormat)inputFormat).getSize().height,//
    ((RGBFormat) inputFormat).getBitsPerPixel()));
    codec.setOutputFormat(new org.monte.media.VideoFormat(//
    org.monte.media.VideoFormat.IMAGE,
    BufferedImage.class,//
    ((VideoFormat)inputFormat).getSize().width,//
    ((VideoFormat)inputFormat).getSize().height,//
    ((RGBFormat) inputFormat).getBitsPerPixel()));
    }*/
}

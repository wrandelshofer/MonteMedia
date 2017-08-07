/* @(#)PassThroughCodec
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this file in compliance with the accompanying license terms. 
 */
package org.monte.media.converter;

import org.monte.media.player.AbstractCodec;
import org.monte.media.player.Buffer;
import org.monte.media.player.BufferFlag;
import org.monte.media.player.Format;
import org.monte.media.math.Rational;

/**
 * {@code PassThroughCodec} passes through all buffers.
 *
 * @author Werner Randelshofer
 * @version $Id: PassThroughCodec.java 364 2016-11-09 19:54:25Z werner $
 */
public class PassThroughCodec extends AbstractCodec {

    public PassThroughCodec() {
        super(new Format[]{
                    new Format(), //
                },
                new Format[]{
                    new Format(), //
                });
        name = "Pass Through";
    }

    @Override
    public Format setInputFormat(Format f) {
        Format fNew= super.setInputFormat(f);
        outputFormat=fNew;
        return fNew;
    }
    

    
    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.setDataTo(in);
        return CODEC_OK;
    }
}

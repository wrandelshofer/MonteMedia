/* @(#)PassThroughCodec
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this file in compliance with the accompanying license terms. 
 */
package ru.sbtqa.monte.media.converter;

import ru.sbtqa.monte.media.AbstractCodec;
import ru.sbtqa.monte.media.Buffer;
import ru.sbtqa.monte.media.BufferFlag;
import ru.sbtqa.monte.media.Format;
import ru.sbtqa.monte.media.math.Rational;

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

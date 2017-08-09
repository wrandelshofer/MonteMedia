/* @(#)PassThroughCodec
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.media.av.codec.time;

import org.monte.media.av.AbstractCodec;
import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
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

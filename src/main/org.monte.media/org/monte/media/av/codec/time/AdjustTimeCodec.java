/* @(#)AdjustTimeCodec
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */
package org.monte.media.av.codec.time;

import org.monte.media.av.AbstractCodec;
import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Format;
import org.monte.media.math.Rational;

/**
 * Adjusts the time stamp of the media.
 *
 * @author Werner Randelshofer
 * @version $Id: AdjustTimeCodec.java 364 2016-11-09 19:54:25Z werner $
 */
public class AdjustTimeCodec extends AbstractCodec {

    private Rational mediaTime=new Rational(0);

    public AdjustTimeCodec() {
        super(new Format[]{
                    new Format(), //
                },
                new Format[]{
                    new Format(), //
                });
        name = "Adjust Time";
    }

    public Rational getMediaTime() {
        return mediaTime;
    }

    public void setMediaTime(Rational mediaTime) {
        this.mediaTime = mediaTime;
    }

    @Override
    public Format setInputFormat(Format f) {
        Format fNew = super.setInputFormat(f);
        outputFormat = fNew;
        return fNew;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.setDataTo(in);

            if (mediaTime != null) {
                out.timeStamp = mediaTime;
                mediaTime = mediaTime.add(out.sampleDuration.multiply(out.sampleCount));
            }

        return CODEC_OK;
    }
}

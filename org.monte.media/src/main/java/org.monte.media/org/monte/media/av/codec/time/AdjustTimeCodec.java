/*
 * @(#)AdjustTimeCodec.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av.codec.time;

import org.monte.media.av.AbstractCodec;
import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.math.Rational;

/**
 * Adjusts the time stamp of the media.
 *
 * @author Werner Randelshofer
 */
public class AdjustTimeCodec extends AbstractCodec {

    private Rational mediaTime = new Rational(0);

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
        int flags = out.setDataTo(in);

        if (mediaTime != null) {
            out.timeStamp = mediaTime;
            mediaTime = mediaTime.add(out.sampleDuration.multiply(out.sampleCount));
        }

        return flags;
    }
}

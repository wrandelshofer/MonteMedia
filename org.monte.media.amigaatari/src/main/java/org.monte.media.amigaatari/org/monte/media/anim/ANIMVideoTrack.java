/*
 * @(#)ANIMVideoTrack.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.anim;

import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.av.Buffer;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.Track;
import org.monte.media.math.Rational;

import java.util.Map;
import java.util.TreeMap;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.BufferFlag.END_OF_MEDIA;
import static org.monte.media.av.BufferFlag.KEYFRAME;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * {@code ANIMVideoTrack}.
 *
 * @author Werner Randelshofer
 */
public class ANIMVideoTrack implements Track {
    private ANIMDemultiplexer demux;
    /**
     * The position in the jiffy count.
     */
    private long jiffyPosition;
    private Format format = null;
    private long jiffyCount = 0;

    /**
     * Key: jiffies position of frame
     * Value: Frame index
     */
    private TreeMap<Long, Integer> jiffiesToFrameMap = new TreeMap<>();
    private boolean hasColorCycling;

    public ANIMVideoTrack(ANIMDemultiplexer demux) {
        this.demux = demux;

        jiffyCount = 0;
        int n = demux.getFrameCount();
        for (int i = 0; i < n; i++) {
            jiffiesToFrameMap.put(jiffyCount, i);
            jiffyCount += demux.getDuration(i);
        }

        hasColorCycling = demux.getColorCyclesCount() > 0;
    }

    @Override
    public long getSampleCount() {
        return demux.getFrameCount();
    }

    @Override
    public void setPosition(long pos) {
        this.jiffyPosition = pos;
    }

    @Override
    public long getPosition() {
        return jiffyPosition;
    }

    @Override
    public void read(Buffer buf) {

        if (jiffyPosition < jiffyCount) {
            Map.Entry<Long, Integer> entry = jiffiesToFrameMap.floorEntry(jiffyPosition);
            Long framePositionInJiffies = entry.getKey();
            int frameIndex = entry.getValue();

            buf.setFlagsTo(KEYFRAME);
            if (!(buf.data instanceof AmigaBitmapImage)) {
                buf.data = demux.createCompatibleBitmap();
            }
            demux.readFrame(frameIndex, (AmigaBitmapImage) buf.data);
            int duration = hasColorCycling ? 1 :
                    (int) (framePositionInJiffies + demux.getDuration(frameIndex) - jiffyPosition);
            buf.sampleDuration = new Rational(duration, demux.getJiffies());
            buf.format = getFormat();
            buf.sampleCount = 1;
            buf.timeStamp = new Rational(jiffyPosition, demux.getJiffies());
            jiffyPosition += duration;
        } else {
            buf.setFlagsTo(DISCARD, END_OF_MEDIA);
        }
    }

    @Override
    public Format getFormat() {
        if (format == null) {

            int depth;
            switch (demux.getResources().getScreenMode()) {
                case ANIMMovieResources.MODE_EHB:
                case ANIMMovieResources.MODE_INDEXED_COLORS:
                    depth = 8;
                    break;
                case ANIMMovieResources.MODE_HAM6:
                case ANIMMovieResources.MODE_HAM8:
                case ANIMMovieResources.MODE_DIRECT_COLORS:
                default:
                    depth = 24;
                    break;
            }

            int jiffies = demux.getJiffies();

            format = new Format(
                    MediaTypeKey, FormatKeys.MediaType.VIDEO,
                    EncodingKey, ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE,
                    DepthKey, depth,
                    WidthKey, demux.getWidth(),
                    HeightKey, demux.getHeight(),
                    FrameRateKey, new Rational(jiffies, 1)
            );
        }
        return format;
    }
}

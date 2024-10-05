package org.monte.media.impl.jcodec.codecs.h264.encode;

import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceType;
import org.monte.media.impl.jcodec.common.model.Picture;
import org.monte.media.impl.jcodec.common.model.Size;

/**
 * This class is part of JCodec ( www.jcodec.org ) This software is distributed
 * under FreeBSD License
 * <p>
 * MPEG 4 AVC ( H.264 ) Encoder pluggable rate control mechanism
 *
 * @author The JCodec project
 */
public interface RateControl {

    int startPicture(Size sz, int maxSize, SliceType sliceType);

    int initialQpDelta(Picture pic, int mbX, int mbY);

    int accept(int bits);
}

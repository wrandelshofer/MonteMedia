/* @(#)BufferFlag.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.codec;

/**
 * {@code BufferFlag}.
 *
 * @author Werner Randelshofer
 * @version $Id: BufferFlag.java 364 2016-11-09 19:54:25Z werner $
 */
public enum BufferFlag {

    /**
     * Indicates that the data in this buffer should be ignored.
     */
    DISCARD,
    /**
     * Indicates that this Buffer holds an intra-coded picture, which can be
     * decoded independently.
     */
    KEYFRAME,
    /**
     * Indicates that the data in this buffer is at the end of the media.
     */
    END_OF_MEDIA,
    /**
     * Indicates that the data in this buffer is used for initializing the
     * decoding queue.
     * <p>
     * This flag is used when the media time of a track is set to a non-keyframe
     * sample. Thus decoding must start at a keyframe at an earlier time.
     * <p>
     * Decoders should decode the buffer. Encoders and Multiplexers should
     * discard the buffer.
     */
    PREFETCH,
    /**
     * Indicates that this buffer is known to have the same data as the previous
     * buffer. This may improve encoding performance.
     */
    SAME_DATA;
}

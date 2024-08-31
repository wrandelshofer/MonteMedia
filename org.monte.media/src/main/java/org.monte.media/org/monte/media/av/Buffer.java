/*
 * @(#)Buffer.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import org.monte.media.math.Rational;
import org.monte.media.util.Methods;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.EnumSet;

/**
 * A {@code Buffer} carries media data from one media processing unit to
 * another.
 *
 * @author Werner Randelshofer
 */
public class Buffer {

    /**
     * A flag mask that describes the boolean attributes for this buffer.
     */
    public EnumSet<BufferFlag> flags = EnumSet.noneOf(BufferFlag.class);
    /**
     * The track number. This can be set to NOT_SPECIFIED or to a number &gt;= 0.
     */
    public int track;
    /**
     * Header information, such as RTP header for this chunk.
     */
    public Object header;
    /**
     * The header offset. This field is only used if {@code header} is an array.
     */
    public int headerOffset;
    /**
     * The header length. This field is only used if {@code header} is an array.
     */
    public int headerLength;

    /**
     * The media data.
     */
    public Object data;
    /**
     * The data offset. This field is only used if {@code data} is an array.
     */
    public int offset;
    /**
     * The data length. This field is only used if {@code data} is an array.
     */
    public int length;
    /**
     * Duration of a sample in seconds. Multiply this with {@code sampleCount}
     * to get the buffer duration.
     */
    public Rational sampleDuration = Rational.ZERO;
    /**
     * The time stamp of this buffer in seconds.
     */
    public Rational timeStamp = Rational.ZERO;
    /**
     * The format of the data in this buffer.
     */
    public Format format;
    /**
     * The number of samples in the data field.
     */
    public int sampleCount = 1;

    /**
     * Sequence number of the buffer. This can be used for debugging.
     */
    public long sequenceNumber;

    /**
     * An exception that explains why this buffer must be discarded.
     */
    public Throwable exception;

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    /**
     * Sets all variables of this buffer to that buffer except for
     * <ul>
     *     <li>{@code data}</li>
     *     <li>{@code offset}</li>
     *     <li>{@code length}</li>
     *     <li>{@code header}</li>
     * </ul>
     */
    public void setMetaTo(Buffer that) {
        this.flags = EnumSet.copyOf(that.flags);
        //this.data=that.data;
        //this.offset=that.offset;
        //this.length=that.length;
        //this.header=that.header;
        this.track = that.track;
        this.sampleDuration = that.sampleDuration;
        this.timeStamp = that.timeStamp;
        this.format = that.format;
        this.sampleCount = that.sampleCount;
        this.sequenceNumber = that.sequenceNumber;
        this.exception = that.exception;
    }

    /**
     * Sets {@code data}, {@code offset}, {@code length} and {@code header} of
     * this buffer to that buffer. Note that this method creates copies of the
     * {@code data} and {@code header}, so that these fields in that buffer can
     * be discarded without affecting the contents of this buffer.
     * <p>
     * Returns {@link Codec#CODEC_FAILED} or {@link Codec#CODEC_OK}
     */
    public int setDataTo(Buffer that) {
        this.offset = that.offset;
        this.length = that.length;
        try {
            this.data = copy(that.data, this.data);
            this.header = copy(that.header, this.header);
        } catch (UnsupportedOperationException e) {
            return Codec.CODEC_FAILED;
        }
        return Codec.CODEC_OK;
    }

    public Rational getBufferDuration() {
        return sampleDuration.multiply(sampleCount);
    }

    public Rational getBufferEndTimestamp() {
        return timeStamp.add(getBufferDuration());
    }

    private Object copy(Object from, Object into) throws UnsupportedOperationException {
        if (from instanceof byte[]) {
            byte[] b = (byte[]) from;
            if (!(into instanceof byte[]) || ((byte[]) into).length < b.length) {
                into = new byte[b.length];
            }
            System.arraycopy(b, 0, into, 0, b.length);
        } else if (from instanceof BufferedImage) {
            // FIXME - Try to reuse BufferedImage in output!
            BufferedImage img = (BufferedImage) from;
            ColorModel cm = img.getColorModel();
            boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
            WritableRaster raster = img.copyData(null);
            into = new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        } else if (from instanceof Cloneable) {
            try {
                into = Methods.invoke(from, "clone");
            } catch (NoSuchMethodException ex) {
                into = from;
            }
        } else {
            throw new UnsupportedOperationException();
        }

        return into;
    }

    /**
     * Returns true if the specified flag is set.
     */
    public boolean isFlag(BufferFlag flag) {
        return flags.contains(flag);
    }

    /**
     * Convenience method for setting a flag.
     */
    public void setFlag(BufferFlag flag) {
        setFlag(flag, true);
    }

    /**
     * Convenience method for clearing a flag.
     */
    public void clearFlag(BufferFlag flag) {
        setFlag(flag, false);
    }

    /**
     * Sets or clears the specified flag.
     */
    public void setFlag(BufferFlag flag, boolean value) {
        if (value) {
            flags.add(flag);
        } else {
            flags.remove(flag);
        }
    }

    /**
     * Clears all flags, and then sets the specified flag.
     */
    public void setFlagsTo(BufferFlag... flags) {
        if (flags.length == 0) {
            this.flags = EnumSet.noneOf(BufferFlag.class);
        } else {
            this.flags = EnumSet.copyOf(Arrays.asList(flags));
        }
    }

    /**
     * Clears all flags, and then sets the specified flag.
     */
    public void setFlagsTo(EnumSet<BufferFlag> flags) {
        if (flags == null) {
            this.flags = EnumSet.noneOf(BufferFlag.class);
        } else {
            this.flags = EnumSet.copyOf(flags);
        }
    }

    public void clearFlags() {
        flags.clear();
    }

    @Override
    public String toString() {
        return super.toString() + "{"
                +//
                "tr#:" + track
                +//
                ",seq#:" + sequenceNumber
                +//
                ",ts:" + timeStamp
                +//
                ",duration:" + sampleDuration
                +//
                ",#samples:" + sampleCount
                +//
                ",flags:" + flags
                +//
                ",format:" + format
                +//
                ",data:" + data
                +//
                ",offset:" + offset
                +//
                ",length:" + length
                +//
                ",header:" + data
                +//
                ",hOffset:" + headerOffset
                +//
                ",hLength:" + headerLength
                +//
                "}";
    }

}

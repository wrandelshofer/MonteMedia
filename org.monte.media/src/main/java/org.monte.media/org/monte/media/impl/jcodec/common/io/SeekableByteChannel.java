package org.monte.media.impl.jcodec.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.nio.channels.Channel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * @author The JCodec project
 */
public interface SeekableByteChannel extends ByteChannel, Channel, Closeable, ReadableByteChannel, WritableByteChannel {
    long position() throws IOException;

    SeekableByteChannel setPosition(long newPosition) throws IOException;

    long size() throws IOException;

    SeekableByteChannel truncate(long size) throws IOException;
}

package org.monte.media.impl.jcodec.common.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

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
public class FileChannelWrapper implements SeekableByteChannel {

    private FileChannel ch;

    public FileChannelWrapper(FileChannel ch) throws FileNotFoundException {
        this.ch = ch;
    }

    @Override
    public int read(ByteBuffer arg0) throws IOException {
        return ch.read(arg0);
    }

    @Override
    public void close() throws IOException {
        ch.close();
    }

    @Override
    public boolean isOpen() {
        return ch.isOpen();
    }

    @Override
    public int write(ByteBuffer arg0) throws IOException {
        return ch.write(arg0);
    }

    @Override
    public long position() throws IOException {
        return ch.position();
    }

    @Override
    public SeekableByteChannel setPosition(long newPosition) throws IOException {
        ch.position(newPosition);
        return this;
    }

    @Override
    public long size() throws IOException {
        return ch.size();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        ch.truncate(size);
        return this;
    }
}
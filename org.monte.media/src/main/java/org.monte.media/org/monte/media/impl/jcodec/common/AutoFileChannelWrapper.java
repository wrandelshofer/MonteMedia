package org.monte.media.impl.jcodec.common;

import org.monte.media.impl.jcodec.common.io.AutoPool;
import org.monte.media.impl.jcodec.common.io.AutoResource;
import org.monte.media.impl.jcodec.common.io.SeekableByteChannel;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static java.lang.System.currentTimeMillis;

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
public class AutoFileChannelWrapper implements SeekableByteChannel, AutoResource {

    private static final long THRESHOLD = 5000; // five seconds

    private FileChannel ch;
    private File file;
    private long savedPos;
    private long curTime;
    private long accessTime;

    public AutoFileChannelWrapper(File file) throws IOException {
        this.file = file;
        this.curTime = currentTimeMillis();
        AutoPool.getInstance().add(this);
        ensureOpen();
    }

    private void ensureOpen() throws IOException {
        accessTime = curTime;
        if (ch == null || !ch.isOpen()) {
            ch = new FileInputStream(file).getChannel();
            ch.position(savedPos);
        }
    }

    @Override
    public int read(ByteBuffer arg0) throws IOException {
        ensureOpen();
        int r = ch.read(arg0);
        savedPos = ch.position();
        return r;
    }

    @Override
    public void close() throws IOException {
        if (ch != null && ch.isOpen()) {
            savedPos = ch.position();
            ch.close();
            ch = null;
        }
    }

    @Override
    public boolean isOpen() {
        return ch != null && ch.isOpen();
    }

    @Override
    public int write(ByteBuffer arg0) throws IOException {
        ensureOpen();
        int w = ch.write(arg0);
        savedPos = ch.position();
        return w;
    }

    @Override
    public long position() throws IOException {
        ensureOpen();
        return ch.position();
    }

    @Override
    public SeekableByteChannel setPosition(long newPosition) throws IOException {
        ensureOpen();
        ch.position(newPosition);
        savedPos = newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        ensureOpen();
        return ch.size();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        ensureOpen();
        ch.truncate(size);
        savedPos = ch.position();
        return this;
    }

    @Override
    public void setCurTime(long curTime) {
        this.curTime = curTime;
        if (ch != null && ch.isOpen() && curTime - accessTime > THRESHOLD) {
            try {
                close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
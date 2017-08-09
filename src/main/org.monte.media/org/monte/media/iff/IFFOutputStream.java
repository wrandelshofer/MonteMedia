/* @(#)IFFOutputStream.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.iff;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Stack;
import javax.imageio.stream.ImageOutputStream;

/**
 * Facilitates writing of EA 85 IFF files.
 * <p>
 * Reference:<br>
 * Commodore-Amiga, Inc. (1991) Amiga ROM Kernel Reference Manual. Devices.
 * Third Edition. Reading: Addison-Wesley.
 *
 * @author Werner Randelshofer
 * @version 1.2 2011-09-01 Adds write buffer to improve performance.
 * <br>1.1 2011-02-19 Adds methods getStreamPosition() and seek().
 * <br>1.0 2010-12-26 Created.
 */
public class IFFOutputStream extends OutputStream {

    private byte[] writeBuffer = new byte[4];
    private Stack<Chunk> stack = new Stack<Chunk>();
    private ImageOutputStream out;
    private long streamOffset;

    public IFFOutputStream(ImageOutputStream out) throws IOException {
        this.out = out;
        streamOffset = out.getStreamPosition();
    }

    public void pushCompositeChunk(String compositeType, String chunkType) throws IOException {
        stack.push(new CompositeChunk(compositeType, chunkType));
    }

    public void pushDataChunk(String chunkType) throws IOException {
        stack.push(new DataChunk(chunkType));
    }

    public void popChunk() throws IOException {
        Chunk chunk = stack.pop();
        chunk.finish();
    }

    public void finish() throws IOException {
        while (!stack.empty()) {
            popChunk();
        }
    }

    @Override
    public void close() throws IOException {
        try {
            finish();
        } finally {
            out.close();
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    /** Gets the position relative to the beginning of the IFF output stream.
     * <p>
     * Usually this value is equal to the stream position of the underlying
     * ImageOutputStream, but can be larger if the underlying stream already
     * contained data.
     *
     * @return The relative stream position.
     * @throws IOException
     */
    public long getStreamPosition() throws IOException {
        return out.getStreamPosition() - streamOffset;
    }

    /** Seeks relative to the beginning of the IFF output stream.
     * <p>
     * Usually this equal to seeking in the underlying ImageOutputStream, but
     * can be different if the underlying stream already contained data.
     *
     */
    public void seek(long newPosition) throws IOException {
        out.seek(newPosition + streamOffset);
    }

    /**
     * Chunk base class.
     */
    private abstract class Chunk {

        /**
         * The chunkType of the chunk. A String with the length of 4 characters.
         */
        protected String chunkType;
        /**
         * The offset of the chunk relative to the start of the
         * ImageOutputStream.
         */
        protected long offset;
        protected boolean finished;

        /**
         * Creates a new Chunk at the current position of the ImageOutputStream.
         * @param chunkType The chunkType of the chunk. A string with a length of 4 characters.
         */
        public Chunk(String chunkType) throws IOException {
            this.chunkType = chunkType;
            offset = getStreamPosition();
        }

        /**
         * Writes the chunk to the ImageOutputStream and disposes it.
         */
        public abstract void finish() throws IOException;

        public abstract boolean isComposite();
    }

    /**
     * A CompositeChunk contains an ordered list of Chunks.
     */
    private class CompositeChunk extends Chunk {

        /**
         * The type of the composite. A String with the length of 4 characters.
         */
        protected String compositeType;

        /**
         * Creates a new CompositeChunk at the current position of the
         * ImageOutputStream.
         * @param compositeType The type of the composite.
         * @param chunkType The type of the chunk.
         */
        public CompositeChunk(String compositeType, String chunkType) throws IOException {
            super(chunkType);
            this.compositeType = compositeType;
            //out.write
            out.writeLong(0); // make room for the chunk header
            out.writeInt(0); // make room for the chunk header
        }

        /**
         * Writes the chunk and all its children to the ImageOutputStream
         * and disposes of all resources held by the chunk.
         * @throws java.io.IOException
         */
        @Override
        public void finish() throws IOException {
            if (!finished) {
                long size = getStreamPosition() - offset;
                if (size > 0xffffffffL) {
                    throw new IOException("CompositeChunk \"" + chunkType + "\" is too large: " + size);
                }

                long pointer = getStreamPosition();
                seek(offset);

                writeTYPE(compositeType);
                writeULONG(size - 8);
                writeTYPE(chunkType);
                seek(pointer);
                if (size % 2 == 1) {
                    out.writeByte(0); // write pad byte
                }
                finished = true;
            }
        }

        @Override
        public boolean isComposite() {
            return true;
        }
    }

    /**
     * Data Chunk.
     */
    private class DataChunk extends Chunk {

        /**
         * Creates a new DataChunk at the current position of the
         * ImageOutputStream.
         * @param chunkType The chunkType of the chunk.
         */
        public DataChunk(String name) throws IOException {
            super(name);
            out.writeLong(0); // make room for the chunk header
        }

        @Override
        public void finish() throws IOException {
            if (!finished) {
                long size = getStreamPosition() - offset;
                if (size > 0xffffffffL) {
                    throw new IOException("DataChunk \"" + chunkType + "\" is too large: " + size);
                }

                long pointer = getStreamPosition();
                seek(offset);

                writeTYPE(chunkType);
                writeULONG(size - 8);
                seek(pointer);
                if (size % 2 == 1) {
                    out.writeByte(0); // write pad byte
                }
                finished = true;
            }
        }

        @Override
        public boolean isComposite() {
            return false;
        }
    }

    public void writeLONG(int v) throws IOException {
        writeBuffer[0] = (byte) (v >>> 24);
        writeBuffer[1] = (byte) (v >>> 16);
        writeBuffer[2] = (byte) (v >>> 8);
        writeBuffer[3] = (byte) (v >>> 0);
        out.write(writeBuffer, 0, 4);
    }

    public void writeULONG(long v) throws IOException {
        writeBuffer[0] = (byte) (v >>> 24);
        writeBuffer[1] = (byte) (v >>> 16);
        writeBuffer[2] = (byte) (v >>> 8);
        writeBuffer[3] = (byte) (v >>> 0);
        out.write(writeBuffer, 0, 4);
    }

    public void writeWORD(int v) throws IOException {
        writeBuffer[0] = (byte) (v >>> 8);
        writeBuffer[1] = (byte) (v >>> 0);
        out.write(writeBuffer, 0, 2);
    }

    public void writeUWORD(int v) throws IOException {
        writeBuffer[0] = (byte) (v >>> 8);
        writeBuffer[1] = (byte) (v >>> 0);
        out.write(writeBuffer, 0, 2);
    }

    public void writeUBYTE(int v) throws IOException {
        out.write(v);
    }

    /**
     * Writes an chunk type identifier (4 bytes).
     * @param s A string with a length of 4 characters.
     */
    public void writeTYPE(String s) throws IOException {
        if (s.length() != 4) {
            throw new IllegalArgumentException("type string must have 4 characters");
        }

        try {
            out.write(s.getBytes("ASCII"), 0, 4);
        } catch (UnsupportedEncodingException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * ByteRun1 Run Encoding.
     * <p>
     * The run encoding scheme in byteRun1 is best described by
     * pseudo code for the decoder Unpacker (called UnPackBits in the
     * Macintosh toolbox):
     * <pre>
     * UnPacker:
     *    LOOP until produced the desired number of bytes
     *       Read the next source byte into n
     *       SELECT n FROM
     *          [ 0..127 ] ⇒ copy the next n+1 bytes literally
     *          [-1..-127] ⇒ replicate the next byte -n+1 timees
     *          -128       ⇒ no operation
     *       ENDCASE
     *    ENDLOOP
     * </pre>
     */
    public void writeByteRun1(byte[] data) throws IOException {
        writeByteRun1(data, 0, data.length);
    }

    public void writeByteRun1(byte[] data, int offset, int length) throws IOException {
        int end = offset + length;

        // Start offset of the literal run
        int literalOffset = offset;
        int i;
        for (i = offset; i < end; i++) {
            // Read a byte
            byte b = data[i];

            // Count repeats of that byte
            int repeatCount = i + 1;
            for (; repeatCount < end; repeatCount++) {
                if (data[repeatCount] != b) {
                    break;
                }
            }
            repeatCount = repeatCount - i;

            if (repeatCount == 1) {
                // Flush the literal run, if it gets too large
                if (i - literalOffset > 127) {
                    write(i - literalOffset - 1);
                    write(data, literalOffset, i - literalOffset);
                    literalOffset = i;
                }

                // If the byte repeats just twice, and we have a literal
                // run with enough space, add it to the literal run
            } else if (repeatCount == 2
                    && literalOffset < i && i - literalOffset < 127) {
                i++;
            } else {
                // Flush the literal run, if we have one
                if (literalOffset < i) {
                    write(i - literalOffset - 1);
                    write(data, literalOffset, i - literalOffset);
                }
                // Write the repeat run
                i += repeatCount - 1;
                literalOffset = i + 1;
                // We have to write multiple runs, if the byte repeats more
                // than 128 times.
                for (; repeatCount > 128; repeatCount -= 128) {
                    write(-127);
                    write(b);
                }
                write(-repeatCount + 1);
                write(b);
            }
        }

        // Flush the literal run, if we have one
        if (literalOffset < end) {
            write(i - literalOffset - 1);
            write(data, literalOffset, i - literalOffset);
        }
    }
}

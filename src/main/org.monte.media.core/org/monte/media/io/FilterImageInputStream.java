/* @(#)FilterImageInputStream.java
 * Copyright © 2009-2013 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.io;

import java.io.IOException;
import javax.imageio.stream.ImageInputStream;

/**
 * {@code FilterImageInputStream}.
 *
 * @author Werner Randelshofer
 * @version $Id: FilterImageInputStream.java 364 2016-11-09 19:54:25Z werner $
 */
public class FilterImageInputStream extends ImageInputStreamImpl2 {
    /**
     * The underlying input stream.
     */
    protected ImageInputStream in;

    public FilterImageInputStream(ImageInputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        flushBits();
        return in.read();
    }

    @Override
    public int read(byte b[], int off, int len) throws IOException {
        flushBits();
        return in.read(b,off,len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        flushBits();
        return in.skipBytes(n);
    }

    @Override
    public long skipBytes(long n) throws IOException {
        flushBits();
        return in.skipBytes(n);
    }

    @Override
    public void close() throws IOException {
        super.close();
         in.close();
    }

    @Override
    public long getStreamPosition() throws IOException {
        return in.getStreamPosition();
    }
    @Override
    public void seek(long pos) throws IOException {
        flushBits();
        in.seek(pos);
    }

    @Override
    public long length()  {
        try {
            return in.length();
        } catch (IOException ex) {
            return -1L;
        }
    }

    @Override
    public void flushBefore(long pos) throws IOException {
        super.flushBefore(pos);
        in.flushBefore(pos);
    }


    @Override
    public boolean isCached() {
        return in.isCached();
    }

    @Override
    public boolean isCachedMemory() {
        return in.isCachedMemory();
    }

    @Override
    public boolean isCachedFile() {
        return in.isCachedFile();
    }
    private void flushBits() {
        bitOffset=0;
    }
}

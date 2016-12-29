/* @(#)IOStreams
 * Copyright (c) 2016 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import javax.imageio.stream.ImageOutputStream;

/**
 * IOStreams.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public class IOStreams {
    /**
     * Copies the source file into the provided target file.
     *
     * @param source the source file
     * @param target the target file
     * @throws IOException TODO
     */
    public static void copy(File source, File target) throws IOException {
        Files.copy(source.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * Copies the remainder of the source stream into the provided target
     * stream.
     *
     * @param source the source stream
     * @param target the target stream
     * @return number of copied bytes
     * @throws IOException TODO
     */
    public static long copy(InputStream source, OutputStream target) throws IOException {
        long n=0L;
        byte[] b = new byte[8192];
        for (int count  = source.read(b); count != -1; count = source.read(b)) {
            target.write(b, 0, count);
            n+=count;
        }
        return n;
    }
    /**
     * Copies the remainder of the source stream into the provided target
     * stream.
     *
     * @param source the source stream
     * @param target the target stream
     * @return number of copied bytes
     * @throws IOException TODO
     */
    public static long copy(InputStream source, ImageOutputStream target) throws IOException {
        long count=0L;
        byte[] b = new byte[8192];
        for (int read  = source.read(b); read != -1; read = source.read(b)) {
            target.write(b, 0, read);
            count+=read;
        }
        return count;
    }
    
    /**
     * Copies up to the specified number of bytes from the remainder of the source stream into the provided target
     * stream.
     *
     * @param source the source stream
     * @param target the target stream
     * @param n the maximal number of bytes to copy
     * @return actual number of copied bytes
     * @throws IOException TODO
     */
    public static long copy(InputStream source, ImageOutputStream target, long n) throws IOException {
        long count=0L;
        byte[] b = new byte[8192];
        for (int read  = source.read(b); read != -1&&count<n; read = source.read(b,0,(int)Math.min(b.length, n-count))) {
            target.write(b, 0, read);
            count+=read;
        }
        return count;
    }

}

package org.monte.media.impl.jcodec.common.io;

import org.monte.media.impl.jcodec.platform.Platform;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

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
public class IOUtils {

    public static final int DEFAULT_BUFFER_SIZE = 4096;

    public static void closeQuietly(Closeable c) {
        if (c == null)
            return;
        try {
            c.close();
        } catch (IOException e) {
        }
    }

    public static byte[] toByteArray(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static int copyDumb(InputStream input, OutputStream output) throws IOException {
        int count = 0;
        int n = 0;
        while (-1 != (n = input.read())) {
            output.write(n);
            count++;
        }
        return count;
    }

    public static byte[] readFileToByteArray(File file) throws IOException {
        return NIOUtils.toArray(NIOUtils.fetchFromFile(file));
    }

    public static String readToString(InputStream is) throws IOException {
        return Platform.stringFromBytes(toByteArray(is));
    }

    public static void writeStringToFile(File file, String str) throws IOException {
        NIOUtils.writeTo(ByteBuffer.wrap(str.getBytes()), file);
    }

    public static void forceMkdir(File directory) throws IOException {
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                String message = "File " + directory + " exists and is "
                        + "not a directory. Unable to create directory.";
                throw new IOException(message);
            }
        } else {
            if (!directory.mkdirs()) {
                // Double-check that some other thread or process hasn't made
                // the directory in the background
                if (!directory.isDirectory()) {
                    String message = "Unable to create directory " + directory;
                    throw new IOException(message);
                }
            }
        }
    }

    public static void copyFile(File src, File dst) throws IOException {
        FileChannelWrapper _in = null;
        FileChannelWrapper out = null;
        try {
            _in = NIOUtils.readableChannel(src);
            out = NIOUtils.writableChannel(dst);
            NIOUtils.copy(_in, out, Long.MAX_VALUE);
        } finally {
            NIOUtils.closeQuietly(_in);
            NIOUtils.closeQuietly(out);
        }
    }
}

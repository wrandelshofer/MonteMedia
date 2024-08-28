/*
 * @(#)ZipMovieWriterSpi.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.zipmovie;

import org.monte.media.av.Format;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.MovieWriterSpi;
import org.monte.media.io.ImageOutputStreamAdapter;

import javax.imageio.stream.ImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * AVIWriterSpi.
 *
 * @author Werner Randelshofer
 */
public class ZipMovieWriterSpi implements MovieWriterSpi {

    private final static List<String> extensions = Collections.unmodifiableList(Arrays.asList(new String[]{"zip"}));

    @Override
    public MovieWriter create(File file) throws IOException {
        return new ZipMovieWriter(file);
    }

    @Override
    public MovieWriter create(ImageOutputStream out) throws IOException {
        return new ZipMovieWriter(new ImageOutputStreamAdapter(out));
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public Format getFileFormat() {
        return ZipMovieWriter.ZIP;
    }

}

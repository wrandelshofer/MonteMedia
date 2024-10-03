/*
 * @(#)MP4WriterSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.mp4;

import org.monte.media.av.Format;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.MovieWriterSpi;

import javax.imageio.stream.ImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * MP4WriterSpi.
 *
 * @author Werner Randelshofer
 */
public class MP4WriterSpi implements MovieWriterSpi {

    private final static List<String> extensions = List.of(new String[]{"mp4x", "m4vx"});

    @Override
    public MovieWriter create(File file) throws IOException {
        return new MP4Writer(file);
    }

    @Override
    public MovieWriter create(ImageOutputStream out) throws IOException {
        return new MP4Writer(out);
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public Format getFileFormat() {
        return MP4Writer.MP4;
    }

}

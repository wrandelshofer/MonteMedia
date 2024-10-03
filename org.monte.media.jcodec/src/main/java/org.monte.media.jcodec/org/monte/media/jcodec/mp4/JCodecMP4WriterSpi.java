/*
 * @(#)JCodecMP4WriterSpi.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.jcodec.mp4;

import org.monte.media.av.Format;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.MovieWriterSpi;

import javax.imageio.stream.ImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JCodecMP4WriterSpi implements MovieWriterSpi {

    private final static List<String> extensions = Collections.unmodifiableList(Arrays.asList(new String[]{"mp4", "m4v", "m4a"}));

    public JCodecMP4WriterSpi() {
    }

    @Override
    public MovieWriter create(File file) throws IOException {
        return new JCodecMP4Writer(file);
    }

    @Override
    public MovieWriter create(ImageOutputStream out) throws IOException {
        throw new UnsupportedOperationException("ImageOutputStream is not supported");
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public Format getFileFormat() {
        return JCodecMP4Writer.MP4;
    }

}

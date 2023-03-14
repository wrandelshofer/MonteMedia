/*
 * @(#)AVIWriterSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.avi;

import org.monte.media.av.Format;
import org.monte.media.av.MovieWriter;
import org.monte.media.av.MovieWriterSpi;

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
public class AVIWriterSpi implements MovieWriterSpi {

    private final static List<String> extensions = Collections.unmodifiableList(Arrays.asList(new String[]{"avi"}));

    @Override
    public MovieWriter create(File file) throws IOException {
        return new AVIWriter(file);
    }

    @Override
    public MovieWriter create(ImageOutputStream out) throws IOException {
        return new AVIWriter(out);
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public Format getFileFormat() {
        return AVIWriter.AVI;
    }

}

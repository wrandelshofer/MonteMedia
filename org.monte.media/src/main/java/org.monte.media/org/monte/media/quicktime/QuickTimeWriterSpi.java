/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime;

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
 * QuickTimeWriterSpi.
 *
 * @author Werner Randelshofer
 */
public class QuickTimeWriterSpi implements MovieWriterSpi {

    private final static List<String> extensions = Collections.unmodifiableList(Arrays.asList(new String[]{"mov"}));

    @Override
    public MovieWriter create(File file) throws IOException {
        return new QuickTimeWriter(file);
    }

    @Override
    public MovieWriter create(ImageOutputStream out) throws IOException {
        return new QuickTimeWriter(out);
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public Format getFileFormat() {
        return QuickTimeWriter.QUICKTIME;
    }

}

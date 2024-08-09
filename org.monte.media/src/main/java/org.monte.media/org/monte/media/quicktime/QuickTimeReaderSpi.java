/*
 * @(#)QuickTimeReaderSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.quicktime;

import org.monte.media.av.Format;
import org.monte.media.av.MovieReaderSpi;

import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * QuickTimeReaderSpi.
 *
 * @author Werner Randelshofer
 */
public class QuickTimeReaderSpi implements MovieReaderSpi {

    private final static List<String> extensions = List.of(new String[]{"mov"});

    @Override
    public QuickTimeReader create(ImageInputStream in) throws IOException {
        return new QuickTimeReader(in);
    }

    @Override
    public QuickTimeReader create(File file) throws IOException {
        return new QuickTimeReader(file);
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public Format getFileFormat() {
        return QuickTimeReader.QUICKTIME;
    }

}

/*
 * @(#)AVIReaderSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.avi;

import org.monte.media.av.Format;
import org.monte.media.av.MovieReaderSpi;

import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * AVIReaderSpi.
 *
 * @author Werner Randelshofer
 */
public class AVIReaderSpi implements MovieReaderSpi {

    private final static List<String> extensions = Collections.unmodifiableList(Arrays.asList(new String[]{"avi"}));

    @Override
    public AVIReader create(ImageInputStream in) throws IOException {
        return new AVIReader(in);
    }

    @Override
    public AVIReader create(File file) throws IOException {
        return new AVIReader(file);
    }

    @Override
    public List<String> getExtensions() {
        return extensions;
    }

    @Override
    public Format getFileFormat() {
        return AVIReader.AVI;
    }

}

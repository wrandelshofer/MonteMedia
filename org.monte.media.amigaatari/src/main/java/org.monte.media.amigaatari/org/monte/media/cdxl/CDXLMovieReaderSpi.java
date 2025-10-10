/*
 * @(#)CDXLMovieReaderSpi.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.cdxl;

import org.monte.media.av.Format;
import org.monte.media.av.MovieReader;
import org.monte.media.av.MovieReaderSpi;

import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class CDXLMovieReaderSpi implements MovieReaderSpi {
    @Override
    public MovieReader create(File file) throws IOException {
        return new CDXLMovieReader(file);
    }

    @Override
    public MovieReader create(ImageInputStream in) throws IOException {
        return new CDXLMovieReader(in);
    }

    @Override
    public List<String> getExtensions() {
        return List.of("cdxl", "cd", "xl");
    }

    @Override
    public Format getFileFormat() {
        return CDXLMovieReader.CDXL;
    }
}

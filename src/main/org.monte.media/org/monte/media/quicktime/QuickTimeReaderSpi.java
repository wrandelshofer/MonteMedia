/* @(#)QuickTimeReaderSpi
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */
package org.monte.media.quicktime;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.imageio.stream.ImageInputStream;
import org.monte.media.av.Format;
import org.monte.media.av.MovieReaderSpi;

/**
 * QuickTimeReaderSpi.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class QuickTimeReaderSpi implements MovieReaderSpi {

    private final static List<String> extensions = Collections.unmodifiableList(Arrays.asList(new String[]{"mov"}));

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

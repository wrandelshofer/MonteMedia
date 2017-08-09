/* @(#)AVIReaderSpi
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */
package org.monte.media.avi;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.imageio.stream.ImageInputStream;
import org.monte.media.av.Format;
import org.monte.media.av.MovieReaderSpi;

/**
 * AVIReaderSpi.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
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

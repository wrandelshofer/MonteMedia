/*
 * @(#)MovieReaderSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import javax.imageio.stream.ImageInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Service provider interface for {@link MovieReader}.
 *
 * @author Werner Randelshofer
 */
public interface MovieReaderSpi {

    MovieReader create(File file) throws IOException;

    MovieReader create(ImageInputStream in) throws IOException;

    List<String> getExtensions();

    Format getFileFormat();
}

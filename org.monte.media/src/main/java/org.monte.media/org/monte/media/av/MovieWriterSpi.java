/*
 * @(#)MovieWriterSpi.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import javax.imageio.stream.ImageOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Service provider interface for {@link MovieWriter}.
 *
 * @author Werner Randelshofer
 */
public interface MovieWriterSpi {

    MovieWriter create(File file) throws IOException;

    MovieWriter create(ImageOutputStream out) throws IOException;

    List<String> getExtensions();

    Format getFileFormat();
}

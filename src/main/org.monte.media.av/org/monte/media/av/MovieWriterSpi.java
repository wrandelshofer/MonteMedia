/* @(#)MovieWriterSpi
 * Copyright (c) 2017 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.av;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.stream.ImageOutputStream;

/**
 * Service provider interface for {@link MovieWriter}.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public interface MovieWriterSpi {

    MovieWriter create(File file) throws IOException;
    MovieWriter create(ImageOutputStream out) throws IOException;

    List<String> getExtensions();

    Format getFileFormat();
}

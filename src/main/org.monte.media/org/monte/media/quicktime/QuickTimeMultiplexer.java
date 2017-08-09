/* @(#)QuickTimeMultiplexer.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License.
 */

package org.monte.media.quicktime;

import java.io.File;
import java.io.IOException;
import javax.imageio.stream.ImageOutputStream;
import org.monte.media.av.Multiplexer;

/**
 * {@code QuickTimeMultiplexer}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-20 Created.
 */
public class QuickTimeMultiplexer extends QuickTimeWriter implements Multiplexer {
 public QuickTimeMultiplexer(File file) throws IOException {

super(file);

    }

    /**
     * Creates a new QuickTime writer.
     *
     * @param out the underlying output stream.
     */
    public QuickTimeMultiplexer(ImageOutputStream out) throws IOException {
        super(out);
    }


}

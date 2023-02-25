/* @(#)QuickTimeMultiplexer.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime;

import org.monte.media.av.Multiplexer;

import javax.imageio.stream.ImageOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * {@code QuickTimeMultiplexer}.
 *
 * @author Werner Randelshofer
 * @version $Id$
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

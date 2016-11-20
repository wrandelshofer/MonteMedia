/* @(#)QuickTimeMultiplexer.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */

package ru.sbtqa.monte.media.quicktime;

import java.io.File;
import java.io.IOException;
import javax.imageio.stream.ImageOutputStream;
import ru.sbtqa.monte.media.Multiplexer;

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

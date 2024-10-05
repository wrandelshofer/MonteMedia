/*
 * @(#)ImageInputStreams.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.io;

import javax.imageio.stream.ImageInputStream;
import java.io.IOException;

public class ImageInputStreams {
    /**
     * Don't let anyone instantiate this class.
     */
    private ImageInputStreams() {

    }

    public static int read(ImageInputStream in, int[] b, int off, int len) throws IOException {
        int nbytes = 0;
        while (nbytes < len && in.getStreamPosition() < in.length() - 4) {
            b[off + nbytes] = in.readInt();
        }
        return nbytes;
    }
}

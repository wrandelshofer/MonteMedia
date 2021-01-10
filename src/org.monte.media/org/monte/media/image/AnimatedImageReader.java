/* @(#)AnimatedImageReader
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.image;

import java.awt.Image;
import java.io.IOException;

/**
 * AnimatedImageReader.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public interface AnimatedImageReader {

    /**
     * Reads an animated image.
     * @param index the image index
     * @return an animated image
     * @throws IOException if reading fails
     */
    Image readAnimatedImage(int index) throws IOException;
}

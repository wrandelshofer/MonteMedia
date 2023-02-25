/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.image;

import java.awt.*;
import java.io.IOException;

/**
 * AnimatedImageReader.
 *
 * @author Werner Randelshofer
 */
public interface AnimatedImageReader {

    /**
     * Reads an animated image.
     *
     * @param index the image index
     * @return an animated image
     * @throws IOException if reading fails
     */
    Image readAnimatedImage(int index) throws IOException;
}

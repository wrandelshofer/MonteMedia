/*
 * @(#)CursorImages.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder.images;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class CursorImages {
    public static final String CURSOR_BLACK_PNG = "Cursor.black.png";
    public static final String CURSOR_BLACK_PRESSED_PNG = "Cursor.black.pressed.png";

    public static BufferedImage getImage(String name) {
        try {
            return ImageIO.read(CursorImages.class.getResource(name));
        } catch (IOException e) {
            return null;
        }
    }
}

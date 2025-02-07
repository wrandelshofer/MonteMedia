/*
 * @(#)MouseConfigs.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.FormatKey;

import java.awt.Point;
import java.awt.image.BufferedImage;

/**
 * Configuration options for recording the mouse cursor.
 */
public class MouseFormatKeys {

    /**
     * "Encoding" for black mouse cursor.
     */
    public final static String ENCODING_BLACK_CURSOR = "black";
    /**
     * "Encoding" for white mouse cursor.
     */
    public final static String ENCODING_WHITE_CURSOR = "white";
    /**
     * "Encoding" for yellow mouse cursor.
     */
    public final static String ENCODING_YELLOW_CURSOR = "yellow";

    public final static FormatKey<BufferedImage> CURSOR_IMAGE_KEY = new FormatKey<>("cursorImage", BufferedImage.class);
    public final static FormatKey<BufferedImage> CURSOR_PRESSED_IMAGE_KEY = new FormatKey<>("cursorPressedImage", BufferedImage.class);
    public final static FormatKey<Point> CURSOR_OFFSET_KEY = new FormatKey<>("cursorOffset", Point.class);
}

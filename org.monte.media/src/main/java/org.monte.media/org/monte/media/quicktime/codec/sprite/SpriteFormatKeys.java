/*
 * @(#)SpriteFormatKeys.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.sprite;

import org.monte.media.av.FormatKey;

public class SpriteFormatKeys {
    public static final String ENCODING_JAVA_SPRITE = "javaSprite";
    public static final Class<?> DATA_CLASS_JAVA_SPRITE = SpriteSample.class;
    public static final String ENCODING_QUICKTIME_SPRITE = "sprite";
    /**
     * The encoding of a sprite image.
     */
    public final static FormatKey<String> SpriteImageEncodingKey = new FormatKey<>("spriteImageEncoding", String.class);
}

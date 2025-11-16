/*
 * @(#)GamutMapper.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;


/**
 * Maps a color into the gamut of a color space.
 * <p>
 * Gamut mapper does nothing, when the color is already inside the gamut of the color space.
 * <p>
 * References:
 * <dl>
 *     <dt>CSS Color Module Level 4. Chapter 13. Gamut Mapping.</dt>
 *     <dd><a href="https://www.w3.org/TR/css-color-4/#gamut-mapping">w3.org</a></dd>
 * </dl>
 */
@FunctionalInterface
public interface GamutMapper {
    /**
     * Maps the specified color into the gamut.
     *
     * @param value  the color value in the color space supported by this gamut mapper
     * @param mapped the mapped value is copied into this array. Can be the same array as {@code value}.
     * @return the mapped value
     */
    float[] map(float[] value, float[] mapped);
}

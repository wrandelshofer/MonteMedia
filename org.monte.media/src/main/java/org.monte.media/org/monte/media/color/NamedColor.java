/*
 * @(#)NamedColor.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;


/**
 * A named color.
 * <p>
 * The name should be a CSS Color Module 4 {@literal <color>} production.
 * <p>
 * References:
 * <dl>
 *      <dt>CSS Color Module Level 4. 4. Representing Colors: the &lt;color&gt; type.</dt>
 *      <dd><a href="https://www.w3.org/TR/2022/CRD-css-color-4-20221101/#color-type">w3.org</a></dd>
 *  </dl>
 *
 * @param c0         the color component 0
 * @param c1         the color component 1
 * @param c2         the color component 2
 * @param c4         the color component 4
 * @param alpha      the alpha component
 * @param colorSpace the color space
 * @param name       the name of the color
 */
public record NamedColor(float c0, float c1, float c2, float c4, float alpha, NamedColorSpace colorSpace,
                         String name) {
}

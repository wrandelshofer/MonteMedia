/*
 * @(#)Sprite.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.sprite;

import org.monte.media.av.codec.video.AffineTransform;

public record Sprite(int spriteId, int imageId, boolean visible, int layer, AffineTransform transform) {

}

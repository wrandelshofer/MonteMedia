/*
 * @(#)SpriteSample.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.sprite;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

/// Represents one sample of a sprite media.
///
/// References:
///
/// Sprite Sample Data
/// :
///         "QuickTime File Format Specification", Apple Inc. 2010-08-03. (qtff)
///          [
///          http://developer.apple.com/library/mac/documentation/QuickTime/QTFF/qtff.pdf
///          ](http://developer.apple.com/library/mac/documentation/QuickTime/QTFF/qtff.pdf/)
///
///
public class SpriteSample implements Cloneable {
    /// Sprite images.
    ///
    /// Images with ids 1,2,3,… are stored at indices 0,1,2,… .
    public List<BufferedImage> images = new ArrayList<>();
    /// Sprite properties.
    public SequencedMap<Integer, Sprite> sprites = new LinkedHashMap<>();

    @Override
    protected SpriteSample clone() {
        try {
            SpriteSample that = (SpriteSample) super.clone();
            that.images = new ArrayList<>(this.images);
            that.sprites = new LinkedHashMap<>(this.sprites);
            return that;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}

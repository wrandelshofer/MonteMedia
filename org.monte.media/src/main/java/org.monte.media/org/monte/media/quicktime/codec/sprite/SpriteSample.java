/*
 * @(#)SpriteKeyframe.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.sprite;

/**
 * Represents one sample of a sprite media.
 * <p>
 * A sample can either be a key frame or an overriding frame:
 * <dl>
 *     <dt>key frame</dt>
 *     <dd>A key frame contains a shared data atom of type 'dflt' and one or
 *         more sprite atoms of type 'sprt'.
 *      </dd>
 *      <dt>overriding frame</dt>
 *      <dd>An overriding frame contains one or more sprite atoms of type 'sprt'.</dd>
 * <p>
 * Atoms:
 * <p>
 * The shared data atom 'dflt' contains a sprite image container atom of type 'imct' and ID=1.
 * <p>
 * The sprite image container atom 'imct' stores one or more sprite image atoms of type 'imag'.
 * <p>
 * The sprite image atoms 'imag' should have ID numbers starting at 1 and counting consecutively upward.
 * <p>
 * Sprite atoms 'sprt' should have ID numbers start at 1 and count consecutively upward.
 * Each sprite atom contains a list of Sprite properties.
 * <pre>
 *     +----------------------+
 *     | dflt                 |
 *     +----------------------+
 *     | +------------------+ |
 *     | | imct             | |
 *     | +------------------+ |
 *     | | +----------+     | |
 *     | | | imag     | ... | |
 *     | | +----------+     | |
 *     | +------------------+ |
 *     |                      |
 *     | +--------------+     |
 *     | | sprt         | ... |
 *     | +--------------+     |
 *     +----------------------+
 *
 * Sprite properties:
 *     Property name                        Value   Leaf data type
 *     kSpritePropertyMatrix                    1   MatrixRecord
 *     kSpritePropertyVisible                   4   short
 *     kSpritePropertyLayer                     5   short
 *     kSpritePropertyGraphicsMode              6   ModifierTrackGraphicsModeRecord
 *     kSpritePropertyActionHandlingSpriteID    8   short
 *     kSpritePropertyImageIndex              100   short
 *
 * Sprite track properties:
 *     Atom type                                     Atom ID  Leaf data type
 *     kSpriteTrackPropertyBackgroundColor           1        RGBColor
 *     kSpriteTrackPropertyOffscreenBitDepth         1        unsigned short
 *     kSpriteTrackPropertySampleFormat              1        long
 *     kSpriteTrackPropertyHasActions                1        Boolean
 *     kSpriteTrackPropertyQTIdleEventsFrequency     1        UInt32
 *     kSpriteTrackPropertyVisible                   1        Boolean
 *     kSpriteTrackPropertyScaleSpritesToScaleWorld  1        Boolean
 *
 *  MatrixRecord:
 *    [ a c x ]
 *    [ b d y ]
 *    [ u v w ]
 *    stored in the sequence: a b u c d v x y w
 *     a    scale/rotate a, 32-bit fixed-point number divided as 16.16
 *     b    skew/rotate b,  32-bit fixed-point number divided as 16.16
 *     u    zero,           32-bit fixed-point number divided as 2.30
 *     c    skew/rotate c,  32-bit fixed-point number divided as 16.16
 *     d    scale/rotate d, 32-bit fixed-point number divided as 16.16
 *     v    zero,           32-bit fixed-point number divided as 2.30
 *     x    translate x,    32-bit fixed-point number divided as 16.16
 *     y    translate y,    32-bit fixed-point number divided as 16.16
 *     w    one,            32-bit fixed-point number divided as 2.30
 *
 * </pre>
 * <p>
 * References:
 * <dl>
 *     <dt>Sprite Sample Data</dt>
 *     <dd>
 *         "QuickTime File Format Specification", Apple Inc. 2010-08-03. (qtff)
 *          <a href="http://developer.apple.com/library/mac/documentation/QuickTime/QTFF/qtff.pdf/">
 *          http://developer.apple.com/library/mac/documentation/QuickTime/QTFF/qtff.pdf
 *          </a>
 *     </dd>
 * </dl>
 */
public class SpriteSample {

}

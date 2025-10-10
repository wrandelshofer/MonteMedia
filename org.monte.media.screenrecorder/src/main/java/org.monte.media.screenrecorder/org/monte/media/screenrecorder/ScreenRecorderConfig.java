/*
 * @(#)ScreenRecorderConfig.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.screenrecorder;

import org.monte.media.av.Format;

import javax.sound.sampled.Mixer;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;
import java.io.File;

public record ScreenRecorderConfig(GraphicsDevice graphicsDevice,
                                   Rectangle captureArea,
                                   Format fileCodecFormat,
                                   Format screenFormat,
                                   Format mouseFormat,
                                   Mixer mixer, Format audioFormat,
                                   File movieFolder) {
}

/*
 * @(#)Transform.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.impl.jcodec.scale;

import org.monte.media.impl.jcodec.common.model.Picture;


/// References:
///
/// JCodecProject. Copyright 2008-2019 JCodecProject.
/// : [BSD 2-Clause License.](https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE)
/// : [github.com](https://github.com/jcodec/jcodec)
///
/// @author The JCodec project
public interface Transform {
    public static enum Levels {
        STUDIO, PC
    }

    public void transform(Picture src, Picture dst);
}
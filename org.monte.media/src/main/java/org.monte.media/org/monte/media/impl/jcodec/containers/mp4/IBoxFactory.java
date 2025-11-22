/*
 * @(#)IBoxFactory.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.impl.jcodec.containers.mp4;

import org.monte.media.impl.jcodec.containers.mp4.boxes.Box;
import org.monte.media.impl.jcodec.containers.mp4.boxes.Header;

/// IBoxFactory.
///
/// References:
///
/// JCodecProject. Copyright 2008-2019 JCodecProject.
/// : [BSD 2-Clause License.](https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE)
/// : [github.com](https://github.com/jcodec/jcodec)
///
public interface IBoxFactory {

    Box newBox(Header header);
}
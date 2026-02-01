/*
 * @(#)NotImplementedException.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.h264.impl.jcodec.api;

/// NotImplementedException.
///
/// References:
///
/// JCodecProject. Copyright 2008-2019 JCodecProject.
/// : [BSD 2-Clause License.](https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE)
/// : [github.com](https://github.com/jcodec/jcodec)
///
public class NotImplementedException extends RuntimeException {

    public NotImplementedException(String string) {
        super(string);
    }

}

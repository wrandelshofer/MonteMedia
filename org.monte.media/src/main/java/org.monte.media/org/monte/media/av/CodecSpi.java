/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

/**
 * Service provider interface for {@link Codec}.
 *
 * @author Werner Randelshofer
 */
public interface CodecSpi {

    Codec create();
}

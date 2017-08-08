/* @(#)CodecSpi
 * Copyright (c) 2017 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.av;

/**
 * Service provider interface for {@link Codec}.
 *
 * @author Werner Randelshofer
 * @version $$Id$$
 */
public interface CodecSpi {

    Codec create();
}

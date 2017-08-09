/* @(#)CodecSpi
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
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

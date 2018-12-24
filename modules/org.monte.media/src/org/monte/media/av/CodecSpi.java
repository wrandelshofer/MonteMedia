/* @(#)CodecSpi.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

/**
 * Service provider interface for {@link Codec}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public interface CodecSpi {

    Codec create();
}

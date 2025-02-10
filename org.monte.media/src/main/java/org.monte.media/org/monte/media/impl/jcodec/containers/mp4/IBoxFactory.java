package org.monte.media.impl.jcodec.containers.mp4;

import org.monte.media.impl.jcodec.containers.mp4.boxes.Box;
import org.monte.media.impl.jcodec.containers.mp4.boxes.Header;

/**
 * IBoxFactory.
 * <p>
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 */
public interface IBoxFactory {

    Box newBox(Header header);
}
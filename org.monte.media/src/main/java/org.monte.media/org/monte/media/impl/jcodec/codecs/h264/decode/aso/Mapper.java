package org.monte.media.impl.jcodec.codecs.h264.decode.aso;

/**
 * References:
 * <p>
 * This code has been derived from JCodecProject.
 * <dl>
 *     <dt>JCodecProject. Copyright 2008-2019 JCodecProject.
 *     <br><a href="https://github.com/jcodec/jcodec/blob/7e5283408a75c3cdbefba98a57d546e170f0b7d0/LICENSE">BSD 2-Clause License.</a></dt>
 *     <dd><a href="https://github.com/jcodec/jcodec">github.com</a></dd>
 * </dl>
 *
 * @author The JCodec project
 */
public interface Mapper {
    boolean leftAvailable(int index);

    boolean topAvailable(int index);

    int getAddress(int index);

    int getMbX(int mbIndex);

    int getMbY(int mbIndex);

    boolean topRightAvailable(int mbIndex);

    boolean topLeftAvailable(int mbIdx);
}

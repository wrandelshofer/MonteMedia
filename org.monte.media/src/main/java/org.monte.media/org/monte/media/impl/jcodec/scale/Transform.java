package org.monte.media.impl.jcodec.scale;

import org.monte.media.impl.jcodec.common.model.Picture;


/**
 * This class is part of JCodec ( www.jcodec.org )
 * This software is distributed under FreeBSD License
 *
 * @author The JCodec project
 */
public interface Transform {
    public static enum Levels {
        STUDIO, PC
    }

    ;

    public void transform(Picture src, Picture dst);
}
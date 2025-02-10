package org.monte.media.impl.jcodec.codecs.h264.decode;

import org.monte.media.impl.jcodec.codecs.h264.decode.aso.Mapper;
import org.monte.media.impl.jcodec.common.model.Picture;

import static org.monte.media.impl.jcodec.codecs.h264.decode.MBlockDecoderUtils.collectPredictors;
import static org.monte.media.impl.jcodec.codecs.h264.decode.MBlockDecoderUtils.saveVectIntra;

/**
 * A decoder for Intra PCM macroblocks.
 * <p>
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
public class MBlockDecoderIPCM {
    private Mapper mapper;
    private DecoderState s;

    public MBlockDecoderIPCM(Mapper mapper, DecoderState decoderState) {
        this.mapper = mapper;
        this.s = decoderState;
    }

    public void decode(MBlock mBlock, Picture mb) {
        int mbX = mapper.getMbX(mBlock.mbIdx);
        collectPredictors(s, mb, mbX);
        saveVectIntra(s, mapper.getMbX(mBlock.mbIdx));
    }
}

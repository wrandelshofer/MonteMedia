package org.monte.media.impl.jcodec.codecs.h264.decode;

import org.monte.media.impl.jcodec.codecs.h264.decode.aso.Mapper;
import org.monte.media.impl.jcodec.common.model.Picture;

import static org.monte.media.impl.jcodec.codecs.h264.decode.MBlockDecoderUtils.collectPredictors;
import static org.monte.media.impl.jcodec.codecs.h264.decode.MBlockDecoderUtils.saveVectIntra;

/**
 * A decoder for Intra PCM macroblocks
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

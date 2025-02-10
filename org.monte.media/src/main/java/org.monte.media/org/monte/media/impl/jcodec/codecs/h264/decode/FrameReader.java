package org.monte.media.impl.jcodec.codecs.h264.decode;

import org.monte.media.impl.jcodec.codecs.common.biari.MDecoder;
import org.monte.media.impl.jcodec.codecs.h264.decode.aso.MapManager;
import org.monte.media.impl.jcodec.codecs.h264.decode.aso.Mapper;
import org.monte.media.impl.jcodec.codecs.h264.io.CABAC;
import org.monte.media.impl.jcodec.codecs.h264.io.CAVLC;
import org.monte.media.impl.jcodec.codecs.h264.io.model.NALUnit;
import org.monte.media.impl.jcodec.codecs.h264.io.model.PictureParameterSet;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SeqParameterSet;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceHeader;
import org.monte.media.impl.jcodec.common.IntObjectMap;
import org.monte.media.impl.jcodec.common.io.BitReader;
import org.monte.media.impl.jcodec.common.io.NIOUtils;
import org.monte.media.impl.jcodec.common.logging.Logger;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.monte.media.impl.jcodec.codecs.h264.H264Utils.unescapeNAL;
import static org.monte.media.impl.jcodec.codecs.h264.io.model.NALUnitType.IDR_SLICE;
import static org.monte.media.impl.jcodec.codecs.h264.io.model.NALUnitType.NON_IDR_SLICE;
import static org.monte.media.impl.jcodec.codecs.h264.io.model.NALUnitType.PPS;
import static org.monte.media.impl.jcodec.codecs.h264.io.model.NALUnitType.SPS;

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
 * <p>
 * MPEG 4 AVC ( H.264 ) Frame reader
 * <p>
 * Conforms to H.264 ( ISO/IEC 14496-10 ) specifications
 *
 * @author The JCodec project
 */
public class FrameReader {
    private IntObjectMap<SeqParameterSet> sps;
    private IntObjectMap<PictureParameterSet> pps;

    public FrameReader() {
        this.sps = new IntObjectMap<SeqParameterSet>();
        this.pps = new IntObjectMap<PictureParameterSet>();
    }

    public List<SliceReader> readFrame(List<ByteBuffer> nalUnits) {
        List<SliceReader> result = new ArrayList<SliceReader>();

        for (ByteBuffer nalData : nalUnits) {
            NALUnit nalUnit = NALUnit.read(nalData);

            unescapeNAL(nalData);
            if (SPS == nalUnit.type) {
                SeqParameterSet _sps = SeqParameterSet.read(nalData);
                sps.put(_sps.seqParameterSetId, _sps);
            } else if (PPS == nalUnit.type) {
                PictureParameterSet _pps = PictureParameterSet.read(nalData);
                pps.put(_pps.picParameterSetId, _pps);
            } else if (IDR_SLICE == nalUnit.type || NON_IDR_SLICE == nalUnit.type) {
                if (sps.size() == 0 || pps.size() == 0) {
                    Logger.warn("Skipping frame as no SPS/PPS have been seen so far...");
                    return null;
                }
                result.add(createSliceReader(nalData, nalUnit));
            }
        }

        return result;
    }

    private SliceReader createSliceReader(ByteBuffer segment, NALUnit nalUnit) {
        BitReader _in = BitReader.createBitReader(segment);
        SliceHeader sh = SliceHeaderReader.readPart1(_in);
        sh.pps = pps.get(sh.picParameterSetId);
        sh.sps = sps.get(sh.pps.seqParameterSetId);
        SliceHeaderReader.readPart2(sh, nalUnit, sh.sps, sh.pps, _in);

        Mapper mapper = new MapManager(sh.sps, sh.pps).getMapper(sh);

        CAVLC[] cavlc = new CAVLC[]{new CAVLC(sh.sps, sh.pps, 2, 2), new CAVLC(sh.sps, sh.pps, 1, 1),
                new CAVLC(sh.sps, sh.pps, 1, 1)};

        int mbWidth = sh.sps.picWidthInMbsMinus1 + 1;
        CABAC cabac = new CABAC(mbWidth);

        MDecoder mDecoder = null;
        if (sh.pps.entropyCodingModeFlag) {
            _in.terminate();
            int[][] cm = new int[2][1024];
            int qp = sh.pps.picInitQpMinus26 + 26 + sh.sliceQpDelta;
            cabac.initModels(cm, sh.sliceType, sh.cabacInitIdc, qp);
            mDecoder = new MDecoder(segment, cm);
        }

        return new SliceReader(sh.pps, cabac, cavlc, mDecoder, _in, mapper, sh, nalUnit);
    }

    public void addSpsList(List<ByteBuffer> spsList) {
        for (ByteBuffer byteBuffer : spsList) {
            addSps(byteBuffer);
        }
    }

    public void addSps(ByteBuffer byteBuffer) {
        ByteBuffer clone = NIOUtils.clone(byteBuffer);
        unescapeNAL(clone);
        SeqParameterSet s = SeqParameterSet.read(clone);
        sps.put(s.seqParameterSetId, s);
    }

    public void addPpsList(List<ByteBuffer> ppsList) {
        for (ByteBuffer byteBuffer : ppsList) {
            addPps(byteBuffer);
        }
    }

    public void addPps(ByteBuffer byteBuffer) {
        ByteBuffer clone = NIOUtils.clone(byteBuffer);
        unescapeNAL(clone);
        PictureParameterSet p = PictureParameterSet.read(clone);
        pps.put(p.picParameterSetId, p);
    }
}
package org.monte.media.impl.jcodec.codecs.h264;

import org.monte.media.impl.jcodec.codecs.h264.io.model.NALUnit;
import org.monte.media.impl.jcodec.codecs.h264.io.model.RefPicMarking.InstrType;
import org.monte.media.impl.jcodec.codecs.h264.io.model.RefPicMarking.Instruction;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceHeader;

import static org.monte.media.impl.jcodec.codecs.h264.io.model.NALUnitType.IDR_SLICE;

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
 * POC ( Picture Order Count ) manager
 * <p>
 * Picture Order Count is used to represent an order of picture in a GOP ( Group
 * of Pictures ) this is needed to correctly reorder and B-framed GOPs. POC is
 * also used when building lists of reference pictures ( see 8.2.4.2 ).
 * <p>
 * There are 3 possible ways of assigning POC to decoded pictures:
 * <p>
 * - Explicit, i.e. POC is directly specified in a slice header in form <POC
 * Pred> + <POC Dec>. <POC Pred> is a significant part of POC ( see 8.2.1.1 ). -
 * Frame based type 1 ( see 8.2.1.2 ). - Frame based type 2 ( see 8.2.1.3 ).
 *
 * @author The JCodec project
 */
public class POCManager {

    private int prevPOCMsb;
    private int prevPOCLsb;

    public int calcPOC(SliceHeader firstSliceHeader, NALUnit firstNu) {
        switch (firstSliceHeader.sps.picOrderCntType) {
            case 0:
                return calcPOC0(firstSliceHeader, firstNu);
            case 1:
                return calcPOC1(firstSliceHeader, firstNu);
            case 2:
                return calcPOC2(firstSliceHeader, firstNu);
            default:
                throw new RuntimeException("POC no!!!");
        }

    }

    private int calcPOC2(SliceHeader firstSliceHeader, NALUnit firstNu) {
        return firstSliceHeader.frameNum << 1;
    }

    private int calcPOC1(SliceHeader firstSliceHeader, NALUnit firstNu) {
        return firstSliceHeader.frameNum << 1;
    }

    private int calcPOC0(SliceHeader firstSliceHeader, NALUnit firstNu) {
        if (firstNu.type == IDR_SLICE) {
            prevPOCMsb = prevPOCLsb = 0;
        }
        int maxPOCLsbDiv2 = 1 << (firstSliceHeader.sps.log2MaxPicOrderCntLsbMinus4 + 3), maxPOCLsb = maxPOCLsbDiv2 << 1;
        int POCLsb = firstSliceHeader.picOrderCntLsb;

        int POCMsb, POC;
        if ((POCLsb < prevPOCLsb) && ((prevPOCLsb - POCLsb) >= maxPOCLsbDiv2))
            POCMsb = prevPOCMsb + maxPOCLsb;
        else if ((POCLsb > prevPOCLsb) && ((POCLsb - prevPOCLsb) > maxPOCLsbDiv2))
            POCMsb = prevPOCMsb - maxPOCLsb;
        else
            POCMsb = prevPOCMsb;

        POC = POCMsb + POCLsb;

        if (firstNu.nal_ref_idc > 0) {
            if (hasMMCO5(firstSliceHeader, firstNu)) {
                prevPOCMsb = 0;
                prevPOCLsb = POC;
            } else {
                prevPOCMsb = POCMsb;
                prevPOCLsb = POCLsb;
            }
        }

        return POC;
    }

    private boolean hasMMCO5(SliceHeader firstSliceHeader, NALUnit firstNu) {
        if (firstNu.type != IDR_SLICE && firstSliceHeader.refPicMarkingNonIDR != null) {
            Instruction[] instructions = firstSliceHeader.refPicMarkingNonIDR.getInstructions();
            for (int i = 0; i < instructions.length; i++) {
                Instruction instruction = instructions[i];
                if (instruction.getType() == InstrType.CLEAR)
                    return true;
            }
        }
        return false;
    }
}
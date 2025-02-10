package org.monte.media.impl.jcodec.codecs.h264.decode;

import org.monte.media.impl.jcodec.codecs.h264.H264Const;
import org.monte.media.impl.jcodec.codecs.h264.io.model.Frame;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceHeader;
import org.monte.media.impl.jcodec.codecs.h264.io.model.SliceType;
import org.monte.media.impl.jcodec.common.IntObjectMap;
import org.monte.media.impl.jcodec.common.model.Picture;
import org.monte.media.impl.jcodec.platform.Platform;

import java.util.Arrays;
import java.util.Comparator;

import static org.monte.media.impl.jcodec.codecs.h264.decode.MBlockDecoderUtils.debugPrint;
import static org.monte.media.impl.jcodec.common.tools.MathUtil.wrap;

/**
 * Contains reference picture list management logic.
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
 * @author The JCodec Project
 */
public class RefListManager {
    private SliceHeader sh;
    private int[] numRef;
    private Frame[] sRefs;
    private IntObjectMap<Frame> lRefs;
    private Frame frameOut;

    public RefListManager(SliceHeader sh, Frame[] sRefs, IntObjectMap<Frame> lRefs, Frame frameOut) {
        this.sh = sh;
        this.sRefs = sRefs;
        this.lRefs = lRefs;
        if (sh.numRefIdxActiveOverrideFlag)
            numRef = new int[]{sh.numRefIdxActiveMinus1[0] + 1, sh.numRefIdxActiveMinus1[1] + 1};
        else
            numRef = new int[]{sh.pps.numRefIdxActiveMinus1[0] + 1, sh.pps.numRefIdxActiveMinus1[1] + 1};
        this.frameOut = frameOut;
    }

    public Frame[][] getRefList() {
        Frame[][] refList = null;
        if (sh.sliceType == SliceType.P) {
            refList = new Frame[][]{buildRefListP(), null};
        } else if (sh.sliceType == SliceType.B) {
            refList = buildRefListB();
        }

        debugPrint("------");
        if (refList != null) {
            for (int l = 0; l < 2; l++) {
                if (refList[l] != null)
                    for (int i = 0; i < refList[l].length; i++)
                        if (refList[l][i] != null)
                            debugPrint("REF[%d][%d]: ", l, i, ((Frame) refList[l][i]).getPOC());
            }
        }
        return refList;
    }

    private Frame[] buildRefListP() {
        int frame_num = sh.frameNum;
        int maxFrames = 1 << (sh.sps.log2MaxFrameNumMinus4 + 4);
        // int nLongTerm = Math.min(lRefs.size(), numRef[0] - 1);
        Frame[] result = new Frame[numRef[0]];

        int refs = 0;
        for (int i = frame_num - 1; i >= frame_num - maxFrames && refs < numRef[0]; i--) {
            int fn = i < 0 ? i + maxFrames : i;
            if (sRefs[fn] != null) {
                result[refs] = sRefs[fn] == H264Const.NO_PIC ? null : sRefs[fn];
                ++refs;
            }
        }
        int[] keys = lRefs.keys();
        Arrays.sort(keys);
        for (int i = 0; i < keys.length && refs < numRef[0]; i++) {
            result[refs++] = lRefs.get(keys[i]);
        }

        reorder(result, 0);

        return result;
    }

    private Frame[][] buildRefListB() {

        Frame[] l0 = buildList(Frame.POCDesc, Frame.POCAsc);
        Frame[] l1 = buildList(Frame.POCAsc, Frame.POCDesc);

        if (Platform.arrayEqualsObj(l0, l1) && count(l1) > 1) {
            Frame frame = l1[1];
            l1[1] = l1[0];
            l1[0] = frame;
        }

        Frame[][] result = {Platform.copyOfObj(l0, numRef[0]), Platform.copyOfObj(l1, numRef[1])};

        reorder(result[0], 0);
        reorder(result[1], 1);

        return result;
    }

    private Frame[] buildList(Comparator<Frame> cmpFwd, Comparator<Frame> cmpInv) {
        Frame[] refs = new Frame[sRefs.length + lRefs.size()];
        Frame[] fwd = copySort(cmpFwd, frameOut);
        Frame[] inv = copySort(cmpInv, frameOut);
        int nFwd = count(fwd);
        int nInv = count(inv);

        int ref = 0;
        for (int i = 0; i < nFwd; i++, ref++)
            refs[ref] = fwd[i];
        for (int i = 0; i < nInv; i++, ref++)
            refs[ref] = inv[i];

        int[] keys = lRefs.keys();
        Arrays.sort(keys);
        for (int i = 0; i < keys.length; i++, ref++)
            refs[ref] = lRefs.get(keys[i]);

        return refs;
    }

    private int count(Frame[] arr) {
        for (int nn = 0; nn < arr.length; nn++)
            if (arr[nn] == null)
                return nn;
        return arr.length;
    }

    private Frame[] copySort(Comparator<Frame> fwd, Frame dummy) {
        Frame[] copyOf = Platform.copyOfObj(sRefs, sRefs.length);
        for (int i = 0; i < copyOf.length; i++)
            if (fwd.compare(dummy, copyOf[i]) > 0)
                copyOf[i] = null;
        Arrays.sort(copyOf, fwd);
        return copyOf;
    }

    private void reorder(Picture[] result, int list) {
        if (sh.refPicReordering[list] == null)
            return;

        int predict = sh.frameNum;
        int maxFrames = 1 << (sh.sps.log2MaxFrameNumMinus4 + 4);

        for (int ind = 0; ind < sh.refPicReordering[list][0].length; ind++) {
            int refType = sh.refPicReordering[list][0][ind];
            int refIdx = sh.refPicReordering[list][1][ind];

            for (int i = numRef[list] - 1; i > ind; i--)
                result[i] = result[i - 1];
            if (refType == 2) {
                result[ind] = lRefs.get(refIdx);
            } else {
                predict = refType == 0 ? wrap(predict - refIdx - 1, maxFrames) : wrap(predict + refIdx + 1, maxFrames);
                result[ind] = sRefs[predict];
            }
            for (int i = ind + 1, j = i; i < numRef[list] && result[i] != null; i++) {
                if (result[i] != sRefs[predict])
                    result[j++] = result[i];
            }
        }
    }

}

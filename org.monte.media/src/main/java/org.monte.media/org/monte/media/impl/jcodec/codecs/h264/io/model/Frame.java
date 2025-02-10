package org.monte.media.impl.jcodec.codecs.h264.io.model;

import org.monte.media.impl.jcodec.codecs.h264.H264Utils.MvList2D;
import org.monte.media.impl.jcodec.common.model.ColorSpace;
import org.monte.media.impl.jcodec.common.model.Picture;
import org.monte.media.impl.jcodec.common.model.Rect;

import java.util.Comparator;

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
 * Picture extension with frame number, makes it easier to debug reordering
 *
 * @author The JCodec project
 */
public class Frame extends Picture {
    private int frameNo;
    private SliceType frameType;
    private MvList2D mvs;
    private Frame[][][] refsUsed;
    private boolean shortTerm;
    private int poc;

    public Frame(int width, int height, byte[][] data, ColorSpace color, Rect crop, int frameNo, SliceType frameType,
                 MvList2D mvs, Frame[][][] refsUsed, int poc) {
        super(width, height, data, null, color, 0, crop);
        this.frameNo = frameNo;
        this.mvs = mvs;
        this.refsUsed = refsUsed;
        this.poc = poc;
        shortTerm = true;
    }

    public static Frame createFrame(Frame pic) {
        Picture comp = pic.createCompatible();
        return new Frame(comp.getWidth(), comp.getHeight(), comp.getData(), comp.getColor(), pic.getCrop(),
                pic.frameNo, pic.frameType, pic.mvs, pic.refsUsed, pic.poc);
    }

    public Frame cropped() {
        Picture cropped = super.cropped();
        return new Frame(cropped.getWidth(), cropped.getHeight(), cropped.getData(), cropped.getColor(), null, frameNo,
                frameType, mvs, refsUsed, poc);
    }

    public void copyFromFrame(Frame src) {
        super.copyFrom(src);
        this.frameNo = src.frameNo;
        this.mvs = src.mvs;
        this.shortTerm = src.shortTerm;
        this.refsUsed = src.refsUsed;
        this.poc = src.poc;
    }

    /**
     * Creates a cropped clone of this picture.
     *
     * @return
     */
    public Frame cloneCropped() {
        if (cropNeeded()) {
            return cropped();
        } else {
            Frame clone = createFrame(this);
            clone.copyFrom(this);
            return clone;
        }
    }

    public int getFrameNo() {
        return frameNo;
    }

    public MvList2D getMvs() {
        return mvs;
    }

    public boolean isShortTerm() {
        return shortTerm;
    }

    public void setShortTerm(boolean shortTerm) {
        this.shortTerm = shortTerm;
    }

    public int getPOC() {
        return poc;
    }

    public static Comparator<Frame> POCAsc = new Comparator<Frame>() {
        public int compare(Frame o1, Frame o2) {
            if (o1 == null && o2 == null)
                return 0;
            else if (o1 == null)
                return 1;
            else if (o2 == null)
                return -1;
            else
                return o1.poc > o2.poc ? 1 : (o1.poc == o2.poc ? 0 : -1);
        }
    };

    public static Comparator<Frame> POCDesc = new Comparator<Frame>() {
        public int compare(Frame o1, Frame o2) {
            if (o1 == null && o2 == null)
                return 0;
            else if (o1 == null)
                return 1;
            else if (o2 == null)
                return -1;
            else
                return o1.poc < o2.poc ? 1 : (o1.poc == o2.poc ? 0 : -1);
        }
    };

    public Frame[][][] getRefsUsed() {
        return refsUsed;
    }

    public SliceType getFrameType() {
        return frameType;
    }
}

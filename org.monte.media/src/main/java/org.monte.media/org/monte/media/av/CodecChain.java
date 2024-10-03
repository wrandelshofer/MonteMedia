/*
 * @(#)CodecChain.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * {@code CodecChain}.
 *
 * @author Werner Randelshofer
 */
public class CodecChain implements Codec {

    private Codec first;
    private Codec second;
    private Buffer tmpBuf;
    private int firstState;
    private int secondState;

    public CodecChain(Codec first, Codec second) {
        if (first == null || second == null) throw new IllegalArgumentException("first and second must not be null");
        this.first = first;
        this.second = second;
    }

    public static Codec createCodecChain(List<Codec> codecs) {
        return createCodecChain(codecs.toArray(new Codec[codecs.size()]));
    }

    public static Codec createCodecChain(Codec... codecs) {
        // get rid of all null values
        ArrayList<Codec> clist = new ArrayList<>();
        for (Codec c : codecs) {
            if (c != null) {
                clist.add(c);
            }
        }
        if (clist.isEmpty()) {
            return null;
        }
        if (clist.size() == 1) {
            return codecs[0];
        } else {
            CodecChain cc = new CodecChain(clist.get(clist.size() - 2), clist.get(clist.size() - 1));
            for (int i = clist.size() - 3; i >= 0; i--) {
                cc = new CodecChain(clist.get(i), cc);
            }
            return cc;
        }
    }

    @Override
    public Format[] getInputFormats() {
        return first.getInputFormats();
    }

    @Override
    public Format[] getOutputFormats(Format input) {
        ArrayList<Format> secondOuts = new ArrayList<>();
        for (Format firstOut : first.getOutputFormats(input)) {
            secondOuts.addAll(Arrays.asList(second.getOutputFormats(firstOut)));
        }

        return secondOuts.toArray(new Format[secondOuts.size()]);
    }

    @Override
    public Format setInputFormat(Format input) {
        return second.setInputFormat(first.setInputFormat(input));
    }

    @Override
    public Format getInputFormat() {
        return first.getInputFormat();
    }

    @Override
    public Format setOutputFormat(Format output) {
        return second.setOutputFormat(output);
    }

    @Override
    public Format getOutputFormat() {
        return second.getOutputFormat();
    }

    @Override
    public int process(Buffer in, Buffer out) {
        if (tmpBuf == null) {
            tmpBuf = new Buffer();
        }


        if (CODEC_INPUT_NOT_CONSUMED == (secondState & CODEC_INPUT_NOT_CONSUMED)) {
            // => second codec needs to process tmpBuffer again
            long start = (System.nanoTime() / 1_000_000);
            secondState = second.process(tmpBuf, out);
            return secondState;
        }


        firstState = first.process(in, tmpBuf);
        if (firstState == CODEC_FAILED) {
            return firstState;
        }
        if (CODEC_OUTPUT_NOT_FILLED == (firstState & CODEC_OUTPUT_NOT_FILLED)) {
            // => first codec needs to process tmpBuffer again
            return firstState;
        }

        secondState = second.process(tmpBuf, out);
        if (secondState == CODEC_FAILED) {
            return secondState;
        }

        return (secondState & (-1 ^ CODEC_INPUT_NOT_CONSUMED)) | (firstState & (-1 ^ CODEC_OUTPUT_NOT_FILLED));
    }

    @Override
    public String getName() {
        return first.getName() + ", " + second.getName();
    }

    @Override
    public void reset() {
        first.reset();
        second.reset();
        tmpBuf = null;
    }

    @Override
    public String toString() {
        return "CodecChain{" + first + "," + second + "}";
    }
}

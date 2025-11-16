/*
 * @(#)AmigaReuseImages.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.amigabitmap;

import org.monte.media.color.quant.ColorQuantizer;
import org.monte.media.color.quant.OctreeColorQuantizer;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferByte;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;

class AmigaReuseImages {
    static AmigaBitmapImage reuseInputImage(AmigaBitmapImage input, BufferedImage output) {
        return input;
    }

    static BufferedImage reuseOutputImage(AmigaBitmapImage input, BufferedImage output) {
        ColorModel inputColorModel = input.getColorModel();
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        if (output != null
                && output.getWidth() == inputWidth
                && output.getHeight() == inputHeight) {
            ColorModel outputColorModel = output.getColorModel();
            if (inputColorModel == outputColorModel
                    || inputColorModel instanceof AmigaHAMColorModel && outputColorModel instanceof DirectColorModel) {
                return output;
            }
            if (inputColorModel instanceof IndexColorModel && output.getType() == BufferedImage.TYPE_BYTE_INDEXED) {
                return new BufferedImage(inputColorModel, output.getRaster(), false, null);
            }
        }
        if (inputColorModel instanceof IndexColorModel icm && !(inputColorModel instanceof AmigaHAMColorModel)) {
            return new BufferedImage(inputWidth, inputHeight, BufferedImage.TYPE_BYTE_INDEXED, icm);
        }
        return new BufferedImage(inputWidth, inputHeight, BufferedImage.TYPE_INT_RGB);
    }

    static BufferedImage reuseInputImage(BufferedImage input, AmigaBitmapImage output) {
        if (!(input.getColorModel() instanceof IndexColorModel)) {
            ColorQuantizer q = new OctreeColorQuantizer(output == null ? 256 : 1 << (output.getDepth() - 1));
            q.addImage(input);
            var icm = q.computeColorPalette();
            int height = input.getHeight();
            int width = input.getWidth();
            BufferedImage dst = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, icm);
            int[] rgbArray = new int[width * height];
            input.getRGB(0, 0, width, height, rgbArray, 0, width);
            byte[] dstArray = ((DataBufferByte) dst.getRaster().getDataBuffer()).getData();
            for (int i = 0, n = Math.min(rgbArray.length, dstArray.length); i < n; i++) {
                dstArray[i] = (byte) q.quant(rgbArray[i]);
            }
            return dst;
        }
        return input;
    }

    static AmigaBitmapImage reuseOutputImage(BufferedImage input, AmigaBitmapImage output) {
        ColorModel inputColorModel = input.getColorModel();
        int inputWidth = input.getWidth();
        int inputHeight = input.getHeight();
        if (output != null
                && output.getWidth() == inputWidth
                && output.getHeight() == inputHeight) {
            ColorModel outputColorModel = output.getColorModel();
            if (inputColorModel == outputColorModel) {
                return output;
            }
            if (inputColorModel instanceof IndexColorModel icm) {
                output.setColorModel(icm);
                return output;
            }
        }
        if (inputColorModel instanceof AmigaHAMColorModel icm) {
            return new AmigaBitmapImage(inputWidth, inputHeight, icm.getType() == AmigaHAMColorModel.Type.HAM8 ? 8 : 6, icm);
        }
        if (inputColorModel instanceof IndexColorModel icm) {
            return new AmigaBitmapImage(inputWidth, inputHeight, Math.clamp(32 - (Integer.numberOfLeadingZeros(icm.getMapSize() - 1)), 1, 8), icm);
        }
        throw new UnsupportedOperationException("can not convert " + input);
    }
}

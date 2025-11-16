/*
 * @(#)LongMultiplyAmigaBitmapImageConverter.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.amigabitmap;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

/**
 * {@link AmigaBitmapImageConverter} optimised for processors that provide
 * long multiplication instructions.
 * <p>
 * This class performs well on processors, that do not provide PDEP (Parallel Deposit/Expand) and
 * PEXT (Parallel Extract) instructions, such as Apple M processors.
 */
public class LongMultiplyAmigaBitmapImageConverter implements AmigaBitmapImageConverter {
    private static final VarHandle LONG_BE = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.BIG_ENDIAN);

    private AmigaBitmapImage bytePixelsToPlanar1(BufferedImage input, AmigaBitmapImage output) {
        byte[] bitmap = output.getBitmap();
        byte[] pixel = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
        int scanlineStride = output.getScanlineStride();
        int width = input.getWidth();
        int height = input.getHeight();
        int bottomScanline = height * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                long chunky = (long) LONG_BE.get(pixel, pixelIndex);//array,offset,value
                int iBitmap = i + iScanline;
                bitmap[iBitmap] = (byte) ((chunky & 1) | (Math.multiplyHigh(chunky & 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L, 0x102040810204080L) << 1));
                pixelIndex += 8;
            }
        }
        return output;
    }

    private AmigaBitmapImage bytePixelsToPlanar2(BufferedImage input, AmigaBitmapImage output) {
        byte[] bitmap = output.getBitmap();
        byte[] pixel = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
        int scanlineStride = output.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = output.getBitplaneStride();
        int height = input.getHeight();
        int bottomScanline = height * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                long chunky = (long) LONG_BE.get(pixel, pixelIndex);//array,offset,value
                int iBitmap = i + iScanline;
                bitmap[iBitmap] = (byte) ((chunky & 1) | (Math.multiplyHigh(chunky & 0x0101010101010101L, 0x102040810204080L) << 1));
                bitmap[iBitmap + bitplaneStride] = (byte) (((chunky >> 1) & 1) | (Math.multiplyHigh(chunky & 0x0202020202020202L, 0x81020408102040L) << 1));
                pixelIndex += 8;
            }
        }
        return output;
    }

    private AmigaBitmapImage bytePixelsToPlanar3(BufferedImage input, AmigaBitmapImage output) {
        byte[] bitmap = output.getBitmap();
        byte[] pixel = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
        int scanlineStride = output.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = output.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int height = input.getHeight();
        int bottomScanline = height * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                long chunky = (long) LONG_BE.get(pixel, pixelIndex);//array,offset,value
                int iBitmap = i + iScanline;
                bitmap[iBitmap] = (byte) ((chunky & 1) | (Math.multiplyHigh(chunky & 0x0101010101010101L, 0x102040810204080L) << 1));
                bitmap[iBitmap + bitplaneStride] = (byte) (((chunky >> 1) & 1) | (Math.multiplyHigh(chunky & 0x0202020202020202L, 0x81020408102040L) << 1));
                bitmap[iBitmap + bitplaneStride2] = (byte) (((chunky >> 2) & 1) | (Math.multiplyHigh(chunky & 0x0404040404040404L, 0x40810204081020L) << 1));
                pixelIndex += 8;
            }
        }
        return output;
    }

    private AmigaBitmapImage bytePixelsToPlanar4(BufferedImage input, AmigaBitmapImage output) {
        byte[] bitmap = output.getBitmap();
        byte[] pixel = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
        int scanlineStride = output.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = output.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bitplaneStride3 = bitplaneStride * 3;
        int height = input.getHeight();
        int bottomScanline = height * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                long chunky = (long) LONG_BE.get(pixel, pixelIndex);//array,offset,value
                int iBitmap = i + iScanline;
                bitmap[iBitmap] = (byte) ((chunky & 1) | (Math.multiplyHigh(chunky & 0x0101010101010101L, 0x102040810204080L) << 1));
                bitmap[iBitmap + bitplaneStride] = (byte) (((chunky >> 1) & 1) | (Math.multiplyHigh(chunky & 0x0202020202020202L, 0x81020408102040L) << 1));
                bitmap[iBitmap + bitplaneStride2] = (byte) (((chunky >> 2) & 1) | (Math.multiplyHigh(chunky & 0x0404040404040404L, 0x40810204081020L) << 1));
                bitmap[iBitmap + bitplaneStride3] = (byte) (((chunky >> 3) & 1) | (Math.multiplyHigh(chunky & 0x0808080808080808L, 0x20408102040810L) << 1));
                pixelIndex += 8;
            }
        }
        return output;
    }

    private AmigaBitmapImage bytePixelsToPlanar5(BufferedImage input, AmigaBitmapImage output) {
        byte[] bitmap = output.getBitmap();
        byte[] pixel = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
        int scanlineStride = output.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = output.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bitplaneStride3 = bitplaneStride * 3;
        int bitplaneStride4 = bitplaneStride * 4;
        int height = input.getHeight();
        int bottomScanline = height * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                long chunky = (long) LONG_BE.get(pixel, pixelIndex);//array,offset,value
                int iBitmap = i + iScanline;
                bitmap[iBitmap] = (byte) ((chunky & 1) | (Math.multiplyHigh(chunky & 0x0101010101010101L, 0x102040810204080L) << 1));
                bitmap[iBitmap + bitplaneStride] = (byte) (((chunky >> 1) & 1) | (Math.multiplyHigh(chunky & 0x0202020202020202L, 0x81020408102040L) << 1));
                bitmap[iBitmap + bitplaneStride2] = (byte) (((chunky >> 2) & 1) | (Math.multiplyHigh(chunky & 0x0404040404040404L, 0x40810204081020L) << 1));
                bitmap[iBitmap + bitplaneStride3] = (byte) (((chunky >> 3) & 1) | (Math.multiplyHigh(chunky & 0x0808080808080808L, 0x20408102040810L) << 1));
                bitmap[iBitmap + bitplaneStride4] = (byte) (((chunky >> 4) & 1) | (Math.multiplyHigh(chunky & 0x1010101010101010L, 0x10204081020408L) << 1));
                pixelIndex += 8;
            }
        }
        return output;
    }

    private AmigaBitmapImage bytePixelsToPlanar6(BufferedImage input, AmigaBitmapImage output) {
        byte[] bitmap = output.getBitmap();
        byte[] pixel = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
        int scanlineStride = output.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = output.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bitplaneStride3 = bitplaneStride * 3;
        int bitplaneStride4 = bitplaneStride * 4;
        int bitplaneStride5 = bitplaneStride * 5;
        int height = input.getHeight();
        int bottomScanline = height * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                long chunky = (long) LONG_BE.get(pixel, pixelIndex);//array,offset,value
                int iBitmap = i + iScanline;
                bitmap[iBitmap] = (byte) ((chunky & 1) | (Math.multiplyHigh(chunky & 0x0101010101010101L, 0x102040810204080L) << 1));
                bitmap[iBitmap + bitplaneStride] = (byte) (((chunky >> 1) & 1) | (Math.multiplyHigh(chunky & 0x0202020202020202L, 0x81020408102040L) << 1));
                bitmap[iBitmap + bitplaneStride2] = (byte) (((chunky >> 2) & 1) | (Math.multiplyHigh(chunky & 0x0404040404040404L, 0x40810204081020L) << 1));
                bitmap[iBitmap + bitplaneStride3] = (byte) (((chunky >> 3) & 1) | (Math.multiplyHigh(chunky & 0x0808080808080808L, 0x20408102040810L) << 1));
                bitmap[iBitmap + bitplaneStride4] = (byte) (((chunky >> 4) & 1) | (Math.multiplyHigh(chunky & 0x1010101010101010L, 0x10204081020408L) << 1));
                bitmap[iBitmap + bitplaneStride5] = (byte) (((chunky >> 5) & 1) | (Math.multiplyHigh(chunky & 0x2020202020202020L, 0x8102040810204L) << 1));
                pixelIndex += 8;
            }
        }
        return output;
    }

    private AmigaBitmapImage bytePixelsToPlanar7(BufferedImage input, AmigaBitmapImage output) {
        byte[] bitmap = output.getBitmap();
        byte[] pixel = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
        int scanlineStride = output.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = output.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bitplaneStride3 = bitplaneStride * 3;
        int bitplaneStride4 = bitplaneStride * 4;
        int bitplaneStride5 = bitplaneStride * 5;
        int bitplaneStride6 = bitplaneStride * 6;
        int height = input.getHeight();
        int bottomScanline = height * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                long chunky = (long) LONG_BE.get(pixel, pixelIndex);//array,offset,value
                int iBitmap = i + iScanline;
                bitmap[iBitmap] = (byte) ((chunky & 1) | (Math.multiplyHigh(chunky & 0x0101010101010101L, 0x102040810204080L) << 1));
                bitmap[iBitmap + bitplaneStride] = (byte) (((chunky >> 1) & 1) | (Math.multiplyHigh(chunky & 0x0202020202020202L, 0x81020408102040L) << 1));
                bitmap[iBitmap + bitplaneStride2] = (byte) (((chunky >> 2) & 1) | (Math.multiplyHigh(chunky & 0x0404040404040404L, 0x40810204081020L) << 1));
                bitmap[iBitmap + bitplaneStride3] = (byte) (((chunky >> 3) & 1) | (Math.multiplyHigh(chunky & 0x0808080808080808L, 0x20408102040810L) << 1));
                bitmap[iBitmap + bitplaneStride4] = (byte) (((chunky >> 4) & 1) | (Math.multiplyHigh(chunky & 0x1010101010101010L, 0x10204081020408L) << 1));
                bitmap[iBitmap + bitplaneStride5] = (byte) (((chunky >> 5) & 1) | (Math.multiplyHigh(chunky & 0x2020202020202020L, 0x8102040810204L) << 1));
                bitmap[iBitmap + bitplaneStride6] = (byte) (((chunky >> 6) & 1) | (Math.multiplyHigh(chunky & 0x4040404040404040L, 0x4081020408102L) << 1));
                pixelIndex += 8;
            }
        }
        return output;
    }

    private AmigaBitmapImage bytePixelsToPlanar8(BufferedImage input, AmigaBitmapImage output) {
        byte[] bitmap = output.getBitmap();
        byte[] pixel = ((DataBufferByte) input.getRaster().getDataBuffer()).getData();
        int scanlineStride = output.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = output.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bitplaneStride3 = bitplaneStride * 3;
        int bitplaneStride4 = bitplaneStride * 4;
        int bitplaneStride5 = bitplaneStride * 5;
        int bitplaneStride6 = bitplaneStride * 6;
        int bitplaneStride7 = bitplaneStride * 7;
        int height = input.getHeight();
        int bottomScanline = height * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                long chunky = (long) LONG_BE.get(pixel, pixelIndex);//array,offset,value
                int iBitmap = i + iScanline;
                bitmap[iBitmap] = (byte) ((chunky & 1) | (Math.multiplyHigh(chunky & 0x0101010101010101L, 0x102040810204080L) << 1));
                bitmap[iBitmap + bitplaneStride] = (byte) (((chunky >> 1) & 1) | (Math.multiplyHigh(chunky & 0x0202020202020202L, 0x81020408102040L) << 1));
                bitmap[iBitmap + bitplaneStride2] = (byte) (((chunky >> 2) & 1) | (Math.multiplyHigh(chunky & 0x0404040404040404L, 0x40810204081020L) << 1));
                bitmap[iBitmap + bitplaneStride3] = (byte) (((chunky >> 3) & 1) | (Math.multiplyHigh(chunky & 0x0808080808080808L, 0x20408102040810L) << 1));
                bitmap[iBitmap + bitplaneStride4] = (byte) (((chunky >> 4) & 1) | (Math.multiplyHigh(chunky & 0x1010101010101010L, 0x10204081020408L) << 1));
                bitmap[iBitmap + bitplaneStride5] = (byte) (((chunky >> 5) & 1) | (Math.multiplyHigh(chunky & 0x2020202020202020L, 0x8102040810204L) << 1));
                bitmap[iBitmap + bitplaneStride6] = (byte) (((chunky >> 6) & 1) | (Math.multiplyHigh(chunky & 0x4040404040404040L, 0x4081020408102L) << 1));
                bitmap[iBitmap + bitplaneStride7] = (byte) (((chunky >> 7) & 1) | (Math.multiplyHigh((chunky >> 7) & 0x0101010101010101L, 0x102040810204080L) << 1));
                pixelIndex += 8;
            }
        }
        return output;
    }

    private BufferedImage ham6ToDirectPixels(AmigaBitmapImage input, BufferedImage output) {
        byte[] bitmap = input.getBitmap();
        int[] pixel = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
        int scanlineStride = input.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = input.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bitplaneStride3 = bitplaneStride * 3;
        int bitplaneStride4 = bitplaneStride * 4;
        int bitplaneStride5 = bitplaneStride * 5;
        int height = input.getHeight();
        int bottomScanline = height * scanlineStride;
        AmigaHAMColorModel colorModel = (AmigaHAMColorModel) input.getColorModel();
        int[] cmap = new int[1 << colorModel.getPixelSize() - 2];
        colorModel.getRGBs(cmap);
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            int colorRegister = cmap[0];
            for (int i = 0; i < width >>> 3; i++) {
                int iBitmap = i + iScanline;
                int plane0 = bitmap[iBitmap];
                int plane1 = bitmap[iBitmap + bitplaneStride];
                int plane2 = bitmap[iBitmap + bitplaneStride2];
                int plane3 = bitmap[iBitmap + bitplaneStride3];
                int plane4 = bitmap[iBitmap + bitplaneStride4];
                int plane5 = bitmap[iBitmap + bitplaneStride5];
                long chunky =
                        Long.expand(plane0, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L)
                                | Long.expand(plane1, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L)
                                | Long.expand(plane2, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L)
                                | Long.expand(plane3, 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L)
                                | Long.expand(plane4, 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L)
                                | Long.expand(plane5, 0b00100000_00100000_00100000_00100000_00100000_00100000_00100000_00100000L);
                for (int j = 56; j >= 0; j -= 8) {
                    int instruction = (int) (chunky >>> j) & 0x3f;
                    int opcode = instruction >> 4;
                    int argument = instruction & 0b1111;
                    colorRegister = switch (opcode) {
                        case 0b00 -> cmap[argument];
                        case 0b01 ->//blue
                                colorRegister & 0xffffff00 | (argument << 4) | argument;
                        case 0b10 ->//red
                                colorRegister & 0xff00ffff | (argument << 20) | (argument << 16);
                        case 0b11 ->//green
                                colorRegister & 0xffff00ff | (argument << 12) | (argument << 8);
                        default -> colorRegister;
                    };
                    pixel[pixelIndex++] = colorRegister;
                }
            }
        }
        return output;
    }

    private BufferedImage ham8ToDirectPixels(AmigaBitmapImage input, BufferedImage output) {
        byte[] bitmap = input.getBitmap();
        int[] pixel = ((DataBufferInt) output.getRaster().getDataBuffer()).getData();
        int scanlineStride = input.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = input.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bitplaneStride3 = bitplaneStride * 3;
        int bitplaneStride4 = bitplaneStride * 4;
        int bitplaneStride5 = bitplaneStride * 5;
        int bitplaneStride6 = bitplaneStride * 6;
        int bitplaneStride7 = bitplaneStride * 7;
        int height = input.getHeight();
        int bottomScanline = height * scanlineStride;
        AmigaHAMColorModel colorModel = (AmigaHAMColorModel) input.getColorModel();
        int[] cmap = new int[1 << colorModel.getPixelSize() - 2];
        colorModel.getRGBs(cmap);
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            int colorRegister = cmap[0];
            for (int i = 0; i < width >>> 3; i++) {
                int iBitmap = i + iScanline;
                int plane0 = bitmap[iBitmap];
                int plane1 = bitmap[iBitmap + bitplaneStride];
                int plane2 = bitmap[iBitmap + bitplaneStride2];
                int plane3 = bitmap[iBitmap + bitplaneStride3];
                int plane4 = bitmap[iBitmap + bitplaneStride4];
                int plane5 = bitmap[iBitmap + bitplaneStride5];
                int plane6 = bitmap[iBitmap + bitplaneStride6];
                int plane7 = bitmap[iBitmap + bitplaneStride7];
                long chunky = Long.expand(plane0, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L)
                        | Long.expand(plane1, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L)
                        | Long.expand(plane2, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L)
                        | Long.expand(plane3, 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L)
                        | Long.expand(plane4, 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L)
                        | Long.expand(plane5, 0b00100000_00100000_00100000_00100000_00100000_00100000_00100000_00100000L)
                        | Long.expand(plane6, 0b01000000_01000000_01000000_01000000_01000000_01000000_01000000_01000000L)
                        | Long.expand(plane7, 0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L);
                for (int j = 56; j >= 0; j -= 8) {
                    int instruction = (int) (chunky >>> j) & 0xff;
                    int opcode = instruction >> 6;
                    int argument = instruction & 0b111111;
                    colorRegister = switch (opcode) {
                        // normal mode
                        case 0b00 -> cmap[argument];
                        // hold and modify blue
                        case 0b01 -> colorRegister & 0xffffff03 | (argument << 2);
                        // hold and modify red;
                        case 0b10 -> colorRegister & 0xff03ffff | (argument << 18);
                        // hold and modify green;
                        case 0b11 -> colorRegister & 0xffff03ff | (argument << 10);
                        default -> colorRegister;
                    };
                    pixel[pixelIndex++] = colorRegister;
                }
            }
        }
        return output;
    }

    private BufferedImage planar1ToBytePixels(AmigaBitmapImage input, BufferedImage output) {
        byte[] bitmap = input.getBitmap();
        byte[] pixel = ((DataBufferByte) output.getRaster().getDataBuffer()).getData();
        int scanlineStride = input.getScanlineStride();
        int width = input.getWidth();
        int bottomScanline = input.getHeight() * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                int iBitmap = i + iScanline;
                int plane0 = bitmap[iBitmap];
                long chunky = ((plane0 & 1) | (((plane0 & 0b11111110) * 0b00000001_00000010_00000100_00001000_00010000_00100000_01000000_10000000L)
                        & 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L));
                LONG_BE.set(pixel, pixelIndex, chunky);//array,offset,value
                pixelIndex += 8;
            }
        }
        return output;
    }

    private BufferedImage planar2ToBytePixels(AmigaBitmapImage input, BufferedImage output) {
        byte[] bitmap = input.getBitmap();
        byte[] pixel = ((DataBufferByte) output.getRaster().getDataBuffer()).getData();
        int scanlineStride = input.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = input.getBitplaneStride();
        int bottomScanline = input.getHeight() * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                int iBitmap = i + iScanline;
                int plane0 = bitmap[iBitmap];
                int plane1 = bitmap[iBitmap + bitplaneStride];
                long chunky = ((plane0 & 1) | (((plane0 & 0b11111110) * 0b00000001_00000010_00000100_00001000_00010000_00100000_01000000_10000000L)
                        & 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L))
                        | (((plane1 << 1) & 0b10) | (((plane1 & 0b11111110) * 0b0000001_00000010_00000100_00001000_00010000_00100000_01000000_100000000L)
                        & 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L));
                LONG_BE.set(pixel, pixelIndex, chunky);//array,offset,value
                pixelIndex += 8;
            }
        }
        return output;
    }

    private BufferedImage planar3ToBytePixels(AmigaBitmapImage input, BufferedImage output) {
        byte[] bitmap = input.getBitmap();
        byte[] pixel = ((DataBufferByte) output.getRaster().getDataBuffer()).getData();
        int scanlineStride = input.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = input.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bottomScanline = input.getHeight() * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                int iBitmap = i + iScanline;
                int plane0 = bitmap[iBitmap];
                int plane1 = bitmap[iBitmap + bitplaneStride];
                int plane2 = bitmap[iBitmap + bitplaneStride2];
                long chunky = ((plane0 & 1) | (((plane0 & 0b11111110) * 0b00000001_00000010_00000100_00001000_00010000_00100000_01000000_10000000L)
                        & 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L))
                        | (((plane1 << 1) & 0b10) | (((plane1 & 0b11111110) * 0b0000001_00000010_00000100_00001000_00010000_00100000_01000000_100000000L)
                        & 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L))
                        | (((plane2 << 2) & 0b100) | (((plane2 & 0b11111110) * 0b000001_00000010_00000100_00001000_00010000_00100000_01000000_1000000000L)
                        & 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L));
                LONG_BE.set(pixel, pixelIndex, chunky);//array,offset,value
                pixelIndex += 8;
            }
        }
        return output;
    }

    private BufferedImage planar4ToBytePixels(AmigaBitmapImage input, BufferedImage output) {
        byte[] bitmap = input.getBitmap();
        byte[] pixel = ((DataBufferByte) output.getRaster().getDataBuffer()).getData();
        int scanlineStride = input.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = input.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bitplaneStride3 = bitplaneStride * 3;
        int bottomScanline = input.getHeight() * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                int iBitmap = i + iScanline;
                int plane0 = bitmap[iBitmap];
                int plane1 = bitmap[iBitmap + bitplaneStride];
                int plane2 = bitmap[iBitmap + bitplaneStride2];
                int plane3 = bitmap[iBitmap + bitplaneStride3];
                long chunky = ((plane0 & 1) | (((plane0 & 0b11111110) * 0b00000001_00000010_00000100_00001000_00010000_00100000_01000000_10000000L)
                        & 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L))
                        | (((plane1 << 1) & 0b10) | (((plane1 & 0b11111110) * 0b0000001_00000010_00000100_00001000_00010000_00100000_01000000_100000000L)
                        & 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L))
                        | (((plane2 << 2) & 0b100) | (((plane2 & 0b11111110) * 0b000001_00000010_00000100_00001000_00010000_00100000_01000000_1000000000L)
                        & 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L))
                        | (((plane3 << 3) & 0b1000) | (((plane3 & 0b11111110) * 0b00001_00000010_00000100_00001000_00010000_00100000_01000000_10000000000L)
                        & 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L));
                LONG_BE.set(pixel, pixelIndex, chunky);//array,offset,value
                pixelIndex += 8;
            }
        }
        return output;
    }

    private BufferedImage planar5ToBytePixels(AmigaBitmapImage input, BufferedImage output) {
        byte[] bitmap = input.getBitmap();
        byte[] pixel = ((DataBufferByte) output.getRaster().getDataBuffer()).getData();
        int scanlineStride = input.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = input.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bitplaneStride3 = bitplaneStride * 3;
        int bitplaneStride4 = bitplaneStride * 4;
        int bottomScanline = input.getHeight() * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                int iBitmap = i + iScanline;
                int plane0 = bitmap[iBitmap];
                int plane1 = bitmap[iBitmap + bitplaneStride];
                int plane2 = bitmap[iBitmap + bitplaneStride2];
                int plane3 = bitmap[iBitmap + bitplaneStride3];
                int plane4 = bitmap[iBitmap + bitplaneStride4];
                long chunky = ((plane0 & 1) | (((plane0 & 0b11111110) * 0b00000001_00000010_00000100_00001000_00010000_00100000_01000000_10000000L)
                        & 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L))
                        | (((plane1 << 1) & 0b10) | (((plane1 & 0b11111110) * 0b0000001_00000010_00000100_00001000_00010000_00100000_01000000_100000000L)
                        & 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L))
                        | (((plane2 << 2) & 0b100) | (((plane2 & 0b11111110) * 0b000001_00000010_00000100_00001000_00010000_00100000_01000000_1000000000L)
                        & 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L))
                        | (((plane3 << 3) & 0b1000) | (((plane3 & 0b11111110) * 0b00001_00000010_00000100_00001000_00010000_00100000_01000000_10000000000L)
                        & 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L))
                        | (((plane4 << 4) & 0b10000) | (((plane4 & 0b11111110) * 0b0001_00000010_00000100_00001000_00010000_00100000_01000000_100000000000L)
                        & 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L));
                LONG_BE.set(pixel, pixelIndex, chunky);//array,offset,value
                pixelIndex += 8;
            }
        }
        return output;
    }

    private BufferedImage planar6ToBytePixels(AmigaBitmapImage input, BufferedImage output) {
        byte[] bitmap = input.getBitmap();
        byte[] pixel = ((DataBufferByte) output.getRaster().getDataBuffer()).getData();
        int scanlineStride = input.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = input.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bitplaneStride3 = bitplaneStride * 3;
        int bitplaneStride4 = bitplaneStride * 4;
        int bitplaneStride5 = bitplaneStride * 5;
        int bottomScanline = input.getHeight() * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                int iBitmap = i + iScanline;
                int plane0 = bitmap[iBitmap];
                int plane1 = bitmap[iBitmap + bitplaneStride];
                int plane2 = bitmap[iBitmap + bitplaneStride2];
                int plane3 = bitmap[iBitmap + bitplaneStride3];
                int plane4 = bitmap[iBitmap + bitplaneStride4];
                int plane5 = bitmap[iBitmap + bitplaneStride5];
                long chunky = ((plane0 & 1) | (((plane0 & 0b11111110) * 0b00000001_00000010_00000100_00001000_00010000_00100000_01000000_10000000L)
                        & 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L))
                        | (((plane1 << 1) & 0b10) | (((plane1 & 0b11111110) * 0b0000001_00000010_00000100_00001000_00010000_00100000_01000000_100000000L)
                        & 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L))
                        | (((plane2 << 2) & 0b100) | (((plane2 & 0b11111110) * 0b000001_00000010_00000100_00001000_00010000_00100000_01000000_1000000000L)
                        & 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L))
                        | (((plane3 << 3) & 0b1000) | (((plane3 & 0b11111110) * 0b00001_00000010_00000100_00001000_00010000_00100000_01000000_10000000000L)
                        & 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L))
                        | (((plane4 << 4) & 0b10000) | (((plane4 & 0b11111110) * 0b0001_00000010_00000100_00001000_00010000_00100000_01000000_100000000000L)
                        & 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L))
                        | (((plane5 << 5) & 0b100000) | (((plane5 & 0b11111110) * 0b001_00000010_00000100_00001000_00010000_00100000_01000000_1000000000000L)
                        & 0b00100000_00100000_00100000_00100000_00100000_00100000_00100000_00100000L));
                LONG_BE.set(pixel, pixelIndex, chunky);//array,offset,value
                pixelIndex += 8;
            }
        }
        return output;
    }

    private BufferedImage planar7ToBytePixels(AmigaBitmapImage input, BufferedImage output) {
        byte[] bitmap = input.getBitmap();
        byte[] pixel = ((DataBufferByte) output.getRaster().getDataBuffer()).getData();
        int scanlineStride = input.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = input.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bitplaneStride3 = bitplaneStride * 3;
        int bitplaneStride4 = bitplaneStride * 4;
        int bitplaneStride5 = bitplaneStride * 5;
        int bitplaneStride6 = bitplaneStride * 6;
        int height = input.getHeight();
        int bottomScanline = height * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                int iBitmap = i + iScanline;
                int plane0 = bitmap[iBitmap];
                int plane1 = bitmap[iBitmap + bitplaneStride];
                int plane2 = bitmap[iBitmap + bitplaneStride2];
                int plane3 = bitmap[iBitmap + bitplaneStride3];
                int plane4 = bitmap[iBitmap + bitplaneStride4];
                int plane5 = bitmap[iBitmap + bitplaneStride5];
                int plane6 = bitmap[iBitmap + bitplaneStride6];
                long chunky = ((plane0 & 1) | (((plane0 & 0b11111110) * 0b00000001_00000010_00000100_00001000_00010000_00100000_01000000_10000000L)
                        & 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L))
                        | (((plane1 << 1) & 0b10) | (((plane1 & 0b11111110) * 0b0000001_00000010_00000100_00001000_00010000_00100000_01000000_100000000L)
                        & 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L))
                        | (((plane2 << 2) & 0b100) | (((plane2 & 0b11111110) * 0b000001_00000010_00000100_00001000_00010000_00100000_01000000_1000000000L)
                        & 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L))
                        | (((plane3 << 3) & 0b1000) | (((plane3 & 0b11111110) * 0b00001_00000010_00000100_00001000_00010000_00100000_01000000_10000000000L)
                        & 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L))
                        | (((plane4 << 4) & 0b10000) | (((plane4 & 0b11111110) * 0b0001_00000010_00000100_00001000_00010000_00100000_01000000_100000000000L)
                        & 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L))
                        | (((plane5 << 5) & 0b100000) | (((plane5 & 0b11111110) * 0b001_00000010_00000100_00001000_00010000_00100000_01000000_1000000000000L)
                        & 0b00100000_00100000_00100000_00100000_00100000_00100000_00100000_00100000L))
                        | (((plane6 << 6) & 0b1000000) | (((plane6 & 0b11111110) * 0b01_00000010_00000100_00001000_00010000_00100000_01000000_10000000000000L)
                        & 0b01000000_01000000_01000000_01000000_01000000_01000000_01000000_01000000L));
                LONG_BE.set(pixel, pixelIndex, chunky);//array,offset,value
                pixelIndex += 8;
            }
        }
        return output;
    }

    private BufferedImage planar8ToBytePixels(AmigaBitmapImage input, BufferedImage output) {
        byte[] bitmap = input.getBitmap();
        byte[] pixel = ((DataBufferByte) output.getRaster().getDataBuffer()).getData();
        int scanlineStride = input.getScanlineStride();
        int width = input.getWidth();
        int bitplaneStride = input.getBitplaneStride();
        int bitplaneStride2 = bitplaneStride * 2;
        int bitplaneStride3 = bitplaneStride * 3;
        int bitplaneStride4 = bitplaneStride * 4;
        int bitplaneStride5 = bitplaneStride * 5;
        int bitplaneStride6 = bitplaneStride * 6;
        int bitplaneStride7 = bitplaneStride * 7;
        int height = input.getHeight();
        int bottomScanline = height * scanlineStride;
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            for (int i = 0; i < width >>> 3; i++) {
                int iBitmap = i + iScanline;
                int plane0 = bitmap[iBitmap];
                int plane1 = bitmap[iBitmap + bitplaneStride];
                int plane2 = bitmap[iBitmap + bitplaneStride2];
                int plane3 = bitmap[iBitmap + bitplaneStride3];
                int plane4 = bitmap[iBitmap + bitplaneStride4];
                int plane5 = bitmap[iBitmap + bitplaneStride5];
                int plane6 = bitmap[iBitmap + bitplaneStride6];
                int plane7 = bitmap[iBitmap + bitplaneStride7];

                long chunky = ((plane0 & 1) | (((plane0 & 0b11111110) * 0b00000001_00000010_00000100_00001000_00010000_00100000_01000000_10000000L)
                        & 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L))
                        | (((plane1 << 1) & 0b10) | (((plane1 & 0b11111110) * 0b0000001_00000010_00000100_00001000_00010000_00100000_01000000_100000000L)
                        & 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L))
                        | (((plane2 << 2) & 0b100) | (((plane2 & 0b11111110) * 0b000001_00000010_00000100_00001000_00010000_00100000_01000000_1000000000L)
                        & 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L))
                        | (((plane3 << 3) & 0b1000) | (((plane3 & 0b11111110) * 0b00001_00000010_00000100_00001000_00010000_00100000_01000000_10000000000L)
                        & 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L))
                        | (((plane4 << 4) & 0b10000) | (((plane4 & 0b11111110) * 0b0001_00000010_00000100_00001000_00010000_00100000_01000000_100000000000L)
                        & 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L))
                        | (((plane5 << 5) & 0b100000) | (((plane5 & 0b11111110) * 0b001_00000010_00000100_00001000_00010000_00100000_01000000_1000000000000L)
                        & 0b00100000_00100000_00100000_00100000_00100000_00100000_00100000_00100000L))
                        | (((plane6 << 6) & 0b1000000) | (((plane6 & 0b11111110) * 0b01_00000010_00000100_00001000_00010000_00100000_01000000_10000000000000L)
                        & 0b01000000_01000000_01000000_01000000_01000000_01000000_01000000_01000000L))
                        | (((plane7 << 7) & 0b10000000) | (((plane7 & 0b11111110) * 0b1_00000010_00000100_00001000_00010000_00100000_01000000_100000000000000L)
                        & 0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L));

                LONG_BE.set(pixel, pixelIndex, chunky);//array,offset,value
                pixelIndex += 8;
            }
        }
        return output;
    }


    @Override
    public AmigaBitmapImage toBitmapImage(BufferedImage input, AmigaBitmapImage output) {
        input = AmigaReuseImages.reuseInputImage(input, output);
        output = AmigaReuseImages.reuseOutputImage(input, output);
        if ((output.getColorModel() instanceof AmigaHAMColorModel)
                && input.getColorModel() instanceof AmigaHAMColorModel acm) {
            return switch (acm.getPixelSize()) {
                case 6 -> bytePixelsToPlanar6(input, output);
                case 8 -> bytePixelsToPlanar8(input, output);
                default ->
                        throw new UnsupportedOperationException("can not convert HAM image with depth=" + acm.getPixelSize());
            };
        }
        if (input.getColorModel() instanceof IndexColorModel icm) {
            return switch (output.getDepth()) {
                case 1 -> bytePixelsToPlanar1(input, output);
                case 2 -> bytePixelsToPlanar2(input, output);
                case 3 -> bytePixelsToPlanar3(input, output);
                case 4 -> bytePixelsToPlanar4(input, output);
                case 5 -> bytePixelsToPlanar5(input, output);
                case 6 -> bytePixelsToPlanar6(input, output);
                case 7 -> bytePixelsToPlanar7(input, output);
                case 8 -> bytePixelsToPlanar8(input, output);
                default -> throw new UnsupportedOperationException("can not convert image " + input);
            };
        }
        throw new UnsupportedOperationException("can not convert image=" + input);
    }

    @Override
    public BufferedImage toBufferedImage(AmigaBitmapImage input, BufferedImage output) {
        input = AmigaReuseImages.reuseInputImage(input, output);
        boolean isOutputHam = output != null && output.getColorModel() instanceof AmigaHAMColorModel;
        output = AmigaReuseImages.reuseOutputImage(input, output);
        if (!isOutputHam
                && input.getColorModel() instanceof AmigaHAMColorModel) {
            return switch (input.getDepth()) {
                case 6 -> ham6ToDirectPixels(input, output);
                case 8 -> ham8ToDirectPixels(input, output);
                default ->
                        throw new UnsupportedOperationException("can not convert HAM image with depth=" + input.getDepth());
            };
        }
        return switch (input.getDepth()) {
            case 1 -> planar1ToBytePixels(input, output);
            case 2 -> planar2ToBytePixels(input, output);
            case 3 -> planar3ToBytePixels(input, output);
            case 4 -> planar4ToBytePixels(input, output);
            case 5 -> planar5ToBytePixels(input, output);
            case 6 -> planar6ToBytePixels(input, output);
            case 7 -> planar7ToBytePixels(input, output);
            case 8 -> planar8ToBytePixels(input, output);
            default ->
                    throw new UnsupportedOperationException("can not convert indexed image with depth=" + input.getDepth());
        };
    }
}

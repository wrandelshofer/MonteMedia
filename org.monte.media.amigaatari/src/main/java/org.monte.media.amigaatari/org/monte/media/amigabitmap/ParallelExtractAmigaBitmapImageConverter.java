/*
 * @(#)ParallelExtractAmigaBitmapImageConverter.java
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
 * PDEP (Parallel Deposit/Expand) and PEXT (Parallel Extract) instructions.
 * <p>
 * This class performs well on AMD and Intel processors with BMI1 (Bit Manipulation Instructions) set,
 * but does not perform well on Apple M processors.
 */
public class ParallelExtractAmigaBitmapImageConverter implements AmigaBitmapImageConverter {
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
                bitmap[iBitmap] = (byte) Long.compress(chunky, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L);
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
                bitmap[iBitmap] = (byte) Long.compress(chunky, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L);
                bitmap[iBitmap + bitplaneStride] = (byte) Long.compress(chunky, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L);
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
                bitmap[iBitmap] = (byte) Long.compress(chunky, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L);
                bitmap[iBitmap + bitplaneStride] = (byte) Long.compress(chunky, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L);
                bitmap[iBitmap + bitplaneStride2] = (byte) Long.compress(chunky, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L);
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
                bitmap[iBitmap] = (byte) Long.compress(chunky, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L);
                bitmap[iBitmap + bitplaneStride] = (byte) Long.compress(chunky, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L);
                bitmap[iBitmap + bitplaneStride2] = (byte) Long.compress(chunky, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L);
                bitmap[iBitmap + bitplaneStride3] = (byte) Long.compress(chunky, 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L);
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
                bitmap[iBitmap] = (byte) Long.compress(chunky, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L);
                bitmap[iBitmap + bitplaneStride] = (byte) Long.compress(chunky, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L);
                bitmap[iBitmap + bitplaneStride2] = (byte) Long.compress(chunky, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L);
                bitmap[iBitmap + bitplaneStride3] = (byte) Long.compress(chunky, 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L);
                bitmap[iBitmap + bitplaneStride4] = (byte) Long.compress(chunky, 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L);
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
                bitmap[iBitmap] = (byte) Long.compress(chunky, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L);
                bitmap[iBitmap + bitplaneStride] = (byte) Long.compress(chunky, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L);
                bitmap[iBitmap + bitplaneStride2] = (byte) Long.compress(chunky, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L);
                bitmap[iBitmap + bitplaneStride3] = (byte) Long.compress(chunky, 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L);
                bitmap[iBitmap + bitplaneStride4] = (byte) Long.compress(chunky, 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L);
                bitmap[iBitmap + bitplaneStride5] = (byte) Long.compress(chunky, 0b00100000_00100000_00100000_00100000_00100000_00100000_00100000_00100000L);
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
                bitmap[iBitmap] = (byte) Long.compress(chunky, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L);
                bitmap[iBitmap + bitplaneStride] = (byte) Long.compress(chunky, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L);
                bitmap[iBitmap + bitplaneStride2] = (byte) Long.compress(chunky, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L);
                bitmap[iBitmap + bitplaneStride3] = (byte) Long.compress(chunky, 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L);
                bitmap[iBitmap + bitplaneStride4] = (byte) Long.compress(chunky, 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L);
                bitmap[iBitmap + bitplaneStride5] = (byte) Long.compress(chunky, 0b00100000_00100000_00100000_00100000_00100000_00100000_00100000_00100000L);
                bitmap[iBitmap + bitplaneStride6] = (byte) Long.compress(chunky, 0b01000000_01000000_01000000_01000000_01000000_01000000_01000000_01000000L);
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
                bitmap[iBitmap] = (byte) Long.compress(chunky, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L);
                bitmap[iBitmap + bitplaneStride] = (byte) Long.compress(chunky, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L);
                bitmap[iBitmap + bitplaneStride2] = (byte) Long.compress(chunky, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L);
                bitmap[iBitmap + bitplaneStride3] = (byte) Long.compress(chunky, 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L);
                bitmap[iBitmap + bitplaneStride4] = (byte) Long.compress(chunky, 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L);
                bitmap[iBitmap + bitplaneStride5] = (byte) Long.compress(chunky, 0b00100000_00100000_00100000_00100000_00100000_00100000_00100000_00100000L);
                bitmap[iBitmap + bitplaneStride6] = (byte) Long.compress(chunky, 0b01000000_01000000_01000000_01000000_01000000_01000000_01000000_01000000L);
                bitmap[iBitmap + bitplaneStride7] = (byte) Long.compress(chunky, 0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L);
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
        int[] rgbs = new int[1 << colorModel.getPixelSize() - 2];
        colorModel.getRGBs(rgbs);
        int pixelIndex = 0;
        for (int iScanline = 0; iScanline < bottomScanline; iScanline += scanlineStride) {
            int colorRegister = rgbs[0];
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
                    int argument = instruction & 0xf;
                    int op = instruction >> 4;
                    colorRegister = switch (op) {
                        case 0b00 -> rgbs[argument];
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
                    int argument = instruction & 0b111111;
                    int opcode = instruction >> 6;
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
                long chunky = Long.expand(plane0, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L);
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
                long chunky =
                        Long.expand(plane0, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L)
                                | Long.expand(plane1, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L);
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
                long chunky =
                        Long.expand(plane0, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L)
                                | Long.expand(plane1, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L)
                                | Long.expand(plane2, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L);
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
                long chunky =
                        Long.expand(plane0, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L)
                                | Long.expand(plane1, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L)
                                | Long.expand(plane2, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L)
                                | Long.expand(plane3, 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L);
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
                long chunky =
                        Long.expand(plane0, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L)
                                | Long.expand(plane1, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L)
                                | Long.expand(plane2, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L)
                                | Long.expand(plane3, 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L)
                                | Long.expand(plane4, 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L);
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
                long chunky =
                        Long.expand(plane0, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L)
                                | Long.expand(plane1, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L)
                                | Long.expand(plane2, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L)
                                | Long.expand(plane3, 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L)
                                | Long.expand(plane4, 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L)
                                | Long.expand(plane5, 0b00100000_00100000_00100000_00100000_00100000_00100000_00100000_00100000L);
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
                long chunky = Long.expand(plane0, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L)
                        | Long.expand(plane1, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L)
                        | Long.expand(plane2, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L)
                        | Long.expand(plane3, 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L)
                        | Long.expand(plane4, 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L)
                        | Long.expand(plane5, 0b00100000_00100000_00100000_00100000_00100000_00100000_00100000_00100000L)
                        | Long.expand(plane6, 0b01000000_01000000_01000000_01000000_01000000_01000000_01000000_01000000L);
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
                long chunky = Long.expand(plane0, 0b00000001_00000001_00000001_00000001_00000001_00000001_00000001_00000001L)
                        | Long.expand(plane1, 0b00000010_00000010_00000010_00000010_00000010_00000010_00000010_00000010L)
                        | Long.expand(plane2, 0b00000100_00000100_00000100_00000100_00000100_00000100_00000100_00000100L)
                        | Long.expand(plane3, 0b00001000_00001000_00001000_00001000_00001000_00001000_00001000_00001000L)
                        | Long.expand(plane4, 0b00010000_00010000_00010000_00010000_00010000_00010000_00010000_00010000L)
                        | Long.expand(plane5, 0b00100000_00100000_00100000_00100000_00100000_00100000_00100000_00100000L)
                        | Long.expand(plane6, 0b01000000_01000000_01000000_01000000_01000000_01000000_01000000_01000000L)
                        | Long.expand(plane7, 0b10000000_10000000_10000000_10000000_10000000_10000000_10000000_10000000L);
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
        } else if (input.getColorModel() instanceof IndexColorModel icm) {
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
        } else {
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
}

/* @(#)SEQDeltaFrame.java
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.seq;

import org.monte.media.image.BitmapImage;
import java.util.Arrays;

/**
 * Represents a delta frame in a movie track.
 * <p>
 * References:<br>
 * <a href="http://www.fileformat.info/format/atari/egff.htm">http://www.fileformat.info/format/atari/egff.htm</a><br>
 * <a href="http://www.atari-forum.com/wiki/index.php/ST_Picture_Formats">http://www.atari-forum.com/wiki/index.php/ST_Picture_Formats</a>
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.0  2010-12-25 Created.
 */
public class SEQDeltaFrame
        extends SEQFrame {

    private int leftBound, topBound, rightBound, bottomBound;
    public final static int //
            OP_Copy = 0,
            OP_XOR = 1;
    public final static int //
            SM_UNCOMPRESSED = 0,
            SM_COMPRESSED = 1;
    private final static int //
            ENCODING_COPY_UNCOMPRESSED = (OP_Copy << 1) | SM_UNCOMPRESSED,
            ENCODING_COPY_COMPRESSED = (OP_Copy << 1) | SM_COMPRESSED,
            ENCODING_XOR_UNCOMPRESSED = (OP_XOR << 1) | SM_UNCOMPRESSED,
            ENCODING_XOR_COMPRESSED = (OP_XOR << 1) | SM_COMPRESSED;
    /** Wether we already printed a warning about a broken encoding. */
    private boolean isWarningPrinted = false;

    public SEQDeltaFrame() {
    }

    private int getEncoding() {
        return (getOperation() << 1) | getStorageMethod();
    }

    @Override
    public void decode(BitmapImage bitmap, SEQMovieTrack track) {
        switch (getEncoding()) {
            case ENCODING_COPY_UNCOMPRESSED:
                decodeCopyUncompressed(bitmap, track);
                break;
            case ENCODING_COPY_COMPRESSED:
                decodeCopyCompressed(bitmap, track);
                break;
            case ENCODING_XOR_UNCOMPRESSED:
                decodeXORUncompressed(bitmap, track);
                break;
            case ENCODING_XOR_COMPRESSED:
                decodeXORCompressed(bitmap, track);
                break;
            default:
                throw new InternalError("Unsupported encoding." + getEncoding());
        }
    }

    private void decodeCopyUncompressed(BitmapImage bitmap, SEQMovieTrack track) {
    }

    /**
     * Compressed data contains a sequence of control WORDs (16-bit signed WORDs)
     * and data. A control WORD with a value between 1 and 32,767 indicates that
     * the next WORD is to be repeated a number of times equal to the control
     * WORD value. A control WORD with a negative value indicates that a run
     * of bytes equal to the absolute value of the control WORD value is to be
     * read from the compressed data.
     */
    private void decodeCopyCompressed(BitmapImage bitmap, SEQMovieTrack track) {
        int di = 0; // data index
        byte[] screen = bitmap.getBitmap();
        Arrays.fill(screen, (byte) 0);

        int bStride = bitmap.getBitplaneStride();
        int sStride = bitmap.getScanlineStride();
        int x = leftBound; // screen x
        int y = topBound; // screen y
        int shift = x & 0x7;
        int b = 0; // screen bitplane
        int si = y * sStride + x / 8; // screen index
        int width = bitmap.getWidth();

        if (shift == 0) {
            while (di < data.length) {
                int op = (((data[di++] & 0xff) << 8) | ((data[di++] & 0xff))); // opcode
                if ((op & 0x8000) == 0) {
                    // => Repeat the next data word op-times
                    byte d1 = data[di++];
                    byte d2 = data[di++];
                    for (int i = 0; i < op; i++) {
                        screen[si] = d1;
                        if (x < width - 8) {
                            screen[si + 1] = d2;
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                } else {
                    // => Copy the next abs(op) words
                    op = op ^ 0x8000;
                    for (int i = 0; i < op; i++) {
                        byte d1 = data[di++];
                        byte d2 = data[di++];
                        screen[si] = d1;
                        if (x < width - 8) {
                            screen[si + 1] = d2;
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                }
            }
        } else {
            int invShift = 8 - shift;
            int mask = (0xff << shift) & 0xff;
            int invMask = (0xff << invShift) & 0xff;
            int xorInvMask = 0xff >>> shift;
            while (di < data.length) {
                int op = (((data[di++] & 0xff) << 8) | ((data[di++] & 0xff))); // opcode
                if ((op & 0x8000) == 0) {
                    // => Repeat the next data word op-times
                    byte d1 = data[di++];
                    byte d2 = data[di++];
                    byte d3 = (byte) (d2 << invShift);
                    d2 = (byte) (((d1 << invShift) & invMask) | ((d2 & 0xff) >>> shift));
                    d1 = (byte) ((d1 & 0xff) >>> shift);
                    for (int i = 0; i < op; i++) {
                        screen[si] = (byte) ((screen[si] & invMask) | d1);
                        if (x < width - 8) {
                            screen[si + 1] = d2;
                            if (x < width - 16) {
                                screen[si + 2] = (byte) ((screen[si + 2] & xorInvMask) | d3);
                            }
                        }
                        //screen[si + 2] = (byte) (d3);
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                } else {
                    // => Copy the next abs(op) words
                    op = op ^ 0x8000;
                    for (int i = 0; i < op; i++) {
                        byte d1 = data[di++];
                        byte d2 = data[di++];
                        byte d3 = (byte) (d2 << invShift);
                        d2 = (byte) (((d1 << invShift) & invMask) | ((d2 & 0xff) >>> shift));
                        d1 = (byte) ((d1 & 0xff) >>> shift);
                        screen[si] = (byte) ((screen[si] & invMask) | d1);
                        if (x < width - 8) {
                            screen[si + 1] = d2;
                            if (x < width - 16) {
                                screen[si + 2] = (byte) ((screen[si + 2] & xorInvMask) | d3);
                            }
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                }
            }
        }
    }

    private void decodeXORUncompressed(BitmapImage bitmap, SEQMovieTrack track) {
    }

    private void decodeXORCompressed(BitmapImage bitmap, SEQMovieTrack track) {
        int di = 0; // data index
        byte[] screen = bitmap.getBitmap();
        int bStride = bitmap.getBitplaneStride();
        int sStride = bitmap.getScanlineStride();
        int x = leftBound; // screen x
        int y = topBound; // screen y
        int shift = x & 0x7;
        int b = 0; // screen bitplane
        int si = y * sStride + x / 8; // screen index
        int width = bitmap.getWidth();

        if (shift == 0) {
            while (di < data.length) {
                int op = (((data[di++] & 0xff) << 8) | ((data[di++] & 0xff))); // opcode
                if ((op & 0x8000) == 0) {
                    // => Repeat the next data word op-times
                    byte d1 = data[di++];
                    byte d2 = data[di++];
                    for (int i = 0; i < op; i++) {
                        screen[si] ^= d1;
                        if (x < width - 8) {
                            screen[si + 1] ^= d2;
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                } else {
                    // => Copy the next abs(op) words
                    op = op ^ 0x8000;
                    for (int i = 0; i < op; i++) {
                        byte d1 = data[di++];
                        byte d2 = data[di++];
                        screen[si] ^= d1;
                        if (x < width - 8) {
                            screen[si + 1] ^= d2;
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                }
            }
        } else {
            int invShift = 8 - shift;
            int mask = (0xff << shift) & 0xff;
            int xorMask = 0xff ^ mask;
            int invMask = (0xff << invShift) & 0xff;
            int xorInvMask = 0xff >>> shift;
            while (di < data.length) {
                int op = (((data[di++] & 0xff) << 8) | ((data[di++] & 0xff))); // opcode
                if ((op & 0x8000) == 0) {
                    // => Repeat the next data word op-times
                    byte d1 = data[di++];
                    byte d2 = data[di++];
                    byte d3 = (byte) (d2 << invShift);
                    d2 = (byte) (((d1 << invShift) & invMask) | ((d2 & 0xff) >>> shift));
                    d1 = (byte) ((d1 & 0xff) >>> shift);
                    for (int i = 0; i < op; i++) {
                        screen[si] = (byte) ((screen[si] & invMask) | ((screen[si] & xorInvMask) ^ d1));
                        if (x < width - 8) {
                            screen[si + 1] ^= d2;
                            if (x < width - 16) {
                                screen[si + 2] = (byte) ((screen[si + 2] & xorInvMask) | ((screen[si + 2] & invMask) ^ d3));
                            }
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                } else {
                    // => Copy the next abs(op) words
                    op = op ^ 0x8000;
                    for (int i = 0; i < op; i++) {
                        byte d1 = data[di++];
                        byte d2 = data[di++];
                        byte d3 = (byte) (d2 << invShift);
                        d2 = (byte) (((d1 << invShift) & invMask) | ((d2 & 0xff) >>> shift));
                        d1 = (byte) ((d1 & 0xff) >>> shift);
                        //screen[si] = (byte) ((screen[si] & mask) | ((screen[si]&xorMask)^d1));
                        screen[si] = (byte) ((screen[si] & invMask) | ((screen[si] & xorInvMask) ^ d1));
                        if (x < width - 8) {
                            screen[si + 1] ^= d2;
                            if (x < width - 16) {
                                screen[si + 2] = (byte) ((screen[si + 2] & xorInvMask) | ((screen[si + 2] & invMask) ^ d3));
                            }
                        }
                        y++;
                        si += sStride;
                        if (y >= bottomBound) {
                            y = topBound;
                            x = x + 16;
                            if (x >= rightBound) {
                                x = leftBound;
                                y = topBound;
                                b = b + 1;
                            }
                            si = b * bStride + y * sStride + x / 8;
                        }
                    }
                }
            }
        }
    }

    public void setBounds(int x, int y, int w, int h) {
        leftBound = x;
        topBound = y;
        rightBound = x + w;
        bottomBound = y + h;
    }

    @Override
    public int getTopBound(SEQMovieTrack track) {
        return topBound;
    }

    @Override
    public int getBottomBound(SEQMovieTrack track) {
        return bottomBound;
    }

    @Override
    public int getLeftBound(SEQMovieTrack track) {
        return leftBound;
    }

    @Override
    public int getRightBound(SEQMovieTrack track) {
        return rightBound;
    }

    /** Returns true if the frame can be decoded over both the previous frame
     * or the subsequent frame. Bidirectional frames can be used efficiently
     * for forward and backward playing a movie.
     * <p>
     * All key frames are bidirectional. Delta frames which use an XOR OP-mode
     * are bidirectional as well.
     */
    @Override
    public boolean isBidirectional() {
        return getOperation() == OP_XOR;
    }
}

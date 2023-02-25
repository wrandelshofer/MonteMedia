/* @(#)ANIMDeltaFrame.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.anim;

import org.monte.media.amigabitmap.AmigaBitmapImage;

/**
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version $Id$
 */
public class ANIMDeltaFrame
        extends ANIMFrame {

    private int leftBound, topBound, rightBound, bottomBound;
    private final static int //
            ENCODING_ILBM = 0,
            ENCODING_XOR_ILBM = 1,
            ENCODING_LONG_DELTA = 2,
            ENCODING_SHORT_DELTA = 3,
            ENCODING_GENERALIZED_SHORTLONG_DELTA = 4,
            ENCODING_BYTE_VERTICAL = 5,
            ENCODING_VERTICAL_7_SHORT = 6,
            ENCODING_VERTICAL_7_LONG = 7,
            ENCODING_VERTICAL_8_SHORT = 8,
            ENCODING_VERTICAL_8_LONG = 9,
            ENCODING_J = 74;
    public final static int //
            OP_Direct = 0,
            OP_XOR = 1,
            OP_LongDelta = 2,
            OP_ShortDelta = 3,
            OP_GeneralDelta = 4,
            OP_ByteVertical = 5,
            OP_StereoDelta = 6,
            OP_Vertical7 = 7,
            OP_Vertical8 = 8,
            OP_J = 74;
    /**
     * Wether we already printed a warning about a broken encoding.
     */
    private boolean isWarningPrinted = false;

    public ANIMDeltaFrame() {
    }

    private int getEncoding() {
        switch (getOperation()) {
            case OP_Direct: // Key Frame (Data stored in ILBM BODY Chunk)
                throw new InternalError("Key Frames not yet supported (Anim Op0)");
            case OP_ByteVertical:
                if (getBits() == BIT_XOR) {
                    // okay
                } else if ((getBits() & BadBitsOP_ByteVertical) != 0) {
                    throw new InternalError("Unknown Bits for Anim Op5 in ANHD; Bits:" + getBits());
                }
                return ENCODING_BYTE_VERTICAL;
            case OP_Vertical7:
                if ((getBits() & BIT_LongData) == 0) {
                    return ENCODING_VERTICAL_7_SHORT;
                } else {
                    return ENCODING_VERTICAL_7_LONG;
                }
            case OP_Vertical8:
                if ((getBits() & BIT_LongData) == 0) {
                    return ENCODING_VERTICAL_8_SHORT;
                } else {
                    return ENCODING_VERTICAL_8_LONG;
                }
            case OP_J:
                return ENCODING_J;
            default:
                throw new InternalError("ANIM Op" + getOperation() + " not supported.");
        }
    }

    @Override
    public void decode(AmigaBitmapImage bitmap, ANIMMovieResources track) {
        switch (getEncoding()) {
            case ENCODING_BYTE_VERTICAL:
                decodeByteVertical(bitmap, track);
                break;
            case ENCODING_VERTICAL_7_SHORT:
                decodeVertical7Short(bitmap, track);
                break;
            case ENCODING_VERTICAL_7_LONG:
                decodeVertical7Long(bitmap, track);
                break;
            case ENCODING_VERTICAL_8_SHORT:
                decodeVertical8Short(bitmap, track);
                break;
            case ENCODING_VERTICAL_8_LONG:
                decodeVertical8Long(bitmap, track);
                break;
            case ENCODING_J:
                decodeJ(bitmap, track);
                break;
            default:
                throw new InternalError("Unsupported encoding." + getEncoding());
        }
    }

    /**
     * 2.2.1 Format for methods 2 & 3.
     * <p>
     * This chunk is a basic data chunk used to hold the delta
     * compression data.  The minimum size of this chunk is 32 bytes
     * as the first 8 long-words are byte pointers into the chunk for
     * the data for each of up to 8 bitplanes.  The pointer for the
     * plane data starting immediately following these 8 pointers will
     * have a value of 32 as the data starts in the 33-rd byte of the
     * chunk (index value of 32 due to zero-base indexing).
     * <p>
     * The data for a given plane consists of groups of data words.  In
     * Long Delta mode, these groups consist of both short and long
     * words - short words for offsets and numbers, and long words for
     * the actual data.  In Short Delta mode, the groups are identical
     * except data words are also shorts so all data is short words.
     * Each group consists of a starting word which is an offset.  If
     * the offset is positive then it indicates the increment in long
     * or short words (whichever is appropriate) through the bitplane.
     * In other words, if you were reconstructing the plane, you would
     * start a pointer (to shorts or longs depending on the mode) to
     * point to the first word of the bitplane.  Then the offset would
     * be added to it and the following data word would be placed at
     * that position.  Then the next offset would be added to the
     * pointer and the following data word would be placed at that
     * position.  And so on...  The data terminates with an offset
     * equal to 0xFFFF.
     * <p>
     * A second interpretation is given if the offset is negative.  In
     * that case, the absolute value is the offset+2.  Then the
     * following short-word indicates the number of data words that
     * follow.  Following that is the indicated number of contiguous
     * data words (longs or shorts depending on mode) which are to
     * be placed in contiguous locations of the bitplane.
     * <p>
     * If there are no changed words in a given plane, then the pointer
     * in the first 32 bytes of the chunk is =0.
     */
    private void decodeMethod2(AmigaBitmapImage bitmap, ANIMMovieResources track) {
        throw new UnsupportedOperationException();

    }

    /**
     * 2.2.2 Format for method 4.
     * <p>
     * The DLTA chunk is modified slightly to have 16 long pointers at
     * the start.  The first 8 are as before - pointers to the start of
     * the data for each of the bitplanes (up to a theoretical max of 8
     * planes).  The next 8 are pointers to the start of the offset/numbers
     * data list.  If there is only one list of offset/numbers for all
     * planes, then the pointer to that list is repeated in all positions
     * so the playback code need not even be aware of it.  In fact, one
     * could get fancy and have some bitplanes share lists while others
     * have different lists, or no lists (the problems in these schemes
     * lie in the generation, not in the playback).
     * <p>
     * The best way to show the use of this format is in a sample playback
     * routine.
     * <pre>
     *           SetDLTAshort(bm,deltaword)
     *           struct BitMap *bm;
     *           WORD *deltaword;
     *           {
     *              int i;
     *              LONG *deltadata;
     *              WORD *ptr,*planeptr;
     *              register int s,size,nw;
     *              register WORD *data,*dest;
     *
     *              deltadata = (LONG *)deltaword;
     *              nw = bm->BytesPerRow >>1;
     *
     *              for (i=0;i&lt;bm->Depth;i++) {
     *                 planeptr = (WORD *)(bm->Planes[i]);
     *                 data = deltaword + deltadata[i];
     *                 ptr  = deltaword + deltadata[i+8];
     *                 while (*ptr != 0xFFFF) {
     *                    dest = planeptr + *ptr++;
     *                    size = *ptr++;
     *                    if (size &lt; 0) {
     *                       for (s=size;s&lt;0;s++) {
     *                          *dest = *data;
     *                          dest += nw;
     *                       }
     *                       data++;
     *                    }
     *                    else {
     *                       for (s=0;s&lt;size;s++) {
     *                          *dest = *data++;
     *                          dest += nw;
     *                       }
     *                    }
     *                 }
     *              }
     *              return(0);
     *           }
     * </pre>
     * The above routine is for short word vertical compression with
     * run length compression.  The most efficient way to support
     * the various options is to replicate this routine and make
     * alterations for, say, long word or XOR.  The variable nw
     * indicates the number of words to skip to go down the vertical
     * column.  This one routine could easily handle horizontal
     * compression by simply setting nw=1.  For ultimate playback
     * speed, the core, at least, of this routine should be coded in
     * assembly language.
     */
    private void decodeMethod4(AmigaBitmapImage bitmap, ANIMMovieResources track) {
        throw new UnsupportedOperationException();

    }

    /**
     * 2.2.2 Format for method 5.
     * <p>
     * In this method the same 16 pointers are used as in option 4.
     * The first 8 are pointers to the data for up to 8 planes.
     * The second set of 8 are not used but were retained for several
     * reasons.  First to be somewhat compatible with code for option
     * 4 (although this has not proven to be of any benefit) and
     * second, to allow extending the format for more bitplanes (code
     * has been written for up to 12 planes).
     * <p>
     * Compression/decompression is performed on a plane-by-plane basis.
     * For each plane, compression can be handled by the skip.c code
     * (provided Public Domain by Jim Kent) and decompression can be
     * handled by unvscomp.asm (also provided Public Domain by Jim Kent).
     * <p>
     * Compression/decompression is performed on a plane-by-plane basis.
     * The following description of the method is taken directly from
     * Jim Kent's code with minor re-wording.  Please refer to Jim's
     * code (skip.c and unvscomp.asm) for more details:
     * <p>
     * Each column of the bitplane is compressed separately.
     * A 320x200 bitplane would have 40 columns of 200 bytes each.
     * Each column starts with an op-count followed by a number
     * of ops.  If the op-count is zero, that's ok, it just means
     * there's no change in this column from the last frame.
     * The ops are of three classes, and followed by a varying
     * amount of data depending on which class:
     * 1. Skip ops - this is a byte with the hi bit clear that
     * says how many rows to move the "dest" pointer forward,
     * ie to skip. It is non-zero.
     * 2. Uniq ops - this is a byte with the hi bit set.  The hi
     * bit is masked down and the remainder is a count of the
     * number of bytes of data to copy literally.  It's of
     * course followed by the data to copy.
     * 3. Same ops - this is a 0 byte followed by a count byte,
     * followed by a byte value to repeat count times.
     * Do bear in mind that the data is compressed vertically rather
     * than horizontally, so to get to the next byte in the destination
     * we add the number of bytes per row instead of one!
     */
    private void decodeByteVertical(AmigaBitmapImage bitmap, ANIMMovieResources track) {
        int columns = 0;
        int iOp = 0;
        byte[] planeBytes = bitmap.getBitmap();
        int iPl = 0;
        int widthInBytes = bitmap.getBitplaneStride();
        int interleave = track.getNbPlanes() * widthInBytes;
        int opCode = 0;
        int opCount = 0;
        byte copyByte = 0;
        leftBound = widthInBytes;
        rightBound = 0;
        topBound = track.getHeight();
        bottomBound = 0;
        int height = track.getHeight();
        boolean isXOR = getBits() == BIT_XOR;

        // Repeat for each plane.
        for (int i = 0, n = track.getNbPlanes(); i < n; ++i) {

            // iOp is the pointer (index) to the op-codes.
            iOp = ((data[i * 4] & 0xff) << 24)
                    + ((data[i * 4 + 1] & 0xff) << 16)
                    + ((data[i * 4 + 2] & 0xff) << 8)
                    + (data[i * 4 + 3] & 0xff);
            try {
                if (iOp > 0) {
                    // Each column of the plane is coded on its own.
                    for (columns = 0; columns < widthInBytes; ++columns) {

                        // Set iPl to the beginning of the column in the plane.
                        iPl = columns + i * widthInBytes;
                        opCount = data[iOp++] & 0xff;

                        if (opCount > 0) {
                            if (columns < leftBound) {
                                leftBound = columns;
                            }
                            if (columns > rightBound) {
                                rightBound = columns;
                            }
                            opCode = data[iOp];
                            if (opCode <= 0) {
                                topBound = 0;
                            } else {
                                if (opCode < topBound) {
                                    topBound = opCode;
                                }
                            }

                            if (isXOR) {
                                for (; opCount > 0; opCount--) {
                                    opCode = data[iOp++];
                                    if (opCode > 0) { // Skip ops
                                        iPl += opCode * interleave;
                                    } else if (opCode < 0) { // Uniq ops
                                        opCode &= 0x7f;
                                        while (opCode-- > 0) {
                                            planeBytes[iPl] ^= data[iOp++];
                                            iPl += interleave;
                                        }
                                    } else { // Repeat ops
                                        opCode = data[iOp++] & 0xff;
                                        if (opCode == 0) {
                                            return;
                                        } //throw new InterpretException("Error in Delta Chunk: copy bytes with count 0.");
                                        copyByte = data[iOp++];
                                        while (opCode-- > 0) {
                                            planeBytes[iPl] ^= copyByte;
                                            iPl += interleave;
                                        }
                                    }
                                }
                            } else {
                                for (; opCount > 0; opCount--) {
                                    opCode = data[iOp++];
                                    if (opCode > 0) { // Skip ops
                                        iPl += opCode * interleave;
                                    } else if (opCode < 0) { // Uniq ops
                                        opCode &= 0x7f;
                                        while (opCode-- > 0) {
                                            planeBytes[iPl] = data[iOp++];
                                            iPl += interleave;
                                        }
                                    } else { // Repeat ops
                                        opCode = data[iOp++] & 0xff;
                                        if (opCode == 0) {
                                            return;
                                        } //throw new InterpretException("Error in Delta Chunk: copy bytes with count 0.");
                                        copyByte = data[iOp++];
                                        while (opCode-- > 0) {
                                            planeBytes[iPl] = copyByte;
                                            iPl += interleave;
                                        }
                                    }
                                }
                            }

                            if (opCode <= 0) {
                                int bottom = (iPl - (columns + i * widthInBytes)) / interleave;
                                if (bottom > bottomBound) {
                                    bottomBound = bottom;
                                }
                            } else {
                                if (height - opCode > bottomBound) {
                                    bottomBound = height - opCode;
                                }
                            }
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        if (leftBound <= rightBound) {
            leftBound *= 8;
            rightBound = rightBound * 8 + 8;
        }
    }

    private void decodeVertical8Short(AmigaBitmapImage bitmap, ANIMMovieResources track) {
        int columns = 0;
        int iOp = 0;
        byte[] planeBytes = bitmap.getBitmap();
        int iPl = 0;
        int widthInBytes = bitmap.getBitplaneStride();
        int interleave = track.getNbPlanes() * widthInBytes;
        int opCode = 0;
        int opCount = 0;
        byte copyByte1 = 0;
        byte copyByte2 = 0;
        leftBound = widthInBytes;
        rightBound = 0;
        topBound = track.getHeight() - 1;
        bottomBound = 0;
        int height = track.getHeight() - 1;

        // Repeat for each plane.
        for (int i = 0; i < track.getNbPlanes(); i++) {

            // iOp points to the Op-Codes.
            iOp = ((data[i * 4] & 0xff) << 24)
                    + ((data[i * 4 + 1] & 0xff) << 16)
                    + ((data[i * 4 + 2] & 0xff) << 8)
                    + (data[i * 4 + 3] & 0xff);

            if (iOp > 0) {
                // Each column has its own Op-codes.
                for (columns = 0; columns < widthInBytes; columns += 2) {

                    // iPl points to the column in the bitmap.
                    iPl = columns + i * widthInBytes;
                    opCount = ((data[iOp++] & 0xff) << 8) | (data[iOp++] & 0xff);

                    if (opCount > 0) {
                        if (columns < leftBound) {
                            leftBound = columns;
                        }
                        if (columns > rightBound) {
                            rightBound = columns;
                        }
                        opCode = (data[iOp] << 8) | (data[iOp + 1] & 0xff);
                        if (opCode <= 0) {
                            topBound = 0;
                        } else {
                            if (opCode < topBound) {
                                topBound = opCode;
                            }
                        }

                        for (; opCount > 0; opCount--) {
                            opCode = (data[iOp++] << 8) | (data[iOp++] & 0xff);
                            if (opCode > 0) { // Skip ops
                                iPl += opCode * interleave;
                            } else if (opCode < 0) { // Uniq ops
                                opCode &= 0x7fff;
                                while (opCode-- > 0) {
                                    planeBytes[iPl] = data[iOp++];
                                    planeBytes[iPl + 1] = data[iOp++];
                                    iPl += interleave;
                                }
                            } else { // Repeat ops
                                opCode = ((data[iOp++] << 8) | (data[iOp++] & 0xff)) & 0xffff;
                                if (opCode == 0) {
                                    return;
                                } //throw new InterpretException("Error in Delta Chunk: copy bytes with count 0.");
                                copyByte1 = data[iOp++];
                                copyByte2 = data[iOp++];
                                while (opCode-- > 0) {
                                    planeBytes[iPl] = copyByte1;
                                    planeBytes[iPl + 1] = copyByte2;
                                    iPl += interleave;
                                }
                            }
                        }

                        if (opCode <= 0) {
                            int bottom = (iPl - (columns + i * widthInBytes)) / interleave;
                            if (bottom > bottomBound) {
                                bottomBound = bottom;
                            }
                        } else {
                            if (height - opCode > bottomBound) {
                                bottomBound = height - opCode;
                            }
                        }
                    }
                }
            }
        }
        if (leftBound <= rightBound) {
            leftBound *= 8;
            rightBound = rightBound * 8 + 16;
        }
    }

    private void decodeVertical8Long(AmigaBitmapImage bitmap, ANIMMovieResources track) {
        int columns = 0;
        int iOp = 0;
        byte[] planeBytes = bitmap.getBitmap();
        int iPl = 0;
        int widthInBytes = bitmap.getBitplaneStride();
        int interleave = track.getNbPlanes() * widthInBytes;
        int opCode = 0;
        int opCount = 0;
        byte copyByte1 = 0;
        byte copyByte2 = 0;
        byte copyByte3 = 0;
        byte copyByte4 = 0;
        leftBound = widthInBytes;
        rightBound = 0;
        topBound = track.getHeight() - 1;
        bottomBound = 0;
        int height = track.getHeight() - 1;

        // Repeat for each plane
        for (int i = 0; i < track.getNbPlanes(); i++) {

            // iOp points to the op-codes.
            iOp = ((data[i * 4] & 0xff) << 24)
                    + ((data[i * 4 + 1] & 0xff) << 16)
                    + ((data[i * 4 + 2] & 0xff) << 8)
                    + (data[i * 4 + 3] & 0xff);

            if (iOp > 0) {
                // Decode each column of the plane separately.
                for (columns = 0; columns < widthInBytes; columns += 4) {

                    // iPl points to the column in the bitmap.
                    iPl = columns + i * widthInBytes;
                    opCount = ((data[iOp++] & 0xff) << 24)
                            + ((data[iOp++] & 0xff) << 16)
                            + ((data[iOp++] & 0xff) << 8)
                            + (data[iOp++] & 0xff);

                    if (opCount > 0) {
                        if (columns < leftBound) {
                            leftBound = columns;
                        }
                        if (columns > rightBound) {
                            rightBound = columns;
                        }
                        opCode = ((data[iOp] & 0xff) << 24)
                                + ((data[iOp + 1] & 0xff) << 16)
                                + ((data[iOp + 2] & 0xff) << 8)
                                + (data[iOp + 3] & 0xff);
                        if (opCode <= 0) {
                            topBound = 0;
                        } else {
                            if (opCode < topBound) {
                                topBound = opCode;
                            }
                        }

                        for (; opCount > 0; opCount--) {
                            opCode = ((data[iOp++] & 0xff) << 24)
                                    + ((data[iOp++] & 0xff) << 16)
                                    + ((data[iOp++] & 0xff) << 8)
                                    + (data[iOp++] & 0xff);
                            if (opCode > 0) { // Skip ops
                                iPl += opCode * interleave;
                            } else if (opCode < 0) { // Uniq ops
                                opCode &= 0x7fffffff;
                                while (opCode-- > 0) {
                                    planeBytes[iPl] = data[iOp++];
                                    planeBytes[iPl + 1] = data[iOp++];
                                    planeBytes[iPl + 2] = data[iOp++];
                                    planeBytes[iPl + 3] = data[iOp++];
                                    iPl += interleave;
                                }
                            } else { // Repeat ops
                                opCode = ((data[iOp++] & 0xff) << 24)
                                        + ((data[iOp++] & 0xff) << 16)
                                        + ((data[iOp++] & 0xff) << 8)
                                        + (data[iOp++] & 0xff);
                                if (opCode == 0) {
                                    return;
                                } //throw new InterpretException("Error in Delta Chunk: copy bytes with count 0.");
                                copyByte1 = data[iOp++];
                                copyByte2 = data[iOp++];
                                copyByte3 = data[iOp++];
                                copyByte4 = data[iOp++];
                                while (opCode-- > 0) {
                                    planeBytes[iPl] = copyByte1;
                                    planeBytes[iPl + 1] = copyByte2;
                                    planeBytes[iPl + 2] = copyByte3;
                                    planeBytes[iPl + 3] = copyByte4;
                                    iPl += interleave;
                                }
                            }
                        }

                        if (opCode <= 0) {
                            int bottom = (iPl - (columns + i * widthInBytes)) / interleave;
                            if (bottom > bottomBound) {
                                bottomBound = bottom;
                            }
                        } else {
                            if (height - opCode > bottomBound) {
                                bottomBound = height - opCode;
                            }
                        }
                    }
                }
            }
        }
        if (leftBound <= rightBound) {
            leftBound *= 8;
            rightBound = rightBound * 8 + 32;
        }
    }

    private void decodeVertical7Short(AmigaBitmapImage bitmap, ANIMMovieResources track) {
        int columns = 0;
        int iOp = 0;
        int iData = 0;
        byte[] planeBytes = bitmap.getBitmap();
        int iPl = 0;
        int widthInBytes = bitmap.getBitplaneStride();
        int interleave = bitmap.getScanlineStride();
        int opCode = 0;
        int opCount = 0;
        byte copyByte1 = 0;
        byte copyByte2 = 0;
        leftBound = widthInBytes;
        rightBound = 0;
        topBound = track.getHeight() - 1;
        bottomBound = 0;
        int height = track.getHeight() - 1;

        for (int i = 0; i < track.getNbPlanes(); i++) {
            iOp = ((data[i * 4] & 0xff) << 24)
                    + ((data[i * 4 + 1] & 0xff) << 16)
                    + ((data[i * 4 + 2] & 0xff) << 8)
                    + (data[i * 4 + 3] & 0xff);

            iData = ((data[i * 4 + 32] & 0xff) << 24)
                    + ((data[i * 4 + 33] & 0xff) << 16)
                    + ((data[i * 4 + 34] & 0xff) << 8)
                    + (data[i * 4 + 35] & 0xff);

            if (iOp > 0) {
                for (columns = 0; columns < widthInBytes; columns += 2) {
                    iPl = columns + i * widthInBytes;
                    opCount = data[iOp++] & 0xff;

                    if (opCount > 0) {
                        if (columns < leftBound) {
                            leftBound = columns;
                        }
                        if (columns > rightBound) {
                            rightBound = columns;
                        }
                        opCode = data[iOp];
                        if (opCode <= 0) {
                            topBound = 0;
                        } else {
                            if (opCode < topBound) {
                                topBound = opCode;
                            }
                        }

                        for (; opCount > 0; opCount--) {
                            opCode = data[iOp++];
                            if (opCode > 0) { // Skip ops
                                iPl += opCode * interleave;
                            } else if (opCode < 0) { // Uniq ops
                                opCode &= 0x7f;
                                while (opCode-- > 0) {
                                    planeBytes[iPl] = data[iData++];
                                    planeBytes[iPl + 1] = data[iData++];
                                    iPl += interleave;
                                }
                            } else { // Repeat ops
                                opCode = data[iOp++] & 0xff;
                                if (opCode == 0) {
                                    return;
                                } //throw new InterpretException("Error in Delta Chunk: copy bytes with count 0.");
                                copyByte1 = data[iData++];
                                copyByte2 = data[iData++];
                                while (opCode-- > 0) {
                                    planeBytes[iPl] = copyByte1;
                                    planeBytes[iPl + 1] = copyByte2;
                                    iPl += interleave;
                                }
                            }
                        }

                        if (opCode <= 0) {
                            int bottom = (iPl - (columns + i * widthInBytes)) / interleave;
                            if (bottom > bottomBound) {
                                bottomBound = bottom;
                            }
                        } else {
                            if (height - opCode > bottomBound) {
                                bottomBound = height - opCode;
                            }
                        }
                    }
                }
            }
        }
        if (leftBound <= rightBound) {
            leftBound *= 8;
            rightBound = rightBound * 8 + 32;
        }
    }

    private void decodeVertical7Long(AmigaBitmapImage bitmap, ANIMMovieResources track) {
        int columns = 0;
        int iOp = 0;
        int iData = 0;
        byte[] planeBytes = bitmap.getBitmap();
        int iPl = 0;
        int widthInBytes = bitmap.getBitplaneStride();
        int interleave = track.getNbPlanes() * widthInBytes;
        int opCode = 0;
        int opCount = 0;
        byte copyByte1 = 0;
        byte copyByte2 = 0;
        byte copyByte3 = 0;
        byte copyByte4 = 0;
        leftBound = widthInBytes;
        rightBound = 0;
        topBound = track.getHeight() - 1;
        bottomBound = 0;
        int height = track.getHeight() - 1;

        for (int i = 0; i < track.getNbPlanes(); i++) {
            iOp = ((data[i * 4] & 0xff) << 24)
                    + ((data[i * 4 + 1] & 0xff) << 16)
                    + ((data[i * 4 + 2] & 0xff) << 8)
                    + (data[i * 4 + 3] & 0xff);

            iData = ((data[i * 4 + 32] & 0xff) << 24)
                    + ((data[i * 4 + 33] & 0xff) << 16)
                    + ((data[i * 4 + 34] & 0xff) << 8)
                    + (data[i * 4 + 35] & 0xff);

            if (iOp > 0) {
                for (columns = 0; columns < widthInBytes; columns += 4) {
                    try {
                        iPl = columns + i * widthInBytes;
                        opCount = data[iOp++] & 0xff;

                        if (opCount > 0) {
                            if (columns < leftBound) {
                                leftBound = columns;
                            }
                            if (columns > rightBound) {
                                rightBound = columns;
                            }
                            opCode = data[iOp];
                            if (opCode <= 0) {
                                topBound = 0;
                            } else {
                                if (opCode < topBound) {
                                    topBound = opCode;
                                }
                            }

                            for (; opCount > 0; opCount--) {
                                opCode = data[iOp++];
                                if (opCode > 0) { // Skip ops
                                    iPl += opCode * interleave;
                                } else if (opCode < 0) { // Uniq ops
                                    opCode &= 0x7f;
                                    while (opCode-- > 0) {
                                        planeBytes[iPl] = data[iData++];
                                        planeBytes[iPl + 1] = data[iData++];
                                        planeBytes[iPl + 2] = data[iData++];
                                        planeBytes[iPl + 3] = data[iData++];
                                        iPl += interleave;
                                    }
                                } else { // Repeat ops
                                    opCode = data[iOp++] & 0xff;
                                    if (opCode == 0) {
                                        return;
                                    } //throw new InterpretException("Error in Delta Chunk: copy bytes with count 0.");
                                    copyByte1 = data[iData++];
                                    copyByte2 = data[iData++];
                                    copyByte3 = data[iData++];
                                    copyByte4 = data[iData++];
                                    while (opCode-- > 0) {
                                        planeBytes[iPl] = copyByte1;
                                        planeBytes[iPl + 1] = copyByte2;
                                        planeBytes[iPl + 2] = copyByte3;
                                        planeBytes[iPl + 3] = copyByte4;
                                        iPl += interleave;
                                    }
                                }
                            }

                            if (opCode <= 0) {
                                int bottom = (iPl - (columns + i * widthInBytes)) / interleave;
                                if (bottom > bottomBound) {
                                    bottomBound = bottom;
                                }
                            } else {
                                if (height - opCode > bottomBound) {
                                    bottomBound = height - opCode;
                                }
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {
                        // Some delta frames write over the bounds
                        // of the bitmap.
                        if (!isWarningPrinted) {
                            e.printStackTrace();
                            isWarningPrinted = true;
                        }
                    }
                }
            }
        }
        if (leftBound <= rightBound) {
            leftBound *= 8;
            rightBound = rightBound * 8 + 64;
        }
    }

    /**
     * Decodes DLTA's in Eric Graham's Compresson mode "J".
     * <p>
     * The following documentation has been taken from Steven Den Beste's docu
     * for his "unmovie" program.
     * <p>
     * A DLTA appears to have three kinds of items in it, with each type being
     * indicated by the value of its first byte:
     * <p>
     * <p>
     * Type 0: indicates the end of the DLTA. Layout: word: 0
     * <p>
     * Type 1: indicates a "wall": This is a section of the image which has full
     * Z-height, is 1 byte wide in X, and has a variable Y size. Layout: word: 1
     * word: 0=unidirectional (store value), 1=bidirectional (XOR value) word:
     * Y-size (number of pixels in Y direction) word: number of blocks to
     * follow: per block: word: offset in each bitplane (note: NOT in the total
     * image!) 1-6 bytes: full Z height for first Y 1-6 bytes: full Z height for
     * second Y etc., extending DOWN.
     * <p>
     * Type 2: indicates a "pile": This is a section of the image which has full
     * Z-height, and has both variable Y size and X size. Layout: word: 2 word:
     * 0=unidirectional, 1=bidirectional word: Y size word: X size word: number
     * of blocks to follow: per block: word: offset in each bitplane (NOT in the
     * total image) successive bytes: a traversed 3D rectangle, with X varying
     * within Y within Z. (X moves right, Z moves up, Y moves down)
     * <p>
     * The movie is double-buffered, but you don't have to know about that part.
     * (Anyway, it is described in the original documentation for "pilbm" if
     * you're curious.
     */
    private void decodeJ(AmigaBitmapImage bitmap, ANIMMovieResources track) {

        int nbPlanes = track.getNbPlanes();
        int widthInBytes = bitmap.getBitplaneStride();

        // Mark all pixels of the delta frame as being changed
        // XXX - Determine minimal bounds
        /*
         rightBound = track.getWidth();
         leftBound = 0;
         bottomBound = track.getHeight();
         topBound = 0;*/
        leftBound = track.getWidth() - 1;
        rightBound = 0;
        topBound = track.getHeight() - 1;
        bottomBound = 0;

        // Current reading position;
        int pos = 0;

        // Output data goes here:
        byte[] planeBytes = bitmap.getBitmap();

        // Change type, 16 bit short: 0=End of Delta, 1=Wall, 2=Pile.
        int changeType;
        try {
            decodingLoop:
            while (pos < data.length) {
                changeType = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);

                switch (changeType) {
                    case 0: /* End of DELTA */

                        break decodingLoop;

                    case 1: { /* Wall */

                        // Read wall header
                        // struct {
                        //        short uni_flag;
                        //      short y_size;
                        //      short num_blocks; } wall;
                        int uniFlag = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);
                        int ySize = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);
                        int numBlocks = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);

                        // Decode wall data
                        for (int b = 0; b < numBlocks; b++) {
                            int offset = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);

                            leftBound = Math.min(leftBound, (offset % widthInBytes) * 8);
                            rightBound = Math.max(rightBound, (offset % widthInBytes) * 8 + 8);
                            topBound = Math.min(topBound, (offset / widthInBytes));
                            bottomBound = Math.max(bottomBound, (offset / widthInBytes) + ySize);

                            int realOffset = (offset / widthInBytes) * nbPlanes;
                            realOffset *= widthInBytes;
                            realOffset += offset % widthInBytes;

                            if (uniFlag == 1) {
                                for (int z = 0; z < nbPlanes; z++) {
                                    for (int y = 0; y < ySize; y++) {
                                        int dest = z * widthInBytes * ySize
                                                + y * widthInBytes
                                                + realOffset;
                                        planeBytes[dest] ^= data[pos++];
                                    }
                                }
                            } else {
                                for (int z = 0; z < nbPlanes; z++) {
                                    for (int y = 0; y < ySize; y++) {
                                        int dest = z * widthInBytes * ySize
                                                + y * widthInBytes
                                                + realOffset;
                                        planeBytes[dest] = data[pos++];
                                    }
                                }
                            }

                            // If we've stopped on an odd boundary, read and throw away
                            // another byte.
                            if (pos % 2 == 1) {
                                pos++;
                            }
                        }
                        break;
                    }
                    case 2: { /* Pile */

                        // Read Pile header
                        // struct {
                        //    short uni_flag;
                        //    short y_size;
                        //    short x_size;
                        //    short num_blocks; } pile;
                        int uniFlag = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);
                        int ySize = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);
                        int xSize = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);
                        int numBlocks = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);

                        // Decode Pile data
                        for (int b = 0; b < numBlocks; b++) {
                            int offset = ((data[pos++] & 0xff) << 8) | ((data[pos++]) & 0xff);

                            leftBound = Math.min(leftBound, (offset % widthInBytes) * 8);
                            rightBound = Math.max(rightBound, (offset % widthInBytes + xSize) * 8 + 8);
                            topBound = Math.min(topBound, (offset / widthInBytes));
                            bottomBound = Math.max(bottomBound, (offset / widthInBytes) + ySize);

                            int realOffset = (offset / widthInBytes) * nbPlanes;
                            realOffset *= widthInBytes;
                            realOffset += offset % widthInBytes;

                            if (uniFlag == 1) {
                                for (int z = 0; z < nbPlanes; z++) {
                                    for (int y = 0; y < ySize; y++) {
                                        for (int x = 0; x < xSize; x++) {
                                            int dest = z * widthInBytes * ySize
                                                    + y * widthInBytes
                                                    + realOffset + x;
                                            planeBytes[dest] ^= data[pos++];
                                        }
                                    }
                                }
                            } else {
                                for (int z = 0; z < nbPlanes; z++) {
                                    for (int y = 0; y < ySize; y++) {
                                        for (int x = 0; x < xSize; x++) {
                                            int dest = z * widthInBytes * ySize
                                                    + y * widthInBytes
                                                    + realOffset + x;
                                            planeBytes[dest] = data[pos++];
                                        }
                                    }
                                }
                            }

                            // If we've stopped on an odd boundary, read and throw away
                            // another byte.
                            if (pos % 2 == 1) {
                                pos++;
                            }
                        }
                        break;
                    }
                    default:
                        System.out.println("Unsupported changeType in 'J' delta frame:" + changeType);
                        break decodingLoop;
                    //throw new InternalError("Unsupported changeType in 'J' delta frame:"+changeType);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            // Some delta frames write over the bounds
            // of the bitmap.
            if (!isWarningPrinted) {
                e.printStackTrace();
                isWarningPrinted = true;
            }
        }

    }

    @Override
    public int getTopBound(ANIMMovieResources track) {
        return topBound;
    }

    @Override
    public int getBottomBound(ANIMMovieResources track) {
        return bottomBound;
    }

    @Override
    public int getLeftBound(ANIMMovieResources track) {
        return leftBound;
    }

    @Override
    public int getRightBound(ANIMMovieResources track) {
        return rightBound;
    }

    /**
     * Returns true if the frame can be decoded over both the previous frame or
     * the subsequent frame. Bidirectional frames can be used efficiently for
     * forward and backward playing a movie.
     * <p>
     * All key frames are bidirectional. Delta frames which use an XOR OP-mode
     * are bidirectional as well.
     */
    @Override
    public boolean isBidirectional() {
        switch (getOperation()) {
            case OP_Direct: // Key Frame (Data stored in ILBM BODY Chunk)
                return true;
            case OP_ByteVertical:
                if (getBits() == BIT_XOR) {
                    return true;
                }
                break;
            case OP_J: // All J-encoded frames seem to be bidirectional
                return true;
            default:
                break;
        }
        return false;
    }
}

/* @(#)ILBMEncoder.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */
package org.monte.media.ilbm;

import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.iff.IFFOutputStream;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import javax.imageio.stream.FileImageOutputStream;

/**
 * {@code ILBMEncoder}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-12-26 Created.
 */
public class ILBMEncoder {

    public ILBMEncoder() {
    }

    public void write(File f, AmigaBitmapImage img, int camg) throws IOException {
        IFFOutputStream out = null;
        try {
            out = new IFFOutputStream(new FileImageOutputStream(f));

            out.pushCompositeChunk("FORM", "ILBM");
            writeBMHD(out, img);
            writeCMAP(out, img);
            writeCAMG(out, camg);
            writeBODY(out, img);
            out.popChunk();
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * Writes the bitmap header (ILBM BMHD).
     *
     * <pre>
     * typedef UBYTE Masking; // Choice of masking technique
     *
     * #define mskNone                 0
     * #define mskHasMask              1
     * #define mskHasTransparentColor  2
     * #define mskLasso                3
     *
     * typedef UBYTE Compression; // Choice of compression algorithm
     *     // applied to the rows of all source and mask planes.
     *     // "cmpByteRun1" is the byte run encoding. Do not compress
     *     // accross rows!
     * #define cmpNone      0
     * #define cmpByteRun1  1
     *
     * typedef struct {
     *   UWORD       w, h; // raster width & height in pixels
     *   WORD        x, y; // pixel position for this image
     *   UBYTE       nbPlanes; // # source bitplanes
     *   Masking     masking;
     *   Compression compression;
     *   UBYTE       pad1;     // unused; ignore on read, write as 0
     *   UWORD       transparentColor; // transparent "color number" (sort of)
     *   UBYTE       xAspect, yAspect; // pixel aspect, a ratio width : height
     *   UWORD       pageWidth, pageHeight; // source "page" size in pixels
     *   } BitmapHeader;
     * </pre>
     */
    private void writeBMHD(IFFOutputStream out, AmigaBitmapImage img) throws IOException {
        out.pushDataChunk("BMHD");
        out.writeUWORD(img.getWidth());
        out.writeUWORD(img.getHeight());
        out.writeWORD(0);
        out.writeWORD(0);
        out.writeUBYTE(img.getDepth());
        out.writeUBYTE(0); // mskNone
        out.writeUBYTE(1); // cmpByteRun1
        out.writeUBYTE(0);
        out.writeUWORD(0);
        out.writeUBYTE(44);
        out.writeUBYTE(52);
        out.writeUWORD(img.getWidth());
        out.writeUWORD(img.getHeight());
        out.popChunk();
    }

    /**
     * Writes the color map (ILBM CMAP).
     */
    private void writeCMAP(IFFOutputStream out, AmigaBitmapImage img) throws IOException {
        out.pushDataChunk("CMAP");

        IndexColorModel cm = (IndexColorModel) img.getPlanarColorModel();
        for (int i = 0, n = cm.getMapSize(); i < n; ++i) {
            out.writeUBYTE(cm.getRed(i));
            out.writeUBYTE(cm.getGreen(i));
            out.writeUBYTE(cm.getBlue(i));
        }

        out.popChunk();
    }

    /**
     * Writes the color amiga viewport mode display id (ILBM CAMG).
     */
    private void writeCAMG(IFFOutputStream out, int camg) throws IOException {
        out.pushDataChunk("CAMG");

        out.writeLONG(camg);

        out.popChunk();
    }

    /**
     * Writes the body (ILBM BODY).
     */
    private void writeBODY(IFFOutputStream out, AmigaBitmapImage img) throws IOException {
        out.pushDataChunk("BODY");

        int w = img.getWidth()/8;
        int ss=img.getScanlineStride();
        int bs=img.getBitplaneStride();

        int offset=0;

       byte[] data = img.getBitmap();

        for (int y = 0, h = img.getHeight(); y < h; y++) {
            for (int p = 0, d = img.getDepth(); p < d; p++) {
                out.writeByteRun1(data, offset+bs*p, w);
            }
            offset+=ss;
        }

        out.popChunk();
    }
}

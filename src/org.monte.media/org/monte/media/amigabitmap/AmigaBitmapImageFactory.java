/* @(#)AmigaBitmapImageFactory.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.amigabitmap;

import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.iff.MC68000OutputStream;
import org.monte.media.iff.MutableIFFChunk;
import org.monte.media.ilbm.ColorCyclingMemoryImageSource;

/**
 * Creates a BufferedImage from a BitmapImage.
 * <p>
 We put these factory methods into this class instead of into class AmigaBitmapImage,
 because we don't want to put this additional code into Java applets that
 don't need this functionality.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class AmigaBitmapImageFactory {
    
    /** Prevent instance creation. */
    private AmigaBitmapImageFactory() {
    }
    
    /**
     * Creates a BufferedImage using the provided AmigaBitmapImage.
     *
     * @param bm The AmigaBitmapImage holding the image data.
     */
    public static BufferedImage toBufferedImage(AmigaBitmapImage bm) {
        BufferedImage image = null;
        Hashtable<?,?> properties = new Hashtable<Object,Object>() ;
        //properties.put("comment","AmigaBitmapImage");
        
        bm.convertToChunky();
        switch (bm.getPixelType()) {
            case AmigaBitmapImage.BYTE_PIXEL : {
                
                image = new BufferedImage(bm.getWidth(), bm.getHeight(),
                        BufferedImage.TYPE_BYTE_INDEXED,
                        (IndexColorModel) bm.getChunkyColorModel());
                WritableRaster ras = image.getRaster();
                byte[] pixels = ((DataBufferByte) ras.getDataBuffer()).getData();
                System.arraycopy(bm.getBytePixels(), 0, pixels, 0, bm.getBytePixels().length);
                break;
            }
            case AmigaBitmapImage.INT_PIXEL : {
                WritableRaster ras = Raster.createPackedRaster(
                        DataBuffer.TYPE_INT, bm.getWidth(), bm.getHeight(),
                        3, 8, new Point());
                image = new BufferedImage(bm.getChunkyColorModel(), ras, false,
                        properties);
                int[] pixels = ((DataBufferInt) ras.getDataBuffer()).getData();
                System.arraycopy(bm.getIntPixels(), 0, pixels, 0, bm.getIntPixels().length);
                break;
            }
        }
        
        return image;
    }
    public static Image toMemoryImage(AmigaBitmapImage bm) {
        bm.convertToChunky();
        switch (bm.getPixelType()) {
            case AmigaBitmapImage.BYTE_PIXEL : {
                
                MemoryImageSource mis = new MemoryImageSource(
                        bm.getWidth(), bm.getHeight(), bm.getChunkyColorModel(),
                        bm.getBytePixels().clone(), 0, bm.getWidth());
                return Toolkit.getDefaultToolkit().createImage(mis);
            }
            case AmigaBitmapImage.INT_PIXEL : {
                MemoryImageSource mis = new MemoryImageSource(
                        bm.getWidth(), bm.getHeight(), bm.getChunkyColorModel(),
                        bm.getIntPixels().clone(), 0, bm.getWidth());
                return Toolkit.getDefaultToolkit().createImage(mis);
            }
        }
        
        return null;
    }

    public static AmigaBitmapImage toBitmapImage(MemoryImageSource mis) {
        return null;
    }
    public static AmigaBitmapImage toBitmapImage(ColorCyclingMemoryImageSource mis) {
        return null;
    }
    public static AmigaBitmapImage toBitmapImage(BufferedImage mis) {
        return null;
    }
    
    public static void write(AmigaBitmapImage bm, File f) throws IOException {
        OutputStream out = new BufferedOutputStream(new FileOutputStream(f));
        try {
            write(bm, out);
        } finally {
            out.close();
        }
    }
    public static void write(AmigaBitmapImage bm, OutputStream out) throws IOException {
        MutableIFFChunk form = new MutableIFFChunk("FORM", "ILBM");
        
        ByteArrayOutputStream buf;
        MC68000OutputStream struct;
        
        // Write BMHD Chunk
        
        /*  Masking techniques  * /
        enum {
            none=0, hasMask=1, hasTransparentColor=2, lasso=3
        } bmhdMasking;
         
        /*  Compression methods * /
        enum {
            none=0, byteRun1=1
        } bmhdCompression;
         
        typedef struct {
            UWORD   width; UWORD height;           /* Width, height in pixels * /
            WORD    xPosition; WORD yPosition;           /* x, y position for this bitmap  * /
            UBYTE   numberOfPlanes;        /* # of planes (not including mask) * /
            UBYTE enum bmhdMasking masking;        /* a masking technique listed above * /
            UBYTE enum bmhdCompression compression;    /* cmpNone or cmpByteRun1 * /
            UBYTE   reserved1;      /* must be zero for now * /
            UWORD   transparentColor;
            UBYTE   xAspect; UBYTE yAspect;
            WORD    pageWidth; WORD pageHeight;
        } ilbmBitmapHeaderChunk;
         */
        struct = new MC68000OutputStream(buf = new ByteArrayOutputStream());
        struct.writeUWORD(bm.getWidth()); // width
        struct.writeUWORD(bm.getHeight()); // height
        struct.writeWORD(0); // xPosition
        struct.writeWORD(0); // yPosition
        struct.writeUBYTE(bm.getDepth()); // numberOfPlanes
        struct.writeUBYTE(0); // masking
        struct.writeUBYTE(1); // compression
        struct.writeUBYTE(0); // reserved1
        struct.writeUWORD(0); // transparentColor
        struct.writeUBYTE(10); // xAspect
        struct.writeUBYTE(11); // yAspect
        struct.writeWORD(bm.getWidth()); // pageWidth
        struct.writeWORD(bm.getHeight()); // pageHeight
        struct.close();
        form.add(new MutableIFFChunk("BMHD", buf.toByteArray()));
        
        
        ColorModel cm = bm.getPlanarColorModel();
        /*
         * ILBM CAMG Amiga Viewport Mode Display ID
         * --------------------------------------------
         * /
        /* The CAMG chunk is used to store the Amiga display mode in which
         * an ILBM is meant to be displayed.  This is very important, especially
         * for special display modes such as HAM and HALFBRITE where the
         * pixels are interpreted differently.
         * Under V37 and higher, store a 32-bit Amiga DisplayID (aka. ModeID)
         * in the ULONG ViewModes CAMG variable (from GetVPModeID(viewport)).
         * Pre-V37, instead store the 16-bit viewport->Modes.
         * See the current IFF manual for information on screening for bad CAMG
         * chunks when interpreting a CAMG as a 32-bit DisplayID or 16-bit ViewMode.
         * The chunk's content is declared as a ULONG.
         * /
         
        set {
           Interlace      = 0x00004,
           ExtraHalfbrite = 0x00080,
           DualPlayfield  = 0x00400,
           HoldAndModify  = 0x00800,
           Hires          = 0x08000,
           Super          = 0x08020
        } ilbmAmigaViewModes;
         
        typedef struct {
            ULONG set ilbmAmigaViewModes ViewModes;
        } ilbmAmigaViewportModeDisplayIDChunk;
         */
        struct = new MC68000OutputStream(buf = new ByteArrayOutputStream());
        int viewMode = 0;
        if (cm instanceof AmigaHAMColorModel) {
            viewMode |= 0x00800;
        }
        struct.writeULONG(viewMode);
        struct.close();
        form.add(new MutableIFFChunk("CAMG", buf.toByteArray()));
        
        /*
         * ILBM CMAP Color map
         * --------------------------------------------
         * /
        /* A CMAP chunk is a packed array of ColorRegisters (3 bytes each). * /
        typedef struct {
            UBYTE red; UBYTE green; UBYTE blue;   /* MUST be UBYTEs so ">> 4" won't sign extend.* /
        } ilbmColorRegister;
         
        typedef struct {
            ilbmColorRegister color[];
        } ilbmColorMapChunk;
         */
        if (cm instanceof AmigaHAMColorModel) {
            AmigaHAMColorModel hcm = (AmigaHAMColorModel) cm;
            struct = new MC68000OutputStream(buf = new ByteArrayOutputStream());
            byte[] r = new byte[hcm.getMapSize()];
            byte[] g = new byte[hcm.getMapSize()];
            byte[] b = new byte[hcm.getMapSize()];
            hcm.getReds(r);
            hcm.getGreens(g);
            hcm.getBlues(b);
            for (int i=0; i <r.length; i++) {
                struct.writeUBYTE(r[i]);
                struct.writeUBYTE(g[i]);
                struct.writeUBYTE(b[i]);
            }
            struct.close();
            form.add(new MutableIFFChunk("CMAP", buf.toByteArray()));
        }
        // XXX - Add support for index color model
        
        /* Write BODY Chunk */
        struct = new MC68000OutputStream(buf = new ByteArrayOutputStream());
        for (int y=0, height=bm.getHeight(); y < height; y++) {
            for (int d=0, depth=bm.getDepth(); d < depth; d++) {
                struct.writeByteRun1(bm.getBitmap(), y * bm.getScanlineStride() + d * bm.getBitplaneStride(), bm.getWidth() / 8);
            }
        }
   
        form.add(new MutableIFFChunk("BODY", buf.toByteArray()));
        
        MC68000OutputStream mout = new MC68000OutputStream(out);
        form.write(mout);
        mout.flush();
    }
}

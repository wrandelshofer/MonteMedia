/* @(#)PBMDecoder.java
 * Copyright © 2005-2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.pbm;

import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import org.monte.media.exception.AbortException;
import org.monte.media.exception.ParseException;
import org.monte.media.iff.IFFChunk;
import org.monte.media.iff.IFFParser;
import org.monte.media.iff.IFFVisitor;
import org.monte.media.iff.MC68000InputStream;
import org.monte.media.ilbm.CRNGColorCycle;
import org.monte.media.ilbm.ColorCycle;
import org.monte.media.ilbm.ColorCyclingMemoryImageSource;
import org.monte.media.ilbm.DRNGColorCycle;

/**
 * Creates Image objects by reading an IFF PBM stream.
 *
 * <p><b>PBM regular expression</b>
 * <pre>
 * PBM ::= "FORM" #{ "PBM" BMHD [CMAP] [GRAB] [DEST] [SPRT] [CAMG] CRNG* CCRT* [BODY] }
 *
 * BMHD ::= "BMHD" #{ BitMapHeader }
 * CMAP ::= "CMAP" #{ (red green blue)* } [0]
 * GRAB ::= "GRAB" #{ Point2D }
 * DEST ::= "DEST" #{ DestMerge }
 * SPRT ::= "SPRT" #{ SpritePrecedence }
 * CAMG ::= "CAMG" #{ LONG }
 *
 * CRNG ::= "CRNG" #{ CRange }
 * CCRT ::= "CCRT" #{ CycleInfo }
 * BODY ::= "BODY" #{ UBYTE* } [0]
 * </pre> The token "#" represents a
 * <code>ckSize</code> LONG count of the following braced data bytes. E.g., a
 * BMHD's "#" should equal
 * <code>sizeof(BitMapHeader)</code>. Literal strings are shown in "quotes",
 * [square bracket items] are optional, and "*" means 0 or more repetitions. A
 * sometimes-needed pad byte is shown as "[0]".
 *
 * @author Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version 1.6 2010-08-02 Added support for CRNG and DRNG chunks. <br>1.5.1
 * 2010-07-03 Improved performance of byterun1 decoder. <br>1.0 2005-04-05
 * Created.
 */
public class PBMDecoder implements IFFVisitor {
    /* ---- constants ---- */

    /**
     * Chunk ID's.
     */
    protected final static int PBM_ID = IFFParser.stringToID("PBM ");
    protected final static int BMHD_ID = IFFParser.stringToID("BMHD");
    protected final static int CMAP_ID = IFFParser.stringToID("CMAP");
    protected final static int CRNG_ID = IFFParser.stringToID("CRNG");
    protected final static int DRNG_ID = IFFParser.stringToID("DRNG");
    protected final static int BODY_ID = IFFParser.stringToID("BODY");
    private final static int AUTH_ID = IFFParser.stringToID("AUTH");
    private final static int ANNO_ID = IFFParser.stringToID("ANNO");
    private final static int COPYRIGHT_ID = IFFParser.stringToID("(c) ");
    /**
     * PBM BMHD chunk: masking technique.
     */
    protected final static int MSK_NONE = 0,
            MSK_HAS_MASK = 1,
            MSK_HAS_TRANSPARENT_COLOR = 2,
            MSK_LASSO = 3;
    /**
     * PBM BMHD chunk: compression algorithm.
     */
    protected final static int CMP_NONE = 0,
            CMP_BYTE_RUN_1 = 1;
    /* ---- instance variables ---- */
    /**
     * Input stream to decode from.
     */
    protected InputStream inputStream;
    /**
     * URL to get the input stream from.
     */
    protected URL location;
    /**
     * Stores all the PBM pictures found during decoding as an instance of
     * MemoryImageSource.
     */
    protected ArrayList<ColorCyclingMemoryImageSource> sources;
   
    /**
     * BMHD data.
     */
    /**
     * Raster width_ and heigth in pixels
     */
    protected int bmhdWidth, bmhdHeight;
    /**
     * pixel position for this image
     */
    protected int bmhdXPosition, bmhdYPosition;
    /**
     * Number of source bitplanes.
     */
    protected int bmhdNbPlanes;
    protected int bmhdMasking;
    protected int bmhdCompression;
    /**
     * Transparent "color number" (sort of).
     */
    protected int bmhdTransparentColor;
    /**
     * Pixel aspect, a ratio width : height
     */
    protected int bmhdXAspect, bmhdYAspect;
    /**
     * Source "page" size in pixels.
     */
    protected int bmhdPageWidth, bmhdPageHeight;
    /**
     * CMAP data.
     */
    protected ColorModel cmapColorModel;
    /**
     * BODY data
     */
    protected ColorCyclingMemoryImageSource memoryImageSource;

    /**
     * Constructors
     */
    public PBMDecoder(InputStream in) {
        inputStream = in;
    }

    public PBMDecoder(URL location) {
        this.location = location;
    }

    /**
     * Processes the input stream and creates a vector of MemoryImageSource
     * instances.
     *
     * @return A vector of java.awt.img.MemoryImageSource.
     */
    public ArrayList<ColorCyclingMemoryImageSource> produce()
            throws IOException {
        InputStream in = null;
        sources = new ArrayList<ColorCyclingMemoryImageSource>();
        boolean mustCloseStream;
        if (inputStream != null) {
            in = inputStream;
            mustCloseStream = false;
        } else {
            in = location.openStream();
            mustCloseStream = true;
        }
        try {

            IFFParser iff = new IFFParser();
            registerChunks(iff);
            iff.parse(in, this);
        } catch (ParseException e1) {
            e1.printStackTrace();//System.out.println(e1);
        } catch (AbortException e) {
            e.printStackTrace();//System.out.println(e);
        } finally {
             if (mustCloseStream) {
                in.close();
            }
        }
        return sources;
    }

    public void registerChunks(IFFParser iff) {
        iff.declareGroupChunk(PBM_ID, IFFParser.ID_FORM);
        iff.declarePropertyChunk(PBM_ID, BMHD_ID);
        iff.declarePropertyChunk(PBM_ID, CMAP_ID);
        iff.declareDataChunk(PBM_ID, BODY_ID);
        iff.declareCollectionChunk(PBM_ID, ANNO_ID);
        iff.declareCollectionChunk(PBM_ID, COPYRIGHT_ID);
        iff.declareCollectionChunk(PBM_ID, AUTH_ID);
        iff.declareCollectionChunk(PBM_ID, CRNG_ID);
        iff.declareCollectionChunk(PBM_ID, DRNG_ID);
    }

    @Override
    public void enterGroup(IFFChunk chunk) {
    }

    @Override
    public void leaveGroup(IFFChunk chunk) {
    }

    @Override
    public void visitChunk(IFFChunk group, IFFChunk chunk)
            throws ParseException, AbortException {
        decodeBMHD(group.getPropertyChunk(BMHD_ID));
        decodeCMAP(group.getPropertyChunk(CMAP_ID));
        decodeBODY(chunk);

        double aspect = (double) bmhdXAspect / (double) bmhdYAspect;
        if (bmhdXAspect == 0 || bmhdYAspect == 0) {
            aspect = 1d;
        }
        @SuppressWarnings("unchecked")
        Hashtable<Object,Object> props = (Hashtable<Object,Object>) memoryImageSource.getProperties();

        props.put("aspect", new Double(aspect));
        String s = "Indexed Colors";
        props.put("screenMode", s);
        props.put("nbPlanes", "" + bmhdNbPlanes + (((bmhdMasking & MSK_HAS_MASK) != 0) ? "+mask" : ""));

        StringBuffer comment = new StringBuffer();
        IFFChunk[] chunks = group.getCollectionChunks(ANNO_ID);
        for (int i = 0; i < chunks.length; i++) {
            if (comment.length() > 0) {
                comment.append('\n');
            }
            comment.append(new String(chunks[i].getData()));
        }
        chunks = group.getCollectionChunks(AUTH_ID);
        for (int i = 0; i < chunks.length; i++) {
            if (comment.length() > 0) {
                comment.append('\n');
            }
            comment.append("Author: ");
            comment.append(new String(chunks[i].getData()));
        }
        chunks = group.getCollectionChunks(COPYRIGHT_ID);
        for (int i = 0; i < chunks.length; i++) {
            if (comment.length() > 0) {
                comment.append('\n');
            }
            comment.append("© ");
            comment.append(new String(chunks[i].getData()));
        }
        if (comment.length() > 0) {
            props.put("comment", comment.toString());
        }
        // Process CRNG and DRNG chunks in the sequence of their
        // location in the file.
        IFFChunk[] crngChunks = group.getCollectionChunks(CRNG_ID);
        IFFChunk[] drngChunks = group.getCollectionChunks(DRNG_ID);
        int activeCycles = 0;
        int j = 0, k = 0;
        for (int i = 0, n = crngChunks.length + drngChunks.length; i < n; i++) {
            if (j < crngChunks.length && (k >= drngChunks.length || crngChunks[j].getScan() < drngChunks[k].getScan())) {
//                System.out.println("ILBMDecoder decoding CRNG@"+crngChunks[j].getScan());
                ColorCycle cc = decodeCRNG(crngChunks[j]);
                memoryImageSource.addColorCycle(cc);
                if (cc.isActive()) {
                    activeCycles++;
                }
                j++;
            } else {
//                System.out.println("ILBMDecoder decoding DRNG@"+drngChunks[k].getScan());
                ColorCycle cc = decodeDRNG(drngChunks[k]);
                memoryImageSource.addColorCycle(cc);
                if (cc.isActive()) {
                    activeCycles++;
                }
                k++;
            }
        }
        if (activeCycles > 0) {
            memoryImageSource.setAnimated(true);
            props.put("colorCycling", activeCycles);
        }
//memoryImageSource.putProperties(props);
        sources.add(memoryImageSource);
    }

    /**
     * Decodes the bitmap header (PBM BMHD).
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
     *   WORD        pageWidth, pageHeight; // source "page" size in pixels
     *   } BitmapHeader;
     * </pre>
     */
    protected void decodeBMHD(IFFChunk chunk)
            throws ParseException {
        if (chunk == null) {
            throw new ParseException("no BMHD -> no Picture");
        }
        try {
            MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));
            bmhdWidth = in.readUWORD();
            bmhdHeight = in.readUWORD();
            bmhdXPosition = in.readWORD();
            bmhdYPosition = in.readWORD();
            bmhdNbPlanes = in.readUBYTE();
            bmhdMasking = in.readUBYTE();
            bmhdCompression = in.readUBYTE();
            in.skip(1);
            bmhdTransparentColor = in.readUWORD();
            bmhdXAspect = in.readUBYTE();
            bmhdYAspect = in.readUBYTE();
            bmhdPageWidth = in.readWORD();
            bmhdPageHeight = in.readWORD();
            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
    }

    protected void decodeCMAP(IFFChunk chunk)
            throws ParseException {
        byte[] red;
        byte[] green;
        byte[] blue;
        byte[] alpha;
        int size = 0;
        int colorsToRead = 0;

        size = ((bmhdMasking & MSK_HAS_MASK) != 0) ? 2 << bmhdNbPlanes : 1 << bmhdNbPlanes;
        colorsToRead = Math.min(size, (int) chunk.getSize() / 3);


        red = new byte[size];
        green = new byte[size];
        blue = new byte[size];

        byte[] data = chunk.getData();
        int j = 0;
        for (int i = 0; i < colorsToRead; i++) {
            red[i] = data[j++];
            green[i] = data[j++];
            blue[i] = data[j++];
        }

        int transparentColorIndex = ((bmhdMasking & MSK_HAS_TRANSPARENT_COLOR) != 0) ? bmhdTransparentColor : -1;

        if ((bmhdMasking & MSK_HAS_MASK) != 0) {
            System.arraycopy(red, 0, red, red.length / 2, red.length / 2);
            System.arraycopy(green, 0, green, green.length / 2, green.length / 2);
            System.arraycopy(blue, 0, blue, blue.length / 2, blue.length / 2);
            alpha = new byte[red.length];
            for (int i = 0, n = red.length / 2; i < n; i++) {
                alpha[i] = (byte) 0xff;
            }
            cmapColorModel = new IndexColorModel(8, red.length, red, green, blue, alpha);
        } else {
            cmapColorModel = new IndexColorModel(8, red.length, red, green, blue, transparentColorIndex);
        }
    }

    /**
     * Decodes the color range cycling (ILBM CRNG).
     *
     * <pre>
     * #define RNG_NORATE  36   // Dpaint uses this rate to mean non-active
     *  set {
     *  active = 1, reverse = 2
     *  } crngActive;
     *
     *  // A CRange is store in a CRNG chunk.
     *  typedef struct {
     *  WORD  pad1;              // reserved for future use; store 0 here *
     *  WORD  rate;              // 60/sec=16384, 30/sec=8192, 1/sec=16384/60=273
     *  WORD set crngActive flags;     // bit0 set = active, bit 1 set = reverse
     *  UBYTE low; UBYTE high;         // lower and upper color registers selected
     *  } ilbmColorRegisterRangeChunk;
     * </pre>
     */
    protected ColorCycle decodeCRNG(IFFChunk chunk)
            throws ParseException {
        ColorCycle cc;
        try {
            MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));

            int pad1 = in.readUWORD();
            int rate = in.readUWORD();
            int flags = in.readUWORD();
            int low = in.readUBYTE();
            int high = in.readUBYTE();
//System.out.println("CRNG pad1:"+pad1+" rate:"+rate+" flags:"+flags+" low:"+low+" high:"+high);
            cc = new CRNGColorCycle(rate, 273, low, high,//
                    (flags & 1) != 0 && rate > 36 && high > low, //
                    (flags & 2) != 0, false);

            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
        return cc;
    }

    /**
     * Decodes the DPaint IV enhanced color cycle chunk (ILBM DRNG) <p> The
     * RNG_ACTIVE flag is set when the range is cyclable. A range should only
     * have the RNG _ACTIVE if it: <ol> <li>contains at least one color
     * register</li> <li>has a defined rate</li> <li>has more than one color
     * and/or color register</li> </ol>
     * <pre>
     * ILBM DRNG DPaint IV enhanced color cycle chunk
     * --------------------------------------------
     *
     * set {
     *     RNG_ACTIVE=1,RNG_DP_RESERVED=4
     * } drngFlags;
     *
     * /* True color cell * /
     * typedef struct {
     *     UBYTE cell;
     *     UBYTE r;
     *     UBYTE g;
     *     UBYTE b;
     * } ilbmDRNGDColor;
     *
     * /* Color register cell * /
     * typedef struct {
     *     UBYTE cell;
     *     UBYTE index;
     * } ilbmDRNGDIndex;
     *
     * /* DRNG chunk. * /
     * typedef struct {
     *     UBYTE min; /* min cell value * /
     *     UBYTE max; /* max cell value * /
     *     UWORD rate; /* color cycling rate, 16384 = 60 steps/second * /
     *     UWORD set drngFlags flags; /* 1=RNG_ACTIVE, 4=RNG_DP_RESERVED * /
     *     UBYTE ntrue; /* number of DColorCell structs to follow * /
     *     UBYTE ntregs; /* number of DIndexCell structs to follow * /
     *     ilbmDRNGDColor[ntrue] trueColorCells;
     *     ilbmDRNGDIndex[ntregs] colorRegisterCells;
     * } ilbmDRangeChunk;
     * </pre>
     */
    protected ColorCycle decodeDRNG(IFFChunk chunk)
            throws ParseException {
        ColorCycle cc;
        try {
            MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));

            int min = in.readUBYTE();
            int max = in.readUBYTE();
            int rate = in.readUWORD();
            int flags = in.readUWORD();
            int ntrue = in.readUBYTE();
            int nregs = in.readUBYTE();
            DRNGColorCycle.Cell[] cells = new DRNGColorCycle.Cell[ntrue + nregs];

            for (int i = 0; i < ntrue; i++) {
                int cell = in.readUBYTE();
                int rgb = (in.readUBYTE() << 16) | (in.readUBYTE() << 8) | in.readUBYTE();
                cells[i] = new DRNGColorCycle.DColorCell(cell, rgb);
            }
            for (int i = 0; i < nregs; i++) {
                int cell = in.readUBYTE();
                int index = in.readUBYTE();
                cells[i + ntrue] = new DRNGColorCycle.DIndexCell(cell, index);
            }

//System.out.println("DRNG min:"+min+" max:"+max+" rate:"+rate+" flags:"+flags+" ntrue:"+ntrue+" nregs:"+nregs);
            cc = new DRNGColorCycle(rate, 273, min, max, //
                    (flags & 1) != 0 && rate > 36 && min <= max && ntrue + nregs > 1,//
                    false, cells);

            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
        return cc;
    }

    protected void decodeBODY(IFFChunk chunk)
            throws ParseException {
        int pixmapWidth = (bmhdWidth % 2 == 1) ? bmhdWidth + 1 : bmhdWidth;
        byte[] pixels = new byte[pixmapWidth * bmhdHeight];

        byte[] data = chunk.getData();

        switch (bmhdCompression) {
            case CMP_NONE:
                System.arraycopy(data, 0, pixels, 0, data.length);
                break;
            case CMP_BYTE_RUN_1:
                unpackByteRun1(data, pixels);
                break;
            default:
                throw new ParseException("unknown compression method: " + bmhdCompression);
        }

        Hashtable<?,?> props = new Hashtable<Object,Object>();
        if ((bmhdMasking & MSK_HAS_MASK) != 0) {
            // XXX - Handle image creation with mask
            System.out.println("PBMDecoder Images with Mask not supported");
            memoryImageSource = new ColorCyclingMemoryImageSource(bmhdWidth, bmhdHeight, cmapColorModel, pixels, 0, pixmapWidth, props);
        } else {
            memoryImageSource = new ColorCyclingMemoryImageSource(bmhdWidth, bmhdHeight, cmapColorModel, pixels, 0, pixmapWidth, props);
        }
    }

    /**
     * ByteRun1 run decoder. <p> The run encoding scheme by <em>byteRun1</em> is
     * best described by pseudo code for the decoder <em>Unpacker</em> (called
     * <em>UnPackBits</em> in the Macintosh toolbox.
     * <pre>
     * UnPacker:
     *  LOOP until produced the desired number of bytes
     *      Read the next source byte into n
     *      SELECT n FROM
     *          [0..127] =&gt; copy the next n+1 bytes literally
     *          [-1..-127] =&gt; replicate the next byte -n+1 times
     *          -128    =&gt; no operation
     *      ENDCASE;
     *   ENDLOOP;
     * </pre>
     *
     * @param in
     * @param out
     * @throws ParseException
     */
    public static int unpackByteRun1(byte[] in, byte[] out)
            throws ParseException {
        try {
            return MC68000InputStream.unpackByteRun1(in, out);
        } catch (IOException ex) {
            ParseException e = new ParseException("couldn't decompress body");
            e.initCause(ex);
            throw e;
        }
    }
}

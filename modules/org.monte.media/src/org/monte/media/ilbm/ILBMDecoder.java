/* @(#)ILBMDecoder.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.ilbm;

import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.amigabitmap.AmigaHAMColorModel;
import org.monte.media.exception.AbortException;
import org.monte.media.exception.ParseException;
import org.monte.media.iff.IFFChunk;
import org.monte.media.iff.IFFParser;
import org.monte.media.iff.IFFVisitor;
import org.monte.media.iff.MC68000InputStream;

/**
 * Creates Image objects by reading an IFF ILBM stream.
 *
 * <p><b>ILBM regular expression</b>
 * <pre>
 * ILBM ::= "FORM" #{ "ILBM" BMHD [CMAP] [GRAB] [DEST] [SPRT] [CAMG] CRNG* CCRT* DRNG* [BODY] }
 *
 * BMHD ::= "BMHD" #{ BitMapHeader }
 * CMAP ::= "CMAP" #{ (red green blue)* } [0]
 * GRAB ::= "GRAB" #{ Point2D }
 * DEST ::= "DEST" #{ DestMerge }
 * SPRT ::= "SPRT" #{ SpritePrecedence }
 * CAMG ::= "CAMG" #{ LONG }
 *
 * CRNG ::= "CRNG" #{ CRange }
 * DRNG ::= "DRNG" #{ DRange }
 * CCRT ::= "CCRT" #{ CycleInfo }
 * BODY ::= "BODY" #{ UBYTE* } [0]
 * </pre>
 * The token "#" represents a <code>ckSize</code> LONG count of the following
 * braced data bytes. E.g., a BMHD's "#" should equal <code>sizeof(BitMapHeader)</code>.
 * Literal strings are shown in "quotes", [square bracket items] are optional, and
 * "*" means 0 or more repetitions. A sometimes-needed pad byte is shown as "[0]".
 *
 * @author  Werner Randelshofer, Hausmatt 10, CH-6405 Goldau, Switzerland
 * @version $Id$
 */
public class ILBMDecoder
        implements IFFVisitor {
    /* ---- constants ---- */

    /** Chunk ID's. */
    protected final static int ILBM_ID = IFFParser.stringToID("ILBM");
    protected final static int BMHD_ID = IFFParser.stringToID("BMHD");
    protected final static int CMAP_ID = IFFParser.stringToID("CMAP");
    protected final static int CAMG_ID = IFFParser.stringToID("CAMG");
    protected final static int CCRT_ID = IFFParser.stringToID("CCRT");
    protected final static int CRNG_ID = IFFParser.stringToID("CRNG");
    protected final static int DRNG_ID = IFFParser.stringToID("DRNG");
    protected final static int BODY_ID = IFFParser.stringToID("BODY");
    protected final static int VDAT_ID = IFFParser.stringToID("VDAT");
    private final static int AUTH_ID = IFFParser.stringToID("AUTH");
    private final static int ANNO_ID = IFFParser.stringToID("ANNO");
    private final static int COPYRIGHT_ID = IFFParser.stringToID("(c) ");
    /** ILBM CAMG chunk: Commodore Amiga Video display modes. */
    /** ILBM CAMG chunk: Mask and ID bits for CAMG ModeID. */
    protected final static int MONITOR_ID_MASK = 0xffff1000;
    protected final static int DEFAULT_MONITOR_ID = 0x00000000;
    protected final static int NTSC_MONITOR_ID = 0x00011000;
    protected final static int PAL_MONITOR_ID = 0x00021000;
    protected final static int VGA_MONITOR_ID = 0x00031000;
    protected final static int A2024_MONITOR_ID = 0x00041000;
    protected final static int PROTO_MONITOR_ID = 0x00051000;
    protected final static int EURO72_MONITOR_ID = 0x00061000;
    protected final static int EURO36_MONITOR_ID = 0x00071000;
    protected final static int SUPER72_MONITOR_ID = 0x00081000;
    protected final static int DBLNTSC_MONITOR_ID = 0x00091000;
    protected final static int DBLPAL_MONITOR_ID = 0x00001000;
    protected final static int MODE_INDEXED_COLORS = 0,
            MODE_DIRECT_COLORS = 1,
            MODE_EHB = 2,
            MODE_HAM6 = 3,
            MODE_HAM8 = 4;
    protected final static int HAM_MASK = 0x00000800;
    protected final static int EHB_MASK = 0x00000080;
    protected final static int HAM_KEY = 0x00000800;
    protected final static int EXTRAHALFBRITE_KEY = 0x00000080;
    /** ILBM BMHD chunk: masking technique. */
    protected final static int MSK_NONE = 0,
            MSK_HAS_MASK = 1,
            MSK_HAS_TRANSPARENT_COLOR = 2,
            MSK_LASSO = 3;
    /** ILBM BMHD chunk: compression algorithm. */
    protected final static int CMP_NONE = 0,
            CMP_BYTE_RUN_1 = 1, CMP_VERTICAL = 2;
    /* ---- instance variables ---- */
    /** Input stream to decode from. */
    protected InputStream inputStream;
    /** URL to get the input stream from. */
    protected URL location;
    /** Stores all the ILBM pictures found during decoding
     * as an instance of ColorCyclingMemoryImageSource. */
    protected ArrayList<ColorCyclingMemoryImageSource> sources;
    protected ArrayList<AmigaBitmapImage> bitmapSources;
    /** BMHD data. */
    /** Raster width and height in pixels */
    protected int bmhdWidth, bmhdHeight;
    /** pixel position for this image */
    protected int bmhdXPosition, bmhdYPosition;
    /** Number of source bitplanes. */
    protected int bmhdNbPlanes;
    protected int bmhdMasking;
    protected int bmhdCompression;
    /** Transparent "color number" (sort of). */
    protected int bmhdTransparentColor;
    /** Pixel aspect, a ratio width : height */
    protected int bmhdXAspect, bmhdYAspect;
    /** Source "page" size in pixels. */
    protected int bmhdPageWidth, bmhdPageHeight;
    /**
     * Commodore Amiga Graphics Mode.
     */
    protected int camg=NTSC_MONITOR_ID;
    /** CAMG Video display mode. */
    protected int camgMode;
    /** CMAP data. */
    protected ColorModel cmapColorModel;
    /** BODY data */
    protected AmigaBitmapImage bodyBitmap;

    /** Constructors */
    public ILBMDecoder(InputStream in) {
        inputStream = in;
    }

    public ILBMDecoder(URL location) {
        this.location = location;
    }

    /**
     * Processes the input stream and creates a vector of
     * ColorCyclingMemoryImageSource instances.
     *
     * @return  A vector of java.awt.img.ColorCyclingMemoryImageSource.
     */
    public ArrayList<ColorCyclingMemoryImageSource> produce()
            throws IOException {
        InputStream in = null;
        sources = new ArrayList<ColorCyclingMemoryImageSource>();
        bitmapSources = null;
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
            e1.printStackTrace();
        } catch (AbortException e) {
            e.printStackTrace();
        } finally {
            if (mustCloseStream) {
                in.close();
            }
        }
        return sources;
    }

    /**
     * Processes the input stream and creates a vector of
     * BitmapImages instances.
     *
     * @return  A vector of java.awt.img.ColorCyclingMemoryImageSource.
     */
    public ArrayList<AmigaBitmapImage> produceBitmaps()
            throws IOException {
        InputStream in = null;
        sources = null;
        bitmapSources = new ArrayList<AmigaBitmapImage>();
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
            e1.printStackTrace();
        } catch (AbortException e) {
            e.printStackTrace();
        } finally {
            if (mustCloseStream) {
                in.close();
            }
        }
        return bitmapSources;
    }

    public void registerChunks(IFFParser iff) {
        iff.declareGroupChunk(ILBM_ID, IFFParser.ID_FORM);
        iff.declarePropertyChunk(ILBM_ID, BMHD_ID);
        iff.declarePropertyChunk(ILBM_ID, CMAP_ID);
        iff.declarePropertyChunk(ILBM_ID, CAMG_ID);
        iff.declareDataChunk(ILBM_ID, BODY_ID);
        iff.declareCollectionChunk(ILBM_ID, ANNO_ID);
        iff.declareCollectionChunk(ILBM_ID, COPYRIGHT_ID);
        iff.declareCollectionChunk(ILBM_ID, AUTH_ID);
        iff.declareCollectionChunk(ILBM_ID, CRNG_ID);
        iff.declareCollectionChunk(ILBM_ID, DRNG_ID);
        iff.declareCollectionChunk(ILBM_ID, CCRT_ID);
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
        decodeCAMG(group.getPropertyChunk(CAMG_ID));
        boolean is4BitsPerChannel = (camg & MONITOR_ID_MASK) == DEFAULT_MONITOR_ID;
        decodeCMAP(group.getPropertyChunk(CMAP_ID), is4BitsPerChannel);
        decodeBODY(chunk);

        Hashtable<String,Object> props = new Hashtable<String,Object>();
        double aspect = (double) bmhdXAspect / (double) bmhdYAspect;
        if (bmhdXAspect == 0 || bmhdYAspect == 0) {
            aspect = 1d;
        }
        props.put("aspect", new Double(aspect));
        String s;
        switch (camgMode) {
            case MODE_INDEXED_COLORS:
                s = "Indexed Colors";
                break;
            case MODE_DIRECT_COLORS:
                s = "Direct Colors";
                break;
            case MODE_EHB:
                s = "EHB";
                break;
            case MODE_HAM6:
                s = "HAM 6";
                break;
            case MODE_HAM8:
                s = "HAM 8";
                break;
            default:
                s = "unknown";
                break;
        }
        props.put("screenMode", s);
        props.put("nbPlanes", "" + bmhdNbPlanes + (((bmhdMasking & MSK_HAS_MASK) != 0) ? "+mask" : ""));
        props.put("CAMG", new Integer(camg));

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

        bodyBitmap.setEnforceDirectColors(false);

        if (sources != null) {
            ColorCyclingMemoryImageSource mis;
            if (bodyBitmap.convertToChunky() == AmigaBitmapImage.BYTE_PIXEL) {
                mis = new ColorCyclingMemoryImageSource(
                        bmhdWidth, bmhdHeight,
                        cmapColorModel,
                        bodyBitmap.getBytePixels(), 0, bmhdWidth,
                        props);
            } else {
                mis = new ColorCyclingMemoryImageSource(
                        bmhdWidth, bmhdHeight,
                        bodyBitmap.getChunkyColorModel(),
                        bodyBitmap.getIntPixels(), 0, bmhdWidth,
                        props);
            }

            // Process CCRT, CRNG and DRNG chunks in the sequence of their
            // location in the file.
            IFFChunk[] ccrtChunks = group.getCollectionChunks(CCRT_ID);
            IFFChunk[] crngChunks = group.getCollectionChunks(CRNG_ID);
            IFFChunk[] drngChunks = group.getCollectionChunks(DRNG_ID);
            int activeCycles = 0;
            int j = 0, k = 0, l = 0;
            for (int i = 0, n = ccrtChunks.length + crngChunks.length + drngChunks.length; i < n; i++) {
                if (j < crngChunks.length //
                        && (k >= drngChunks.length || crngChunks[j].getScan() < drngChunks[k].getScan()) //
                        && (l >= ccrtChunks.length || crngChunks[j].getScan() < ccrtChunks[l].getScan())) {
                    ColorCycle cc = decodeCRNG(crngChunks[j]);
                    if (cc != null) {
                        mis.addColorCycle(cc);
                        if (cc.isActive()) {
                            activeCycles++;
                        }
                    }
                    j++;
                } else if (k < drngChunks.length //
                        && (l >= ccrtChunks.length || drngChunks[k].getScan() < ccrtChunks[l].getScan())) {
                    ColorCycle cc = decodeDRNG(drngChunks[k]);
                    if (cc != null) {
                        mis.addColorCycle(cc);
                        if (cc.isActive()) {
                            activeCycles++;
                        }
                    }
                    k++;
                } else {
                    ColorCycle cc = decodeCCRT(ccrtChunks[l]);
                    if (cc != null) {
                        mis.addColorCycle(cc);
                        if (cc.isActive()) {
                            activeCycles++;
                        }
                    }
                    l++;
                }
            }
            if (activeCycles > 0) {
                mis.setAnimated(true);
                props.put("colorCycling", activeCycles);
            }

            sources.add(mis);
        }
        if (bitmapSources != null) {
            bitmapSources.add(bodyBitmap.clone());
        }
    }

//    /**
//     * Decodes the bitmap header (ILBM BMHD).
//     *
//     * <pre>
//     * typedef UBYTE Masking; // Choice of masking technique
//     *
//     * #define mskNone                 0
//     * #define mskHasMask              1
//     * #define mskHasTransparentColor  2
//     * #define mskLasso                3
//     *
//     * typedef UBYTE Compression; // Choice of compression algorithm
//     *     // applied to the rows of all source and mask planes.
//     *     // "cmpByteRun1" is the byte run encoding. Do not compress
//     *     // accross rows!
//     * #define cmpNone      0
//     * #define cmpByteRun1  1
//     *
//     * typedef struct {
//     *   UWORD       w, h; // raster width & height in pixels
//     *   WORD        x, y; // pixel position for this image
//     *   UBYTE       nbPlanes; // # source bitplanes
//     *   Masking     masking;
//     *   Compression compression;
//     *   UBYTE       pad1;     // unused; ignore on read, write as 0
//     *   UWORD       transparentColor; // transparent "color number" (sort of)
//     *   UBYTE       xAspect, yAspect; // pixel aspect, a ratio width : height
//     *   WORD        pageWidth, pageHeight; // source "page" size in pixels
//     *   } BitmapHeader;
//     * </pre>
//     */
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

    /**
     * Decodes the CAMG Chunk.
     * Requires data from BMHD Chunk.
     */
    protected void decodeCAMG(IFFChunk chunk)
            throws ParseException {
        camg = 0;

        if (chunk != null) {
            try {
                MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));

                camg = in.readLONG();

                in.close();
            } catch (IOException e) {
                throw new ParseException(e.toString());
            }
        }

        // Extract color mode bits
        switch (camg & (HAM_MASK | EHB_MASK)) {
            case EXTRAHALFBRITE_KEY:
                camgMode = MODE_EHB;
                break;
            case HAM_KEY:
                if (bmhdNbPlanes == 6) {
                    camgMode = MODE_HAM6;
                } else {
                    if (bmhdNbPlanes == 8) {
                        camgMode = MODE_HAM8;
                    } else {
                        throw new ParseException("unsupported Ham Mode with " + bmhdNbPlanes + " bitplanes");
                    }
                }
                break;
            default:
                if (bmhdNbPlanes <= 8) {
                    camgMode = MODE_INDEXED_COLORS;
                } else {
                    camgMode = MODE_DIRECT_COLORS;
                }
        }
    }

    protected void decodeCMAP(IFFChunk chunk, boolean is4BitsPerChannel)
            throws ParseException {
        byte[] red;
        byte[] green;
        byte[] blue;
        byte[] alpha;
        int size = 0;
        int colorsToRead = 0;

        if (chunk == null && camgMode != MODE_DIRECT_COLORS) {
            if (camgMode == MODE_INDEXED_COLORS) {
                size = 1 << bmhdNbPlanes;
                red = new byte[size];
                green = new byte[size];
                blue = new byte[size];
                for (int i = 0; i < size; i++) {
                    red[i] = green[i] = blue[i] = (byte) ((i / (float) (size - 1)) * 255);
                }
                cmapColorModel = new IndexColorModel(8, size, red, green, blue);
                return;
            } else {
                if (size == 0) {
                    throw new ParseException("No CMAP, not supported for this CAMG mode.");
                }
            }
        }

        switch (camgMode) {
            case MODE_EHB:
                size = ((bmhdMasking & MSK_HAS_MASK) != 0) ? 128 : 64;
                colorsToRead = Math.min(32, (int) chunk.getSize() / 3);
                break;
            case MODE_HAM6:
            case MODE_HAM8:
                if ((bmhdMasking & MSK_HAS_MASK) != 0) {
                    throw new ParseException("Masking for HAM not supported");
                }
                size = 1 << (bmhdNbPlanes - 2);
                colorsToRead = Math.min(size, (int) chunk.getSize() / 3);
                break;
            case MODE_INDEXED_COLORS:
                size = ((bmhdMasking & MSK_HAS_MASK) != 0) ? 2 << bmhdNbPlanes : 1 << bmhdNbPlanes;
                colorsToRead = Math.min(size, (int) chunk.getSize() / 3);
                break;
            case MODE_DIRECT_COLORS:
                if (bmhdMasking != MSK_NONE) {
                    throw new ParseException("Masking for true color not supported");
                }
                /* MASKING NOT (YET) SUPPORTED
                if (bmhdMasking == MSK_HAS_TRANSPARENT_COLOR) {
                cmapColorModel = new DirectColorModel(25,0x00ff0000,0x0000ff00,0x000000ff,0x01000000);
                } else {
                cmapColorModel = new DirectColorModel(24,0x00ff0000,0x0000ff00,0x000000ff);
                }*/
                cmapColorModel = new DirectColorModel(24, 0x00ff0000, 0x0000ff00, 0x000000ff);
                return;
            default:
                throw new ParseException("Unsupported CAMG mode :" + camgMode);
        }


        red = new byte[size];
        green = new byte[size];
        blue = new byte[size];

        byte[] data = chunk.getData();
        int j = 0;
       if (is4BitsPerChannel) {
            for (int i = 0; i < colorsToRead; i++) {
                red[i] = (byte) (data[j] & 0xf0 | ((data[j] & 0xf0) >>> 4));
                green[i] = (byte) (data[j + 1] & 0xf0 | ((data[j + 1] & 0xf0) >>> 4));
                blue[i] = (byte) (data[j + 2] & 0xf0 | ((data[j + 2] & 0xf0) >>> 4));
                j += 3;
            }
        } else {

            for (int i = 0; i < colorsToRead; i++) {
                red[i] = data[j++];
                green[i] = data[j++];
                blue[i] = data[j++];
            }
        }

        int transparentColorIndex = ((bmhdMasking & MSK_HAS_TRANSPARENT_COLOR) != 0) ? bmhdTransparentColor : -1;

        switch (camgMode) {
            case MODE_EHB:
                j = 32;
                for (int i = 0; i < 32; i++, j++) {
                    red[j] = (byte) ((red[i] & 255) / 2);
                    green[j] = (byte) ((green[i] & 255) / 2);
                    blue[j] = (byte) ((blue[i] & 255) / 2);
                }
                if ((bmhdMasking & MSK_HAS_MASK) != 0) {
                    System.arraycopy(red, 0, red, 64, 64);
                    System.arraycopy(green, 0, green, 64, 64);
                    System.arraycopy(green, 0, green, 64, 64);
                    alpha = new byte[128];
                    for (int i = 0; i < 64; i++) {
                        alpha[i] = (byte) 0xff;
                    }
                    cmapColorModel = new IndexColorModel(8, 64, red, green, blue, alpha);
                } else {
                    cmapColorModel = new IndexColorModel(8, 64, red, green, blue, transparentColorIndex);
                }
                break;
            case MODE_HAM6:
                cmapColorModel = new AmigaHAMColorModel(AmigaHAMColorModel.HAM6, 16, red, green, blue, false);
                break;
            case MODE_HAM8:
                cmapColorModel = new AmigaHAMColorModel(AmigaHAMColorModel.HAM8, 64, red, green, blue, false);
                break;
            case MODE_INDEXED_COLORS:
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
                break;
        }
    }

    /**
     * Decodes the color cycling range and timing chunk (ILBM CCRT).
     *
     * <pre>
     * enum {
     *     dontCycle = 0, forward = 1, backwards = -1
     * } ccrtDirection;
     * typedef struct {
     *   WORD enum ccrtDirection direction;  // 0=don't cycle, 1=forward, -1=backwards
     *   UBYTE start;      // range lower
     *   UBYTE end;        // range upper
     *   ULONG  seconds;    // seconds between cycling
     *   ULONG  microseconds; // msecs between cycling
     *   WORD  pad;        // future exp - store 0 here
     * } ilbmColorCyclingRangeAndTimingChunk;
     * </pre>
     */
    protected ColorCycle decodeCCRT(IFFChunk chunk)
            throws ParseException {
        ColorCycle cc;
        try {
            MC68000InputStream in = new MC68000InputStream(new ByteArrayInputStream(chunk.getData()));

            int direction = in.readWORD();
            int start = in.readUBYTE();
            int end = in.readUBYTE();
            long seconds = in.readULONG();
            long microseconds = in.readULONG();
            int pad = in.readWORD();
            cc = new CRNGColorCycle(1000000/(int)(seconds*1000+microseconds/1000), 1000, start, end,//
                    direction==1||direction==-1, //
                    direction==1, camgMode == MODE_EHB);

            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
        return cc;
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
            cc = new CRNGColorCycle(rate, 273, low, high,//
                    (flags & 1) != 0 && rate > 36 && high > low, //
                    (flags & 2) != 0, camgMode == MODE_EHB);

            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
        return cc;
    }

    /**
     * Decodes the DPaint IV enhanced color cycle chunk (ILBM DRNG)
     * <p>
     * The RNG_ACTIVE flag is set when the range is cyclable. A range should
     * only have the RNG _ACTIVE if it:
     * <ol>
     * <li>contains at least one color register</li>
     * <li>has a defined rate</li>
     * <li>has more than one color and/or color register</li>
     * </ol>
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

            cc = new DRNGColorCycle(rate, 273, min, max, //
                    (flags & 1) != 0 && rate > 36 && min <= max && ntrue + nregs > 1,//
                    camgMode == MODE_EHB, cells);

            in.close();
        } catch (IOException e) {
            throw new ParseException(e.toString());
        }
        return cc;
    }

    protected void decodeBODY(IFFChunk chunk)
            throws ParseException {
        if ((bmhdMasking & MSK_HAS_MASK) != 0) {
            bodyBitmap = new AmigaBitmapImage(bmhdWidth, bmhdHeight, bmhdNbPlanes + 1, cmapColorModel);
        } else {
            bodyBitmap = new AmigaBitmapImage(bmhdWidth, bmhdHeight, bmhdNbPlanes, cmapColorModel);
        }

        byte[] data = chunk.getData();

        switch (bmhdCompression) {
            case CMP_NONE:
                System.arraycopy(data, 0, bodyBitmap.getBitmap(), 0, data.length);
                break;
            case CMP_BYTE_RUN_1:
                unpackByteRun1(data, bodyBitmap.getBitmap());
                break;
            case CMP_VERTICAL:
                unpackVertical(data, bodyBitmap);
                break;
            default:
                throw new ParseException("unknown compression method: " + bmhdCompression);
        }
    }

    /**
     * ByteRun1 run decoder.
     * <p>
     * The run encoding scheme by <em>byteRun1</em> is best described by pseudo
     * code for the decoder <em>Unpacker</em> (called <em>UnPackBits</em> in
     * the Macintosh toolbox.
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

    /**
     * Vertical run decoder.
     * <p>
     * Each plane is stored in a separate VDAT chunk.
     * <p>
     * A VDAT chunk consists of an id, a length, and a body.
     * <pre>
     * struct {
     *    uint16 id;  // The 4 ASCII characters "VDAT"
     *    uint16 length,
     *    byte[length] body
     * }
     * </pre>
     * The body consists of a command list and a data list.
     * <pre>
     * struct {
     *    uint16         cnt;        // Command count + 2
     *    uint8[cnt - 2] cmd;        // The commands
     *    uint16[]       data;       // Data words
     * }
     * </pre>
     * Pseudo code for the unpacker:
     * <pre>
     * UnPacker:
     *  Read cnt;
     *  LOOP cnt - 2 TIMES
     *      Read the next command byte into cmd
     *      SELECT cmd FROM
     *          0 =&gt;
     *                  Read the next data word into n
     *                  Copy the next n data words literally
     *          1    =&gt;
     *                  Read the next data word into n
     *                  Replicate the next data word n times
     *          [2..127] =&gt;
     *                  Replicate the next data word cmd times
     *          [-1..-128] =&gt;
     *                  Copy the next -cmd data words literally
     *      ENDCASE;
     *      IF end of data reached THEN EXIT END;
     *   ENDLOOP;
     * </pre>
     *
     */
    public void unpackVertical(byte[] in, AmigaBitmapImage bm)
            throws ParseException {
        byte[] out = bm.getBitmap();
        int iIn = 0; // input index
        int endOfData = 0;
        byte buf[] = new byte[bmhdWidth * bmhdHeight / 8]; // temporary bitplane buffer
        int scanlineStride = bm.getScanlineStride();
        int columnCount = (bmhdWidth / 8) * bmhdHeight;
        int columnStride = bmhdHeight * 2;


        try {
            for (int p = 0; p < bmhdNbPlanes; p++) {
                // Each plane is stored in a separate VDAT chunk.
                // ----------------------------------------------
                int iBuf = 0;
                iIn = endOfData;

                // read the "VDAT" chunk id and length
                int id = (in[iIn++] & 0xff) << 24 | (in[iIn++] & 0xff) << 16 | (in[iIn++] & 0xff) << 8 | (in[iIn++] & 0xff);
                if (id != VDAT_ID) {
                    throw new ParseException("Illegal VDAT chunk ID:" + IFFParser.idToString(id) + " at " + (iIn - 4));
                }
                long length = (in[iIn++] & 0xffL) << 24 | (in[iIn++] & 0xffL) << 16 | (in[iIn++] & 0xffL) << 8 | (in[iIn++] & 0xffL);
                if (iIn + length > in.length) {
                    throw new ParseException("Illegal VDAT chunk length:" + length + " at " + (iIn - 4));
                }
                endOfData += length + 8;

                // The body consists of a command list and a data list.
                // ----------------------------------------------------

                // read the command count, compute the offset to the data list
                int cnt = (in[iIn++] & 0xff) << 8 | (in[iIn++] & 0xff);
                int iCmd = iIn;
                iIn = iIn + cnt - 2;
                try {
                    // Process the commands until all commands read or end of data reached
                    for (int i = cnt - 2; i > 0 && iIn < endOfData; i--) {
                        int cmd = in[iCmd++];
                        if (cmd == 0) {
                            // 0 => Read the next data word into n
                            //      Copy the next n data words literally
                            int n = (in[iIn++] & 0xff) << 8 | (in[iIn++] & 0xff);
                            for (n *= 2; n > 0; n--) {
                                buf[iBuf++] = in[iIn++];
                            }
                        } else if (cmd == 1) {
                            // 1 => Read the next data word into n
                            //      Replicate the next data word n times
                            int n = (in[iIn++] & 0xff) << 8 | (in[iIn++] & 0xff);
                            byte dhigh = in[iIn++]; // high byte
                            byte dlow = in[iIn++]; // low byte
                            for (; n > 0; n--) {
                                buf[iBuf++] = dhigh;
                                buf[iBuf++] = dlow;
                            }
                        } else if (cmd >= 2) {
                            // [2..127] => Replicate the next data word cmd times
                            byte dhigh = in[iIn++]; // high byte
                            byte dlow = in[iIn++]; // low byte
                            for (int n = cmd; n > 0; n--) {
                                buf[iBuf++] = dhigh;
                                buf[iBuf++] = dlow;
                            }
                        } else {
                            // [-1..-128] => Copy the next -cmd data words literally
                            for (int n = cmd * -2; n > 0; n--) {
                                buf[iBuf++] = in[iIn++];
                            }
                        }

                    }
                } catch (IndexOutOfBoundsException e) {
                    System.err.println("IndexOutOfBounds in bitplane " + p);
                    e.printStackTrace();
                }

                // Copy buffer into bitmap
                int bitplaneOffset = bm.getBitplaneStride() * p;
                for (int xBuf = 0, xOut = 0; xBuf < columnCount; xBuf += columnStride, xOut += 2) {
                    for (int yBuf = 0, yOut = bitplaneOffset; yBuf < columnStride; yBuf += 2, yOut += scanlineStride) {
                        out[xOut + yOut] = buf[xBuf + yBuf];
                        out[xOut + 1 + yOut] = buf[xBuf + 1 + yBuf];
                    }
                }
            }


        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }
    /*
    /* Normal identifiers. * /
    public final static int DEFAULT_MONITOR_ID = 0x00000000;
    public final static int NTSC_MONITOR_ID = 0x00011000;
    public final static int PAL_MONITOR_ID = 0x00021000;
    public final static int LORES_MASK  = 0x00000000;
    public final static int LACE_MASK  = 0x00000004;
    public final static int HIRES_MASK  = 0x00008000;
    public final static int SUPER_MASK  = 0x00008020;
    public final static int HAM_MASK    = 0x00000800;
    public final static int DPF_MASK    = 0x00000400;
    public final static int DPF2_MASK  = 0x00000440;
    public final static int EHB_MASK    = 0x00000080;

    /*
    The following 20 composite keys are for Modes on the default Monitor.
    NTSC & PAL "flavours" of these particular keys may be made by or'ing
    the NTSC or PAL MONITOR_ID with the desired MODE_KEY.
     * /
    public final static int LORES_KEY      = 0x00000000; // NTSC:320*200,44x52,50fps  PAL:320x256,44x44,60fps
    public final static int HIRES_KEY      = 0x00008000; // NTSC:640*200,22x52  PAL:640*256,22x44
    public final static int SUPER_KEY      = 0x00008020; // NTSC:1280*200,11x52  PAL:1280x256,11x44
    public final static int HAM_KEY        = 0x00000800; // NTSC:320*200,44x52  PAL:320x256,44x44
    public final static int LORESLACE_KEY    = 0x00000004; // NTSC:320*400,44x26 PAL:320x512,44x22
    public final static int HIRESLACE_KEY    = 0x00008004; // NTSC:640*400,22x26  PAL:640x512,22x22
    public final static int SUPERLACE_KEY    = 0x00008024; // NTSC:1280*400,11x26  PAL:1280x512,11x22
    public final static int HAMLACE_KEY      = 0x00000804; // NTSC:320*400,44x26  PAL:320x512,44x22
    public final static int LORESDPF_KEY      = 0x00000400; // 320*240,256
    public final static int HIRESDPF_KEY      = 0x00008400; // 640*240,256
    public final static int SUPERDPF_KEY      = 0x00008420; // 1280*240,256
    public final static int LORESLACEDPF_KEY    = 0x00000404; // 320*480,512
    public final static int HIRESLACEDPF_KEY    = 0x00008404; // 640*480,512
    public final static int SUPERLACEDPF_KEY    = 0x00008424; // 1280*480,512
    public final static int LORESDPF2_KEY    = 0x00000440; // 320*240,256
    public final static int HIRESDPF2_KEY    = 0x00008440; // 640*240,256
    public final static int SUPERDPF2_KEY    = 0x00008460; // 1280*240,256
    public final static int LORESLACEDPF2_KEY  = 0x00000444; // 320*480,512
    public final static int HIRESLACEDPF2_KEY  = 0x00008444; // 640*480,512
    public final static int SUPERLACEDPF2_KEY  = 0x00008464; // 1280*480,512
    public final static int EXTRAHALFBRITE_KEY  = 0x00000080; // NTSC:320*200,44x52  PAL:320*256,44x44
    public final static int EXTRAHALFBRITELACE_KEY  = 0x00000084; // NTSC:320*400,44x26  PAL:320*512,44x22

    /* VGA identifiers. * /
    public final static int VGA_MONITOR_ID  = 0x00031000;
    public final static int VGALACE_MASK    = 0x00000001;
    public final static int VGALORES_MASK  = 0x00008000;

    public final static int VGAEXTRALORES_KEY    = 0x00031004; // 160*480 v 88x22
    public final static int VGALORES_KEY        = 0x00039004; // 320*480 v 44x22
    public final static int VGAPRODUCT_KEY      = 0x00039024; // 640*480 v 22x22
    public final static int VGAHAM_KEY        = 0x00031804; //
    public final static int VGAEXTRALORESLACE_KEY  = 0x00031005; // 160*960 v 88x11
    public final static int VGALORESLACE_KEY      = 0x00039005; // 320*960 v 44x11
    public final static int VGAPRODUCTLACE_KEY    = 0x00039025; // 640*960 v 22x11
    public final static int VGAHAMLACE_KEY      = 0x00031805; //
    public final static int VGAEXTRALORESDPF_KEY    = 0x00031404; //
    public final static int VGALORESDPF_KEY      = 0x00039404;
    public final static int VGAPRODUCTDPF_KEY    = 0x00039424;
    public final static int VGAEXTRALORESLACEDPF_KEY  = 0x00031405;
    public final static int VGALORESLACEDPF_KEY    = 0x00039405;
    public final static int VGAPRODUCTLACEDPF_KEY  = 0x00039425; //
    public final static int VGAEXTRALORESDPF2_KEY  = 0x00031444;
    public final static int VGALORESDPF2_KEY      = 0x00039444;
    public final static int VGAPRODUCTDPF2_KEY    = 0x00039464;
    public final static int VGAEXTRALORESLACEDPF2_KEY  = 0x00031445;
    public final static int VGALORESLACEDPF2_KEY    = 0x00039445;
    public final static int VGAPRODUCTLACEDPF2_KEY  = 0x00039465; // 640*960
    public final static int VGAEXTRAHALFBRITE_KEY  = 0x00031084;
    public final static int VGAEXTRAHALFBRITELACE_KEY = 0x00031085;

    /* A2024 identifiers. * /
    public final static int A2024_MONITOR_ID = 0x00041000;

    public final static int A2024TENHERTZ_KEY    = 0x00041000;
    public final static int A2024FIFTEENHERTZ_KEY  = 0x00049000;

    /* Proto identifiers. * /
    public final static int PROTO_MONITOR_ID = 0x00051000;

    /* Euro identifiers * /
    public final static int EURO36_MONITOR_ID = 0x00071000;

    public final static int EURO36EXTRAHALFBRITE_KEY  = 0x00071080; // 320*200 v 44x44
    public final static int EURO36EXTRAHALFBRITELACE_KEY = 0x00071084; // 320*400 v 44x22
    public final static int EURO36HAM_KEY      = 0x00071800; // 320*200 v 44x44
    public final static int EURO36HAMLACE_KEY    = 0x00071804; // 320*400 v 44x22
    public final static int EURO36HIRES_KEY      = 0x00079000; // 640*200 v 22x44
    public final static int EURO36HIRESLACE_KEY    = 0x00079004; // 640*400 v 22x22
    public final static int EURO36LORES_KEY      = 0x00071000; // 320*200 v 44x44
    public final static int EURO36LORESLACE_KEY    = 0x00071004; // 320*400 v 44x22
    public final static int EURO36SUPERHIRES_KEY    = 0x00079020; // 1280*200 v 11x44
    public final static int EURO36SUPERHIRESLACE_KEY  = 0x00079024; // 1280*400 v 11x44
    public final static int EURO72ECS_KEY      = 0x00069004; // 320*400 v 44x22
    public final static int EURO72ECSLACE_KEY    = 0x00069005; // 320*800 v 44x11
    public final static int EURO72PRODUCT_KEY    = 0x00069024; // 640*400 v 22x22
    public final static int EURO72PRODUCTLACE_KEY  = 0x00069025; // 640*800 v 22x11


    public final static int EURO_KEY = 0x00071000;
     */
}

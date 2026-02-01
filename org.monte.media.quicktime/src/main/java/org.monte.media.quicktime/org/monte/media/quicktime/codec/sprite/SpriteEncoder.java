/*
 * @(#)SpriteEncoder.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.sprite;

import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.Registry;
import org.monte.media.io.ByteArrayImageOutputStream;
import org.monte.media.qtff.AtomOutputStream;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.monte.media.av.BufferFlag.DISCARD;
import static org.monte.media.av.FormatKeys.DataClassKey;
import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.KeyFrameIntervalKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_RAW;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;
import static org.monte.media.quicktime.codec.sprite.SpriteFormatKeys.DATA_CLASS_JAVA_SPRITE;
import static org.monte.media.quicktime.codec.sprite.SpriteFormatKeys.ENCODING_JAVA_SPRITE;
import static org.monte.media.quicktime.codec.sprite.SpriteFormatKeys.ENCODING_QUICKTIME_SPRITE;
import static org.monte.media.quicktime.codec.sprite.SpriteFormatKeys.SpriteImageEncodingKey;

/// Codec for [SpriteSample]s.
///
///
/// A sample can either be a key frame or an overriding frame:
/// <dl>
///     <dt>key frame</dt>
///     <dd>A key frame contains a shared data atom of type 'dflt' and one or
///         more sprite atoms of type 'sprt'.
///      </dd>
///      <dt>differenced frame</dt>
///      <dd>An differenced frame contains one or more sprite atoms of type 'sprt'.</dd>
///
/// Atoms:
///
/// The shared data atom 'dflt' contains a sprite image container atom of type 'imct' and ID=1.
///
/// The sprite image container atom 'imct' stores one or more sprite image atoms of type 'imag'.
///
/// The sprite image atoms 'imag' should have ID numbers starting at 1 and counting consecutively upward.
/// Each sprite image atom contains an image sample description
/// immediately followed by the sprite’s compressed image data.
///
/// Sprite atoms 'sprt' should have ID numbers start at 1 and count consecutively upward.
/// Each sprite atom contains a list of Sprite properties.
/// In a key frame, the following properties are required: Image index, Matrix, Layer, and Visibility.
/// In a differenced frame, those sprite properties that change need to be specified.
/// If none of a sprite's properties change in a given frame, then the sprite does not need an atom in the
/// differenced frame.
/// <pre>
/// +------------------------------------------------------+
/// | 'sean' Atom                                          |
/// +------------------------------------------------------+
/// | struct header                                        |
/// |   +-------------------------------------------+      |
/// |   | 'dflt' Atom                               |      |
/// |   +-------------------------------------------+      |
/// |   | struct header                             |      |
/// |   | +---------------------------------------+ |      |
/// |   | | 'imct' Atom                           | |      |
/// |   | +---------------------------------------+ |      |
/// |   | | struct header                         | |      |
/// |   | | +-------------------------------+     | |      |
/// |   | | | 'imag' Atom                   | …   | |      |
/// |   | | +-------------------------------+     | |      |
/// |   | | | struct header                 |     | |      |
/// |   | | | +---------------------------+ |     | |      |
/// |   | | | | 'imda' Atom               | |     | |      |
/// |   | | | +---------------------------+ |     | |      |
/// |   | | | | struct imageDescription   | |     | |      |
/// |   | | | | +-----------------------+ | |     | |      |
/// |   | | | | | 'idat' Atom           | | |     | |      |
/// |   | | | | +-----------------------+ | |     | |      |
/// |   | | | | | byte[] imageData      | | |     | |      |
/// |   | | | | +-----------------------+ | |     | |      |
/// |   | | | +---------------------------+ |     | |      |
/// |   | | +-------------------------------+     | |      |
/// |   | +---------------------------------------+ |      |
/// |   +------------------------------------------+       |
/// |                                                      |
/// |   +--------------------------------------------+     |
/// |   | 'sprt' Atom                                | …   |
/// |   +--------------------------------------------+     |
/// |   | struct header                              |     |
/// |   | +------------------------------------+     |     |
/// |   | | property                           | …   |     |
/// |   | +------------------------------------+     |     |
/// |   +--------------------------------------------+     |
/// |                                                      |
/// +------------------------------------------------------+
///
/// typedef struct {
///     uint32 id;
///     uint16 reserved, set to 0;
///     uint16 childCount;
///     uint32 reserved, set to 0;
/// } header;
///
/// typedef struct {
///     uint32 image description size, set to 86 bytes;
///     magic4 Compressor identifier, 'jpeg'
///     uint32 reserved, set to 0;
///     uint16 reserved, set to 0;
///     uint16 reserved, set to 0;
///     uint16 Major version of this data, 0 if not applicable
///     uint16 Minor version of this data, 0 if not applicable
///     magic4 Vendor who compressed this data, 'appl'
///     uint32 Temporal quality, 0 (no temporal compression)
///     uint32 Spatial quality, codecNormalQuality 0x000002000 'codecNormalQuality'
///     uint16 imageWidth
///     uint16 imageHeight
///     fixed1616 horizontal resolution, 72 dpi
///     fixed1616 vertical resolution, 72 dpi
///     uint32 data size, (use 0 if unknown)
///     uint16 frameCount, 1
///     pascal32 fCompressor name, "Photo - JPEG" (32-byte Pascal string)
///     uint16 Image bit depth, 24
///     int16 Color lookup table ID, -1 (none)
/// } imageDescription;
///
/// Sprite properties:
///     Property name                        Value   Leaf data type
///     kSpritePropertyMatrix                    1   struct TransformationMatrix
///     kSpritePropertyVisible                   4   short
///     kSpritePropertyLayer                     5   short
///     kSpritePropertyGraphicsMode              6   ModifierTrackGraphicsModeRecord
///     kSpritePropertyActionHandlingSpriteID    8   short
///     kSpritePropertyImageIndex              100   short
///
/// Sprite track properties:
///     Atom type                                     Atom ID  Leaf data type
///     kSpriteTrackPropertyBackgroundColor           1        RGBColor
///     kSpriteTrackPropertyOffscreenBitDepth         1        unsigned short
///     kSpriteTrackPropertySampleFormat              1        long
///     kSpriteTrackPropertyHasActions                1        Boolean
///     kSpriteTrackPropertyQTIdleEventsFrequency     1        UInt32
///     kSpriteTrackPropertyVisible                   1        Boolean
///     kSpriteTrackPropertyScaleSpritesToScaleWorld  1        Boolean
///
///  TransformationMatrix:
///    The transformation matrix [a,b,u;c,d,v;x,y,w].
///               [a b u;
///    [x y 1] *   c d v; = [x' y'1]
///                x y w]
///    stored in the sequence: a b u c d v x y w
///     a    scale/rotate a, 32-bit fixed-point number divided as 16.16
///     b    skew/rotate b,  32-bit fixed-point number divided as 16.16
///     u    zero,           32-bit fixed-point number divided as 2.30
///     c    skew/rotate c,  32-bit fixed-point number divided as 16.16
///     d    scale/rotate d, 32-bit fixed-point number divided as 16.16
///     v    zero,           32-bit fixed-point number divided as 2.30
///     x    translate x,    32-bit fixed-point number divided as 16.16
///     y    translate y,    32-bit fixed-point number divided as 16.16
///     w    one,            32-bit fixed-point number divided as 2.30
///
/// </pre>
///
/// References:
///
/// Sprite Sample Data
/// :
///         "QuickTime File Format Specification", Apple Inc. 2010-08-03. (qtff)
///          [
///          http://developer.apple.com/library/mac/documentation/QuickTime/QTFF/qtff.pdf
///          ](http://developer.apple.com/library/mac/documentation/QuickTime/QTFF/qtff.pdf/)
///
///
public class SpriteEncoder extends org.monte.media.av.AbstractCodec {
    private static final String SPRITE_PROPERTY_MATRIX_TYPE = "\0\0\0\u0001";
    private static final String SPRITE_PROPERTY_VISIBLE_TYPE = "\0\0\0\u0004";
    private static final String SPRITE_PROPERTY_LAYER_TYPE = "\0\0\0\u0005";
    private static final String SPRITE_PROPERTY_IMAGE_INDEX_TYPE = "\0\0\0\u0064";
    private static final String ATOM_TYPE_SEAN = "sean";
    private static final String ATOM_TYPE_DFLT = "dflt";
    private static final String ATOM_TYPE_IMCT = "imct";
    private static final String ATOM_TYPE_IMAG = "imag";
    private static final String ATOM_TYPE_IMDA = "imda";
    private static final String ATOM_TYPE_NAME = "name";
    private static final String PNG_COMPRESSOR_ID = ENCODING_QUICKTIME_PNG;
    private static final String JAVA_VENDOR_ID = "java";
    private static final String ATOM_TYPE_SPRT = "sprt";
    private int frameCounter;
    private SpriteSample previousSpriteSample;

    public SpriteEncoder() {
        super(new Format[]{
                new Format(MediaTypeKey, FormatKeys.MediaType.SPRITE, MimeTypeKey, MIME_JAVA,
                        EncodingKey, ENCODING_JAVA_SPRITE, DataClassKey, DATA_CLASS_JAVA_SPRITE), //
        }, new Format[]{
                new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                        EncodingKey, ENCODING_BUFFERED_IMAGE, DataClassKey, BufferedImage.class), //
                new Format(MediaTypeKey, FormatKeys.MediaType.SPRITE, MimeTypeKey, MIME_QUICKTIME,
                        EncodingKey, ENCODING_QUICKTIME_SPRITE, DataClassKey, byte[].class
                ),
        });
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        out.format = outputFormat;
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        try {
            if (outputFormat.get(EncodingKey).equals(ENCODING_JAVA_SPRITE)) {
                out.setFlag(DISCARD);
                return CODEC_FAILED;
            } else if (outputFormat.get(EncodingKey).equals(ENCODING_BUFFERED_IMAGE)) {
                out.setFlag(DISCARD);
                return CODEC_FAILED;
            } else {
                return encodeSprite(in, out);
            }
        } catch (Throwable e) {
            out.setFlag(DISCARD);
            out.setException(e);
            return CODEC_FAILED;
        }
    }


    /// Encode [SpriteSample] to byte array.
    ///
    /// @param in     input buffer
    /// @param outBuf output buffer
    /// @return status code
    private int encodeSprite(Buffer in, Buffer outBuf) {

        boolean isKeyFrame = previousSpriteSample == null || frameCounter % outputFormat.get(KeyFrameIntervalKey, 60) == 0;
        String spriteImageEncodingType = outputFormat.get(SpriteImageEncodingKey, ENCODING_QUICKTIME_RAW);

        outBuf.setFlag(BufferFlag.KEYFRAME, isKeyFrame);
        SpriteSample spriteSample = (SpriteSample) in.data;

        try (var bOut = new ByteArrayImageOutputStream(outBuf.data instanceof byte[] b ? b : new byte[0], 0, 0, ByteOrder.BIG_ENDIAN);
             var out = new AtomOutputStream(bOut)) {
            writeSeanAtom(out, isKeyFrame, spriteSample, spriteImageEncodingType);
            out.close();
            outBuf.data = bOut.getBuffer();
            outBuf.length = bOut.size();
        } catch (IOException e) {
            outBuf.exception = e;
            outBuf.setFlag(DISCARD);
            return CODEC_FAILED;
        }

        previousSpriteSample = spriteSample.clone();
        frameCounter++;
        return CODEC_OK;
    }

    private void writeSeanAtom(AtomOutputStream out, boolean isKeyFrame, SpriteSample spriteSample, String spriteImageEncodingType) throws IOException {

        List<Sprite> changedSprites = new ArrayList<>();
        if (previousSpriteSample == null || isKeyFrame) {
            changedSprites = new ArrayList<>(spriteSample.sprites.values());
        } else {
            for (Sprite s : spriteSample.sprites.values()) {
                var prevS = previousSpriteSample.sprites.get(s.spriteId());
                if (!s.equals(prevS)) {
                    changedSprites.add(s);
                }
            }
        }

        out.pushAtom(ATOM_TYPE_SEAN);
        out.writeUInt(1);//id must be 1
        out.writeUShort(0);//reserved must be 0
        out.writeUShort(isKeyFrame ? 1 + spriteSample.sprites.size() : changedSprites.size());//childCount
        out.writeUInt(0);//reserved must be 0
        if (isKeyFrame) {
            writeDfltAtom(out, isKeyFrame, spriteSample, spriteImageEncodingType);
        }

        for (var sprite : changedSprites) {
            writeSprtAtom(out, sprite, isKeyFrame);
        }
        out.popAtom();
    }

    private void writeDfltAtom(AtomOutputStream out, boolean isKeyFrame, SpriteSample spriteSample, String spriteImageEncodingType) throws IOException {
        out.pushAtom(ATOM_TYPE_DFLT);
        // Write the header
        out.writeUInt(1);//id must be 1
        out.writeUShort(0);//reserved must be 0
        out.writeUShort(1);//childCount
        out.writeUInt(0);//reserved must be 0
        writeImctAtom(out, spriteSample.images, spriteImageEncodingType);
        out.popAtom();
    }

    private void writeImctAtom(AtomOutputStream out, List<BufferedImage> images, String spriteImageEncodingType) throws IOException {
        out.pushAtom(ATOM_TYPE_IMCT);
        // Write header
        out.writeUInt(1);//id must be 1
        out.writeUShort(0);//reserved must be 0
        out.writeUShort(images.size());//child count
        out.writeUInt(0);//reserved must be 0
        for (int i = 0; i < images.size(); i++) {
            writeImagtAtom(out, i + 1, images.get(i), spriteImageEncodingType);
        }
        out.popAtom();
    }

    private void writeImagtAtom(AtomOutputStream out, int id, BufferedImage img, String spriteImageEncodingType) throws IOException {
        out.pushAtom(ATOM_TYPE_IMAG);
        // Write header
        out.writeUInt(id);//id
        out.writeUShort(0);//reserved must be 0
        out.writeUShort(1);//childCount
        out.writeUInt(0);//reserved must be 0
        writeImdaAtom(out, id, img, spriteImageEncodingType);
        out.popAtom();
    }

    private void writeImdaAtom(AtomOutputStream out, int id, BufferedImage img, String spriteImageEncodingType) throws IOException {
        out.pushAtom(ATOM_TYPE_IMDA);

        // Write header
        out.writeUInt(1);//id must be 1
        out.writeUShort(0);//reserved must be 0
        out.writeUShort(0);//child count must be 0
        out.writeUInt(0);//reserved must be 0

        writeImageDescription(out, img, spriteImageEncodingType);


        Format inputFormat = new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                MimeTypeKey, MIME_JAVA,
                EncodingKey, ENCODING_BUFFERED_IMAGE);
        Format outputFormat = new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO,
                MimeTypeKey, MIME_QUICKTIME,
                EncodingKey, spriteImageEncodingType,
                WidthKey, img.getWidth(), HeightKey, img.getHeight(),
                DepthKey, img.getColorModel().getPixelSize());
        Codec codec = Registry.getInstance().getCodec(inputFormat, outputFormat);
        if (codec == null) {
            throw new IOException("No codec for " + inputFormat);
        }
        Buffer inBuf = new Buffer();
        inBuf.format = inputFormat;
        inBuf.data = img;
        Buffer outBuf = new Buffer();
        codec.process(inBuf, outBuf);
        if (outBuf.data instanceof byte[] b) {
            out.write(b, outBuf.offset, outBuf.length);
        }
        if (outBuf.exception != null) {
            throw outBuf.exception instanceof IOException ioe ? ioe : new IOException("Could not encode the sprite image");
        }


        //ImageIO.write(img, "PNG", out);
        out.popAtom();
    }

    private static void writeImageDescription(AtomOutputStream out, BufferedImage img, String spriteImageEncodingType) throws IOException {
        out.writeUInt(86);//image description size
        out.writeType(spriteImageEncodingType);//compressor identifier
        out.writeUInt(0);//reserved
        out.writeUShort(0);//reserved
        out.writeUShort(0);//reserved
        out.writeUShort(0);//major version
        out.writeUShort(0);//minor version
        out.writeType(JAVA_VENDOR_ID);//vendor who compressed this data
        out.writeUInt(0);//temporal quality
        out.writeUInt(0x000002000);//spatial quality
        out.writeUShort(img.getWidth());//image width
        out.writeUShort(img.getHeight());//image height
        out.writeFixed16D16(72.0);//horizontal resolution 72 dpi
        out.writeFixed16D16(72.0);//vertical resolution 72 dpi
        out.writeUInt(0);//data size, (use 0 if unknown)
        out.writeUShort(1);//frame count, 1
        out.writePString("Photo - PNG", 32);
        out.writeUShort(img.getColorModel().getPixelSize());//image bit depth
        out.writeShort(-1);//color lookup table ID, -1 (none)
    }

    private void writeSprtAtom(AtomOutputStream out, Sprite sprite, boolean keyFrame) throws IOException {
        Sprite previousSprite = null;
        if (previousSpriteSample != null && !keyFrame) {
            previousSprite = previousSpriteSample.sprites.get(sprite.spriteId());
        }
        if (!keyFrame && sprite.equals(previousSprite)) {
            return;
        }
        boolean needsImageIndex = previousSprite == null || previousSprite.imageId() != sprite.imageId();
        boolean needsVisible = previousSprite == null || previousSprite.visible() != sprite.visible();
        boolean needsLayer = previousSprite == null || previousSprite.layer() != sprite.layer();
        boolean needsMatrix = previousSprite == null || !previousSprite.transform().equals(sprite.transform());
        int childCount = (needsImageIndex ? 1 : 0)
                + (needsVisible ? 1 : 0)
                + (needsLayer ? 1 : 0)
                + (needsMatrix ? 1 : 0);
        out.pushAtom(ATOM_TYPE_SPRT);

        // Write the header
        out.writeUInt(sprite.spriteId());//id must greater or equal 1
        out.writeUShort(0);//reserved must be 0
        out.writeUShort(childCount);//childCount
        out.writeUInt(0);//reserved must be 0

        if (needsImageIndex) {
            out.pushAtom(SPRITE_PROPERTY_IMAGE_INDEX_TYPE);
            // Write the header
            out.writeUInt(1);//id must be 1
            out.writeUShort(0);//reserved must be 0
            out.writeUShort(0);//childCount must be 0
            out.writeUInt(0);//reserved must be 0
            // Write the data
            out.writeUShort(sprite.imageId());
            out.popAtom();
        }

        if (needsVisible) {
            out.pushAtom(SPRITE_PROPERTY_VISIBLE_TYPE);
            // Write the header
            out.writeUInt(1);//id must be 1
            out.writeUShort(0);//reserved must be 0
            out.writeUShort(0);//childCount must be 0
            out.writeUInt(0);//reserved must be 0
            // Write the data
            out.writeUShort(sprite.visible() ? 1 : 0);
            out.popAtom();
        }

        if (needsLayer) {
            out.pushAtom(SPRITE_PROPERTY_LAYER_TYPE);
            // Write the header
            out.writeUInt(1);//id must be 1
            out.writeUShort(0);//reserved must be 0
            out.writeUShort(0);//childCount must be 0
            out.writeUInt(0);//reserved must be 0
            // Write the data
            out.writeUShort(sprite.layer());
            out.popAtom();
        }

        if (needsMatrix) {
            out.pushAtom(SPRITE_PROPERTY_MATRIX_TYPE);
            // Write the header
            out.writeUInt(1);//id must be 1
            out.writeUShort(0);//reserved must be 0
            out.writeUShort(0);//childCount must be 0
            out.writeUInt(0);//reserved must be 0
            // Write the data
            double[] m = sprite.transform().getFlatMatrix();
            out.writeFixed16D16(m[0]);//a
            out.writeFixed16D16(m[2]);//b
            out.writeFixed2D30(0);//u
            out.writeFixed16D16(m[1]);//c
            out.writeFixed16D16(m[3]);//d
            out.writeFixed2D30(0);//v
            out.writeFixed16D16(m[4]);//x
            out.writeFixed16D16(m[5]);//y
            out.writeFixed2D30(1);//w
            out.popAtom();
        }

        out.popAtom();
    }
}

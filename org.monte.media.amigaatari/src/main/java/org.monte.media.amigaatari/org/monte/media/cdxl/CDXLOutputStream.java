/*
 * @(#)CDXLOutputStream.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.cdxl;

import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.amigabitmap.AmigaHAMColorModel;

import javax.imageio.stream.ImageOutputStream;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteOrder;

public class CDXLOutputStream extends AbstractCDXLStream implements Closeable {
    private final ImageOutputStream out;
    private int back = 0;
    private int frame = 0;

    public CDXLOutputStream(ImageOutputStream out) {
        this.out = out;
        out.setByteOrder(ByteOrder.BIG_ENDIAN);
    }

    public void write(AmigaBitmapImage image, boolean stereo, int frequency, byte[] audioSamples) throws IOException {
        byte[] video;
        if (image.isInterleaved()) {
            byte[] interleaved = image.getBitmap();
            video = new byte[interleaved.length];
            int bitplaneStride = image.getBitplaneStride();
            int scanlineStride = image.getScanlineStride();
            int height = image.getHeight();
            int depth = image.getDepth();

            int videoIndex = 0;
            for (int plane = 0; plane < depth; plane++) {
                int interleavedIndex = plane * bitplaneStride;
                for (int y = 0; y < height; y++) {
                    System.arraycopy(
                            interleaved, interleavedIndex,
                            video, videoIndex,
                            bitplaneStride);
                    interleavedIndex += scanlineStride;
                    videoIndex += bitplaneStride;
                }
            }
        } else {
            video = image.getBitmap();
            ;
        }
        write(image.getColorModel(), image.getWidth(), image.getHeight(), image.getDepth(), video, stereo, frequency, audioSamples);
    }

    public void write(ColorModel colorModel, int width, int height, int depth, byte[] video, boolean stereo, int frequency, byte[] audio) throws IOException {
        int[] colorMap;
        boolean isHam;
        if (colorModel instanceof AmigaHAMColorModel hamModel) {
            isHam = true;
            hamModel.getRGBs(colorMap = new int[hamModel.getMapSize()]);
        } else if (colorModel instanceof IndexColorModel indexModel) {
            isHam = false;
            indexModel.getRGBs(colorMap = new int[indexModel.getMapSize()]);
        } else {
            throw new IOException("Unsupported colorModel=" + colorModel);
        }
        write(isHam, colorMap, width, height, depth, video, stereo, frequency, audio);
    }


    public void write(boolean isHam, int[] colorMap, int xSize, int ySize, int pixelSize, byte[] video, boolean stereo, int frequency, byte[] audio) throws IOException {
        //Type
        out.writeByte(PanStructureType.CUSTOM.getEncodedValue());
        //Info
        out.writeByte(
                (PixelValueOrientation.PLANES.encode() << 5)
                        | ((stereo ? AudioType.STEREO : AudioType.MONO).encode() << 4)
                        | ((isHam ? VideoType.HAM : VideoType.STANDARD).encode())
        );
        //size
        int size = 32 + colorMap.length * 3 + video.length + audio.length;
        out.writeInt(size);
        //back
        out.writeInt(back);
        //frame
        out.writeInt(frame++);
        //xSize,ySize,reserved
        out.writeShort(xSize);
        out.writeShort(ySize);
        out.writeByte(0);//reserved
        //pixelSize,colorMapSize,audioSize
        out.writeByte(pixelSize);
        out.writeShort(colorMap.length * 3);
        out.writeShort(stereo ? audio.length / 2 : audio.length);
        //AGABlasterExtension (8 bytes)
        out.writeShort(frequency);//frequency 0=unspecified
        out.writeByte(0);//frames per second unspecified

        AGABlasterResolution resolution = AGABlasterResolution.LORES;
        AgaBlasterKillEhb killEhb = AgaBlasterKillEhb.KEEP_EHB_MODE;
        AGABlasterColorMode colorMode = AGABlasterColorMode._24_BIT_COLORS;
        AgaBlasterFrameLengthMode frameLengthMode = AgaBlasterFrameLengthMode.FIXED_LENGTH;

        out.writeByte((frameLengthMode.encode() << 6)
                | (killEhb.encode() << 5)
                | (colorMode.encode() << 4)
                | (resolution.encode())
        );//Info2
        out.writeByte(0);//audio padding=0, video padding=0
        out.writeByte(0);//palette padding=0,padding mode=unspecified
        out.writeShort(0);//reserved

        // data
        for (int color : colorMap) {
            out.writeByte(color >>> 16);
            out.writeShort(color);
        }
        //out.writeInts(colorMap, 0, colorMap.length);
        out.write(video);
        out.write(audio);

        //
        back = size;
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}

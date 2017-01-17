/* @(#)BitmapCodec.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.image;

import ru.sbtqa.monte.media.AbstractVideoCodec;
import ru.sbtqa.monte.media.Buffer;
import ru.sbtqa.monte.media.Codec;
import ru.sbtqa.monte.media.Format;
import ru.sbtqa.monte.media.ilbm.ColorCyclingMemoryImageSource;
import ru.sbtqa.monte.media.ilbm.ILBMDecoder;
import ru.sbtqa.monte.media.pbm.PBMDecoder;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import javax.imageio.ImageIO;
import static ru.sbtqa.monte.media.VideoFormatKeys.*;
import static ru.sbtqa.monte.media.BufferFlag.*;

/**
 * Decodes media data into a {@code Bitmap}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-20 Created.
 */
public class BitmapCodec extends AbstractVideoCodec {
  public BitmapCodec() {
        super(new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BUFFERED_IMAGE), //
                },
                new Format[]{
                    new Format(MediaTypeKey, MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                    EncodingKey, ENCODING_BITMAP_IMAGE, DataClassKey, BitmapImage.class), //
                });
    }
    @Override
    public Format setOutputFormat(Format f) {
        super.setOutputFormat(f);

        // This codec can not scale an image.
        // Enforce these properties
        if (outputFormat != null) {
            if (inputFormat != null) {
                outputFormat = outputFormat.prepend(inputFormat.intersectKeys(WidthKey, HeightKey,DepthKey));
            }
        }
        return this.outputFormat;
    }

    @Override
    public int process(Buffer in, Buffer out) {
        out.setMetaTo(in);
        if (in.isFlag(DISCARD)) {
            return CODEC_OK;
        }
        out.format = outputFormat;
        try {
            out.setFlag(KEYFRAME);

            if (in.data instanceof File) {
                File f = (File) in.data;
                boolean success;
                {
                    InputStream ins = new BufferedInputStream(new FileInputStream(f));
                    try {
                        ILBMDecoder d = new ILBMDecoder(ins);
                        ArrayList<BitmapImage> imgs = d.produceBitmaps();
                        BitmapImage img = imgs.get(0);
                        out.data = img;
                        success = true;
                    } catch (IOException e) {
                        success = false;
                    } finally {
                        ins.close();
                    }
                }
                if (!success) {
                    InputStream ins = new BufferedInputStream(new FileInputStream(f));
                    try {
                        PBMDecoder d = new PBMDecoder(ins);
                        ArrayList<ColorCyclingMemoryImageSource> imgs = d.produce();
                        ColorCyclingMemoryImageSource mis = imgs.get(0);

                        out.data = BitmapImageFactory.toBitmapImage(mis);
                        success = true;
                    } catch (IOException e) {
                        success = false;
                    } finally {
                        ins.close();
                    }
                }
                if (!success) {
                    BufferedImage img = ImageIO.read(f);
                    out.data = BitmapImageFactory.toBitmapImage(img);
                    success = true;
                }
            } else if (in.data instanceof BitmapImage) {
                out.data = in.data;
            } else if (in.data instanceof BufferedImage) {
                out.data = BitmapImageFactory.toBitmapImage((BufferedImage) in.data);
            }
            return CODEC_OK;
        } catch (IOException e) {
            e.printStackTrace();
            out.setFlag(DISCARD);
            return CODEC_FAILED;
        }
    }
}

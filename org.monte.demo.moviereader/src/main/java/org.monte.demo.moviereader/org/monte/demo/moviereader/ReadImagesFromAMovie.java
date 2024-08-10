/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.demo.moviereader;


import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Codec;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.MovieReader;
import org.monte.media.av.Registry;
import org.monte.media.image.Images;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;

/**
 * This example shows how to read all images from the first video track of a movie into a list.
 *
 * @author Werner Randelshofer
 */
public class ReadImagesFromAMovie {

    /**
     * Reads all images from the specified movie file into a list.
     *
     * @param file a movie file
     * @return a list of images
     * @throws IOException on IO failure
     */
    public List<BufferedImage> readImages(File file) throws IOException {
        List<BufferedImage> images = new ArrayList<>();

        try (MovieReader in = Registry.getInstance().getReader(file)) {
            int track = in.findTrack(0, new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO));
            if (track < 0) {
                throw new IOException("Could not find a video track.");
            }
            Codec codec = Registry.getInstance().getCodec(in.getFormat(track), new Format(DataClassKey, BufferedImage.class));
            if (codec == null) {
                throw new IOException("Could not find a codec for BufferedImages.");
            }

            Buffer inbuf = new Buffer();
            Buffer outbuf = new Buffer();
            do {
                in.read(track, inbuf);
                codec.process(inbuf, outbuf);
                if (!outbuf.isFlag(BufferFlag.DISCARD)) {
                    images.add(Images.cloneImage((BufferedImage) outbuf.data));
                }
            } while (!inbuf.isFlag(BufferFlag.END_OF_MEDIA));
        }

        return images;
    }
}

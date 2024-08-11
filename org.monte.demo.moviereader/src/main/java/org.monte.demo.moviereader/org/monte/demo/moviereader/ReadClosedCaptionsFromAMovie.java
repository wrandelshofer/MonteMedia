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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.text.TextFormatKeys.ENCODING_HTML;
import static org.monte.media.av.codec.text.TextFormatKeys.ENCODING_STRING;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;

/**
 * Reads closed captions from a Movie with the Monte Media library.
 *
 * @author Werner Randelshofer
 */
public class ReadClosedCaptionsFromAMovie {


    public List<String> readClosedCaptions(File file) throws IOException {
        List<String> closedCaptions = new ArrayList<>();
        try (MovieReader in = Registry.getInstance().getReader(file)) {
            printTrackInfo(file, in);
            int track = in.findTrack(0, new Format(MediaTypeKey, FormatKeys.MediaType.TEXT));
            if (track < 0) {
                throw new IOException("Could not find a closed caption track.");
            }
            Codec codec = Registry.getInstance().getCodec(in.getFormat(track), new Format(DataClassKey, String.class, EncodingKey, ENCODING_HTML));
            Codec codec2 = Registry.getInstance().getCodec(in.getFormat(track), new Format(DataClassKey, String.class, EncodingKey, ENCODING_STRING));
            if (codec == null)
                throw new IOException("Could not find a String codec for Strings.");
            if (codec2 == null)
                throw new IOException("Could not find a HTML codec for Strings.");

            Buffer inbuf = new Buffer();
            Buffer outbuf = new Buffer();
            Buffer outbuf2 = new Buffer();
            do {
                in.read(track, inbuf);
                codec.process(inbuf, outbuf);
                codec2.process(inbuf, outbuf2);
                if (!outbuf.isFlag(BufferFlag.DISCARD)) {
                    String text = (String) outbuf.data;
                    closedCaptions.add(text);
                    System.out.println(inbuf.timeStamp + " " + inbuf.sampleDuration + " " + inbuf.flags + " " + text);
                } else {
                    System.out.println(inbuf.timeStamp + " " + inbuf.sampleDuration + " " + "DISCARD " + outbuf.exception);
                }
                if (!outbuf2.isFlag(BufferFlag.DISCARD)) {
                    String text = (String) outbuf2.data;
                    System.out.println(inbuf.timeStamp + " " + inbuf.sampleDuration + " " + inbuf.flags + " " + text);
                } else {
                    System.out.println(inbuf.timeStamp + " " + inbuf.sampleDuration + " " + "DISCARD " + outbuf2.exception);
                }
            } while (!inbuf.isFlag(BufferFlag.END_OF_MEDIA));
        }
        return closedCaptions;
    }

    private static void printTrackInfo(File file, MovieReader in) throws IOException {
        System.out.println(file);
        for (int i = 0, n = in.getTrackCount(); i < n; i++) {
            System.out.println("  track " + i + ": " + in.getFormat(i));
        }
    }
}

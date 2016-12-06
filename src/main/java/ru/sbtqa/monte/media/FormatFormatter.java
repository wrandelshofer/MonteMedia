/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.sbtqa.monte.media;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static ru.sbtqa.monte.media.AudioFormatKeys.*;
import static ru.sbtqa.monte.media.FormatKeys.MediaType.VIDEO;
import static ru.sbtqa.monte.media.VideoFormatKeys.*;
import ru.sbtqa.monte.media.math.Rational;

/**
 * {@code FormatFormatter}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2012-01-11 Created.
 */
public class FormatFormatter {

    private static String audioToString(Format f) {
        StringBuilder buf = new StringBuilder();
        buf.append(f.get(EncodingKey).equals(ENCODING_AVI_PCM) ? "PCM" : f.get(EncodingKey))
              .append(", ")//
              .append(f.get(ChannelsKey))//
              .append(" channel(s)")//
              .append(", ")//
              .append(f.get(SampleSizeInBitsKey))//
              .append("-bit")//
              .append(f.get(SignedKey) ? " signed" : " unsigned")//
              .append(f.get(ByteOrderKey) == BIG_ENDIAN ? " BE" : " LE")//
              .append(", ")//
              .append(f.get(SampleRateKey))//
              .append(" Hz, ")//
              .append("")//
              ;
        return buf.toString();
    }

    private static String videoToString(Format f) {
        StringBuilder buf = new StringBuilder();
        buf.append(f.get(EncodingKey))//
              .append(", ")//
              .append(f.get(WidthKey))//
              .append("x")//
              .append(f.get(HeightKey))//
              .append(", ")//
              .append(f.get(DepthKey))//
              .append("-bit, ")//
              .append(f.get(FrameRateKey, new Rational(0, 0)))//
              .append(" fps")//
              .append(f.get(FixedFrameRateKey, false) ? ", fixed rate" : "")//
              .append(f.get(PixelAspectRatioKey, new Rational(1, 1)).equals(new Rational(1, 1)) ? "" : ", " + f.get(PixelAspectRatioKey) + " pixel ratio")//
              .append("")//
              ;
        return buf.toString();
    }

    private static String fileToString(Format f) {
        StringBuilder buf = new StringBuilder();
        buf.append(f.get(MimeTypeKey));//
        return buf.toString();
    }

    public static String toString(Format f) {
        switch (f.get(MediaTypeKey, VIDEO)) {
            case AUDIO:
                return audioToString(f);
            case VIDEO:
                return videoToString(f);
            case FILE:
                return fileToString(f);
            default:
                return f.toString();
        }
    }
}

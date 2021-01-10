/* @(#)AnimToQuickTimeConverter.java
 * Copyright © 2021 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.animconverter;

import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.amigabitmap.codec.video.AmigaBitmapCodec;
import org.monte.media.anim.ANIMDecoder;
import org.monte.media.anim.ANIMDemultiplexer;
import org.monte.media.anim.ANIMFrame;
import org.monte.media.anim.ANIMMovieResources;
import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Codec;
import org.monte.media.av.CodecChain;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.Track;
import org.monte.media.av.codec.video.PNGCodec;
import org.monte.media.av.codec.video.ScaleImageCodec;
import org.monte.media.av.codec.video.TechSmithCodec;
import org.monte.media.ilbm.ColorCycle;
import org.monte.media.ilbm.ColorCyclingMemoryImageSource;
import org.monte.media.image.Images;
import org.monte.media.math.Rational;
import org.monte.media.quicktime.QuickTimeMultiplexer;
import org.monte.media.quicktime.QuickTimeReader;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.FrameRateKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.COMPRESSOR_NAME_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.av.codec.video.VideoFormatKeys.CompressorNameKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DepthKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_AVI_TECHSMITH_SCREEN_CAPTURE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_PNG;
import static org.monte.media.av.codec.video.VideoFormatKeys.FixedFrameRateKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.HeightKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.WidthKey;

/**
 * Converts an Amiga IFF Cell Animation file into a QuickTime movie file.
 */
public class AnimToQuickTimeConverter {
    /**
     * Two bitmaps are needed for double buffering.
     */
    private AmigaBitmapImage bitmapEven, bitmapOdd;
    /**
     * The memory image source handles the image
     * producer/consumer protocol.
     */
    private ColorCyclingMemoryImageSource memoryImage;

    /**
     * Converts the given input file to the given output file.
     *
     * @param animFile      input file
     * @param quickTimeFile output file
     * @throws IOException if the input file does not exit, or if the output file exists
     */
    public void convert(String animFile, String quickTimeFile) throws IOException {
        Path userDir = Paths.get(System.getProperty("user.dir"));
        Path animPath = userDir.resolve(Paths.get(animFile));
        Path quickTimePath = userDir.resolve(Paths.get(quickTimeFile));

        if (!Files.exists(animPath)) {
            throw new IOException("Input file does not exist: " + animPath);
        }
        if (Files.isDirectory(quickTimePath)) {
            throw new IOException("Output file must not be a directory: " + quickTimePath);
        }

        ANIMDemultiplexer demux = new ANIMDemultiplexer(animPath.toFile());
        QuickTimeMultiplexer mux = new QuickTimeMultiplexer(quickTimePath.toFile());

        Track videoTrack = null;
        Track audioTrack = null;


        Format inputVideoFormat = null;
        for (Track track : demux.getTracks()) {
            switch (track.getFormat().get(FormatKeys.MediaTypeKey)) {
            case VIDEO:
                videoTrack = track;
                inputVideoFormat = track.getFormat();
                break;
            case AUDIO:
                break;
            }
        }


        Buffer inBuf = new Buffer();
        Buffer outBuf = new Buffer();
        AmigaBitmapCodec amigaBitmapCodec = new AmigaBitmapCodec();
        PNGCodec pngCodec = new PNGCodec();
        pngCodec.setOutputFormat(
                new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_QUICKTIME,
                        EncodingKey, ENCODING_QUICKTIME_PNG, DataClassKey, byte[].class
                )
                        .append(inputVideoFormat));
        amigaBitmapCodec.setOutputFormat(new Format(MediaTypeKey, FormatKeys.MediaType.VIDEO, MimeTypeKey, MIME_JAVA,
                EncodingKey, ENCODING_BUFFERED_IMAGE)
                .append(inputVideoFormat));


        Codec codecChain = CodecChain.createCodecChain(
                amigaBitmapCodec,
                pngCodec);


        int videoTrackId = -1;
        for (Track track : demux.getTracks()) {
            switch (track.getFormat().get(FormatKeys.MediaTypeKey)) {
            case VIDEO:
                videoTrack = track;
                videoTrackId = mux.addTrack(
                        codecChain.getOutputFormat()
                );
                break;
            case AUDIO:
                audioTrack = track;
                break;
            }
        }

        do {
            videoTrack.read(inBuf);
            int process;
            do {
                process = codecChain.process(inBuf, outBuf);
                mux.write(videoTrackId, outBuf);
            } while ((process & Codec.CODEC_INPUT_NOT_CONSUMED) == Codec.CODEC_INPUT_NOT_CONSUMED);

        } while (!outBuf.isFlag(BufferFlag.END_OF_MEDIA));

        mux.finish();
    }
}

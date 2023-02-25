/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.animconverter;

import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.amigabitmap.codec.video.AmigaBitmapCodec;
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
import org.monte.media.iff.MC68000InputStream;
import org.monte.media.ilbm.ColorCyclingMemoryImageSource;
import org.monte.media.math.Rational;
import org.monte.media.quicktime.QuickTimeMultiplexer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MIME_QUICKTIME;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_BUFFERED_IMAGE;
import static org.monte.media.av.codec.video.VideoFormatKeys.ENCODING_QUICKTIME_PNG;

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
    private boolean setSwapLeftRightChannels;

    /**
     * Converts the given input file to the given output file.
     *
     * @param animFile      input file, animFile or zipFile containing anims
     * @param quickTimeFile output file
     * @throws IOException if the input file does not exit, or if the output file exists
     */
    public void convert(String animFile, String quickTimeFile) throws IOException {
        Path userDir = Paths.get(System.getProperty("user.dir"));
        Path inputPath = userDir.resolve(Paths.get(animFile));
        Path quickTimePath = userDir.resolve(Paths.get(quickTimeFile));

        if (!Files.exists(inputPath)) {
            throw new IOException("Input file does not exist: " + inputPath);
        }
        if (Files.isDirectory(quickTimePath)) {
            throw new IOException("Output file must not be a directory: " + quickTimePath);
        }
        if (Files.isDirectory(inputPath)) {
            Files.walk(inputPath)
                    .filter(Files::isRegularFile)
                    .sorted().forEach(f -> {
                        try {
                            String fName = f.getFileName().toString();
                            String fNameWithoutExtension =
                                    getFilenameWithoutExtension(fName);

                            if (fName.endsWith(".zip")) {
                                convertZipFile(f);
                            } else if (isANIM(f)) {
                                ANIMDemultiplexer demux = createDemux(f);
                                if (!demux.getResources().getColorCycles().isEmpty()) {
                                    System.out.println("CC: " + f);
                                }
                                Path quickTimePath2 = f.getParent().resolve(fNameWithoutExtension + ".mov");
                                Path mp4Path = f.getParent().resolve(fNameWithoutExtension + ".mp4");
                                if (!Files.exists(quickTimePath2)) {
                                    QuickTimeMultiplexer mux = new QuickTimeMultiplexer(quickTimePath2.toFile());
                                    mux.setMovieTimeScale(demux.getTimeBase());
                                    convertToQuickTime(demux, mux, fName);
                                }
                                if (!Files.exists(mp4Path)) {
                                    convertToMP4(demux, quickTimePath2, mp4Path);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });

        } else if (animFile.endsWith(".zip")) {
            convertZipFile(inputPath);
        } else {
            ANIMDemultiplexer demux = createDemux(inputPath);
            if (!Files.exists(quickTimePath)) {
                QuickTimeMultiplexer mux = new QuickTimeMultiplexer(quickTimePath.toFile());
                mux.setMovieTimeScale(demux.getTimeBase());
                convertToQuickTime(demux, mux, inputPath.toString()
                );
            }
            String fileNameWoEx = getFilenameWithoutExtension(inputPath.getFileName().toString());
            Path mp4Path = inputPath.getParent().resolve(fileNameWoEx + ".mp4");
            if (!Files.exists(mp4Path)) {
                convertToMP4(demux, quickTimePath, mp4Path);
            }

        }
    }

    private ANIMDemultiplexer createDemux(Path f) throws IOException {
        ANIMDemultiplexer demux = new ANIMDemultiplexer(f.toFile());
        initDemux(demux);
        return demux;
    }

    private ANIMDemultiplexer createDemux(BufferedInputStream bin) throws IOException {
        ANIMDemultiplexer demux = new ANIMDemultiplexer(bin);
        initDemux(demux);
        return demux;
    }

    private void initDemux(ANIMDemultiplexer demux) {
        if (frameDuration != null && frameDuration > 0) {
            long newRelTime = this.frameDuration;
            ANIMMovieResources res = demux.getResources();
            for (int i = 0, n = res.getFrameCount(); i < n; i++) {
                ANIMFrame frame = res.getFrame(i);
                frame.setRelTime(newRelTime);
            }
        }

    }


    private void convertZipFile(Path zipFile) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(zipFile))) {
            for (ZipEntry entry = zin.getNextEntry(); entry != null; entry = zin.getNextEntry()) {
                if (!entry.isDirectory()) {
                    String name = entry.getName();
                    int pendIndex = name.lastIndexOf('.');
                    int separatorIndex = name.lastIndexOf('/');
                    if (pendIndex < separatorIndex) {
                        pendIndex = -1;
                    }
                    String nameWithoutExtension = pendIndex == -1 ? name : name.substring(0, pendIndex);
                    Path quickTimeFile = zipFile.getParent().resolve(nameWithoutExtension + ".mov");
                    Path mp4File = zipFile.getParent().resolve(nameWithoutExtension + ".mp4");
                    if (!Files.exists(quickTimeFile) || !Files.exists(mp4File)) {

                        BufferedInputStream bin = new BufferedInputStream(zin) {
                            public void close() {
                                // keep ZipInputStream open!
                            }
                        };
                        bin.mark(20);
                        boolean anim = isANIM(bin);
                        if (anim) {

                            bin.reset();
                            System.out.println(zipFile + "!" + entry.getName());
                            ANIMDemultiplexer demux;
                            demux = createDemux(bin);
                            if (!demux.getResources().getColorCycles().isEmpty()) {
                                System.out.println("CC: " + zipFile + "!" + name);
                                continue;
                            }

                            if (!Files.exists(quickTimeFile)) {
                                QuickTimeMultiplexer mux = new QuickTimeMultiplexer(quickTimeFile.toFile());
                                convertToQuickTime(demux, mux, zipFile + "!" + name
                                );
                            }
                            if (!Files.exists(mp4File)) {
                                convertToMP4(demux, quickTimeFile, mp4File);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Converts the input from the given demultiplexer to the given multiplexer.
     *
     * @param demux    input file
     * @param mux      output file
     * @param animFile
     * @throws IOException on io exception
     */
    public void convertToQuickTime(ANIMDemultiplexer demux, QuickTimeMultiplexer mux,
                                   String animFile) throws IOException {


        // Check if demux has color cycling
        if (!demux.getResources().getColorCycles().isEmpty()) {
            System.out.println(animFile + " has color cycles.");
            return;
        }


        // Create the tracks
        Track videoTrack = null, audioTrack = null;
        Codec videoCodecChain = null;
        int videoTrackId = -1;
        int audioTrackId = -1;
        for (Track track : demux.getTracks()) {
            switch (track.getFormat().get(FormatKeys.MediaTypeKey)) {
                case VIDEO: {
                    videoTrack = track;
                    Format inputVideoFormat = track.getFormat();
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


                    videoCodecChain = CodecChain.createCodecChain(
                            amigaBitmapCodec,
                            pngCodec);
                    videoTrackId = mux.addTrack(videoCodecChain.getOutputFormat());
                }
                break;
                case AUDIO:
                    if (audioTrackId == -1) {
                        audioTrack = track;
                        audioTrackId = mux.addTrack(audioTrack.getFormat());
                    }
                    break;
            }
        }

        Buffer videoInBuf = new Buffer();
        Buffer videoOutBuf = new Buffer();
        Buffer audioBuf = new Buffer();


        // Start with first buffer of audio track
        Rational nextAudioBufferTime;
        if (audioTrack != null) {
            audioTrack.read(audioBuf);
            mux.write(audioTrackId, audioBuf);
            if (!audioBuf.isFlag(BufferFlag.DISCARD)) {
                nextAudioBufferTime = audioBuf.timeStamp.add(audioBuf.sampleDuration.multiply(audioBuf.sampleCount));
            } else {
                nextAudioBufferTime = Rational.ZERO;
            }
        } else {
            nextAudioBufferTime = Rational.ZERO;
        }

        // Write video track buffers,
        // while keeping audio track one buffer ahead of video tracks

        if (videoTrack != null) {
            do {
                videoTrack.read(videoInBuf);
                int process;
                do {
                    process = videoCodecChain.process(videoInBuf, videoOutBuf);

                    if (audioTrack != null && !videoOutBuf.isFlag(BufferFlag.DISCARD)
                            && nextAudioBufferTime.compareTo(videoOutBuf.timeStamp) <= 0) {
                        audioTrack.read(audioBuf);
                        if (!audioBuf.isFlag(BufferFlag.DISCARD)) {
                            mux.write(audioTrackId, audioBuf);
                            nextAudioBufferTime = audioBuf.timeStamp.add(audioBuf.sampleDuration.multiply(audioBuf.sampleCount));
                        }
                    }

                    mux.write(videoTrackId, videoOutBuf);
                } while ((process & Codec.CODEC_INPUT_NOT_CONSUMED) == Codec.CODEC_INPUT_NOT_CONSUMED);

            } while (!videoOutBuf.isFlag(BufferFlag.END_OF_MEDIA));
        }

        // Write remaining audio buffers
        if (audioTrack != null) {
            while (!audioBuf.isFlag(BufferFlag.END_OF_MEDIA)) {
                audioTrack.read(audioBuf);
                mux.write(audioTrackId, audioBuf);
            }
        }
        mux.finish();
    }

    private void convertToMP4(ANIMDemultiplexer demux, Path quickTimeFile, Path mp4File) throws IOException {
        if (!Files.exists(mp4File)) {
            // 1920 x 1080p = full hd
            int maxWidth = 1920;
            int maxHeight = 1080;

            int xAspect = this.xAspect == null ? demux.getResources().getXAspect() : this.xAspect;
            int yAspect = this.yAspect == null ? demux.getResources().getYAspect() : this.yAspect;
            int width = demux.getWidth();
            int height = demux.getHeight();
            System.out.println("  w=" + width + " h=" + height + " xa=" + xAspect + " ya=" + yAspect);
            int adjustedWidth = xAspect * width;
            int adjustedHeight = yAspect * height;

            double maxWidthFactor = maxWidth / (double) adjustedWidth;
            double maxHeightFactor = maxHeight / (double) adjustedHeight;
            double maxFactor = Math.min(maxWidthFactor, maxHeightFactor);
            int targetWidth = (int) Math.round(adjustedWidth * maxFactor);
            int targetHeight = (int) Math.round(adjustedHeight * maxFactor);

            // targetWidth and targetHeight must be dividable by 2
            targetWidth += targetWidth % 2;
            targetHeight += targetHeight % 2;

            System.out.println("  tw=" + targetWidth + " th=" + targetHeight);

            // Convert to MP4
            System.out.println(quickTimeFile + "  converting to " + mp4File);
            String[] args = {
                    "ffmpeg",
                    "-i", quickTimeFile.toAbsolutePath().toString(),
                    "-vf", "scale=" + targetWidth + ":" + targetHeight,
                    "-sws_flags", "neighbor",
                    "-vcodec", "h264",
                    "-pix_fmt", "yuv420p",
                    "-profile:v", "baseline",
                    "-level", "3",
                    "-preset:v", "slow",
                    "-max_muxing_queue_size", "9999",
                    "-n", // never overwrite
                    mp4File.toAbsolutePath().toString()
            };
            StringBuilder buf = new StringBuilder();
            for (String arg : args) {
                if (buf.length() != 0) {
                    buf.append(" ");
                }
                buf.append(arg);
            }
            System.out.println(buf);
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            InputStream inputStream = process.getInputStream();
            new Thread(() -> {
                try (BufferedReader r = new BufferedReader(new InputStreamReader(inputStream))) {
                    for (String line = r.readLine(); line != null; line = r.readLine()) {
                        System.out.println(line);
                    }
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                } finally {
                    System.out.println("Reader finished.");
                }
            }).start();

        }

    }

    public void setSwapLeftRightChannels(boolean value) {
        this.setSwapLeftRightChannels = value;
    }

    private boolean isANIM(Path file) {
        try (InputStream in = Files.newInputStream(file)) {
            return isANIM(in);
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Reads up to 20 bytes at the current position of the input stream
     * and returns true if the current position of the input stream denotes
     * an IFF "FORM" "ANIM" file.
     *
     * @param in an input stream
     * @return true if anim
     * @throws IOException
     */
    private boolean isANIM(InputStream in) throws IOException {
        MC68000InputStream mc = new MC68000InputStream(in);
        int form = (int) mc.readULONG();
        long size = mc.readULONG();
        if (size == 0) {
            size = mc.readINT64();
        }
        int type = mc.readLONG();
        return form == 0x464f524d && type == 0x414e494d;
    }

    private String getFilenameWithoutExtension(String filename) {
        String fName = filename;
        int endIndex = fName.lastIndexOf('.');
        String fNameWithoutExtension = endIndex == -1 ? fName : fName.substring(0, endIndex);
        return fNameWithoutExtension;
    }

    /**
     * Pixel aspect ratio. Overrides the pixel aspect ratio of the ANIM
     * file if the value is non-null.
     * Must be greater or equal than 1.
     */
    private Integer xAspect, yAspect;

    /**
     * Duration of a frame in jiffies. Overrides the frame duration of
     * the ANIM if the value is non-null.
     * Must be greater or equal than 1.
     */
    private Integer frameDuration;

    public void setXAspect(Integer xAspect) {
        this.xAspect = xAspect;
    }

    public void setYAspect(Integer yAspect) {
        this.yAspect = yAspect;
    }

    public void setFrameDuration(Integer frameDuration) {
        this.frameDuration = frameDuration;
    }
}

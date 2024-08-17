/*
 * @(#)PlayerEngine.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.javafx.movieplayer.monteplayer;


import javafx.application.Platform;
import javafx.scene.image.WritableImage;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.monte.demo.javafx.movieplayer.model.TrackInterface;
import org.monte.media.av.AbstractPlayer;
import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Codec;
import org.monte.media.av.CodecChain;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.MovieReader;
import org.monte.media.av.Registry;
import org.monte.media.av.codec.audio.AudioFormatKeys;
import org.monte.media.av.codec.video.VideoFormatKeys;
import org.monte.media.math.Rational;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MIME_JAVA;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;
import static org.monte.media.av.codec.audio.AudioFormatKeys.ENCODING_PCM_SIGNED;
import static org.monte.media.av.codec.audio.AudioFormatKeys.SignedKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;

class PlayerEngine extends AbstractPlayer {
    private final MonteMediaPlayer player;
    private final MonteMedia media;
    private MovieReader reader;
    /**
     * Method {@link #seek(Rational)} sets the seek time to a non-null value.
     * <p>
     * Methods {@link #doPrefetched()} and {@link #doStarted()} retrieve the value (using {@code seekTime.getAndSet(null)}).
     * And seek to the desired time.
     */
    private final AtomicReference<Rational> seekTime = new AtomicReference<>();
    private MonteVideoTrack vTrack;

    public PlayerEngine(MonteMediaPlayer player) {
        this.player = player;
        this.media = (MonteMedia) player.getMedia();
    }

    @Override
    protected void fireStateChanged(int oldState, int newState) {
        Platform.runLater(() -> {
            player.setStatus(
                    switch (newState) {
                        case REALIZED -> MediaPlayer.Status.READY;
                        case PREFETCHING, PREFETCHED -> MediaPlayer.Status.PAUSED;
                        case STARTED -> MediaPlayer.Status.PLAYING;
                        case CLOSED -> MediaPlayer.Status.DISPOSED;
                        default -> MediaPlayer.Status.UNKNOWN;
                    });
        });
    }

    @Override
    protected void fireErrorHappened(Throwable error) {
        Platform.runLater(() -> {
            media.setError(error);
        });
    }

    @Override
    protected void doClosed() throws Exception {
        if (reader != null) {
            var tmpReader = reader;
            reader = null;
            tmpReader.close();
        }
    }

    @Override
    protected void doUnrealized() {

    }

    @Override
    protected void doRealizing() throws Exception {
        if (reader != null) {
            var tmpReader = reader;
            reader = null;
            tmpReader.close();
        }

        reader = Registry.getInstance().getReader(new File(new URI(media.getSource())));
        List<TrackInterface> tracks = new ArrayList<>();
        int mediaWidth = 0, mediaHeight = 0;
        Format fileFormat = reader.getFileFormat();
        for (int i = 0, n = reader.getTrackCount(); i < n; i++) {
            mediaWidth = (fileFormat.get(VideoFormatKeys.WidthKey, 0));
            mediaHeight = (fileFormat.get(VideoFormatKeys.HeightKey, 0));
            Format trackFormat = reader.getFormat(i);
            Format format = trackFormat;
            final Map<String, Object> metadata = new LinkedHashMap<>();
            format.getProperties().entrySet().iterator().forEachRemaining(e -> metadata.put(e.getKey().getName(), e.getValue()));
            tracks.add(switch (format.get(MediaTypeKey)) {
                case FormatKeys.MediaType.VIDEO -> {
                    realizeVideoTrack(i, metadata, format, trackFormat);
                    yield vTrack;
                }
                case FormatKeys.MediaType.AUDIO -> {
                    MonteAudioTrack audioTrack = realizeAudioTrack(i, metadata, trackFormat, format);
                    yield audioTrack;
                }
                case FormatKeys.MediaType.TEXT -> realizeSubtitleTrack(i, metadata);
                default -> new MonteUnsupportedTrack(Locale.ENGLISH, i, i + "", metadata);
            });
        }
        int finalWidth = mediaWidth;
        int finalHeight = mediaHeight;
        runAndWait(() -> {
            media.setFormat(fileFormat);
            media.getTracks().addAll(tracks);
            media.setDuration(Duration.seconds(reader.getDuration().doubleValue()));
            media.setWidth(finalWidth);
            media.setHeight(finalHeight);
            player.setCurrentTime(Duration.seconds(vTrack.getRenderedStartTime().doubleValue()));
            player.setCurrentCount(0);
            player.setCurrentRate(0.0);
            return null;
        });

    }

    private static MonteSubtitleTrack realizeSubtitleTrack(int i, Map<String, Object> metadata) {
        return new MonteSubtitleTrack(Locale.ENGLISH, i, i + "", metadata);
    }

    private MonteAudioTrack realizeAudioTrack(int i, Map<String, Object> metadata, Format trackFormat, Format format) throws LineUnavailableException, IOException {
        MonteAudioTrack audioTrack = new MonteAudioTrack(Locale.ENGLISH, i, i + "", metadata);
        audioTrack.setFormat(trackFormat);
        Format desiredOutputFormat = new Format(MediaTypeKey, FormatKeys.MediaType.AUDIO,//
                EncodingKey, ENCODING_PCM_SIGNED,//
                MimeTypeKey, MIME_JAVA,//
                SignedKey, true);
        Codec codec1 = Registry.getInstance().getCodec(format, desiredOutputFormat);
        if (codec1 != null) {
            codec1.setInputFormat(format);
            codec1.setOutputFormat(desiredOutputFormat);
            audioTrack.setCodec(codec1);

            Format actualOutputFormat = codec1.getOutputFormat();
            AudioFormat audioFormat = new AudioFormat(
                    actualOutputFormat.get(AudioFormatKeys.SampleRateKey).floatValue(),
                    actualOutputFormat.get(AudioFormatKeys.SampleSizeInBitsKey),
                    actualOutputFormat.get(AudioFormatKeys.ChannelsKey),
                    actualOutputFormat.get(SignedKey), actualOutputFormat.get(AudioFormatKeys.ByteOrderKey) == ByteOrder.BIG_ENDIAN);
            SourceDataLine sourceDataLine = AudioSystem.getSourceDataLine(audioFormat);
            audioTrack.setSourceDataLine(sourceDataLine);
            sourceDataLine.open(audioFormat);
            sourceDataLine.start();

            audioTrack.setFormat(trackFormat);
            Buffer inBuf = audioTrack.inBuffer;
            Buffer outBuf = audioTrack.outBufferA;
            reader.read(i, inBuf);
            int status;
            do {
                status = codec1.process(inBuf, outBuf);
            } while (status == Codec.CODEC_OUTPUT_NOT_FILLED);

        }
        return audioTrack;
    }

    private void realizeVideoTrack(int i, Map<String, Object> metadata, Format format, Format trackFormat) throws IOException {
        vTrack = new MonteVideoTrack(Locale.ENGLISH, i, i + "", metadata);
        vTrack.setWidth(format.get(VideoFormatKeys.WidthKey));
        vTrack.setHeight(format.get(VideoFormatKeys.HeightKey));
        CodecChain codecChain = null;
        Codec codec1 = Registry.getInstance().getCodec(format, new Format(DataClassKey, BufferedImage.class));
        if (codec1 != null) {
            Codec codec2 = Registry.getInstance().getCodec(codec1.getOutputFormat(), new Format(EncodingKey, VideoFormatKeys.ENCODING_WRITABLE_IMAGE, DataClassKey, WritableImage.class));
            if (codec2 != null) {
                codecChain = new CodecChain(codec1, codec2);
            }
            vTrack.setCodec(codecChain);

            vTrack.setFormat(trackFormat);
            Buffer inBuf = vTrack.inBuffer;
            Buffer outBuf = vTrack.outBufferA;
            reader.read(i, inBuf);
            int status;
            do {
                status = codecChain.process(inBuf, outBuf);
            } while (status == Codec.CODEC_OUTPUT_NOT_FILLED);
            if (!outBuf.isFlag(BufferFlag.DISCARD) && outBuf.data instanceof WritableImage wImg) {
                vTrack.setVideoImage(wImg);
                vTrack.setRenderedStartTime(outBuf.timeStamp);
                vTrack.setRenderedEndTime(outBuf.timeStamp.add(outBuf.sampleDuration));
            } else {
                throw new IOException("Could not decode the video track.");
            }
        }
    }

    public Rational getFrameAfter(Rational seconds) {
        int trackID = (int) vTrack.getTrackID();
        try {
            long sample = reader.timeToSample(trackID, seconds);
            Rational time = reader.sampleToTime(trackID, sample);
            Rational duration = reader.getDuration(trackID, sample);
            return time.add(duration);
        } catch (IOException e) {
            return Rational.ZERO;
        }
    }

    public Rational getFrameBefore(Rational seconds) {
        int trackID = (int) vTrack.getTrackID();
        try {
            long sample = reader.timeToSample(trackID, seconds);
            Rational time = reader.sampleToTime(trackID, Math.max(0, sample - 1));
            return time;
        } catch (IOException e) {
            return Rational.ZERO;
        }
    }

    protected <T> T runAndWait(Callable<T> r) throws Exception {
        CompletableFuture<T> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                future.complete(r.call());
            } catch (Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future.get();
    }

    @Override
    protected void doRealized() {

    }

    @Override
    protected void doPrefetching() throws Exception {
    }

    @Override
    protected void doPrefetched() throws Exception {
        Rational playTime = seekTime.getAndSet(null);
        if (playTime == null || vTrack == null) {
            return;
        }

        reader.setMovieReadTime(playTime);
        updateBuffers(playTime);
        renderBuffers(playTime, false);
    }

    private Rational frameRate = Rational.valueOf(1, 60);

    @Override
    protected void doStarted() throws Exception {
        int trackId = (int) vTrack.getTrackID();
        Rational playTime = vTrack.getRenderedEndTime() == null ? Rational.valueOf(player.getCurrentTime().toSeconds()) : vTrack.getRenderedEndTime();
        if (playTime == null || vTrack == null) {
            return;
        }
        // Start from beginning if we are at the end of the movie
        Rational playEndTime = reader.getDuration(trackId);
        if (playTime.compareTo(playEndTime) >= 0) {
            playTime = Rational.ZERO;
        }

        reader.setMovieReadTime(playTime);
        Rational playStartTime = playTime;
        long startNanoTime = System.nanoTime();
        while (playTime.compareTo(playEndTime) <= 0 && getTargetState() == PlayerEngine.STARTED) {

            updateBuffers(playTime);

            int elapsedMovieMillis = playTime.subtract(playStartTime).multiply(1000).intValue();
            int elapsedSystemMillis = (int) ((System.nanoTime() - startNanoTime) / 1_000_000L);
            int sleepMillis = (elapsedMovieMillis - elapsedSystemMillis);
            if (sleepMillis > 0) {
                Thread.sleep(sleepMillis);
            }

            renderBuffers(playTime, true);

            // Compute the next play time
            Rational newTargetTime = seekTime.getAndSet(null);
            if (newTargetTime != null) {
                newTargetTime = Rational.clamp(newTargetTime, Rational.ZERO, playEndTime);
                reader.setMovieReadTime(newTargetTime);
                playStartTime = playTime = newTargetTime;
                startNanoTime = System.nanoTime();
            } else {
                playTime = playTime.add(frameRate);
            }
        }

        stopAudio();
    }

    private void stopAudio() {
        for (var t : media.getTracks()) {
            if (t instanceof MonteAudioTrack mat) {
                SourceDataLine sourceDataLine = mat.getSourceDataLine();
                if (sourceDataLine != null) {
                    sourceDataLine.flush();
                }
            }
        }
    }

    private void updateBuffers(Rational playTime) throws IOException {
        for (var track : media.getTracks()) {
            if (!(track instanceof MonteTrackInterface tr)
                    || tr.getCodec() == null) {
                // we cannot decode this track
                continue;
            }
            Buffer outBuf = tr.getOutBufferA();
            if (outBuf.timeStamp.compareTo(playTime) <= 0 &&
                    playTime.compareTo(outBuf.getBufferEndTimestamp()) < 0) {
                // outBufA contains the sample for this playTime
                continue;
            }

            outBuf = tr.swapOutBuffers();
            Buffer inBuf = tr.getInBuffer();
            var codec = tr.getCodec();
            int trackId = (int) tr.getTrackID();
            int status;
            do {
                reader.read(trackId, inBuf);
                do {
                    status = codec.process(inBuf, outBuf);
                } while (status == Codec.CODEC_OUTPUT_NOT_FILLED);
            } while (status == Codec.CODEC_OK
                    && outBuf.getBufferEndTimestamp().compareTo(playTime) < 0
                    && !outBuf.isFlag(BufferFlag.END_OF_MEDIA));
        }
    }

    private void renderBuffers(Rational renderTime, boolean playAudio) throws IOException {
        Platform.runLater(() -> {
            boolean muted = player.muteProperty().get();
            player.setCurrentTime(Duration.seconds(renderTime.doubleValue()));
            for (var track : media.getTracks()) {
                if (!(track instanceof MonteTrackInterface tr)
                        || tr.getCodec() == null) {
                    // we cannot decode this track
                    continue;
                }
                Buffer outBuf = tr.getOutBufferA();
                if (!outBuf.isFlag(BufferFlag.DISCARD)) {
                    // outBufA contains the sample for this playTime

                    Rational bufferStartTime = outBuf.timeStamp;
                    Rational bufferEndTime = outBuf.getBufferEndTimestamp();
                    boolean previouslyRenderedBufferTimeIntersectsPlayTime = tr.getRenderedStartTime().compareTo(renderTime) <= 0 &&
                            renderTime.compareTo(tr.getRenderedEndTime()) < 0;
                    boolean bufferTimeIntersectsPlayTime = bufferStartTime.compareTo(renderTime) <= 0 &&
                            renderTime.compareTo(bufferEndTime) < 0;
                    if (bufferTimeIntersectsPlayTime) {
                        switch (tr) {
                            case MonteVideoTrack mvt -> {
                                if (outBuf.data instanceof WritableImage img) {
                                    mvt.setVideoImage(img);
                                }
                            }
                            case MonteAudioTrack mat -> {
                                if (mat.getSourceDataLine() != null && outBuf.data instanceof byte[] byteArray) {
                                    if (playAudio & !muted) {
                                        long currentNanoTime = System.nanoTime();
                                        boolean isRenderTimeValid = mat.renderTimeValidUntilNanoTime > currentNanoTime;
                                        int skipSamples;
                                        if (isRenderTimeValid && tr.getRenderedStartTime().compareTo(renderTime) < 0
                                                && renderTime.compareTo(tr.getRenderedEndTime()) < 0) {
                                            skipSamples = (bufferStartTime.compareTo(tr.getRenderedEndTime()) < 0) ?
                                                    tr.getRenderedEndTime().subtract(bufferStartTime).divide(outBuf.sampleDuration).intValue() : 0;
                                        } else {
                                            skipSamples = 0;
                                        }
                                        if (skipSamples < outBuf.sampleCount) {
                                            int sampleSize = outBuf.length / outBuf.sampleCount;
                                            byte[] clippedSamples = new byte[sampleSize * (outBuf.sampleCount - skipSamples)];
                                            System.arraycopy(byteArray, outBuf.offset + skipSamples * sampleSize, clippedSamples, 0, clippedSamples.length);
                                            mat.dispatcher.execute(() -> {
                                                mat.getSourceDataLine().write(clippedSamples, 0, clippedSamples.length);
                                            });
                                            Rational clippedBufferDuration = outBuf.sampleDuration.multiply(outBuf.sampleCount - skipSamples);
                                            mat.renderTimeValidUntilNanoTime = (long) (currentNanoTime + clippedBufferDuration.doubleValue() * 1e9);

                                        }
                                    }
                                }
                            }
                            default -> {
                                // unsupported track type
                            }
                        }
                    }
                    tr.setRenderedStartTime(bufferStartTime);
                    tr.setRenderedEndTime(bufferEndTime);
                }
            }
        });
    }


    public void seek(Rational seconds) {
        seekTime.set(seconds);
        prefetch();
    }
}
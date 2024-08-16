/*
 * @(#)PlayerEngine.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.movieplayer.monteplayer;


import javafx.application.Platform;
import javafx.scene.image.WritableImage;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import org.monte.demo.movieplayer.model.TrackInterface;
import org.monte.media.av.Buffer;
import org.monte.media.av.BufferFlag;
import org.monte.media.av.Codec;
import org.monte.media.av.CodecChain;
import org.monte.media.av.Format;
import org.monte.media.av.FormatKeys;
import org.monte.media.av.MovieReader;
import org.monte.media.av.Registry;
import org.monte.media.av.codec.video.VideoFormatKeys;
import org.monte.media.math.Rational;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.codec.video.VideoFormatKeys.DataClassKey;

class PlayerEngine extends org.monte.media.av.AbstractPlayer {
    private final MonteMediaPlayer player;
    private final MonteMedia media;
    private MovieReader reader;
    private volatile Rational targetTime;
    private MonteVideoTrack vTrack;

    public PlayerEngine(MonteMediaPlayer player) {
        this.player = player;
        this.media = (MonteMedia) player.getMedia();
    }

    @Override
    protected void fireStateChanged(int oldState, int newState) {
        Platform.runLater(() -> {
            var status = switch (newState) {
                case REALIZED -> MediaPlayer.Status.READY;
                case PREFETCHING, PREFETCHED -> MediaPlayer.Status.PAUSED;
                case STARTED -> MediaPlayer.Status.PLAYING;
                case CLOSED -> MediaPlayer.Status.DISPOSED;
                default -> MediaPlayer.Status.UNKNOWN;
            };
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
        int width = 0, height = 0;
        for (int i = 0, n = reader.getTrackCount(); i < n; i++) {
            Format fileFormat = reader.getFileFormat();
            Format format = reader.getFormat(i);
            final Map<String, Object> metadata = new LinkedHashMap<>();
            format.getProperties().entrySet().iterator().forEachRemaining(e -> metadata.put(e.getKey().getName(), e.getValue()));
            tracks.add(switch (format.get(MediaTypeKey)) {
                case FormatKeys.MediaType.VIDEO -> {
                    //FIXME apply transform to video
                    //AffineTransform transform = format.getOrDefault(VideoFormatKeys.TransformKey);
                    width = Math.max(width, format.get(VideoFormatKeys.WidthKey));
                    height = Math.max(height, format.get(VideoFormatKeys.HeightKey));
                    vTrack = new MonteVideoTrack(Locale.ENGLISH, i, i + "", metadata);
                    vTrack.setWidth(format.get(VideoFormatKeys.WidthKey));
                    vTrack.setHeight(format.get(VideoFormatKeys.HeightKey));

                    Codec codec1 = Registry.getInstance().getCodec(format, new Format(DataClassKey, BufferedImage.class));
                    if (codec1 == null) {
                        throw new IOException("Could not find a codec for the video track.");
                    }
                    Codec codec2 = Registry.getInstance().getCodec(codec1.getOutputFormat(), new Format(EncodingKey, VideoFormatKeys.ENCODING_WRITABLE_IMAGE, DataClassKey, WritableImage.class));
                    if (codec2 == null) {
                        throw new IOException("Could not find a codec for JavaFX WritableImage.");
                    }
                    CodecChain codec = new CodecChain(codec1, codec2);
                    vTrack.setCodec(codec);
                    Buffer inBuf = vTrack.inBuffer;
                    vTrack.outBufferIndex = (vTrack.outBufferIndex + 1) % vTrack.outBuffer.length;
                    Buffer outBuf = vTrack.outBuffer[vTrack.outBufferIndex];
                    reader.read(i, inBuf);
                    int status;
                    do {
                        status = codec.process(inBuf, outBuf);
                    } while (status == Codec.CODEC_OUTPUT_NOT_FILLED);
                    if (!outBuf.isFlag(BufferFlag.DISCARD) && outBuf.data instanceof WritableImage wImg) {
                        vTrack.setVideoImage(wImg);
                        vTrack.setCurrentStartTime(outBuf.timeStamp);
                        vTrack.setCurrentEndTime(outBuf.timeStamp.add(outBuf.sampleDuration));
                    } else {
                        throw new IOException("Could not decode the video track.");
                    }
                    yield vTrack;
                }
                case FormatKeys.MediaType.AUDIO -> new MonteAudioTrack(Locale.ENGLISH, i, i + "", metadata);
                case FormatKeys.MediaType.TEXT -> new MonteSubtitleTrack(Locale.ENGLISH, i, i + "", metadata);
                default -> new MonteTrack(Locale.ENGLISH, i, i + "", metadata);
            });
        }
        int finalWidth = width;
        int finalHeight = height;
        runAndWait(() -> {
            media.getTracks().addAll(tracks);
            media.setDuration(Duration.seconds(reader.getDuration().doubleValue()));
            media.setWidth(finalWidth);
            media.setHeight(finalHeight);
            for (TrackInterface track : tracks) {
                if (track instanceof MonteVideoTrack mvt
                        && !media.videoImage.isBound()) {
                    media.videoImage.bind(mvt.videoImage);
                }
            }
            player.setCurrentTime(Duration.seconds(vTrack.getCurrentStartTime().doubleValue()));
            player.setCurrentCount(0);
            player.setCurrentRate(0.0);
            return null;
        });

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
        Rational requestedTime = targetTime;
        if (requestedTime == null || vTrack == null) {
            return;
        }
        int trackId = (int) vTrack.getTrackID();
        requestedTime = Rational.min(reader.getDuration(trackId), requestedTime);

        if (vTrack.getCurrentStartTime().compareTo(requestedTime) <= 0 &&
                requestedTime.compareTo(vTrack.getCurrentEndTime()) <= 0) {
            return;
        }

        reader.setMovieReadTime(requestedTime);
        var codec = vTrack.getCodec();
        Buffer inBuf = vTrack.inBuffer;
        vTrack.outBufferIndex = (vTrack.outBufferIndex + 1) % vTrack.outBuffer.length;
        Buffer outBuf = vTrack.outBuffer[vTrack.outBufferIndex];
        int status;
        Rational startTime = Rational.ZERO;
        Rational endTime = Rational.ZERO;
        do {
            reader.read(trackId, inBuf);
            /*
            if(inBuf.isFlag(BufferFlag.DISCARD)&&inBuf.isFlag(BufferFlag.END_OF_MEDIA)){
                break;
            }*/
            do {
                status = codec.process(inBuf, outBuf);
            } while (status == Codec.CODEC_OUTPUT_NOT_FILLED);
            startTime = outBuf.timeStamp;
            endTime = startTime.add(outBuf.sampleDuration);
        } while (status == Codec.CODEC_OK && endTime.compareTo(requestedTime) < 0 && !outBuf.isFlag(BufferFlag.END_OF_MEDIA));

        final Rational finalStartTime = startTime;
        final Rational finalEndTime = endTime;
        vTrack.setCurrentStartTime(finalStartTime);
        vTrack.setCurrentEndTime(finalEndTime);
        if (!outBuf.isFlag(BufferFlag.DISCARD) && outBuf.data instanceof WritableImage wImg) {
            runAndWait(() -> {
                vTrack.setVideoImage(wImg);
                player.setCurrentTime(Duration.seconds(finalStartTime.doubleValue()));
                player.setCurrentRate(0.0);
                return null;
            });
        }
    }

    @Override
    protected void doStarted() {

    }

    public void seek(Rational seconds) {
        targetTime = seconds;
        prefetch();
    }
}

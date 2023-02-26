/*
 * @(#)Main.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.seq;

import org.monte.media.amigabitmap.AmigaBitmapImage;

import javax.imageio.stream.FileImageInputStream;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * {@code SEQReader}.
 *
 * @author Werner Randelshofer
 */
public class SEQReader {

    private SEQMovieTrack track;
    // ---- BEGIN Decoder variables ----
    /**
     * Index of the frame which has been delta
     * decoded in its even or odd bitmap buffer.
     */
    private int fetchedEven = -1, fetchedOdd = -1;
    /**
     * Two bitmaps are needed for double buffering.
     */
    private AmigaBitmapImage bitmapEven, bitmapOdd;
    // ---- END Decoder variables ----

    public SEQReader(File file) throws IOException {
        this(file, true);
    }

    public SEQReader(File file, boolean variableFramerate) throws IOException {
        FileImageInputStream in = null;
        try {
            in = new FileImageInputStream(file);
            SEQDecoder decoder = new SEQDecoder(in);
            track = new SEQMovieTrack();
            decoder.produce(track, false);
            if (variableFramerate) {
                int removed = removeDuplicateFrames(track);
                // System.out.println(removed + " frames removed");
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    public void close() throws IOException {
        // nothing to do
    }

    public int getFrameCount() {
        return track.getFrameCount();
    }

    public int getTimeBase() {
        return track.getJiffies();
    }

    public AmigaBitmapImage createCompatibleBitmap() {
        return new AmigaBitmapImage(
                track.getWidth(),
                track.getHeight(),
                track.getNbPlanes() + (track.getMasking() == SEQMovieTrack.MSK_HAS_MASK ? 1 : 0),
                track.getFrame(0).getColorModel());
    }

    /**
     * Reads a frame into the supplied image.
     */
    public void readFrame(int index, AmigaBitmapImage image) {
        AmigaBitmapImage fetched = fetchFrame(index);

        System.arraycopy(fetched.getBitmap(), 0, image.getBitmap(), 0, fetched.getBitmap().length);
        image.setPlanarColorModel(track.getFrame(index).getColorModel());

    }

    public int getJiffies() {
        return track.getJiffies();
    }

    /**
     * Reads the duration of the specified frame.
     */
    public int getDuration(int index) {
        return (int) track.getFrame(index).getRelTime();
    }

    private AmigaBitmapImage fetchFrame(int index) {
        if (bitmapOdd == null || bitmapEven == null) {
            bitmapOdd = createCompatibleBitmap();
            bitmapEven = createCompatibleBitmap();
        }

        SEQFrame frame = null;
        int fetched;
        int interleave = track.getInterleave();
        AmigaBitmapImage bitmap;
        if (interleave == 1 || (index & 1) == 0) {
            // even?
            if (fetchedEven == index) {
                return bitmapEven;
            }
            fetched = fetchedEven;
            bitmap = bitmapEven;
            fetchedEven = index;
            if (fetched == index + interleave && track.getFrame(fetched).isBidirectional()) {
                frame = track.getFrame(fetched);
                frame.decode(bitmap, track);
                return bitmap;
            } else {
                if (fetched > index) {
                    frame = track.getFrame(0);
                    frame.decode(bitmap, track);
                    fetched = 0;
                }
            }
        } else {
            // odd?
            if (fetchedOdd == index) {
                return bitmapOdd;
            }
            fetched = fetchedOdd;
            bitmap = bitmapOdd;
            fetchedOdd = index;
            if (fetched == index + interleave && track.getFrame(fetched).isBidirectional()) {
                frame = track.getFrame(fetched);
                frame.decode(bitmap, track);
                return bitmap;
            } else {
                if (fetched > index) {
                    frame = track.getFrame(0);
                    frame.decode(bitmap, track);
                    frame = track.getFrame(1);
                    frame.decode(bitmap, track);
                    fetched = 1;
                }
            }
        }
        for (int i = fetched + interleave; i <= index; i += interleave) {
            frame = track.getFrame(i);
            frame.decode(bitmap, track);
        }
        return bitmap;
    }

    private int removeDuplicateFrames(SEQMovieTrack track) {
        int width = track.getWidth();
        int height = track.getHeight();

        SEQFrame f0 = track.getFrame(0);
        AmigaBitmapImage bmp = new AmigaBitmapImage(width, height, track.getNbPlanes(), f0.getColorModel());
        bmp.setPreferredChunkyColorModel(f0.getColorModel());
        byte[] previousBmp = new byte[bmp.getBitmap().length];
        int[] previousColors = new int[16];
        int[] colors = new int[16];

        int removed = 0;
        SEQFrame previousF = f0;
        for (int i = 1, n = track.getFrameCount(); i < n; i++) {
            SEQFrame f = track.getFrame(i);
            f.decode(bmp, track);

            ((IndexColorModel) f.getColorModel()).getRGBs(colors);
            if (Arrays.equals(bmp.getBitmap(), previousBmp)
                    && Arrays.equals(colors, previousColors)) {
                previousF.setRelTime(previousF.getRelTime() + f.getRelTime());
                track.removeFrame(i);
                --n;
                --i;
                ++removed;
                continue;
            } else {
                System.arraycopy(colors, 0, previousColors, 0, 16);
                System.arraycopy(bmp.getBitmap(), 0, previousBmp, 0, previousBmp.length);
            }

            previousF = f;
        }
        return removed;
    }
}

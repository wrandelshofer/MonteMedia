/* @(#)ANIMReader.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.anim;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.monte.media.amigabitmap.AmigaBitmapImage;
import org.monte.media.seq.SEQMovieTrack;

/**
 * {@code ANIMReader}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class ANIMReader  {

    private ANIMMovieResources resources;
    // ---- BEGIN Decoder variables ----
    /**
     * Index of the frame which has been delta
     * decoded in its even or odd bitmap buffer.
     */
    private int fetchedEven = Integer.MAX_VALUE, fetchedOdd = Integer.MAX_VALUE;
    /** Two bitmaps are needed for double buffering. */
    private AmigaBitmapImage bitmapEven, bitmapOdd;
    // ---- END Decoder variables ----

    public ANIMReader(File file) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            ANIMDecoder decoder = new ANIMDecoder(in);
            resources = new ANIMMovieResources();
            decoder.produce(resources, 0, true);
        }
    }
    public ANIMReader(InputStream in) throws IOException {
            ANIMDecoder decoder = new ANIMDecoder(in);
            resources = new ANIMMovieResources();
            decoder.produce(resources, 0, true);
    }
    public void close() throws IOException {
        // nothing to do
    }

    public int getFrameCount() {
        return getResources() .getFrameCount();
    }

    public ANIMMovieResources getResources() {
        return resources;
    }

    public int getTimeBase() {
        return getResources() .getJiffies();
    }

    public AmigaBitmapImage createCompatibleBitmap() {
        return new AmigaBitmapImage(
                resources.getWidth(),
                resources.getHeight(),
                resources.getNbPlanes() + (resources.getMasking() == SEQMovieTrack.MSK_HAS_MASK ? 1 : 0),
                resources.getFrame(0).getColorModel());
    }

    public int getWidth() {
        return resources.getWidth();
    }
    public int getHeight() {
        return resources.getHeight();
    }

    /** Reads a frame into the supplied image. */
    public void readFrame(int index,AmigaBitmapImage image) {
        AmigaBitmapImage fetched = fetchFrame(index);

        System.arraycopy(fetched.getBitmap(), 0, image.getBitmap(), 0, fetched.getBitmap().length);
        image.setPlanarColorModel(resources.getFrame(index).getColorModel());
    }

    /** Reads the duration of the specified frame. */
    public int getDuration(int index) {
        return Math.max(1,(int) resources.getFrame(index).getRelTime());
    }

    private AmigaBitmapImage fetchFrame(int index) {
        if (bitmapOdd == null || bitmapEven == null) {
            bitmapOdd = createCompatibleBitmap();
            bitmapEven = createCompatibleBitmap();
        }

        ANIMFrame frame = null;
        int fetched;
        int interleave = resources.getInterleave();
        AmigaBitmapImage bitmap;
        if (interleave == 1 || (index & 1) == 0) {
            // even?
            if (fetchedEven == index) {
                return bitmapEven;
            }
            fetched = fetchedEven;
            bitmap = bitmapEven;
            fetchedEven = index;
            if (fetched == index + interleave && resources.getFrame(fetched).isBidirectional()) {
                frame = resources.getFrame(fetched);
                frame.decode(bitmap, resources);
                return bitmap;
            } else {
                if (fetched > index) {
                    frame = resources.getFrame(0);
                    frame.decode(bitmap, resources);
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
            if (fetched == index + interleave && resources.getFrame(fetched).isBidirectional()) {
                frame = resources.getFrame(fetched);
                frame.decode(bitmap, resources);
                return bitmap;
            } else {
                if (fetched > index) {
                    frame = resources.getFrame(0);
                    frame.decode(bitmap, resources);
                    frame = resources.getFrame(1);
                    frame.decode(bitmap, resources);
                    fetched = 1;
                }
            }
        }
        for (int i = fetched + interleave; i <= index; i += interleave) {
            frame = resources.getFrame(i);
            frame.decode(bitmap, resources);
        }
        return bitmap;
    }

    public int getJiffies() {
        return resources.getJiffies();
    }

    public int getColorCyclesCount() {
        return resources.getColorCyclesCount();
    }

}

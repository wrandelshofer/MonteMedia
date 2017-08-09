/* @(#)MPOFiles.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Under the MIT License.
 */

package org.monte.media.mpo;

import org.monte.media.jfif.JFIFInputStream;
import org.monte.media.jfif.JFIFOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Utility methods for {@code MPOFiles}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-27 Created.
 */
public class MPOFiles {

    private MPOFiles() {
    }
    /** Splits a MPO file into two JPEG files.
     * <p>
     * A MPO file consists of two or more concatenated JPEG files with
     * multi-picture file meta-data in APP2 segments which start with the
     * character sequence "MPF\0".
     * <p>
     * This method writes each JPEG file into a separate file and strips
     * the multi-picture meta-data.
     */
    public static ArrayList<File> splitMPOFile(File f) throws IOException {
        int imgCount = 0;
        ArrayList<File> splittedFiles=new ArrayList<File>();
        JFIFOutputStream out = null;
        byte[] buf = new byte[2048];
        JFIFInputStream in = new JFIFInputStream(f);
        for (JFIFInputStream.Segment seg = in.getNextSegment(); seg != null; seg = in.getNextSegment()) {
            if (seg.marker == JFIFInputStream.SOI_MARKER) {
                String ext;
                switch (imgCount++) {
                    case 0:
                        ext = "_l.JPG";
                        break;
                    case 1:
                        ext = "_r.JPG";
                        break;
                    default:
                        ext = "_" + imgCount + ".JPG";
                        break;
                }
                String name = f.getName();
                int p = name.lastIndexOf('.');
                if (p == -1) {
                    p = name.length();
                }
                File imgFile = new File(f.getParentFile(), name.substring(0, p) + ext);
                splittedFiles.add(imgFile);
                out = new JFIFOutputStream(imgFile);

                out.pushSegment(seg.marker);
                out.popSegment();
            } else if (out != null) {
                // Skip APP2 segments which start with the string "MPF\0".
                if (seg.marker == JFIFInputStream.APP2_MARKER) {
                    // read fully up to 4 bytes
                    int len = 4, off = 0, n = 0;
                    while (n < len) {
                        int count = in.read(buf, off + n, len - n);
                        if (count < 0) {
                            break;
                        }
                        n += count;
                    }
                    if (n == 4 && (buf[0] & 0xff) == 'M' && (buf[1] & 0xff) == 'P' && (buf[2] & 0xff) == 'F' && buf[3] == 0) {
                        continue;
                    } else {
                        out.pushSegment(seg.marker);
                        out.write(buf, 0, n);
                    }
                } else {
                    out.pushSegment(seg.marker);
                }

                for (int len = in.read(buf, 0, buf.length); len != -1; len = in.read(buf, 0, buf.length)) {
                    out.write(buf, 0, len);
                }
                out.popSegment();
            }
        }
        return splittedFiles;
    }


}

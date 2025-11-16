/*
 * @(#)ColorSpaceUtil.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * A utility class for {@code ColorSpace} objects.
 *
 */
public class ColorSpaceUtil {

    /**
     * Don't let anyone instantiate this class.
     */
    private ColorSpaceUtil() {
    }


    /**
     * Returns the name of the color space. If the color space is an
     * {@code ICC_ColorSpace} the name is retrieved from the "desc" data element
     * of the color profile.
     *
     * @param a A ColorSpace.
     * @return The name.
     */
    public static String getName(ColorSpace a) {
        if (a instanceof NamedColorSpace) {
            return ((NamedColorSpace) a).getName();
        }
        if ((a instanceof ICC_ColorSpace icc)) {
            ICC_Profile p = icc.getProfile();
            // Get the name from the profile description tag
            byte[] desc = p.getData(ICC_Profile.icSigProfileDescriptionTag);
            if (desc != null) {
                DataInputStream in = new DataInputStream(new ByteArrayInputStream(desc));
                try {
                    int magic = in.readInt();
                    int reserved = in.readInt();
                    if (magic != 0x64657363) {
                        throw new IOException("Illegal magic value=" + Integer.toHexString(magic));
                    }
                    if (reserved != 0x0) {
                        throw new IOException("Illegal reserved value=" + Integer.toHexString(reserved));
                    }
                    long nameLength = in.readInt() & 0xffffffffL;
                    StringBuilder buf = new StringBuilder();
                    for (int i = 0; i < nameLength - 1; i++) {
                        buf.append((char) in.readUnsignedByte());
                    }
                    return buf.toString();
                } catch (IOException e) {
                    // fall back


                }
            }
        }

        if (a instanceof ICC_ColorSpace) {
            // Fall back if no description is available
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < a.getNumComponents(); i++) {
                if (!buf.isEmpty()) {
                    buf.append("-");
                }
                buf.append(a.getName(i));
            }
            return buf.toString();
        } else {
            return a.getClass().getSimpleName();
        }
    }


}

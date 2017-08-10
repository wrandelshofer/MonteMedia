/* @(#)ColorSpaces.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;

/**
 * ColorSpaces.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class ColorSpaces {

    public static String toString(ColorSpace cs) {
        if (cs instanceof ICC_ColorSpace) {
            return toString((ICC_ColorSpace) cs);
        } else {
            return cs.toString();
        }
    }

    public static String toString(ICC_ColorSpace cs) {
        StringBuilder b = new StringBuilder();
        b.append("ICC_ColorSpace{");
        b.append("components=" + cs.getNumComponents());
        //
        ICC_Profile p = cs.getProfile();
        b.append(",colorSpaceType=").append(colorSpaceTypeToString(p.getColorSpaceType()));
        b.append(",profileClass=").append(profileClassToString(p.getProfileClass()));
        /*
        b.append(",pcsType=");
        switch(p.getPCSType()) {
          case ColorSpace.TYPE_Lab:
            b.append("Lab");
          default:
                b.append(p.getPCSType());
        }*/
        b.append('}');
        return b.toString();
    }

    private static String profileClassToString(int profileClass) {
        switch (profileClass) {
            case ICC_Profile.CLASS_ABSTRACT:
                return "abstract";
            case ICC_Profile.CLASS_COLORSPACECONVERSION:
                return "colorspace conversion";
            case ICC_Profile.CLASS_DEVICELINK:
                return "device link";
            case ICC_Profile.CLASS_DISPLAY:
                return "display";
            case ICC_Profile.CLASS_INPUT:
                return "input";
            case ICC_Profile.CLASS_NAMEDCOLOR:
                return "named color";
            case ICC_Profile.CLASS_OUTPUT:
                return "output";
            default:
                return Integer.toString(profileClass);
        }
    }

    private static String colorSpaceTypeToString(int colorSpaceType) {
        switch (colorSpaceType) {
            case ColorSpace.TYPE_CMYK:
                return "CMYK";
            case ColorSpace.TYPE_RGB:
                return "RGB";
            default:
                return Integer.toString(colorSpaceType);
        }
    }

    public static String toString(ICC_Profile p) {
        StringBuilder b = new StringBuilder();
        b.append("ICC_Profile{");
        b.append("version:");
        b.append(p.getMajorVersion());
        b.append('.');
        b.append(p.getMinorVersion());
        b.append(" numComponents:");
        b.append(p.getNumComponents());
        b.append(",colorSpaceType=").append(colorSpaceTypeToString(p.getColorSpaceType()));
        b.append(",profileClass=").append(profileClassToString(p.getProfileClass()));
        b.append(p.getPCSType());
        b.append(p.getProfileClass());
        b.append('}');
        return b.toString();

    }

}

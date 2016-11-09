 /* @(#)ColorSpaces.java
 * Copyright © 2014 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.color;

import java.awt.color.ColorSpace;
import java.awt.color.ICC_ColorSpace;
import java.awt.color.ICC_Profile;

/**
 * ColorSpaces.
 *
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
        b.append(",colorSpaceType=");
        switch (p.getColorSpaceType()) {
            case ColorSpace.TYPE_CMYK:
                b.append("CMYK");
                break;
            case ColorSpace.TYPE_RGB :
                b.append("RGB");
                break;
            default:
                b.append(p.getColorSpaceType());

        }
        b.append(",profileClass=");
        switch(p.getProfileClass()) {
          case ICC_Profile.CLASS_ABSTRACT:
            b.append("abstract");
            break;
          case ICC_Profile.CLASS_COLORSPACECONVERSION:
            b.append("colorspace conversion");
            break;
          case ICC_Profile.CLASS_DEVICELINK:
            b.append("device link");
            break;
          case ICC_Profile.CLASS_DISPLAY:
            b.append("display");
            break;
          case ICC_Profile.CLASS_INPUT:
            b.append("input");
            break;
          case ICC_Profile.CLASS_NAMEDCOLOR:
            b.append("named color");
            break;
          case ICC_Profile.CLASS_OUTPUT:
            b.append("output");
            break;
          default:
                b.append(p.getProfileClass());
        }
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
}

/* @(#)IFDEnumFormatter.java
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package org.monte.media.tiff;

import java.io.UnsupportedEncodingException;

/**
 * Formats byte arrays as string.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-03-22 Created.
 */
public class ASCIIValueFormatter implements ValueFormatter  {

    /** Creates a new enumeration.
     * The enumeration consists of a list of String=Integer pairs.
     */
    public ASCIIValueFormatter() {
    }

    @Override
    public Object format(Object value) {
        if (value instanceof byte[]) {
            try {
                return new String((byte[]) value, "ASCII");
            } catch (UnsupportedEncodingException ex) {
                throw new InternalError("ASCII not supported");
            }
            }
        return value;
    }
    @Override
    public Object prettyFormat(Object value) {
        return format(value);
    }

    @Override
    public String descriptionFormat(Object data) {
       return null;
    }
}

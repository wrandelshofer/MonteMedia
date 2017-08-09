/* @(#)DateValueFormatter.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. Licensed under the MIT License. */

package org.monte.media.tiff;

/**
 * DateValueFormatter.
 *
 * @author Werner Randelshofer
 * @version 1.0 2010-07-24 Created.
 */
public class DateValueFormatter implements ValueFormatter {

    public DateValueFormatter() {
    }

    @Override
    public Object format(Object value) {
        return value;
    }

    @Override
    public Object prettyFormat(Object value) {
        return value;
    }

    @Override
    public String descriptionFormat(Object data) {
       return null;
    }
}

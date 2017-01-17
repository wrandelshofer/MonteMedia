/* @(#)DateValueFormatter.java
 * Copyright © 2010 Werner Randelshofer, Switzerland.
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.media.tiff;

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

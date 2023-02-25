/* @(#)DateValueFormatter.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.tiff;

/**
 * DateValueFormatter.
 *
 * @author Werner Randelshofer
 * @version $Id$
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

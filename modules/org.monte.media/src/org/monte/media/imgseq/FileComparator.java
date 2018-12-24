/* @(#)FileComparator.java
 * Copyright © 2017 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.imgseq;

import java.io.File;
import java.util.Comparator;

/**
 * {@code FileComparator}.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class FileComparator implements Comparator<File> {

    private OSXCollator collator = new OSXCollator();

    @Override
    public int compare(File o1, File o2) {
        return collator.compare(o1.getName(), o2.getName());
    }
}

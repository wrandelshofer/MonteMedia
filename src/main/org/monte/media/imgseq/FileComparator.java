/* @(#)FileComparator.java
 * Copyright © 2011 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */

package org.monte.media.imgseq;

import java.io.File;
import java.util.Comparator;

/**
 * {@code FileComparator}.
 *
 * @author Werner Randelshofer
 * @version 1.0 2011-02-20 Created.
 */
public class FileComparator implements Comparator<File> {
private OSXCollator collator=new OSXCollator();

    @Override
    public int compare(File o1, File o2) {
        return collator.compare(o1.getName(), o2.getName());
    }
}

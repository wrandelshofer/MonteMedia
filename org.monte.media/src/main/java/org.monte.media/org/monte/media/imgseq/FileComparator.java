/*
 * @(#)FileComparator.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.imgseq;

import java.io.File;
import java.util.Comparator;

/**
 * {@code FileComparator}.
 *
 * @author Werner Randelshofer
 */
public class FileComparator implements Comparator<File> {

    private final NaturalSortCollator collator = new NaturalSortCollator();

    @Override
    public int compare(File o1, File o2) {
        return collator.compare(o1.getName(), o2.getName());
    }
}

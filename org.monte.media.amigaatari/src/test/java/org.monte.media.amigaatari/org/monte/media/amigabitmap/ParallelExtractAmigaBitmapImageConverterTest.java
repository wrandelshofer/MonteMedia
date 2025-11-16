/*
 * @(#)ParallelExtractAmigaBitmapImageConverterTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.amigabitmap;

public class ParallelExtractAmigaBitmapImageConverterTest extends AbstractAmigaBitmapImageConverterTest {
    @Override
    protected AmigaBitmapImageConverter newInstance() {
        return new ParallelExtractAmigaBitmapImageConverter();
    }
}
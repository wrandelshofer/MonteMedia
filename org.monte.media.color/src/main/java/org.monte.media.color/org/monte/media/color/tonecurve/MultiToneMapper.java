/*
 * @(#)MultiToneMapper.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.tonecurve;

public class MultiToneMapper implements ToneMapper {
    private final ToneMapper[] mappers;

    public MultiToneMapper(ToneMapper... mappers) {
        this.mappers = mappers.clone();
    }

    @Override
    public float fromLinear(int component, float y) {
        return mappers[component].fromLinear(0, y);
    }

    @Override
    public float toLinear(int component, float x) {
        return mappers[component].toLinear(component, x);
    }
}

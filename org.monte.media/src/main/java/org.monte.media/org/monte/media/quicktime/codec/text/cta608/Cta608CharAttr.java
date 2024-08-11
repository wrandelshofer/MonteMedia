/*
 * @(#)Cta608CharAttr.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text.cta608;

public record Cta608CharAttr(Cta608Color foreground, Cta608Color background, boolean italic, boolean underlined) {
    public Cta608CharAttr withBackground(Cta608Color bg) {
        return bg == null ? this : new Cta608CharAttr(this.foreground, bg, this.italic, this.underlined);
    }

    public Cta608CharAttr withForeground(Cta608Color fg) {
        return fg == null ? this : new Cta608CharAttr(fg, this.background, this.italic, this.underlined);
    }

    public Cta608CharAttr withUnderline(Boolean b) {
        return b == null ? this : new Cta608CharAttr(this.foreground, this.background, this.italic, b);
    }

    public Cta608CharAttr withItalics(Boolean b) {
        return b == null ? this : new Cta608CharAttr(this.foreground, this.background, b, this.underlined);
    }
}

/*
 * @(#)Cta608TextToken.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text.cta608;

public final class TextToken implements Cta608Token {
    private final String text;

    public TextToken(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}

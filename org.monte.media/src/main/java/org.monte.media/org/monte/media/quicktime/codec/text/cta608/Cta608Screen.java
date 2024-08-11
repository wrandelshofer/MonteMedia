/*
 * @(#)Cta608Screen.java
 * Copyright © 2024 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text.cta608;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;

public class Cta608Screen {
    public Cta608Style style = Cta608Style.POP_ON;
    public static final int WIDTH = 32;
    public static final int HEIGHT = 15;
    public static final int CAPACITY = WIDTH * HEIGHT;
    public static final Cta608CharAttr DEFAULT_ATTR = new Cta608CharAttr(Cta608Color.WHITE, Cta608Color.BLACK_SEMI, false, false);

    public final char[] chars = new char[CAPACITY];
    public final Cta608CharAttr[] attrs = new Cta608CharAttr[CAPACITY];

    public char charAt(int x, int y) {
        return chars[y * WIDTH + x];
    }

    public Cta608CharAttr attrAt(int x, int y) {
        return attrs[y * WIDTH + x];
    }

    public void erase() {
        Arrays.fill(chars, '\0');
        Arrays.fill(attrs, DEFAULT_ATTR);
    }

    public int getMinX(int y) {
        int xy = y * WIDTH;
        for (int x = 0; x < WIDTH; x++) {
            if (chars[xy + x] != '\0') return x;
        }
        return WIDTH;
    }

    public int getMaxX(int y) {
        int xy = y * WIDTH;
        for (int x = WIDTH - 1; x >= 0; x--) {
            if (chars[xy + x] != '\0') return x;
        }
        return -1;
    }

    public Rectangle getTextBox() {
        int minX = WIDTH, minY = HEIGHT, maxX = -1, maxY = -1;
        for (int y = 0; y < HEIGHT && minX > 0; y++) {
            minX = Math.min(minX, getMinX(y));
            if (minX < WIDTH) {
                minY = Math.min(minY, y);
            }
        }
        for (int y = HEIGHT - 1; y >= 0 && maxX < WIDTH - 1; y--) {
            maxX = Math.max(maxX, getMaxX(y));
            if (maxX > -1) {
                maxY = Math.max(maxY, y);
            }
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    public void textRestart() {
        for (int i = 0; i < chars.length; i++) {
            chars[i] = chars[i] != '\0' ? ' ' : '\0';
        }
    }

    public Point write(Point pos, Cta608CharAttr attr, String text) {
        int x = Math.clamp(pos.x, 0, WIDTH - 1);
        int y = Math.clamp(pos.y, 0, HEIGHT - 1);
        int length = text.length();
        int xy = y * WIDTH;
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            attrs[xy + x] = attr;
            chars[xy + x] = Character.isISOControl(c) ? ' ' : c;
            x = Math.min(WIDTH - 1, x + 1);
        }
        return new Point(x, y);
    }

    public void deleteToEndOfRow(Point pos) {
        int x = Math.clamp(pos.x, 0, WIDTH - 1);
        int y = Math.clamp(pos.y, 0, HEIGHT - 1);
        int xy = y * WIDTH + x;
        int endOfRow = y * WIDTH + WIDTH;
        Arrays.fill(chars, xy, endOfRow, '\0');
        Arrays.fill(attrs, xy, endOfRow, DEFAULT_ATTR);
    }

    public void rollUp(int numRows) {
        int shift = WIDTH * Math.clamp(numRows, 0, HEIGHT);
        if (shift < CAPACITY) {
            System.arraycopy(chars, shift, chars, 0, CAPACITY - shift);
            System.arraycopy(attrs, shift, attrs, 0, CAPACITY - shift);
        }
        Arrays.fill(chars, CAPACITY - shift, CAPACITY, '\0');
        Arrays.fill(attrs, CAPACITY - shift, CAPACITY, DEFAULT_ATTR);
    }
}

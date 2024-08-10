/*
 * @(#)Cta608Token.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text.cta608;

/**
 * CTA-608 Cta608Token.
 * <p>
 * References:
 * <dl>
 *     <dt>ANSI/CTA Standard. Line 21 Data Services. ANSI/CTA-608-E S-2019. April 2008.</dt>
 *     <dd><a href="https://shop.cta.tech/products/line-21-data-services">ANSI-CTA-608-E-S-2019-Final.pdf</a></dd>
 * </dl>
 */
public sealed interface Cta608Token permits CmdToken, PacToken, TextToken {
    static short fixParityBits(short opCode) {
        int low = opCode & 0x7f;
        int high = opCode & 0x7f00;
        int lowParity = (Integer.bitCount(low) & 1) == 0 ? 0x80 : 0;
        int highParity = (Integer.bitCount(high) & 1) == 0 ? 0x8000 : 0;
        return (short) (opCode & 0x7f7f | lowParity | highParity);
    }
}

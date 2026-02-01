/*
 * @(#)Cta608Memory.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text.cta608;

/// Represents the memory of the closed captioning screen.
///
/// References:
/// <dl>
///     <dt>ANSI/CTA Standard. Digital Television (DTV) Closed Captioning. CTA-708-E S-2023. August 2013.</dt>
///     <dd>[
///         ANSI CTA-708-E S-2023 + Errata Letter and Replacement Pages FINAL.pdf](https://shop.cta.tech/collections/standards/products/digital-television-dtv-closed-captioning)</dd>
/// </dl>
public class Cta608Memory {

    public Cta608Screen displayed = new Cta608Screen();
    public Cta608Screen nonDisplayed = new Cta608Screen();

    public void flipMemories() {
        Cta608Screen flip = displayed;
        displayed = nonDisplayed;
        nonDisplayed = flip;
    }
}

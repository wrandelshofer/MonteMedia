/*
 * @(#)CCDataInputStream.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text.cta708;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Input stream for the payload of a cc_data() struct.
 * <p>
 * The cc_data() structure begins with two header bytes. After this header is a repeating
 * structure of three bytes. The first of the three bytes defines the type of data carried
 * in the next two bytes which are referred to as byte-pairs.
 * <p>
 * ISO EBNF Grammar:
 * <pre>
 *     cc_data()      = cc_data_header , { cc_payload } ;
 *     cc_data_header = uint16BE;
 *     cc_payload         = cc_valid_and_type , cc_data_1, cc_data_2 ;
 *     cc_valid_and_type  = uint8 ; (* validity and type of the two data bytes that follow it. *)
 *     cc_data_1          = uint8 ; (* the first of two data bytes *)
 *     cc_data_2          = uint8 ; (* the second of two data bytes *)
 * </pre>
 * References:
 * <dl>
 *     <dt>ANSI/CTA Standard. Digital Television (DTV) Closed Captioning. CTA-708-E S-2023. August 2013.</dt>
 *     <dd><a href="https://shop.cta.tech/collections/standards/products/digital-television-dtv-closed-captioning">
 *         ANSI CTA-708-E S-2023 + Errata Letter and Replacement Pages FINAL.pdf</a></dd>
 * </dl>
 */
public class CCDataInputStream extends InputStream {
    private InputStream in;
    /**
     * Number of remaining triplets times 2.
     * <p>
     * If remaining is zero, we must read the
     * header of the cc_data struct.
     * <p>
     * If remaining is even, we must read the Flag
     * of the triplet, and then the first
     * Data element of the triplet.
     * <p>
     * If remaining is odd, we must read the second
     * Data element of the triplet.
     */
    long remaining;

    /**
     * Constructs a new instance, which is positioned before a cc_data_header.
     *
     * @param in the underlying input stream
     */
    protected CCDataInputStream(InputStream in) {
        this.in = in;
    }

    /**
     * Constructs a new instance, which is positioned before a cc_data_header
     * if remaining==0, or is positioned before a cc_payload otherwise.
     *
     * @param in        the underlying input stream
     * @param remaining number of remaining payload byte triplets to parse
     */
    protected CCDataInputStream(InputStream in, int remaining) {
        this.in = in;
        this.remaining = Math.max(0, remaining) * 2L;
    }

    private void readHeader() throws IOException {

    }

    @Override
    public int read() throws IOException {
        while (true) {
            if (remaining <= 0) {
                int header = in.read();
                int cc_count = 0;
                while (true) {
                    if (header < 0) return -1;// end of file
                    int headerMarkerBits = header & 0b1010_0000;
                    if (headerMarkerBits != 0b1000_0000) {
                        // this is not a valid header byte => retry with next byte
                        header = in.read();
                        continue;
                    }
                    int reserved = readFully();
                    if (reserved != 0xff) {
                        // this is not a valid reserved byte => retry with this byte
                        header = reserved;
                        continue;
                    }
                    int process_cc_data_flag = header & 0b0100_0000;
                    cc_count = header & 0b11111;
                    if (process_cc_data_flag == 0) {
                        in.skipNBytes(cc_count);
                        header = in.read();
                        continue;
                    }
                    if (cc_count == 0) {
                        header = in.read();
                        continue;
                    }
                    remaining = cc_count * 2;
                    break;
                }
            }
            if ((remaining & 1) == 0) {
                remaining--;
                int cc_valid_and_type = in.read();
                if (cc_valid_and_type < 0) {
                    return -1;
                }
                int cc_valid = cc_valid_and_type & 0b100;
                int cc_type = cc_valid_and_type & 0b11;
                if (cc_valid == 0 || cc_type < 2) {
                    int b = in.read() | in.read();
                    if (b < 0) return -1;
                    remaining--;
                    continue;
                }
                return in.read();
            }
            break;
        }
        remaining--;
        return in.read();
    }

    private int readFully() throws IOException {
        int data = in.read();
        if (data < 0) throw new EOFException();
        return data;
    }
}

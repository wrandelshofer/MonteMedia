/*
 * @(#)Cta708ParserTest.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.quicktime.codec.text.cta708;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled("Parser is not implemented yet")
class Cta708ParserTest {
    @Test
    public void shouldParseCaption() throws IOException {
        // "2125/2997 88000/2997 [KEYFRAME] ih rùü)ýøÿL5þ( þheþavþy þroþckþ mþusþicþ )þ ú  ú  ú  ú  ú  ú  ú  ú  ú  ú  sòà   ~?ÿáengÁ?ÿt â";
        byte[] bytes = {(byte) 0x96, (byte) 0x69, (byte) 0x68, (byte) 0x1f, (byte) 0x7f, (byte) 0x0, (byte) 0x11, (byte) 0x72, (byte) 0xf9, (byte) 0xfc, (byte) 0x94, (byte) 0x29, (byte) 0xfd, (byte) 0x80, (byte) 0x80, (byte) 0xf8, (byte) 0x80, (byte) 0x80, (byte) 0xff, (byte) 0x4c, (byte) 0x35, (byte) 0xfe, (byte) 0x28, (byte) 0x20, (byte) 0xfe, (byte) 0x68, (byte) 0x65, (byte) 0xfe, (byte) 0x61, (byte) 0x76, (byte) 0xfe, (byte) 0x79, (byte) 0x20, (byte) 0xfe, (byte) 0x72, (byte) 0x6f, (byte) 0xfe, (byte) 0x63, (byte) 0x6b, (byte) 0xfe, (byte) 0x20, (byte) 0x6d, (byte) 0xfe, (byte) 0x75, (byte) 0x73, (byte) 0xfe, (byte) 0x69, (byte) 0x63, (byte) 0xfe, (byte) 0x20, (byte) 0x29, (byte) 0xfe, (byte) 0x3, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0x73, (byte) 0xf2, (byte) 0xe0, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x7e, (byte) 0x3f, (byte) 0xff, (byte) 0xe1, (byte) 0x65, (byte) 0x6e, (byte) 0x67, (byte) 0xc1, (byte) 0x3f, (byte) 0xff, (byte) 0x74, (byte) 0x0, (byte) 0x11, (byte) 0xe2};

        Cta708Parser p = new Cta708Parser();
        String actual = p.parse(new ByteArrayInputStream(bytes));
        String expected = "( heavy rock music )";
        System.out.println(actual);
        assertEquals(expected, actual);
    }

    @Test
    public void shouldParseTruncatedCaption() throws IOException {
        // þ( þheþavþy þroþckþ mþusþicþ )þ ú  ú  ú  ú  ú  ú  ú  ú  ú  ú  sòà   ~?ÿáengÁ?ÿt â";
        byte[] bytes = {(byte) 0xfe, (byte) 0x28, (byte) 0x20, (byte) 0xfe, (byte) 0x68, (byte) 0x65, (byte) 0xfe, (byte) 0x61, (byte) 0x76, (byte) 0xfe, (byte) 0x79, (byte) 0x20, (byte) 0xfe, (byte) 0x72, (byte) 0x6f, (byte) 0xfe, (byte) 0x63, (byte) 0x6b, (byte) 0xfe, (byte) 0x20, (byte) 0x6d, (byte) 0xfe, (byte) 0x75, (byte) 0x73, (byte) 0xfe, (byte) 0x69, (byte) 0x63, (byte) 0xfe, (byte) 0x20, (byte) 0x29, (byte) 0xfe, (byte) 0x3, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0xfa, (byte) 0x0, (byte) 0x0, (byte) 0x73, (byte) 0xf2, (byte) 0xe0, (byte) 0x20, (byte) 0x20, (byte) 0x20, (byte) 0x7e, (byte) 0x3f, (byte) 0xff, (byte) 0xe1, (byte) 0x65, (byte) 0x6e, (byte) 0x67, (byte) 0xc1, (byte) 0x3f, (byte) 0xff, (byte) 0x74, (byte) 0x0, (byte) 0x11, (byte) 0xe2};

        Cta708Parser p = new Cta708Parser();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        String actual = p.parse(in);
        String expected = "( heavy rock music )";
        System.out.println(actual);
        assertEquals(expected, actual);
    }

}
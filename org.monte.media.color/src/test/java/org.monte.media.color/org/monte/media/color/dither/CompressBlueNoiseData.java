/*
 * @(#)CompressBlueNoiseData.java
 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color.dither;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.monte.media.io.StreamPosTokenizer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.fail;

public class CompressBlueNoiseData {
    private static void loadDataFromTextFile(InputStream in, float[][] data) throws IOException {
        try (InputStreamReader r = new InputStreamReader(in)) {
            var tt = new StreamPosTokenizer(r);
            tt.slashStarComments(true);
            tt.parseExponents();
            int index = 0;
            int shift = 31 - Integer.numberOfLeadingZeros(data.length);
            while (tt.nextToken() != StreamTokenizer.TT_EOF) {
                switch (tt.ttype) {
                    case StreamTokenizer.TT_NUMBER -> {
                        data[index >> shift][index & (data.length - 1)] = Math.clamp((float) tt.nval, -1f, 1f);
                        index++;
                    }
                    case StreamTokenizer.TT_WORD -> {
                        if (!"f".equals(tt.sval))
                            throw new IOException("Invalid data format at lineno=" + tt.lineno() + " tt.ttype=" + tt.ttype + (tt.ttype > 0 ? " tt.val='" + (char) tt.ttype + "'" : " tt.sval=" + tt.sval));
                    }
                    case '[', ']', '{', '}', ',', ';' -> {
                    }
                    default ->
                            throw new IOException("Invalid data format at lineno=" + tt.lineno() + " tt.ttype=" + tt.ttype + (tt.ttype > 0 ? "  tt.val='" + (char) tt.ttype + "'" : " tt.sval=" + tt.sval));

                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"BlueNoiseData0_128",
            "BlueNoiseData1_128",
            "BlueNoiseData2_128",
            "BlueNoiseData3_128"})
    public void testDataFile128(String filename) throws IOException, URISyntaxException {
        float[][] expected = new float[128][128];
        String floatFile = filename + ".txt";
        try (InputStream in = CompressBlueNoiseData.class.getResourceAsStream(floatFile)) {
            loadDataFromTextFile(in, expected);
        }
        float[][] actual = new float[128][128];
        String hexFile = filename + ".hex";
        try (InputStream in = CompressBlueNoiseData.class.getResourceAsStream(hexFile)) {
            if (in != null) {
                BlueNoiseData128.loadData(in, actual);
            }
        }

        boolean isEqual = true;
        for (int i = 0; i < 128; i++) {
            isEqual &= Arrays.equals(expected[i], actual[i]);
        }

        if (!isEqual) {
            File newHexFile = new File("target", filename + ".hex");
            try (var out = Files.newBufferedWriter(newHexFile.toPath())) {
                out.write(
                        "/*\n" +
                        " * @(#)" + filename +
                        " * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.\n" +
                        " */\n"
                );
                for (int i = 0; i < 128; i++) {
                    for (int j = 0; j < 128; j++) {
                        out.write(Integer.toHexString(Float.floatToIntBits(expected[i][j])));
                        out.write(' ');
                    }
                    out.write('\n');
                }
            }
            System.out.println("data is not equal. New file " + newHexFile.getAbsolutePath());
            fail();
        }

    }
}

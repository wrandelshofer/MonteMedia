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
            while (tt.nextToken() != StreamTokenizer.TT_EOF) {
                switch (tt.ttype) {
                    case StreamTokenizer.TT_NUMBER -> {
                        data[index >> 8][index & 255] = Math.clamp((float) tt.nval, -1f, 1f);
                        index++;
                    }
                    case StreamTokenizer.TT_WORD -> {
                        if (!"f".equals(tt.sval))
                            throw new IOException("Invalid data format at lineno=" + tt.lineno() + " tt.ttype=" + tt.ttype + (tt.ttype > 0 ? " tt.val='" + (char) tt.ttype + "'" : " tt.sval=" + tt.sval));
                    }
                    case '{', '}', ',', ';' -> {
                    }
                    default ->
                            throw new IOException("Invalid data format at lineno=" + tt.lineno() + " tt.ttype=" + tt.ttype + (tt.ttype > 0 ? "  tt.val='" + (char) tt.ttype + "'" : " tt.sval=" + tt.sval));

                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"BlueNoiseData0_256",
            "BlueNoiseData1_256",
            "BlueNoiseData2_256",
            "BlueNoiseData3_256"})
    public void testDataFile(String filename) throws IOException, URISyntaxException {
        float[][] expected = new float[256][256];
        String floatFile = filename + ".txt";
        try (InputStream in = CompressBlueNoiseData.class.getResourceAsStream(floatFile)) {
            loadDataFromTextFile(in, expected);
        }
        float[][] actual = new float[256][256];
        String hexFile = filename + ".hex";
        try (InputStream in = CompressBlueNoiseData.class.getResourceAsStream(hexFile)) {
            BlueNoiseData256.loadData(in, actual);
        }

        boolean isEqual = true;
        for (int i = 0; i < 256; i++) {
            isEqual &= Arrays.equals(expected[i], actual[i]);
        }

        if (!isEqual) {
            File newHexFile = new File("target", filename + ".hex");
            try (var out = Files.newBufferedWriter(newHexFile.toPath())) {
                out.write(
                        """
                                /*
                                 * @(#)BlueNoiseData0_256.hex
                                 * Copyright © 2026 Werner Randelshofer, Switzerland. MIT License.
                                 */
                                """
                );
                for (int i = 0; i < 256; i++) {
                    for (int j = 0; j < 256; j++) {
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

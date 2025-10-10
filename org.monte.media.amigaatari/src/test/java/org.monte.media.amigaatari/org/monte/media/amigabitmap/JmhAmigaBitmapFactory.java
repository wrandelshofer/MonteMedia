/*
 * @(#)JmhAmigaBitmapImage.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.amigabitmap;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * # VM version: JDK 26-ea, OpenJDK 64-Bit Server VM, 26-ea+12-1260
 * # Intel(R) Core(TM) i7-8700B CPU @ 3.20GHz
 * <pre>
 * Benchmark                     (depth)  Mode  Cnt       Score       Error  Units
 * m01ParallelExtractToBitmap          1  avgt    4    5799.860 ±    92.019  ns/op
 * m01ParallelExtractToBitmap          2  avgt    4    9414.867 ±   167.481  ns/op
 * m01ParallelExtractToBitmap          3  avgt    4   13170.348 ±    85.472  ns/op
 * m01ParallelExtractToBitmap          4  avgt    4   17464.886 ±   432.903  ns/op
 * m01ParallelExtractToBitmap          5  avgt    4   21074.581 ±   281.445  ns/op
 * m01ParallelExtractToBitmap          6  avgt    4   37376.487 ±   520.674  ns/op
 * m01ParallelExtractToBitmap          7  avgt    4   40878.658 ±   322.069  ns/op
 * m01ParallelExtractToBitmap          8  avgt    4   44725.539 ±  5210.926  ns/op
 * m01ParallelExtractToBuffered        1  avgt    4    6155.780 ±   211.312  ns/op
 * m01ParallelExtractToBuffered        2  avgt    4    9771.360 ±   220.870  ns/op
 * m01ParallelExtractToBuffered        3  avgt    4   12585.877 ±   323.696  ns/op
 * m01ParallelExtractToBuffered        4  avgt    4   17165.255 ±   469.843  ns/op
 * m01ParallelExtractToBuffered        5  avgt    4   21374.271 ±  1965.655  ns/op
 * m01ParallelExtractToBuffered        6  avgt    4   28374.657 ±  3060.258  ns/op
 * m01ParallelExtractToBuffered        7  avgt    4   34317.437 ±  2172.506  ns/op
 * m01ParallelExtractToBuffered        8  avgt    4   37545.022 ±   566.041  ns/op
 * m02LongMultiplyToBuffered           1  avgt    4   11138.122 ±   884.093  ns/op
 * m02LongMultiplyToBuffered           2  avgt    4   21743.848 ±   211.417  ns/op
 * m02LongMultiplyToBuffered           3  avgt    4   32966.565 ±   237.552  ns/op
 * m02LongMultiplyToBuffered           4  avgt    4   41064.406 ±  1294.827  ns/op
 * m02LongMultiplyToBuffered           5  avgt    4   51583.525 ±   685.677  ns/op
 * m02LongMultiplyToBuffered           6  avgt    4   63479.072 ±   568.360  ns/op
 * m02LongMultiplyToBuffered           7  avgt    4   75189.745 ±   603.555  ns/op
 * m02LongMultiplyToBuffered           8  avgt    4   83740.286 ±  6469.352  ns/op
 * m02LongMultiplyToBitmap             1  avgt    4   11009.536 ±   42.967  ns/op
 * m02LongMultiplyToBitmap             2  avgt    4   20686.472 ±  578.160  ns/op
 * m02LongMultiplyToBitmap             3  avgt    4   29267.826 ± 1218.658  ns/op
 * m02LongMultiplyToBitmap             4  avgt    4   46589.680 ±  596.599  ns/op
 * m02LongMultiplyToBitmap             5  avgt    4   55045.795 ±  594.922  ns/op
 * m02LongMultiplyToBitmap             6  avgt    4   69605.663 ± 1236.752  ns/op
 * m02LongMultiplyToBitmap             7  avgt    4   76408.834 ± 1261.087  ns/op
 * m02LongMultiplyToBitmap             8  avgt    4   88465.215 ± 2680.727  ns/op
 * # Apple M2 Max
 * Benchmark                     (depth)  Mode  Cnt       Score      Error  Units
 * m01ParallelExtractToBitmap          1  avgt    4   19801.942 ±  127.636  ns/op
 * m01ParallelExtractToBitmap          2  avgt    4   34340.086 ±  278.684  ns/op
 * m01ParallelExtractToBitmap          3  avgt    4   49805.743 ±  259.987  ns/op
 * m01ParallelExtractToBitmap          4  avgt    4   64056.068 ± 1824.166  ns/op
 * m01ParallelExtractToBitmap          5  avgt    4   79036.541 ±  724.121  ns/op
 * m01ParallelExtractToBitmap          6  avgt    4   95352.500 ±  952.130  ns/op
 * m01ParallelExtractToBitmap          7  avgt    4  110687.177 ± 1435.553  ns/op
 * m01ParallelExtractToBitmap          8  avgt    4  125357.232 ± 1271.414  ns/op
 * m01ParallelExtractToBuffered        1  avgt    4   19448.624 ±  607.056  ns/op
 * m01ParallelExtractToBuffered        2  avgt    4   38507.438 ±  686.935  ns/op
 * m01ParallelExtractToBuffered        3  avgt    4   55064.892 ±   37.794  ns/op
 * m01ParallelExtractToBuffered        4  avgt    4   71074.170 ± 1543.289  ns/op
 * m01ParallelExtractToBuffered        5  avgt    4   87841.606 ± 4431.516  ns/op
 * m01ParallelExtractToBuffered        6  avgt    4  103295.903 ± 1669.717  ns/op
 * m01ParallelExtractToBuffered        7  avgt    4  118669.142 ± 1426.232  ns/op
 * m01ParallelExtractToBuffered        8  avgt    4  132987.985 ±   45.451  ns/op
 * m02LongMultiplyToBuffered           1  avgt    4    8546.859 ±  204.955  ns/op
 * m02LongMultiplyToBuffered           2  avgt    4   16944.012 ±   79.833  ns/op
 * m02LongMultiplyToBuffered           3  avgt    4   24029.709 ± 1407.378  ns/op
 * m02LongMultiplyToBuffered           4  avgt    4   31046.023 ± 1489.087  ns/op
 * m02LongMultiplyToBuffered           5  avgt    4   37919.887 ± 1911.358  ns/op
 * m02LongMultiplyToBuffered           6  avgt    4   42582.044 ± 1849.463  ns/op
 * m02LongMultiplyToBuffered           7  avgt    4   52615.740 ± 1076.056  ns/op
 * m02LongMultiplyToBuffered           8  avgt    4   60058.511 ±  577.027  ns/op
 * m02LongMultiplyToBitmap             1  avgt    4    6627.204 ±  511.453  ns/op
 * m02LongMultiplyToBitmap             2  avgt    4   11760.858 ±  142.145  ns/op
 * m02LongMultiplyToBitmap             3  avgt    4   16318.853 ±  533.972  ns/op
 * m02LongMultiplyToBitmap             4  avgt    4   23384.981 ± 1545.686  ns/op
 * m02LongMultiplyToBitmap             5  avgt    4   28460.291 ± 1063.741  ns/op
 * m02LongMultiplyToBitmap             6  avgt    4   34045.215 ± 1056.896  ns/op
 * m02LongMultiplyToBitmap             7  avgt    4   38049.140 ±  456.028  ns/op
 * m02LongMultiplyToBitmap             8  avgt    4   45341.827 ±  867.579  ns/op
 */

@Fork(value = 1, jvmArgsAppend = {"-XX:+UnlockExperimentalVMOptions", "--add-modules", "jdk.incubator.vector",
        "--enable-preview"
        //,"-XX:+UnlockDiagnosticVMOptions", "-XX:PrintAssemblyOptions=intel", "-XX:CompileCommand=print,ch/randelshofer/fastdoubleparser/EightDigitsJmh.*"
})
@Measurement(iterations = 4)
@Warmup(iterations = 3)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
@State(Scope.Benchmark)
public class JmhAmigaBitmapFactory {
    public AmigaBitmapImage bitmap;
    public BufferedImage buffered;
    @Param({"1", "2", "3", "4", "5", "6", "7", "8"})
    public int depth;

    @Setup
    public void prepare() {
        byte[] red = new byte[1 << depth];
        byte[] green = new byte[1 << depth];
        byte[] blue = new byte[1 << depth];
        IndexColorModel colorModel = new IndexColorModel(depth, 1 << depth, red, green, blue);
        bitmap = new AmigaBitmapImage(320, 240, depth, colorModel);
        buffered = new BufferedImage(320, 240, BufferedImage.TYPE_BYTE_INDEXED, colorModel);
        var rng = new Random(0);
        rng.nextBytes(bitmap.getBitmap());
        rng.nextBytes(((DataBufferByte) buffered.getRaster().getDataBuffer()).getData());
    }

    // @Benchmark
    public AmigaBitmapImage m01ParallelExtractToBitmap() {
        return new ParallelExtractAmigaBitmapImageConverter().toBitmapImage(buffered, bitmap);
    }

    //  @Benchmark
    public BufferedImage m01ParallelExtractToBuffered() {
        return new ParallelExtractAmigaBitmapImageConverter().toBufferedImage(bitmap, buffered);
    }

    @Benchmark
    public AmigaBitmapImage m02LongMultiplyToBitmap() {
        return new LongMultiplyAmigaBitmapImageConverter().toBitmapImage(buffered, bitmap);
    }

    //@Benchmark
    public BufferedImage m02LongMultiplyToBuffered() {
        return new LongMultiplyAmigaBitmapImageConverter().toBufferedImage(bitmap, buffered);
    }

}
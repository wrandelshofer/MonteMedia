/*
 * @(#)AbstractNamedColorSpaceTest.java
 * Copyright © 2025 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.media.color;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.monte.media.math.FloatFunction;

import java.awt.color.ColorSpace;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.monte.media.math.Scalars.almostEqual;


public abstract class AbstractNamedColorSpaceTest {
    /// Number of precision bits (used by most tests in this class).
    static final float EPSILON = 0x1p-12f;
    /// Smaller epsilon for component->XYZ->component round-trips.
    ///
    /// The worst offender is [ParametricScaledColorSpace].
    static final float MINI_EPSILON = 0x1p-13f;
    /// Number of precision bits (used for comparing the Java sRGB-ColorSpace implementation
    /// with the test instance).
    private static final float EPSILON_EXPECTED_XYZ = 0x1p-10f;

    protected abstract NamedColorSpace getInstance();

    @Test
    public void shouldHaveExpectedComponentNames() {
        NamedColorSpace cs = getInstance();
        switch (cs.getType()) {

        }
        String[] expectedCompName = switch (cs.getType()) {
            case ColorSpace.TYPE_XYZ -> new String[]{"X", "Y", "Z"};
            case ColorSpace.TYPE_Lab -> new String[]{"L", "a", "b"};
            case ColorSpace.TYPE_Luv -> new String[]{"L", "u", "v"};
            case ColorSpace.TYPE_YCbCr -> new String[]{"Y", "Cb", "Cr"};
            case ColorSpace.TYPE_Yxy -> new String[]{"Y", "x", "y"};
            case ColorSpace.TYPE_RGB -> new String[]{"Red", "Green", "Blue"};
            case ColorSpace.TYPE_GRAY -> new String[]{"Gray"};
            case ColorSpace.TYPE_HSV -> new String[]{"Hue", "Saturation", "Value"};
            case ColorSpace.TYPE_HLS -> new String[]{"Hue", "Lightness", "Saturation"};
            case ColorSpace.TYPE_CMYK -> new String[]{"Cyan", "Magenta", "Yellow",
                    "Black"};
            case ColorSpace.TYPE_CMY -> new String[]{"Cyan", "Magenta", "Yellow"};
            case NamedColorSpace.TYPE_LCH -> new String[]{"Lightness", "Chroma", "Hue"};
            default -> {
                String[] tmp = new String[cs.getNumComponents()];
                for (int i = 0; i < tmp.length; i++) {
                    tmp[i] = "Unnamed color component(" + i + ")";
                }
                yield tmp;
            }
        };

        String[] actualCompName = new String[cs.getNumComponents()];
        for (int i = 0; i < actualCompName.length; i++) {
            actualCompName[i] = cs.getName(i);
        }
        assertArrayEquals(expectedCompName, actualCompName);
    }

    @Test
    public void shouldBijectWithTransferFunction() {
        if (!(getInstance() instanceof ParametricNonLinearRgbColorSpace cs)) {
            return;
        }
        FloatFunction toLinear = cs.getToLinear();
        FloatFunction fromLinear = cs.getFromLinear();

        // should biject with values in range
        for (int i = 0; i < 256; i++) {
            float value = i / 255f;

            float linear = toLinear.apply(value);
            float actualValue = fromLinear.apply(linear);

            assertEquals(value, actualValue, EPSILON, "i=" + i + " linear=" + linear);
        }

        // should biject with values out of positive range
        for (int i = 256; i < 512; i++) {
            float value = i / 255f;

            float linear = toLinear.apply(value);
            float actualValue = fromLinear.apply(linear);

            assertEquals(value, actualValue, EPSILON, "i=" + i + " linear=" + linear);
        }

        // should biject with values out of negative range
        for (int i = -512; i < 0; i++) {
            float value = i / 255f;

            float linear = toLinear.apply(value);
            float actualValue = fromLinear.apply(linear);

            assertEquals(value, actualValue, EPSILON, "i=" + i + " linear=" + linear);
        }
    }

    @Test
    public void shouldBijectWithSrgb() {
        NamedColorSpace cs = getInstance();
        AtomicInteger failures = new AtomicInteger();
        IntStream.range(0, 1 << 24).parallel().forEach(
                (rgb) -> {
                    float[] rgbf = new float[3];
                    float[] actualRgbf = new float[3];
                    float[] componentf = new float[cs.getNumComponents()];
                    RgbBitConverters.rgb24ToRgbFloat(rgb, rgbf);
                    cs.fromRGB(rgbf, componentf);
                    cs.toRGB(componentf, actualRgbf);
                    RgbBitConverters.rgbFloatToArgb32(actualRgbf);
                    int actualRgb = RgbBitConverters.rgbFloatToArgb32(actualRgbf);
                    try {
                        assertEquals(rgb | 0xff000000, actualRgb);
                        //assertArrayEquals(rgbf, actualRgbf, EPSILON);
                    } catch (AssertionError e) {
                        if (failures.get() < 10) {
                            String message =
                                    "\ninput rgb: " + Integer.toHexString(rgb | 0xff000000)
                                            + "\ninput rgbf: " + Arrays.toString(rgbf)
                                            + "\ncomponentf: " + Arrays.toString(componentf)
                                            + "\noutput rgb: " + Integer.toHexString(actualRgb)
                                            + "\noutput rgbf: " + Arrays.toString(actualRgbf);
                            System.out.println(message);
                        }
                        failures.incrementAndGet();
                    }
                });
        assertTrue(failures.get() < 1, "too many failures=" + failures.get());
    }

    @Test
    public void shouldBijectWithXyzForAllSrgbValues() {
        NamedColorSpace cs = getInstance();
        AtomicInteger failures = new AtomicInteger();
        IntStream.range(0, 1 << 24)
                .parallel()
                .forEach((rgb) -> {
                    float[] rgbf = new float[3];
                    float[] actualRgbf = new float[3];
                    float[] componentf = new float[cs.getNumComponents()];
                    float[] actualComponentf = new float[cs.getNumComponents()];
                    float[] xyzf = new float[3];
                    RgbBitConverters.rgb24ToRgbFloat(rgb, rgbf);
                    RgbBitConverters.rgb24ToRgbFloat(rgb, rgbf);
                    cs.fromRGB(rgbf, componentf);

                    cs.toCIEXYZ(componentf, xyzf);
                    cs.fromCIEXYZ(xyzf, actualComponentf);

                    cs.toRGB(actualComponentf, actualRgbf);

                    try {
                        float eps0 = (cs.getMaxValue(0) - cs.getMinValue(0)) * EPSILON;
                        float eps1 = (cs.getMaxValue(1) - cs.getMinValue(1)) * EPSILON;
                        float eps2 = (cs.getMaxValue(2) - cs.getMinValue(2)) * EPSILON;
                        if (cs.getType() == NamedColorSpace.TYPE_LCH && almostEqual(cs.getMinValue(1), componentf[1], eps1)) {
                            // When chroma is almost at min, then hue is powerless and can be ignored.
                            assertEquals(componentf[0], actualComponentf[0], eps0, cs.getName(0));
                            assertEquals(componentf[1], actualComponentf[1], eps1, cs.getName(1));

                        } else if (cs.getType() == NamedColorSpace.TYPE_LCH
                                && (almostEqual(cs.getMinValue(2), componentf[2], eps2) || almostEqual(cs.getMaxValue(2), componentf[2], eps2))) {
                            // When hue is almost at min it is acceptable if it is at almost at max
                            assertEquals(componentf[0], actualComponentf[0], eps0, cs.getName(0));
                            assertEquals(componentf[1], actualComponentf[1], eps1, cs.getName(1));
                            assertTrue(almostEqual(componentf[2], actualComponentf[2], eps2)
                                    || almostEqual(cs.getMinValue(2), actualComponentf[2], eps2)
                                    || almostEqual(cs.getMaxValue(2), actualComponentf[2], eps2), cs.getName(2));

                        } else if (cs.getType() == ColorSpace.TYPE_HSV && almostEqual(cs.getMinValue(1), componentf[1], eps1)) {
                            // When saturation is almost min, then hue is powerless and can be ignored.
                            assertEquals(componentf[1], actualComponentf[1], eps1, cs.getName(1));
                            assertEquals(componentf[2], actualComponentf[2], eps2, cs.getName(2));
                        } else if (cs.getType() == ColorSpace.TYPE_HSV && almostEqual(cs.getMinValue(2), componentf[2], eps2)) {
                            // When value is almost min, then hue is powerless and can be ignored.
                            assertEquals(componentf[1], actualComponentf[1], eps1, cs.getName(1));
                            assertEquals(componentf[2], actualComponentf[2], eps2, cs.getName(2));
                        } else if (cs.getType() == ColorSpace.TYPE_HSV
                                && (almostEqual(cs.getMinValue(0), componentf[0], eps0) || almostEqual(cs.getMaxValue(0), componentf[0], eps0))) {
                            // When hue is almost at max it is acceptable if it is at almost at min
                            assertEquals(componentf[1], actualComponentf[1], eps1, cs.getName(1));
                            assertEquals(componentf[2], actualComponentf[2], eps2, cs.getName(2));
                            assertTrue(almostEqual(componentf[0], actualComponentf[0], eps0)
                                            || almostEqual(cs.getMinValue(0), actualComponentf[0], eps0)
                                            || almostEqual(cs.getMaxValue(0), actualComponentf[0], eps0),
                                    cs.getName(0) + " wrap around max/min");

                        } else if (cs.getType() == ColorSpace.TYPE_HLS
                                && (almostEqual(cs.getMinValue(1), componentf[1], eps1)
                                || almostEqual(cs.getMaxValue(1), componentf[1], eps1))) {
                            // When lightness is almost min or almost max, then hue and saturation are powerless and can be ignored.
                            assertEquals(componentf[1], actualComponentf[1], eps1, cs.getName(1));
                        } else if (cs.getType() == ColorSpace.TYPE_HLS && almostEqual(cs.getMinValue(2), componentf[2], eps2)) {
                            // When saturation is almost min, then hue is powerless and can be ignored.
                            assertEquals(componentf[1], actualComponentf[1], eps1, cs.getName(1));
                            assertEquals(componentf[2], actualComponentf[2], eps2, cs.getName(2));
                        } else if (cs.getType() == ColorSpace.TYPE_HLS
                                && (almostEqual(cs.getMinValue(0), componentf[0], eps0) || almostEqual(cs.getMaxValue(0), componentf[0], eps0))) {
                            // When hue is almost at max it is acceptable if it is at almost at min
                            assertEquals(componentf[1], actualComponentf[1], eps1, cs.getName(1));
                            assertEquals(componentf[2], actualComponentf[2], eps2, cs.getName(2));
                            assertTrue(almostEqual(componentf[0], actualComponentf[0], eps0)
                                    || almostEqual(cs.getMinValue(0), actualComponentf[0], eps0)
                                    || almostEqual(cs.getMaxValue(0), actualComponentf[0], eps0), cs.getName(0));
                        } else {
                            assertEquals(componentf[0], actualComponentf[0], eps0, cs.getName(0));
                        }
                        int actualRgb = RgbBitConverters.rgbFloatToRgb24(actualRgbf);
                        assertEquals(rgb, actualRgb, "RGB");
                    } catch (AssertionError e) {
                        if (failures.get() < 10) {
                            String message =
                                    "\ninitial rgb : " + Integer.toHexString(rgb)
                                            + "\ninitial rgbf: " + Arrays.toString(rgbf)
                                            + "\ninitial componentf: " + Arrays.toString(componentf)
                                            + "\nxyz: " + Arrays.toString(xyzf)
                                            + "\nactual  componentf: " + Arrays.toString(actualComponentf)
                                            + "\nactual  rgbf: " + Arrays.toString(actualRgbf)
                                            + "\nactual  rgb: " + Integer.toHexString(RgbBitConverters.rgbFloatToRgb24(actualRgbf))
                                            + "\n" + e.getMessage();
                            System.out.println(message);
                        }
                        failures.incrementAndGet();
                    }
                });
        assertTrue(failures.get() == 0, "too many failures=" + failures.get());
    }

    @ParameterizedTest
    @CsvSource({
            // name r g b
            "red,1,0,0",
            "green,0,1,0",
            "blue,0,0,1",
            "white,1,1,1",
    })
    public void shouldHaveExpectedXYZValues(String name, float r, float g, float b) {
        float[] inputRgb = {r, g, b};

        ColorSpace referenceCs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        float[] referenceComponent = referenceCs.fromRGB(inputRgb);
        float[] referenceXyz = referenceCs.toCIEXYZ(referenceComponent);

        NamedColorSpace instance = getInstance();
        float[] actualComponent = instance.fromRGB(inputRgb);
        float[] actualXYZ = instance.toCIEXYZ(actualComponent);
        assertArrayEquals(referenceXyz, actualXYZ, EPSILON_EXPECTED_XYZ, name + " rgb->component->XYZ");

        // check round-trip error
        float[] roundtripComponent = instance.fromCIEXYZ(actualXYZ);
        float[] roundtripRgb = instance.toRGB(roundtripComponent);
        int type = instance.getType();
        if (type != ColorSpace.TYPE_HSV
                && type != ColorSpace.TYPE_HLS
                && type != NamedColorSpace.TYPE_LCH) {
            assertArrayEquals(actualComponent, roundtripComponent, MINI_EPSILON, name + " component->XYZ->component");
        }
        assertArrayEquals(inputRgb, roundtripRgb, EPSILON, name + " rgb->component->XYZ->component->rgb");
    }

}
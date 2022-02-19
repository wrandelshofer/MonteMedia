/* @(#)Main.java
 * Copyright © 2021 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.animconverter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Converts an Amiga IFF Cell Animation file into a QuickTime movie file.
 * <p>
 * The resulting video file can be converted into H.264
 * using the following command:
 * <p>
 * Mostly lossless encoding, will only work in Chrome:
 * <pre>
 *     ffmpeg -i Baron.anim.mov -vcodec h264 -pix_fmt yuv444p -profile:v high444 -crf 0 -preset:v slow Baron.mp4
 * </pre>
 * H.264 Baseline Level 3 encoding, will work in most browsers:
 * We upscale the video to reduce artefacts due to yuv420.
 * <pre>
 *     ffmpeg -i Baron.anim.mov -vf "scale=iw*4:ih*4" -sws_flags neighbor -vcodec h264 -pix_fmt yuv420p -profile:v baseline -level 3 -preset:v slow -max_muxing_queue_size 9999 Baron.mp4
 * </pre>
 * For small looping animations without sound, conversion to APNG
 * is also a good possibility:
 * <pre>
 *     ffmpeg -i Baron.anim.mov -sws_flags neighbor -f apng -plays 0 -vf "scale=iw*2:ih*2,split[s0][s1];[s0]palettegen[p];[s1][p]paletteuse" Baron.png
 * </pre>
 */
public class Main {
    public static void main(String... args) throws IOException {
        Map<String, String> options = parseArgs(args);
        if (options.containsKey(HELP_KEY)
                || !options.containsKey(INPUTFILE_KEY)) {
            String version = Main.class.getPackage().getImplementationVersion();
            System.out.println("ANIMConverter" + (version == null ? "" : " " + version));
            System.out.println("Converts an Amiga IFF Cell Animation into a QuickTime movie.");
            System.out.println("Copyright © Werner Randelshofer, Switzerland.");
            System.out.println("License: MIT License");
            System.out.println("");
            System.out.println("Usage:");
            System.out.println("ANIMConverter [options] inputfile [outputfile]");
            System.out.println("  -h          show help");
            System.out.println("  -?          show help");
            System.out.println("  -help       show help");
            System.out.println("  --help      show help");
            System.out.println("  --swap-left-right-channels       swaps the left and right audio channel");
            System.out.println("  -xaspect    int   pixel x-aspect ratio, >= 1");
            System.out.println("  -yaspect    int   pixel y-aspect ratio, >= 1");
            System.out.println("  -frameduration    int   duration of a frame in jiffies, >= 1");
            System.out.println("  inputfile   Amiga IFF Cell Animation file, zip file or directory");
            System.out.println("  outputfile  QuickTime movie file");
            System.out.println("              If this argument is omitted, then the output filename ");
            System.out.println("              is the input filename with \".mov\" appended. ");
            System.exit(0);
        }

        if (!options.containsKey(OUTPUTFILE_KEY)) {
            options.put(OUTPUTFILE_KEY, options.get(INPUTFILE_KEY) + ".mov");
        }


        AnimToQuickTimeConverter converter = new AnimToQuickTimeConverter();

        converter.setXAspect(parseInteger(options.get(X_ASPECT)));
        converter.setYAspect(parseInteger(options.get(Y_ASPECT)));
        converter.setFrameDuration(parseInteger(options.get(FRAME_DURATION)));

        converter.setSwapLeftRightChannels(options.containsKey(SWAP_LEFT_RIGHT_CHANNELS));
        converter.convert(
                options.get(INPUTFILE_KEY),
                options.get(OUTPUTFILE_KEY));


    }

    private static Integer parseInteger(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NullPointerException|NumberFormatException e) {
            return null;
        }
    }

    private final static String HELP_KEY = "-h";
    private final static String X_ASPECT = "-xaspect";
    private final static String Y_ASPECT = "-yaspect";
    private final static String FRAME_DURATION = "-frameduration";
    private final static String SWAP_LEFT_RIGHT_CHANNELS = "--swap-left-right-channels";
    private final static String INPUTFILE_KEY = "-i";
    private final static String OUTPUTFILE_KEY = "-o";

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> options = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
            case "-h":
            case "-?":
            case "-help":
            case "--help":
                options.put(HELP_KEY, args[i]);
                break;
            case SWAP_LEFT_RIGHT_CHANNELS:
                options.put(SWAP_LEFT_RIGHT_CHANNELS, args[i]);
                break;
            case X_ASPECT:
            case Y_ASPECT:
            case FRAME_DURATION:
                if (i < args.length - 1) {
                        options.put(args[i], args[++i]);
                } else {
                    System.err.println("Argument required after \"" + args[i] + "\".");
                }
                break;
            default:
                if (args[i].startsWith("-")) {
                    System.err.println("Unrecognized option \"" + args[i] + "\".");
                    System.exit(10);
                }
                if (!options.containsKey(INPUTFILE_KEY)) {
                    options.put(INPUTFILE_KEY, args[i]);
                } else if (!options.containsKey(OUTPUTFILE_KEY)) {
                    options.put(OUTPUTFILE_KEY, args[i]);
                } else {
                    System.err.println("Unrecognized argument \"" + args[i] + "\".");
                    System.exit(10);
                }
                break;
            }
        }
        return options;
    }
}

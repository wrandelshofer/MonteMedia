/* @(#)TrimDemoMain.java
 * Copyright © 2012 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package org.monte.iodemo;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import org.monte.media.Buffer;
import org.monte.media.BufferFlag;
import org.monte.media.Codec;
import org.monte.media.Format;
import org.monte.media.FormatFormatter;
import org.monte.media.FormatKeys;
import static org.monte.media.FormatKeys.*;
import org.monte.media.MovieReader;
import org.monte.media.MovieWriter;
import org.monte.media.Registry;
import org.monte.media.converter.AdjustTimeCodec;
import org.monte.media.converter.CodecChain;
import org.monte.media.converter.TrimTimeCodec;
import org.monte.media.math.Rational;

/**
 * Demonstrates how to trim a movie file without re-encoding the entire media
 * data.  This demo is more complex than {@code ConcatDemoMain}, because we
 * need to re-encode the first video frame, if the movie is cut at a
 * non-keyframe.
 *
 * @author Werner Randelshofer
 * @version $Id: TrimDemoMain.java 364 2016-11-09 19:54:25Z werner $
 */
public class TrimDemoMain {

    /**
     * Main function.  Takes one output file and one or more input files as
     * arguments. Concatenates all input files into the output file.
     * 
     * TrimDemo [-o outputfile] [-i inputfile ...] [-s rational] [-e rational]
     * 
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Use hardcoded arguments for debugging
        /*
        args = new String[]{//
            "-i",
            "/Users/werni/Movies/Poker.avi", //
            "-o",
            "/Users/werni/Movies/PokerTrim.avi",//
            "-ss", "00:00:01",//
        "-t", "00:05:00",//
        };
        */
        
        // Parse arguments
        File outfile = null;
        ArrayList<File> infiles = new ArrayList<File>();
        Rational start = null;
        Rational end = null;
        String startString = null, durationString = null;
        try {
            char arg = ' ';
            for (int i = 0; i < args.length; i++) {
                if (args[i].length() > 1 && args[i].charAt(0) == '-') {
                    arg = args[i].charAt(1);
                    if (!args[i].matches("-i|-o|-ss|-t")) {
                        throw new IllegalArgumentException("error: illegal option: " + args[i]);
                    }
                } else {
                    switch (arg) {
                        case 'o':
                            if (outfile != null) {
                                throw new IllegalArgumentException("error: only one outputfile allowed");
                            }
                            outfile = new File(args[i]);
                            break;
                        case 'i':
                            infiles.add(new File(args[i]));
                            break;
                        case 's':
                            startString = args[i];
                            break;
                        case 't':
                            durationString = args[i];
                            break;
                        default:
                            throw new IllegalArgumentException("error: illegal option: " + args[i]);
                    }
                }
            }
            if (outfile == null) {
                if (infiles.isEmpty()) {
                    throw new IllegalArgumentException("error: no inputfiles specified");
                }
                info(infiles);
                return;

            }
            /*
             if (outfile.exists()) {
             throw new IllegalArgumentException("error: outputfile exists: " + outfile);
             }*/
            for (File f : infiles) {
                if (!f.exists()) {
                    throw new IllegalArgumentException("error: inputfile does not exist: " + f);
                }
            }

            { // Parse the start time and duration values
                // This may require reading the first movie file.
                MovieReader[] r = new MovieReader[1];
                try {
                    start = parseTime(startString, infiles, r);
                    Rational duration = parseTime(durationString, infiles, r);
                    if (duration != null) {
                        end = (start == null) ? duration : start.add(duration);

                    }

                } catch (IOException ex) {
                    ex.printStackTrace();
                } finally {
                    if (r[0] != null) {
                        try {
                            r[0].close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            }

            if (start != null || end != null) {
                System.out.println("Trimming input files at seconds. start:" + (start == null ? "start of movie" : start.toDescriptiveString()) + ", end:" + (end == null ? "end of movie" : end.toDescriptiveString()));
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("usage: java -jar TrimDemo.jar [-o outputfile] [-i inputfile ...] [-ss startTime] [-t duration]");
            System.err.println("       -o Output file. Filename. e.g. movie.avi");
            System.err.println("       -i Input file. More than one can be specified. e.g. movie1.avi movie2.avi");
            System.err.println("       -ss Start time. Double, rational, or hh:mm:ss.frame. e.g. 1.5 or 3/2 or 00:00:01.15");
            System.err.println("       -t Duration. Double, rational, or hh:mm:ss.frame");
            String version = TrimDemoMain.class.getPackage().getImplementationVersion();
            System.err.println("TrimDemo " + (version == null ? "" : version + " ") + "(c) Werner Randelshofer");
            System.exit(10);
        }
        try {
            concat(outfile, infiles, start, end);
        } catch (Throwable e) {
            e.printStackTrace();
            System.err.println("error: " + e.getMessage());
        }
    }

    /**
     * Prints an info about each input file.
     */
    private static void info(ArrayList<File> infiles) {
        for (File f : infiles) {
            System.out.println("Movie: " + f);
            if (!f.exists()) {
                System.out.println("  File does not exist.");
                continue;
            }
            MovieReader in = Registry.getInstance().getReader(f);
            try {
                info(in);
            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    in.close();
                } catch (IOException ex) {
                    //
                }
            }
            System.out.println();
        }
    }

    /**
     * Prints an info about an output file.
     */
    private static void info(MovieWriter w) throws IOException {
        System.out.println("  Format: " + FormatFormatter.toString(w.getFileFormat()));
        //System.out.println("  Duration: " + in.getDuration().toDescriptiveString() + " seconds");
        for (int t = 0; t < w.getTrackCount(); t++) {
            System.out.println("  Track " + t);
            System.out.println("    Format: " + FormatFormatter.toString(w.getFormat(t)));
            System.out.println("    Duration: " + w.getDuration(t).toDescriptiveString() + " seconds");
        }

    }

    /**
     * Prints an info about an input file.
     */
    private static void info(MovieReader in) throws IOException {
        System.out.println("  Format: " + FormatFormatter.toString(in.getFileFormat()));
        System.out.println("  Duration: " + in.getDuration().toDescriptiveString() + " seconds");
        for (int t = 0; t < in.getTrackCount(); t++) {
            System.out.println("  Track " + t);
            System.out.println("    Format: " + FormatFormatter.toString(in.getFormat(t)));
            System.out.println("    Duration: " + in.getDuration(t).toDescriptiveString() + " seconds");
            System.out.println("    Chunk Count: " + in.getChunkCount(t));
        }
    }

    private static void concat(File outfile, ArrayList<File> infiles, Rational startTime, Rational endTime) throws IOException {
        MovieWriter out = Registry.getInstance().getWriter(outfile);
        if (out == null) {
            throw new IOException("output file format not supported for: " + outfile);
        }
        try {
            // -------------------
            // Analyze input files
            // -------------------

            // For each input track in each input file, find a matching output track.
            // If no matching output track has been found, create a new output track.
            int[][] matchingT = new int[infiles.size()][0];
            for (int i = 0, imax = infiles.size(); i < imax; i++) {
                File infile = infiles.get(i);
                MovieReader in = Registry.getInstance().getReader(infile);
                if (in == null) {
                    throw new IOException("input file format not supported for: " + infile);
                }
                System.out.println(infile);
                info(in);

                try {
                    matchingT[i] = new int[in.getTrackCount()];
                    for (int t = 0, jmax = in.getTrackCount(); t < jmax; t++) {
                        matchingT[i][t] = -1;
                        for (int outputTrack = 0, nOutputTracks = out.getTrackCount(); outputTrack < nOutputTracks; outputTrack++) {
                            if (in.getFormat(t).matches(out.getFormat(outputTrack))) {
                                // if a movie has multiple tracks with the same format,
                                // we assign them to multiple output tracks
                                for (int tt = 0; tt < t; tt++) {
                                    if (matchingT[i][tt] == outputTrack) {
                                        continue;
                                    }
                                }
                                matchingT[i][t] = outputTrack;
                                break;
                            }
                        }
                        if (matchingT[i][t] == -1) {
                            matchingT[i][t] = out.getTrackCount();
                            out.addTrack(in.getFormat(t));
                        }
                    }

                } finally {
                    in.close();
                }
            }

            // -----------------
            // Set up the codecs
            // -----------------
            int trackCount = out.getTrackCount();



            Codec[] ensureFirstFrameIsKeyframe = new Codec[trackCount];
            Codec[] passThrough = new Codec[trackCount];
            AdjustTimeCodec[] adjustTime = new AdjustTimeCodec[trackCount];
            for (int t = 0; t < ensureFirstFrameIsKeyframe.length; t++) {
                Codec decode = Registry.getInstance().getDecoder(out.getFormat(t));
                decode.setOutputFormat(decode.getOutputFormats(out.getFormat(t))[0]);
                Codec encode = Registry.getInstance().getCodec(decode.getOutputFormat(), out.getFormat(t));
                adjustTime[t] = new AdjustTimeCodec();
                TrimTimeCodec trimTime = new TrimTimeCodec();
                trimTime.setStartTime(startTime);
                trimTime.setEndTime(endTime);
                ensureFirstFrameIsKeyframe[t] = CodecChain.createCodecChain(adjustTime[t], decode, trimTime, encode);
                passThrough[t] = CodecChain.createCodecChain(adjustTime[t], trimTime);
            }

            // For each input file, write all media samples into the matching
            // output tracks.
            Buffer inBuf = new Buffer();
            Buffer outBuf = new Buffer();


            int tracksNeeded = (1 << trackCount) - 1;
            int tracksDone = 0;


            // -----------------------
            // Process the input files
            // -----------------------
            for (int i = 0, imax = infiles.size(); i < imax; i++) {
                File infile = infiles.get(i);
                MovieReader in = Registry.getInstance().getReader(infile);
                if (in == null) {
                    throw new IOException("input file format not supported for: " + infile);
                }

                // Speed up 1: Skip to start time on the first movie file
                if (i == 0 && startTime != null) {
                    in.setMovieReadTime(startTime);
                    for (int t = 0; t < in.getTrackCount(); t++) {
                        int outTrack = matchingT[i][t];
                        adjustTime[outTrack].setMediaTime(in.getReadTime(t));
                    }
                }

                try {
                    for (int t = in.nextTrack(); t != -1; t = in.nextTrack()) {
                        int outTrack = matchingT[i][t];

                        in.read(t, inBuf);

                        int state;
                        if (out.isEmpty(outTrack)) {
                            state = ensureFirstFrameIsKeyframe[outTrack].process(inBuf, outBuf);
                        } else {
                            state = passThrough[outTrack].process(inBuf, outBuf);
                        }


                        // Speed up 2: Stop if all tracks are done
                        if (endTime != null && outBuf.timeStamp.compareTo(endTime) > 0) {
                            tracksDone |= 1 << outTrack;
                            if (tracksDone == tracksNeeded) {
                                break;
                            }
                        }
                        out.write(outTrack, outBuf);
                    }

                } finally {
                    in.close();
                }
            }

            System.out.println(outfile);
            info(out);

        } finally {
            out.close();
        }
    }

    private static Rational parseTime(String str, ArrayList<File> infiles, MovieReader[] r) throws IOException {
        if (str != null) {
            try {
                return Rational.valueOf(str);
            } catch (NumberFormatException e) {
                if (r[0] == null && !infiles.isEmpty()) {
                    r[0] = Registry.getInstance().getReader(infiles.get(0));
                }
                if (r[0] != null) {
                    int t = r[0].findTrack(0, new Format(MediaTypeKey, MediaType.VIDEO));
                    if (t != -1) {
                        Format f = r[0].getFormat(t);
                        Rational frameRate = f.get(FrameRateKey);

                        long seconds = 0, frame = 0;
                        StreamTokenizer tt = new StreamTokenizer(new StringReader(str));
                        tt.resetSyntax();
                        tt.wordChars('0', '9');
                        if (tt.nextToken() != StreamTokenizer.TT_WORD) {
                            throw new NumberFormatException("hh:mm:ss.frame, hours missing: " + str);
                        }
                        seconds += Long.valueOf(tt.sval) * 3600;
                        if (tt.nextToken() != ':') {
                            throw new NumberFormatException("hh:mm:ss.frame, 1st ':' missing: " + str);
                        }
                        if (tt.nextToken() != StreamTokenizer.TT_WORD) {
                            throw new NumberFormatException("hh:mm:ss.frame, minutes missing: " + str);
                        }
                        seconds += Long.valueOf(tt.sval) * 60;
                        if (tt.nextToken() != ':') {
                            throw new NumberFormatException("hh:mm:ss.frame, 2nd ':' missing: " + str);
                        }
                        if (tt.nextToken() != StreamTokenizer.TT_WORD) {
                            throw new NumberFormatException("hh:mm:ss, seconds missing: " + str);
                        }
                        seconds += Long.valueOf(tt.sval);
                        if (tt.nextToken() == '.') {// frame number is optional
                            if (tt.nextToken() != StreamTokenizer.TT_WORD) {
                                throw new NumberFormatException("hh:mm:ss.frame, frames missing: " + str);
                            }
                            frame += Long.valueOf(tt.sval);
                        }
                        return new Rational(seconds).add(new Rational(frame).divide(frameRate));
                    }
                }
            }
        }
        return null;
    }
}

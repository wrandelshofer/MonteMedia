/* @(#)ConcatDemoMain.java
 * Copyright © 2012 Werner Randelshofer, Switzerland. 
 * You may only use this software in accordance with the license terms.
 */
package ru.sbtqa.monte.iodemo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import ru.sbtqa.monte.media.Buffer;
import ru.sbtqa.monte.media.MovieReader;
import ru.sbtqa.monte.media.MovieWriter;
import ru.sbtqa.monte.media.Registry;

/**
 * Demonstrates how to concatenate multiple movie files without re-encoding the
 * media data. <p> Caveat: If an input file has multiple tracks of different
 * durations, then the audio/video sync of subsequent input file will drift. On
 * the upside this allows to merge a file which only contains video with a file
 * that only contains audio.
 *
 * @author Werner Randelshofer
 * @version $Id$
 */
public class ConcatDemoMain {

    /**
     * Main function. <p> Takes one output file and one or more input files as
     * arguments. Concatenates all input files into the output file.
     * <pre>
     * ConcatDemo [-o outputfile] [-i inputfile ...]
     * </pre>
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // Use hardcoded arguments for debugging
        /*
         args=new String[] {"-i",
         "/Users/werni/Movies/ScreenRecording 2012-07-27 at 21.34.31.avi",
         "/Users/werni/Movies/ScreenRecording 2012-07-27 at 21.34.31.avi",
         "/Users/werni/Movies/ScreenRecording 2012-07-27 at 21.34.31.avi",
         "-o",
         "/Users/werni/Movies/concat1.avi"};
         */

        // Parse arguments
        File outfile = null;
        ArrayList<File> infiles = new ArrayList<File>();

        try {
            char arg = ' ';
            for (int i = 0; i < args.length; i++) {
                if (args[i].length() > 1 && args[i].charAt(0) == '-') {
                    arg = args[i].charAt(1);
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
                        default:
                            throw new IllegalArgumentException("error: illegal option: " + args[i]);
                    }
                }
            }
            if (outfile == null) {
                throw new IllegalArgumentException("error: no outputfile specified");
            }
            /*
            if (outfile.exists()) {
                throw new IllegalArgumentException("error: outputfile exists: " + outfile);
            }*/
            if (infiles.size() == 0) {
                throw new IllegalArgumentException("error: no inputfiles specified");
            }
            for (File f : infiles) {
                if (!f.exists()) {
                    throw new IllegalArgumentException("error: inputfile does not exist: " + f);
                }
            }
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("usage: java -jar ConcatDemo.jar [-o outputfile] [-i inputfile ...]");
            String version = ConcatDemoMain.class.getPackage().getImplementationVersion();
            System.err.println("ConcatDemo " + (version == null ? "" : version + " ") + "(c) Werner Randelshofer");
            System.exit(10);
        }
        try {
            concat(outfile, infiles);
        } catch (IOException e) {
            System.err.println("error: " + e.getMessage());
        }
    }

    private static void concat(File outfile, ArrayList<File> infiles) throws IOException {
        MovieWriter out = Registry.getInstance().getWriter(outfile);
        if (out == null) {
            throw new IOException("output file format not supported for: " + outfile);
        }
        try {
            // For each input track in each input file, find a matching output track.
            // If no matching output track has been found, create a new output track.
            int[][] matchingT = new int[infiles.size()][0];
            for (int i = 0, imax = infiles.size(); i < imax; i++) {
                File infile = infiles.get(i);
                MovieReader in = Registry.getInstance().getReader(infile);
                if (in == null) {
                    throw new IOException("input file format not supported for: " + infile);
                }
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
            // For each input file, write all media samples into the matching
            // output tracks.
            Buffer buf = new Buffer();
            for (int i = 0, imax = infiles.size(); i < imax; i++) {
                File infile = infiles.get(i);
                MovieReader in = Registry.getInstance().getReader(infile);
                if (in == null) {
                    throw new IOException("input file format not supported for: " + infile);
                }
                try {
                    for (int t = in.nextTrack(); t != -1; t = in.nextTrack()) {
                        in.read(t, buf);
                        out.write(matchingT[i][t], buf);
                    }
                } finally {
                    in.close();
                }
            }
        } finally {
            out.close();
        }
    }
}

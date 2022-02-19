/*
 * @(#)Main.java
 * Copyright © 2021 Werner Randelshofer, Switzerland. MIT License.
 */

package org.monte.demo.sift;

import org.monte.media.exception.AbortException;
import org.monte.media.exception.ParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Takes any IFF file and tells you what's in it.
 * <p>
 */
public class Main {
    enum Mode {
        SIFT,
        HELP,
        HEXDUMP,
        EXTRACT,
        INSERT,
        REMOVE
    }

    public static void main(String... args) throws ParseException, IOException, AbortException {
        Path inputPath = null;
        Mode mode = Mode.SIFT;
        int indexInTree = -1;
        int childIndex = -1;
        Path outputPath = null;
        Path insertionPath = null;
        String chunkId = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                continue;
            }
            switch (args[i]) {
            case "--help":
                mode = Mode.HELP;
                break;
            case "--hexdump":
                mode = mode == Mode.SIFT ? Mode.HEXDUMP : Mode.HELP;
                break;
            case "--extract":
                mode = mode == Mode.SIFT ? Mode.EXTRACT : Mode.HELP;
                if (i < args.length - 2) {
                    indexInTree = Integer.parseInt(args[++i]);
                    outputPath = Paths.get(args[++i]);
                } else {
                    mode = Mode.HELP;
                }
                break;
            case "--remove":
                mode = mode == Mode.SIFT ? Mode.REMOVE : Mode.HELP;
                if (i < args.length - 2) {
                    indexInTree = Integer.parseInt(args[++i]);
                    outputPath = Paths.get(args[++i]);
                } else {
                    mode = Mode.HELP;
                }
                break;
            case "--insert":
                mode = mode == Mode.SIFT ? Mode.INSERT : Mode.HELP;
                if (i < args.length - 4) {
                    indexInTree = Integer.parseInt(args[++i]);
                    childIndex = Integer.parseInt(args[++i]);
                    chunkId = args[++i];
                    insertionPath = Paths.get(args[++i]);
                    outputPath = Paths.get(args[++i]);
                } else {
                    mode = Mode.HELP;
                }
                break;
            default:
                inputPath = Paths.get(args[i]);
                break;
            }
        }
        //---------------
        switch (mode) {
        case SIFT:
            Sift sift = new Sift();
            sift.sift(inputPath);
            break;
        case HELP:
            System.out.println("Usage:"
                    + "\nsift --help"
                    + "\nsift inputFile"
                    + "\nsift --hexdump inputFile"
                    + "\nsift --extract indexInTree outputFile inputFile"
                    + "\nsift --remove indexInTree outputFile inputFile"
                    + "\nsift --insert indexInTree chunkid insertionFile outputFile inputFile"
            );
            break;
        case HEXDUMP:
            Sift siftHex = new Sift();
            siftHex.setHexdump(true);
            siftHex.sift(inputPath);
            break;
        case EXTRACT:
            SiftExtract siftEx = new SiftExtract();
            siftEx.extract(inputPath, outputPath, indexInTree);
            break;
        case REMOVE:
            SiftRemove siftRem = new SiftRemove();
            siftRem.remove(inputPath, outputPath, indexInTree);
            break;
        case INSERT:
            SiftInsert siftInsert = new SiftInsert();
            siftInsert.insert(inputPath, outputPath, indexInTree,
                    childIndex,chunkId,insertionPath);
            break;
        }
    }
}

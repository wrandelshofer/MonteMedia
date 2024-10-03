/*
 * @(#)Registry.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.monte.media.av.FormatKeys.EncodingKey;
import static org.monte.media.av.FormatKeys.MediaTypeKey;
import static org.monte.media.av.FormatKeys.MimeTypeKey;

/**
 * The {@code Registry} for audio and video codecs.
 *
 * @author Werner Randelshofer
 */
public abstract class Registry {

    private static Registry instance;

    /**
     * Gets the registry instance.
     * <p>
     * Will create and return a {@link DefaultRegistry} if no instance has been set.
     *
     * @return the instance
     */
    public static Registry getInstance() {
        if (instance == null) {
            instance = new DefaultRegistry();
        }
        return instance;
    }

    /**
     * Sets the registry instance.
     *
     * @param instanceNullable a new instance or null
     */
    public void setInstance(Registry instanceNullable) {
        instance = instanceNullable;
    }

    /**
     * Gets a codec which can transcode from the specified input format to the
     * specified output format.
     *
     * @param inputFormat  The input format.
     * @param outputFormat The output format.
     * @return A codec or null.
     */
    public final Codec getCodec(Format inputFormat, Format outputFormat) {
        List<Codec> codecs = getCodecs(inputFormat, outputFormat);
        return codecs.isEmpty() ? null : codecs.get(0);
    }

    /**
     * Gets all codecs which can transcode from the specified input format to
     * the specified output format.
     *
     * @param inputFormat  The input format. Or null if any is acceptable.
     * @param outputFormat The output format. Or null if any is acceptable.
     * @return An unmodifiale list of codes. If no codecs were found, an empty
     * list is returned.
     */
    public abstract List<Codec> getCodecs(Format inputFormat, Format outputFormat);

    /**
     * Gets the first codec which can decode the specified format.
     *
     * @param inputFormat The output format.
     * @return A codec. Returns null if no codec was found.
     */
    public final Codec getDecoder(Format inputFormat) {
        return getCodec(inputFormat, null);
    }

    /**
     * Gets all codecs which can decode the specified format.
     *
     * @param inputFormat The input format.
     * @return An unmodifiable list of codecs.
     */
    public final List<Codec> getDecoders(Format inputFormat) {
        return getCodecs(inputFormat, null);
    }

    /**
     * Gets the first codec which can encode the specified format.
     *
     * @param outputFormat The output format.
     * @return A codec. Returns null if no codec was found.
     */
    public final Codec getEncoder(Format outputFormat) {
        return getCodec(null, outputFormat);
    }

    /**
     * Gets all codecs which can encode the specified format.
     *
     * @param outputFormat The output format.
     * @return An unmodifiable list of codecs.
     */
    public final List<Codec> getEncoders(Format outputFormat) {
        return getCodecs(null, outputFormat);
    }

    public abstract String getExtension(Format ff);

    public abstract Format getFileFormat(File file);

    /**
     * Gets a reader for the specified file format and file.
     *
     * @param fileFormat the desired file format.
     * @param file       the desired file
     * @return a reader
     * @throws IOException if no reader could be found
     */
    public abstract MovieReader getReader(Format fileFormat, File file) throws IOException;

    /**
     * Gets a reader for the specified file.
     *
     * @param file the desired file
     * @return a reader
     * @throws IOException if no reader could be found
     */
    public MovieReader getReader(File file) throws IOException {
        Format format = getFileFormat(file);
        if (format == null) {
            throw new IOException("Could not identify the file format of file " + file + ".");
        }
        return getReader(format, file);
    }

    /**
     * Gets all registered reader formats.
     * @return the reader formats
     */
    public abstract List<Format> getReaderFormats();

    /**
     * Gets all reader SPIs for the specified file.
     *
     * @param file       the desired file
     * @return the reader SPIs
     * @throws IOException if no reader could be found
     */
    public List<MovieReaderSpi> getReaderSpis(File file) throws IOException {
        Format format = getFileFormat(file);
        if (format == null) {
            throw new IOException("Could not identify the file format of file " + file + ".");
        }
        return getReaderSpis(format);
    }

    /**
     * Gets all reader SPIs for the specified file format.
     *
     * @param fileFormat the desired file format.
     * @return the reader SPIs
     * @throws IOException if no reader could be found
     */
    public abstract List<MovieReaderSpi> getReaderSpis(Format fileFormat) throws IOException;

    /**
     * Gets all reader SPIs.
     *
     * @return the reader SPIs
     */
    public abstract List<MovieReaderSpi> getReaderSpis();

    /**
     * Gets a writer for the specified file.
     *
     * @param file the desired file
     * @return a writer or null
     */
    public MovieWriter getWriter(File file) throws IOException {
        Format format = getFileFormat(file);
        return format == null ? null : getWriter(format, file);
    }

    /**
     * Gets a writer for the specified file format and file.
     *
     * @param fileFormat the desired file format.
     * @param file       the desired file
     * @return a writer or null
     */
    public abstract MovieWriter getWriter(Format fileFormat, File file) throws IOException;

    /**
     * Gets all registered writer formats.
     * @return the reader formats
     */
    public abstract List<Format> getWriterFormats();

    /**
     * Gets all reader SPIs for the specified file.
     *
     * @param file the desired file
     * @return the reader SPIs
     * @throws IOException if no reader could be found
     */
    public List<MovieWriterSpi> getWriterSpis(File file) throws IOException {
        Format format = getFileFormat(file);
        if (format == null) {
            throw new IOException("Could not identify the file format of file " + file + ".");
        }
        return getWriterSpis(format);
    }

    /**
     * Gets all writer SPIs.
     *
     * @return the writer SPIs
     */
    public abstract List<MovieWriterSpi> getWriterSpis();

    /**
     * Gets all writer SPIs for the specified file format.
     *
     * @param fileFormat the desired file format.
     * @return the reader SPIs
     * @throws IOException if no reader could be found
     */
    public abstract List<MovieWriterSpi> getWriterSpis(Format fileFormat) throws IOException;

    /**
     * Suggests output formats for the given input media format and specified
     * file format.
     *
     * @param inputMediaFormat
     * @param outputFileFormat
     * @return List of output media formats.
     */
    public ArrayList<Format> suggestOutputFormats(Format inputMediaFormat, Format outputFileFormat) {
        ArrayList<Format> formats = new ArrayList<>();
        Format matchFormat = new Format(//
                MimeTypeKey, outputFileFormat.get(MimeTypeKey),//
                MediaTypeKey, inputMediaFormat.get(MediaTypeKey));
        List<Codec> codecs = getEncoders(matchFormat);
        int matchingCount = 0;
        for (Codec c : codecs) {
            for (Format mf : c.getOutputFormats(null)) {
                if (mf.matches(matchFormat)) {
                    if (inputMediaFormat.matchesWithout(mf, MimeTypeKey)) {
                        // add matching formats first
                        formats.add(0, mf.append(inputMediaFormat));
                        matchingCount++;
                    } else if (inputMediaFormat.matchesWithout(mf, MimeTypeKey, EncodingKey)) {
                        // add formats which match everything but the encoding second
                        formats.add(matchingCount, mf.append(inputMediaFormat));
                    } else {
                        // add remaining formats last
                        formats.add(mf.append(inputMediaFormat));
                    }
                }
            }
        }

        // remove duplicates
        for (int i = formats.size() - 1; i >= 0; i--) {
            Format fi = formats.get(i);
            for (int j = i - 1; j >= 0; j--) {
                Format fj = formats.get(j);
                if (fi.matches(fj)) {
                    formats.remove(i);
                    break;
                }
            }
        }

        return formats;
    }

}

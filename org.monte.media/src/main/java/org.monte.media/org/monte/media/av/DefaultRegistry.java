/*
 * @(#)DefaultRegistry.java
 * Copyright © 2023 Werner Randelshofer, Switzerland. MIT License.
 */
package org.monte.media.av;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * This default {@link Registry} uses {@link ServiceLoader} to discover
 * {@link CodecSpi}s, {@link MovieReaderSpi}s, and {@link MovieWriterSpi}s.
 *
 * @author Werner Randelshofer
 */
public class DefaultRegistry extends Registry {

    private List<CodecSpi> codecSpis;
    private List<MovieReaderSpi> readerSpis;
    private List<MovieWriterSpi> writerSpis;
    private Map<String, String> mimeTypeToExtensionMap;
    private Map<String, Format> extensionToFormatMap;

    private synchronized List<CodecSpi> getCodecSpis() {
        if (codecSpis == null) {
            codecSpis = new ArrayList<>();
            for (CodecSpi spi : ServiceLoader.load(CodecSpi.class)) {
                codecSpis.add(spi);
            }
        }
        return codecSpis;
    }

    private synchronized Map<String, String> getMimeTypeToExtensionMap() {
        if (mimeTypeToExtensionMap == null) {
            mimeTypeToExtensionMap = new LinkedHashMap<>();
            for (MovieReaderSpi spi : getReaderSpis()) {
                mimeTypeToExtensionMap.put(spi.getFileFormat().get(FormatKeys.MimeTypeKey), spi.getExtensions().isEmpty() ? "" : spi.getExtensions().getFirst());
            }
            for (MovieWriterSpi spi : getWriterSpis()) {
                mimeTypeToExtensionMap.put(spi.getFileFormat().get(FormatKeys.MimeTypeKey), spi.getExtensions().isEmpty() ? "" : spi.getExtensions().getFirst());
            }
        }
        return mimeTypeToExtensionMap;
    }

    private synchronized Map<String, Format> getExtensionToFormatMap() {
        if (extensionToFormatMap == null) {
            extensionToFormatMap = new LinkedHashMap<>();
            for (MovieReaderSpi spi : getReaderSpis()) {
                for (String ext : spi.getExtensions()) {
                    extensionToFormatMap.put(ext, spi.getFileFormat());
                }
            }
            for (MovieWriterSpi spi : getWriterSpis()) {
                for (String ext : spi.getExtensions()) {
                    extensionToFormatMap.put(ext, spi.getFileFormat());
                }
            }
        }
        return extensionToFormatMap;
    }

    private synchronized List<MovieReaderSpi> getReaderSpis() {
        if (readerSpis == null) {
            readerSpis = new ArrayList<>();
            for (MovieReaderSpi spi : ServiceLoader.load(MovieReaderSpi.class)) {
                readerSpis.add(spi);
            }
        }
        return readerSpis;
    }

    private synchronized List<MovieWriterSpi> getWriterSpis() {
        if (writerSpis == null) {
            writerSpis = new ArrayList<>();
            for (MovieWriterSpi spi : ServiceLoader.load(MovieWriterSpi.class)) {
                writerSpis.add(spi);
            }
        }
        return writerSpis;
    }

    @Override
    public List<Codec> getCodecs(Format inputFormat, Format outputFormat) {
        List<Codec> codecs = new ArrayList<>();
        for (CodecSpi spi : getCodecSpis()) {
            Codec codec = spi.create();
            if (inputFormat != null) {
                Format actual = codec.setInputFormat(inputFormat);
                if (actual == null) {
                    continue;
                }
            }
            if (outputFormat != null) {
                Format actual = codec.setOutputFormat(outputFormat);
                if (actual == null) {
                    continue;
                }
            }
            codecs.add(codec);
        }
        return Collections.unmodifiableList(codecs);
    }

    @Override
    public String getExtension(Format ff) {
        return getMimeTypeToExtensionMap().get(ff.get(FormatKeys.MimeTypeKey));
    }

    private String getExtension(File file) {
        final String name = file.getName();
        final int p = name.lastIndexOf('.');
        return p == -1 ? "" : name.substring(p + 1).toLowerCase();
    }

    @Override
    public Format getFileFormat(File file) {
        final String extension = getExtension(file);
        return getExtensionToFormatMap().get(extension);
    }

    @Override
    public List<Format> getReaderFormats() {
        Set<Format> result = new LinkedHashSet<>();
        for (MovieReaderSpi spi : getReaderSpis()) {
            result.add(spi.getFileFormat());
        }
        return Collections.unmodifiableList(new ArrayList<>(result));
    }

    @Override
    public MovieReader getReader(Format fileFormat, File file) throws IOException {
        if (fileFormat == null) {
            fileFormat = getFileFormat(file);
        }
        for (MovieReaderSpi spi : getReaderSpis()) {
            if (spi.getFileFormat().matches(fileFormat)) {
                return spi.create(file);
            }
        }
        throw new IOException("Could not find a reader with format " + fileFormat + " for file " + file + ".");
    }

    @Override
    public MovieWriter getWriter(Format fileFormat, File file) throws IOException {
        if (fileFormat == null) {
            fileFormat = getFileFormat(file);
        }
        for (MovieWriterSpi spi : getWriterSpis()) {
            if (spi.getFileFormat().matches(fileFormat)) {
                return spi.create(file);
            }
        }
        return null;
    }

    @Override
    public List<Format> getWriterFormats() {
        Set<Format> result = new LinkedHashSet<>();
        for (MovieWriterSpi spi : getWriterSpis()) {
            result.add(spi.getFileFormat());
        }
        return Collections.unmodifiableList(new ArrayList<>(result));
    }

}

package me.hsgamer.universaldatafile;

import me.hsgamer.universaldatafile.api.FormatReader;
import me.hsgamer.universaldatafile.exception.RuntimeIOException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class UniversalDataReader {
    private final List<FormatReader> formatReaders;
    private final AtomicReference<Reader> reader;

    UniversalDataReader() {
        formatReaders = new ArrayList<>();
        reader = new AtomicReference<>();
    }

    public UniversalDataReader addFormatReader(FormatReader reader) {
        formatReaders.add(reader);
        return this;
    }

    public UniversalDataReader setReader(Reader reader) {
        this.reader.set(reader);
        return this;
    }

    public UniversalDataReader setFile(File file) {
        try {
            Utils.createIfNotExists(file);
            return setReader(new FileReader(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void read() {
        Reader fromReader = reader.get();
        if (fromReader == null) {
            throw new IllegalStateException("Reader is null");
        }
        if (formatReaders.isEmpty()) {
            throw new IllegalStateException("No format reader");
        }
        try (BufferedReader bufferedReader = new BufferedReader(fromReader)) {
            List<String> lines = new ArrayList<>();

            FormatReader formatReader = null;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (formatReader == null && line.startsWith(Constants.START_FORMAT)) {
                    String name = line.substring(Constants.START_FORMAT.length());
                    formatReader = getFormatReader(name);
                } else if (formatReader != null) {
                    if (line.startsWith(Constants.END_FORMAT)) {
                        formatReader.read(lines);
                        lines.clear();
                        formatReader = null;
                    } else {
                        lines.add(line);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    private FormatReader getFormatReader(String name) {
        return formatReaders.parallelStream()
                .filter(formatReader -> formatReader.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}

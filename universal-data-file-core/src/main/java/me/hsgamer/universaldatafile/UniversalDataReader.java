package me.hsgamer.universaldatafile;

import me.hsgamer.universaldatafile.api.FormatReader;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class UniversalDataReader {
    private final List<FormatReader> formatReaders;
    private final AtomicReference<Reader> reader;

    private UniversalDataReader() {
        formatReaders = new ArrayList<>();
        reader = new AtomicReference<>();
    }

    public static UniversalDataReader create() {
        return new UniversalDataReader();
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

    public void read() throws IOException {
        Reader fromReader = reader.get();
        if (fromReader == null) {
            throw new IllegalStateException("Reader is null");
        }
        if (formatReaders.isEmpty()) {
            throw new IllegalStateException("No format reader");
        }
        try (BufferedReader bufferedReader = new BufferedReader(fromReader)) {
            FormatReader formatReader = null;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.isEmpty()) continue;
                if (formatReader == null) {
                    formatReader = getFormatReader(line);
                    continue;
                }
                if (line.equals(formatReader.getEndFormat())) {
                    formatReader = null;
                    continue;
                }
                formatReader.read(line);
            }
        }
    }

    public void readSafe() {
        try {
            read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private FormatReader getFormatReader(String line) {
        return formatReaders.parallelStream()
                .filter(formatReader -> line.equals(formatReader.getStartFormat()))
                .findFirst()
                .orElse(null);
    }
}

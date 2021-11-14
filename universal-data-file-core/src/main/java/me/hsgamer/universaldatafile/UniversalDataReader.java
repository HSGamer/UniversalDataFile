package me.hsgamer.universaldatafile;

import me.hsgamer.universaldatafile.api.FormatReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UniversalDataReader {
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
                if (formatReader == null) formatReader = getFormatReader(line);
                if (formatReader == null) continue;
                if (line.equals(Constants.END_FORMAT + formatReader.getName())) {
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
        for (FormatReader formatReader : formatReaders) {
            if (line.equals(Constants.START_FORMAT + formatReader.getName())) {
                return formatReader;
            }
        }
        return null;
    }
}

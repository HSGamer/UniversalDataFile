package me.hsgamer.universaldatafile;

import me.hsgamer.universaldatafile.api.FormatReader;
import me.hsgamer.universaldatafile.exception.RuntimeIOException;
import me.hsgamer.universaldatafile.runner.ReaderRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class UniversalDataReader {
    private final Map<String, FormatReader> formatReaders;
    private final AtomicReference<Reader> reader;

    private UniversalDataReader() {
        formatReaders = new HashMap<>();
        reader = new AtomicReference<>();
    }

    public static UniversalDataReader create() {
        return new UniversalDataReader();
    }

    public UniversalDataReader addFormatReader(FormatReader reader) {
        formatReaders.put(reader.getName(), reader);
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
            throw new RuntimeIOException(e);
        }
    }

    public CompletableFuture<Void> read() {
        Reader fromReader = reader.get();
        if (fromReader == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Reader is null"));
        }
        if (formatReaders.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("No format reader"));
        }
        return CompletableFuture.supplyAsync(() -> {
            try (BufferedReader bufferedReader = new BufferedReader(fromReader)) {
                List<ReaderRunner> readerRunners = new ArrayList<>();
                List<String> lines = new ArrayList<>();
                FormatReader formatReader = null;
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (formatReader == null && line.startsWith(Constants.START_FORMAT)) {
                        String name = line.substring(Constants.START_FORMAT.length());
                        formatReader = formatReaders.get(name);
                    } else if (formatReader != null) {
                        if (line.startsWith(Constants.END_FORMAT)) {
                            List<String> finalLines = new ArrayList<>(lines);
                            readerRunners.add(new ReaderRunner(formatReader, finalLines));
                            lines.clear();
                            formatReader = null;
                        } else {
                            lines.add(line);
                        }
                    }
                }
                return readerRunners;
            } catch (IOException e) {
                throw new RuntimeIOException(e);
            }
        }).thenComposeAsync(readerRunners -> {
            List<CompletableFuture<Void>> completableFutures = new ArrayList<>(readerRunners.size());
            for (ReaderRunner readerRunner : readerRunners) {
                completableFutures.add(readerRunner.getOrRunFuture());
            }
            return CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
        });
    }

    public void readSync() {
        read().join();
    }
}

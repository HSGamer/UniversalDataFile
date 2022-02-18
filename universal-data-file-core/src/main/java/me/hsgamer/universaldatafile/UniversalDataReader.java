package me.hsgamer.universaldatafile;

import me.hsgamer.universaldatafile.api.FormatReader;
import me.hsgamer.universaldatafile.exception.RuntimeIOException;
import me.hsgamer.universaldatafile.runner.QueueRunner;
import me.hsgamer.universaldatafile.runner.ReaderRunner;
import me.hsgamer.universaldatafile.runner.TaskRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class UniversalDataReader {
    private final Map<String, FormatReader> formatReaders;
    private final AtomicReference<Reader> reader;
    private final AtomicInteger limitRunningPool;
    private final TagSettings tagSettings;

    private UniversalDataReader(TagSettings tagSettings) {
        formatReaders = new HashMap<>();
        reader = new AtomicReference<>();
        limitRunningPool = new AtomicInteger(10);
        this.tagSettings = tagSettings;
    }

    public static UniversalDataReader create() {
        return create(TagSettings.DEFAULT);
    }

    public static UniversalDataReader create(TagSettings tagSettings) {
        return new UniversalDataReader(tagSettings);
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

    public UniversalDataReader setLimitRunningPool(int limit) {
        this.limitRunningPool.set(limit);
        return this;
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
                            if (formatReader == null && line.startsWith(tagSettings.startFormat)) {
                                String name = line.substring(tagSettings.startFormat.length());
                                formatReader = formatReaders.get(name);
                            } else if (formatReader != null) {
                                if (line.startsWith(tagSettings.endFormat)) {
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
                })
                .thenApplyAsync(readerRunners -> new QueueRunner<>(readerRunners, limitRunningPool.get(), 0))
                .thenComposeAsync(TaskRunner::getOrRunFuture);
    }

    public void readSync() {
        read().join();
    }
}

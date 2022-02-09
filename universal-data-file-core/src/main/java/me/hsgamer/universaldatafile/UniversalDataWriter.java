package me.hsgamer.universaldatafile;

import me.hsgamer.universaldatafile.api.FormatWriter;
import me.hsgamer.universaldatafile.exception.RuntimeIOException;
import me.hsgamer.universaldatafile.runner.WriterRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

public final class UniversalDataWriter {
    private final List<FormatWriter> formatWriters;
    private final AtomicReference<Writer> writer;

    private UniversalDataWriter() {
        this.formatWriters = new ArrayList<>();
        this.writer = new AtomicReference<>();
    }

    public static UniversalDataWriter create() {
        return new UniversalDataWriter();
    }

    public UniversalDataWriter addFormatWriter(FormatWriter formatWriter) {
        formatWriters.add(formatWriter);
        return this;
    }

    public UniversalDataWriter setWriter(Writer writer) {
        this.writer.set(writer);
        return this;
    }

    public UniversalDataWriter setFile(File file) {
        try {
            Utils.createIfNotExists(file);
            return setWriter(new FileWriter(file));
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
    }

    public CompletableFuture<Void> write() {
        Writer toWriter = writer.get();
        if (toWriter == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Writer is null"));
        }
        if (formatWriters.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("No format writer"));
        }

        List<WriterRunner> writerRunners = new LinkedList<>();
        List<CompletableFuture<Void>> completableFutures = new ArrayList<>();
        for (FormatWriter formatWriter : formatWriters) {
            WriterRunner writerRunner = new WriterRunner(formatWriter);
            writerRunners.add(writerRunner);
            completableFutures.add(CompletableFuture.runAsync(writerRunner));
        }

        return CompletableFuture
                .allOf(completableFutures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    try (BufferedWriter bufferedWriter = new BufferedWriter(writer.get())) {
                        for (WriterRunner writerRunner : writerRunners) {
                            bufferedWriter.write(Constants.START_FORMAT + writerRunner.getName());
                            bufferedWriter.newLine();
                            for (String line : writerRunner.getLines()) {
                                bufferedWriter.write(line);
                                bufferedWriter.newLine();
                            }
                            bufferedWriter.write(Constants.END_FORMAT);
                            bufferedWriter.newLine();
                        }
                    } catch (IOException e) {
                        throw new RuntimeIOException(e);
                    }
                });
    }

    public void writeSync() {
        write().join();
    }
}

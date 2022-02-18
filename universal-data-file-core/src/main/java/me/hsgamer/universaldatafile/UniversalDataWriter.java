package me.hsgamer.universaldatafile;

import me.hsgamer.universaldatafile.api.FormatWriter;
import me.hsgamer.universaldatafile.exception.RuntimeIOException;
import me.hsgamer.universaldatafile.runner.QueueRunner;
import me.hsgamer.universaldatafile.runner.TaskRunner;
import me.hsgamer.universaldatafile.runner.WriterRunner;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public final class UniversalDataWriter {
    private final List<FormatWriter> formatWriters;
    private final AtomicReference<Writer> writer;
    private final AtomicInteger limitRunningPool;
    private final AtomicInteger limitCompletedPool;
    private final TagSettings tagSettings;

    private UniversalDataWriter(TagSettings tagSettings) {
        this.formatWriters = new ArrayList<>();
        this.writer = new AtomicReference<>();
        this.limitRunningPool = new AtomicInteger(10);
        this.limitCompletedPool = new AtomicInteger(10);
        this.tagSettings = tagSettings;
    }

    public static UniversalDataWriter create() {
        return create(TagSettings.DEFAULT);
    }

    public static UniversalDataWriter create(TagSettings tagSettings) {
        return new UniversalDataWriter(tagSettings);
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

    public UniversalDataWriter setLimitRunningPool(int limit) {
        this.limitRunningPool.set(limit);
        return this;
    }

    public UniversalDataWriter setLimitCompletedPool(int limit) {
        this.limitCompletedPool.set(limit);
        return this;
    }

    public CompletableFuture<Void> write() {
        Writer toWriter = writer.get();
        if (toWriter == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Writer is null"));
        }
        if (formatWriters.isEmpty()) {
            return CompletableFuture.failedFuture(new IllegalStateException("No format writer"));
        }

        return CompletableFuture.supplyAsync(() -> {
            List<WriterRunner> writerRunners = new LinkedList<>();
            for (FormatWriter formatWriter : formatWriters) {
                WriterRunner writerRunner = new WriterRunner(formatWriter);
                writerRunners.add(writerRunner);
            }
            return writerRunners;
        }).thenApplyAsync(writerRunners -> new QueueRunner<>(writerRunners, limitRunningPool.get(), limitCompletedPool.get()) {
            final BufferedWriter bufferedWriter = new BufferedWriter(writer.get());

            @Override
            protected void onFinish() {
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    throw new RuntimeIOException(e);
                }
            }

            @Override
            protected void onCompleted(WriterRunner writerRunner) {
                try {
                    bufferedWriter.write(tagSettings.startFormat + writerRunner.getName());
                    bufferedWriter.newLine();
                    for (String line : writerRunner.getLines()) {
                        bufferedWriter.write(line);
                        bufferedWriter.newLine();
                    }
                    bufferedWriter.write(tagSettings.endFormat);
                    bufferedWriter.newLine();
                } catch (IOException e) {
                    throw new RuntimeIOException(e);
                }
            }
        }).thenComposeAsync(TaskRunner::getOrRunFuture);
    }

    public void writeSync() {
        write().join();
    }
}

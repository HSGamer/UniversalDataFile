package me.hsgamer.universaldatafile;

import me.hsgamer.universaldatafile.api.FormatWriter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public final class UniversalDataWriter {
    private final List<FormatWriter> formatWriters;
    private final AtomicReference<Writer> writer;

    UniversalDataWriter() {
        this.formatWriters = new ArrayList<>();
        this.writer = new AtomicReference<>();
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
            throw new RuntimeException(e);
        }
    }

    public void write() throws IOException {
        Writer toWriter = writer.get();
        if (toWriter == null) {
            throw new IllegalStateException("Writer is null");
        }
        if (formatWriters.isEmpty()) {
            throw new IllegalStateException("No format writer");
        }
        try (BufferedWriter bufferedWriter = new BufferedWriter(writer.get())) {
            for (FormatWriter formatWriter : formatWriters) {
                bufferedWriter.write(Constants.START_FORMAT + formatWriter.getName());
                bufferedWriter.newLine();
                for (String line : formatWriter.write()) {
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
                bufferedWriter.write(Constants.END_FORMAT);
                bufferedWriter.newLine();
            }
        }
    }

    public void writeSafe() {
        try {
            write();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

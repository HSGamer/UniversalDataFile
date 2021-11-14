package me.hsgamer.universaldatafile;

import me.hsgamer.universaldatafile.api.FormatWriter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UniversalDataWriter {
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
                List<String> lines = formatWriter.write();
                for (int i = 0; i < lines.size(); i++) {
                    bufferedWriter.write(lines.get(i));
                    if (i != lines.size() - 1) {
                        bufferedWriter.newLine();
                    }
                }
                bufferedWriter.write(Constants.END_FORMAT + formatWriter.getName());
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

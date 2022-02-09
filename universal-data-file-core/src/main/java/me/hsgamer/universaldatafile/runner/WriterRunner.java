package me.hsgamer.universaldatafile.runner;

import me.hsgamer.universaldatafile.api.FormatWriter;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WriterRunner implements Runnable {
    private final FormatWriter writer;
    private final AtomicBoolean completed;
    private List<String> lines;

    public WriterRunner(FormatWriter writer) {
        this.writer = writer;
        this.completed = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        lines = writer.write();
        completed.set(true);
    }

    public boolean isCompleted() {
        return completed.get();
    }

    public String getName() {
        return writer.getName();
    }

    public List<String> getLines() {
        return lines == null ? Collections.emptyList() : lines;
    }
}

package me.hsgamer.universaldatafile.runner;

import me.hsgamer.universaldatafile.api.FormatWriter;

import java.util.Collections;
import java.util.List;

public class WriterRunner implements Runnable {
    private final FormatWriter writer;
    private List<String> lines;

    public WriterRunner(FormatWriter writer) {
        this.writer = writer;
    }

    @Override
    public void run() {
        lines = writer.write();
    }

    public String getName() {
        return writer.getName();
    }

    public List<String> getLines() {
        return lines == null ? Collections.emptyList() : lines;
    }
}

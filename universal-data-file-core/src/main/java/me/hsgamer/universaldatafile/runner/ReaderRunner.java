package me.hsgamer.universaldatafile.runner;

import me.hsgamer.universaldatafile.api.FormatReader;

import java.util.List;

public class ReaderRunner extends TaskRunner {
    private final FormatReader reader;
    private final List<String> lines;

    public ReaderRunner(FormatReader reader, List<String> lines) {
        this.reader = reader;
        this.lines = lines;
    }

    @Override
    public void run() {
        reader.read(lines);
    }

    public String getName() {
        return reader.getName();
    }
}

package me.hsgamer.universaldatafile.objects;

import me.hsgamer.universaldatafile.api.FormatWriter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TestWriter implements FormatWriter {
    private final String name;

    public TestWriter(int i) {
        this.name = Integer.toString(i);
    }

    @Override
    public List<String> write() {
        List<String> list = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            list.add(Integer.toString(ThreadLocalRandom.current().nextInt()));
        }
        return list;
    }

    @Override
    public String getName() {
        return name;
    }
}

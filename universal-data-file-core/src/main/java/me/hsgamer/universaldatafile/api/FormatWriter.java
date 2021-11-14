package me.hsgamer.universaldatafile.api;

import java.util.List;

public interface FormatWriter {
    List<String> write();

    String getName();
}

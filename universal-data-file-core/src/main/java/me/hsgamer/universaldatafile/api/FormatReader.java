package me.hsgamer.universaldatafile.api;

import java.util.List;

public interface FormatReader extends Identifier {
    void read(List<String> line);
}

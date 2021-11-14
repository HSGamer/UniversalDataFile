package me.hsgamer.universaldatafile.api;

import java.util.List;

public interface FormatWriter extends Identifier {
    List<String> write();
}

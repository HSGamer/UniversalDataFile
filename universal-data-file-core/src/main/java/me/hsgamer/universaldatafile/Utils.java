package me.hsgamer.universaldatafile;

import java.io.File;
import java.io.IOException;

public final class Utils {
    private Utils() {
        // EMPTY
    }

    public static void createIfNotExists(File file) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
    }
}

package me.hsgamer.universaldatafile;

import java.io.File;
import java.io.IOException;

public final class Utils {
    private Utils() {
        // EMPTY
    }

    public static void createIfNotExists(File file) throws IOException {
        if (file.exists()) {
            return;
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory " + parent);
        }
        if (file.createNewFile()) {
            throw new IOException("Failed to create file " + file);
        }
    }
}

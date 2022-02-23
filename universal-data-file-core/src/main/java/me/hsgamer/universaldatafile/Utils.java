package me.hsgamer.universaldatafile;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;

public final class Utils {
    private static final Logger logger = Logger.getLogger("UniversalDataFile");

    private Utils() {
        // EMPTY
    }

    public static Logger getLogger() {
        return logger;
    }

    public static void logThrowable(Throwable throwable) {
        logger.log(SEVERE, "An error occurred", throwable);
    }

    public static void createIfNotExists(File file) throws IOException {
        if (file.exists()) {
            return;
        }
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Failed to create directory " + parent);
        }
        if (!file.createNewFile()) {
            throw new IOException("Failed to create file " + file);
        }
    }
}

package me.hsgamer.universaldatafile.exception;

import java.io.IOException;

public class RuntimeIOException extends RuntimeException {
    public RuntimeIOException(IOException e) {
        super(e);
    }
}

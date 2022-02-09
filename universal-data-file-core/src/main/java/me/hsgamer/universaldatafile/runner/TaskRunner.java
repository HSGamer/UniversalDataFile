package me.hsgamer.universaldatafile.runner;

import java.util.concurrent.CompletableFuture;

public abstract class TaskRunner {
    private final CompletableFuture<Void> completableFuture;

    protected TaskRunner() {
        completableFuture = CompletableFuture.runAsync(this::run);
    }

    public abstract void run();

    public CompletableFuture<Void> getCompletableFuture() {
        return completableFuture;
    }
}

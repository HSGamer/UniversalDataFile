package me.hsgamer.universaldatafile.runner;

import java.util.concurrent.CompletableFuture;

public abstract class TaskRunner {
    private CompletableFuture<Void> completableFuture;

    public abstract void run();

    public CompletableFuture<Void> getOrRunFuture() {
        if (completableFuture == null) {
            completableFuture = CompletableFuture.runAsync(this::run);
        }
        return completableFuture;
    }

    public boolean isStarted() {
        return completableFuture != null;
    }

    public boolean isCompleted() {
        return isStarted() && completableFuture.isDone();
    }
}

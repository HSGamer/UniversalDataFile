package me.hsgamer.universaldatafile.runner;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class QueueRunner<T extends TaskRunner> extends TaskRunner {
    private final int limitRunning;
    private final Queue<T> pending = new LinkedList<>();
    private final Queue<T> running = new LinkedList<>();

    public QueueRunner(Collection<T> taskRunners, int limitRunning) {
        pending.addAll(taskRunners);
        this.limitRunning = limitRunning;
    }

    @Override
    public void run() {
        while (!pending.isEmpty() || !running.isEmpty()) {
            checkRunning();
            checkPending();
        }
    }

    protected void onCompleted(T taskRunner) {
        // EMPTY
    }

    protected void onFinalized() {
        // EMPTY
    }

    private void checkRunning() {
        T taskRunner = running.poll();
        if (taskRunner == null) return;
        if (!taskRunner.isCompleted())
            running.add(taskRunner);
        else {
            try {
                onCompleted(taskRunner);
            } catch (Exception exception) {
                onFinalized();
                throw exception;
            }
        }
    }

    private void checkPending() {
        if (limitRunning > 0 && running.size() >= limitRunning) return;
        T taskRunner = pending.poll();
        if (taskRunner == null) return;
        taskRunner.getOrRunFuture();
        running.add(taskRunner);
    }
}

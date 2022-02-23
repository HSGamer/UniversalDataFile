package me.hsgamer.universaldatafile.runner;

import me.hsgamer.universaldatafile.Utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;

public class QueueRunner<T extends TaskRunner> extends TaskRunner {
    private final int limitRunning;
    private final int limitCompleted;
    private final Queue<T> pending = new LinkedList<>();
    private final Queue<T> running = new LinkedList<>();
    private final Queue<T> completed = new LinkedList<>();

    public QueueRunner(Collection<T> taskRunners, int limitRunning, int limitCompleted) {
        pending.addAll(taskRunners);
        this.limitRunning = limitRunning;
        this.limitCompleted = limitCompleted;
    }

    @Override
    public void run() {
        while (!pending.isEmpty() || !running.isEmpty() || !completed.isEmpty()) {
            checkCompleted();
            checkRunning();
            checkPending();
        }
        finishRun();
    }

    protected void onCompleted(T taskRunner) {
        // EMPTY
    }

    protected void onFinish() {
        // EMPTY
    }

    private void finishRun() {
        onFinish();
        completed.clear();
        pending.clear();
        running.forEach(runner -> runner.getOrRunFuture().cancel(true));
        running.clear();
    }

    private void checkCompleted() {
        while (true) {
            T taskRunner = completed.poll();
            if (taskRunner == null) break;
            try {
                onCompleted(taskRunner);
            } catch (Exception exception) {
                finishRun();
                Utils.logThrowable(exception);
                throw exception;
            }
        }
    }

    private void checkRunning() {
        T taskRunner = running.poll();
        if (taskRunner == null) return;
        if (!taskRunner.isCompleted() || (limitCompleted > 0 && completed.size() > limitCompleted))
            running.add(taskRunner);
        else
            completed.add(taskRunner);
    }

    private void checkPending() {
        if (limitRunning > 0 && running.size() > limitRunning) return;
        T taskRunner = pending.poll();
        if (taskRunner == null) return;
        taskRunner.getOrRunFuture();
        running.add(taskRunner);
    }
}

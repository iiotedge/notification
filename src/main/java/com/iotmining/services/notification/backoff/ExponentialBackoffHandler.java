package com.iotmining.services.notification.backoff;

import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExponentialBackoffHandler implements RejectedExecutionHandler {

    private final int maxRetries;
    private final int initialDelay;
    private final int maxDelay;
    private final double backoffFactor;

    public ExponentialBackoffHandler(int maxRetries, int initialDelay, int maxDelay, double backoffFactor) {
        this.maxRetries = maxRetries;
        this.initialDelay = initialDelay;
        this.maxDelay = maxDelay;
        this.backoffFactor = backoffFactor;
    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        int attempt = 0;
        long delay = initialDelay;

        while (attempt < maxRetries) {
            try {
                TimeUnit.MILLISECONDS.sleep(delay);
                executor.execute(r);
                return;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Task execution interrupted", e);
            } catch (Exception e) {
                attempt++;
                delay = Math.min((long) (delay * backoffFactor), maxDelay);
            }
        }

        throw new RejectedExecutionException("Task rejected after max retries");
    }
}

package com.veloriastudio.atlas.internal.async;

import com.veloriastudio.atlas.api.async.AsyncService;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

public final class DefaultAsyncService implements AsyncService, AutoCloseable {

    private final ExecutorService executor;

    public DefaultAsyncService(){
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    @Override
    public CompletableFuture<Void> run(Runnable task) {

        Objects.requireNonNull(task, "task cannot be null");

        return CompletableFuture.runAsync(task, executor);
    }

    @Override
    public <T> CompletableFuture<T> supply(Supplier<T> supplier) {

        Objects.requireNonNull(supplier, "supplier cannot be null");

        return CompletableFuture.supplyAsync(supplier, executor);
    }

    @Override
    public void close() {
        executor.close();
    }
}

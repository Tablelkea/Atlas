package com.veloriastudio.atlas.api.async;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public interface AsyncService {

    CompletableFuture<Void> run(Runnable task);

    <T> CompletableFuture<T> supply(Supplier<T> supplier);

}

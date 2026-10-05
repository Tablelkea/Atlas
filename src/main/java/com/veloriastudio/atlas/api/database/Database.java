package com.veloriastudio.atlas.api.database;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface Database extends AutoCloseable {

    CompletableFuture<Integer> update(String sql, Object... parameters);

    <T> CompletableFuture<List<T>> query(String sql, RowMapper<T> mapper, Object... parameters);

    @Override
    void close();

    <T> CompletableFuture<T> transaction(TransactionCallback<T> callback);

    CompletableFuture<int[]> batch(
            String sql,
            List<Object[]> parameterSets
    );

}

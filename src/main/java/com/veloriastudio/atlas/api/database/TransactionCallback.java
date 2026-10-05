package com.veloriastudio.atlas.api.database;

@FunctionalInterface
public interface TransactionCallback<T> {

    T execute(DatabaseTransaction transaction) throws Exception;

}

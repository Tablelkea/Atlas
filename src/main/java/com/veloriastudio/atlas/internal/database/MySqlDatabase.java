package com.veloriastudio.atlas.internal.database;

import com.veloriastudio.atlas.api.database.*;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

final class MySqlDatabase implements Database {

    private final HikariDataSource dataSource;
    private final ExecutorService executor;

    MySqlDatabase(MySqlConfig config) {

        Objects.requireNonNull(config, "config cannot be null");

        HikariConfig hikariConfig = new HikariConfig();

        hikariConfig.setJdbcUrl(
                "jdbc:mysql://"
                        + config.host()
                        + ":"
                        + config.port()
                        + "/"
                        + config.database()
        );

        hikariConfig.setUsername(config.username());
        hikariConfig.setPassword(config.password());

        this.dataSource = new HikariDataSource(hikariConfig);
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    private static int executeUpdate(
            Connection connection,
            String sql,
            Object[] parameters
    ) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            bindParameters(statement, parameters);

            return statement.executeUpdate();
        }
    }

    private static <T> List<T> executeQuery(
            Connection connection,
            String sql,
            RowMapper<T> mapper,
            Object[] parameters
    ) throws SQLException {

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            bindParameters(statement, parameters);

            try (ResultSet resultSet = statement.executeQuery()) {

                List<T> results = new ArrayList<>();

                while (resultSet.next()) {
                    results.add(mapper.map(resultSet));
                }

                return results;
            }
        }
    }

    private static void bindParameters(
            PreparedStatement statement,
            Object[] parameters
    ) throws SQLException {

        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i]);
        }
    }

    @Override
    public CompletableFuture<Integer> update(
            String sql,
            Object... parameters
    ) {

        Objects.requireNonNull(sql, "sql cannot be null");
        Objects.requireNonNull(parameters, "parameters cannot be null");

        return CompletableFuture.supplyAsync(() -> {

            try (Connection connection = dataSource.getConnection()) {
                return executeUpdate(connection, sql, parameters);
            } catch (SQLException exception) {
                throw new CompletionException(exception);
            }

        }, executor);
    }

    @Override
    public <T> CompletableFuture<List<T>> query(
            String sql,
            RowMapper<T> mapper,
            Object... parameters
    ) {

        Objects.requireNonNull(sql, "sql cannot be null");
        Objects.requireNonNull(mapper, "mapper cannot be null");
        Objects.requireNonNull(parameters, "parameters cannot be null");

        return CompletableFuture.supplyAsync(() -> {

            try (Connection connection = dataSource.getConnection()) {
                return executeQuery(
                        connection,
                        sql,
                        mapper,
                        parameters
                );
            } catch (SQLException exception) {
                throw new CompletionException(exception);
            }

        }, executor);
    }

    @Override
    public <T> CompletableFuture<T> transaction(
            TransactionCallback<T> callback
    ) {

        Objects.requireNonNull(callback, "callback cannot be null");

        return CompletableFuture.supplyAsync(() -> {

            try (Connection connection = dataSource.getConnection()) {

                connection.setAutoCommit(false);

                try {

                    DatabaseTransaction transaction =
                            new MySqlTransaction(connection);

                    T result = callback.execute(transaction);

                    connection.commit();

                    return result;

                } catch (Exception exception) {

                    try {
                        connection.rollback();
                    } catch (SQLException rollbackException) {
                        exception.addSuppressed(rollbackException);
                    }

                    throw new CompletionException(exception);
                }

            } catch (SQLException exception) {
                throw new CompletionException(exception);
            }

        }, executor);
    }

    @Override
    public void close() {
        executor.close();
        dataSource.close();
    }

    @Override
    public CompletableFuture<int[]> batch(
            String sql,
            List<Object[]> parameterSets
    ) {

        Objects.requireNonNull(sql, "sql cannot be null");
        Objects.requireNonNull(parameterSets, "parameterSets cannot be null");

        return CompletableFuture.supplyAsync(() -> {

            try (
                    Connection connection = dataSource.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)
            ) {

                for (Object[] parameters : parameterSets) {
                    bindParameters(statement, parameters);
                    statement.addBatch();
                }

                return statement.executeBatch();

            } catch (SQLException exception) {
                throw new CompletionException(exception);
            }

        }, executor);
    }

    private record MySqlTransaction(Connection connection)
                implements DatabaseTransaction {

        @Override
            public int update(
                    String sql,
                    Object... parameters
            ) {

                Objects.requireNonNull(sql, "sql cannot be null");
                Objects.requireNonNull(parameters, "parameters cannot be null");

                try {
                    return executeUpdate(
                            connection,
                            sql,
                            parameters
                    );
                } catch (SQLException exception) {
                    throw new CompletionException(exception);
                }
            }

            @Override
            public <T> List<T> query(
                    String sql,
                    RowMapper<T> mapper,
                    Object... parameters
            ) {

                Objects.requireNonNull(sql, "sql cannot be null");
                Objects.requireNonNull(mapper, "mapper cannot be null");
                Objects.requireNonNull(parameters, "parameters cannot be null");

                try {
                    return executeQuery(
                            connection,
                            sql,
                            mapper,
                            parameters
                    );
                } catch (SQLException exception) {
                    throw new CompletionException(exception);
                }
            }
        }
}
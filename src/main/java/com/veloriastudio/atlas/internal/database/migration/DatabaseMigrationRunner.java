package com.veloriastudio.atlas.internal.database.migration;

import com.veloriastudio.atlas.api.database.Database;
import com.veloriastudio.atlas.api.database.migration.DatabaseMigration;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class DatabaseMigrationRunner {

    private static final String CREATE_TABLE_SQL = """
            CREATE TABLE IF NOT EXISTS atlas_schema_migrations (
                version INT NOT NULL,
                name VARCHAR(255) NOT NULL,
                applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                PRIMARY KEY (version)
            )
            """;

    private static final String SELECT_VERSIONS_SQL = """
            SELECT version
            FROM atlas_schema_migrations
            """;

    private static final String INSERT_MIGRATION_SQL = """
            INSERT INTO atlas_schema_migrations (version, name)
            VALUES (?, ?)
            """;

    private final Database database;

    public DatabaseMigrationRunner(Database database) {
        this.database = Objects.requireNonNull(database, "database cannot be null");
    }

    public CompletableFuture<Void> run(
            List<DatabaseMigration> migrations
    ) {

        Objects.requireNonNull(migrations, "migrations cannot be null");

        List<DatabaseMigration> sorted = migrations.stream()
                .sorted(Comparator.comparingInt(DatabaseMigration::version))
                .toList();

        validateVersions(sorted);

        return database.update(CREATE_TABLE_SQL)
                .thenCompose(ignored ->
                        database.query(
                                SELECT_VERSIONS_SQL,
                                resultSet -> resultSet.getInt("version")
                        )
                )
                .thenCompose(appliedVersions ->
                        applyPending(sorted, new HashSet<>(appliedVersions))
                );
    }

    private CompletableFuture<Void> applyPending(
            List<DatabaseMigration> migrations,
            Set<Integer> appliedVersions
    ) {

        CompletableFuture<Void> chain =
                CompletableFuture.completedFuture(null);

        for (DatabaseMigration migration : migrations) {

            if (appliedVersions.contains(migration.version())) {
                continue;
            }

            chain = chain.thenCompose(ignored ->
                    database.transaction(transaction -> {

                        migration.migrate(transaction);

                        transaction.update(
                                INSERT_MIGRATION_SQL,
                                migration.version(),
                                migration.name()
                        );

                        return null;
                    }).thenApply(result -> null)
            );
        }

        return chain;
    }

    private void validateVersions(
            List<DatabaseMigration> migrations
    ) {

        Set<Integer> versions = new HashSet<>();

        for (DatabaseMigration migration : migrations) {

            if (migration.version() <= 0) {
                throw new IllegalArgumentException(
                        "Migration version must be greater than 0"
                );
            }

            if (!versions.add(migration.version())) {
                throw new IllegalArgumentException(
                        "Duplicate migration version: "
                                + migration.version()
                );
            }
        }
    }
}
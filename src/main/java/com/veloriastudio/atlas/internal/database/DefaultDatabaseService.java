package com.veloriastudio.atlas.internal.database;

import com.veloriastudio.atlas.api.database.Database;
import com.veloriastudio.atlas.api.database.DatabaseService;
import com.veloriastudio.atlas.api.database.MySqlConfig;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultDatabaseService implements DatabaseService {

    private final Map<String, Database> databases = new ConcurrentHashMap<>();

    @Override
    public Database createMySql(String name, MySqlConfig config) {

        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(config, "config cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        Database database = new MySqlDatabase(config);

        Database existing = databases.putIfAbsent(name, database);

        if (existing != null) {
            database.close();
            throw new IllegalStateException(
                    "database already exists: " + name
            );
        }

        return database;
    }

    @Override
    public Optional<Database> find(String name) {

        Objects.requireNonNull(name, "name cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        return Optional.ofNullable(databases.get(name));
    }

    @Override
    public void closeAll() {

        for (Database database : databases.values()) {
            database.close();
        }

        databases.clear();
    }
}
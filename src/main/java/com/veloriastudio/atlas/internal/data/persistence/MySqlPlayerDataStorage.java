package com.veloriastudio.atlas.internal.data.persistence;

import com.veloriastudio.atlas.api.data.DataKeyId;
import com.veloriastudio.atlas.api.data.PlayerDataKey;
import com.veloriastudio.atlas.api.database.Database;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class MySqlPlayerDataStorage implements PlayerDataStorage {

    private static final String SELECT_SQL = """
            SELECT namespace, data_key, value
            FROM atlas_player_data
            WHERE player_uuid = ?
            """;
    private static final String DELETE_SQL = """
            DELETE FROM atlas_player_data
            WHERE player_uuid = ? AND namespace = ? AND data_key = ?
            """;
    private static final String UPSERT_SQL = """
            INSERT INTO atlas_player_data
                (player_uuid, namespace, data_key, value)
            VALUES (?, ?, ?, ?)
            ON DUPLICATE KEY UPDATE value = ?
            """;
    private final Database database;

    public MySqlPlayerDataStorage(
            Database database
    ) {
        this.database = Objects.requireNonNull(database, "database cannot be null");
    }

    @Override
    public CompletableFuture<Void> save(UUID playerId, Map<PlayerDataKey<?>, SerializedDirtyEntry> changes) {

        Objects.requireNonNull(playerId, "playerId cannot be null");
        Objects.requireNonNull(changes, "changes cannot be null");

        return database.transaction(transaction -> {

            for (Map.Entry<PlayerDataKey<?>, SerializedDirtyEntry> entry
                    : changes.entrySet()) {

                PlayerDataKey<?> key = entry.getKey();
                SerializedDirtyEntry change = entry.getValue();

                DataKeyId id = key.id();

                switch (change.operation()) {
                    case DELETE -> transaction.update(
                            DELETE_SQL,
                            playerId.toString(),
                            id.namespace(),
                            id.name()
                    );

                    case UPSERT -> {
                        String value = change.value()
                                .orElseThrow(() ->
                                        new IllegalStateException(
                                                "UPSERT must contain a value"
                                        )
                                );

                        transaction.update(
                                UPSERT_SQL,
                                playerId.toString(),
                                id.namespace(),
                                id.name(),
                                value,
                                value
                        );
                    }
                }
            }

            return null;
        });

    }

    @Override
    public CompletableFuture<Map<DataKeyId, String>> load(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        return database.query(
                SELECT_SQL,
                resultSet -> new LoadedValue(
                        new DataKeyId(
                                resultSet.getString("namespace"),
                                resultSet.getString("data_key")
                        ),
                        resultSet.getString("value")
                ),
                playerId.toString()

        ).thenApply(values -> {

            Map<DataKeyId, String> result = new HashMap<>();

            for (LoadedValue value : values) {
                result.put(value.id(), value.value());
            }

            return Map.copyOf(result);
        });
    }

    private record LoadedValue(
            DataKeyId id,
            String value
    ) {
    }
}

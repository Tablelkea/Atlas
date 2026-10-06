package com.veloriastudio.atlas.internal.item.persistence;

import com.veloriastudio.atlas.api.database.Database;
import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class MySqlStoredCustomItemStorage
        implements StoredCustomItemStorage {

    private static final String UPSERT_SQL = """
            INSERT INTO atlas_custom_items (
                item_id,
                category,
                item_data
            )
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE
                category = ?,
                item_data = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM atlas_custom_items
            WHERE item_id = ?
            """;

    private static final String SELECT_ALL_SQL = """
            SELECT item_id, category, item_data
            FROM atlas_custom_items
            """;

    private final Database database;

    MySqlStoredCustomItemStorage(
            Database database
    ) {
        this.database = Objects.requireNonNull(
                database,
                "database cannot be null"
        );
    }

    @Override
    public CompletableFuture<Void> save(
            StoredCustomItemRecord item
    ) {

        Objects.requireNonNull(
                item,
                "item cannot be null"
        );

        byte[] data = item.itemData();

        return database.update(
                UPSERT_SQL,
                item.id(),
                item.category(),
                data,
                item.category(),
                data
        ).thenApply(ignored -> null);
    }

    @Override
    public CompletableFuture<Void> delete(
            NamespacedKey id
    ) {

        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        return database.update(
                DELETE_SQL,
                id.asString()
        ).thenApply(ignored -> null);
    }

    @Override
    public CompletableFuture<List<StoredCustomItemRecord>> loadAll() {

        return database.query(
                SELECT_ALL_SQL,
                resultSet ->
                        new StoredCustomItemRecord(
                                resultSet.getString("item_id"),
                                resultSet.getString("category"),
                                resultSet.getBytes("item_data")
                        )
        );
    }
}
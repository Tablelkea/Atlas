package com.veloriastudio.atlas.internal.item.persistence;

import com.veloriastudio.atlas.api.database.Database;
import com.veloriastudio.atlas.api.item.custom.StoredCustomItem;
import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public final class StoredCustomItemPersistenceService {

    private final StoredCustomItemStorage storage;
    private final StoredCustomItemCodec codec;

    StoredCustomItemPersistenceService(
            StoredCustomItemStorage storage,
            StoredCustomItemCodec codec
    ) {
        this.storage = Objects.requireNonNull(storage);
        this.codec = Objects.requireNonNull(codec);
    }

    public static StoredCustomItemPersistenceService mysql(
            Database database
    ) {

        Objects.requireNonNull(
                database,
                "database cannot be null"
        );

        return new StoredCustomItemPersistenceService(
                new MySqlStoredCustomItemStorage(database),
                new StoredCustomItemCodec()
        );
    }

    public CompletableFuture<Void> save(
            StoredCustomItem item
    ) {

        StoredCustomItemRecord record =
                codec.encode(item);

        return storage.save(record);
    }

    public CompletableFuture<Void> delete(
            NamespacedKey id
    ) {
        return storage.delete(id);
    }

    public CompletableFuture<List<StoredCustomItem>> loadAll() {

        return storage.loadAll()
                .thenApply(records ->
                        records.stream()
                                .map(codec::decode)
                                .toList()
                );
    }

    public StoredCustomItem decode(
            StoredCustomItemRecord record
    ) {
        return codec.decode(record);
    }
}
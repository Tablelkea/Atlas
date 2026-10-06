package com.veloriastudio.atlas.internal.item.persistence;

import org.bukkit.NamespacedKey;

import java.util.List;
import java.util.concurrent.CompletableFuture;

interface StoredCustomItemStorage {

    CompletableFuture<Void> save(
            StoredCustomItemRecord item
    );

    CompletableFuture<Void> delete(
            NamespacedKey id
    );

    CompletableFuture<List<StoredCustomItemRecord>> loadAll();
}
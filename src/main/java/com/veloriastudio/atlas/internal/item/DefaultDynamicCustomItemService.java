package com.veloriastudio.atlas.internal.item;

import com.veloriastudio.atlas.api.item.custom.CustomItem;
import com.veloriastudio.atlas.api.item.custom.CustomItemCategory;
import com.veloriastudio.atlas.api.item.custom.CustomItemRegistry;
import com.veloriastudio.atlas.api.item.custom.DynamicCustomItemService;
import com.veloriastudio.atlas.api.item.custom.StoredCustomItem;
import com.veloriastudio.atlas.internal.item.persistence.StoredCustomItemPersistenceService;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public final class DefaultDynamicCustomItemService
        implements DynamicCustomItemService {

    private final CustomItemRegistry registry;
    private final StoredCustomItemPersistenceService persistence;

    private final ConcurrentMap<NamespacedKey, CompletableFuture<Void>> operations =
            new ConcurrentHashMap<>();

    public DefaultDynamicCustomItemService(
            CustomItemRegistry registry,
            StoredCustomItemPersistenceService persistence
    ) {
        this.registry = Objects.requireNonNull(
                registry,
                "registry cannot be null"
        );

        this.persistence = Objects.requireNonNull(
                persistence,
                "persistence cannot be null"
        );
    }

    @Override
    public CompletableFuture<StoredCustomItem> create(
            NamespacedKey id,
            CustomItemCategory category,
            ItemStack template
    ) {
        validate(
                id,
                category,
                template
        );

        StoredCustomItem item =
                new StoredCustomItem(
                        id,
                        category,
                        template
                );

        return executeSerially(
                id,
                () -> createInternal(item)
        );
    }

    @Override
    public CompletableFuture<StoredCustomItem> update(
            NamespacedKey id,
            CustomItemCategory category,
            ItemStack template
    ) {
        validate(
                id,
                category,
                template
        );

        StoredCustomItem updated =
                new StoredCustomItem(
                        id,
                        category,
                        template
                );

        return executeSerially(
                id,
                () -> updateInternal(updated)
        );
    }

    @Override
    public CompletableFuture<Boolean> delete(
            NamespacedKey id
    ) {
        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        return executeSerially(
                id,
                () -> deleteInternal(id)
        );
    }

    public CompletableFuture<Void> loadStoredItems() {
        return persistence.loadAll()
                .thenAccept(
                        items -> {
                            for (StoredCustomItem item : items) {
                                registry.register(item);
                            }
                        }
                );
    }

    private CompletableFuture<StoredCustomItem> createInternal(
            StoredCustomItem item
    ) {
        NamespacedKey id =
                item.id();

        if (registry.find(id).isPresent()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "custom item already exists: "
                                    + id.asString()
                    )
            );
        }

        return persistence.save(item)
                .thenApply(
                        ignored -> {
                            registry.register(item);
                            return item;
                        }
                );
    }

    private CompletableFuture<StoredCustomItem> updateInternal(
            StoredCustomItem updated
    ) {
        NamespacedKey id =
                updated.id();

        Optional<CustomItem> existing =
                registry.find(id);

        if (existing.isEmpty()) {
            return CompletableFuture.failedFuture(
                    new IllegalArgumentException(
                            "unknown custom item: "
                                    + id.asString()
                    )
            );
        }

        if (!(existing.get()
                instanceof StoredCustomItem)) {

            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "code-defined custom item cannot be edited: "
                                    + id.asString()
                    )
            );
        }

        return persistence.save(updated)
                .thenApply(
                        ignored -> {
                            registry.replace(updated);
                            return updated;
                        }
                );
    }

    private CompletableFuture<Boolean> deleteInternal(
            NamespacedKey id
    ) {
        Optional<CustomItem> existing =
                registry.find(id);

        if (existing.isEmpty()) {
            return CompletableFuture.completedFuture(
                    false
            );
        }

        if (!(existing.get()
                instanceof StoredCustomItem)) {

            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "code-defined custom item cannot be deleted: "
                                    + id.asString()
                    )
            );
        }

        return persistence.delete(id)
                .thenApply(
                        ignored ->
                                registry.unregister(id)
                                        .isPresent()
                );
    }

    private <T> CompletableFuture<T> executeSerially(
            NamespacedKey id,
            Supplier<CompletableFuture<T>> operation
    ) {
        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        Objects.requireNonNull(
                operation,
                "operation cannot be null"
        );

        CompletableFuture<T> result =
                new CompletableFuture<>();

        AtomicReference<CompletableFuture<Void>> tailReference =
                new AtomicReference<>();

        operations.compute(
                id,
                (key, previous) -> {
                    CompletableFuture<Void> start =
                            previous == null
                                    ? CompletableFuture.completedFuture(null)
                                    : previous.handle(
                                    (ignored, throwable) -> null
                            );

                    CompletableFuture<Void> tail =
                            start.thenCompose(
                                    ignored -> executeOperation(
                                            operation,
                                            result
                                    )
                            );

                    tailReference.set(
                            tail
                    );

                    return tail;
                }
        );

        CompletableFuture<Void> tail =
                tailReference.get();

        /*
         * remove(key, value) est volontaire :
         * une ancienne opération terminée ne doit jamais
         * supprimer une nouvelle chaîne déjà installée.
         */
        tail.whenComplete(
                (ignored, throwable) ->
                        operations.remove(
                                id,
                                tail
                        )
        );

        return result;
    }

    private <T> CompletableFuture<Void> executeOperation(
            Supplier<CompletableFuture<T>> operation,
            CompletableFuture<T> result
    ) {
        CompletableFuture<T> future;

        try {
            future = Objects.requireNonNull(
                    operation.get(),
                    "operation cannot return null"
            );

        } catch (Throwable throwable) {
            result.completeExceptionally(
                    throwable
            );

            return CompletableFuture.completedFuture(
                    null
            );
        }

        return future.handle(
                (value, throwable) -> {
                    if (throwable != null) {
                        result.completeExceptionally(
                                throwable
                        );
                    } else {
                        result.complete(
                                value
                        );
                    }

                    return null;
                }
        );
    }

    private void validate(
            NamespacedKey id,
            CustomItemCategory category,
            ItemStack template
    ) {
        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        Objects.requireNonNull(
                category,
                "category cannot be null"
        );

        Objects.requireNonNull(
                template,
                "template cannot be null"
        );

        if (template.getType().isAir()) {
            throw new IllegalArgumentException(
                    "template cannot be air"
            );
        }
    }
}
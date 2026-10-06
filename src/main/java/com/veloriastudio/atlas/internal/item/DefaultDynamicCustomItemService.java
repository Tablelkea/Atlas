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

public final class DefaultDynamicCustomItemService
        implements DynamicCustomItemService {

    private final CustomItemRegistry registry;
    private final StoredCustomItemPersistenceService persistence;

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

        validate(id, category, template);

        if (registry.find(id).isPresent()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "custom item already exists: "
                                    + id.asString()
                    )
            );
        }

        StoredCustomItem item =
                new StoredCustomItem(
                        id,
                        category,
                        template
                );

        return persistence.save(item)
                .thenApply(ignored -> {
                    registry.register(item);
                    return item;
                });
    }

    @Override
    public CompletableFuture<StoredCustomItem> update(
            NamespacedKey id,
            CustomItemCategory category,
            ItemStack template
    ) {

        validate(id, category, template);

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

        if (!(existing.get() instanceof StoredCustomItem)) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "code-defined custom item cannot be edited: "
                                    + id.asString()
                    )
            );
        }

        StoredCustomItem updated =
                new StoredCustomItem(
                        id,
                        category,
                        template
                );

        return persistence.save(updated)
                .thenApply(ignored -> {
                    registry.replace(updated);
                    return updated;
                });
    }

    @Override
    public CompletableFuture<Boolean> delete(
            NamespacedKey id
    ) {

        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        Optional<CustomItem> existing =
                registry.find(id);

        if (existing.isEmpty()) {
            return CompletableFuture.completedFuture(false);
        }

        if (!(existing.get() instanceof StoredCustomItem)) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException(
                            "code-defined custom item cannot be deleted: "
                                    + id.asString()
                    )
            );
        }

        return persistence.delete(id)
                .thenApply(ignored ->
                        registry.unregister(id)
                                .isPresent()
                );
    }

    public CompletableFuture<Void> loadStoredItems() {

        return persistence.loadAll()
                .thenAccept(items -> {

                    for (StoredCustomItem item : items) {
                        registry.register(item);
                    }
                });
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
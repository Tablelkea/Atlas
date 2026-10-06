package com.veloriastudio.atlas.internal.item;

import com.veloriastudio.atlas.api.item.ItemData;
import com.veloriastudio.atlas.api.item.custom.CustomItem;
import com.veloriastudio.atlas.api.item.custom.CustomItemRegistry;
import com.veloriastudio.atlas.api.pdc.PdcKey;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultCustomItemRegistry
        implements CustomItemRegistry {

    private final Map<NamespacedKey, CustomItem> items =
            new ConcurrentHashMap<>();

    private final PdcKey<String> identityKey;

    public DefaultCustomItemRegistry(
            Plugin atlasPlugin
    ) {
        Objects.requireNonNull(
                atlasPlugin,
                "atlasPlugin cannot be null"
        );

        this.identityKey = PdcKey.string(
                atlasPlugin,
                "custom_item_id"
        );
    }

    @Override
    public void register(CustomItem item) {

        Objects.requireNonNull(
                item,
                "item cannot be null"
        );

        NamespacedKey id =
                Objects.requireNonNull(
                        item.id(),
                        "custom item id cannot be null"
                );

        CustomItem existing =
                items.putIfAbsent(
                        id,
                        item
                );

        if (existing != null) {
            throw new IllegalStateException(
                    "custom item already registered: "
                            + id.asString()
            );
        }
    }

    @Override
    public Optional<CustomItem> find(
            NamespacedKey id
    ) {

        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        return Optional.ofNullable(
                items.get(id)
        );
    }

    @Override
    public ItemStack create(
            NamespacedKey id
    ) {

        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        CustomItem customItem =
                find(id).orElseThrow(
                        () -> new IllegalArgumentException(
                                "unknown custom item: "
                                        + id.asString()
                        )
                );

        ItemStack item =
                Objects.requireNonNull(
                        customItem.create(),
                        "CustomItem#create() cannot return null"
                );

        ItemData.set(
                item,
                identityKey,
                id.asString()
        );

        return item;
    }

    @Override
    public Optional<CustomItem> identify(
            ItemStack item
    ) {

        Objects.requireNonNull(
                item,
                "item cannot be null"
        );

        Optional<String> storedId =
                ItemData.get(
                        item,
                        identityKey
                );

        if (storedId.isEmpty()) {
            return Optional.empty();
        }

        NamespacedKey id =
                NamespacedKey.fromString(
                        storedId.get()
                );

        if (id == null) {
            return Optional.empty();
        }

        return find(id);
    }

    @Override
    public boolean is(
            ItemStack item,
            NamespacedKey id
    ) {

        Objects.requireNonNull(
                item,
                "item cannot be null"
        );

        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        return ItemData.get(
                        item,
                        identityKey
                )
                .map(id.asString()::equals)
                .orElse(false);
    }

    @Override
    public void replace(CustomItem item) {

        Objects.requireNonNull(
                item,
                "item cannot be null"
        );

        NamespacedKey id =
                Objects.requireNonNull(
                        item.id(),
                        "custom item id cannot be null"
                );

        if (items.replace(id, item) == null) {
            throw new IllegalArgumentException(
                    "unknown custom item: " + id.asString()
            );
        }
    }

    @Override
    public Optional<CustomItem> unregister(
            NamespacedKey id
    ) {

        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        return Optional.ofNullable(
                items.remove(id)
        );
    }

    @Override
    public Collection<CustomItem> all() {

        return items.values()
                .stream()
                .sorted(
                        Comparator.comparing(
                                item -> item.id().asString()
                        )
                )
                .toList();
    }
}
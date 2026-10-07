package com.veloriastudio.atlas.api.item;

import com.veloriastudio.atlas.api.pdc.PdcKey;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.Optional;

public final class ItemData {

    private ItemData() {
    }

    public static <T> void set(
            ItemStack item,
            PdcKey<T> key,
            T value
    ) {

        Objects.requireNonNull(
                item,
                "item cannot be null"
        );

        Objects.requireNonNull(
                key,
                "key cannot be null"
        );

        Objects.requireNonNull(
                value,
                "value cannot be null"
        );

        item.editPersistentDataContainer(
                container ->
                        container.set(
                                key.key(),
                                key.type(),
                                value
                        )
        );
    }

    public static <T> Optional<T> get(
            ItemStack item,
            PdcKey<T> key
    ) {

        Objects.requireNonNull(
                item,
                "item cannot be null"
        );

        Objects.requireNonNull(
                key,
                "key cannot be null"
        );

        return Optional.ofNullable(
                item.getPersistentDataContainer()
                        .get(
                                key.key(),
                                key.type()
                        )
        );
    }

    public static boolean has(
            ItemStack item,
            PdcKey<?> key
    ) {

        Objects.requireNonNull(item, "item cannot be null");
        Objects.requireNonNull(key, "key cannot be null");

        return item.getPersistentDataContainer()
                .has(
                        key.key(),
                        key.type()
                );
    }

    public static void remove(
            ItemStack item,
            PdcKey<?> key
    ) {

        Objects.requireNonNull(item, "item cannot be null");
        Objects.requireNonNull(key, "key cannot be null");

        item.editPersistentDataContainer(
                container ->
                        container.remove(
                                key.key()
                        )
        );
    }
}
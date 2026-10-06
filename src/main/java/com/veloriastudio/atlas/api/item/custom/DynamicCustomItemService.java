package com.veloriastudio.atlas.api.item.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.concurrent.CompletableFuture;

public interface DynamicCustomItemService {

    CompletableFuture<StoredCustomItem> create(
            NamespacedKey id,
            CustomItemCategory category,
            ItemStack template
    );

    CompletableFuture<StoredCustomItem> update(
            NamespacedKey id,
            CustomItemCategory category,
            ItemStack template
    );

    CompletableFuture<Boolean> delete(
            NamespacedKey id
    );
}
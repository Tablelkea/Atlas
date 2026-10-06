package com.veloriastudio.atlas.api.item.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Optional;

public interface CustomItemRegistry {

    void register(CustomItem item);

    void replace(CustomItem item);

    Optional<CustomItem> unregister(NamespacedKey id);

    Optional<CustomItem> find(NamespacedKey id);

    Optional<CustomItem> identify(ItemStack item);

    ItemStack create(NamespacedKey id);

    boolean is(
            ItemStack item,
            NamespacedKey id
    );

    Collection<CustomItem> all();
}
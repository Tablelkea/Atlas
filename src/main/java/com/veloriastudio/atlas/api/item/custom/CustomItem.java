package com.veloriastudio.atlas.api.item.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public interface CustomItem {

    NamespacedKey id();

    ItemStack create();

    default CustomItemCategory category() {
        return CustomItemCategory.of("misc");
    }

    default boolean editable() {
        return false;
    }
}
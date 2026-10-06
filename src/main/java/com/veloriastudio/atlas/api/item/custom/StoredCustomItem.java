package com.veloriastudio.atlas.api.item.custom;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public final class StoredCustomItem implements CustomItem {

    private final NamespacedKey id;
    private final CustomItemCategory category;
    private final ItemStack template;

    public StoredCustomItem(
            NamespacedKey id,
            CustomItemCategory category,
            ItemStack template
    ) {
        this.id = Objects.requireNonNull(id);
        this.category = Objects.requireNonNull(category);

        Objects.requireNonNull(template);

        if (template.getType().isAir()) {
            throw new IllegalArgumentException(
                    "template cannot be air"
            );
        }

        this.template = template.clone();
    }

    @Override
    public NamespacedKey id() {
        return id;
    }

    @Override
    public CustomItemCategory category() {
        return category;
    }

    @Override
    public boolean editable() {
        return true;
    }

    @Override
    public ItemStack create() {
        return template.clone();
    }

    public ItemStack template() {
        return template.clone();
    }
}


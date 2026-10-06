package com.veloriastudio.atlas.api.item.custom;

import com.veloriastudio.atlas.api.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class LegendarySword implements CustomItem {

    private final NamespacedKey id;

    public LegendarySword(Plugin plugin) {
        this.id = new NamespacedKey(
                plugin,
                "legendary_sword"
        );
    }

    @Override
    public NamespacedKey id() {
        return id;
    }

    @Override
    public ItemStack create() {
        return ItemBuilder.of(Material.DIAMOND_SWORD)
                .name(Component.text("Legendary Sword"))
                .glint(true)
                .build();
    }
}

package com.veloriastudio.atlas.api.item;

import com.veloriastudio.atlas.api.pdc.PdcKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Objects;

public final class ItemBuilder {

    private final ItemStack itemStack;

    private ItemBuilder(Material material) {
        Objects.requireNonNull(
                material,
                "material cannot be null"
        );

        this.itemStack = new ItemStack(material);
    }

    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder amount(int amount) {

        if (amount < 1) {
            throw new IllegalArgumentException(
                    "amount must be greater than 0"
            );
        }

        itemStack.setAmount(amount);

        return this;
    }

    public ItemBuilder name(Component name) {

        Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        itemStack.editMeta(meta ->
                meta.customName(name)
        );

        return this;
    }

    public ItemBuilder lore(List<? extends Component> lore) {

        Objects.requireNonNull(
                lore,
                "lore cannot be null"
        );

        itemStack.editMeta(meta ->
                meta.lore(List.copyOf(lore))
        );

        return this;
    }

    public ItemBuilder lore(Component... lines) {

        Objects.requireNonNull(
                lines,
                "lines cannot be null"
        );

        return lore(List.of(lines));
    }

    public ItemBuilder unbreakable() {

        itemStack.editMeta(meta ->
                meta.setUnbreakable(true)
        );

        return this;
    }

    public ItemStack build() {
        return itemStack.clone();
    }

    public ItemBuilder enchant(
            Enchantment enchantment,
            int level
    ) {

        Objects.requireNonNull(
                enchantment,
                "enchantment cannot be null"
        );

        if (level < 1) {
            throw new IllegalArgumentException(
                    "enchantment level must be greater than 0"
            );
        }

        itemStack.editMeta(meta ->
                meta.addEnchant(
                        enchantment,
                        level,
                        false
                )
        );

        return this;
    }

    public ItemBuilder unsafeEnchant(
            Enchantment enchantment,
            int level
    ) {

        Objects.requireNonNull(
                enchantment,
                "enchantment cannot be null"
        );

        if (level < 1) {
            throw new IllegalArgumentException(
                    "enchantment level must be greater than 0"
            );
        }

        itemStack.editMeta(meta ->
                meta.addEnchant(
                        enchantment,
                        level,
                        true
                )
        );

        return this;
    }

    public ItemBuilder flags(
            ItemFlag... flags
    ) {

        Objects.requireNonNull(
                flags,
                "flags cannot be null"
        );

        itemStack.editMeta(meta ->
                meta.addItemFlags(flags)
        );

        return this;
    }

    public ItemBuilder glint(boolean glint) {

        itemStack.editMeta(meta ->
                meta.setEnchantmentGlintOverride(glint)
        );

        return this;
    }

    public ItemBuilder maxStackSize(
            int maxStackSize
    ) {

        if (maxStackSize < 1
                || maxStackSize > 99) {

            throw new IllegalArgumentException(
                    "maxStackSize must be between 1 and 99"
            );
        }

        itemStack.editMeta(meta ->
                meta.setMaxStackSize(maxStackSize)
        );

        return this;
    }

    public <T> ItemBuilder data(
            PdcKey<T> key,
            T value
    ) {

        ItemData.set(
                itemStack,
                key,
                value
        );

        return this;
    }
}
package com.veloriastudio.atlas.internal.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

final class AtlasGuiHolder implements InventoryHolder {

    private final DefaultGui gui;
    private Inventory inventory;

    AtlasGuiHolder(DefaultGui gui) {
        this.gui = Objects.requireNonNull(gui, "gui cannot be null");
    }

    void attach(Inventory inventory) {
        this.inventory = Objects.requireNonNull(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {

        return Objects.requireNonNull(inventory, "inventory has not been attached yet");

    }

    public DefaultGui gui() {
        return this.gui;
    }
}

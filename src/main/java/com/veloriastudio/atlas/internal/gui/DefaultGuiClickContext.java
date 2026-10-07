package com.veloriastudio.atlas.internal.gui;

import com.veloriastudio.atlas.api.gui.GuiClickContext;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Objects;

record DefaultGuiClickContext(
        Player player,
        int slot,
        ClickType click
) implements GuiClickContext {

    DefaultGuiClickContext {
        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        Objects.requireNonNull(
                click,
                "click cannot be null"
        );

        if (slot < 0) {
            throw new IllegalArgumentException(
                    "slot cannot be negative"
            );
        }
    }
}
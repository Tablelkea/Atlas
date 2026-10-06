package com.veloriastudio.atlas.internal.gui;

import com.veloriastudio.atlas.api.gui.GuiClickContext;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Objects;

public record DefaultGuiClickContext(Player player, int slot, ClickType click) implements GuiClickContext {

    public DefaultGuiClickContext(Player player, int slot, ClickType click) {

        this.player = Objects.requireNonNull(player, "player cannot be null");
        this.click = Objects.requireNonNull(click, "click cannot be null");
        this.slot = slot;

    }
}

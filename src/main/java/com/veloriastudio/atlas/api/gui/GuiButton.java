package com.veloriastudio.atlas.api.gui;

import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public interface GuiButton {

    static GuiButton of(ItemStack icon, Consumer<GuiClickContext> consumer) {
        return new DefaultGuiButton(icon, consumer);
    }

    ItemStack icon();

    void click(GuiClickContext context);

}

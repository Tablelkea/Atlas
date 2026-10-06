package com.veloriastudio.atlas.api.gui;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;

public final class DefaultGuiButton implements GuiButton {

    private final ItemStack itemStack;
    private final Consumer<GuiClickContext> consumer;

    DefaultGuiButton(ItemStack itemStack, Consumer<GuiClickContext> context) {

        this.itemStack = Objects.requireNonNull(itemStack, "itemstack cannot be null").clone();
        this.consumer = Objects.requireNonNull(context, "context cannot be null");

    }

    @Override
    public ItemStack icon() {
        return this.itemStack.clone();
    }

    @Override
    public void click(GuiClickContext context) {

        Objects.requireNonNull(context, "context cannot be null");

        consumer.accept(context);
    }
}

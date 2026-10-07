package com.veloriastudio.atlas.api.gui;

import org.bukkit.inventory.ItemStack;

import java.util.Objects;
import java.util.function.Consumer;

final class DefaultGuiButton implements GuiButton {

    private final ItemStack itemStack;
    private final Consumer<GuiClickContext> consumer;

    DefaultGuiButton(
            ItemStack itemStack,
            Consumer<GuiClickContext> consumer
    ) {
        this.itemStack = Objects.requireNonNull(
                itemStack,
                "itemStack cannot be null"
        ).clone();

        this.consumer = Objects.requireNonNull(
                consumer,
                "consumer cannot be null"
        );
    }

    @Override
    public ItemStack icon() {
        return itemStack.clone();
    }

    @Override
    public void click(GuiClickContext context) {
        Objects.requireNonNull(
                context,
                "context cannot be null"
        );

        consumer.accept(context);
    }
}
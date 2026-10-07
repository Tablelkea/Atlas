package com.veloriastudio.atlas.internal.gui;

import com.veloriastudio.atlas.api.gui.Gui;
import com.veloriastudio.atlas.api.gui.GuiButton;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class DefaultGui
        implements Gui {

    private final Component title;
    private final int rows;
    private final Map<Integer, GuiButton> buttons;

    public DefaultGui(
            Component title,
            int rows,
            Map<Integer, GuiButton> buttons
    ) {
        this.title = Objects.requireNonNull(
                title,
                "title cannot be null"
        );

        if (rows < 1
                || rows > 6) {

            throw new IllegalArgumentException(
                    "rows must be between 1 and 6"
            );
        }

        this.rows = rows;

        Objects.requireNonNull(
                buttons,
                "buttons cannot be null"
        );

        int maxSlot =
                rows * 9 - 1;

        for (Map.Entry<Integer, GuiButton> entry
                : buttons.entrySet()) {

            Integer slot =
                    Objects.requireNonNull(
                            entry.getKey(),
                            "button slot cannot be null"
                    );

            Objects.requireNonNull(
                    entry.getValue(),
                    "button cannot be null"
            );

            if (slot < 0
                    || slot > maxSlot) {

                throw new IllegalArgumentException(
                        "every button slot must be between 0 and "
                                + maxSlot
                );
            }
        }

        this.buttons =
                Map.copyOf(buttons);
    }

    @Override
    public Component title() {
        return title;
    }

    @Override
    public int rows() {
        return rows;
    }

    @Override
    public void open(
            Player player
    ) {
        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        AtlasGuiHolder holder =
                new AtlasGuiHolder(
                        this
                );

        Inventory inventory =
                Bukkit.createInventory(
                        holder,
                        rows * 9,
                        title
                );

        holder.attach(
                inventory
        );

        for (Map.Entry<Integer, GuiButton> entry
                : buttons.entrySet()) {

            GuiButton button =
                    entry.getValue();

            ItemStack icon =
                    Objects.requireNonNull(
                            button.icon(),
                            "GuiButton#icon() cannot return null"
                    );

            if (icon.getType().isAir()) {
                throw new IllegalStateException(
                        "GuiButton#icon() cannot return air"
                );
            }

            inventory.setItem(
                    entry.getKey(),
                    icon.clone()
            );
        }

        player.openInventory(
                inventory
        );
    }

    Optional<GuiButton> button(
            int slot
    ) {
        return Optional.ofNullable(
                buttons.get(slot)
        );
    }
}
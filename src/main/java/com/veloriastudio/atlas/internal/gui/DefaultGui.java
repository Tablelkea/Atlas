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

public final class DefaultGui implements Gui {

    private final Component title;
    private final int rows;
    private final Map<Integer, GuiButton> buttons;

    public DefaultGui(Component title, int rows, Map<Integer, GuiButton> buttons) {
        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.rows = rows;
        this.buttons = Map.copyOf(Objects.requireNonNull(buttons, "buttons cannot be null"));

        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("rows must be between 1 and 6");
        }

        int maxSlot = rows * 9 - 1;

        for (Integer slot : buttons.keySet()) {
            if (slot < 0 || slot > maxSlot) {
                throw new IllegalArgumentException("every slot button must be between 0 and " + maxSlot);
            }
        }
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
    public void open(Player player) {

        Objects.requireNonNull(player, "player cannot be null");

        AtlasGuiHolder holder = new AtlasGuiHolder(this);

        int size = rows * 9;

        Inventory gui = Bukkit.createInventory(holder, size, title);

        holder.attach(gui);

        for (Map.Entry<Integer, GuiButton> entry : buttons.entrySet()) {

            ItemStack icon = entry.getValue().icon().clone();
            int slot = entry.getKey();
            gui.setItem(slot, icon);

        }

        player.openInventory(gui);

    }

    Optional<GuiButton> button(int slot) {

        return Optional.ofNullable(buttons.get(slot));

    }
}

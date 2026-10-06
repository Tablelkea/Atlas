package com.veloriastudio.atlas.internal.gui;

import com.veloriastudio.atlas.api.gui.GuiButton;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.Optional;

public final class AtlasGuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {

        Inventory top = event.getView().getTopInventory();

        HumanEntity entity = event.getWhoClicked();

        if (!(top.getHolder() instanceof AtlasGuiHolder holder)) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        int rawSlot = event.getRawSlot();

        if (rawSlot >= 0 && rawSlot < top.getSize()) {
            event.setCancelled(true);

            Optional<GuiButton> button = holder.gui().button(rawSlot);

            if (button.isEmpty()) {
                return;
            }
            button.get().click(new DefaultGuiClickContext(player, rawSlot, event.getClick()));
        }

        InventoryAction action = event.getAction();

        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY || action == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {

        Inventory top = event.getView().getTopInventory();

        if (!(top.getHolder() instanceof AtlasGuiHolder)) {
            return;
        }

        boolean touchesGui = event.getRawSlots().stream().anyMatch(rawSlot -> rawSlot < top.getSize());

        if (touchesGui) {
            event.setCancelled(true);
        }

    }

}

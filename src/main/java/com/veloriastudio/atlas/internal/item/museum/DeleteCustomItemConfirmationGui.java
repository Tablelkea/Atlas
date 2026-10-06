package com.veloriastudio.atlas.internal.item.museum;

import com.veloriastudio.atlas.api.gui.Gui;
import com.veloriastudio.atlas.api.gui.GuiBuilder;
import com.veloriastudio.atlas.api.gui.GuiButton;
import com.veloriastudio.atlas.api.item.ItemBuilder;
import com.veloriastudio.atlas.api.item.custom.CustomItem;
import com.veloriastudio.atlas.api.item.custom.DynamicCustomItemService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class DeleteCustomItemConfirmationGui implements Gui {

    private Component title;
    private CustomItem item;
    private DynamicCustomItemService storage;
    private JavaPlugin plugin;

    public DeleteCustomItemConfirmationGui(
            Component title,
            CustomItem item,
            DynamicCustomItemService storage,
            JavaPlugin plugin

    ) {

        this.title = Objects.requireNonNull(title, "title cannot be null");
        this.item = Objects.requireNonNull(item, "item cannot be null");
        this.storage = Objects.requireNonNull(storage, "storage cannot be null");
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }

    public void cancel(Player player) {

        Objects.requireNonNull(player, "player cannot be null");

        player.sendMessage(Component.text("Opération annulée.").color(NamedTextColor.RED));
    }

    public void accept(Player player) {

        Objects.requireNonNull(player, "player cannot be null");

        storage.delete(item.id()).whenComplete(
                (deleted, throwable) -> {
                    if (throwable != null) {
                        player.sendMessage(Component.text("Une erreur c'est produite, opération annulée.").color(NamedTextColor.RED));
                        throw new IllegalStateException("Error while deleting this item: " + item.id(), throwable);
                    }
                    player.sendMessage(Component.text("Opération déroulée avec succès.").color(NamedTextColor.GREEN));
                }
        );
    }

    @Override
    public Component title() {
        return this.title;
    }

    @Override
    public int rows() {
        return 3;
    }

    @Override
    public void open(Player player) {
        Objects.requireNonNull(player, "player cannot be null");

        ItemStack accept = ItemBuilder.of(Material.LIME_CONCRETE)
                .amount(1)
                .name(Component.text("Accepter").color(NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true))
                .build();

        ItemStack cancel = ItemBuilder.of(Material.RED_CONCRETE)
                .amount(1)
                .name(Component.text("Annuler").color(NamedTextColor.RED).decoration(TextDecoration.BOLD, true))
                .build();

        GuiBuilder builder = new GuiBuilder();

        Gui gui = builder.title(title)
                .rows(rows())
                .button(16, GuiButton.of(
                        accept,
                        context -> {
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                accept(player);
                                player.closeInventory();

                            });
                        }
                ))
                .button(10, GuiButton.of(
                        cancel,
                        context -> {
                            plugin.getServer().getScheduler().runTask(plugin, () -> {
                                cancel(player);
                                player.closeInventory();
                            });
                        }
                ))
                .button(13, GuiButton.of(
                        item.create(),
                        context -> {}
                ))

                .border(
                        GuiButton.of(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE), action -> {})
                ).fill(
                        GuiButton.of(ItemStack.of(Material.GRAY_STAINED_GLASS_PANE), action -> {})
                )
                .build();



        gui.open(player);
    }
}

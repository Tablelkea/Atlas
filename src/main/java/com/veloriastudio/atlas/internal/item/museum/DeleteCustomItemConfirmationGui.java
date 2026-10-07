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
import java.util.logging.Level;

final class DeleteCustomItemConfirmationGui implements Gui {

    private static final int ROWS = 3;

    private final Component title;
    private final CustomItem item;
    private final DynamicCustomItemService dynamicItems;
    private final JavaPlugin plugin;

    DeleteCustomItemConfirmationGui(
            Component title,
            CustomItem item,
            DynamicCustomItemService dynamicItems,
            JavaPlugin plugin
    ) {
        this.title = Objects.requireNonNull(
                title,
                "title cannot be null"
        );

        this.item = Objects.requireNonNull(
                item,
                "item cannot be null"
        );

        this.dynamicItems = Objects.requireNonNull(
                dynamicItems,
                "dynamicItems cannot be null"
        );

        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );

        if (!item.editable()) {
            throw new IllegalArgumentException(
                    "custom item is not editable: "
                            + item.id().asString()
            );
        }
    }

    @Override
    public Component title() {
        return title;
    }

    @Override
    public int rows() {
        return ROWS;
    }

    @Override
    public void open(Player player) {
        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        ItemStack acceptButton =
                ItemBuilder.of(Material.LIME_CONCRETE)
                        .name(
                                Component.text("Accepter")
                                        .color(NamedTextColor.GREEN)
                                        .decoration(
                                                TextDecoration.BOLD,
                                                true
                                        )
                        )
                        .build();

        ItemStack cancelButton =
                ItemBuilder.of(Material.RED_CONCRETE)
                        .name(
                                Component.text("Annuler")
                                        .color(NamedTextColor.RED)
                                        .decoration(
                                                TextDecoration.BOLD,
                                                true
                                        )
                        )
                        .build();

        Gui gui = new GuiBuilder()
                .title(title)
                .rows(ROWS)
                .button(
                        10,
                        GuiButton.of(
                                cancelButton,
                                context -> {
                                    context.player()
                                            .closeInventory();

                                    cancel(
                                            context.player()
                                    );
                                }
                        )
                )
                .button(
                        13,
                        GuiButton.of(
                                item.create(),
                                context -> {
                                }
                        )
                )
                .button(
                        16,
                        GuiButton.of(
                                acceptButton,
                                context -> {
                                    context.player()
                                            .closeInventory();

                                    delete(
                                            context.player()
                                    );
                                }
                        )
                )
                .border(
                        GuiButton.of(
                                ItemStack.of(
                                        Material.BLACK_STAINED_GLASS_PANE
                                ),
                                context -> {
                                }
                        )
                )
                .fill(
                        GuiButton.of(
                                ItemStack.of(
                                        Material.GRAY_STAINED_GLASS_PANE
                                ),
                                context -> {
                                }
                        )
                )
                .build();

        gui.open(player);
    }

    private void cancel(Player player) {
        player.sendMessage(
                Component.text(
                        "Opération annulée."
                ).color(
                        NamedTextColor.RED
                )
        );
    }

    private void delete(Player player) {
        dynamicItems.delete(
                item.id()
        ).whenComplete(
                (deleted, throwable) ->
                        plugin.getServer()
                                .getScheduler()
                                .runTask(
                                        plugin,
                                        () -> handleDeleteResult(
                                                player,
                                                deleted,
                                                throwable
                                        )
                                )
        );
    }

    private void handleDeleteResult(
            Player player,
            Boolean deleted,
            Throwable throwable
    ) {
        if (throwable != null) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Failed to delete custom item "
                            + item.id().asString(),
                    throwable
            );

            if (player.isOnline()) {
                player.sendMessage(
                        Component.text(
                                "Une erreur est survenue pendant la suppression."
                        ).color(
                                NamedTextColor.RED
                        )
                );
            }

            return;
        }

        if (!player.isOnline()) {
            return;
        }

        if (!Boolean.TRUE.equals(deleted)) {
            player.sendMessage(
                    Component.text(
                            "L'item n'existe plus."
                    ).color(
                            NamedTextColor.RED
                    )
            );

            return;
        }

        player.sendMessage(
                Component.text(
                        "Item supprimé avec succès."
                ).color(
                        NamedTextColor.GREEN
                )
        );
    }
}
package com.veloriastudio.atlas.internal.item.museum;

import com.veloriastudio.atlas.api.gui.Gui;
import com.veloriastudio.atlas.api.gui.GuiButton;
import com.veloriastudio.atlas.api.gui.PaginatedGui;
import com.veloriastudio.atlas.api.item.custom.CustomItem;
import com.veloriastudio.atlas.api.item.custom.CustomItemRegistry;
import com.veloriastudio.atlas.api.item.custom.DynamicCustomItemService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class CustomItemMuseum {

    private final CustomItemRegistry registry;
    private final DynamicCustomItemService storage;
    private final JavaPlugin plugin;

    public CustomItemMuseum(CustomItemRegistry registry, DynamicCustomItemService storage, JavaPlugin plugin) {
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
        this.storage = Objects.requireNonNull(storage, "storage cannot be null");
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }

    public void open(Player player) {
        open(player, "");
    }

    public void open(Player player, String path) {

        Objects.requireNonNull(player, "player cannot be null");
        Objects.requireNonNull(path, "path cannot be null");

        List<CustomItem> items = findItems(path);

        List<Integer> contentSlots = List.of(10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34);


        Gui gui = new PaginatedGui<>(Component.text(path.isEmpty() ? "Custom Item Museum" : "Museum - " + path), 5, items, contentSlots, item -> {

            ItemStack icon = registry.create(item.id());

            icon.editMeta(meta -> meta.lore(buildMuseumLore(meta, item)));

            return GuiButton.of(icon, click -> {
                switch (click.click()) {

                    case LEFT -> {
                        player.getInventory().addItem(registry.create(item.id()));
                    }

                    case RIGHT -> click.player().sendMessage("EDIT " + item.id().asString());

                    case DROP, CONTROL_DROP -> new DeleteCustomItemConfirmationGui(
                            Component.text("Supression - " + item.id().asString()),
                            item,
                            storage,
                            plugin
                    ).open(player);

                    default -> {
                    }
                }
            });
        }

        ).fill(GuiButton.of(ItemStack.of(Material.GRAY_STAINED_GLASS_PANE), click -> {
        })).border(GuiButton.of(ItemStack.of(Material.BLACK_STAINED_GLASS_PANE), click -> {
        }));

        gui.open(player);
    }

    private List<CustomItem> findItems(String path) {

        List<CustomItem> items = new ArrayList<>();

        for (CustomItem item : registry.all()) {

            String itemPath = item.category().path();

            if (path.isEmpty() || itemPath.equals(path) || itemPath.startsWith(path + "/")) {
                items.add(item);
            }
        }

        return items;
    }

    private List<Component> buildMuseumLore(ItemMeta meta, CustomItem item) {

        List<Component> lore = meta.lore();
        List<Component> museumLore = lore == null ? new ArrayList<>() : new ArrayList<>(lore);

        museumLore.add(Component.empty());

        museumLore.add(Component.text("Chemin: ").color(NamedTextColor.DARK_GRAY).decoration(TextDecoration.ITALIC, false).append(Component.text(item.category().path()).color(NamedTextColor.YELLOW)));

        museumLore.add(Component.empty());

        museumLore.add(Component.text("Clic gauche ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.YELLOW).append(Component.text("→ Prendre 1").color(NamedTextColor.GRAY)));

        museumLore.add(Component.text("Clic droit ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.AQUA).append(Component.text("→ Modifier").color(NamedTextColor.GRAY)));

        museumLore.add(Component.text("Lâcher (Q) ").decoration(TextDecoration.ITALIC, false).color(NamedTextColor.RED).append(Component.text("→ Supprimer").color(NamedTextColor.GRAY)));

        return museumLore;

    }
}
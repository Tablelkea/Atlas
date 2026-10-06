package com.veloriastudio.atlas.internal.item.museum;

import com.veloriastudio.atlas.api.dialog.DialogBuilder;
import com.veloriastudio.atlas.api.gui.Gui;
import com.veloriastudio.atlas.api.gui.GuiButton;
import com.veloriastudio.atlas.api.gui.PaginatedGui;
import com.veloriastudio.atlas.api.item.custom.CustomItem;
import com.veloriastudio.atlas.api.item.custom.CustomItemCategory;
import com.veloriastudio.atlas.api.item.custom.CustomItemRegistry;
import com.veloriastudio.atlas.api.item.custom.DynamicCustomItemService;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class CustomItemMuseum {

    private final CustomItemRegistry registry;
    private final DynamicCustomItemService storage;
    private final JavaPlugin plugin;
    private final CustomItemMuseum museum;

    public CustomItemMuseum(CustomItemRegistry registry, DynamicCustomItemService storage, JavaPlugin plugin, CustomItemMuseum museum) {
        this.registry = Objects.requireNonNull(registry, "registry cannot be null");
        this.storage = Objects.requireNonNull(storage, "storage cannot be null");
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        this.museum = Objects.requireNonNull(museum, "museum cannot be null");
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

                        ItemStack created =
                                registry.create(item.id());

                        created.setAmount(1);

                        click.player()
                                .getInventory()
                                .addItem(created);
                    }

                    case RIGHT -> {

                        ItemStack itemStack = item.create();
                        ItemMeta meta = itemStack.getItemMeta();

                        Component title = meta.hasCustomName()
                                ? meta.customName()
                                : itemStack.effectiveName();

                        String serializedTitle = meta.hasCustomName()
                                ? MiniMessage.miniMessage().serialize(meta.customName())
                                : "";

                        boolean initialGlint =
                                meta.hasEnchantmentGlintOverride()
                                        && meta.getEnchantmentGlintOverride();

                        int maxStack = meta.hasMaxStackSize()
                                ? meta.getMaxStackSize()
                                : itemStack.getMaxStackSize();

                        String lore = "";

                        if(itemStack.lore() != null){
                            lore = itemStack.lore().stream()
                                    .map(MiniMessage.miniMessage()::serialize)
                                    .collect(Collectors.joining("\n"));
                        }



                        DialogBuilder.create(Component.text("Edition d'item"))
                                .body(
                                        DialogBody.item(
                                                itemStack
                                        ).build()
                                ).text(
                                        "item_id",
                                        Component.text(
                                                "Item ID"
                                        ),
                                        item.id().asString()
                                ).text(
                                        "category",
                                        Component.text(
                                                "Category"
                                        ),
                                        item.category().path()
                                ).text(
                                        "display_name",
                                        Component.text(
                                                "Display Name"
                                        ),
                                        serializedTitle
                                ).textArea(
                                        "lore",
                                        Component.text("Lore"),
                                        lore
                                ).toggle(
                                        "glind",
                                Component.text("Glint"),
                                        initialGlint
                                ).toggle(
                                        "unbreakable",
                                Component.text("Unbreakable"),
                                meta.isUnbreakable()
                                ).number(
                                        "max_stack",
                                        Component.text("Max Stack Size"),
                                        1,
                                        99,
                                        maxStack,
                                        1
                                ).cancel(Component.text("Cancel the edition")
                                ).confirm(Component.text("Complete the edition"), (response, audience) -> {

                                    // 1. Récupération
                                    String itemId = response.getText("item_id");
                                    String category = response.getText("category");
                                    String displayName = response.getText("display_name");
                                    String loreInput = response.getText("lore");

                                    Boolean glint = response.getBoolean("glind");
                                    Boolean unbreakable = response.getBoolean("unbreakable");

                                    Float maxStackInput = response.getFloat("max_stack");

                                    // 2. Validation
                                    if (itemId == null
                                            || category == null
                                            || displayName == null
                                            || loreInput == null
                                            || glint == null
                                            || unbreakable == null
                                            || maxStackInput == null) {

                                        player.sendMessage(Component.text("Invalid item data."));
                                        return;
                                    }

                                    int editMaxStack = maxStackInput.intValue();

                                    ItemStack updatedStack = itemStack.clone();
                                    ItemMeta newMeta = updatedStack.getItemMeta();

                                    newMeta.setEnchantmentGlintOverride(glint);
                                    newMeta.setUnbreakable(unbreakable);
                                    newMeta.setMaxStackSize(editMaxStack);

                                    MiniMessage miniMessage = MiniMessage.miniMessage();

                                    List<Component> newLore = Arrays.stream(loreInput.split("\\R"))
                                            .map(miniMessage::deserialize)
                                            .map(component ->
                                                    component.decoration(TextDecoration.ITALIC, false)
                                            )
                                            .toList();

                                    newMeta.lore(newLore);

                                    newMeta.customName(
                                            miniMessage.deserialize(displayName)
                                                    .decoration(TextDecoration.ITALIC, false)
                                    );

                                    updatedStack.setItemMeta(newMeta);

                                    storage.update(
                                            item.id(),
                                            CustomItemCategory.of(category),
                                            updatedStack
                                    );

                                    player.closeInventory();
                                    museum.open(player);
                                })


                                .open(player);
                    }


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
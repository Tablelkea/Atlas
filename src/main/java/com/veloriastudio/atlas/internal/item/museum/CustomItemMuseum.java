package com.veloriastudio.atlas.internal.item.museum;

import com.veloriastudio.atlas.api.dialog.DialogBuilder;
import com.veloriastudio.atlas.api.gui.Gui;
import com.veloriastudio.atlas.api.gui.GuiButton;
import com.veloriastudio.atlas.api.gui.PaginatedGui;
import com.veloriastudio.atlas.api.item.custom.CustomItem;
import com.veloriastudio.atlas.api.item.custom.CustomItemCategory;
import com.veloriastudio.atlas.api.item.custom.CustomItemRegistry;
import com.veloriastudio.atlas.api.item.custom.DynamicCustomItemService;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.stream.Collectors;

public final class CustomItemMuseum {

    private static final List<Integer> CONTENT_SLOTS =
            List.of(
                    10, 11, 12, 13, 14, 15, 16,
                    19, 20, 21, 22, 23, 24, 25,
                    28, 29, 30, 31, 32, 33, 34
            );

    private static final MiniMessage MINI_MESSAGE =
            MiniMessage.miniMessage();

    private final CustomItemRegistry registry;
    private final DynamicCustomItemService dynamicItems;
    private final JavaPlugin plugin;

    public CustomItemMuseum(
            CustomItemRegistry registry,
            DynamicCustomItemService dynamicItems,
            JavaPlugin plugin
    ) {
        this.registry = Objects.requireNonNull(
                registry,
                "registry cannot be null"
        );

        this.dynamicItems = Objects.requireNonNull(
                dynamicItems,
                "dynamicItems cannot be null"
        );

        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );
    }

    public void open(
            Player player
    ) {
        open(
                player,
                ""
        );
    }

    public void open(
            Player player,
            String path
    ) {
        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        Objects.requireNonNull(
                path,
                "path cannot be null"
        );

        List<CustomItem> items =
                findItems(path);

        Gui gui =
                new PaginatedGui<>(
                        Component.text(
                                path.isEmpty()
                                        ? "Custom Item Museum"
                                        : "Museum - " + path
                        ),
                        5,
                        items,
                        CONTENT_SLOTS,
                        this::createMuseumButton
                )
                        .fill(
                                GuiButton.of(
                                        ItemStack.of(
                                                Material.GRAY_STAINED_GLASS_PANE
                                        ),
                                        click -> {
                                        }
                                )
                        )
                        .border(
                                GuiButton.of(
                                        ItemStack.of(
                                                Material.BLACK_STAINED_GLASS_PANE
                                        ),
                                        click -> {
                                        }
                                )
                        );

        gui.open(player);
    }

    private GuiButton createMuseumButton(
            CustomItem item
    ) {
        ItemStack icon =
                registry.create(
                        item.id()
                );

        icon.editMeta(
                meta ->
                        meta.lore(
                                buildMuseumLore(
                                        meta,
                                        item
                                )
                        )
        );

        return GuiButton.of(
                icon,
                context -> {
                    switch (context.click()) {

                        case LEFT ->
                                giveItem(
                                        context.player(),
                                        item
                                );

                        case RIGHT -> {
                            if (item.editable()) {
                                openEditDialog(
                                        context.player(),
                                        item
                                );
                            }
                        }

                        case DROP, CONTROL_DROP -> {
                            if (item.editable()) {
                                openDeleteConfirmation(
                                        context.player(),
                                        item
                                );
                            }
                        }

                        default -> {
                        }
                    }
                }
        );
    }

    private void giveItem(
            Player player,
            CustomItem item
    ) {
        ItemStack created =
                registry.create(
                        item.id()
                );

        created.setAmount(1);

        player.getInventory()
                .addItem(created);
    }

    private void openEditDialog(
            Player player,
            CustomItem item
    ) {
        ItemStack itemStack =
                item.create();

        ItemMeta meta =
                itemStack.getItemMeta();

        boolean damageable =
                isDamageable(
                        itemStack
                );

        /*
         * Paper sérialise l'ItemStack donné à DialogBody.item().
         *
         * MAX_DAMAGE + MAX_STACK_SIZE > 1 est une combinaison
         * invalide côté Minecraft.
         *
         * On crée donc uniquement une copie sûre pour l'aperçu.
         * L'item original reste intact jusqu'à la sauvegarde.
         */
        ItemStack preview =
                createDialogPreview(
                        itemStack
                );

        String serializedTitle =
                meta.hasCustomName()
                        ? MINI_MESSAGE.serialize(
                        Objects.requireNonNull(
                                meta.customName()
                        )
                )
                        : "";

        String lore =
                serializeLore(
                        itemStack
                );

        boolean glint =
                meta.hasEnchantmentGlintOverride()
                        && meta.getEnchantmentGlintOverride();

        int currentMaxStack =
                damageable
                        ? 1
                        : Math.min(
                        99,
                        itemStack.getMaxStackSize()
                );

        int maxStackLimit =
                damageable
                        ? 1
                        : 99;

        DialogBuilder builder =
                DialogBuilder.create(
                                Component.text(
                                        "Modifier l'item"
                                )
                        )
                        .body(
                                DialogBody.item(
                                                preview
                                        )
                                        .build()
                        )
                        .body(
                                DialogBody.plainMessage(
                                        Component.text(
                                                "ID : "
                                                        + item.id()
                                                        .asString()
                                        )
                                )
                        )
                        .text(
                                "category",
                                Component.text(
                                        "Catégorie"
                                ),
                                item.category()
                                        .path()
                        )
                        .text(
                                "display_name",
                                Component.text(
                                        "Nom"
                                ),
                                serializedTitle
                        )
                        .textArea(
                                "lore",
                                Component.text(
                                        "Lore"
                                ),
                                lore
                        )
                        .toggle(
                                "glint",
                                Component.text(
                                        "Glint"
                                ),
                                glint
                        )
                        .toggle(
                                "unbreakable",
                                Component.text(
                                        "Incassable"
                                ),
                                meta.isUnbreakable()
                        )
                        .number(
                                "max_stack",
                                Component.text(
                                        damageable
                                                ? "Taille du stack (fixée à 1)"
                                                : "Taille du stack"
                                ),
                                1,
                                maxStackLimit,
                                currentMaxStack,
                                1
                        )
                        .cancel(
                                Component.text(
                                        "Annuler"
                                )
                        )
                        .confirm(
                                Component.text(
                                        "Sauvegarder"
                                ),
                                (response, audience) ->
                                        applyEdit(
                                                player,
                                                item,
                                                itemStack,
                                                damageable,
                                                response
                                        )
                        );

        builder.open(
                player
        );
    }

    private void applyEdit(
            Player player,
            CustomItem item,
            ItemStack original,
            boolean damageable,
            DialogResponseView response
    ) {
        String categoryInput =
                response.getText(
                        "category"
                );

        String displayNameInput =
                response.getText(
                        "display_name"
                );

        String loreInput =
                response.getText(
                        "lore"
                );

        Boolean glint =
                response.getBoolean(
                        "glint"
                );

        Boolean unbreakable =
                response.getBoolean(
                        "unbreakable"
                );

        Float maxStackInput =
                response.getFloat(
                        "max_stack"
                );

        if (categoryInput == null
                || displayNameInput == null
                || loreInput == null
                || glint == null
                || unbreakable == null
                || maxStackInput == null) {

            player.sendMessage(
                    Component.text(
                                    "Données du formulaire invalides."
                            )
                            .color(
                                    NamedTextColor.RED
                            )
            );

            return;
        }

        int maxStack =
                maxStackInput.intValue();

        if (maxStack < 1
                || maxStack > 99) {

            player.sendMessage(
                    Component.text(
                                    "La taille du stack doit être comprise entre 1 et 99."
                            )
                            .color(
                                    NamedTextColor.RED
                            )
            );

            return;
        }

        /*
         * Sécurité supplémentaire :
         * même avec une réponse trafiquée, un item damageable
         * ne pourra jamais être enregistré avec un stack > 1.
         */
        if (damageable
                && maxStack != 1) {

            player.sendMessage(
                    Component.text(
                                    "Un item damageable ne peut pas avoir une taille de stack supérieure à 1."
                            )
                            .color(
                                    NamedTextColor.RED
                            )
            );

            return;
        }

        try {
            CustomItemCategory category =
                    CustomItemCategory.of(
                            categoryInput
                    );

            ItemStack updated =
                    original.clone();

            ItemMeta meta =
                    updated.getItemMeta();

            meta.setEnchantmentGlintOverride(
                    glint
            );

            meta.setUnbreakable(
                    unbreakable
            );

            /*
             * C'est aussi ce qui répare automatiquement
             * un ancien item invalide déjà présent en BDD.
             */
            meta.setMaxStackSize(
                    damageable
                            ? 1
                            : maxStack
            );

            if (displayNameInput.isBlank()) {
                meta.customName(
                        null
                );
            } else {
                meta.customName(
                        MINI_MESSAGE.deserialize(
                                        displayNameInput
                                )
                                .decoration(
                                        TextDecoration.ITALIC,
                                        false
                                )
                );
            }

            meta.lore(
                    parseLore(
                            loreInput
                    )
            );

            updated.setItemMeta(
                    meta
            );

            saveEdit(
                    player,
                    item,
                    category,
                    updated
            );

        } catch (IllegalArgumentException exception) {
            player.sendMessage(
                    Component.text(
                                    exception.getMessage() == null
                                            ? "Données invalides."
                                            : exception.getMessage()
                            )
                            .color(
                                    NamedTextColor.RED
                            )
            );
        }
    }

    private void saveEdit(
            Player player,
            CustomItem item,
            CustomItemCategory category,
            ItemStack updated
    ) {
        dynamicItems.update(
                        item.id(),
                        category,
                        updated
                )
                .whenComplete(
                        (ignored, throwable) ->
                                plugin.getServer()
                                        .getScheduler()
                                        .runTask(
                                                plugin,
                                                () -> {
                                                    if (throwable != null) {
                                                        plugin.getLogger()
                                                                .log(
                                                                        Level.SEVERE,
                                                                        "Failed to update custom item "
                                                                                + item.id()
                                                                                .asString(),
                                                                        throwable
                                                                );

                                                        if (player.isOnline()) {
                                                            player.sendMessage(
                                                                    Component.text(
                                                                                    "Impossible de sauvegarder l'item."
                                                                            )
                                                                            .color(
                                                                                    NamedTextColor.RED
                                                                            )
                                                            );
                                                        }

                                                        return;
                                                    }

                                                    if (player.isOnline()) {
                                                        player.sendMessage(
                                                                Component.text(
                                                                                "Item sauvegardé."
                                                                        )
                                                                        .color(
                                                                                NamedTextColor.GREEN
                                                                        )
                                                        );

                                                        open(
                                                                player
                                                        );
                                                    }
                                                }
                                        )
                );
    }

    private void openDeleteConfirmation(
            Player player,
            CustomItem item
    ) {
        new DeleteCustomItemConfirmationGui(
                Component.text(
                        "Suppression - "
                                + item.id()
                                .asString()
                ),
                item,
                dynamicItems,
                plugin
        ).open(
                player
        );
    }

    private ItemStack createDialogPreview(
            ItemStack source
    ) {
        ItemStack preview =
                source.clone();

        preview.setAmount(1);

        if (isDamageable(preview)
                && preview.getMaxStackSize() > 1) {

            preview.editMeta(
                    meta ->
                            meta.setMaxStackSize(
                                    1
                            )
            );
        }

        return preview;
    }

    private boolean isDamageable(
            ItemStack item
    ) {
        if (item.getType()
                .getMaxDurability() > 0) {

            return true;
        }

        ItemMeta meta =
                item.getItemMeta();

        return meta instanceof Damageable damageable
                && damageable.hasMaxDamage();
    }

    private List<CustomItem> findItems(
            String path
    ) {
        List<CustomItem> items =
                new ArrayList<>();

        for (CustomItem item
                : registry.all()) {

            String itemPath =
                    item.category()
                            .path();

            if (path.isEmpty()
                    || itemPath.equals(path)
                    || itemPath.startsWith(
                    path + "/"
            )) {

                items.add(item);
            }
        }

        return items;
    }

    private String serializeLore(
            ItemStack item
    ) {
        List<Component> lore =
                item.lore();

        if (lore == null
                || lore.isEmpty()) {

            return "";
        }

        return lore.stream()
                .map(
                        MINI_MESSAGE::serialize
                )
                .collect(
                        Collectors.joining(
                                "\n"
                        )
                );
    }

    private List<Component> parseLore(
            String input
    ) {
        if (input.isBlank()) {
            return List.of();
        }

        return Arrays.stream(
                        input.split(
                                "\\R"
                        )
                )
                .map(
                        MINI_MESSAGE::deserialize
                )
                .map(
                        component ->
                                component.decoration(
                                        TextDecoration.ITALIC,
                                        false
                                )
                )
                .toList();
    }

    private List<Component> buildMuseumLore(
            ItemMeta meta,
            CustomItem item
    ) {
        List<Component> currentLore =
                meta.lore();

        List<Component> lore =
                currentLore == null
                        ? new ArrayList<>()
                        : new ArrayList<>(
                        currentLore
                );

        lore.add(
                Component.empty()
        );

        lore.add(
                Component.text(
                                "Chemin: "
                        )
                        .color(
                                NamedTextColor.DARK_GRAY
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        )
                        .append(
                                Component.text(
                                                item.category()
                                                        .path()
                                        )
                                        .color(
                                                NamedTextColor.YELLOW
                                        )
                        )
        );

        lore.add(
                Component.empty()
        );

        lore.add(
                Component.text(
                                "Clic gauche "
                        )
                        .color(
                                NamedTextColor.YELLOW
                        )
                        .decoration(
                                TextDecoration.ITALIC,
                                false
                        )
                        .append(
                                Component.text(
                                                "→ Prendre 1"
                                        )
                                        .color(
                                                NamedTextColor.GRAY
                                        )
                        )
        );

        if (item.editable()) {
            lore.add(
                    Component.text(
                                    "Clic droit "
                            )
                            .color(
                                    NamedTextColor.AQUA
                            )
                            .decoration(
                                    TextDecoration.ITALIC,
                                    false
                            )
                            .append(
                                    Component.text(
                                                    "→ Modifier"
                                            )
                                            .color(
                                                    NamedTextColor.GRAY
                                            )
                            )
            );

            lore.add(
                    Component.text(
                                    "Lâcher (Q) "
                            )
                            .color(
                                    NamedTextColor.RED
                            )
                            .decoration(
                                    TextDecoration.ITALIC,
                                    false
                            )
                            .append(
                                    Component.text(
                                                    "→ Supprimer"
                                            )
                                            .color(
                                                    NamedTextColor.GRAY
                                            )
                            )
            );
        }

        return lore;
    }
}
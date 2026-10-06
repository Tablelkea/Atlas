package com.veloriastudio.atlas.internal.item.dialog;

import com.veloriastudio.atlas.api.item.ItemBuilder;
import com.veloriastudio.atlas.api.item.custom.CustomItemCategory;
import com.veloriastudio.atlas.api.item.custom.CustomItemRegistry;
import com.veloriastudio.atlas.api.item.custom.DynamicCustomItemService;
import com.veloriastudio.atlas.api.message.MessageService;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class CustomItemCreationDialog {

    private final Plugin plugin;
    private final DynamicCustomItemService dynamicItems;
    private final CustomItemRegistry customItems;
    private final MessageService messageService;

    public CustomItemCreationDialog(
            Plugin plugin,
            DynamicCustomItemService dynamicItems,
            CustomItemRegistry customItems,
            MessageService messageService
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );

        this.dynamicItems = Objects.requireNonNull(
                dynamicItems,
                "dynamicItems cannot be null"
        );

        this.customItems = Objects.requireNonNull(
                customItems,
                "customItems cannot be null"
        );

        this.messageService = Objects.requireNonNull(
                messageService,
                "messageService cannot be null"
        );
    }

    public void open(
            Player player,
            ItemStack source
    ) {

        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        Objects.requireNonNull(
                source,
                "source cannot be null"
        );

        if (source.getType().isAir()) {
            throw new IllegalArgumentException(
                    "source item cannot be air"
            );
        }

        /*
         * On garde une copie uniquement pour vérifier plus tard
         * que le joueur tient toujours le même item.
         */
        ItemStack original =
                source.clone();

        Material material =
                source.getType();

        /*
         * IMPORTANT :
         * preview entièrement propre.
         *
         * On n'envoie PAS source dans le Dialog,
         * donc aucun ancien component/meta problématique.
         */
        ItemStack preview =
                new ItemStack(material);

        Dialog dialog =
                Dialog.create(builder ->
                        builder.empty()
                                .base(
                                        DialogBase.builder(
                                                        Component.text(
                                                                "Create Custom Item"
                                                        )
                                                )
                                                .body(
                                                        List.of(
                                                                DialogBody.item(
                                                                                preview
                                                                        )
                                                                        .build(),

                                                                DialogBody.plainMessage(
                                                                        Component.text(
                                                                                "Base material: "
                                                                                        + material.name()
                                                                        )
                                                                )
                                                        )
                                                )
                                                .inputs(
                                                        List.of(
                                                                DialogInput.text(
                                                                                "item_id",
                                                                                Component.text(
                                                                                        "Item ID"
                                                                                )
                                                                        )
                                                                        .width(300)
                                                                        .maxLength(64)
                                                                        .build(),

                                                                DialogInput.text(
                                                                                "category",
                                                                                Component.text(
                                                                                        "Category"
                                                                                )
                                                                        )
                                                                        .initial("misc")
                                                                        .width(300)
                                                                        .maxLength(128)
                                                                        .build(),

                                                                DialogInput.text(
                                                                                "display_name",
                                                                                Component.text(
                                                                                        "Display name"
                                                                                )
                                                                        )
                                                                        .initial(
                                                                                defaultDisplayName(
                                                                                        material
                                                                                )
                                                                        )
                                                                        .width(300)
                                                                        .maxLength(256)
                                                                        .build(),

                                                                DialogInput.text(
                                                                                "lore",
                                                                                Component.text(
                                                                                        "Lore"
                                                                                )
                                                                        )
                                                                        .width(300)
                                                                        .maxLength(2048)
                                                                        .multiline(
                                                                                TextDialogInput.MultilineOptions.create(
                                                                                        10,
                                                                                        120
                                                                                )
                                                                        )
                                                                        .build(),

                                                                DialogInput.bool(
                                                                                "glint",
                                                                                Component.text(
                                                                                        "Glint"
                                                                                )
                                                                        )
                                                                        .initial(false)
                                                                        .build(),

                                                                DialogInput.bool(
                                                                                "unbreakable",
                                                                                Component.text(
                                                                                        "Unbreakable"
                                                                                )
                                                                        )
                                                                        .initial(false)
                                                                        .build(),

                                                                DialogInput.numberRange(
                                                                                "max_stack",
                                                                                Component.text(
                                                                                        "Max stack size"
                                                                                ),
                                                                                1.0F,
                                                                                99.0F
                                                                        )
                                                                        .initial(
                                                                                (float) Math.min(
                                                                                        material.getMaxStackSize(),
                                                                                        99
                                                                                )
                                                                        )
                                                                        .step(1.0F)
                                                                        .width(300)
                                                                        .build()
                                                        )
                                                )
                                                .build()
                                )
                                .type(
                                        DialogType.confirmation(
                                                createButton(
                                                        original,
                                                        material
                                                ),
                                                ActionButton.create(
                                                        Component.text(
                                                                "Cancel"
                                                        ),
                                                        Component.text(
                                                                "Cancel creation"
                                                        ),
                                                        120,
                                                        null
                                                )
                                        )
                                )
                );

        player.showDialog(dialog);
    }

    private ActionButton createButton(
            ItemStack original,
            Material material
    ) {

        return ActionButton.create(
                Component.text("Create"),
                Component.text(
                        "Create and save this custom item"
                ),
                120,
                DialogAction.customClick(
                        (view, audience) -> {

                            if (!(audience instanceof Player player)) {
                                return;
                            }

                            try {
                                String idValue =
                                        requireText(
                                                view.getText("item_id"),
                                                "Item ID"
                                        );

                                String categoryValue =
                                        requireText(
                                                view.getText("category"),
                                                "Category"
                                        );

                                String displayName =
                                        requireText(
                                                view.getText(
                                                        "display_name"
                                                ),
                                                "Display name"
                                        );

                                String lore =
                                        Objects.requireNonNullElse(
                                                view.getText("lore"),
                                                ""
                                        );

                                boolean glint =
                                        Boolean.TRUE.equals(
                                                view.getBoolean(
                                                        "glint"
                                                )
                                        );

                                boolean unbreakable =
                                        Boolean.TRUE.equals(
                                                view.getBoolean(
                                                        "unbreakable"
                                                )
                                        );

                                Float maxStackValue =
                                        view.getFloat(
                                                "max_stack"
                                        );

                                int maxStack =
                                        maxStackValue == null
                                                ? material.getMaxStackSize()
                                                : Math.round(
                                                maxStackValue
                                        );

                                NamespacedKey id =
                                        new NamespacedKey(
                                                plugin,
                                                idValue
                                        );

                                CustomItemCategory category =
                                        CustomItemCategory.of(
                                                categoryValue
                                        );

                                ItemStack newItem =
                                        buildItem(
                                                material,
                                                displayName,
                                                lore,
                                                glint,
                                                unbreakable,
                                                maxStack
                                        );

                                saveAndReplace(
                                        player,
                                        original,
                                        id,
                                        category,
                                        newItem
                                );

                            } catch (IllegalArgumentException exception) {

                                player.sendMessage(
                                        Component.text(
                                                "Invalid item: "
                                                        + exception.getMessage()
                                        )
                                );
                            }
                        },

                        ClickCallback.Options.builder()
                                .uses(1)
                                .lifetime(
                                        ClickCallback.DEFAULT_LIFETIME
                                )
                                .build()
                )
        );
    }

    private ItemStack buildItem(
            Material material,
            String displayName,
            String lore,
            boolean glint,
            boolean unbreakable,
            int maxStack
    ) {

        ItemBuilder builder =
                ItemBuilder.of(material)
                        .name(
                                messageService.render(
                                        displayName
                                )
                        )
                        .glint(glint)
                        .maxStackSize(maxStack);

        if (unbreakable) {
            builder.unbreakable();
        }

        List<Component> loreComponents =
                Arrays.stream(
                                lore.split("\\R")
                        )
                        .filter(line ->
                                !line.isBlank()
                        )
                        .map(
                                messageService::render
                        )
                        .toList();

        if (!loreComponents.isEmpty()) {
            builder.lore(
                    loreComponents
            );
        }

        return builder.build();
    }

    private void saveAndReplace(
            Player player,
            ItemStack original,
            NamespacedKey id,
            CustomItemCategory category,
            ItemStack newItem
    ) {

        dynamicItems.create(
                id,
                category,
                newItem
        ).whenComplete(
                (storedItem, exception) -> {

                    /*
                     * Le Future BDD peut terminer hors
                     * du thread serveur.
                     */
                    plugin.getServer()
                            .getScheduler()
                            .runTask(
                                    plugin,
                                    () -> {

                                        if (exception != null) {

                                            plugin.getLogger()
                                                    .severe(
                                                            "Failed to create custom item "
                                                                    + id.asString()
                                                                    + ": "
                                                                    + exception.getMessage()
                                                    );

                                            player.sendMessage(
                                                    Component.text(
                                                            "Unable to create the custom item."
                                                    )
                                            );

                                            return;
                                        }

                                        /*
                                         * Sécurité :
                                         * on ne remplace pas quelque chose
                                         * que le joueur aurait changé pendant
                                         * l'écriture BDD.
                                         */
                                        ItemStack current =
                                                player.getInventory()
                                                        .getItemInMainHand();

                                        if (!sameItem(
                                                current,
                                                original
                                        )) {

                                            player.sendMessage(
                                                    Component.text(
                                                            "Custom item saved, but your held item changed, so it was not replaced."
                                                    )
                                            );

                                            return;
                                        }

                                        /*
                                         * registry.create(id) ajoute également
                                         * notre PDC custom_item_id.
                                         */
                                        ItemStack created =
                                                customItems.create(
                                                        id
                                                );

                                        player.getInventory()
                                                .setItemInMainHand(
                                                        created
                                                );

                                        player.sendMessage(
                                                Component.text(
                                                        "Custom item created: "
                                                                + id.asString()
                                                )
                                        );
                                    }
                            );
                }
        );
    }

    private boolean sameItem(
            ItemStack current,
            ItemStack original
    ) {

        return current.getAmount()
                == original.getAmount()
                && current.isSimilar(
                original
        );
    }

    private String requireText(
            String value,
            String field
    ) {

        if (value == null
                || value.isBlank()) {

            throw new IllegalArgumentException(
                    field + " cannot be blank"
            );
        }

        return value.trim();
    }

    private String defaultDisplayName(
            Material material
    ) {

        return Arrays.stream(
                        material.name()
                                .toLowerCase(
                                        Locale.ROOT
                                )
                                .split("_")
                )
                .map(word ->
                        Character.toUpperCase(
                                word.charAt(0)
                        )
                                + word.substring(1)
                )
                .reduce(
                        (left, right) ->
                                left + " " + right
                )
                .orElse(
                        material.name()
                );
    }
}
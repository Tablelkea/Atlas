package com.veloriastudio.atlas.internal.item.dialog;

import com.veloriastudio.atlas.api.dialog.DialogBuilder;
import com.veloriastudio.atlas.api.item.ItemBuilder;
import com.veloriastudio.atlas.api.item.custom.CustomItemCategory;
import com.veloriastudio.atlas.api.item.custom.CustomItemRegistry;
import com.veloriastudio.atlas.api.item.custom.DynamicCustomItemService;
import com.veloriastudio.atlas.api.message.MessageService;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Level;

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

        ItemStack original =
                source.clone();

        Material material =
                source.getType();

        boolean damageable =
                material.getMaxDurability() > 0;

        /*
         * On utilise uniquement le matériau pour l'aperçu.
         * Aucun composant potentiellement invalide du source
         * n'est envoyé au Dialog.
         */
        ItemStack preview =
                new ItemStack(material);

        DialogBuilder builder =
                DialogBuilder.create(
                                Component.text(
                                        "Create Custom Item"
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
                                                "Base material: "
                                                        + material.name()
                                        )
                                )
                        )
                        .text(
                                "item_id",
                                Component.text(
                                        "Item ID"
                                )
                        )
                        .text(
                                "category",
                                Component.text(
                                        "Category"
                                ),
                                "misc"
                        )
                        .text(
                                "display_name",
                                Component.text(
                                        "Display name"
                                ),
                                defaultDisplayName(
                                        material
                                )
                        )
                        .textArea(
                                "lore",
                                Component.text(
                                        "Lore"
                                ),
                                ""
                        )
                        .toggle(
                                "glint",
                                Component.text(
                                        "Glint"
                                ),
                                false
                        )
                        .toggle(
                                "unbreakable",
                                Component.text(
                                        "Unbreakable"
                                ),
                                false
                        );

        /*
         * MAX_DAMAGE et MAX_STACK_SIZE > 1 ne peuvent pas
         * coexister côté Minecraft.
         */
        if (damageable) {
            builder.body(
                    DialogBody.plainMessage(
                            Component.text(
                                    "This item is damageable. "
                                            + "Its maximum stack size is fixed to 1."
                            )
                    )
            );
        } else {
            builder.number(
                    "max_stack",
                    Component.text(
                            "Max stack size"
                    ),
                    1,
                    99,
                    Math.min(
                            material.getMaxStackSize(),
                            99
                    ),
                    1
            );
        }

        builder.cancel(
                        Component.text(
                                "Cancel"
                        )
                )
                .confirm(
                        Component.text(
                                "Create"
                        ),
                        (response, audience) -> {
                            if (!(audience instanceof Player callbackPlayer)) {
                                return;
                            }

                            createItem(
                                    callbackPlayer,
                                    original,
                                    material,
                                    damageable,
                                    response
                            );
                        }
                )
                .open(
                        player
                );
    }

    private void createItem(
            Player player,
            ItemStack original,
            Material material,
            boolean damageable,
            DialogResponseView response
    ) {
        try {
            String idValue =
                    requireText(
                            response.getText(
                                    "item_id"
                            ),
                            "Item ID"
                    );

            String categoryValue =
                    requireText(
                            response.getText(
                                    "category"
                            ),
                            "Category"
                    );

            String displayName =
                    requireText(
                            response.getText(
                                    "display_name"
                            ),
                            "Display name"
                    );

            String lore =
                    Objects.requireNonNullElse(
                            response.getText(
                                    "lore"
                            ),
                            ""
                    );

            boolean glint =
                    Boolean.TRUE.equals(
                            response.getBoolean(
                                    "glint"
                            )
                    );

            boolean unbreakable =
                    Boolean.TRUE.equals(
                            response.getBoolean(
                                    "unbreakable"
                            )
                    );

            int maxStack =
                    damageable
                            ? 1
                            : readMaxStack(
                            response
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
    }

    private int readMaxStack(
            DialogResponseView response
    ) {
        Float value =
                response.getFloat(
                        "max_stack"
                );

        if (value == null
                || !Float.isFinite(value)) {

            throw new IllegalArgumentException(
                    "Max stack size is invalid"
            );
        }

        int maxStack =
                Math.round(value);

        if (maxStack < 1
                || maxStack > 99) {

            throw new IllegalArgumentException(
                    "Max stack size must be between 1 and 99"
            );
        }

        return maxStack;
    }

    private ItemStack buildItem(
            Material material,
            String displayName,
            String lore,
            boolean glint,
            boolean unbreakable,
            int maxStack
    ) {
        /*
         * Dernière protection avant construction.
         * Même si createItem() est modifié plus tard,
         * on ne pourra pas créer cette combinaison invalide.
         */
        if (material.getMaxDurability() > 0
                && maxStack != 1) {

            throw new IllegalArgumentException(
                    "damageable items must have a maximum stack size of 1"
            );
        }

        ItemBuilder builder =
                ItemBuilder.of(
                                material
                        )
                        .name(
                                messageService.render(
                                        displayName
                                )
                        )
                        .glint(
                                glint
                        )
                        .maxStackSize(
                                maxStack
                        );

        if (unbreakable) {
            builder.unbreakable();
        }

        List<Component> loreComponents =
                Arrays.stream(
                                lore.split(
                                        "\\R"
                                )
                        )
                        .filter(
                                line ->
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
                )
                .whenComplete(
                        (storedItem, throwable) ->
                                plugin.getServer()
                                        .getScheduler()
                                        .runTask(
                                                plugin,
                                                () -> {
                                                    if (throwable != null) {
                                                        plugin.getLogger()
                                                                .log(
                                                                        Level.SEVERE,
                                                                        "Failed to create custom item "
                                                                                + id.asString(),
                                                                        throwable
                                                                );

                                                        if (player.isOnline()) {
                                                            player.sendMessage(
                                                                    Component.text(
                                                                            "Unable to create the custom item."
                                                                    )
                                                            );
                                                        }

                                                        return;
                                                    }

                                                    if (!player.isOnline()) {
                                                        return;
                                                    }

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
                                        )
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
                .map(
                        word ->
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
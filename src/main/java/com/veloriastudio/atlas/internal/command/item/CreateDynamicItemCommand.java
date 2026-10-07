package com.veloriastudio.atlas.internal.command.item;

import com.veloriastudio.atlas.api.command.AtlasCommand;
import com.veloriastudio.atlas.api.command.CommandContext;
import com.veloriastudio.atlas.api.command.annotation.DescribeCommand;
import com.veloriastudio.atlas.api.message.LocalizedMessages;
import com.veloriastudio.atlas.internal.item.dialog.CustomItemCreationDialog;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

@DescribeCommand(
        name = "itemeditor",
        description = "Create a stored custom item",
        permission = "atlas.item.create",
        aliases = {"ie"},
        playerOnly = true
)
public final class CreateDynamicItemCommand
        implements AtlasCommand {

    private final CustomItemCreationDialog dialog;
    private final LocalizedMessages messages;

    public CreateDynamicItemCommand(
            CustomItemCreationDialog dialog,
            LocalizedMessages messages
    ) {
        this.dialog = Objects.requireNonNull(
                dialog,
                "dialog cannot be null"
        );

        this.messages = Objects.requireNonNull(
                messages,
                "messages cannot be null"
        );
    }

    @Override
    public void execute(
            CommandContext context
    ) {
        Player player =
                context.player();

        ItemStack hand =
                player.getInventory()
                        .getItemInMainHand();

        if (hand.getType().isAir()) {
            messages.get(
                            player.locale()
                                    .getLanguage()
                    )
                    .send(
                            player,
                            "item.create.must-hold-item"
                    );

            return;
        }

        dialog.open(
                player,
                hand
        );
    }
}
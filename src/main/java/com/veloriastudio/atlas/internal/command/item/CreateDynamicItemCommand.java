package com.veloriastudio.atlas.internal.command.item;

import com.veloriastudio.atlas.api.command.AtlasCommand;
import com.veloriastudio.atlas.api.command.CommandContext;
import com.veloriastudio.atlas.api.command.annotation.DescribeCommand;
import com.veloriastudio.atlas.internal.item.dialog.CustomItemCreationDialog;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

@DescribeCommand(
        name = "atlasitemcreate",
        description = "Create a stored custom item",
        permission = "atlas.item.create",
        playerOnly = true
)
public final class CreateDynamicItemCommand
        implements AtlasCommand {

    private final CustomItemCreationDialog dialog;

    public CreateDynamicItemCommand(
            CustomItemCreationDialog dialog
    ) {
        this.dialog = Objects.requireNonNull(dialog);
    }

    @Override
    public void execute(CommandContext context) {

        Player player = context.player();

        ItemStack hand =
                player.getInventory()
                        .getItemInMainHand();

        if (hand.getType().isAir()) {
            player.sendMessage(
                    "Tu dois tenir un item en main."
            );
            return;
        }

        dialog.open(
                player,
                hand
        );
    }
}
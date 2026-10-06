package com.veloriastudio.atlas.internal.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.veloriastudio.atlas.api.command.AtlasCommand;
import com.veloriastudio.atlas.api.command.CommandArguments;
import com.veloriastudio.atlas.api.command.CommandContext;
import com.veloriastudio.atlas.api.command.annotation.DescribeCommand;
import com.veloriastudio.atlas.api.item.custom.CustomItemRegistry;
import com.veloriastudio.atlas.internal.item.museum.CustomItemMuseum;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;

@DescribeCommand(name = "museum", permission = "atlas.museum", playerOnly = true)
public class MuseumCommand implements AtlasCommand {

    private final CustomItemMuseum museum;
    private final CustomItemRegistry registry;

    public MuseumCommand(CustomItemMuseum museum, CustomItemRegistry registry) {
        this.museum = Objects.requireNonNull(museum);
        this.registry = Objects.requireNonNull(registry);
    }

    @Override
    public void arguments(CommandArguments arguments) {

        arguments.optionalGreedyString("path").suggests(context -> registry.all().stream().map(item -> item.category().path()).distinct().sorted().toList());
    }

    @Override
    public void execute(CommandContext context) throws CommandSyntaxException {

        Player player = context.player();

        Optional<String> path = context.optionalString("path");

        if (path.isEmpty()) {
            museum.open(player);
            return;
        }

        museum.open(player, path.get());
    }
}

package com.veloriastudio.atlas.internal.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.veloriastudio.atlas.api.command.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.Optional;

final class DefaultCommandContext implements CommandContext {

    private final com.mojang.brigadier.context.CommandContext<CommandSourceStack> context;

    public DefaultCommandContext(
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context
    ) {
        this.context = Objects.requireNonNull(
                context,
                "context cannot be null"
        );
    }

    @Override
    public CommandSender sender() {
        return context.getSource().getSender();
    }

    @Override
    public <T> T argument(String name, Class<T> type) {

        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(type, "type cannot be null");

        return context.getArgument(name, type);
    }

    @Override
    public String string(String name) {
        return CommandContext.super.string(name);
    }

    @Override
    public int integer(String name) {
        return CommandContext.super.integer(name);
    }

    @Override
    public double decimal(String name) {
        return CommandContext.super.decimal(name);
    }

    @Override
    public boolean bool(String name) {
        return CommandContext.super.bool(name);
    }

    @Override
    public Player player(String name) throws CommandSyntaxException {
        Objects.requireNonNull(name, "name cannot be null");

        PlayerSelectorArgumentResolver resolver = context.getArgument(
                name,
                PlayerSelectorArgumentResolver.class
        );

        return resolver.resolve(context.getSource()).getFirst();
    }

    @Override
    public Player player() {
        if (sender() instanceof Player player) {
            return player;
        }

        throw new IllegalStateException(
                "command sender is not a player"
        );
    }

    @Override
    public Optional<String> optionalString(String name) {
        try {
            return Optional.of(context.getArgument(name, String.class));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
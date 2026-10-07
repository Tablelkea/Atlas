package com.veloriastudio.atlas.internal.command;

import com.mojang.brigadier.context.CommandContext;
import com.veloriastudio.atlas.api.command.CommandSuggestionContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;

import java.util.Objects;
import java.util.Optional;

final class DefaultCommandSuggestionContext implements CommandSuggestionContext {

    private final CommandContext<CommandSourceStack> context;

    DefaultCommandSuggestionContext(CommandContext<CommandSourceStack> context) {
        this.context = Objects.requireNonNull(context, "context cannot be null");
    }

    @Override
    public CommandSender sender() {
        return context.getSource().getSender();
    }

    @Override
    public <T> Optional<T> argument(String name, Class<T> type) {

        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(type, "type cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        try {
            return Optional.ofNullable(context.getArgument(name, type));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
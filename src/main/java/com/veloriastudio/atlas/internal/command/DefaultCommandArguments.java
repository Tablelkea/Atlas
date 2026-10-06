package com.veloriastudio.atlas.internal.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.veloriastudio.atlas.api.command.CommandArguments;
import com.veloriastudio.atlas.api.command.CommandSuggestionProvider;

import java.util.Objects;

final class DefaultCommandArguments implements CommandArguments {

    private DefaultCommandBuilder current;
    private boolean hasArgument;

    DefaultCommandArguments(DefaultCommandBuilder root) {
        this.current = Objects.requireNonNull(root, "root cannot be null");
    }

    @Override
    public <T> CommandArguments argument(String name, ArgumentType<T> type) {

        current = current.addArgument(name, type);

        hasArgument = true;

        return this;
    }

    @Override
    public CommandArguments suggests(CommandSuggestionProvider provider) {

        Objects.requireNonNull(provider, "provider cannot be null");

        if (!hasArgument) {
            throw new IllegalStateException("suggests() must be called after an argument");
        }

        current.suggestionProvider(provider);

        return this;
    }

    DefaultCommandBuilder terminal() {
        return current;
    }


}
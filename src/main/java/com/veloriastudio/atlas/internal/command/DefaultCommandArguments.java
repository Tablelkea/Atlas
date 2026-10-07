package com.veloriastudio.atlas.internal.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.veloriastudio.atlas.api.command.CommandArguments;
import com.veloriastudio.atlas.api.command.CommandSuggestionProvider;

import java.util.Objects;

final class DefaultCommandArguments
        implements CommandArguments {

    private DefaultCommandBuilder current;

    private boolean hasArgument;
    private boolean terminalArgument;

    private DefaultCommandBuilder optionalExecutionNode;

    DefaultCommandArguments(
            DefaultCommandBuilder root
    ) {
        this.current = Objects.requireNonNull(
                root,
                "root cannot be null"
        );
    }

    @Override
    public <T> CommandArguments argument(
            String name,
            ArgumentType<T> type
    ) {
        return argument(
                name,
                type,
                false
        );
    }

    @Override
    public CommandArguments greedyString(
            String name
    ) {
        return argument(
                name,
                StringArgumentType.greedyString(),
                false
        );
    }

    @Override
    public CommandArguments optionalGreedyString(
            String name
    ) {
        return argument(
                name,
                StringArgumentType.greedyString(),
                true
        );
    }

    @Override
    public CommandArguments suggests(
            CommandSuggestionProvider provider
    ) {
        Objects.requireNonNull(
                provider,
                "provider cannot be null"
        );

        if (!hasArgument) {
            throw new IllegalStateException(
                    "suggests() must be called after an argument"
            );
        }

        current.suggestionProvider(
                provider
        );

        return this;
    }

    private <T> CommandArguments argument(
            String name,
            ArgumentType<T> type,
            boolean optional
    ) {
        Objects.requireNonNull(
                type,
                "type cannot be null"
        );

        if (terminalArgument) {
            throw new IllegalStateException(
                    "cannot add an argument after a greedy argument"
            );
        }

        if (optional) {
            optionalExecutionNode =
                    current;
        }

        current =
                current.addArgument(
                        name,
                        type,
                        optional
                );

        hasArgument = true;

        if (isGreedy(type)) {
            terminalArgument = true;
        }

        return this;
    }

    private boolean isGreedy(
            ArgumentType<?> type
    ) {
        return type instanceof StringArgumentType stringType
                && stringType.getType()
                == StringArgumentType.StringType.GREEDY_PHRASE;
    }

    DefaultCommandBuilder terminal() {
        return current;
    }

    DefaultCommandBuilder optionalExecutionNode() {
        return optionalExecutionNode;
    }
}
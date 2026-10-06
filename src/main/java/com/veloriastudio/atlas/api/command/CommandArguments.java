package com.veloriastudio.atlas.api.command;

import com.mojang.brigadier.arguments.*;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public interface CommandArguments {

    <T> CommandArguments argument(
            String name,
            ArgumentType<T> type
    );

    CommandArguments suggests(
            CommandSuggestionProvider provider
    );

    default CommandArguments suggests(
            String... suggestions
    ) {
        return suggests(
                context -> List.of(suggestions)
        );
    }

    default CommandArguments suggests(
            Collection<String> suggestions
    ) {
        Objects.requireNonNull(
                suggestions,
                "suggestions cannot be null"
        );

        List<String> copy =
                List.copyOf(suggestions);

        return suggests(
                context -> copy
        );
    }

    default CommandArguments integer(String name) {
        return argument(
                name,
                IntegerArgumentType.integer()
        );
    }

    default CommandArguments integer(
            String name,
            int min
    ) {
        return argument(
                name,
                IntegerArgumentType.integer(min)
        );
    }

    default CommandArguments integer(
            String name,
            int min,
            int max
    ) {
        return argument(
                name,
                IntegerArgumentType.integer(min, max)
        );
    }

    default CommandArguments decimal(String name) {
        return argument(
                name,
                DoubleArgumentType.doubleArg()
        );
    }

    default CommandArguments bool(String name) {
        return argument(
                name,
                BoolArgumentType.bool()
        );
    }

    default CommandArguments word(String name) {
        return argument(
                name,
                StringArgumentType.word()
        );
    }

    default CommandArguments string(String name) {
        return argument(
                name,
                StringArgumentType.string()
        );
    }

    default CommandArguments greedyString(String name) {
        return argument(
                name,
                StringArgumentType.greedyString()
        );
    }

    default CommandArguments player(String name) {
        return argument(
                name,
                ArgumentTypes.player()
        );
    }

    CommandArguments optionalGreedyString(String name);
}
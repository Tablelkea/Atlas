package com.veloriastudio.atlas.internal.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.veloriastudio.atlas.api.command.CommandSuggestionProvider;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

final class DefaultCommandBuilder {

    private final Map<String, DefaultCommandBuilder> subcommands =
            new LinkedHashMap<>();

    private final Map<String, DefaultArgumentNode> arguments =
            new LinkedHashMap<>();

    private final Set<String> permissions =
            new LinkedHashSet<>();

    private InternalCommandExecutor executor;

    private boolean playerOnly;
    private boolean hideWithoutPermission = true;

    private String description = "";

    private CommandSuggestionProvider suggestionProvider;

    void executor(InternalCommandExecutor executor) {
        this.executor = Objects.requireNonNull(
                executor,
                "executor cannot be null"
        );
    }

    InternalCommandExecutor executor() {
        return executor;
    }

    void addPermission(String permission) {

        Objects.requireNonNull(
                permission,
                "permission cannot be null"
        );

        if (permission.isBlank()) {
            throw new IllegalArgumentException(
                    "permission cannot be blank"
            );
        }

        permissions.add(permission);
    }

    Set<String> permissions() {
        return Collections.unmodifiableSet(
                new LinkedHashSet<>(permissions)
        );
    }

    String description() {
        return description;
    }

    void description(String description) {
        this.description = Objects.requireNonNull(
                description,
                "description cannot be null"
        );
    }

    Map<String, DefaultCommandBuilder> subcommands() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(subcommands)
        );
    }

    Map<String, DefaultArgumentNode> arguments() {
        return Collections.unmodifiableMap(
                new LinkedHashMap<>(arguments)
        );
    }

    DefaultCommandBuilder addArgument(
            String name,
            ArgumentType<?> type
    ) {

        validateChild(name);

        Objects.requireNonNull(
                type,
                "type cannot be null"
        );

        DefaultCommandBuilder child =
                new DefaultCommandBuilder();

        inheritTo(child);

        arguments.put(
                name,
                new DefaultArgumentNode(
                        name,
                        type,
                        child
                )
        );

        return child;
    }

    DefaultCommandBuilder addSubcommand(String name) {

        validateChild(name);

        DefaultCommandBuilder child =
                new DefaultCommandBuilder();

        inheritTo(child);

        subcommands.put(name, child);

        return child;
    }

    private void validateChild(String name) {

        Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "name cannot be blank"
            );
        }

        if (subcommands.containsKey(name)
                || arguments.containsKey(name)) {

            throw new IllegalStateException(
                    "a command child already exists: " + name
            );
        }
    }

    private void inheritTo(
            DefaultCommandBuilder child
    ) {

        child.playerOnly = this.playerOnly;
        child.permissions.addAll(this.permissions);
        child.hideWithoutPermission =
                this.hideWithoutPermission;
    }

    boolean playerOnly() {
        return playerOnly;
    }

    void playerOnly(boolean playerOnly) {
        this.playerOnly = playerOnly;
    }

    boolean hideWithoutPermission() {
        return hideWithoutPermission;
    }

    void hideWithoutPermission(
            boolean hideWithoutPermission
    ) {
        this.hideWithoutPermission =
                hideWithoutPermission;
    }

    CommandSuggestionProvider suggestionProvider() {
        return suggestionProvider;
    }

    void suggestionProvider(
            CommandSuggestionProvider provider
    ) {

        Objects.requireNonNull(
                provider,
                "provider cannot be null"
        );

        if (suggestionProvider != null) {
            throw new IllegalStateException(
                    "suggestions are already configured for this argument"
            );
        }

        this.suggestionProvider = provider;
    }
}
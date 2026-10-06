package com.veloriastudio.atlas.internal.command;

import com.veloriastudio.atlas.api.command.AtlasCommand;
import com.veloriastudio.atlas.api.command.AtlasSubcommand;
import com.veloriastudio.atlas.api.command.annotation.DescribeCommand;
import com.veloriastudio.atlas.api.command.annotation.DescribeSubcommand;
import com.veloriastudio.atlas.api.command.annotation.Subcommands;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

final class CommandTreeFactory {

    CommandTree create(AtlasCommand command) {

        Objects.requireNonNull(
                command,
                "command cannot be null"
        );

        DescribeCommand description =
                command.getClass()
                        .getAnnotation(DescribeCommand.class);

        if (description == null) {
            throw new IllegalArgumentException(
                    command.getClass().getName()
                            + " must be annotated with @DescribeCommand"
            );
        }

        DefaultCommandBuilder root =
                new DefaultCommandBuilder();

        root.description(
                description.description()
        );

        root.playerOnly(
                description.playerOnly()
        );

        root.hideWithoutPermission(
                description.hideWithoutPermission()
        );

        if (!description.permission().isBlank()) {
            root.addPermission(
                    description.permission()
            );
        }

        DefaultCommandArguments arguments =
                new DefaultCommandArguments(root);

        command.arguments(arguments);

        arguments.terminal().executor(
                command::execute
        );

        configureSubcommands(
                command.getClass(),
                root
        );

        return new CommandTree(
                description.name(),
                description.description(),
                List.copyOf(
                        Arrays.asList(
                                description.aliases()
                        )
                ),
                description.autoHelp(),
                root
        );
    }

    private void configureSubcommands(
            Class<?> ownerType,
            DefaultCommandBuilder parent
    ) {

        Subcommands annotation =
                ownerType.getAnnotation(
                        Subcommands.class
                );

        if (annotation == null) {
            return;
        }

        for (Class<? extends AtlasSubcommand> type
                : annotation.value()) {

            registerSubcommand(
                    parent,
                    type
            );
        }
    }

    private void registerSubcommand(
            DefaultCommandBuilder parent,
            Class<? extends AtlasSubcommand> type
    ) {

        DescribeSubcommand description =
                type.getAnnotation(
                        DescribeSubcommand.class
                );

        if (description == null) {
            throw new IllegalArgumentException(
                    type.getName()
                            + " must be annotated with @DescribeSubcommand"
            );
        }

        AtlasSubcommand subcommand =
                instantiate(type);

        DefaultCommandBuilder child =
                parent.addSubcommand(
                        description.name()
                );

        child.description(
                description.description()
        );

        child.playerOnly(
                parent.playerOnly()
                        || description.playerOnly()
        );

        child.hideWithoutPermission(
                description.hideWithoutPermission()
        );

        if (!description.permission().isBlank()) {
            child.addPermission(
                    description.permission()
            );
        }

        /*
         * Important :
         * permissions / playerOnly doivent être définis
         * AVANT de créer les arguments.
         */
        DefaultCommandArguments arguments =
                new DefaultCommandArguments(child);

        subcommand.arguments(arguments);

        arguments.terminal().executor(
                subcommand::execute
        );

        configureSubcommands(
                type,
                child
        );
    }

    private AtlasSubcommand instantiate(
            Class<? extends AtlasSubcommand> type
    ) {

        try {
            return type.getDeclaredConstructor()
                    .newInstance();

        } catch (ReflectiveOperationException exception) {

            throw new IllegalStateException(
                    "Cannot instantiate subcommand "
                            + type.getName()
                            + ". A public no-argument constructor is required.",
                    exception
            );
        }
    }
}
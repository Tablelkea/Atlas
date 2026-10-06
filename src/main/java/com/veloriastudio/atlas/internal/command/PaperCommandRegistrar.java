package com.veloriastudio.atlas.internal.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.veloriastudio.atlas.api.command.AtlasCommand;
import com.veloriastudio.atlas.api.command.CommandSuggestionProvider;
import com.veloriastudio.atlas.api.message.LocalizedMessages;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

public final class PaperCommandRegistrar {

    private final CommandTreeFactory treeFactory;
    private final CommandHelpService helpService;
    private final CommandExecutionService executionService;

    public PaperCommandRegistrar(LocalizedMessages localizedMessages, Logger logger) {
        Objects.requireNonNull(localizedMessages, "localizedMessages cannot be null");

        Objects.requireNonNull(logger, "logger cannot be null");

        this.treeFactory = new CommandTreeFactory();
        this.helpService = new CommandHelpService(localizedMessages);
        this.executionService = new CommandExecutionService(localizedMessages, logger);
    }

    public void register(Commands commands, AtlasCommand command) {

        Objects.requireNonNull(commands, "commands cannot be null");

        Objects.requireNonNull(command, "command cannot be null");

        CommandTree tree = treeFactory.create(command);

        DefaultCommandBuilder root = tree.root();

        if (tree.autoHelp()) {
            helpService.install(tree.name(), root);
        }

        LiteralArgumentBuilder<CommandSourceStack> brigadierRoot = Commands.literal(tree.name());

        configureNode(brigadierRoot, root);

        commands.register(brigadierRoot.build(), tree.description(), tree.aliases());
    }

    private void configureNode(ArgumentBuilder<CommandSourceStack, ?> node, DefaultCommandBuilder builder) {

        if (builder.hideWithoutPermission() && !builder.permissions().isEmpty()) {

            node.requires(source -> builder.permissions().stream().allMatch(source.getSender()::hasPermission));
        }

        if (builder.executor() != null) {
            node.executes(context -> executionService.execute(builder, context));
        }

        for (Map.Entry<String, DefaultCommandBuilder> entry : builder.subcommands().entrySet()) {

            node.then(buildLiteralNode(entry.getKey(), entry.getValue()));
        }

        for (DefaultArgumentNode argument : builder.arguments().values()) {

            node.then(buildArgumentNode(argument));
        }
    }

    private LiteralArgumentBuilder<CommandSourceStack> buildLiteralNode(String name, DefaultCommandBuilder builder) {

        LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(name);

        configureNode(node, builder);

        return node;
    }

    private RequiredArgumentBuilder<CommandSourceStack, ?> buildArgumentNode(DefaultArgumentNode argument) {

        RequiredArgumentBuilder<CommandSourceStack, ?> node = Commands.argument(argument.name(), argument.type());

        CommandSuggestionProvider provider = argument.builder().suggestionProvider();

        if (provider != null) {

            node.suggests((context, suggestionsBuilder) -> {

                DefaultCommandSuggestionContext suggestionContext = new DefaultCommandSuggestionContext(context);

                String remaining = suggestionsBuilder.getRemainingLowerCase();

                Collection<String> suggestions = provider.suggest(suggestionContext);

                if (suggestions != null) {
                    suggestions.stream().filter(Objects::nonNull).filter(suggestion -> suggestion.toLowerCase(Locale.ROOT).startsWith(remaining)).forEach(suggestionsBuilder::suggest);
                }

                return suggestionsBuilder.buildFuture();
            });
        }

        configureNode(node, argument.builder());

        return node;
    }
}
package com.veloriastudio.atlas.internal.command;

import com.veloriastudio.atlas.api.command.CommandContext;
import com.veloriastudio.atlas.api.message.LocalizedMessages;
import com.veloriastudio.atlas.api.message.MessageBundle;
import com.veloriastudio.atlas.api.message.MessagePlaceholders;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Objects;

final class CommandHelpService {

    private final LocalizedMessages localizedMessages;

    CommandHelpService(
            LocalizedMessages localizedMessages
    ) {
        this.localizedMessages = Objects.requireNonNull(
                localizedMessages,
                "localizedMessages cannot be null"
        );
    }

    void install(
            String commandName,
            DefaultCommandBuilder root
    ) {

        Objects.requireNonNull(
                commandName,
                "commandName cannot be null"
        );

        Objects.requireNonNull(
                root,
                "root cannot be null"
        );

        if (root.subcommands().containsKey("help")) {
            return;
        }

        DefaultCommandBuilder help =
                root.addSubcommand("help");

        help.description(
                "Show command help"
        );

        help.executor(context ->
                sendHelp(
                        context,
                        commandName,
                        root
                )
        );
    }

    private void sendHelp(
            CommandContext context,
            String commandName,
            DefaultCommandBuilder root
    ) {

        CommandSender sender =
                context.sender();

        MessageBundle messages =
                messagesFor(sender);

        messages.send(
                sender,
                "command.help.header",
                MessagePlaceholders.text(
                        "command",
                        commandName
                )
        );

        boolean found = false;

        for (Map.Entry<String, DefaultCommandBuilder> entry
                : root.subcommands().entrySet()) {

            String name =
                    entry.getKey();

            DefaultCommandBuilder child =
                    entry.getValue();

            if (name.equals("help")) {
                continue;
            }

            if (!isVisible(sender, child)) {
                continue;
            }

            String usage =
                    commandName
                            + " "
                            + name
                            + argumentUsage(child);

            messages.send(
                    sender,
                    "command.help.entry",
                    MessagePlaceholders.text(
                            "usage",
                            usage
                    ),
                    MessagePlaceholders.text(
                            "description",
                            child.description()
                    )
            );

            found = true;
        }

        if (!found) {
            messages.send(
                    sender,
                    "command.help.empty"
            );
        }
    }

    private boolean isVisible(
            CommandSender sender,
            DefaultCommandBuilder builder
    ) {

        boolean hasPermissions =
                builder.permissions()
                        .stream()
                        .allMatch(
                                sender::hasPermission
                        );

        return hasPermissions
                || !builder.hideWithoutPermission();
    }

    private String argumentUsage(
            DefaultCommandBuilder builder
    ) {

        StringBuilder usage =
                new StringBuilder();

        DefaultCommandBuilder current =
                builder;

        while (current.arguments().size() == 1) {

            DefaultArgumentNode argument =
                    current.arguments()
                            .values()
                            .iterator()
                            .next();

            usage.append(" <")
                    .append(argument.name())
                    .append(">");

            current =
                    argument.builder();
        }

        return usage.toString();
    }

    private MessageBundle messagesFor(
            CommandSender sender
    ) {

        if (sender instanceof Player player) {
            return localizedMessages.get(
                    player.locale().getLanguage()
            );
        }

        return localizedMessages.getDefault();
    }
}
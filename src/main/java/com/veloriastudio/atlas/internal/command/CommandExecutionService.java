package com.veloriastudio.atlas.internal.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.veloriastudio.atlas.api.message.CommonMessages;
import com.veloriastudio.atlas.api.message.LocalizedMessages;
import com.veloriastudio.atlas.api.message.MessageBundle;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

final class CommandExecutionService {

    private final LocalizedMessages localizedMessages;
    private final Logger logger;

    CommandExecutionService(
            LocalizedMessages localizedMessages,
            Logger logger
    ) {
        this.localizedMessages = Objects.requireNonNull(
                localizedMessages,
                "localizedMessages cannot be null"
        );

        this.logger = Objects.requireNonNull(
                logger,
                "logger cannot be null"
        );
    }

    int execute(
            DefaultCommandBuilder builder,
            com.mojang.brigadier.context.CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {

        Objects.requireNonNull(
                builder,
                "builder cannot be null"
        );

        Objects.requireNonNull(
                context,
                "context cannot be null"
        );

        DefaultCommandContext atlasContext =
                new DefaultCommandContext(context);

        CommandSender sender =
                atlasContext.sender();

        if (!hasPermissions(sender, builder)) {

            messagesFor(sender).send(
                    sender,
                    CommonMessages.NO_PERMISSION
            );

            return 0;
        }

        if (builder.playerOnly()
                && !(sender instanceof Player)) {

            messagesFor(sender).send(
                    sender,
                    CommonMessages.PLAYER_ONLY
            );

            return 0;
        }

        try {

            builder.executor()
                    .execute(atlasContext);

            return Command.SINGLE_SUCCESS;

        } catch (CommandSyntaxException exception) {

            throw exception;

        } catch (Exception exception) {

            logger.log(
                    Level.SEVERE,
                    "Failed to execute command for sender "
                            + sender.getName(),
                    exception
            );

            messagesFor(sender).send(
                    sender,
                    CommonMessages.ERROR
            );

            return 0;
        }
    }

    private boolean hasPermissions(
            CommandSender sender,
            DefaultCommandBuilder builder
    ) {

        return builder.permissions()
                .stream()
                .allMatch(sender::hasPermission);
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
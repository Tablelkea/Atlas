package com.veloriastudio.atlas.internal.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.veloriastudio.atlas.api.command.CommandContext;

@FunctionalInterface
interface InternalCommandExecutor {

    void execute(CommandContext context)
        throws CommandSyntaxException;

}

package com.veloriastudio.atlas.api.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

public interface AtlasCommand {

    default void arguments(CommandArguments arguments) {
    }

    void execute(CommandContext context)
            throws CommandSyntaxException;
}

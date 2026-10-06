package com.veloriastudio.atlas.internal.command;

import com.mojang.brigadier.arguments.ArgumentType;

record DefaultArgumentNode(
        String name,
        ArgumentType<?> type,
        DefaultCommandBuilder builder
) {
}

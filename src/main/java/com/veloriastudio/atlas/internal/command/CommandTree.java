package com.veloriastudio.atlas.internal.command;

import java.util.List;

record CommandTree(
        String name,
        String description,
        List<String> aliases,
        boolean autoHelp,
        DefaultCommandBuilder root
) {
}
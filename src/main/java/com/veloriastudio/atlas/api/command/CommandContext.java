package com.veloriastudio.atlas.api.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public interface CommandContext {

    CommandSender sender();

    <T> T argument(String name, Class<T> type);

    default String string(String name) {
        return argument(name, String.class);
    }

    default int integer(String name) {
        return argument(name, Integer.class);
    }

    default double decimal(String name) {
        return argument(name, Double.class);
    }

    default boolean bool(String name) {
        return argument(name, Boolean.class);
    }

    Player player(String name) throws CommandSyntaxException;

    Player player();
}

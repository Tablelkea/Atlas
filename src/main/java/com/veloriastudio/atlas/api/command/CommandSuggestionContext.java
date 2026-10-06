package com.veloriastudio.atlas.api.command;

import org.bukkit.command.CommandSender;

import java.util.Optional;

public interface CommandSuggestionContext {

    CommandSender sender();

    <T> Optional<T> argument(
            String name,
            Class<T> type
    );
}
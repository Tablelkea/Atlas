package com.veloriastudio.atlas.api.command;

import java.util.Collection;

@FunctionalInterface
public interface CommandSuggestionProvider {

    Collection<String> suggest(
            CommandSuggestionContext context
    );
}
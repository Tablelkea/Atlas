package com.veloriastudio.atlas.api.command;

import org.bukkit.plugin.Plugin;

public interface CommandService {

    void register(Plugin owner, AtlasCommand command);

}

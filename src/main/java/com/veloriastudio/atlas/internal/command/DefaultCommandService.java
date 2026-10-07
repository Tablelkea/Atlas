package com.veloriastudio.atlas.internal.command;

import com.veloriastudio.atlas.api.command.AtlasCommand;
import com.veloriastudio.atlas.api.command.CommandService;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DefaultCommandService implements CommandService {

    private final PaperCommandRegistrar registrar;

    public DefaultCommandService(PaperCommandRegistrar registrar) {
        this.registrar = Objects.requireNonNull(registrar, "registrar cannot be null");
    }

    @Override
    public void register(Plugin owner, AtlasCommand command) {

        Objects.requireNonNull(owner, "owner cannot be null");
        Objects.requireNonNull(command, "command cannot be null");

        owner.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> registrar.register(event.registrar(), command));

    }


}

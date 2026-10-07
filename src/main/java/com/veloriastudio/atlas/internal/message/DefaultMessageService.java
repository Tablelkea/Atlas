package com.veloriastudio.atlas.internal.message;

import com.veloriastudio.atlas.api.message.MessageService;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Objects;

public final class DefaultMessageService implements MessageService {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public Component render(String message, TagResolver... resolvers) {

        Objects.requireNonNull(message, "message cannot be null");
        Objects.requireNonNull(resolvers, "resolvers cannot be null");

        return miniMessage.deserialize(message, resolvers);
    }

    @Override
    public void send(Audience audience, String message, TagResolver... resolvers) {

        Objects.requireNonNull(audience, "audience cannot be null");

        audience.sendMessage(render(message, resolvers));
    }
}

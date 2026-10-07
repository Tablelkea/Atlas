package com.veloriastudio.atlas.api.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public interface MessageService {

    Component render(String message, TagResolver... resolvers);

    void send(Audience audience, String message, TagResolver... resolvers);
}

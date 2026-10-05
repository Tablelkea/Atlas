package com.veloriastudio.atlas.api.message;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;


public interface MessageBundle {

    Component render(String path, TagResolver... resolvers);

    void send(Audience audience, String path, TagResolver... resolvers);

}

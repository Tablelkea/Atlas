package com.veloriastudio.atlas.internal.message;

import com.veloriastudio.atlas.api.config.Config;
import com.veloriastudio.atlas.api.message.MessageBundle;
import com.veloriastudio.atlas.api.message.MessageService;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Objects;

public final class DefaultMessageBundle implements MessageBundle {

    private final Config config;
    private final MessageService messageService;

    public DefaultMessageBundle(
            Config config,
            MessageService messageService
    ) {

        this.config = Objects.requireNonNull(config, "config cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "messageService cannot be null");
    }

    @Override
    public Component render(String path, TagResolver... resolvers) {

        Objects.requireNonNull(path, "path cannot be null");
        Objects.requireNonNull(resolvers, "resolvers cannot be null");

        if (path.isBlank()) {
            throw new IllegalArgumentException("path cannot be blank");
        }

        String message = config.getString(path)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "message path does not exist: " + path
                        )
                );
        String prefix = config.getString("prefix").orElse("");

        Component prefixComponent = messageService.render(prefix);

        TagResolver prefixResolver = Placeholder.component("prefix", prefixComponent);

        TagResolver[] allResolvers = new TagResolver[resolvers.length + 1];

        allResolvers[0] = prefixResolver;

        System.arraycopy(
                resolvers,
                0,
                allResolvers,
                1,
                resolvers.length
        );

        return messageService.render(message, allResolvers);
    }

    @Override
    public void send(Audience audience, String path, TagResolver... resolvers) {

        Objects.requireNonNull(audience, "audience cannot be null");

        audience.sendMessage(render(path, resolvers));
    }
}

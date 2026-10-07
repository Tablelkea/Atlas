package com.veloriastudio.atlas.api.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.Objects;

public final class MessagePlaceholders {

    private MessagePlaceholders() {
    }

    public static TagResolver text(String name, String value) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(value, "value cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        return Placeholder.unparsed(name, value);
    }

    public static TagResolver component(String name, Component value) {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(value, "value cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        return Placeholder.component(name, value);
    }

}

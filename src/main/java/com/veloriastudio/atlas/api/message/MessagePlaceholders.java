package com.veloriastudio.atlas.api.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

public class MessagePlaceholders {

    private MessagePlaceholders() {
    }

    public static TagResolver text(String name, String value) {
        return Placeholder.unparsed(name, value);
    }

    public static TagResolver component(String name, Component value) {
        return Placeholder.component(name, value);
    }

}

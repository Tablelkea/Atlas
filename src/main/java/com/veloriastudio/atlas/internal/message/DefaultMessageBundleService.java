package com.veloriastudio.atlas.internal.message;

import com.veloriastudio.atlas.api.config.Config;
import com.veloriastudio.atlas.api.config.ConfigService;
import com.veloriastudio.atlas.api.message.MessageBundle;
import com.veloriastudio.atlas.api.message.MessageBundleService;
import com.veloriastudio.atlas.api.message.MessageService;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultMessageBundleService implements MessageBundleService {

    private final ConfigService configService;
    private final MessageService messageService;
    private final Map<String, MessageBundle> bundles = new ConcurrentHashMap<>();

    public DefaultMessageBundleService(
            ConfigService configService,
            MessageService messageService
    ) {

        this.configService = Objects.requireNonNull(configService, "configService cannot be null");
        this.messageService = Objects.requireNonNull(messageService, "messageService cannot be null");
    }

    @Override
    public MessageBundle load(String configName) {

        Objects.requireNonNull(configName, "configName cannot be null");

        if (configName.isBlank()) {
            throw new IllegalArgumentException("configName cannot be blank");
        }

        return bundles.computeIfAbsent(
                configName, name -> {
                    Config config = configService.loadResource(name);
                    return new DefaultMessageBundle(config, messageService);
                }
        );
    }
}

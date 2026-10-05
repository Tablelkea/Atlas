package com.veloriastudio.atlas.internal.message;

import com.veloriastudio.atlas.api.message.LocalizedMessages;
import com.veloriastudio.atlas.api.message.MessageBundle;
import com.veloriastudio.atlas.api.message.MessageBundleService;

import java.util.Map;
import java.util.Objects;

public final class DefaultLocalizedMessages implements LocalizedMessages {

    private final MessageBundleService bundleService;
    private final String defaultLocale;
    private final Map<String, String> localeFiles;

    public DefaultLocalizedMessages(
            MessageBundleService bundleService,
            String defaultLocale,
            Map<String, String> localeFiles
    ) {

        this.bundleService = Objects.requireNonNull(bundleService, "bundleService cannot be null");
        this.defaultLocale = Objects.requireNonNull(defaultLocale, "defaultLocale cannot be null");
        Objects.requireNonNull(localeFiles, "localeFiles cannot be null");

        if (defaultLocale.isBlank()) {
            throw new IllegalArgumentException("defaultLocale cannot be blank");
        }

        this.localeFiles = Map.copyOf(localeFiles);


        if (!this.localeFiles.containsKey(defaultLocale)) {
            throw new IllegalArgumentException(
                    "localeFiles must contain defaultLocale"
            );
        }

    }

    @Override
    public MessageBundle get(String locale) {

        Objects.requireNonNull(locale, "locale cannot be null");

        String configName = localeFiles.get(locale);

        if (configName == null) {
            configName = localeFiles.get(defaultLocale);
        }

        return bundleService.load(configName);
    }

    @Override
    public MessageBundle getDefault() {
        return bundleService.load(localeFiles.get(defaultLocale));
    }
}

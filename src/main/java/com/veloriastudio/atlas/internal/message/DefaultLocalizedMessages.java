package com.veloriastudio.atlas.internal.message;

import com.veloriastudio.atlas.api.message.LocalizedMessages;
import com.veloriastudio.atlas.api.message.MessageBundle;
import com.veloriastudio.atlas.api.message.MessageBundleService;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public final class DefaultLocalizedMessages
        implements LocalizedMessages {

    private final MessageBundleService bundleService;
    private final String defaultLocale;
    private final Map<String, String> localeFiles;

    public DefaultLocalizedMessages(
            MessageBundleService bundleService,
            String defaultLocale,
            Map<String, String> localeFiles
    ) {
        this.bundleService = Objects.requireNonNull(
                bundleService,
                "bundleService cannot be null"
        );

        Objects.requireNonNull(
                defaultLocale,
                "defaultLocale cannot be null"
        );

        Objects.requireNonNull(
                localeFiles,
                "localeFiles cannot be null"
        );

        if (defaultLocale.isBlank()) {
            throw new IllegalArgumentException(
                    "defaultLocale cannot be blank"
            );
        }

        this.defaultLocale =
                normalizeLocale(defaultLocale);

        Map<String, String> normalizedFiles =
                new LinkedHashMap<>();

        for (Map.Entry<String, String> entry
                : localeFiles.entrySet()) {

            String locale =
                    Objects.requireNonNull(
                            entry.getKey(),
                            "locale cannot be null"
                    );

            String configName =
                    Objects.requireNonNull(
                            entry.getValue(),
                            "configName cannot be null"
                    );

            if (locale.isBlank()) {
                throw new IllegalArgumentException(
                        "locale cannot be blank"
                );
            }

            if (configName.isBlank()) {
                throw new IllegalArgumentException(
                        "configName cannot be blank"
                );
            }

            String normalized =
                    normalizeLocale(locale);

            if (normalizedFiles.putIfAbsent(
                    normalized,
                    configName
            ) != null) {
                throw new IllegalArgumentException(
                        "duplicate locale: "
                                + normalized
                );
            }
        }

        this.localeFiles =
                Map.copyOf(normalizedFiles);

        if (!this.localeFiles.containsKey(
                this.defaultLocale
        )) {
            throw new IllegalArgumentException(
                    "localeFiles must contain defaultLocale"
            );
        }
    }

    @Override
    public MessageBundle get(
            String locale
    ) {
        Objects.requireNonNull(
                locale,
                "locale cannot be null"
        );

        if (locale.isBlank()) {
            return getDefault();
        }

        String normalized =
                normalizeLocale(locale);

        String configName =
                localeFiles.get(normalized);

        if (configName == null) {
            String language =
                    Locale.forLanguageTag(
                                    normalized
                            )
                            .getLanguage();

            if (!language.isBlank()) {
                configName =
                        localeFiles.get(language);
            }
        }

        if (configName == null) {
            configName =
                    localeFiles.get(defaultLocale);
        }

        return bundleService.load(
                configName
        );
    }

    @Override
    public MessageBundle getDefault() {
        return bundleService.load(
                localeFiles.get(defaultLocale)
        );
    }

    private String normalizeLocale(
            String locale
    ) {
        return locale
                .trim()
                .replace('_', '-')
                .toLowerCase(Locale.ROOT);
    }
}
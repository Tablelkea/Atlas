package com.veloriastudio.atlas.api.pdc;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public final class PdcKey<T> {

    private final NamespacedKey key;
    private final PersistentDataType<?, T> type;

    private PdcKey(
            NamespacedKey key,
            PersistentDataType<?, T> type
    ) {
        this.key = Objects.requireNonNull(
                key,
                "key cannot be null"
        );

        this.type = Objects.requireNonNull(
                type,
                "type cannot be null"
        );
    }

    public static <P, T> PdcKey<T> of(
            Plugin plugin,
            String name,
            PersistentDataType<P, T> type
    ) {

        Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );

        Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "name cannot be blank"
            );
        }

        return new PdcKey<>(
                new NamespacedKey(plugin, name),
                type
        );
    }

    public static PdcKey<String> string(
            Plugin plugin,
            String name
    ) {
        return of(
                plugin,
                name,
                PersistentDataType.STRING
        );
    }

    public static PdcKey<Integer> integer(
            Plugin plugin,
            String name
    ) {
        return of(
                plugin,
                name,
                PersistentDataType.INTEGER
        );
    }

    public static PdcKey<Long> longKey(
            Plugin plugin,
            String name
    ) {
        return of(
                plugin,
                name,
                PersistentDataType.LONG
        );
    }

    public static PdcKey<Double> decimal(
            Plugin plugin,
            String name
    ) {
        return of(
                plugin,
                name,
                PersistentDataType.DOUBLE
        );
    }

    public static PdcKey<Boolean> bool(
            Plugin plugin,
            String name
    ) {
        return of(
                plugin,
                name,
                PersistentDataType.BOOLEAN
        );
    }

    public NamespacedKey key() {
        return key;
    }

    public PersistentDataType<?, T> type() {
        return type;
    }
}
package com.veloriastudio.atlas.internal.data;

import com.veloriastudio.atlas.api.data.DataKeyId;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class PlayerDataKeyStore {

    private final ConcurrentMap<DataKeyId, DefaultPlayerDataKey<?>> keys =
            new ConcurrentHashMap<>();

    void put(
            DefaultPlayerDataKey<?> key
    ) {
        Objects.requireNonNull(
                key,
                "key cannot be null"
        );

        DefaultPlayerDataKey<?> existing =
                keys.putIfAbsent(
                        key.id(),
                        key
                );

        if (existing != null
                && existing != key) {

            throw new IllegalStateException(
                    "player data key already registered: "
                            + key.id()
            );
        }
    }

    Optional<DefaultPlayerDataKey<?>> find(
            DataKeyId id
    ) {
        Objects.requireNonNull(
                id,
                "id cannot be null"
        );

        return Optional.ofNullable(
                keys.get(id)
        );
    }
}
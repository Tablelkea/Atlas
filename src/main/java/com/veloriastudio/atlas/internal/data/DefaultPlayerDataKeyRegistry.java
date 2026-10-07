package com.veloriastudio.atlas.internal.data;

import com.veloriastudio.atlas.api.data.DataKeyId;
import com.veloriastudio.atlas.api.data.DataPersistence;
import com.veloriastudio.atlas.api.data.PlayerDataKey;
import com.veloriastudio.atlas.api.data.PlayerDataKeyRegistry;
import com.veloriastudio.atlas.api.data.codec.DataCodec;

import java.util.Objects;

public final class DefaultPlayerDataKeyRegistry
        implements PlayerDataKeyRegistry {

    private final String namespace;
    private final PlayerDataKeyStore keyStore;

    public DefaultPlayerDataKeyRegistry(
            String namespace,
            PlayerDataKeyStore keyStore
    ) {
        this.namespace = Objects.requireNonNull(
                namespace,
                "namespace cannot be null"
        );

        this.keyStore = Objects.requireNonNull(
                keyStore,
                "keyStore cannot be null"
        );

        /*
         * Valide immédiatement le namespace plutôt
         * que d'attendre le premier register(...).
         */
        new DataKeyId(
                namespace,
                "validation"
        );
    }

    @Override
    public <T> PlayerDataKey<T> registerTransient(
            String name,
            Class<T> type
    ) {
        Objects.requireNonNull(
                type,
                "type cannot be null"
        );

        return register(
                name,
                type,
                DataPersistence.TRANSIENT,
                null
        );
    }

    @Override
    public <T> PlayerDataKey<T> registerPersistent(
            String name,
            Class<T> type,
            DataCodec<T> codec
    ) {
        Objects.requireNonNull(
                type,
                "type cannot be null"
        );

        Objects.requireNonNull(
                codec,
                "codec cannot be null"
        );

        return register(
                name,
                type,
                DataPersistence.PERSISTENT,
                codec
        );
    }

    private <T> PlayerDataKey<T> register(
            String name,
            Class<T> type,
            DataPersistence persistence,
            DataCodec<T> codec
    ) {
        Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        DataKeyId id =
                new DataKeyId(
                        namespace,
                        name
                );

        /*
         * Plusieurs registries peuvent partager le même
         * store. Le couple find + put doit donc rester
         * atomique.
         */
        synchronized (keyStore) {
            DefaultPlayerDataKey<?> existing =
                    keyStore.find(id)
                            .orElse(null);

            if (existing != null) {
                return validateExisting(
                        existing,
                        type,
                        persistence,
                        codec
                );
            }

            DefaultPlayerDataKey<T> created =
                    new DefaultPlayerDataKey<>(
                            id,
                            type,
                            persistence,
                            codec
                    );

            keyStore.put(
                    created
            );

            return created;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> PlayerDataKey<T> validateExisting(
            DefaultPlayerDataKey<?> existing,
            Class<T> type,
            DataPersistence persistence,
            DataCodec<T> codec
    ) {
        if (existing.type() != type) {
            throw new IllegalStateException(
                    "player data key "
                            + existing.id()
                            + " is already registered with type "
                            + existing.type().getName()
            );
        }

        if (existing.persistence() != persistence) {
            throw new IllegalStateException(
                    "player data key "
                            + existing.id()
                            + " is already registered as "
                            + existing.persistence()
            );
        }

        if (persistence == DataPersistence.PERSISTENT
                && existing.codec() != codec) {

            throw new IllegalStateException(
                    "player data key "
                            + existing.id()
                            + " is already registered with another codec"
            );
        }

        return (PlayerDataKey<T>) existing;
    }
}
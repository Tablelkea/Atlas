package com.veloriastudio.atlas.internal.data;

import com.veloriastudio.atlas.api.data.DataKeyId;
import com.veloriastudio.atlas.api.data.DataPersistence;
import com.veloriastudio.atlas.api.data.PlayerDataKey;
import com.veloriastudio.atlas.api.data.PlayerDataKeyRegistry;
import com.veloriastudio.atlas.api.data.codec.DataCodec;

import java.util.Objects;

public class DefaultPlayerDataKeyRegistry implements PlayerDataKeyRegistry {

    private final String namespace;
    private final PlayerDataKeyStore keyStore;

    public DefaultPlayerDataKeyRegistry(
            String namespace,
            PlayerDataKeyStore keyStore
    ) {

        this.namespace = Objects.requireNonNull(namespace, "namespace cannot be null");
        this.keyStore = Objects.requireNonNull(keyStore, "keyStore cannot be null");

    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> PlayerDataKey<T> registerTransient(String name, Class<T> type) {

        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(type, "type cannot be null");

        DataKeyId key = new DataKeyId(namespace, name);

        DefaultPlayerDataKey<?> existingKey = keyStore.find(key).orElse(null);

        if (existingKey == null) {
            DefaultPlayerDataKey<T> createdKey = new DefaultPlayerDataKey<>(key, type, DataPersistence.TRANSIENT, null);
            keyStore.put(createdKey);
            return createdKey;
        }

        if (existingKey.type() != type) {
            throw new IllegalStateException("this key already exist with another type: " + type.getSimpleName());
        }

        if (existingKey.persistence() != DataPersistence.TRANSIENT) {
            throw new IllegalStateException("this key already exist with another persistence: " + DataPersistence.PERSISTENT.name());
        }

        return (DefaultPlayerDataKey<T>) existingKey;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> PlayerDataKey<T> registerPersistent(String name, Class<T> type, DataCodec<T> codec) {

        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(codec, "codec cannot be null");

        DataKeyId key = new DataKeyId(namespace, name);

        DefaultPlayerDataKey<?> existingKey = keyStore.find(key).orElse(null);

        if (existingKey == null) {
            DefaultPlayerDataKey<T> createdKey = new DefaultPlayerDataKey<>(key, type, DataPersistence.PERSISTENT, codec);
            keyStore.put(createdKey);
            return createdKey;
        }

        if (existingKey.type() != type) {
            throw new IllegalStateException("this key already exist with another type: " + type.getSimpleName());
        }

        if (existingKey.persistence() != DataPersistence.PERSISTENT) {
            throw new IllegalStateException("this key already exist with another persistence: " + DataPersistence.TRANSIENT.name());
        }

        if (existingKey.codec() != codec) {
            throw new IllegalStateException("this key already exist with another codec");
        }

        return (DefaultPlayerDataKey<T>) existingKey;
    }
}

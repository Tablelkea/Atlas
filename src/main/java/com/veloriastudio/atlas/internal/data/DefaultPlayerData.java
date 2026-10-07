package com.veloriastudio.atlas.internal.data;

import com.veloriastudio.atlas.api.data.DataPersistence;
import com.veloriastudio.atlas.api.data.PlayerData;
import com.veloriastudio.atlas.api.data.PlayerDataKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

public final class DefaultPlayerData implements PlayerData {

    private final UUID playerId;
    private final ConcurrentMap<PlayerDataKey<?>, Object> values =
            new ConcurrentHashMap<>();
    private final ConcurrentMap<PlayerDataKey<?>, DirtyState> dirtyKeys = new ConcurrentHashMap<>();

    private final AtomicLong revisionCounter = new AtomicLong();

    DefaultPlayerData(UUID playerId) {
        this.playerId = Objects.requireNonNull(playerId, "playerId cannot be null");
    }

    @Override
    public UUID playerId() {
        return playerId;
    }

    @Override
    public <T> Optional<T> get(PlayerDataKey<T> key) {

        Objects.requireNonNull(key, "key cannot be null");

        Object object = values.get(key);

        if (object == null) {
            return Optional.empty();
        }

        return Optional.of(
                key.type().cast(object)
        );
    }

    @Override
    public boolean contains(PlayerDataKey<?> key) {

        Objects.requireNonNull(key, "key cannot be null");

        return values.containsKey(key);
    }

    @Override
    public <T> void set(PlayerDataKey<T> key, T value) {

        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");

        values.put(
                key, key.type().cast(value)
        );

        if (key.persistence() == DataPersistence.PERSISTENT) {
            markDirty(key, DirtyOperation.UPSERT);
        }
    }

    @Override
    public <T> Optional<T> remove(PlayerDataKey<T> key) {

        Objects.requireNonNull(key, "key cannot be null");

        Object object = values.remove(key);

        if (object == null) {
            return Optional.empty();
        }

        if (key.persistence() == DataPersistence.PERSISTENT) {
            markDirty(key, DirtyOperation.DELETE);
        }

        return Optional.of(
                key.type().cast(object)
        );

    }

    @Override
    public <T> Optional<T> update(PlayerDataKey<T> key, UnaryOperator<T> updater) {

        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(updater, "updater cannot be null");

        Object object = values.get(key);

        if (object == null) {
            return Optional.empty();
        }

        T oldValue = key.type().cast(object);
        T newValue = updater.apply(oldValue);

        Objects.requireNonNull(newValue, "updater cannot return null");

        values.put(key, newValue);

        if (key.persistence() == DataPersistence.PERSISTENT) {
            markDirty(key, DirtyOperation.UPSERT);
        }

        return Optional.of(newValue);
    }

    private void markDirty(PlayerDataKey<?> key, DirtyOperation operation) {
        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(operation, "operation cannot be null");

        long revision = revisionCounter.incrementAndGet();

        dirtyKeys.put(
                key,
                new DirtyState(operation, revision)
        );
    }

    void markClean(PlayerDataKey<?> key, long savedRevision) {

        Objects.requireNonNull(key, "key cannot be null");

        DirtyState actualState = dirtyKeys.get(key);

        if (actualState != null && actualState.revision() == savedRevision) {
            dirtyKeys.remove(key);
        }
    }

    Map<PlayerDataKey<?>, DirtyEntry> dirtySnapshot() {

        Map<PlayerDataKey<?>, DirtyEntry> map = new HashMap<>();

        for (Map.Entry<PlayerDataKey<?>, DirtyState> entry : dirtyKeys.entrySet()) {


            PlayerDataKey<?> key = entry.getKey();
            DirtyState dirtyState = entry.getValue();

            DirtyOperation operation = dirtyState.operation();

            if (operation == DirtyOperation.UPSERT) {
                map.put(key, new DirtyEntry(operation, dirtyState.revision(), Optional.of(values.get(key))));
            } else if (operation == DirtyOperation.DELETE) {
                map.put(key, new DirtyEntry(operation, dirtyState.revision(), Optional.empty()));
            }
        }

        return Map.copyOf(map);
    }

    <T> void loadValue(PlayerDataKey<T> key, T value) {

        Objects.requireNonNull(key, "key cannot be null");
        Objects.requireNonNull(value, "value cannot be null");


        values.put(
                key,
                key.type().cast(value)
        );

    }


}

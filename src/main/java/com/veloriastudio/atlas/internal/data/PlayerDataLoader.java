package com.veloriastudio.atlas.internal.data;

import com.veloriastudio.atlas.api.data.DataKeyId;
import com.veloriastudio.atlas.internal.data.persistence.PlayerDataStorage;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerDataLoader {

    private final PlayerDataStorage storage;
    private final PlayerDataKeyStore keyStore;

    public PlayerDataLoader(
            PlayerDataStorage storage,
            PlayerDataKeyStore keyStore
    ) {

        this.storage = Objects.requireNonNull(storage, "storage cannot be null");
        this.keyStore = Objects.requireNonNull(keyStore, "keyStore cannot be null");

    }

    CompletableFuture<DefaultPlayerData> load(UUID playerId) {
        return storage.load(playerId).thenApply(
                values -> {

                    DefaultPlayerData playerData = new DefaultPlayerData(playerId);

                    for (Map.Entry<DataKeyId, String> entry : values.entrySet()) {

                        Optional<DefaultPlayerDataKey<?>> optionalKey = keyStore.find(entry.getKey());
                        if (optionalKey.isEmpty()) {
                            continue;
                        }

                        DefaultPlayerDataKey<?> key = optionalKey.get();

                        loadValue(playerData, key, entry.getValue());

                    }

                    return playerData;
                });
    }

    private <T> void loadValue(
            DefaultPlayerData playerData,
            DefaultPlayerDataKey<T> key,
            String serialized
    ) {
        T value = key.codec().decode(serialized);

        Objects.requireNonNull(value, "codec cannot return null");

        playerData.loadValue(key, value);
    }

}

package com.veloriastudio.atlas.internal.data.persistence;

import com.veloriastudio.atlas.api.data.DataKeyId;
import com.veloriastudio.atlas.api.data.PlayerDataKey;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDataStorage {

    CompletableFuture<Void> save(
            UUID playerId,
            Map<PlayerDataKey<?>, SerializedDirtyEntry> changes
    );

    CompletableFuture<Map<DataKeyId, String>> load(UUID playerId);

}

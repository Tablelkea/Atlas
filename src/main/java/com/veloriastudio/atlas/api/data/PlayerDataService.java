package com.veloriastudio.atlas.api.data;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface PlayerDataService {

    Optional<PlayerData> findLoaded(
            UUID playerId
    );

    CompletableFuture<PlayerData> load(
            UUID playerId
    );

    CompletableFuture<Void> flush(
            UUID playerId
    );

    CompletableFuture<Void> flushAll();
}
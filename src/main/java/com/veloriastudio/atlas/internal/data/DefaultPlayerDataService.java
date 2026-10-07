package com.veloriastudio.atlas.internal.data;

import com.veloriastudio.atlas.api.data.PlayerData;
import com.veloriastudio.atlas.api.data.PlayerDataService;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DefaultPlayerDataService
        implements PlayerDataService {

    private final PlayerDataLoader loader;
    private final PlayerDataSaver saver;

    private final ConcurrentMap<UUID, DefaultPlayerData> loaded =
            new ConcurrentHashMap<>();

    private final ConcurrentMap<UUID, CompletableFuture<DefaultPlayerData>> loading =
            new ConcurrentHashMap<>();

    public DefaultPlayerDataService(
            PlayerDataLoader loader,
            PlayerDataSaver saver
    ) {
        this.loader = Objects.requireNonNull(
                loader,
                "loader cannot be null"
        );

        this.saver = Objects.requireNonNull(
                saver,
                "saver cannot be null"
        );
    }

    @Override
    public Optional<PlayerData> findLoaded(
            UUID playerId
    ) {
        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        return Optional.ofNullable(
                loaded.get(playerId)
        );
    }

    @Override
    public CompletableFuture<PlayerData> load(
            UUID playerId
    ) {
        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        DefaultPlayerData existing =
                loaded.get(playerId);

        if (existing != null) {
            return CompletableFuture.completedFuture(
                    existing
            );
        }

        CompletableFuture<DefaultPlayerData> future =
                loading.computeIfAbsent(
                        playerId,
                        id ->
                                loader.load(id)
                                        .thenApply(data -> {
                                            DefaultPlayerData alreadyLoaded =
                                                    loaded.putIfAbsent(
                                                            id,
                                                            data
                                                    );

                                            return alreadyLoaded != null
                                                    ? alreadyLoaded
                                                    : data;
                                        })
                );

        future.whenComplete(
                (data, throwable) ->
                        loading.remove(
                                playerId,
                                future
                        )
        );

        return future.thenApply(
                data -> data
        );
    }

    @Override
    public CompletableFuture<Void> flush(
            UUID playerId
    ) {
        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        DefaultPlayerData data =
                loaded.get(playerId);

        if (data != null) {
            return flushData(data);
        }

        CompletableFuture<DefaultPlayerData> loadingFuture =
                loading.get(playerId);

        if (loadingFuture == null) {
            return CompletableFuture.completedFuture(
                    null
            );
        }

        /*
         * Le joueur peut quitter alors que son
         * chargement BDD est encore en cours.
         */
        return loadingFuture.thenCompose(
                this::flushData
        );
    }

    @Override
    public CompletableFuture<Void> flushAll() {
        Set<UUID> playerIds =
                new HashSet<>();

        playerIds.addAll(
                loaded.keySet()
        );

        /*
         * Les chargements encore en cours doivent aussi
         * être attendus lors de l'arrêt du plugin.
         */
        playerIds.addAll(
                loading.keySet()
        );

        CompletableFuture<?>[] futures =
                playerIds.stream()
                        .map(this::flush)
                        .toArray(
                                CompletableFuture[]::new
                        );

        return CompletableFuture.allOf(
                futures
        );
    }

    void unload(
            UUID playerId
    ) {
        Objects.requireNonNull(
                playerId,
                "playerId cannot be null"
        );

        loaded.remove(playerId);
    }

    private CompletableFuture<Void> flushData(
            DefaultPlayerData data
    ) {
        return saver.save(data)
                .thenAccept(
                        snapshot ->
                                snapshot.forEach(
                                        (key, entry) ->
                                                data.markClean(
                                                        key,
                                                        entry.revision()
                                                )
                                )
                );
    }
}
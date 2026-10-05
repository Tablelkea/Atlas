package com.veloriastudio.atlas.internal.data;

import com.veloriastudio.atlas.api.data.PlayerData;
import com.veloriastudio.atlas.api.data.PlayerDataService;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DefaultPlayerDataService implements PlayerDataService {

    private final PlayerDataLoader loader;
    private final PlayerDataSaver saver;

    private final ConcurrentMap<UUID, DefaultPlayerData> loaded = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, CompletableFuture<DefaultPlayerData>> loading = new ConcurrentHashMap<>();

    private final Duration retention;
    private final Map<UUID, Instant> inactiveSince = new ConcurrentHashMap<>();

    public DefaultPlayerDataService(PlayerDataLoader loader, PlayerDataSaver saver, Duration retention) {
        this.loader = Objects.requireNonNull(loader, "loader cannot be null");
        this.saver = Objects.requireNonNull(saver, "saver cannot be null");
        this.retention = Objects.requireNonNull(retention, "duration cannot be null");
    }

    @Override
    public Optional<PlayerData> findLoaded(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        PlayerData data = loaded.get(playerId);

        if (data == null) {
            return Optional.empty();
        }

        return Optional.of(data);
    }

    @Override
    public CompletableFuture<PlayerData> load(UUID playerId) {

        Objects.requireNonNull(playerId, "playerId cannot be null");

        PlayerData loadData = loaded.get(playerId);

        if (loadData != null) {
            return CompletableFuture.completedFuture(loadData);
        }

        CompletableFuture<DefaultPlayerData> future = loading.computeIfAbsent(playerId, id -> {

            DefaultPlayerData existingData = loaded.get(id);

            if (existingData != null) {
                return CompletableFuture.completedFuture(existingData);
            }

            return loader.load(id).thenApply(data -> {
                loaded.put(id, data);
                return data;
            });
        });

        future.whenComplete((data, error) -> loading.remove(playerId, future));

        return future.thenApply(data -> data);
    }

    CompletableFuture<Void> flush(UUID playerId) {
        DefaultPlayerData data = loaded.get(playerId);

        if (data == null) {
            return CompletableFuture.completedFuture(null);
        }

        return saver.save(data).thenAccept(snapshot -> snapshot.forEach((key, entry) -> data.markClean(key, entry.revision())));
    }

    void markActive(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        inactiveSince.remove(playerId);
    }

    void markInactive(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId cannot be null");
        inactiveSince.put(playerId, Instant.now());
    }

    CompletableFuture<Void> evictExpired() {

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map.Entry<UUID, Instant> entry : inactiveSince.entrySet()) {

            UUID playerId = entry.getKey();
            Instant inactiveAt = entry.getValue();

            if (Duration.between(inactiveAt, Instant.now()).compareTo(retention) >= 0) {

                CompletableFuture<Void> future = flush(playerId).thenRun(() -> inactiveSince.compute(playerId, (id, currentInactiveAt) -> {
                    if (Objects.equals(currentInactiveAt, inactiveAt)) {
                        loaded.remove(id);
                        return null;
                    }

                    return currentInactiveAt;
                }));

                futures.add(future);
            }
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    public CompletableFuture<Void> flushAll() {

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (UUID playerId : loaded.keySet()) {
            CompletableFuture<Void> future = flush(playerId);

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }
}

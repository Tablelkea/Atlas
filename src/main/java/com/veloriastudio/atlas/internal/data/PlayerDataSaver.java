package com.veloriastudio.atlas.internal.data;

import com.veloriastudio.atlas.api.data.PlayerDataKey;
import com.veloriastudio.atlas.internal.data.persistence.PlayerDataChangeSerializer;
import com.veloriastudio.atlas.internal.data.persistence.PlayerDataStorage;
import com.veloriastudio.atlas.internal.data.persistence.SerializedDirtyEntry;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class PlayerDataSaver {

    private final PlayerDataChangeSerializer serializer;
    private final PlayerDataStorage storage;

    public PlayerDataSaver(
            PlayerDataChangeSerializer serializer,
            PlayerDataStorage storage
    ) {

        this.serializer = Objects.requireNonNull(serializer, "serializer cannot be null");
        this.storage = Objects.requireNonNull(storage, "storage cannot be null");

    }

    CompletableFuture<Map<PlayerDataKey<?>, DirtyEntry>> save(DefaultPlayerData data) {

        Objects.requireNonNull(data, "data cannot be null");

        Map<PlayerDataKey<?>, DirtyEntry> snapshot = data.dirtySnapshot();

        if (snapshot.isEmpty()) {
            return CompletableFuture.completedFuture(snapshot);
        }

        Map<PlayerDataKey<?>, SerializedDirtyEntry> serialized =
                serializer.serialize(snapshot);

        return storage.save(data.playerId(), serialized).thenApply(
                (ignored -> snapshot)
        );

    }

}

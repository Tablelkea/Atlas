package com.veloriastudio.atlas.internal.data.persistence;

import com.veloriastudio.atlas.api.data.PlayerDataKey;
import com.veloriastudio.atlas.internal.data.DefaultPlayerDataKey;
import com.veloriastudio.atlas.internal.data.DirtyEntry;
import com.veloriastudio.atlas.internal.data.DirtyOperation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PlayerDataChangeSerializer {

    public Map<PlayerDataKey<?>, SerializedDirtyEntry> serialize(
            Map<PlayerDataKey<?>, DirtyEntry> changes
    ) {

        Map<PlayerDataKey<?>, SerializedDirtyEntry> map = new HashMap<>();

        for (Map.Entry<PlayerDataKey<?>, DirtyEntry> entry : changes.entrySet()) {

            DirtyEntry state = entry.getValue();
            PlayerDataKey<?> key = entry.getKey();

            DirtyOperation operation = state.operation();

            if (operation == DirtyOperation.DELETE) {
                map.put(key, new SerializedDirtyEntry(operation, state.revision(), Optional.empty()));
            } else if (operation == DirtyOperation.UPSERT) {

                if (!(key instanceof DefaultPlayerDataKey<?> defaultKey)) {
                    throw new IllegalStateException("Unsupported PlayerDataKey implementation");
                }

                map.put(key, serializeUpsert(defaultKey, state));
            }
        }

        return Map.copyOf(map);
    }

    private <T> SerializedDirtyEntry serializeUpsert(
            DefaultPlayerDataKey<T> key,
            DirtyEntry state
    ) {
        Object rawValue = state.value()
                .orElseThrow(() -> new IllegalStateException("UPSERT must contain a value"));

        T value = key.type().cast(rawValue);

        String encoded = Objects.requireNonNull(
                key.codec().encode(value),
                "codec cannot return null"
        );

        return new SerializedDirtyEntry(
                state.operation(),
                state.revision(),
                Optional.of(encoded)
        );
    }

}

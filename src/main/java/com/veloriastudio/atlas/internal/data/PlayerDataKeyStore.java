package com.veloriastudio.atlas.internal.data;

import com.veloriastudio.atlas.api.data.DataKeyId;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class PlayerDataKeyStore {

    private final Map<DataKeyId, DefaultPlayerDataKey<?>> keys = new HashMap<>();

    void put(DefaultPlayerDataKey<?> key) {

        Objects.requireNonNull(key, "key cannot be null");
        keys.put(key.id(), key);

    }

    Optional<DefaultPlayerDataKey<?>> find(DataKeyId id) {

        Objects.requireNonNull(id, "id cannot be null");
        return Optional.ofNullable(keys.get(id));

    }

}

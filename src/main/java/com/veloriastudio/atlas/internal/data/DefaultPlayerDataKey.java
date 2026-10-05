package com.veloriastudio.atlas.internal.data;

import com.veloriastudio.atlas.api.data.DataKeyId;
import com.veloriastudio.atlas.api.data.DataPersistence;
import com.veloriastudio.atlas.api.data.PlayerDataKey;
import com.veloriastudio.atlas.api.data.codec.DataCodec;

import java.util.Objects;

public record DefaultPlayerDataKey<T>(

        DataKeyId id,
        Class<T> type,
        DataPersistence persistence,
        DataCodec<T> codec

) implements PlayerDataKey<T> {

    public DefaultPlayerDataKey {

        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(persistence, "persistence cannot be null");

        if (persistence == DataPersistence.PERSISTENT && codec == null) {
            throw new IllegalArgumentException("codec cannot be null when data is persistent");
        }

        if (persistence == DataPersistence.TRANSIENT && codec != null) {
            throw new IllegalArgumentException("codec must be null when data is transient");
        }

    }

}

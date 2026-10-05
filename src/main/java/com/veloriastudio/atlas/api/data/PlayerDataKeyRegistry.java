package com.veloriastudio.atlas.api.data;

import com.veloriastudio.atlas.api.data.codec.DataCodec;

public interface PlayerDataKeyRegistry {

    <T> PlayerDataKey<T> registerTransient(String name, Class<T> type);

    <T> PlayerDataKey<T> registerPersistent(String name, Class<T> type, DataCodec<T> codec);

}

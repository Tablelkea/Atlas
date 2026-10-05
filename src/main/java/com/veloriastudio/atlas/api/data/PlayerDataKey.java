package com.veloriastudio.atlas.api.data;

public interface PlayerDataKey<T> {

    DataKeyId id();

    Class<T> type();

    DataPersistence persistence();

}

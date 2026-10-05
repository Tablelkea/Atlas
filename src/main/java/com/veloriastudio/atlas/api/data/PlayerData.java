package com.veloriastudio.atlas.api.data;

import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

public interface PlayerData {

    UUID playerId();

    <T> Optional<T> get(PlayerDataKey<T> key);

    boolean contains(PlayerDataKey<?> key);

    <T> void set(PlayerDataKey<T> key, T value);

    <T> Optional<T> remove(PlayerDataKey<T> key);

    <T> Optional<T> update(PlayerDataKey<T> key, UnaryOperator<T> updater);

}

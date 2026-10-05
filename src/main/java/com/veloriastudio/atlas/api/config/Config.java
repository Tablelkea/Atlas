package com.veloriastudio.atlas.api.config;

import java.util.List;
import java.util.Optional;

public interface Config {

    String name();

    boolean contains(String path);

    <T> Optional<T> get(String path, Class<T> type);

    Optional<String> getString(String path);

    Optional<Integer> getInt(String path);


    Optional<Long> getLong(String path);


    Optional<Double> getDouble(String path);


    Optional<Boolean> getBoolean(String path);


    Optional<List<String>> getStringList(String path);

    <T> T getOrDefault(String path, Class<T> type, T defaultValue);

    void set(String path, Object value);

    void save();

    void reload();

}

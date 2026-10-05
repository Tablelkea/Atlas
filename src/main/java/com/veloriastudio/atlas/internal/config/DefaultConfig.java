package com.veloriastudio.atlas.internal.config;

import com.veloriastudio.atlas.api.config.Config;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class DefaultConfig implements Config {

    private final String name;
    private final File file;
    private YamlConfiguration configuration;

    public DefaultConfig(String name, File file) {
        this.name = Objects.requireNonNull(name, "name cannot be null");
        this.file = Objects.requireNonNull(file, "file cannot be null");
        this.configuration = YamlConfiguration.loadConfiguration(file);
    }


    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean contains(String path) {

        Objects.requireNonNull(path, "path cannot be null");

        return configuration.contains(path);
    }

    @Override
    public <T> Optional<T> get(String path, Class<T> type) {

        Objects.requireNonNull(path, "path cannot be null");
        Objects.requireNonNull(type, "type cannot be null");

        Object value = configuration.get(path);

        if (value == null) {
            return Optional.empty();
        }

        return Optional.of(type.cast(value));
    }

    @Override
    public Optional<String> getString(String path) {
        return get(path, String.class);
    }

    @Override
    public Optional<Integer> getInt(String path) {
        return get(path, Integer.class);
    }

    @Override
    public Optional<Long> getLong(String path) {
        return get(path, Long.class);
    }

    @Override
    public Optional<Double> getDouble(String path) {
        return get(path, Double.class);
    }

    @Override
    public Optional<Boolean> getBoolean(String path) {
        return get(path, Boolean.class);
    }

    @Override
    public Optional<List<String>> getStringList(String path) {

        Objects.requireNonNull(path, "path cannot be null");

        Object value = configuration.get(path);

        if (value == null) {
            return Optional.empty();
        }

        if (!(value instanceof List<?> list)) {
            throw new ClassCastException(
                    "value at path '" + path + "' is not a list"
            );
        }

        for (Object element : list) {
            if (!(element instanceof String)) {
                throw new ClassCastException(
                        "list at path '" + path + "' contains a non-string value"
                );
            }
        }

        return Optional.of(
                list.stream()
                        .map(String.class::cast)
                        .toList()
        );
    }

    @Override
    public <T> T getOrDefault(String path, Class<T> type, T defaultValue) {

        Objects.requireNonNull(defaultValue, "defaultValue cannot be null");

        Optional<T> config = get(path, type);

        return config.orElse(defaultValue);

    }

    @Override
    public void set(String path, Object value) {

        Objects.requireNonNull(path, "path cannot be null");
        Objects.requireNonNull(value, "value cannot be null");

        configuration.set(path, value);
    }

    @Override
    public void save() {

        try {
            configuration.save(file);

        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save config " + name, exception);
        }

    }

    @Override
    public void reload() {
        configuration = YamlConfiguration.loadConfiguration(file);
    }
}

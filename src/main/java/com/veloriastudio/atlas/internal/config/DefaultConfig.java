package com.veloriastudio.atlas.internal.config;

import com.veloriastudio.atlas.api.config.Config;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DefaultConfig
        implements Config {

    private final String name;
    private final File file;

    private YamlConfiguration configuration;

    public DefaultConfig(
            String name,
            File file
    ) {
        this.name = Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        this.file = Objects.requireNonNull(
                file,
                "file cannot be null"
        );

        this.configuration =
                YamlConfiguration.loadConfiguration(
                        file
                );
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean contains(
            String path
    ) {
        Objects.requireNonNull(
                path,
                "path cannot be null"
        );

        return configuration.contains(
                path
        );
    }

    @Override
    public <T> Optional<T> get(
            String path,
            Class<T> type
    ) {
        Objects.requireNonNull(
                path,
                "path cannot be null"
        );

        Objects.requireNonNull(
                type,
                "type cannot be null"
        );

        Object value =
                configuration.get(path);

        if (value == null) {
            return Optional.empty();
        }

        if (!type.isInstance(value)) {
            throw new ClassCastException(
                    "value at path '"
                            + path
                            + "' is "
                            + value.getClass().getSimpleName()
                            + ", expected "
                            + type.getSimpleName()
            );
        }

        return Optional.of(
                type.cast(value)
        );
    }

    @Override
    public Optional<String> getString(
            String path
    ) {
        return get(
                path,
                String.class
        );
    }

    @Override
    public Optional<Integer> getInt(
            String path
    ) {
        return number(path)
                .map(number ->
                        convertToInt(
                                path,
                                number
                        )
                );
    }

    @Override
    public Optional<Long> getLong(
            String path
    ) {
        return number(path)
                .map(number ->
                        convertToLong(
                                path,
                                number
                        )
                );
    }

    @Override
    public Optional<Double> getDouble(
            String path
    ) {
        return number(path)
                .map(Number::doubleValue);
    }

    @Override
    public Optional<Boolean> getBoolean(
            String path
    ) {
        return get(
                path,
                Boolean.class
        );
    }

    @Override
    public Optional<List<String>> getStringList(
            String path
    ) {
        Objects.requireNonNull(
                path,
                "path cannot be null"
        );

        Object value =
                configuration.get(path);

        if (value == null) {
            return Optional.empty();
        }

        if (!(value instanceof List<?> list)) {
            throw new ClassCastException(
                    "value at path '"
                            + path
                            + "' is not a list"
            );
        }

        for (Object element : list) {
            if (!(element instanceof String)) {
                throw new ClassCastException(
                        "list at path '"
                                + path
                                + "' contains a non-string value"
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
    public <T> T getOrDefault(
            String path,
            Class<T> type,
            T defaultValue
    ) {
        Objects.requireNonNull(
                defaultValue,
                "defaultValue cannot be null"
        );

        return get(
                path,
                type
        ).orElse(defaultValue);
    }

    @Override
    public void set(
            String path,
            Object value
    ) {
        Objects.requireNonNull(
                path,
                "path cannot be null"
        );

        Objects.requireNonNull(
                value,
                "value cannot be null"
        );

        configuration.set(
                path,
                value
        );
    }

    @Override
    public void save() {
        try {
            configuration.save(
                    file
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to save config "
                            + name,
                    exception
            );
        }
    }

    @Override
    public void reload() {
        configuration =
                YamlConfiguration.loadConfiguration(
                        file
                );
    }

    private Optional<Number> number(
            String path
    ) {
        Objects.requireNonNull(
                path,
                "path cannot be null"
        );

        Object value =
                configuration.get(path);

        if (value == null) {
            return Optional.empty();
        }

        if (!(value instanceof Number number)) {
            throw new ClassCastException(
                    "value at path '"
                            + path
                            + "' is "
                            + value.getClass().getSimpleName()
                            + ", expected a number"
            );
        }

        return Optional.of(number);
    }

    private int convertToInt(
            String path,
            Number number
    ) {
        double value =
                number.doubleValue();

        if (!Double.isFinite(value)
                || value != Math.rint(value)
                || value < Integer.MIN_VALUE
                || value > Integer.MAX_VALUE) {

            throw new ArithmeticException(
                    "value at path '"
                            + path
                            + "' cannot be represented as an int"
            );
        }

        return number.intValue();
    }

    private long convertToLong(
            String path,
            Number number
    ) {
        if (number instanceof Byte
                || number instanceof Short
                || number instanceof Integer
                || number instanceof Long) {

            return number.longValue();
        }

        double value =
                number.doubleValue();

        if (!Double.isFinite(value)
                || value != Math.rint(value)
                || value < Long.MIN_VALUE
                || value > Long.MAX_VALUE) {

            throw new ArithmeticException(
                    "value at path '"
                            + path
                            + "' cannot be represented as a long"
            );
        }

        return number.longValue();
    }
}
package com.veloriastudio.atlas.api.item.custom;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public record CustomItemCategory(String path) {

    private static final Pattern PATTERN =
            Pattern.compile(
                    "[a-z0-9_-]+(?:/[a-z0-9_-]+)*"
            );

    public CustomItemCategory {

        Objects.requireNonNull(
                path,
                "path cannot be null"
        );

        if (path.isBlank()) {
            throw new IllegalArgumentException(
                    "category path cannot be blank"
            );
        }

        if (!PATTERN.matcher(path).matches()) {
            throw new IllegalArgumentException(
                    "invalid category path: " + path
            );
        }
    }

    public static CustomItemCategory of(
            String path
    ) {
        return new CustomItemCategory(path);
    }

    public List<String> segments() {
        return List.of(
                path.split("/")
        );
    }

    public String name() {

        List<String> segments =
                segments();

        return segments.getLast(
        );
    }

    public int depth() {
        return segments().size();
    }

    @Override
    public @NotNull String toString() {
        return path;
    }
}
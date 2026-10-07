package com.veloriastudio.atlas.api.data;

import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.regex.Pattern;

public record DataKeyId(
        String namespace,
        String name
) {

    private static final int MAX_NAMESPACE_LENGTH = 64;
    private static final int MAX_NAME_LENGTH = 255;

    private static final Pattern NAMESPACE_PATTERN =
            Pattern.compile(
                    "[a-z0-9_-]+"
            );

    private static final Pattern NAME_PATTERN =
            Pattern.compile(
                    "[a-z0-9_/-]+"
            );

    public DataKeyId {
        Objects.requireNonNull(
                namespace,
                "namespace cannot be null"
        );

        Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        if (namespace.isBlank()) {
            throw new IllegalArgumentException(
                    "namespace cannot be blank"
            );
        }

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "name cannot be blank"
            );
        }

        if (namespace.length()
                > MAX_NAMESPACE_LENGTH) {

            throw new IllegalArgumentException(
                    "namespace cannot exceed "
                            + MAX_NAMESPACE_LENGTH
                            + " characters"
            );
        }

        if (name.length()
                > MAX_NAME_LENGTH) {

            throw new IllegalArgumentException(
                    "name cannot exceed "
                            + MAX_NAME_LENGTH
                            + " characters"
            );
        }

        if (!NAMESPACE_PATTERN.matcher(
                namespace
        ).matches()) {

            throw new IllegalArgumentException(
                    "namespace may only contain lowercase letters, digits, '_' and '-'"
            );
        }

        if (!NAME_PATTERN.matcher(
                name
        ).matches()) {

            throw new IllegalArgumentException(
                    "name may only contain lowercase letters, digits, '_', '-' and '/'"
            );
        }

        if (name.startsWith("/")) {
            throw new IllegalArgumentException(
                    "name cannot start with /"
            );
        }

        if (name.endsWith("/")) {
            throw new IllegalArgumentException(
                    "name cannot end with /"
            );
        }

        if (name.contains("//")) {
            throw new IllegalArgumentException(
                    "name cannot contain //"
            );
        }
    }

    @Override
    public @NonNull String toString() {
        return namespace
                + ":"
                + name;
    }
}
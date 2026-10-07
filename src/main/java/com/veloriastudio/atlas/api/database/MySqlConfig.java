package com.veloriastudio.atlas.api.database;

import java.util.Objects;

public record MySqlConfig(String host, int port, String database, String username, String password) {

    public MySqlConfig(String host, int port, String database, String username, String password) {

        this.host = Objects.requireNonNull(host, "host cannot be null");
        this.port = port;
        this.database = Objects.requireNonNull(database, "database cannot be null");
        this.username = Objects.requireNonNull(username, "username cannot be null");
        this.password = Objects.requireNonNull(password, "password cannot be null");

        if (host.isBlank()) {
            throw new IllegalArgumentException("host cannot be blank");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("port must be between 1 and 65535");
        }

        if (database.isBlank()) {
            throw new IllegalArgumentException("database cannot be blank");
        }

        if (username.isBlank()) {
            throw new IllegalArgumentException("username cannot be blank");
        }
    }
}

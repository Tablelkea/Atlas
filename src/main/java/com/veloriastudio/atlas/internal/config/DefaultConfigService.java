package com.veloriastudio.atlas.internal.config;

import com.veloriastudio.atlas.api.config.Config;
import com.veloriastudio.atlas.api.config.ConfigService;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public final class DefaultConfigService
        implements ConfigService {

    private final JavaPlugin plugin;
    private final Path rootDirectory;

    private final Map<String, Config> configs =
            new ConcurrentHashMap<>();

    public DefaultConfigService(
            JavaPlugin plugin
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );

        this.rootDirectory =
                plugin.getDataFolder()
                        .toPath()
                        .toAbsolutePath()
                        .normalize();
    }

    @Override
    public Config load(
            String name
    ) {
        validateName(name);

        return configs.computeIfAbsent(
                name,
                this::loadConfig
        );
    }

    @Override
    public Config loadResource(
            String name
    ) {
        validateName(name);

        return configs.computeIfAbsent(
                name,
                this::loadResourceConfig
        );
    }

    @Override
    public void reload(
            String name
    ) {
        validateName(name);

        Config config =
                configs.get(name);

        if (config == null) {
            throw new IllegalArgumentException(
                    "config is not loaded: "
                            + name
            );
        }

        config.reload();
    }

    @Override
    public void reloadAll() {
        for (Config config : configs.values()) {
            config.reload();
        }
    }

    private Config loadConfig(
            String name
    ) {
        Path filePath =
                resolvePath(name);

        createParentDirectories(
                filePath,
                name
        );

        try {
            if (!Files.exists(filePath)) {
                Files.createFile(
                        filePath
                );
            }

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to create config "
                            + name,
                    exception
            );
        }

        return new DefaultConfig(
                name,
                filePath.toFile()
        );
    }

    private Config loadResourceConfig(
            String name
    ) {
        Path filePath =
                resolvePath(name);

        createParentDirectories(
                filePath,
                name
        );

        if (!Files.exists(filePath)) {
            plugin.saveResource(
                    name,
                    false
            );
        }

        return new DefaultConfig(
                name,
                filePath.toFile()
        );
    }

    private void createParentDirectories(
            Path filePath,
            String name
    ) {
        Path parent =
                filePath.getParent();

        if (parent == null) {
            return;
        }

        try {
            Files.createDirectories(
                    parent
            );

        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to create directories for config "
                            + name,
                    exception
            );
        }
    }

    private Path resolvePath(
            String name
    ) {
        Path filePath =
                rootDirectory.resolve(name)
                        .normalize();

        if (!filePath.startsWith(rootDirectory)) {
            throw new IllegalArgumentException(
                    "config path cannot escape plugin directory"
            );
        }

        return filePath;
    }

    private void validateName(
            String name
    ) {
        Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "name cannot be blank"
            );
        }
    }
}
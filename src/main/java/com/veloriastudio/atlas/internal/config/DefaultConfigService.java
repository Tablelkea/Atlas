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

public class DefaultConfigService implements ConfigService {

    private final JavaPlugin plugin;
    private final Path rootDirectory;

    private final Map<String, Config> configs = new ConcurrentHashMap<>();

    public DefaultConfigService(JavaPlugin plugin) {

        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");

        this.rootDirectory = plugin.getDataFolder().toPath().toAbsolutePath().normalize();
    }

    @Override
    public Config load(String name) {

        Objects.requireNonNull(name, "name cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        return configs.computeIfAbsent(name, this::loadConfig);

    }

    @Override
    public Config loadResource(String name) {

        Objects.requireNonNull(name, "name cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("name cannot be blank");
        }

        Config existing = configs.get(name);

        if (existing != null) {
            return existing;
        }

        Path filePath = resolvePath(name);

        if (!Files.exists(filePath)) {
            plugin.saveResource(name, false);
        }

        Config config = new DefaultConfig(name, filePath.toFile());

        configs.put(name, config);

        return config;

    }

    @Override
    public void reload(String name) {

        Config config = configs.get(name);

        if (config == null) {
            throw new IllegalArgumentException(
                    "config is not loaded: " + name
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

    private Config loadConfig(String name) {

        Path filePath = resolvePath(name);

        Path parent = filePath.getParent();

        try {
            if (parent != null) {
                Files.createDirectories(parent);
            }

            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

            return new DefaultConfig(name, filePath.toFile());

        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load config " + name, exception);
        }
    }

    private Path resolvePath(String name) {
        Path filePath = rootDirectory.resolve(name).normalize();

        if (!filePath.startsWith(rootDirectory)) {
            throw new IllegalArgumentException("config path cannot escape plugin directory");
        }

        return filePath;
    }

}

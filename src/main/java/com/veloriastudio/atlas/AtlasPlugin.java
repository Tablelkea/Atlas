package com.veloriastudio.atlas;

import com.veloriastudio.atlas.api.database.Database;
import com.veloriastudio.atlas.api.database.DatabaseService;
import com.veloriastudio.atlas.api.database.MySqlConfig;
import com.veloriastudio.atlas.internal.data.DefaultPlayerDataService;
import com.veloriastudio.atlas.internal.data.PlayerDataKeyStore;
import com.veloriastudio.atlas.internal.data.PlayerDataLoader;
import com.veloriastudio.atlas.internal.data.PlayerDataSaver;
import com.veloriastudio.atlas.internal.data.persistence.MySqlPlayerDataStorage;
import com.veloriastudio.atlas.internal.data.persistence.PlayerDataChangeSerializer;
import com.veloriastudio.atlas.internal.database.DefaultDatabaseService;
import com.veloriastudio.atlas.internal.database.migration.CreatePlayerDataTableMigration;
import com.veloriastudio.atlas.internal.database.migration.DatabaseMigrationRunner;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.List;
import java.util.logging.Level;

public final class AtlasPlugin extends JavaPlugin {

    private Database database;
    private DefaultPlayerDataService playerDataService;
    private PlayerDataKeyStore keyStore;
    private DatabaseService databaseService;

    @Override
    public void onEnable() {

        MySqlConfig config = new MySqlConfig(
                "YOUR_HOST",
                3306,
                "YOUR_DATABASE",
                "YOUR_USERNAME",
                "YOUR_PASSWORD"
        );

        this.databaseService = new DefaultDatabaseService();

        this.database = databaseService.createMySql(
                "atlas",
                config
        );

        MySqlPlayerDataStorage storage =
                new MySqlPlayerDataStorage(database);

        this.keyStore = new PlayerDataKeyStore();

        PlayerDataChangeSerializer serializer =
                new PlayerDataChangeSerializer();

        PlayerDataSaver saver =
                new PlayerDataSaver(serializer, storage);

        PlayerDataLoader loader =
                new PlayerDataLoader(storage, keyStore);

        this.playerDataService =
                new DefaultPlayerDataService(
                        loader,
                        saver,
                        Duration.ofMinutes(5)
                );

        DatabaseMigrationRunner migrationRunner =
                new DatabaseMigrationRunner(database);

        migrationRunner.run(
                List.of(
                        new CreatePlayerDataTableMigration()
                )
        ).thenRun(() ->
                getLogger().info("Database migrations completed")
        ).exceptionally(exception -> {
            getLogger().log(
                    Level.SEVERE,
                    "Database migrations failed",
                    exception
            );
            return null;
        });

        getLogger().info("Atlas has been enabled!");
    }

    @Override
    public void onDisable() {

        if (playerDataService != null) {
            try {
                playerDataService.flushAll().join();
            } catch (Exception exception) {
                getLogger().log(
                        Level.SEVERE,
                        "Failed to flush PlayerData during shutdown",
                        exception
                );
            }
        }

        if (databaseService != null) {
            databaseService.closeAll();
        }

        getLogger().info("Atlas has been disabled!");
    }
}
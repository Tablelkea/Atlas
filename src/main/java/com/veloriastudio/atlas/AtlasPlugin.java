package com.veloriastudio.atlas;

import com.veloriastudio.atlas.api.config.Config;
import com.veloriastudio.atlas.api.config.ConfigService;
import com.veloriastudio.atlas.api.database.Database;
import com.veloriastudio.atlas.api.database.DatabaseService;
import com.veloriastudio.atlas.api.database.MySqlConfig;
import com.veloriastudio.atlas.api.message.LocalizedMessages;
import com.veloriastudio.atlas.api.message.MessageBundleService;
import com.veloriastudio.atlas.api.message.MessageService;
import com.veloriastudio.atlas.internal.config.DefaultConfigService;
import com.veloriastudio.atlas.internal.data.DefaultPlayerDataService;
import com.veloriastudio.atlas.internal.data.PlayerDataKeyStore;
import com.veloriastudio.atlas.internal.data.PlayerDataLoader;
import com.veloriastudio.atlas.internal.data.PlayerDataSaver;
import com.veloriastudio.atlas.internal.data.persistence.MySqlPlayerDataStorage;
import com.veloriastudio.atlas.internal.data.persistence.PlayerDataChangeSerializer;
import com.veloriastudio.atlas.internal.database.DefaultDatabaseService;
import com.veloriastudio.atlas.internal.database.migration.CreatePlayerDataTableMigration;
import com.veloriastudio.atlas.internal.database.migration.DatabaseMigrationRunner;
import com.veloriastudio.atlas.internal.message.DefaultLocalizedMessages;
import com.veloriastudio.atlas.internal.message.DefaultMessageBundleService;
import com.veloriastudio.atlas.internal.message.DefaultMessageService;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class AtlasPlugin extends JavaPlugin {

    private DatabaseService databaseService;

    private DefaultPlayerDataService playerDataService;
    private PlayerDataKeyStore playerDataKeyStore;

    private ConfigService configService;

    private MessageService messageService;
    private MessageBundleService messageBundleService;
    private LocalizedMessages localizedMessages;

    @Override
    public void onEnable() {

        initializeConfig();

        Database database = initializeDatabase();

        runMigrations(database);

        initializePlayerData(database);
        initializeMessages();

        getLogger().info("Atlas has been enabled!");
    }

    @Override
    public void onDisable() {

        flushPlayerData();
        closeDatabases();

        getLogger().info("Atlas has been disabled!");
    }

    private void initializeConfig() {

        this.configService = new DefaultConfigService(this);

        configService.loadResource("config.yml");
    }

    private Database initializeDatabase() {

        Config config = configService.loadResource("config.yml");

        String host = config.getString("database.host")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Missing configuration: database.host"
                        )
                );

        int port = config.getInt("database.port")
                .orElse(3306);

        String databaseName = config.getString("database.name")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Missing configuration: database.name"
                        )
                );

        String username = config.getString("database.username")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Missing configuration: database.username"
                        )
                );

        String password = config.getString("database.password")
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Missing configuration: database.password"
                        )
                );

        MySqlConfig mySqlConfig = new MySqlConfig(
                host,
                port,
                databaseName,
                username,
                password
        );

        this.databaseService = new DefaultDatabaseService();

        return databaseService.createMySql(
                "atlas",
                mySqlConfig
        );
    }

    private void runMigrations(Database database) {

        DatabaseMigrationRunner migrationRunner =
                new DatabaseMigrationRunner(database);

        try {
            migrationRunner.run(
                    List.of(
                            new CreatePlayerDataTableMigration()
                    )
            ).join();

            getLogger().info("Database migrations completed");

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to execute database migrations",
                    exception
            );
        }
    }

    private void initializePlayerData(Database database) {

        MySqlPlayerDataStorage storage =
                new MySqlPlayerDataStorage(database);

        this.playerDataKeyStore =
                new PlayerDataKeyStore();

        PlayerDataChangeSerializer serializer =
                new PlayerDataChangeSerializer();

        PlayerDataSaver saver =
                new PlayerDataSaver(
                        serializer,
                        storage
                );

        PlayerDataLoader loader =
                new PlayerDataLoader(
                        storage,
                        playerDataKeyStore
                );

        this.playerDataService =
                new DefaultPlayerDataService(
                        loader,
                        saver,
                        Duration.ofMinutes(5)
                );
    }

    private void initializeMessages() {

        this.messageService =
                new DefaultMessageService();

        this.messageBundleService =
                new DefaultMessageBundleService(
                        configService,
                        messageService
                );

        this.localizedMessages =
                new DefaultLocalizedMessages(
                        messageBundleService,
                        "fr",
                        Map.of(
                                "fr", "messages/fr.yml",
                                "en", "messages/en.yml"
                        )
                );
    }

    private void flushPlayerData() {

        if (playerDataService == null) {
            return;
        }

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

    private void closeDatabases() {

        if (databaseService != null) {
            databaseService.closeAll();
        }
    }
}
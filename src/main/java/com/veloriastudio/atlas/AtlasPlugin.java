package com.veloriastudio.atlas;

import com.veloriastudio.atlas.api.command.CommandService;
import com.veloriastudio.atlas.api.config.Config;
import com.veloriastudio.atlas.api.config.ConfigService;
import com.veloriastudio.atlas.api.cooldown.CooldownService;
import com.veloriastudio.atlas.api.data.PlayerDataService;
import com.veloriastudio.atlas.api.database.Database;
import com.veloriastudio.atlas.api.database.DatabaseService;
import com.veloriastudio.atlas.api.database.MySqlConfig;
import com.veloriastudio.atlas.api.item.custom.CustomItemRegistry;
import com.veloriastudio.atlas.api.item.custom.DynamicCustomItemService;
import com.veloriastudio.atlas.api.message.LocalizedMessages;
import com.veloriastudio.atlas.api.message.MessageBundleService;
import com.veloriastudio.atlas.api.message.MessageService;
import com.veloriastudio.atlas.api.scheduler.SchedulerService;
import com.veloriastudio.atlas.internal.async.DefaultAsyncService;
import com.veloriastudio.atlas.internal.command.DefaultCommandService;
import com.veloriastudio.atlas.internal.command.MuseumCommand;
import com.veloriastudio.atlas.internal.command.PaperCommandRegistrar;
import com.veloriastudio.atlas.internal.command.item.CreateDynamicItemCommand;
import com.veloriastudio.atlas.internal.config.DefaultConfigService;
import com.veloriastudio.atlas.internal.cooldown.DefaultCooldownService;
import com.veloriastudio.atlas.internal.data.*;
import com.veloriastudio.atlas.internal.data.persistence.MySqlPlayerDataStorage;
import com.veloriastudio.atlas.internal.data.persistence.PlayerDataChangeSerializer;
import com.veloriastudio.atlas.internal.database.DefaultDatabaseService;
import com.veloriastudio.atlas.internal.database.migration.CreateCustomItemsTableMigration;
import com.veloriastudio.atlas.internal.database.migration.CreatePlayerDataTableMigration;
import com.veloriastudio.atlas.internal.database.migration.DatabaseMigrationRunner;
import com.veloriastudio.atlas.internal.gui.AtlasGuiListener;
import com.veloriastudio.atlas.internal.item.DefaultCustomItemRegistry;
import com.veloriastudio.atlas.internal.item.DefaultDynamicCustomItemService;
import com.veloriastudio.atlas.internal.item.dialog.CustomItemCreationDialog;
import com.veloriastudio.atlas.internal.item.museum.CustomItemMuseum;
import com.veloriastudio.atlas.internal.item.persistence.StoredCustomItemPersistenceService;
import com.veloriastudio.atlas.internal.message.DefaultLocalizedMessages;
import com.veloriastudio.atlas.internal.message.DefaultMessageBundleService;
import com.veloriastudio.atlas.internal.message.DefaultMessageService;
import com.veloriastudio.atlas.internal.scheduler.DefaultSchedulerService;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class AtlasPlugin extends JavaPlugin {

    private DatabaseService databaseService;
    private PlayerDataService playerDataService;

    private ConfigService configService;

    private MessageService messageService;
    private LocalizedMessages localizedMessages;

    private CustomItemRegistry customItemRegistry;
    private DynamicCustomItemService dynamicCustomItemService;
    private CustomItemMuseum customItemMuseum;

    private SchedulerService schedulerService;
    private CooldownService cooldownService;
    private DefaultAsyncService defaultAsyncService;

    @Override
    public void onEnable() {
        Config config = initializeConfig();
        initializeExecutionServices();
        Database database = initializeDatabase(config);

        runMigrations(database);

        DefaultPlayerDataService defaultPlayerDataService = initializePlayerData(database);

        initializeMessages();
        initializeItems(database);
        initializeListeners(defaultPlayerDataService);
        initializeCommands();

        getLogger().info("Atlas has been enabled!");
    }

    @Override
    public void onDisable() {
        flushPlayerData();
        closeAsyncService();
        closeDatabases();

        getLogger().info("Atlas has been disabled!");
    }

    private Config initializeConfig() {
        this.configService = new DefaultConfigService(this);

        return configService.loadResource("config.yml");
    }

    private void closeAsyncService(){
        if(defaultAsyncService == null){
            return;
        }

        defaultAsyncService.close();
    }

    private Database initializeDatabase(Config config) {
        String host = config.getString("database.host").orElseThrow(() -> new IllegalStateException("Missing configuration: database.host"));

        int port = config.getInt("database.port").orElse(3306);

        String databaseName = config.getString("database.name").orElseThrow(() -> new IllegalStateException("Missing configuration: database.name"));

        String username = config.getString("database.username").orElseThrow(() -> new IllegalStateException("Missing configuration: database.username"));

        String password = config.getString("database.password").orElseThrow(() -> new IllegalStateException("Missing configuration: database.password"));

        MySqlConfig mySqlConfig = new MySqlConfig(host, port, databaseName, username, password);

        this.databaseService = new DefaultDatabaseService();

        return databaseService.createMySql("atlas", mySqlConfig);
    }

    private void runMigrations(Database database) {
        DatabaseMigrationRunner migrationRunner = new DatabaseMigrationRunner(database);

        try {
            migrationRunner.run(List.of(new CreatePlayerDataTableMigration(), new CreateCustomItemsTableMigration())).join();

            getLogger().info("Database migrations completed");

        } catch (Exception exception) {
            throw new IllegalStateException("Failed to execute database migrations", exception);
        }
    }

    private DefaultPlayerDataService initializePlayerData(Database database) {
        MySqlPlayerDataStorage storage = new MySqlPlayerDataStorage(database);

        PlayerDataKeyStore keyStore = new PlayerDataKeyStore();

        PlayerDataChangeSerializer serializer = new PlayerDataChangeSerializer();

        PlayerDataSaver saver = new PlayerDataSaver(serializer, storage);

        PlayerDataLoader loader = new PlayerDataLoader(storage, keyStore);

        DefaultPlayerDataService service = new DefaultPlayerDataService(loader, saver);

        this.playerDataService = service;

        return service;
    }

    private void initializeMessages() {
        this.messageService = new DefaultMessageService();

        MessageBundleService messageBundleService = new DefaultMessageBundleService(configService, messageService);

        this.localizedMessages = new DefaultLocalizedMessages(messageBundleService, "fr", Map.of("fr", "messages/fr.yml", "en", "messages/en.yml"));
    }

    private void initializeItems(Database database) {
        this.customItemRegistry = new DefaultCustomItemRegistry(this);

        StoredCustomItemPersistenceService persistence = StoredCustomItemPersistenceService.mysql(database);

        DefaultDynamicCustomItemService dynamicItems = new DefaultDynamicCustomItemService(customItemRegistry, persistence);

        this.dynamicCustomItemService = dynamicItems;

        this.customItemMuseum = new CustomItemMuseum(customItemRegistry, dynamicCustomItemService, this);

        try {
            dynamicItems.loadStoredItems().join();

            getLogger().info("Stored custom items loaded: " + customItemRegistry.all().size());

        } catch (Exception exception) {
            throw new IllegalStateException("Failed to load stored custom items", exception);
        }
    }

    private void initializeListeners(DefaultPlayerDataService playerDataService) {
        getServer().getPluginManager().registerEvents(new AtlasGuiListener(), this);

        getServer().getPluginManager().registerEvents(new PlayerDataLifecycleListener(this, playerDataService), this);
    }

    private void initializeCommands() {
        PaperCommandRegistrar registrar = new PaperCommandRegistrar(localizedMessages, getLogger());

        CommandService commandService = new DefaultCommandService(registrar);

        CustomItemCreationDialog itemCreationDialog = new CustomItemCreationDialog(this, dynamicCustomItemService, customItemRegistry, messageService);

        commandService.register(this, new CreateDynamicItemCommand(itemCreationDialog, localizedMessages));

        commandService.register(this, new MuseumCommand(customItemMuseum, customItemRegistry));
    }

    private void flushPlayerData() {
        if (playerDataService == null) {
            return;
        }

        try {
            playerDataService.flushAll().join();

        } catch (Exception exception) {
            getLogger().log(Level.SEVERE, "Failed to flush PlayerData during shutdown", exception);
        }
    }

    private void closeDatabases() {
        if (databaseService == null) {
            return;
        }

        databaseService.closeAll();
    }

    private void initializeExecutionServices() {
        this.schedulerService = new DefaultSchedulerService(this);
        this.defaultAsyncService = new DefaultAsyncService();
        this.cooldownService = new DefaultCooldownService();
    }
}
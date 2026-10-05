package com.veloriastudio.atlas.internal.data;

import com.veloriastudio.atlas.api.data.PlayerDataKey;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class PlayerDataLifecycleListener implements Listener {

    private final JavaPlugin plugin;
    private final DefaultPlayerDataService service;
    private final PlayerDataKey<Integer> joinsKey;

    public PlayerDataLifecycleListener(
            JavaPlugin javaPlugin,
            DefaultPlayerDataService dataService,
            PlayerDataKey<Integer> dataKey
    ) {

        this.plugin = Objects.requireNonNull(javaPlugin, "javaPlugin cannot be null");
        this.service = Objects.requireNonNull(dataService, "dataService cannot be null");
        this.joinsKey = Objects.requireNonNull(dataKey, "dataKey cannot be null");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        service.markActive(playerId);

        service.load(playerId).thenAccept(data ->
                Bukkit.getScheduler().runTask(plugin, () -> {

                    if (!player.isOnline()) {
                        return;
                    }

                    int joins = data.get(joinsKey).orElse(0);

                    data.set(joinsKey, joins + 1);

                    plugin.getLogger().info(
                            player.getName() + " joins = " + (joins + 1)
                    );
                })
        );
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();

        service.markInactive(playerId);

        service.flush(playerId)
                .exceptionally(exception -> {
                    plugin.getLogger().log(
                            Level.SEVERE,
                            "Failed to save PlayerData for " + playerId,
                            exception
                    );
                    return null;
                });
    }

}

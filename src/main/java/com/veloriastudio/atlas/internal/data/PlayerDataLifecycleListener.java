package com.veloriastudio.atlas.internal.data;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public final class PlayerDataLifecycleListener
        implements Listener {

    private final JavaPlugin plugin;
    private final DefaultPlayerDataService service;

    public PlayerDataLifecycleListener(
            JavaPlugin plugin,
            DefaultPlayerDataService service
    ) {
        this.plugin = Objects.requireNonNull(
                plugin,
                "plugin cannot be null"
        );

        this.service = Objects.requireNonNull(
                service,
                "service cannot be null"
        );
    }

    @EventHandler
    public void onJoin(
            PlayerJoinEvent event
    ) {
        Player player =
                event.getPlayer();

        UUID playerId =
                player.getUniqueId();

        service.load(playerId)
                .exceptionally(throwable -> {
                    plugin.getLogger().log(
                            Level.SEVERE,
                            "Failed to load PlayerData for "
                                    + playerId,
                            throwable
                    );

                    return null;
                });
    }

    @EventHandler
    public void onQuit(
            PlayerQuitEvent event
    ) {
        UUID playerId =
                event.getPlayer()
                        .getUniqueId();

        service.flush(playerId)
                .whenComplete(
                        (ignored, throwable) ->
                                plugin.getServer()
                                        .getScheduler()
                                        .runTask(
                                                plugin,
                                                () -> finishQuit(
                                                        playerId,
                                                        throwable
                                                )
                                        )
                );
    }

    private void finishQuit(
            UUID playerId,
            Throwable throwable
    ) {
        if (throwable != null) {
            plugin.getLogger().log(
                    Level.SEVERE,
                    "Failed to save PlayerData for "
                            + playerId,
                    throwable
            );

            /*
             * En cas d'échec de sauvegarde, on conserve
             * les données en mémoire plutôt que de les
             * perdre définitivement.
             */
            return;
        }

        Player player =
                plugin.getServer()
                        .getPlayer(playerId);

        /*
         * Le joueur a pu se reconnecter pendant
         * l'écriture asynchrone en base.
         */
        if (player != null
                && player.isOnline()) {

            return;
        }

        service.unload(playerId);
    }
}
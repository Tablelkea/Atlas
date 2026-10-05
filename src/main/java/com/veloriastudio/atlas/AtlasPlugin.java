package com.veloriastudio.atlas;

import org.bukkit.plugin.java.JavaPlugin;

public final class AtlasPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info(
                "Atlas has been enabled!"
        );
    }

    @Override
    public void onDisable() {
        getLogger().info(
                "Atlas has been disabled!"
        );
    }

}

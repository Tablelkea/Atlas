package com.veloriastudio.atlas.api.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface Gui {

    Component title();

    int rows();

    void open(Player player);

}

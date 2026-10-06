package com.veloriastudio.atlas.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public interface GuiClickContext {

    Player player();

    int slot();

    ClickType click();

}

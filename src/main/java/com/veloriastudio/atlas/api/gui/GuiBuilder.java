package com.veloriastudio.atlas.api.gui;

import com.veloriastudio.atlas.internal.gui.DefaultGui;
import net.kyori.adventure.text.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class GuiBuilder {

    private final Map<Integer, GuiButton> buttons = new HashMap<>();
    private Component title;
    private int rows = 3;
    private GuiButton background;
    private GuiButton border;

    static GuiBuilder builder() {
        return new GuiBuilder();
    }

    public GuiBuilder title(Component title) {
        this.title = Objects.requireNonNull(title, "title cannot be null");
        return this;
    }

    public GuiBuilder rows(int rows) {
        this.rows = rows;
        return this;
    }

    public GuiBuilder button(int slot, GuiButton button) {

        Objects.requireNonNull(button, "button cannot be null");

        if (slot < 0) {
            throw new IllegalArgumentException("slot cannot be less than 0");
        }

        buttons.put(slot, button);
        return this;
    }

    public Gui build() {

        if (title == null) {
            throw new IllegalStateException("title must be defined");
        }

        if (rows <= 0 || rows > 6) {
            throw new IllegalStateException("rows must be between 1 and 6");
        }


        int maxSlot = rows * 9 - 1;
        Map<Integer, GuiButton> result = new HashMap<>();

        if (background != null) {
            for (int i = 0; i <= maxSlot; i++) {
                result.put(i, background);
            }

        }

        if (border != null) {
            for (int slot = 0; slot <= maxSlot; slot++) {

                int row = slot / 9;
                int column = slot % 9;

                boolean isBorder = row == 0 || row == rows - 1 || column == 0 || column == 8;

                if (isBorder) {
                    result.put(slot, border);
                }
            }
        }

        for (Map.Entry<Integer, GuiButton> entry : buttons.entrySet()) {

            int slot = entry.getKey();

            if (slot < 0 || slot > maxSlot) {
                throw new IllegalStateException("slot must be between 0 and " + maxSlot);
            }
        }

        result.putAll(buttons);

        return new DefaultGui(title, rows, Map.copyOf(result));
    }

    public GuiBuilder border(GuiButton button) {
        this.border = Objects.requireNonNull(button, "button cannot be null");

        return this;
    }

    public GuiBuilder fill(GuiButton button) {
        this.background = Objects.requireNonNull(button, "button cannot be null");
        return this;
    }

}

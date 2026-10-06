package com.veloriastudio.atlas.api.gui;

import com.veloriastudio.atlas.api.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public final class PaginatedGui<T> implements Gui {

    private Component title;
    private int rows;
    private List<T> items;
    private List<Integer> contentSlots;
    private Function<T, GuiButton> renderer;
    private GuiButton background;
    private GuiButton border;

    public PaginatedGui(Component title, int rows, List<T> items, List<Integer> contentSlots, Function<T, GuiButton> renderer) {
        this.title = Objects.requireNonNull(title, "title cannot be null");

        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException("rows must be between 1 and 6");
        }

        this.rows = rows;
        this.items = List.copyOf(Objects.requireNonNull(items, "items cannot be null"));

        Objects.requireNonNull(contentSlots, "contentSlots cannot be null");

        if (contentSlots.isEmpty()) {
            throw new IllegalArgumentException("contentSlots cannot be empty");
        }

        this.contentSlots = List.copyOf(contentSlots);
        this.renderer = Objects.requireNonNull(renderer, "renderer cannot be null");
    }

    @Override
    public Component title() {
        return this.title;
    }

    @Override
    public int rows() {
        return this.rows;
    }

    public List<T> items() {
        return this.items;
    }

    public List<Integer> contentSlots() {
        return this.contentSlots;
    }

    Function<T, GuiButton> renderer() {
        return this.renderer;
    }

    @Override
    public void open(Player player) {
        Objects.requireNonNull(player, "player cannot be null");
        openPage(player, 0);
    }

    public void openPage(Player player, int page) {

        int pageSize = contentSlots.size();
        int pageCount = Math.max(1, (int) Math.ceil((double) items.size() / pageSize));

        int previousSlot = rows * 9 - 2;
        int nextSlot = rows * 9 - 1;

        if (page < 0 || page >= pageCount) {
            throw new IllegalArgumentException("invalid page: " + page);
        }

        int start = page * pageSize;
        int end = Math.min(start + pageSize, items.size());

        GuiBuilder builder = new GuiBuilder();
        builder.title(title).rows(rows);

        if (background != null) {
            builder.fill(background);
        }

        if (border != null) {
            builder.border(border);
        }

        for (int itemIndex = start; itemIndex < end; itemIndex++) {

            T item = items.get(itemIndex);

            int localIndex = itemIndex - start;

            int slot = contentSlots.get(localIndex);

            GuiButton button = renderer.apply(item);

            builder.button(slot, button);
        }

        if (page > 0) {
            builder.button(previousSlot, GuiButton.of(ItemBuilder.of(Material.PAPER).amount(1).name(Component.text("§a§lPage Précédente")).build(), context -> openPage(context.player(), page - 1)));
        }

        if (page < pageCount - 1) {
            builder.button(nextSlot, GuiButton.of(ItemBuilder.of(Material.PAPER).amount(1).name(Component.text("§2§lPage Suivante")).build(), context -> openPage(context.player(), page + 1)));
        }

        Gui gui = builder.build();

        gui.open(player);

    }

    public PaginatedGui<T> fill(GuiButton button) {
        this.background = Objects.requireNonNull(button, "button cannot be null");

        return this;
    }

    public PaginatedGui<T> border(GuiButton button) {
        this.border = Objects.requireNonNull(button, "button cannot be null");

        return this;
    }
}

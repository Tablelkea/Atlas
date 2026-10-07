package com.veloriastudio.atlas.api.gui;

import com.veloriastudio.atlas.api.item.ItemBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public final class PaginatedGui<T> implements Gui {

    private final Component title;
    private final int rows;
    private final List<T> items;
    private final List<Integer> contentSlots;
    private final Function<T, GuiButton> renderer;

    private GuiButton background;
    private GuiButton border;

    private Component previousPageName =
            Component.text("Previous page");

    private Component nextPageName =
            Component.text("Next page");

    public PaginatedGui(
            Component title,
            int rows,
            List<T> items,
            List<Integer> contentSlots,
            Function<T, GuiButton> renderer
    ) {
        this.title = Objects.requireNonNull(
                title,
                "title cannot be null"
        );

        if (rows < 1 || rows > 6) {
            throw new IllegalArgumentException(
                    "rows must be between 1 and 6"
            );
        }

        this.rows = rows;

        this.items = List.copyOf(
                Objects.requireNonNull(
                        items,
                        "items cannot be null"
                )
        );

        Objects.requireNonNull(
                contentSlots,
                "contentSlots cannot be null"
        );

        if (contentSlots.isEmpty()) {
            throw new IllegalArgumentException(
                    "contentSlots cannot be empty"
            );
        }

        validateContentSlots(contentSlots);

        this.contentSlots = List.copyOf(contentSlots);

        this.renderer = Objects.requireNonNull(
                renderer,
                "renderer cannot be null"
        );
    }

    @Override
    public Component title() {
        return title;
    }

    @Override
    public int rows() {
        return rows;
    }

    public List<T> items() {
        return items;
    }

    public List<Integer> contentSlots() {
        return contentSlots;
    }

    Function<T, GuiButton> renderer() {
        return renderer;
    }

    @Override
    public void open(Player player) {
        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        openPage(player, 0);
    }

    public void openPage(
            Player player,
            int page
    ) {
        Objects.requireNonNull(
                player,
                "player cannot be null"
        );

        int pageSize = contentSlots.size();

        int pageCount = Math.max(
                1,
                (int) Math.ceil(
                        (double) items.size() / pageSize
                )
        );

        if (page < 0 || page >= pageCount) {
            throw new IllegalArgumentException(
                    "invalid page: " + page
            );
        }

        int previousSlot = rows * 9 - 2;
        int nextSlot = rows * 9 - 1;

        int start = page * pageSize;
        int end = Math.min(
                start + pageSize,
                items.size()
        );

        GuiBuilder builder = new GuiBuilder()
                .title(title)
                .rows(rows);

        if (background != null) {
            builder.fill(background);
        }

        if (border != null) {
            builder.border(border);
        }

        for (int itemIndex = start;
             itemIndex < end;
             itemIndex++) {

            T item = items.get(itemIndex);

            int localIndex =
                    itemIndex - start;

            int slot =
                    contentSlots.get(localIndex);

            GuiButton button =
                    Objects.requireNonNull(
                            renderer.apply(item),
                            "renderer cannot return null"
                    );

            builder.button(
                    slot,
                    button
            );
        }

        if (page > 0) {
            builder.button(
                    previousSlot,
                    GuiButton.of(
                            ItemBuilder.of(Material.PAPER)
                                    .name(previousPageName)
                                    .build(),
                            context ->
                                    openPage(
                                            context.player(),
                                            page - 1
                                    )
                    )
            );
        }

        if (page < pageCount - 1) {
            builder.button(
                    nextSlot,
                    GuiButton.of(
                            ItemBuilder.of(Material.PAPER)
                                    .name(nextPageName)
                                    .build(),
                            context ->
                                    openPage(
                                            context.player(),
                                            page + 1
                                    )
                    )
            );
        }

        builder.build().open(player);
    }

    public PaginatedGui<T> fill(
            GuiButton button
    ) {
        this.background = Objects.requireNonNull(
                button,
                "button cannot be null"
        );

        return this;
    }

    public PaginatedGui<T> border(
            GuiButton button
    ) {
        this.border = Objects.requireNonNull(
                button,
                "button cannot be null"
        );

        return this;
    }

    public PaginatedGui<T> previousPageName(
            Component name
    ) {
        this.previousPageName = Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        return this;
    }

    public PaginatedGui<T> nextPageName(
            Component name
    ) {
        this.nextPageName = Objects.requireNonNull(
                name,
                "name cannot be null"
        );

        return this;
    }

    private void validateContentSlots(
            List<Integer> slots
    ) {
        int maxSlot = rows * 9 - 1;

        int previousSlot = maxSlot - 1;
        int nextSlot = maxSlot;

        Set<Integer> uniqueSlots =
                new HashSet<>();

        for (Integer slot : slots) {

            Objects.requireNonNull(
                    slot,
                    "contentSlots cannot contain null"
            );

            if (slot < 0 || slot > maxSlot) {
                throw new IllegalArgumentException(
                        "content slot must be between 0 and "
                                + maxSlot
                );
            }

            if (slot == previousSlot
                    || slot == nextSlot) {

                throw new IllegalArgumentException(
                        "contentSlots cannot use pagination slots "
                                + previousSlot
                                + " or "
                                + nextSlot
                );
            }

            if (!uniqueSlots.add(slot)) {
                throw new IllegalArgumentException(
                        "contentSlots cannot contain duplicates"
                );
            }
        }
    }
}
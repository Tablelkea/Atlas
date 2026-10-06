package com.veloriastudio.atlas.internal.item.persistence;

import java.util.Objects;

record StoredCustomItemRecord(
        String id,
        String category,
        byte[] itemData
) {

    StoredCustomItemRecord {

        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(category, "category cannot be null");
        Objects.requireNonNull(itemData, "itemData cannot be null");

        itemData = itemData.clone();
    }

    @Override
    public byte[] itemData() {
        return itemData.clone();
    }
}

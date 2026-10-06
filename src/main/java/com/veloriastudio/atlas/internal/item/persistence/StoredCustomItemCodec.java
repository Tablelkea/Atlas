package com.veloriastudio.atlas.internal.item.persistence;

import com.veloriastudio.atlas.api.item.custom.CustomItemCategory;
import com.veloriastudio.atlas.api.item.custom.StoredCustomItem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

final class StoredCustomItemCodec {

    StoredCustomItemRecord encode(
            StoredCustomItem item
    ) {

        return new StoredCustomItemRecord(
                item.id().asString(),
                item.category().path(),
                item.template().serializeAsBytes()
        );
    }

    StoredCustomItem decode(
            StoredCustomItemRecord record
    ) {

        NamespacedKey id =
                NamespacedKey.fromString(
                        record.id()
                );

        if (id == null) {
            throw new IllegalStateException(
                    "invalid stored custom item id: "
                            + record.id()
            );
        }

        ItemStack template =
                ItemStack.deserializeBytes(
                        record.itemData()
                );

        return new StoredCustomItem(
                id,
                CustomItemCategory.of(
                        record.category()
                ),
                template
        );
    }
}
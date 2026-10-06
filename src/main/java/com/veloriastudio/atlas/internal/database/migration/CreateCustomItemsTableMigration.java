package com.veloriastudio.atlas.internal.database.migration;

import com.veloriastudio.atlas.api.database.DatabaseTransaction;
import com.veloriastudio.atlas.api.database.migration.DatabaseMigration;

public final class CreateCustomItemsTableMigration
        implements DatabaseMigration {

    @Override
    public int version() {
        return 2;
    }

    @Override
    public String name() {
        return "create custom items table";
    }

    @Override
    public void migrate(
            DatabaseTransaction transaction
    ) {

        transaction.update("""
                CREATE TABLE IF NOT EXISTS atlas_custom_items (
                    item_id VARCHAR(255) NOT NULL,
                    category VARCHAR(64) NOT NULL,
                    item_data LONGBLOB NOT NULL,
                    PRIMARY KEY (item_id)
                )
                """);
    }
}
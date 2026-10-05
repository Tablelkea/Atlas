package com.veloriastudio.atlas.internal.database.migration;

import com.veloriastudio.atlas.api.database.DatabaseTransaction;
import com.veloriastudio.atlas.api.database.migration.DatabaseMigration;

public final class CreatePlayerDataTableMigration
        implements DatabaseMigration {

    @Override
    public int version() {
        return 1;
    }

    @Override
    public String name() {
        return "create_player_data_table";
    }

    @Override
    public void migrate(DatabaseTransaction transaction) {

        transaction.update("""
                CREATE TABLE IF NOT EXISTS atlas_player_data (
                    player_uuid VARCHAR(36) NOT NULL,
                    namespace VARCHAR(64) NOT NULL,
                    data_key VARCHAR(255) NOT NULL,
                    value TEXT NOT NULL,
                    PRIMARY KEY (player_uuid, namespace, data_key)
                )
                """);
    }
}
package com.veloriastudio.atlas.api.database.migration;

import com.veloriastudio.atlas.api.database.DatabaseTransaction;

public interface DatabaseMigration {

    int version();

    String name();

    void migrate(DatabaseTransaction transaction);
}
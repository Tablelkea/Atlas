package com.veloriastudio.atlas.api.database;

import java.util.Optional;

public interface DatabaseService {

    Database createMySql(String name, MySqlConfig config);

    Optional<Database> find(String name);

    void closeAll();
}
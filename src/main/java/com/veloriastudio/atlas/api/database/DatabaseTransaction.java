package com.veloriastudio.atlas.api.database;

import java.util.List;

public interface DatabaseTransaction {

    int update(String sql, Object... parameters);

    <T> List<T> query(
            String sql,
            RowMapper<T> mapper,
            Object... parameters
    );

}

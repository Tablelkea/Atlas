package com.veloriastudio.atlas.api.database;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface RowMapper<T> {

    T map(ResultSet resultSet) throws SQLException;

}

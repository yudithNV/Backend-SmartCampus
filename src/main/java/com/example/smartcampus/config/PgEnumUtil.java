// config/PgEnumConverter.java
package com.example.smartcampus.config;

import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class PgEnumUtil {
    public static void setEnum(PreparedStatement ps, int index, Enum<?> value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.OTHER);
        } else {
            ps.setObject(index, value.name(), Types.OTHER);
        }
    }
}
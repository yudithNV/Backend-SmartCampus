package com.example.smartcampus.converter;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.usertype.UserType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class PostgreSQLEnumType implements UserType<Enum> {

    @Override
    public int getSqlType() {
        return Types.OTHER;
    }

    @Override
    public Class<Enum> returnedClass() {
        return Enum.class;
    }

    @Override
    public boolean equals(Enum x, Enum y) throws HibernateException {
        return x == y;
    }

    @Override
    public int hashCode(Enum x) throws HibernateException {
        return x == null ? 0 : x.hashCode();
    }

    @Override
    public Enum nullSafeGet(ResultSet rs, int position, SharedSessionContractImplementor session, Object owner) throws SQLException {
        String columnValue = rs.getString(position);
        if (rs.wasNull()) {
            return null;
        }

        // Aquí deberías especificar tu enum class específico
        // Por simplicidad, returno null si no puedo determinarlo
        return null;
    }

    @Override
    public void nullSafeSet(PreparedStatement st, Enum value, int index, SharedSessionContractImplementor session) throws SQLException {
        if (value == null) {
            st.setNull(index, Types.OTHER);
        } else {
            st.setObject(index, value.toString(), Types.OTHER);
        }
    }

    @Override
    public Enum deepCopy(Enum value) throws HibernateException {
        return value;
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(Enum value) throws HibernateException {
        return value;
    }

    @Override
    public Enum assemble(Serializable cached, Object owner) throws HibernateException {
        return (Enum) cached;
    }
}
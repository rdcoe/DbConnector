package com.comdev.da.persist.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface ParameterizedSelect<T, P>
{
    abstract T selectOne();

    abstract List<T> selectAll();

    void fillDTO( ResultSet rs )
        throws SQLException;
}

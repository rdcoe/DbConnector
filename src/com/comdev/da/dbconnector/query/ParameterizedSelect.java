package com.comdev.da.dbconnector.query;

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

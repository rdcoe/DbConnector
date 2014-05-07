package com.comdev.da.persist.query;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

class SelectOneResultSetHandler<T, P> implements ResultSetHandler<T>
{
    private final AbstractSelectQuery<T, P> query;

    SelectOneResultSetHandler( AbstractSelectQuery<T, P> selectQuery )
    {
        query = selectQuery;
    }

    @Override
    public T handle( ResultSet rs )
        throws SQLException
    {
        try {
            if( rs.next() ) {
                query.fillDTO( rs );
            }
        } finally {
            DbUtils.closeQuietly( rs );
        }

        return query.dto;
    }
}
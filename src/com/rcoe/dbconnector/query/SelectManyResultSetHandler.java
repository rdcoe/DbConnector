package com.rcoe.dbconnector.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.ResultSetHandler;

public class SelectManyResultSetHandler<T, P> implements ResultSetHandler<List<T>>
{
    private final AbstractSelectQuery<T, P> query;

    public SelectManyResultSetHandler( AbstractSelectQuery<T, P> selectQuery )
    {
        query = selectQuery;
    }

    @Override
    public List<T> handle( ResultSet rs )
        throws SQLException
    {
        ArrayList<T> results = new ArrayList<T>();

        try {
            while( rs.next() ) {
                query.fillDTO( rs );
                results.add( query.dto );
            }
        } finally {
            DbUtils.closeQuietly( rs );
        }
        
        return results;
    }
}

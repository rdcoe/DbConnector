package com.rcoe.dbconnector.query;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractBatchInsert<T> extends AbstractQuery implements ParameterizedInsert<int[], T>
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );
    private final Connection conn;

    public AbstractBatchInsert( Connection conn, String sql, String schema )
    {
        super( sql, schema );
        this.conn = conn;
    }

    abstract protected Object[][] buildParams( T records );

    @Override
    public int[] insert( T records )
        throws SQLException
    {
        int[] rows = {0};

        Object[][] params = buildParams( records );

        QueryRunner run = new QueryRunner();
        try {
            rows = run.batch( conn, query, params );
        } catch( SQLException e ) {
            logger.error( this.getClass().getCanonicalName()
                       + " Failed to execute query: "
                       + e.getMessage() );
            throw e;
        }

        return rows;
    }
}

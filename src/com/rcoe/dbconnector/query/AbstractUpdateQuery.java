package com.rcoe.dbconnector.query;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractUpdateQuery<T, P> extends AbstractParameterizedQuery<T, P>
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );

    public AbstractUpdateQuery( String sql, DataSource ds, String schema )
    {
        super( sql, ds, schema );
    }

    @Override
    public boolean execute( T[] keys )
    {
        Objects.requireNonNull( keys );

        boolean updated = false;

        Connection conn = null;
        QueryRunner runner = new QueryRunner();
        try {
            conn = ds.getConnection();
            P[][] parms = generateParms( keys );
            int[] rows = runner.batch( conn, query, parms );
            if( rows.length > 0 ) {
                updated = true;
                for ( int i = 0; i < rows.length; i++ ) {
                    if( rows[i] > 0 ) {
                        logger.trace( "Updated "
                                      + rows[i]
                                      + " records executing query \""
                                      + query
                                      + "\" using keys: "
                                      + Arrays.toString( parms[i] ) );
                    }
                }
            }
        } catch( SQLException e ) {
            logger.error( " Failed to update record(s): "
                          + e.getMessage() );
            DbUtils.rollbackAndCloseQuietly( conn );
        } finally {
            DbUtils.closeQuietly( conn );
        }

        return updated;
    }
}

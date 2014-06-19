package com.comdev.da.dbconnector.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInsertAndReturnKeys<P> extends AbstractQuery implements InsertAndReturnKeys<P>
{
    private final Logger logger = LoggerFactory.getLogger( this.getClass() );

    private final Connection conn;

    public AbstractInsertAndReturnKeys( Connection conn, String sql, String schema )
    {
        super( sql, schema );
        this.conn = conn;
    }
    
    abstract public Object[] buildParams( P records );

    @Override
    public Long[] insertAndReturnKeys( P record )
        throws SQLException
    {
        ArrayList<Long> keys = new ArrayList<Long>();

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = conn.prepareStatement( query, Statement.RETURN_GENERATED_KEYS );

            QueryRunner queryRunner = new QueryRunner();
            queryRunner.fillStatement( pstmt, buildParams( record ) );

            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();
            rs.next();
            keys.add( rs.getLong( 1 ) );
        } catch( SQLException e ) {
            logger.error( this.getClass().getCanonicalName()
                          + " Failed to execute query: "
                          + e.getMessage() );
            throw e;
        } finally {
            DbUtils.closeQuietly( rs );
            DbUtils.closeQuietly( pstmt );
        }

        return keys.toArray( new Long[keys.size()] );
    }
}

package com.comdev.da.persist.query;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comdev.da.auth.Identity;
import com.comdev.da.persist.DbConnectionFactory;
import com.comdev.da.persist.DbConnectionFactoryException;

public abstract class AbstractSelectQuery<T, P> extends AbstractQuery implements
        ParameterizedSelect<T, P[]>
{
    private final Logger log = LoggerFactory.getLogger( this.getClass() );
    private final DbConnectionFactory factory;
    private Connection conn;
    private PreparedStatement stmt;
    private ResultSet rs;
    private QueryRunner run;
    protected final Identity identity;
    protected T dto;

    public AbstractSelectQuery( DbConnectionFactory dbFactory,
                                String sql,
                                String schema,
                                Identity ident )
    {
        super( sql, schema );
        factory = dbFactory;
        identity = ident;
    }

    abstract public void fillDTO( ResultSet rs )
        throws SQLException;

    protected T selectOne( P[] id )
    {
        formatQuery();

        try {
            stmt = registerStatement( id );
            rs = stmt.executeQuery();
            ResultSetHandler<T> rsh = new SelectOneResultSetHandler<T, P>( this );
            dto = rsh.handle( rs );
        } catch( SQLException e ) {
            log.warn( this.getClass().getCanonicalName()
                      + " Failed to execute query: "
                      + e.getMessage() );
        } finally {
            DbUtils.closeQuietly( stmt );
            DbUtils.closeQuietly( conn );
        }

        return dto;
    }

    protected void formatQuery()
    {}

    PreparedStatement registerStatement( P[] id )
        throws SQLException
    {
        return registerStatement( id, true );
    }

    PreparedStatement registerStatement( P[] id, boolean closeActive )
        throws SQLException
    {
        try {
            conn = factory.getConnection( identity, closeActive );
            stmt = conn.prepareStatement( query );

            if( id != null
                && id.length >= 1 ) {
                run = new QueryRunner();
                run.fillStatement( stmt, id );
            }
            factory.registerStatement( conn, stmt );
        } catch( DbConnectionFactoryException e ) {
            log.error( this.getClass().getCanonicalName()
                       + " Failed to retrieve database connection, giving up: "
                       + e.getMessage() );
        }

        return stmt;
    }

    protected List<T> selectAll( P[] id )
    {
        List<T> results = new ArrayList<T>();

        formatQuery();

        try {
            stmt = registerStatement( id );
            rs = stmt.executeQuery();
            ResultSetHandler<List<T>> rsh = new SelectManyResultSetHandler<T, P>( this );
            results = rsh.handle( rs );
        } catch( SQLException e ) {
            log.warn( this.getClass().getCanonicalName()
                      + " Failed to execute query: "
                      + e.getMessage() );
        } finally {
            DbUtils.closeQuietly( stmt );
            DbUtils.closeQuietly( conn );
        }

        return results;
    }
}
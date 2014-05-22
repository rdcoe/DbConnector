package com.comdev.da.persist;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.jdbcx.JdbcDataSource;

import com.comdev.da.persist.DbConnectionFactory.DbType;

public class H2DataSourceFactory extends AbstractDataSourceFactory
{
    private final String DEFAULT_USER = "sa";
    private final String DEFAULT_PASSWD = "";

    private final String user;
    private final String passwd;
    private static String dbDir = System.getProperty( "user.dir" );

    private JdbcDataSource ds;
    private JdbcConnectionPool pool;

    public H2DataSourceFactory( String dbName, String schemaName, String user, String passwd )
    {
        this( dbName, schemaName, user, passwd, dbDir );
    }

    public H2DataSourceFactory( String dbName,
                                String schemaName,
                                String user,
                                String passwd,
                                String location )
    {
        super( dbName, schemaName );
        this.user = ( user == null ? DEFAULT_USER : user );
        this.passwd = ( passwd == null ? DEFAULT_PASSWD : passwd );

        if( new File( location ).isDirectory() ) {
            dbDir = location;
        }

        init();
        super.ds = (DataSource)ds;
        ready = true;
    }

    protected void init()
    {
        ds = new JdbcDataSource();
        ds.setURL( "jdbc:h2:" + dbDir + "/" + getDbName() + ";IGNORECASE=TRUE" );
        ds.setUser( user );
        ds.setPassword( passwd );

        bind( ds );

        pool = JdbcConnectionPool.create( "jdbc:h2:" + dbDir + "/" + getDbName(), user, passwd );
    }

    @Override
    public void setSessionState()
    {
        if( logger.isDebugEnabled() ) {
            String sql = "SET TRACE_LEVEL_FILE 4";
            Connection conn = null;
            Statement stmt = null;
            try {
                conn = getConnection();
                stmt = conn.createStatement();
                stmt.execute( sql );
                logger.debug( "Debug state initialized." );
            } catch( SQLException e ) {
                logger.error( e.getMessage(), e );
            } finally {
                DbUtils.closeQuietly( conn, stmt, null );
            }
        }
    }

    @Override
    public String getDataSourceBinding()
    {
        return getDataSourceBinding( DbType.H2 );
    }

    @Override
    public String getUrl()
    {
        return ds.getURL();
    }

    @Override
    public Connection getConnection()
        throws SQLException
    {
        return ds.getConnection();
    }

    @Override
    public Connection getPooledConnection()
        throws SQLException
    {
        return pool.getConnection();
    }

    @Override
    public String testIsDbInitializedSQL()
    {
        return "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='" +
               getSchemaName().toUpperCase() +
               "'";
    }

    @Override
    public String getPreShutdownCommand()
    {
        return "SHUTDOWN DEFRAG";
    }

    @Override
    public void close()
    {
        pool.dispose();
        super.close();
    }
}

package com.rcoe.da.dbconnector;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.rcoe.da.dbconnector.DbConnectionFactory.DbType;

public class PostgresDataSourceFactory extends AbstractDataSourceFactory
{
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 5432;
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASSWD = "postgres";

    private final String host;
    private final int port;
    private final String user;
    private final String passwd;

    private DataSource ds;

    public PostgresDataSourceFactory( String host,
                                      int port,
                                      String dbName,
                                      String schema,
                                      String user,
                                      String passwd )
    {
        super( dbName, schema );
        this.host = ( host == null ? DEFAULT_HOST : host );
        this.port = ( port <= 0 ? DEFAULT_PORT : port );
        this.user = ( user == null ? DEFAULT_USER : user );
        this.passwd = ( passwd == null ? DEFAULT_PASSWD : passwd );

        init();
    }

    public PostgresDataSourceFactory( String dbName, String schema, String user, String passwd )
    {
        this( DEFAULT_HOST, DEFAULT_PORT, dbName, schema, user, passwd );
    }

    public PostgresDataSourceFactory( String dbName, String schema )
    {
        this( DEFAULT_HOST, DEFAULT_PORT, dbName, schema, null, null );
    }

    @Override
    protected void init()
    {
        if( ready )
            return;

        PoolProperties p = new PoolProperties();
        p.setUrl( "jdbc:postgresql://" + host + ":" + port + "/" + getDbName() );
        p.setDriverClassName( "org.postgresql.Driver" );
        p.setUsername( user );
        p.setPassword( passwd );
        p.setJmxEnabled( true );
        p.setTestWhileIdle( false );
        p.setTestOnBorrow( true );
        p.setValidationQuery( "SELECT 1" );
        p.setTestOnReturn( false );
        p.setValidationInterval( 30000 );
        p.setTimeBetweenEvictionRunsMillis( 30000 );
        p.setMaxActive( 100 );
        p.setInitialSize( 10 );
        p.setMaxWait( 10000 );
        p.setRemoveAbandonedTimeout( 60 );
        p.setMinEvictableIdleTimeMillis( 30000 );
        p.setMinIdle( 10 );
        p.setLogAbandoned( true );
        p.setRemoveAbandoned( true );
        p.setJdbcInterceptors( "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                               + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer" );
        p.setDefaultAutoCommit( true );

        ds = new DataSource();
        ds.setPoolProperties( p );

        super.ds = (DataSource)ds;

        ready = true;
    }

    @Override
    public String getDataSourceBinding()
    {
        return getDataSourceBinding( DbType.PGSQL );
    }

    @Override
    public String getUrl()
    {
        return ds.getUrl();
    }

    @Override
    public String testIsDbInitializedSQL()
    {
        return "SELECT TABLES.TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLES.TABLE_SCHEMA='" +
               getSchemaName().toLowerCase() +
               "'";
    }

    @Override
    public Connection getConnection()
        throws SQLException
    {
        return getPooledConnection();
    }

    @Override
    public Connection getPooledConnection()
        throws SQLException
    {
        return ds.getConnection();
    }

    @Override
    public void setSessionState()
    {}

    @Override
    public DataSource getDataSource()
    {
        return (DataSource)ds;
    }

    @Override
    public void close()
    {
        ds.close();
        super.close();
    }
}

package com.rcoe.da.dbconnector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import javax.sql.DataSource;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rcoe.da.auth.Identity;
import com.rcoe.da.common.Log4jConfigLoader;

public class DbConnectionFactory
{
    public static final String DEFAULT_LOGLEVEL = "info";
    public static final DbType DEFAULT_DBTYPE = DbType.H2;

    private final Logger logger = LoggerFactory.getLogger( DbConnectionFactory.class );

    static Hashtable<String, DbConnectionFactory> instanceTable = new Hashtable<String, DbConnectionFactory>();
    private static DbConnectionFactory instance;

    private final DbConnectionMonitor monitor;
    private DatasourceFactory factory;
    private final String name;
    private final DbType type;

    public enum DbType {
        H2( H2DataSourceFactory.class ), PGSQL( PostgresDataSourceFactory.class );

        private final Class<? extends DatasourceFactory> factory;

        private DbType( Class<? extends DatasourceFactory> type )
        {
            factory = type;
        }

        public Class<? extends DatasourceFactory> getFactory()
        {
            return factory;
        }

        public String toString()
        {
            return this.name().toLowerCase();
        }
    }

    @SuppressWarnings( "unused" )
    private DbConnectionFactory()
    {
        this( "strictly_for_testing", DEFAULT_DBTYPE );
    }

    DbConnectionFactory( String dbName, DbType dbType )
    {
        name = dbName;
        type = dbType;
        monitor = new DbConnectionMonitor();
    }

    /**
     * The instance() method returns a database factory of the specified type, keyed against the
     * dbname. The method does not guarantee that the specified database has been initialised with
     * the correct schema. The initialisation process does not create the database itself.
     * 
     * @param dbType
     *            enum that associates the type with the jdbc driver for the database engine
     * @param dbName
     *            name of the database
     * @return
     * @throws DbConnectionFactoryException
     */
    public static synchronized DbConnectionFactory instance( String dbName )
        throws DbConnectionFactoryException
    {
        return instance( dbName, DEFAULT_DBTYPE );
    }

    public static synchronized DbConnectionFactory instance( String dbName,
                                                             DbType dbType )
        throws DbConnectionFactoryException
    {
        if( !instanceTable.containsKey( dbName ) ) {
            instance = new DbConnectionFactory( dbName, dbType );
            instanceTable.put( dbName, instance );

            new VersionInfo().printVersionInfo();
        }

        return instance;
    }

    public void initializeLogger( String loglevel )
    {
        Log4jConfigLoader loader = new Log4jConfigLoader( loglevel );
        loader.initializeLogger( DbConnectionFactory.class.getPackage().getName() );
    }

    public void init( String schema, String dbUser, String passwd )
        throws DbConnectionFactoryException
    {
        Class<? extends DatasourceFactory> factoryType = type.getFactory();
        Constructor<? extends DatasourceFactory> constructor;
        try {
            constructor = factoryType.getDeclaredConstructor( String.class,
                                                              String.class,
                                                              String.class,
                                                              String.class );

            factory = constructor.newInstance( name, schema, dbUser, passwd );
        } catch( Exception e ) {
            logger.error( "Failed to initialize the connection factory:  ", e );
            throw new DbConnectionFactoryException( e );
        }

        logger.info( "Using datasource URL: "
                     + factory.getUrl()
                     + "." );

        factory.setSessionState();
    }

    public void executeSQL( final InputStream sqlFileStream )
        throws DbConnectionFactoryException
    {
        if( !isReady() ) {
            throw new DbConnectionFactoryException( "DbConnectionFactory is not initialized." );
        }

        StringBuilder sql = prepareSQL( sqlFileStream, factory.getSchemaName() );

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            stmt.execute( sql.toString() );
        } catch( SQLException e ) {
            logger.error( e.getMessage(), e );
            throw new DbConnectionFactoryException( e );
        } finally {
            DbUtils.closeQuietly( conn, stmt, null );
        }
    }

    public boolean isReady()
    {
        boolean ready = false;
        if( factory != null ) {
            ready = factory.isReady();
        }

        return ready;
    }

    public Connection getConnection()
        throws DbConnectionFactoryException
    {
        Connection conn = null;
        try {
            conn = factory.getPooledConnection();
        } catch( SQLException e ) {
            throw new DbConnectionFactoryException( e );
        }

        return conn;
    }

    public boolean isDbInitialized()
        throws DbConnectionFactoryException
    {
        boolean initialized = false;

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery( factory.testIsDbInitializedSQL() );
            if( rs.next() ) {
                initialized = true;
            } else {
                initialized = false;
            }
        } catch( SQLException e ) {
            logger.error( e.getMessage() );
            initialized = false;
        } catch( DbConnectionFactoryException e ) {
            logger.error( e.getMessage() );
            initialized = false;
        } finally {
            DbUtils.closeQuietly( conn, stmt, rs );
        }

        return initialized;
    }

    private StringBuilder prepareSQL( final InputStream sqlFileStream, String schemaName )
        throws DbConnectionFactoryException
    {
        StringBuilder sql = new StringBuilder();

        BufferedReader br = new BufferedReader( new InputStreamReader( sqlFileStream ) );
        String line;
        try {
            while( ( line = br.readLine() ) != null ) {
                sql.append( line.replaceAll( "\\{schema_name\\}", schemaName ) ).append( "\n" );
            }
        } catch( IOException e ) {
            logger.error( "Failed to prepare SQL statement. {}", e.getMessage() );
            throw new DbConnectionFactoryException();
        } finally {
            IOUtils.closeQuietly( br );
        }

        return sql;
    }

    public Connection getConnection( Identity identity )
        throws DbConnectionFactoryException
    {
        Connection conn = getConnection();
        monitor.add( identity, conn );

        return conn;
    }

    /**
     * The getConnection( Identity, closeActive ) method will optionally close any connections held
     * by {@link Identity}.
     * 
     * @param identity
     * @param closeActive
     * @return
     * @throws DbConnectionFactoryException
     */
    public Connection getConnection( Identity identity, boolean closeActive )
        throws DbConnectionFactoryException
    {
        if( closeActive ) {
            monitor.closeConnections( identity );
        }

        return getConnection( identity );
    }

    public void registerStatement( Connection conn, PreparedStatement stmt )
    {
        monitor.registerStatement( conn, stmt );
    }

    /**
     * The getDataSourceFactory method returns the {@link DatasourceFactory} used by the
     * {@link DbConnectionFactory}
     * 
     * @return
     */
    public DatasourceFactory getDataSourceFactory()
    {
        return factory;
    }

    public DataSource getDataSource()
        throws DbConnectionFactoryException
    {
        return factory.getDataSource();
    }

    public synchronized void close()
    {
        String sql = factory.getPreShutdownCommand();
        if( sql != null ) {
            logger.debug( "Executing database pre-shutdown command." );

            Connection conn = null;
            Statement stmt = null;
            try {
                conn = getConnection();
                stmt = conn.createStatement();
                stmt.execute( sql.toString() );

            } catch( SQLException e ) {
                logger.error( "...exception thrown when executing pre-shutdown command.", e );
            } catch( DbConnectionFactoryException e ) {
                logger.error( "...failed to retrieve a database connection to execute the pre-shutdown command.",
                              e );
            } finally {
                DbUtils.closeQuietly( conn, stmt, null );
            }
        }

        logger.info( "Closing all active database connections." );
        monitor.closeAll();

        factory.close();
        instanceTable.remove( name );
        logger.info( name
                     + " database successfully closed." );
    }

    public DbType getDbType()
    {
        return type;
    }
}

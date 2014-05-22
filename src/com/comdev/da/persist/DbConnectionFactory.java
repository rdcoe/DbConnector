package com.comdev.da.persist;

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

import com.comdev.da.auth.Identity;

public class DbConnectionFactory
{
    private static final Logger logger = LoggerFactory.getLogger( DbConnectionFactory.class );
    public static final DbType DEFAULT_DBTYPE = DbType.H2;

    private static Hashtable<DbType, DbConnectionFactory> instanceTable = new Hashtable<DbConnectionFactory.DbType, DbConnectionFactory>();
    private static DbConnectionFactory instance;

    private final DbConnectionMonitor monitor;
    private SQLFactory factory;
    private String schema;

    public final DbType dbType;

    public enum DbType {
        H2( H2DataSourceFactory.class ), PGSQL( PostgresDataSourceFactory.class );

        private final Class<? extends SQLFactory> factory;

        private DbType( Class<? extends SQLFactory> type )
        {
            factory = type;
        }

        public Class<? extends SQLFactory> getFactory()
        {
            return factory;
        }

        public String toString()
        {
            return this.name().toLowerCase();
        }
    }

    private DbConnectionFactory()
    {
        this( DEFAULT_DBTYPE );
    }

    private DbConnectionFactory( DbType type )
    {
        monitor = new DbConnectionMonitor();
        dbType = type;
    }

    /**
     * The instance() method returns a fully constructed database factory of the specified type. The
     * method guarantees that the specified database has been initialised with the correct schema.
     * The initialisation process does not create the database itself.
     * 
     * @param dbType
     *            enum that associates the type with the jdbc driver for the database engine
     * @param dbName
     *            name of the database
     * @return
     * @throws DbConnectionFactoryException
     */
    public static synchronized DbConnectionFactory instance( DbType dbType )
        throws DbConnectionFactoryException
    {
        if( !instanceTable.containsKey( dbType ) ) {
            instance = new DbConnectionFactory( dbType );
            instanceTable.put( dbType, instance );
        }

        return instance;
    }

    public void init( String dbName, String schema, String dbUser, String passwd )
        throws DbConnectionFactoryException
    {
        Class<? extends SQLFactory> factoryType = dbType.getFactory();
        Constructor<? extends SQLFactory> constructor;
        try {
            constructor = factoryType.getDeclaredConstructor( String.class,
                                                              String.class,
                                                              String.class,
                                                              String.class );

            factory = constructor.newInstance( dbName, schema, dbUser, passwd );
            this.schema = schema;
        } catch( Exception e ) {
            logger.error( "Failed to initialize the connection factory:  ", e );
            throw new DbConnectionFactoryException( e );
        }

        logger.info( "Using datasource URL: " + factory.getUrl() + "." );

        factory.setSessionState();
    }

    public boolean executeSQL( final InputStream sqlFileStream )
        throws DbConnectionFactoryException
    {
        if( !isReady() ) {
            return false;
        }

        StringBuilder sql = loadSchema( sqlFileStream, schema );

        boolean success = false;

        Connection conn = null;
        Statement stmt = null;
        try {
            conn = getConnection();
            stmt = conn.createStatement();
            success = stmt.execute( sql.toString() );
        } catch( SQLException e ) {
            logger.error( e.getMessage(), e );
            throw new DbConnectionFactoryException( e );
        } finally {
            DbUtils.closeQuietly( conn, stmt, null );
        }

        return success;
    }

    public boolean isReady()
    {
        boolean ready = false;
        if( factory != null ) {
            ready = factory.isReady();
        }

        return ready;
    }

    Connection getConnection()
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
        if( !isReady() ) {
            return false;
        }

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

    public Connection getConnection( Identity identity )
        throws DbConnectionFactoryException
    {
        Connection conn = getConnection();
        monitor.add( identity, conn );

        return conn;
    }

    private StringBuilder loadSchema( final InputStream sqlFileStream, String schemaName )
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
            throw new DbConnectionFactoryException( "Failed to load schema initialization file.", e );
        } finally {
            IOUtils.closeQuietly( br );
        }

        return sql;
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
     * The getDataSourceFactory method returns the {@link SQLFactory} used by the
     * {@link DbConnectionFactory}
     * 
     * @return
     */
    public SQLFactory getDataSourceFactory()
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

        logger.info( dbType + " database successfully closed." );
    }
}

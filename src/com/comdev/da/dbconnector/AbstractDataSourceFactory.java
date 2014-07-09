package com.comdev.da.dbconnector;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comdev.da.dbconnector.DbConnectionFactory.DbType;

public abstract class AbstractDataSourceFactory implements DatasourceFactory
{
    protected Logger logger = LoggerFactory.getLogger( getClass() );

    protected DataSource ds;
    private final String dbName;
    private final String schema;

    protected boolean ready = false;

    protected AbstractDataSourceFactory( String dbName, String schema )
    {
        this.dbName = dbName;
        this.schema = schema;
    }

    protected abstract void init();

    protected void bind( DataSource ds )
    {
        try {
            InitialContext ic = new InitialContext();
            ic.rebind( "java:/comp/env/"
                       + getDataSourceBinding(), (DataSource)ds );
        } catch( NamingException e ) {
            // this is an expected exception when the datasource is instantiated multiple times
            logger.error( "Failed to bind the datasource.  "
                          + e.getExplanation() );
        }
    }

    protected String getDataSourceBinding( DbType type )
    {
        return "jdbc/"
               + type.toString()
               + "."
               + dbName;
    }

    @Override
    public String getDbName()
    {
        return dbName;
    }

    @Override
    public String getSchemaName()
    {
        return schema;
    }

    @Override
    public String getPreShutdownCommand()
    {
        return null;
    }

    @Override
    public DataSource getDataSource()
    {
        return ds;
    }

    @Override
    public boolean isReady()
    {
        return ready;
    }

    @Override
    public void close()
    {
        ready = false;
    }
}

package com.comdev.da.dbconnector;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * A DatasourceFactory provides expected behaviour that all {@link DataSource} implementations must
 * use.
 * 
 * @see H2DataSourceFactory
 * @see PostgresDataSourceFactory
 * 
 * @author rcoe
 * 
 */
public interface DatasourceFactory
{
    public String getUrl();

    public String testIsDbInitializedSQL();

    public String getDataSourceBinding();

    public String getPreShutdownCommand();

    public Connection getConnection()
        throws SQLException;

    public Connection getPooledConnection()
        throws SQLException;

    public void setSessionState();

    public DataSource getDataSource();

    public String getDbName();

    public String getSchemaName();

    public boolean isReady();

    public void close();

}

package com.comdev.da.persist;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import junit.framework.Assert;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Before;
import org.junit.Test;

import com.comdev.da.persist.DbConnectionFactory.DbType;

public class InitializeDataSourceTest
{
    private static final String DB_NAME = "testDb";
    private static final String SCHEMA_NAME = "testSchema";
    private String dbuser = "";
    private String dbPasswd = "";

    private static enum table {
        RESOURCE_REF, LOOKUP, ACL_ENTRY
    }

    @Before
    public void setUp()
    {
        EnvironmentFactory.setNamingContext();
    }

    @Test
    public void testH2DataSource()
        throws Exception
    {
        dbuser = "sa";
        dbPasswd = "";

        DbConnectionFactory instance = DbConnectionFactory.instance( DbType.H2 );
        instance.init( DB_NAME, SCHEMA_NAME, dbuser, dbPasswd );
        if( !instance.isDbInitialized() ) {
            instance.initializeDatabase();
        }

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean schemaExists = false;
        try {
            conn = instance.getConnection();
            stmt = conn.createStatement();

            H2DataSourceFactory factory = new H2DataSourceFactory( DB_NAME,
                                                                   SCHEMA_NAME,
                                                                   dbuser,
                                                                   dbPasswd );
            rs = stmt.executeQuery( factory.testIsDbInitializedSQL() );
            while( rs.next() ) {
                schemaExists = true;
                String tablename = rs.getString( 1 ).toUpperCase();
                switch( table.valueOf( tablename ) ) {
                case RESOURCE_REF:
                case LOOKUP:
                case ACL_ENTRY:
                    continue;
                default:
                    Assert.fail( "Invalid table name returned." );
                }
            }
        } finally {
            DbUtils.closeQuietly( conn, stmt, rs );
            DbConnectionFactory.instance( DbType.H2 ).close();
        }

        Assert.assertTrue( "Schema not created.", schemaExists );
    }

    @Test
    public void testPGSQLDataSource()
        throws Exception
    {
        dbuser = "postgres";
        dbPasswd = "postgres";

        DbConnectionFactory instance = DbConnectionFactory.instance( DbType.PGSQL );
        instance.init( DB_NAME, SCHEMA_NAME, dbuser, dbPasswd );
        if( !instance.isDbInitialized() ) {
            instance.initializeDatabase();
        }

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        boolean schemaExists = false;
        try {
            conn = instance.getConnection();
            stmt = conn.createStatement();

            PostgresDataSourceFactory factory = new PostgresDataSourceFactory( DB_NAME,
                                                                               SCHEMA_NAME,
                                                                               dbuser,
                                                                               dbPasswd );
            rs = stmt.executeQuery( factory.testIsDbInitializedSQL() );
            while( rs.next() ) {
                schemaExists = true;
                String tablename = rs.getString( 1 ).toUpperCase();
                switch( table.valueOf( tablename ) ) {
                case RESOURCE_REF:
                case LOOKUP:
                case ACL_ENTRY:
                    continue;
                default:
                    Assert.fail( "Invalid table name returned." );
                }
            }
        } finally {
            DbUtils.closeQuietly( conn, stmt, rs );
            DbConnectionFactory.instance( DbType.PGSQL ).close();
        }

        Assert.assertTrue( "Schema not created.", schemaExists );
    }
}

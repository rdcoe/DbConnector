package com.rcoe.dbconnector;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.commons.dbutils.DbUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import com.rcoe.dbconnector.DbConnectionFactory;
import com.rcoe.dbconnector.DbConnectionFactory.DbType;
import com.rcoe.dbconnector.EnvironmentFactory;
import com.rcoe.dbconnector.H2DataSourceFactory;
import com.rcoe.dbconnector.PostgresDataSourceFactory;

import junit.framework.Assert;

public class InitializeDataSourceTest
{
    private static final String SCHEMA_FILE = "schema_init.sql";
    private static final String PGSQL_INIT_SCHEMA_FILE = "pgsql_init.sql";
    private static final String PGSQL_SCHEMA_INDEX_FILE = "pgsql_indices.sql";
    private static final String H2_INIT_SCHEMA_FILE = "h2_init.sql";
    private static final String H2_SCHEMA__INDEX_FILE = "h2_indices.sql";
    private static final String DB_NAME = "testDb";
    private static final String SCHEMA_NAME = "testSchema";
    private String dbuser = "";
    private String dbPasswd = "";

    private static enum DBTYPE {
                                H2, PGSQL
    };

    private static DBTYPE dbUnderTest;
    {
        String db = System.getenv( "datasource.arg" );
        db = ( db == null ? "-d H2" : db );
        switch( db.toUpperCase() ) {
        case "-D PGSQL":
            dbUnderTest = DBTYPE.PGSQL;
            break;
        case "-D H2":
            dbUnderTest = DBTYPE.H2;
            break;
        default:
            dbUnderTest = DBTYPE.H2;
        }
    }

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
        Assume.assumeTrue( dbUnderTest == DBTYPE.H2 );
        dbuser = "sa";
        dbPasswd = "";

        DbConnectionFactory instance = DbConnectionFactory.instance( DB_NAME );
        instance.init( SCHEMA_NAME, dbuser, dbPasswd );
        if( !instance.isDbInitialized() ) {
            InputStream fis = ClassLoader.getSystemResourceAsStream( H2_INIT_SCHEMA_FILE );
            instance.executeSQL( fis );
            fis = ClassLoader.getSystemResourceAsStream( SCHEMA_FILE );
            instance.executeSQL( fis );
            fis = ClassLoader.getSystemResourceAsStream( H2_SCHEMA__INDEX_FILE );
            instance.executeSQL( fis );
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
            stmt.execute( "DROP SCHEMA " + SCHEMA_NAME );
            DbUtils.closeQuietly( conn, stmt, rs );
            DbConnectionFactory.instance( DB_NAME ).close();
        }

        Assert.assertTrue( "Schema not created.", schemaExists );
    }

    @Test
    public void testPGSQLDataSource()
        throws Exception
    {
        Assume.assumeTrue( dbUnderTest == DBTYPE.PGSQL );
        dbuser = "postgres";
        dbPasswd = "postgres";

        DbConnectionFactory instance = DbConnectionFactory.instance( DB_NAME, DbType.PGSQL );
        instance.init( SCHEMA_NAME, dbuser, dbPasswd );
        if( !instance.isDbInitialized() ) {
            InputStream fis = ClassLoader.getSystemResourceAsStream( PGSQL_INIT_SCHEMA_FILE );
            instance.executeSQL( fis );
            fis = ClassLoader.getSystemResourceAsStream( SCHEMA_FILE );
            instance.executeSQL( fis );
            fis = ClassLoader.getSystemResourceAsStream( PGSQL_SCHEMA_INDEX_FILE );
            instance.executeSQL( fis );
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
            stmt.execute( "DROP SCHEMA " + SCHEMA_NAME +
                          " CASCADE" );
            DbUtils.closeQuietly( conn, stmt, rs );
            DbConnectionFactory.instance( DB_NAME ).close();
        }

        Assert.assertTrue( "Schema not created.", schemaExists );
    }
}

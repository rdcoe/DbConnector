package com.rcoe.da.dbconnector;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;

import javax.sql.DataSource;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.rcoe.da.dbconnector.DbConnectionFactory.DbType;

@RunWith( PowerMockRunner.class )
@PrepareForTest( {DbConnectionFactory.class} )
public class DbConnectionFactoryTest
{
    private static final String DB_NAME = "testDb";
    private static final String SCHEMA_NAME = "testSchema";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWD = "";

    @After
    public void tearDown()
        throws Exception
    {
        Hashtable<String, DbConnectionFactory> instanceTable = DbConnectionFactory.instanceTable;
        instanceTable.clear();
    }

    @Test
    public void testGetInstanceReturnsH2Instance()
        throws Exception
    {
        DbConnectionFactory instance = DbConnectionFactory.instance( DB_NAME );
        instance.init( SCHEMA_NAME, DB_USER, DB_PASSWD );

        Hashtable<String, DbConnectionFactory> instanceTable = DbConnectionFactory.instanceTable;
        DbConnectionFactory expected = instanceTable.get( DB_NAME );

        Assert.assertSame( expected, instance );
    }

    @Test
    public void testInitializationThrowsException()
        throws Exception
    {
        DbConnectionFactory mockFactory = PowerMock.createMockAndExpectNew( DbConnectionFactory.class,
                                                                            EasyMock.isA( String.class ),
                                                                            EasyMock.isA( DbType.class ) );
        mockFactory.initializeLogger( EasyMock.isA( String.class ) );

        mockFactory.init( SCHEMA_NAME, DB_USER, DB_PASSWD );
        PowerMock.expectLastCall().andThrow( new DbConnectionFactoryException() );

        PowerMock.replayAll();

        boolean thrown = false;
        try {
            DbConnectionFactory instance = DbConnectionFactory.instance( DB_NAME );
            instance.initializeLogger( "info" );
            instance.init( SCHEMA_NAME, DB_USER, DB_PASSWD );
        } catch( DbConnectionFactoryException e ) {
            thrown = true;
        }

        PowerMock.verifyAll();

        Assert.assertTrue( "DbConnectionFactoryException should have been thrown", thrown );
    }

    @Test
    public void testIsDbInitialized()
        throws Exception
    {
        DbConnectionFactory instance = PowerMock.createPartialMockAndInvokeDefaultConstructor( DbConnectionFactory.class,
                                                                                               "getConnection" );

        DatasourceFactory mockSqlFactory = PowerMock.createMock( DatasourceFactory.class );
        Whitebox.setInternalState( instance, DatasourceFactory.class, mockSqlFactory );

        Connection mockCon = PowerMock.createMock( Connection.class );
        Statement mockStmt = PowerMock.createMock( Statement.class );
        ResultSet mockRs = PowerMock.createMock( ResultSet.class );

        EasyMock.expect( instance.getConnection() ).andReturn( mockCon );
        EasyMock.expect( mockCon.createStatement() ).andReturn( mockStmt );
        EasyMock.expect( mockStmt.executeQuery( EasyMock.isA( String.class ) ) ).andReturn( mockRs );
        EasyMock.expect( mockSqlFactory.testIsDbInitializedSQL() ).andReturn( "" );

        // test true branch
        EasyMock.expect( mockRs.next() ).andReturn( true );

        mockRs.close();
        mockStmt.close();
        mockCon.close();

        PowerMock.replayAll();

        boolean isInited = instance.isDbInitialized();
        Assert.assertTrue( "should be inited.", isInited );

        PowerMock.verifyAll();

        // test false branch
        PowerMock.resetAll();

        EasyMock.expect( instance.getConnection() ).andReturn( mockCon );
        EasyMock.expect( mockCon.createStatement() ).andReturn( mockStmt );
        EasyMock.expect( mockStmt.executeQuery( EasyMock.isA( String.class ) ) ).andReturn( mockRs );
        EasyMock.expect( mockSqlFactory.testIsDbInitializedSQL() ).andReturn( "" );

        // test false branch
        EasyMock.expect( mockRs.next() ).andReturn( false );

        mockRs.close();
        mockStmt.close();
        mockCon.close();

        PowerMock.replayAll();

        isInited = instance.isDbInitialized();
        Assert.assertFalse( "should not be inited.", isInited );

        PowerMock.verifyAll();
    }

    @Test
    public void testGetConnection()
        throws Exception
    {
        DbConnectionFactory instance = DbConnectionFactory.instance( DB_NAME );

        DatasourceFactory mockSqlFactory = PowerMock.createMock( DatasourceFactory.class );
        Connection mockCon = PowerMock.createMock( Connection.class );
        EasyMock.expect( mockSqlFactory.getPooledConnection() ).andReturn( mockCon );

        PowerMock.replayAll();

        Whitebox.setInternalState( instance, DatasourceFactory.class, mockSqlFactory );
        instance.getConnection();

        PowerMock.verifyAll();
    }

    @Test
    public void testGetDataSource()
        throws Exception
    {
        DbConnectionFactory instance = DbConnectionFactory.instance( DB_NAME );

        DatasourceFactory mockSqlFactory = PowerMock.createMock( DatasourceFactory.class );
        DataSource mockDs = PowerMock.createMock( DataSource.class );
        EasyMock.expect( mockSqlFactory.getDataSource() ).andReturn( mockDs );

        PowerMock.replayAll();

        Whitebox.setInternalState( instance, DatasourceFactory.class, mockSqlFactory );
        instance.getDataSource();

        PowerMock.verifyAll();
    }

    @Test
    public void testExecuteSQL()
        throws Exception
    {
        DbConnectionFactory instance = PowerMock.createPartialMockAndInvokeDefaultConstructor( DbConnectionFactory.class,
                                                                                               "prepareSQL" );
        PowerMock.replay( instance );
        PowerMock.reset( instance );

        DatasourceFactory mockSqlFactory = PowerMock.createMock( DatasourceFactory.class );
        EasyMock.expect( mockSqlFactory.isReady() ).andReturn( true );
        EasyMock.expect( mockSqlFactory.getSchemaName() ).andReturn( SCHEMA_NAME );

        StringBuilder sql = new StringBuilder();
        PowerMock.expectPrivate( instance,
                                 "prepareSQL",
                                 EasyMock.isA( InputStream.class ),
                                 EasyMock.isA( String.class ) ).andStubReturn( sql );

        Connection mockCon = PowerMock.createMock( Connection.class );
        Statement mockStmt = PowerMock.createMock( Statement.class );

        EasyMock.expect( mockSqlFactory.getPooledConnection() ).andReturn( mockCon );
        EasyMock.expect( mockCon.createStatement() ).andReturn( mockStmt );
        EasyMock.expect( mockStmt.execute( EasyMock.isA( String.class ) ) ).andReturn( true );

        mockStmt.close();
        mockCon.close();

        PowerMock.replayAll();

        Whitebox.setInternalState( instance, DatasourceFactory.class, mockSqlFactory );

        instance.executeSQL( new BufferedInputStream( null ) );

        PowerMock.verifyAll();
    }

    @Test
    public void testprepareSQL()
        throws Exception
    {
        DbConnectionFactory instance = new DbConnectionFactory( DB_NAME, DbType.H2 );

        DatasourceFactory mockSQLFactory = EasyMock.createMock( DatasourceFactory.class );
        Whitebox.setInternalState( instance, DatasourceFactory.class, mockSQLFactory );

        PowerMock.replayAll();

        instance.init( SCHEMA_NAME, DB_USER, DB_PASSWD );

        InputStream fis = ClassLoader.getSystemResourceAsStream( "schema_init.sql" );
        StringBuilder actual = Whitebox.invokeMethod( instance, "prepareSQL", fis, SCHEMA_NAME );

        PowerMock.verifyAll();

        String expected = "CREATE TABLE testSchema.resource_ref(\n"
                          + "    uid SERIAL PRIMARY KEY,\n"
                          + "    path VARCHAR(512) NOT NULL,\n"
                          + "    filename VARCHAR(255) NOT NULL,\n"
                          + "    createdTime TIMESTAMP NOT NULL,\n"
                          + "    lastAccessed TIMESTAMP NOT NULL,\n"
                          + "    lastModified TIMESTAMP NOT NULL,\n"
                          + "    size BIGINT NOT NULL,\n"
                          + "    fileKey INTEGER NOT NULL,\n"
                          + "    visible BOOLEAN DEFAULT true NOT NULL\n"
                          + ");\n"
                          + "\n"
                          + "CREATE TABLE testSchema.lookup(\n"
                          + "    uid SERIAL PRIMARY KEY,\n"
                          + "    lookupKey VARCHAR(255) NOT NULL,\n"
                          + "    resourceId BIGINT NOT NULL REFERENCES testSchema.resource_ref(uid) ON DELETE CASCADE,\n"
                          + "    createdTime TIMESTAMP NOT NULL,\n"
                          + "    updatedTime TIMESTAMP\n"
                          + ");\n"
                          + "\n"
                          + "CREATE TABLE testSchema.acl_entry(\n"
                          + "    uid SERIAL PRIMARY KEY,\n"
                          + "    resourceId BIGINT NOT NULL REFERENCES testSchema.resource_ref(uid) ON DELETE CASCADE,\n"
                          + "    principalName VARCHAR(255) NOT NULL,\n"
                          + "    lastModified TIMESTAMP NOT NULL\n"
                          + ");\n";

        Assert.assertEquals( "Schema does not match.", expected, actual.toString() );
    }

    @Test
    public void testClose()
        throws Exception
    {
        DbConnectionFactory instance = PowerMock.createPartialMockAndInvokeDefaultConstructor( DbConnectionFactory.class,
                                                                                               "getConnection" );
        PowerMock.replay( instance );
        PowerMock.reset( instance );

        DatasourceFactory mockSqlFactory = PowerMock.createMock( DatasourceFactory.class );
        EasyMock.expect( mockSqlFactory.getPreShutdownCommand() ).andReturn( "" );

        Connection mockCon = PowerMock.createMock( Connection.class );
        Statement mockStmt = PowerMock.createMock( Statement.class );

        EasyMock.expect( instance.getConnection() ).andReturn( mockCon );
        EasyMock.expect( mockCon.createStatement() ).andReturn( mockStmt );
        EasyMock.expect( mockStmt.execute( EasyMock.isA( String.class ) ) ).andReturn( true );

        mockSqlFactory.close();

        mockStmt.close();
        mockCon.close();

        PowerMock.replayAll();

        Whitebox.setInternalState( instance, DatasourceFactory.class, mockSqlFactory );

        instance.close();

        PowerMock.verifyAll();
    }
}

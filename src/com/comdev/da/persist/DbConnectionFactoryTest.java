package com.comdev.da.persist;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Set;

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

import com.comdev.da.persist.DbConnectionFactory.DbType;

@RunWith( PowerMockRunner.class )
@PrepareForTest( {DbConnectionFactory.class} )
public class DbConnectionFactoryTest
{
    private DbConnectionFactory instance;
    private static final String DB_NAME = "testDb";
    private static final String SCHEMA_NAME = "testSchema";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWD = "";

    @SuppressWarnings( {"rawtypes", "unchecked"} )
    @After
    public void tearDown()
        throws Exception
    {
        Field field = Whitebox.getField( DbConnectionFactory.class, "instanceTable" );
        Hashtable<DbType, DbConnectionFactory> instanceTable = (Hashtable)field.get( instance );
        instanceTable.clear();
    }

    @Test
    public void testGetInstanceReturnsH2Instance()
        throws Exception
    {
        instance = DbConnectionFactory.instance( DbType.H2 );
        instance.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        Field field = Whitebox.getField( DbConnectionFactory.class, "instanceTable" );
        @SuppressWarnings( {"unchecked", "rawtypes"} )
        Hashtable<DbType, DbConnectionFactory> instanceTable = (Hashtable)field.get( instance );
        DbConnectionFactory expected = instanceTable.get( DbType.H2 );

        Assert.assertSame( expected, instance );
    }

    @Test
    public void testInitializationThrowsException()
        throws Exception
    {
        DbConnectionFactory mockFactory = PowerMock.createMockAndExpectNew( DbConnectionFactory.class,
                                                                            EasyMock.anyObject( DbType.class ) );

        mockFactory.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );
        PowerMock.expectLastCall().andThrow( new DbConnectionFactoryException() );

        PowerMock.replayAll();

        boolean thrown = false;
        try {
            mockFactory = DbConnectionFactory.instance( DbType.H2 );
            mockFactory.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );
        } catch( DbConnectionFactoryException e ) {
            thrown = true;
        }

        PowerMock.verifyAll();

        Assert.assertTrue( "DbConnectionFactoryException should have been thrown", thrown );
    }

    @Test
    public void testIsDbInitializedReturnsTrue()
        throws Exception
    {
        DbConnectionFactory mockFactory = PowerMock.createPartialMock( DbConnectionFactory.class,
                                                                       "init",
                                                                       "getConnection" );
        PowerMock.expectNew( DbConnectionFactory.class, EasyMock.anyObject( DbType.class ) )
                 .andReturn( mockFactory );
        mockFactory.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        SQLFactory mockSqlFactory = PowerMock.createMock( SQLFactory.class );

        Connection mockCon = PowerMock.createMock( Connection.class );
        Statement mockStmt = PowerMock.createMock( Statement.class );
        ResultSet mockRs = PowerMock.createMock( ResultSet.class );

        EasyMock.expect( mockFactory.getConnection() ).andReturn( mockCon );
        EasyMock.expect( mockCon.createStatement() ).andReturn( mockStmt );
        EasyMock.expect( mockStmt.executeQuery( EasyMock.isA( String.class ) ) ).andReturn( mockRs );
        EasyMock.expect( mockSqlFactory.testIsDbInitializedSQL() ).andReturn( "" );
        EasyMock.expect( mockRs.next() ).andReturn( true );

        mockRs.close();
        mockStmt.close();
        mockCon.close();

        PowerMock.replayAll();

        instance = DbConnectionFactory.instance( DbType.H2 );
        instance.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        Whitebox.setInternalState( instance, SQLFactory.class, mockSqlFactory );
        boolean isInited = instance.isDbInitialized();

        PowerMock.verifyAll();

        Assert.assertTrue( isInited );
    }

    @Test
    public void testGetConnection()
        throws Exception
    {
        DbConnectionFactory mockFactory = PowerMock.createPartialMock( DbConnectionFactory.class,
                                                                       "init" );
        PowerMock.expectNew( DbConnectionFactory.class, EasyMock.anyObject( DbType.class ) )
                 .andReturn( mockFactory );
        mockFactory.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        SQLFactory mockSqlFactory = PowerMock.createMock( SQLFactory.class );
        Connection mockCon = PowerMock.createMock( Connection.class );
        EasyMock.expect( mockSqlFactory.getPooledConnection() ).andReturn( mockCon );

        PowerMock.replayAll();

        instance = DbConnectionFactory.instance( DbType.H2 );
        instance.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        Whitebox.setInternalState( instance, SQLFactory.class, mockSqlFactory );
        instance.getConnection();

        PowerMock.verifyAll();
    }

    @Test
    public void testGetDataSource()
        throws Exception
    {
        DbConnectionFactory mockFactory = PowerMock.createPartialMock( DbConnectionFactory.class,
                                                                       "init" );
        PowerMock.expectNew( DbConnectionFactory.class, EasyMock.anyObject( DbType.class ) )
                 .andReturn( mockFactory );
        mockFactory.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        SQLFactory mockSqlFactory = PowerMock.createMock( SQLFactory.class );
        DataSource mockDs = PowerMock.createMock( DataSource.class );
        EasyMock.expect( mockSqlFactory.getDataSource() ).andReturn( mockDs );

        PowerMock.replayAll();

        instance = DbConnectionFactory.instance( DbType.H2 );
        instance.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        Whitebox.setInternalState( instance, SQLFactory.class, mockSqlFactory );
        instance.getDataSource();

        PowerMock.verifyAll();
    }

    @Test
    public void testInitializeDatabase()
        throws Exception
    {
        DbConnectionFactory mockFactory = PowerMock.createPartialMock( DbConnectionFactory.class,
                                                                       "init",
                                                                       "getConnection",
                                                                       "isDbInitialized",
                                                                       "loadSchema" );
        PowerMock.expectNew( DbConnectionFactory.class, EasyMock.anyObject( DbType.class ) )
                 .andReturn( mockFactory );
        mockFactory.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        SQLFactory mockSqlFactory = PowerMock.createMock( SQLFactory.class );

        Connection mockCon = PowerMock.createMock( Connection.class );
        Statement mockStmt = PowerMock.createMock( Statement.class );

        EasyMock.expect( mockFactory.getConnection() ).andReturn( mockCon );
        EasyMock.expect( mockCon.createStatement() ).andReturn( mockStmt );
        EasyMock.expect( mockStmt.execute( EasyMock.isA( String.class ) ) ).andReturn( true );

        PowerMock.expectPrivate( mockFactory,
                                 "loadSchema",
                                 EasyMock.anyObject( String.class ),
                                 EasyMock.anyObject( String.class ) )
                 .andReturn( new StringBuilder() )
                 .times( 4 );

        EasyMock.expect( mockFactory.isDbInitialized() ).andReturn( true );

        mockStmt.close();
        mockCon.close();

        PowerMock.replayAll();

        DbType dbType = DbType.H2;
        instance = DbConnectionFactory.instance( dbType );
        instance.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        Whitebox.setInternalState( instance, DbType.class, dbType );
        Whitebox.setInternalState( instance, SQLFactory.class, mockSqlFactory );
        instance.initializeDatabase();

        PowerMock.verifyAll();
    }

    @Test
    public void testLoadSchema()
        throws Exception
    {
        DbConnectionFactory mockFactory = PowerMock.createPartialMock( DbConnectionFactory.class,
                                                                       "init" );
        PowerMock.expectNew( DbConnectionFactory.class, EasyMock.anyObject( DbType.class ) )
                 .andReturn( mockFactory );
        mockFactory.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        PowerMock.replayAll();

        DbType dbType = DbType.H2;
        instance = DbConnectionFactory.instance( dbType );
        instance.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        Whitebox.setInternalState( instance, DbType.class, dbType );
        StringBuilder actual = Whitebox.invokeMethod( instance,
                                                      "loadSchema",
                                                      dbType.toString()
                                                              + "_init.sql",
                                                      SCHEMA_NAME );

        Set<Field> fields = Whitebox.getAllStaticFields( DbConnectionFactory.class );
        for( Field f : fields ) {
            if( "DB_SCHEMA_SQL".equals( f.getName() ) ) {
                actual.append( Whitebox.invokeMethod( instance,
                                                      "loadSchema",
                                                      f.get( instance ),
                                                      SCHEMA_NAME ) );
                break;
            }
        }

        PowerMock.verifyAll();
    }

    @Test
    public void testClose()
        throws Exception
    {
        DbConnectionFactory mockFactory = PowerMock.createPartialMockAndInvokeDefaultConstructor( DbConnectionFactory.class,
                                                                                                  "init",
                                                                                                  "getConnection" );
        PowerMock.expectNew( DbConnectionFactory.class, EasyMock.anyObject( DbType.class ) )
                 .andReturn( mockFactory );
        mockFactory.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        SQLFactory mockSqlFactory = PowerMock.createMock( SQLFactory.class );
        EasyMock.expect( mockSqlFactory.getPreShutdownCommand() ).andReturn( "" );

        Connection mockCon = PowerMock.createMock( Connection.class );
        Statement mockStmt = PowerMock.createMock( Statement.class );

        EasyMock.expect( mockFactory.getConnection() ).andReturn( mockCon );
        EasyMock.expect( mockCon.createStatement() ).andReturn( mockStmt );
        EasyMock.expect( mockStmt.execute( EasyMock.isA( String.class ) ) ).andReturn( true );

        mockSqlFactory.close();

        mockStmt.close();
        mockCon.close();

        PowerMock.replayAll();

        DbType dbType = DbType.H2;
        instance = DbConnectionFactory.instance( dbType );
        instance.init( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD );

        Whitebox.setInternalState( instance, DbType.class, dbType );
        Whitebox.setInternalState( instance, SQLFactory.class, mockSqlFactory );

        instance.close();

        PowerMock.verifyAll();
    }
}

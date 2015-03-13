package com.rcoe.da.dbconnector;

import java.io.File;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.easymock.EasyMock;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith( PowerMockRunner.class )
@PrepareForTest( {H2DataSourceFactory.class} )
public class H2DataSourceFactoryTest
{
    private static final String DB_NAME = "testDb";
    private static final String SCHEMA_NAME = "testSchema";
    private static final String DB_USER = "sa";
    private static final String DB_PASSWD = "";

    @Test
    public void testInvalidLocationIgnored()
        throws Exception
    {
        // setup data
        String location = "non-existant-directory";
        File mockFile = PowerMock.createMock( File.class );
        JdbcDataSource mockDs = PowerMock.createMock( JdbcDataSource.class );
        InitialContext mockCtx = PowerMock.createMock( InitialContext.class );

        // setup expectations
        PowerMock.expectNiceNew( File.class, EasyMock.isA( String.class ) ).andReturn( mockFile );
        EasyMock.expect( mockFile.isDirectory() ).andReturn( false );

        PowerMock.expectNew( JdbcDataSource.class ).andReturn( mockDs );
        mockDs.setURL( EasyMock.isA( String.class ) );
        mockDs.setUser( EasyMock.isA( String.class ) );
        mockDs.setPassword( EasyMock.isA( String.class ) );

        PowerMock.expectNew( InitialContext.class ).andReturn( mockCtx );
        mockCtx.rebind( EasyMock.isA( String.class ), EasyMock.isA( DataSource.class ) );

        PowerMock.replayAll();

        // exercise
        new H2DataSourceFactory( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD, location );

        // verify behaviour
        PowerMock.verifyAll();
    }

    @Test
    public void testValidLocationOverridesDefault()
        throws Exception
    {
        String location = "/valid/path/to/location";

        H2DataSourceFactory mockFactory = PowerMock.createMock( H2DataSourceFactory.class );

        File mockFile = PowerMock.createMock( File.class );
        PowerMock.expectNiceNew( File.class, EasyMock.isA( String.class ) ).andReturn( mockFile );
        EasyMock.expect( mockFile.isDirectory() ).andReturn( true );

        PowerMock.expectPrivate( mockFactory, (Object[])null ).once();

        PowerMock.replayAll();

        new H2DataSourceFactory( DB_NAME, SCHEMA_NAME, DB_USER, DB_PASSWD, location );

        PowerMock.verifyAll();
    }
}

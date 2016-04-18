package com.rcoe.dbconnector.common;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

@RunWith( PowerMockRunner.class )
@PrepareForTest( {Log4jConfigLoader.class} )
@SuppressStaticInitializationFor( {"Log4jConfigLoader"} )
public class Log4jConfigLoaderTest
{
    private static List<String> log_properties = new ArrayList<String>();

    static {
        log_properties.add( "log4j.rootLogger=info, R" );
        log_properties.add( "" );
        log_properties.add( "log4j.appender.stdout=org.apache.log4j.ConsoleAppender" );
        log_properties.add( "log4j.appender.stdout.layout=org.apache.log4j.PatternLayout" );
        log_properties.add( "# Pattern to output the caller's file name and line number." );
        log_properties.add( "log4j.appender.stdout.layout.ConversionPattern=%5p [%t] (%F:%L) - %m%n" );
        log_properties.add( "log4j.appender.R=org.apache.log4j.RollingFileAppender" );
        log_properties.add( "log4j.appender.R.File=cadmportal.log" );
        log_properties.add( "log4j.appender.R.MaxFileSize=100KB" );
        log_properties.add( "# Keep one backup file" );
        log_properties.add( "log4j.appender.R.MaxBackupIndex=1" );
        log_properties.add( "log4j.appender.R.layout=org.apache.log4j.PatternLayout" );
        log_properties.add( "log4j.appender.R.layout.ConversionPattern=%p %t %c - %m%n" );
    }

    @Test
    public void testLoggerInitialization()
        throws Exception
    {
        Log4jConfigLoader mockConfigLoader = PowerMock.createPartialMock( Log4jConfigLoader.class,
                                                                          new String[] {"loadLogProperties",
                                                                                        "initMe"},
                                                                          "info" );
        String loglevel = "debug";
        Whitebox.setInternalState( mockConfigLoader, "loglevel", loglevel );

        PowerMock.expectNew( Log4jConfigLoader.class, EasyMock.isA( String.class ) )
                 .andReturn( mockConfigLoader );
        PowerMock.expectPrivate( mockConfigLoader, "initMe" );
        PowerMock.expectPrivate( mockConfigLoader, "loadLogProperties" ).andReturn( log_properties );

        PowerMock.replayAll();

        Log4jConfigLoader loader = new Log4jConfigLoader( "info" );
        loader.initializeLogger( getClass().getPackage().getName() );

        PowerMock.verifyAll();

        Level lev = LogManager.getLoggerRepository().getRootLogger().getLevel();

        Assert.assertEquals( lev.toString().toLowerCase(), loglevel );
    }

    @Test
    @PrepareForTest( {Log4jConfigLoader.class, IOUtils.class} )
    public void testLoadingProperties()
        throws Exception
    {
        InputStream mockIs = PowerMock.createNiceMock( InputStream.class );
        PowerMock.mockStatic( Log4jConfigLoader.class );
        EasyMock.expect( Log4jConfigLoader.class.getResourceAsStream( EasyMock.isA( String.class ) ) )
                .andReturn( mockIs );

        PowerMock.mockStatic( IOUtils.class );
        EasyMock.expect( IOUtils.readLines( EasyMock.anyObject( InputStream.class ),
                                            EasyMock.isA( String.class ) ) )
                .andReturn( log_properties );
        IOUtils.closeQuietly( mockIs );

        PowerMock.replayAll();

        Log4jConfigLoader loader = new Log4jConfigLoader( "info" );
        loader.loadLogProperties();

        PowerMock.verifyAll();
    }

}

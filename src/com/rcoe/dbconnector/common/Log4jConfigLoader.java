package com.rcoe.dbconnector.common;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PropertyConfigurator;

import com.rcoe.dbconnector.VersionInfo;

public class Log4jConfigLoader
{
    // this should be static but the testing framework wouldn't initialise it
    private static final Pattern ROOTLOGGER_PATTERN = Pattern.compile( "log4j.rootLogger\\s*=\\s*(.*)\\s*,\\s*.*" );

    private static boolean doInit = true;
    private boolean rootLoggerConfigured = false;
    private String loglevel = "info";
    private String basepath;

    public Log4jConfigLoader( String level )
    {
        loglevel = level;
    }

    public void initializeLogger( String log4jPackage )
    {
        String logdir = System.getProperty( "log.dir", System.getProperty( "user.dir" )
                                                       + File.separator
                                                       + "logs" );

        initializeLogger( log4jPackage, logdir );
    }

    public synchronized void initializeLogger( String log4jPackage, String logdir )
    {
        setLogPath( logdir );

        initMe();

        basepath = "/"
                   + log4jPackage.replaceAll( "\\.", "/" )
                   + "/";

        try {
            List<String> log4j_input = loadLogProperties();

            Properties props = new Properties();
            for ( String s : log4j_input ) {
                int pos = s.indexOf( "=" );
                if( pos > 0 ) {
                    s = rootLevelOverride( s );
                    String key = s.substring( 0, pos ).trim();
                    String value = s.substring( pos + 1 ).trim();
                    props.put( key, value );
                }
            }

            // force the logger to reset
            props.put( "log4j.reset", "true" );

            PropertyConfigurator.configure( props );

            consolePrintStatus( log4jPackage );
        } catch( IOException e ) {
            System.err.println( "Failed to load log properties file.  "
                                + e.getMessage() );
        }
    }

    private void setLogPath( String logdir )
    {
        Path root = Paths.get( "/" );
        Path logdirPath;
        try {
            logdirPath = Files.createDirectories( root.resolve( logdir ) );
            System.setProperty( "log.dir", logdirPath.normalize().toString() );
        } catch( IOException e ) {
            File home = new File( System.getProperty( "user.dir" ) );
            logdirPath = home.toPath().resolve( home.toPath() );
            System.setProperty( "log.dir", logdirPath.normalize().toString() );
            System.err.printf( "Failed to create specified log directory, %s.  Defaulting to %s.\n",
                               logdir,
                               logdirPath.toString() );
        }
    }

    public synchronized void initMe()
    {
        if( doInit ) {
            doInit = false;

            Log4jConfigLoader cfgLoader = new Log4jConfigLoader( "info" );
            String thisPkg = Log4jConfigLoader.class.getPackage().getName();
            cfgLoader.initializeLogger( thisPkg );
            cfgLoader.printLogMode();

            new VersionInfo().printVersionInfo();
        }
    }

    private String rootLevelOverride( String s )
    {
        if( loglevel != null
            && !rootLoggerConfigured ) {
            Matcher m = ROOTLOGGER_PATTERN.matcher( s );
            if( m.matches() ) {
                s = s.replaceFirst( m.group( 1 ), loglevel.toUpperCase() );
                rootLoggerConfigured = true;
            }
        }
        return s;
    }

    List<String> loadLogProperties()
        throws IOException
    {
        InputStream is = null;
        List<String> log4j_input = null;
        try {
            is = getClass().getResourceAsStream( basepath
                                                 + "log4j.properties" );

            log4j_input = IOUtils.readLines( is, "UTF-8" );
        } finally {
            IOUtils.closeQuietly( is );
        }

        return log4j_input;
    }

    private void printLogMode()
    {
        System.out.printf( "Log files located in %s.\n", System.getProperty( "log.dir" ) );
    }

    private void consolePrintStatus( String log4jPackage )
    {
        if( log4jPackage == null || log4jPackage.length() == 0 ) {
            return;
        }

        Level lev = LogManager.getLoggerRepository().getRootLogger().getLevel();
        String libname = log4jPackage.substring( log4jPackage.lastIndexOf( '.' ) + 1 );
        libname = libname.replace( libname.charAt( 0 ), Character.toUpperCase( libname.charAt( 0 ) ) );
        System.out.printf( "%s application logger configured in %s mode.\n",
                           libname,
                           lev.toString() );
    }

    public void setLoglevel( String loglevel )
    {
        this.loglevel = loglevel;
    }
}

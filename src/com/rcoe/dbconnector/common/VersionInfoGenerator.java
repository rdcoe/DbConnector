package com.rcoe.dbconnector.common;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Properties;

public class VersionInfoGenerator
{
    public static void main( String[] args )
        throws Exception
    {   
        String[] fileArgs = args[0].split( " " );
        String buildProps = fileArgs[0].split( "=" )[1];
        String versionTemplate = fileArgs[1].split( "=" )[1];
        
        File propsFile = new File( buildProps );
        Properties props = loadVersionProperties( propsFile );
        StringBuilder source = loadSourceFile( versionTemplate );
        replaceFields( source, props );
        String classPath = propsFile.getParent();
        writeBack( source.toString(), classPath );
    }

    /*
     * This method is used when building the common library itself.
     */
    private static Properties loadVersionProperties( File propsFile )
        throws Exception
    {
        Properties props = new Properties();

        try( Reader r = new FileReader( propsFile ) ) {
            props.load( r );
            return props;
        } catch( IOException e ) {
            throw new Exception( "Could not find project.properties file in the class path." );
        }
    }

    /*
     * This method is used when building other projects that use the version.xml build step
     */
    private static StringBuilder loadSourceFile( String classPath )
        throws FileNotFoundException, IOException
    {
        File sourceFile = new File( classPath );
        StringBuilder sb = new StringBuilder();
        
        try( BufferedReader reader = new BufferedReader( new FileReader( sourceFile ) ) ) {
            load( sb, reader );
        }

        return sb;
    }

    private static void load( StringBuilder sb, BufferedReader reader )
        throws IOException
    {
        String line = null;
        while( ( line = reader.readLine() ) != null ) {
            sb.append( line ).append( "\n" );
        }
    }

    private static void replaceFields( StringBuilder source, Properties props )
    {
        String[] fields = {"%appname%",
                           "%version%",
                           "%buildnum%",
                           "%buildtime%",
                           "%system%",
                           "%builder%"};

        for( String field : fields ) {
            int index = source.indexOf( field );
            String key = field.substring( 1, field.length() - 1 );
            if( index != -1 ) {
                source.replace( index, index
                                       + field.length(), props.getProperty( key ) );
            }
        }
    }

    private static void writeBack( String source, String classPath )
        throws IOException
    {
        System.out.println(classPath);
        File sourceFile = new File( classPath, "VersionInfo.java" );
        try( BufferedWriter br = Files.newBufferedWriter( sourceFile.toPath(),
                                                          java.nio.charset.StandardCharsets.US_ASCII ) ) {
            br.write( source );
        }
    }

}

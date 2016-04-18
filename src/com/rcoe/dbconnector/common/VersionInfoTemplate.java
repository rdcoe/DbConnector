package com.rcoe.dbconnector.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionInfoTemplate
{
    private final static Logger logger = LoggerFactory.getLogger( "VersionInfo" );

    // These values are replaced by the build script during compilation
    public static final String APPNAME = "%appname%";
    public static final String VERSION = "%version%";
    public static final String BUILD = "%buildnum%";
    public static final String BUILDTIME = "%buildtime%";
    public static final String SYSTEM = "%system%";
    public static final String BUILDER = "%builder%";

    public static void printVersionInfo()
    {
        logger.info( "Application: {}", APPNAME );
        logger.info( "...version: {}", VERSION );
        logger.info( "...build id: {}", BUILD );
        logger.info( "...built on: {}", BUILDTIME );
        logger.info( "...platform: {}", SYSTEM );
        logger.info( "...builder: {}", BUILDER );
    }
}
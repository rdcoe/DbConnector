package com.rcoe.da.dbconnector;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class EnvironmentFactory
{
    public static void setNamingContext()
    {
        try {
            InitialContext.doLookup( "java:" );
            return;
        } catch( NamingException e1 ) {
            // expected when unit tests are running outside of a JNDI container
        }
        
        // Create initial context
        System.setProperty( Context.INITIAL_CONTEXT_FACTORY,
                            "org.apache.commons.naming.java.javaURLContextFactory" );
        System.setProperty( Context.URL_PKG_PREFIXES, "org.apache.commons.naming" );

        try {
            InitialContext ctx = new InitialContext();
            ctx.createSubcontext( "java:" );
            ctx.createSubcontext( "java:/comp" );
            ctx.createSubcontext( "java:/comp/env" );
            ctx.createSubcontext( "java:/comp/env/jdbc" );
        } catch( NamingException e ) {
            System.out.println( "Exception thrown when setting initial jndi java: context." );
            e.printStackTrace();
        }
    }
}

package com.comdev.da.persist;

import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.dbutils.DbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.comdev.da.auth.Identity;

/**
 * The DbConnectionMonitor is responsible for holding references to all open database connections.
 * This class can be used to visualise connections, as well as forcibly close connections.
 * 
 * @author rcoe
 * 
 */
public class DbConnectionMonitor
{
    private final Logger logger = LoggerFactory.getLogger( getClass() );
    private final ConcurrentHashMap<Identity, Set<Connection>> openConnections;
    private Map<Connection, Statement> activeStatements = new HashMap<Connection, Statement>();

    DbConnectionMonitor()
    {
        openConnections = new ConcurrentHashMap<Identity, Set<Connection>>();
    }

    synchronized void add( Identity identity, Connection connection )
    {
        Set<Connection> connections = openConnections.get( identity );
        if( connections == null ) {
            connections = Collections.synchronizedSet( new HashSet<Connection>() );
            openConnections.put( identity, connections );
        }
        connections.add( connection );

        logger.debug( "{} has {} active connection(s).", new Object[] {identity.getName(),
                                                                       count( identity )} );
    }

    public void registerStatement( Connection conn, Statement stmt )
    {
        activeStatements.put( conn, stmt );
    }

    /**
     * The count method will return the number of connections that are currently held open by an
     * {@link Identity}. The implementation checks each connection in the user's map, because pooled
     * connections that are closed are replaced by connection proxies.
     * 
     * @param identity
     * @return
     */
    int count( Identity identity )
    {
        int count = 0;

        Set<Connection> userConnections = openConnections.get( identity );
        if( userConnections != null ) {
            Iterator<Connection> it = openConnections.get( identity ).iterator();
            while( it.hasNext() ) {
                Connection conn = it.next();
                try {
                    if( !conn.isClosed() ) {
                        count++;
                    }
                } catch( Exception e ) {}
            }
        }

        return count;
    }

    /**
     * The count method will return a count of all active Connections.
     * 
     * @return
     */
    int count()
    {
        int count = 0;

        Set<Identity> identities = openConnections.keySet();
        if( identities != null ) {
            for( Identity ident : identities ) {
                count += count( ident );
            }
        }

        return count;
    }

    /**
     * The closeAll method will roll back and close any open connections held by {@link Identity}.
     * 
     * @param identity
     */
    synchronized void closeConnections( Identity identity )
    {
        if( openConnections.get( identity ) != null ) {

            Iterator<Connection> it = openConnections.get( identity ).iterator();
            while( it.hasNext() ) {
                try {
                    Connection conn = it.next();
                    if( !conn.isClosed() ) {
                        logger.debug( "Closing active connections held for {}.", identity.getName() );
                        long start = System.currentTimeMillis();

                        Statement stmt = activeStatements.get( conn );
                        it.remove();
                        activeStatements.remove( conn );
                        if( !stmt.isClosed() ) {
                            stmt.cancel();
                            DbUtils.closeQuietly( stmt );
                        }
                        DbUtils.rollbackAndCloseQuietly( conn );

                        long end = System.currentTimeMillis();

                        logger.debug( "Canceled running statement and closed connection in {} ms.",
                                      ( end - start ) );
                    }
                } catch( Exception e ) {}
            }
        }
    }

    void closeAll()
    {
        Set<Identity> identities = openConnections.keySet();
        if( identities != null ) {
            for( Identity ident : identities ) {
                closeConnections( ident );
            }
        }
    }
}

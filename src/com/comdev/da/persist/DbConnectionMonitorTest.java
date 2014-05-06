package com.comdev.da.persist;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;

import org.easymock.EasyMock;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import com.comdev.da.auth.Identity;
import com.comdev.da.auth.IdentityImpl;

@RunWith( PowerMockRunner.class )
@PrepareForTest( {DbConnectionMonitor.class} )
public class DbConnectionMonitorTest
{
    @Test
    public final void testAddOnFirstConnection()
        throws SQLException
    {
        Connection mockConn = PowerMock.createMock( Connection.class );
        EasyMock.expect( mockConn.isClosed() ).andReturn( false ).anyTimes();

        PowerMock.replayAll();

        DbConnectionMonitor monitor = new DbConnectionMonitor();

        Identity ident = new IdentityImpl();
        ident.setName( "unit.test" );

        monitor.add( ident, mockConn );
        int count = monitor.count( ident );

        PowerMock.verifyAll();

        Assert.assertEquals( "Monitor should be holding 1 connection.", 1, count );
    }

    @Test
    public final void testCountReturnsZeroWhenNoConnectionsOpen()
        throws SQLException
    {
        Connection mockConn = PowerMock.createMock( Connection.class );
        EasyMock.expect( mockConn.isClosed() ).andReturn( true ).anyTimes();

        PowerMock.replayAll();

        DbConnectionMonitor monitor = new DbConnectionMonitor();

        Identity ident = new IdentityImpl();
        ident.setName( "unit.test" );

        monitor.add( ident, mockConn );
        int count = monitor.count( ident );

        PowerMock.verifyAll();

        Assert.assertEquals( "Monitor should be holding 0 connections.", 0, count );
    }

    @Test
    public final void testCountReturnsZeroConnectionsAreToldToClose()
        throws SQLException
    {
        Connection mockConn = PowerMock.createNiceMock( Connection.class );
        Statement mockStmt = PowerMock.createMock( Statement.class );

        EasyMock.expect( mockConn.isClosed() ).andReturn( false );
        EasyMock.expect( mockStmt.isClosed() ).andReturn( false );

        mockStmt.cancel();
        EasyMock.expectLastCall().times( 1 );
        mockStmt.close();
        EasyMock.expectLastCall().times( 1 );
        mockConn.rollback();
        EasyMock.expectLastCall().times( 1 );
        mockConn.close();
        EasyMock.expectLastCall().times( 1 );

        PowerMock.replayAll();

        DbConnectionMonitor monitor = new DbConnectionMonitor();

        Identity ident = new IdentityImpl();
        ident.setName( "unit.test" );

        monitor.add( ident, mockConn );
        monitor.registerStatement( mockConn, mockStmt );
        monitor.closeConnections( ident );
        int count = monitor.count( ident );

        PowerMock.verifyAll();

        Assert.assertEquals( "Monitor should be holding 0 connections.", 0, count );
    }

    @Test
    public final void testCloseAllClearsActiveConnections()
        throws SQLException
    {
        int loopCount = 2;
        Connection[] connections = new Connection[loopCount];
        Statement[] statements = new Statement[loopCount];

        DbConnectionMonitor monitor = new DbConnectionMonitor();
        ConcurrentHashMap<Identity, Set<Connection>> openConnections = new ConcurrentHashMap<Identity, Set<Connection>>();
        Map<Connection, Statement> activeStatements = new HashMap<Connection, Statement>();
        
        Identity ident = new IdentityImpl();
        ident.setName( "unit.test" );

        for( int i = 0; i < loopCount; i++ ) {
            Connection mockConn = PowerMock.createNiceMock( Connection.class );
            connections[i] = mockConn;

            Statement mockStmt = PowerMock.createNiceMock( Statement.class );
            statements[i] = mockStmt;
            activeStatements.put( mockConn, mockStmt );
        }
        openConnections.put( ident, new HashSet<Connection>( Arrays.asList( connections ) ) );
        
        Whitebox.setInternalState( monitor, "openConnections", openConnections );
        Whitebox.setInternalState( monitor, "activeStatements", activeStatements );

        // expectations when closeAll is called
        for( int i = 0; i < loopCount; i++ ) {
            EasyMock.expect( connections[i].isClosed() ).andReturn( false ).anyTimes();
            EasyMock.expect( statements[i].isClosed() ).andReturn( false ).anyTimes();
            statements[i].cancel();
            statements[i].close();
            connections[i].rollback();
            connections[i].close();
        }

        PowerMock.replayAll();

        Assert.assertEquals( "Monitor should be holding 5 connections.", loopCount, monitor.count() );

        monitor.closeAll();

        Assert.assertEquals( "Monitor should be holding 0 connections.", 0, monitor.count() );

        PowerMock.verifyAll();
    }
}

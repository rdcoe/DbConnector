package com.rcoe.dbconnector.query;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.rcoe.dbconnector.DbConnectionFactory;
import com.rcoe.dbconnector.auth.Identity;
import com.rcoe.dbconnector.auth.IdentityImpl;
import com.rcoe.dbconnector.common.Persistable;
import com.rcoe.dbconnector.tests.stubs.SelectQueryStub;

import junit.framework.Assert;

@RunWith( PowerMockRunner.class )
@PrepareForTest( {SelectOneResultSetHandler.class} )
public class SelectOneResultSetHandlerTest
{
    private AbstractSelectQuery<Persistable, String> stubQuery;

    @Before
    public void setUp()
    {
        Identity identity = new IdentityImpl();
        identity.setName( "stubQuery" );

        DbConnectionFactory mockFactory = PowerMock.createMock( DbConnectionFactory.class );
        stubQuery = new SelectQueryStub( mockFactory, identity );
    }

    @Test
    public void testDTOReturnedWhenResultSetNotEmpty()
        throws SQLException
    {
        // setup data
        SelectOneResultSetHandler<Persistable, String> handler = new SelectOneResultSetHandler<Persistable, String>( stubQuery );

        ResultSet mockRs = PowerMock.createMock( ResultSet.class );

        // setup expectations
        EasyMock.expect( mockRs.next() ).andReturn( true );
        mockRs.close();
        EasyMock.expectLastCall().times( 1 );

        PowerMock.replayAll();

        // exercise
        Persistable actual = handler.handle( mockRs );

        // verify results
        PowerMock.verifyAll();

        Assert.assertNotNull( actual );
    }

    @Test
    public void testNullDTOWhenResultSetEmpty()
        throws SQLException
    {
        // setup data
        SelectOneResultSetHandler<Persistable, String> handler = new SelectOneResultSetHandler<Persistable, String>( stubQuery );

        ResultSet mockRs = PowerMock.createMock( ResultSet.class );

        // setup expectations
        EasyMock.expect( mockRs.next() ).andReturn( false );
        mockRs.close();
        EasyMock.expectLastCall().times( 1 );

        PowerMock.replayAll();

        // exercise
        Persistable actual = handler.handle( mockRs );

        // verify results
        PowerMock.verifyAll();

        Assert.assertNull( actual );
    }
}
